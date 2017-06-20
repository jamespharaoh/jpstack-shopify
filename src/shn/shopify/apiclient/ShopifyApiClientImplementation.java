package shn.shopify.apiclient;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.etc.Misc.lessThan;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import shn.shopify.model.ShnShopifyStoreRec;

@SingletonComponent ("shopifyApiClient")
public
class ShopifyApiClientImplementation
	implements ShopifyApiClient {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@PrototypeDependency
	@NamedDependency ("shopifyProductListHttpSender")
	ComponentProvider <GenericHttpSender <
		ShopifyProductListRequest,
		ShopifyProductListResponse
	>> productListHttpSenderProvider;

	// public implementation

	@Override
	public
	ShopifyApiClientCredentials getCredentials (
			@NonNull Transaction parentTransaction,
			@NonNull ShnShopifyStoreRec shopifyStore) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getCredentials");

		) {

			return new ShopifyApiClientCredentials ()

				.storeName (
					shopifyStore.getStoreName ())

				.username (
					shopifyStore.getApiKey ())

				.password (
					shopifyStore.getPassword ())

			;

		}

	}

	@Override
	public
	ShopifyProductListResponse listAllProducts (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"listProducts")

		) {

			ImmutableList.Builder <ShopifyProductListEntryResponse> builder =
				ImmutableList.builder ();

			for (
				long page = 0l;
				true;
				page ++
			) {

				ShopifyProductListResponse response =
					productListHttpSenderProvider.provide (
						taskLogger)

					.allInOne (
						taskLogger,
						new ShopifyProductListRequest ()

					.credentials (
						credentials)

					.limit (
						250l)

					.page (
						page)

				);

				builder.addAll (
					response.products);

				if (
					lessThan (
						collectionSize (
							response.products ()),
						250l)
				) {
					break;
				}

			}

			return new ShopifyProductListResponse ()

				.products (
					builder.build ())

			;

		}

	}

}