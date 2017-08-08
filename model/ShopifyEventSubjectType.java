package wbs.integrations.shopify.model;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

public
enum ShopifyEventSubjectType {

	cart,
	checkout,
	collection,
	collectionPublication,
	customer,
	customerSavedSearch,
	draftOrder,
	fulfillment,
	fulfillmentEvent,
	order,
	orderTransaction,
	product,
	productPublication,
	refund,
	shop,
	theme;

	public final static
	ShopifyEventSubjectType fromTopic (
			@NonNull String topicSubjectTypeString) {

		return mapItemForKeyRequired (
			fromTopicMap,
			topicSubjectTypeString);

	}

	final static
	Map <String, ShopifyEventSubjectType> fromTopicMap =
		ImmutableMap.<String, ShopifyEventSubjectType> builder ()

		.put (
			"carts",
			cart)

		.put (
			"checkouts",
			checkout)

		.put (
			"collections",
			collection)

		.put (
			"collection_listings",
			collectionPublication)

		.put (
			"customers",
			customer)

		.put (
			"customer_groups",
			customerSavedSearch)

		.put (
			"draft_orders",
			draftOrder)

		.put (
			"fulfillments",
			fulfillment)

		.put (
			"fulfillment_events",
			fulfillmentEvent)

		.put (
			"orders",
			order)

		.put (
			"order_transactions",
			orderTransaction)

		.put (
			"products",
			product)

		.put (
			"product_listings",
			productPublication)

		.put (
			"refunds",
			refund)

		.put (
			"app",
			shop)

		.put (
			"shop",
			shop)

		.put (
			"themes",
			theme)

		.build ()

	;

}
