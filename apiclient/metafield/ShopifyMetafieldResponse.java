package wbs.integrations.shopify.apiclient.metafield;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;

import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;

@Accessors (fluent = true)
@Data
public
class ShopifyMetafieldResponse
	implements ShopifyApiResponseItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "owner_resource")
	String ownerResource;

	@DataAttribute (
		name = "owner_id")
	Long ownerId;

	@DataAttribute (
		name = "key")
	String key;

	@DataAttribute (
		name = "namespace")
	String namespace;

	@DataAttribute (
		name = "value")
	String value;

	@DataAttribute (
		name = "value_type")
	String valueType;

	@DataAttribute (
		name = "description")
	String description;

	@DataAttribute (
		name = "created_at")
	String createdAt;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

}
