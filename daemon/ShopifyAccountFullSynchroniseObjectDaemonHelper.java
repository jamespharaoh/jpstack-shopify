package wbs.integrations.shopify.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToSet;
import static wbs.utils.etc.EnumUtils.enumNameHyphens;
import static wbs.utils.etc.Misc.contains;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiLogic;
import wbs.integrations.shopify.apiclient.customcollection.ShopifyCustomCollectionApiClient;
import wbs.integrations.shopify.apiclient.product.ShopifyProductApiClient;
import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;
import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectObjectHelper;
import wbs.integrations.shopify.model.ShopifyEventSubjectRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectType;

import wbs.platform.daemon.ObjectDaemon;

import wbs.utils.random.RandomLogic;

@SingletonComponent ("shopifyAccountFullSynchroniseObjectDaemonHelper")
public
class ShopifyAccountFullSynchroniseObjectDaemonHelper
	implements ObjectDaemon <Long> {

	// singleton dependencies

	@SingletonDependency
	private
	Database database;

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	ObjectManager objectManager;

	@SingletonDependency
	private
	RandomLogic randomLogic;

	@SingletonDependency
	private
	ShopifyAccountObjectHelper shopifyAccountHelper;

	@SingletonDependency
	private
	ShopifyApiLogic shopifyApiLogic;

	@SingletonDependency
	private
	ShopifyCustomCollectionApiClient shopifyCustomCollectionApiClient;

	@SingletonDependency
	private
	ShopifyEventSubjectObjectHelper shopifyEventSubjectHelper;

	@SingletonDependency
	private
	ShopifyProductApiClient shopifyProductApiClient;

	// details

	@Override
	public
	String backgroundProcessName () {
		return "shopify-account.full-synchronise";
	}

	@Override
	public
	String itemNameSingular () {
		return "shopify account";
	}

	@Override
	public
	String itemNamePlural () {
		return "shopify account";
	}

	@Override
	public
	LogContext logContext () {
		return logContext;
	}

	// object daemon implementation

	@Override
	public
	List <Long> findObjectIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"findObjectIds");

		) {

			return shopifyAccountHelper.findPendingFullSynchroniseIds (
				transaction,
				transaction.now ());

		}

	}

	@Override
	public
	void processObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long accountId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processObject",
					keyEqualsDecimalInteger (
						"accountId",
						accountId));

		) {

			// get account data

			Optional <AccountData> accountDataOptional =
				getAccountData (
					taskLogger,
					accountId);

			if (
				optionalIsNotPresent (
					accountDataOptional)
			) {
				return;
			}

			AccountData accountData =
				optionalGetRequired (
					accountDataOptional);

			// get remote data

			ShopifyData shopifyData =
				new ShopifyData ()

				.customCollectionIds (
					shopifyCustomCollectionApiClient.listAllIds (
						taskLogger,
						accountData.credentials ()))

				.productIds (
					shopifyProductApiClient.listAllIds (
						taskLogger,
						accountData.credentials ()))

			;

			// update event subjects

			updateEventSubjects (
				taskLogger,
				accountId,
				shopifyData);

		}

	}

	// private implementation

	private
	Optional <AccountData> getAccountData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long accountId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getAccountData");

		) {

			ShopifyAccountRec account =
				shopifyAccountHelper.findRequired (
					transaction,
					accountId);

			if (account.getDeleted ()) {
				return optionalAbsent ();
			}

			return optionalOf (
				new AccountData ()

				.id (
					accountId)

				.credentials (
					shopifyApiLogic.getApiCredentials (
						transaction,
						account))

			);

		}

	}

	private
	void updateEventSubjects (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long accountId,
			@NonNull ShopifyData shopifyData) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"updateEventSubjects");

		) {

			ShopifyAccountRec account =
				shopifyAccountHelper.findRequired (
					transaction,
					accountId);

			if (account.getDeleted ()) {
				return;
			}

			updateEventSubjects (
				transaction,
				account,
				ShopifyEventSubjectType.collection,
				shopifyData.customCollectionIds ());

			updateEventSubjects (
				transaction,
				account,
				ShopifyEventSubjectType.product,
				shopifyData.productIds ());

			transaction.commit ();

		}

	}

	private
	void updateEventSubjects (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyAccountRec account,
			@NonNull ShopifyEventSubjectType subjectType,
			@NonNull List <Long> shopifyIds) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateEventSubjects");

		) {

			List <ShopifyEventSubjectRec> existingSubjects =
				shopifyEventSubjectHelper.findBySubjectType (
					transaction,
					account,
					subjectType);

			Set <Long> existingSubjectIds =
				iterableMapToSet (
					existingSubjects,
					ShopifyEventSubjectRec::getSubjectId);

			for (
				Long shopifyId
					: shopifyIds
			) {

				if (
					contains (
						existingSubjectIds,
						shopifyId)
				) {
					continue;
				}

				shopifyEventSubjectHelper.insert (
					transaction,
					shopifyEventSubjectHelper.createInstance ()

					.setAccount (
						account)

					.setSubjectType (
						subjectType)

					.setSubjectId (
						shopifyId)

					.setDeleted (
						false)

					.setPending (
						true)

				);

				transaction.noticeFormat (
					"Shopify full synchronise %s found %s %s",
					objectManager.objectPathMini (
						transaction,
						account),
					enumNameHyphens (
						subjectType),
					integerToDecimalString (
						shopifyId));

			}

			account.setNextFullSynchronise (
				transaction.now ().plus (
					randomLogic.randomDuration (
						Duration.standardDays (1l),
						Duration.standardHours (1l))));

			transaction.flush ();

		}

	}

	// data classes

	@Accessors (fluent = true)
	@Data
	private static
	class AccountData {

		Long id;

		ShopifyApiClientCredentials credentials;

	}

	@Accessors (fluent = true)
	@Data
	private static
	class ShopifyData {

		List <Long> customCollectionIds;
		List <Long> productIds;

	}

}
