package wbs.integrations.shopify.apiclient.collect;

import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;

public
interface ShopifyCollectApiClient {

	ShopifyCollectListResponse listAll (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials request);

	ShopifyCollectResponse create (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyCollectRequest request);

	ShopifyCollectResponse update (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyCollectRequest request);

	void remove (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			Long id);

}
