package wbs.integrations.shopify.apiclient.metafield;

import java.util.List;

import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;

public
interface ShopifyMetafieldApiClient {

	List <ShopifyMetafieldResponse> listAll (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials);

	List <ShopifyMetafieldResponse> listByNamespaceAndOwnerResource (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			String namespace,
			String ownerResource);

	ShopifyMetafieldResponse create (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyMetafieldRequest request);

	ShopifyMetafieldResponse update (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyMetafieldRequest request);

	void remove (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			Long id);

}
