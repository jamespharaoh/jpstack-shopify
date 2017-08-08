package wbs.integrations.shopify.apiclient.customcollection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;

@Accessors (fluent = true)
@Data
public
class ShopifyCustomCollectionImageResponse {

	@DataAttribute (
		name = "created_at")
	String createdAt;

	@DataAttribute (
		name = "src")
	String src;

}
