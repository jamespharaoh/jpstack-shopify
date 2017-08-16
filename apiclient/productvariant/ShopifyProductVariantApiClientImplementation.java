package wbs.integrations.shopify.apiclient.productvariant;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiLogic;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

@SingletonComponent ("shopifyProductVariantApiClient")
public
class ShopifyProductVariantApiClientImplementation
	implements ShopifyProductVariantApiClient {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShopifyApiLogic shopifyApiLogic;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("shopifyHttpSender")
	ComponentProvider <GenericHttpSender <
		ShopifyApiRequest,
		ShopifyApiResponse
	>> shopifyHttpSenderProvider;

	// public implementation

	@Override
	public
	ShopifyProductVariantResponse update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyProductVariantRequest productVariant)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"update");

		) {

			ShopifyProductVariantUpdateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,

					new ShopifyProductVariantUpdateRequest ()

						.httpCredentials (
							credentials)

						.variant (
							productVariant),

					shopifyApiLogic.createOutboundLog (
						taskLogger,
						credentials.accountId ())

				)

			);

			return response.variant ();

		}

	}

}
