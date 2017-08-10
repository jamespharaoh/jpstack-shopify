package wbs.integrations.shopify.apiclient;

import static wbs.framework.logging.TaskLogUtils.taskLogToString;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrNull;
import static wbs.utils.etc.TypeUtils.classInSafe;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.web.utils.UrlUtils.urlPath;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogEvent;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;
import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyMetafieldOwnerResource;
import wbs.integrations.shopify.model.ShopifyOutboundLogObjectHelper;

import wbs.web.utils.JsonUtils;

@SingletonComponent ("shopifyApiLogic")
public
class ShopifyApiLogicImplementation
	implements ShopifyApiLogic {

	// singleton dependencies

	@SingletonDependency
	ShopifyAccountObjectHelper accountHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShopifyOutboundLogObjectHelper outboundLogHelper;

	// public implementation

	@Override
	public
	ShopifyApiClientCredentials getApiCredentials (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyAccountRec shopifyAccount) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getApiCredentials");

		) {

			return new ShopifyApiClientCredentials ()

				.accountId (
					shopifyAccount.getId ())

				.storeName (
					shopifyAccount.getStoreName ())

				.username (
					shopifyAccount.getApiKey ())

				.password (
					shopifyAccount.getPassword ())

			;

		}

	}

	@Override
	public
	Object responseToLocal (
			@NonNull Transaction parentTransaction,
			@NonNull Object responseValue,
			@NonNull Class <?> localClass) {

		// convert string to instant

		if (

			classInSafe (
				responseValue.getClass (),
				String.class)

			&& classInSafe (
				localClass,
				Instant.class,
				ReadableInstant.class)

		) {

			return Instant.parse (
				dynamicCastRequired (
					String.class,
					responseValue));

		}

		// convert string to metafield owner resource

		if (

			classInSafe (
				responseValue.getClass (),
				String.class)

			&& classInSafe (
				localClass,
				ShopifyMetafieldOwnerResource.class)

		) {

			return mapItemForKeyRequired (
				stringToMetafieldOwnerResource,
				dynamicCastRequired (
					String.class,
					responseValue));

		}

		// return value unchanged

		return responseValue;

	}

	@Override
	public
	void createOutboundLog (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull TaskLogEvent taskLogEvent,
			@NonNull Long accountId,
			@NonNull GenericHttpSender <?, ?> httpSender) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createOutboundLog");

		) {

			ShopifyAccountRec account =
				accountHelper.findRequired (
					transaction,
					accountId);

			outboundLogHelper.insert (
				transaction,
				outboundLogHelper.createInstance ()

				.setTimestamp (
					transaction.now ())

				.setAccount (
					account)

				.setMethod (
					enumName (
						httpSender.helper ().method ()))

				.setPath (
					urlPath (
						httpSender.helper ().url ()))

				.setSuccess (
					httpSender.success ())

				.setLog (
					taskLogToString (
						"  ",
						taskLogEvent))

				.setRequest (
					optionalMapRequiredOrNull (
						optionalFromNullable (
							httpSender.requestTrace ()),
						JsonUtils::jsonEncode))

				.setResponse (
					optionalMapRequiredOrNull (
						optionalFromNullable (
							httpSender.responseTrace ()),
						JsonUtils::jsonEncode))

			);

			transaction.commit ();

		}

	}

	// static data

	Map <String, ShopifyMetafieldOwnerResource> stringToMetafieldOwnerResource =
		ImmutableMap.<String, ShopifyMetafieldOwnerResource> builder ()

		.put (
			"custom_collection",
			ShopifyMetafieldOwnerResource.customCollection)

		.put (
			"product",
			ShopifyMetafieldOwnerResource.product)

		.build ()

	;

}
