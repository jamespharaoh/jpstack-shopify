package wbs.integrations.shopify.apiclient.collect;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;

import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;

@Accessors (fluent = true)
@Data
public
class ShopifyCollectResponse
	implements ShopifyApiResponseItem {

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

	@DataAttribute (
		name = "created_at")
	String createdAt;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

}
