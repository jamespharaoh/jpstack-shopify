package wbs.integrations.shopify.apiclient.productvariant;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;

import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

@Accessors (fluent = true)
@Data
public
class ShopifyProductVariantUpdateResponse
	implements ShopifyApiResponse {

	@DataChild (
		name = "variant")
	ShopifyProductVariantResponse variant;

}
