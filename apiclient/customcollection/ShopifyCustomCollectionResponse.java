package wbs.integrations.shopify.apiclient.customcollection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;

import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;

@Accessors (fluent = true)
@Data
public
class ShopifyCustomCollectionResponse
	implements ShopifyApiResponseItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "body_html")
	String bodyHtml;

	@DataAttribute (
		name = "handle")
	String handle;

	@DataChild (
		name = "image")
	ShopifyCustomCollectionImageResponse image;

	@DataAttribute (
		name = "published_at")
	String publishedAt;

	@DataAttribute (
		name = "published_scope")
	String publishedScope;

	@DataAttribute (
		name = "sort_order")
	String sortOrder;

	@DataAttribute (
		name = "template_suffix")
	String templateSuffix;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

}
