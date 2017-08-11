package wbs.integrations.shopify.apiclient.metafield;

import java.util.List;

import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClient;
import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.model.ShopifyMetafieldOwnerResource;

public
interface ShopifyMetafieldApiClient
	extends ShopifyApiClient <
		ShopifyMetafieldRequest,
		ShopifyMetafieldResponse
	> {

	List <ShopifyMetafieldResponse> listByOwner (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			ShopifyMetafieldOwnerResource ownerResource,
			Long ownerId)
		throws InterruptedException;

}
