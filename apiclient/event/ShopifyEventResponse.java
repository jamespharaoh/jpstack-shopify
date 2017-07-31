package wbs.integrations.shopify.apiclient.event;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;

import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;

@Accessors (fluent = true)
@Data
public
class ShopifyEventResponse
	implements ShopifyApiResponseItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataChildren (
		childrenElement = "arguments")
	List <String> arguments;

	@DataAttribute (
		name = "author")
	String author;

	@DataAttribute (
		name = "body")
	String body;

	@DataAttribute (
		name = "created_at")
	String createdAt;

	@DataAttribute (
		name = "description")
	String description;

	@DataAttribute (
		name = "message")
	String message;

	@DataAttribute (
		name = "path")
	String path;

	@DataAttribute (
		name = "subject_id")
	Long subjectId;

	@DataAttribute (
		name = "subject_type")
	String subjectType;

	@DataAttribute (
		name = "verb")
	String verb;

}
