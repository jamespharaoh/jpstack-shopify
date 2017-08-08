package wbs.integrations.shopify.apiclient.product;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;

import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

@Accessors (fluent = true)
@Data
public
class ShopifyProductUpdateResponse
	implements ShopifyApiResponse {

	@DataChild (
		name = "product")
	ShopifyProductResponse product;

}
