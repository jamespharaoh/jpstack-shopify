package wbs.integrations.shopify.apiclient;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.TypeUtils.classInSafe;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyMetafieldOwnerResource;

@SingletonComponent ("shopifyApiLogic")
public
class ShopifyApiLogicImplementation
	implements ShopifyApiLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
