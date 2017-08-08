package wbs.integrations.shopify.apiclient.customcollection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyCustomCollectionImageRequest {

	@DataAttribute (
		name = "attachment")
	String attachment;

	@DataAttribute (
		name = "src")
	String src;

}
