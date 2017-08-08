package wbs.integrations.shopify.apiclient.customcollection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;

import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

@Accessors (fluent = true)
@Data
public
class ShopifyCustomCollectionUpdateResponse
	implements ShopifyApiResponse {

	@DataChild (
		name = "custom_collection")
	ShopifyCustomCollectionResponse customCollection;

}
