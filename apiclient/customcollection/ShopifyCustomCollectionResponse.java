package wbs.integrations.shopify.apiclient.customcollection;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;

import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;
import wbs.integrations.shopify.apiclient.metafield.ShopifyMetafieldResponse;

@Accessors (fluent = true)
@Data
public
class ShopifyCustomCollectionResponse
	implements ShopifyApiResponseItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "handle")
	String handle;

	@DataAttribute (
		name = "title")
	String title;

	@DataAttribute (
		name = "body_html")
	String bodyHtml;

	@DataAttribute (
		name = "published_scope")
	String publishedScope;

	@DataAttribute (
		name = "published_at")
	String publishedAt;

	@DataChild (
		name = "image")
	ShopifyCustomCollectionImageResponse image;

	@DataAttribute (
		name = "updated_at")
	String updatedAt;

	@DataChildren (
		childrenElement = "metafields")
	List <ShopifyMetafieldResponse> metafields =
		emptyList ();

}
