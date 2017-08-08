package wbs.integrations.shopify.apiclient.product;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;

@Accessors (fluent = true)
@Data
public
class ShopifyProductVariantResponse {

	// id

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "product_id")
	Long productId;

	@DataAttribute (
		name = "position")
	Long position;

	// general information

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "sku")
	String sku;

	@DataAttribute (
		name = "barcode")
	String barcode;

	@DataAttribute (
		name = "image_id")
	Long imageId;

	// pricing

	@DataAttribute (
		name = "price")
	String price;

	@DataAttribute (
		name = "compare_at_price")
	String compareAtPrice;

	@DataAttribute (
		name = "taxable")
    Boolean taxable;

	// inventory

	@DataAttribute (
		name = "inventory_management")
	String inventoryManagement;

	@DataAttribute (
		name = "inventory_policy")
	String inventoryPolicy;

	@DataAttribute (
		name = "inventory_quantity")
	Long inventoryQuantity;

	@DataAttribute (
		name = "old_inventory_quantity")
	Long oldInventoryQuantity;

	// weight

	@DataAttribute (
		name = "grams")
	Long grams;

	@DataAttribute (
		name = "weight")
	Double weight;

	@DataAttribute (
		name = "weight_unit")
	String weightUnit;

	// delivery

	@DataAttribute (
		name = "fulfillment_service")
	String fulfillmentService;

	@DataAttribute (
		name = "requires_shipping")
    Boolean requiresShipping;

	// options

	@DataAttribute (
		name = "option1")
	String option1;

	@DataAttribute (
		name = "option2")
	String option2;

	@DataAttribute (
		name = "option3")
	String option3;

	// misc

	@DataAttribute (
		name = "created_at")
	String createdAt;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

}
