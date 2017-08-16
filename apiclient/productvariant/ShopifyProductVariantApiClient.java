package wbs.integrations.shopify.apiclient.productvariant;

import wbs.integrations.shopify.apiclient.ShopifyApiClient;

public
interface ShopifyProductVariantApiClient
	extends ShopifyApiClient <
		ShopifyProductVariantRequest,
		ShopifyProductVariantResponse
	> {

}
