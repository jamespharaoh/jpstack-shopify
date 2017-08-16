package wbs.integrations.shopify.apiclient.product;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiRequestItem;
import wbs.integrations.shopify.apiclient.metafield.ShopifyMetafieldRequest;
import wbs.integrations.shopify.apiclient.productimage.ShopifyProductImageRequest;
import wbs.integrations.shopify.apiclient.productvariant.ShopifyProductVariantRequest;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductRequest
	implements ShopifyApiRequestItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "body_html")
	String bodyHtml;

	@DataAttribute (
		name = "vendor")
	String vendor;

	@DataAttribute (
		name = "product_type")
	String productType;

	@DataAttribute (
		name = "published")
	Boolean published;

	@DataChildren (
		childrenElement = "images")
	List <ShopifyProductImageRequest> images;

	@DataChildren (
		childrenElement = "options")
	List <ShopifyProductOptionRequest> options;

	@DataChildren (
		childrenElement = "variants")
	List <ShopifyProductVariantRequest> variants;

	@DataChildren (
		childrenElement = "metafields")
	List <ShopifyMetafieldRequest> metafields;

}
