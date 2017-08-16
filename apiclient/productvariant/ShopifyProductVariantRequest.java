package wbs.integrations.shopify.apiclient.productvariant;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiRequestItem;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductVariantRequest
	implements ShopifyApiRequestItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "sku")
	String sku;

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

	@DataAttribute (
		name = "inventory_quantity_adjustment")
	Long inventoryQuantityAdjustment;

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

	@DataAttribute (
		name = "option4")
	String option4;

	@DataAttribute (
		name = "option5")
	String option5;

	@DataAttribute (
		name = "option6")
	String option6;

	@DataAttribute (
		name = "option7")
	String option7;

	@DataAttribute (
		name = "option8")
	String option8;

}
