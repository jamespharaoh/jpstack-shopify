package wbs.integrations.shopify.apiclient.metafield;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiRequestItem;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyMetafieldRequest
	implements ShopifyApiRequestItem {

	@DataAttribute (
		name = "id")
	Long id;

	@DataAttribute (
		name = "key")
	String key;

	@DataAttribute (
		name = "namespace")
	String namespace;

	@DataAttribute (
		name = "value")
	Long integerValue;

	@DataAttribute (
		name = "value")
	String stringValue;

	@DataAttribute (
		name = "value_type")
	String valueType;

	@DataAttribute (
		name = "description")
	String description;

	// static constructors

	public static
	ShopifyMetafieldRequest of (
			@NonNull String namespace,
			@NonNull String key,
			@NonNull String value) {

		return new ShopifyMetafieldRequest ()

			.namespace (
				namespace)

			.key (
				key)

			.stringValue (
				value)

			.valueType (
				"string")

		;

	}

	public static
	ShopifyMetafieldRequest of (
			@NonNull String namespace,
			@NonNull String key,
			@NonNull String value,
			@NonNull String description) {

		return new ShopifyMetafieldRequest ()

			.namespace (
				namespace)

			.key (
				key)

			.stringValue (
				value)

			.valueType (
				"string")

			.description (
				description)

		;

	}

	public static
	ShopifyMetafieldRequest of (
			@NonNull String namespace,
			@NonNull String key,
			@NonNull Long value) {

		return new ShopifyMetafieldRequest ()

			.namespace (
				namespace)

			.key (
				key)

			.integerValue (
				value)

			.valueType (
				"integer")

		;

	}

	public static
	ShopifyMetafieldRequest of (
			@NonNull String namespace,
			@NonNull String key,
			@NonNull Long value,
			@NonNull String description) {

		return new ShopifyMetafieldRequest ()

			.namespace (
				namespace)

			.key (
				key)

			.integerValue (
				value)

			.valueType (
				"integer")

			.description (
				description)

		;

	}

}
