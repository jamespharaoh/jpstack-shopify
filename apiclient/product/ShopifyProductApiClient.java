package wbs.integrations.shopify.apiclient.product;

import wbs.integrations.shopify.apiclient.ShopifyApiClient;

public
interface ShopifyProductApiClient
	extends ShopifyApiClient <
		ShopifyProductRequest,
		ShopifyProductResponse
	> {

}
