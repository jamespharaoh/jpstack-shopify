package wbs.integrations.shopify.apiclient.collect;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiRequestItem;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyCollectRequest
	implements ShopifyApiRequestItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "collection_id")
	Long collectionId;

	@DataAttribute (
		name = "product_id")
	Long productId;

	@DataAttribute (
		name = "featured")
	Boolean featured;

}
