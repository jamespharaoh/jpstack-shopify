package wbs.integrations.shopify.apiclient.product;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductImageRequest {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "attachment")
	String attachment;

	@DataAttribute (
		name = "src")
	String src;

}
