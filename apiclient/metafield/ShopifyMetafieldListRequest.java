package wbs.integrations.shopify.apiclient.metafield;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.EnumUtils.enumNameHyphens;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.presentInstancesMap;
import static wbs.utils.string.StringUtils.joinWithComma;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;
import wbs.integrations.shopify.model.ShopifyMetafieldOwnerResource;

import wbs.utils.data.Pair;

import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@Data
public
class ShopifyMetafieldListRequest
	implements ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials;

	List <Long> ids;
	Long limit;
	Long page;

	ShopifyMetafieldOwnerResource ownerResource;
	Long ownerId;

	String namespace;

	List <String> fields;

	// shopify api request implemenation

	@Override
	public
	HttpMethod httpMethod () {
		return HttpMethod.get;
	}

	@Override
	public
	String httpPath () {

		if (
			isNull (
				ownerResource)
		) {

			throw new NullPointerException (
				"Must set owner resource");

		}

		switch (ownerResource) {

		case customCollection:

			return stringFormat (
				"/admin/collections/%s/metafields.json",
				integerToDecimalString (
					ownerId));

		case product:

			return stringFormat (
				"/admin/products/%s/metafields.json",
				integerToDecimalString (
					ownerId));

		default:

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle owner resource: %s",
					enumNameHyphens (
						ownerResource)));

		}

	}

	@Override
	public
	Map <String, List <String>> httpParameters () {

		return presentInstancesMap (

			Pair.of (
				"ids",
				optionalMapRequired (
					optionalFromNullable (
						ids ()),
					ids ->
						singletonList (
							joinWithComma (
								integerToDecimalString (
									ids))))),

			Pair.of (
				"limit",
				optionalMapRequired (
					optionalFromNullable (
						limit ()),
					limit ->
						singletonList (
							integerToDecimalString (
								limit)))),

			Pair.of (
				"page",
				optionalMapRequired (
					optionalFromNullable (
						page ()),
					page ->
						singletonList (
							integerToDecimalString (
								page + 1)))),

			Pair.of (
				"namespace",
				optionalMapRequired (
					optionalFromNullable (
						namespace ()),
					namespace ->
						singletonList (
							namespace))),

			Pair.of (
				"fields",
				optionalMapRequired (
					optionalFromNullable (
						fields ()),
					fields ->
						singletonList (
							joinWithComma (
								fields))))

		);

	}

	@Override
	public
	Class <? extends ShopifyApiResponse> httpResponseClass () {
		return ShopifyMetafieldListResponse.class;
	}

}
