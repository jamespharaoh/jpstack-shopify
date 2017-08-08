package wbs.integrations.shopify.apiclient.product;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;

import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;

@Accessors (fluent = true)
@Data
public
class ShopifyProductResponse
	implements ShopifyApiResponseItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "body_html")
	String bodyHtml;

	@DataAttribute (
		name = "created_at")
	String createdAt;

	@DataAttribute (
		name = "handle")
	String handle;

	@DataAttribute (
		name = "metafields_global_title_tag")
	String metafieldsGlobalTitleTag;

	@DataAttribute (
		name = "metafields_global_description_tag")
	String metafieldsGlobalDescriptionTag;

	@DataAttribute (
		name = "product_type")
	String productType;

	@DataAttribute (
		name = "published_at")
	String publishedAt;

	@DataAttribute (
		name = "published_scope")
	String publishedScope;

	@DataAttribute (
		name = "template_suffix")
	String templateSuffix;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

	@DataAttribute (
		name = "vendor")
	String vendor;

	@DataChildren (
		childrenElement = "images")
	List <ShopifyProductImageResponse> images =
		emptyList ();

	@DataChildren (
		childrenElement = "variants")
	List <ShopifyProductVariantResponse> variants =
		emptyList ();

}
