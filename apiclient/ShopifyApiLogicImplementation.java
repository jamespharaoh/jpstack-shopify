package wbs.integrations.shopify.apiclient;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountRec;

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

}
