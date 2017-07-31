package wbs.integrations.shopify.daemon;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiLogic;
import wbs.integrations.shopify.apiclient.event.ShopifyEventApiClient;
import wbs.integrations.shopify.apiclient.event.ShopifyEventResponse;
import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;
import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyEventObjectHelper;
import wbs.integrations.shopify.model.ShopifyEventSubjectObjectHelper;
import wbs.integrations.shopify.model.ShopifyEventSubjectRec;

import wbs.platform.daemon.ObjectDaemon;

import wbs.utils.data.Pair;

@SingletonComponent ("shopifyAccountEventPollObjectDaemonHelper")
public
class ShopifyAccountEventPollObjectDaemonHelper
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
	ShopifyAccountObjectHelper shopifyAccountHelper;

	@SingletonDependency
	private
	ShopifyApiLogic shopifyApiLogic;

	@SingletonDependency
	private
	ShopifyEventApiClient shopifyEventApiClient;

	@SingletonDependency
	private
	ShopifyEventObjectHelper shopifyEventHelper;

	@SingletonDependency
	private
	ShopifyEventSubjectObjectHelper shopifyEventSubjectHelper;

	// details

	@Override
	public
	String backgroundProcessName () {
		return "shopify-account.event-poll";
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

			return iterableMapToList (
				shopifyAccountHelper.findNotDeleted (
					transaction),
				ShopifyAccountRec::getId);

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

			while (! Thread.interrupted ()) {

				Pair <Long, ShopifyApiClientCredentials>
					lastEventIdAndCredentials =
						getLastEventIdAndCredentials (
							taskLogger,
							accountId);

				Long lastEventId =
					lastEventIdAndCredentials.left ();

				ShopifyApiClientCredentials credentials =
					lastEventIdAndCredentials.right ();

				List <ShopifyEventResponse> eventResponses =
					shopifyEventApiClient.findSinceIdLimit (
						taskLogger,
						credentials,
						lastEventId,
						100l);

				storeEvents (
					taskLogger,
					accountId,
					eventResponses);

				if (
					lessThan (
						collectionSize (
							eventResponses),
						100l)
				) {
					break;
				}

			}

		}

	}

	// private implementation

	private
	Pair <Long, ShopifyApiClientCredentials> getLastEventIdAndCredentials (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long accountId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getLastEventId",
					keyEqualsDecimalInteger (
						"accountId",
						accountId));

		) {

			ShopifyAccountRec shopifyAccount =
				shopifyAccountHelper.findRequired (
					transaction,
					accountId);

			return Pair.of (
				shopifyAccount.getLastEventId (),
				shopifyApiLogic.getApiCredentials (
					transaction,
					shopifyAccount));

		}

	}

	private
	void storeEvents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long accountId,
			@NonNull List <ShopifyEventResponse> eventResponses) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"storeEvents")

		) {

			ShopifyAccountRec shopifyAccount =
				shopifyAccountHelper.findRequired (
					transaction,
					accountId);

			for (
				ShopifyEventResponse eventResponse
					: eventResponses
			) {

				shopifyEventHelper.insert (
					transaction,
					shopifyEventHelper.createInstance ()

					.setAccount (
						shopifyAccount)

					.setShopifyId (
						eventResponse.id ())

					.setPending (
						true)

					.setBody (
						eventResponse.body ())

					.setCreatedAt (
						Instant.parse (
							eventResponse.createdAt ()))

					.setDescription (
						eventResponse.description ())

					.setPath (
						eventResponse.path ())

					.setMessage (
						eventResponse.message ())

					.setSubjectId (
						eventResponse.subjectId ())

					.setSubjectType (
						eventResponse.subjectType ())

					.setVerb (
						eventResponse.verb ())

					// TODO arguments

				);

				shopifyAccount.setLastEventId (
					eventResponse.id ());

				Optional <ShopifyEventSubjectRec> eventSubjectOptional =
					shopifyEventSubjectHelper.findBySubjectTypeAndId (
						transaction,
						shopifyAccount,
						eventResponse.subjectType (),
						eventResponse.subjectId ());

				if (
					optionalIsPresent (
						eventSubjectOptional)
				) {

					ShopifyEventSubjectRec eventSubject =
						optionalGetRequired (
							eventSubjectOptional);

					if (
						moreThan (
							eventResponse.id (),
							eventSubject.getLastEventId ())
					) {

						eventSubject

							.setLastEventId (
								eventResponse.id ())

							.setLastEventVerb (
								eventResponse.verb ())

							.setPending (
								true)

						;

					}

				} else {

					shopifyEventSubjectHelper.insert (
						transaction,
						shopifyEventSubjectHelper.createInstance ()

						.setAccount (
							shopifyAccount)

						.setSubjectType (
							eventResponse.subjectType ())

						.setSubjectId (
							eventResponse.subjectId ())

						.setLastEventId (
							eventResponse.id ())

						.setLastEventVerb (
							eventResponse.verb ())

						.setPending (
							true)

					);

				}

				transaction.noticeFormat (
					"SHOPIFY %s %s %s %s",
					integerToDecimalString (
						eventResponse.id ()),
					eventResponse.subjectType (),
					integerToDecimalString (
						eventResponse.subjectId ()),
					eventResponse.verb ());

			}

			transaction.commit ();

		}

	}

}
