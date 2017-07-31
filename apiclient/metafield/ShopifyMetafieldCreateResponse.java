package wbs.integrations.shopify.apiclient.metafield;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;

import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

@Accessors (fluent = true)
@Data
public
class ShopifyMetafieldCreateResponse
	implements ShopifyApiResponse {

	@DataChild (
		name = "metafield")
	ShopifyMetafieldResponse metafield;

}
