package wbs.integrations.shopify.apiclient.productimage;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;

@Accessors (fluent = true)
@Data
public
class ShopifyProductImageResponse {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "product_id")
	Long productId;

	@DataAttribute (
		name = "position")
	Long position;

	@DataAttribute (
		name = "created_at")
	String createdAt;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

	@DataAttribute (
		name = "src")
	String src;

	Long mediaId;

}
