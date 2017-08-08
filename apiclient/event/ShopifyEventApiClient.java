package wbs.integrations.shopify.apiclient.event;

import java.util.List;

import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;

public
interface ShopifyEventApiClient {

	List <ShopifyEventResponse> findSinceIdLimit (
			TaskLogger parentTaskLogger,
			ShopifyApiClientCredentials credentials,
			Long eventId,
			Long maxItems);

}
