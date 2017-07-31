package wbs.integrations.shopify.apiclient.metafield;

import static wbs.utils.collection.CollectionUtils.singletonList;
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

	String namespace;
	String metafieldOwnerResource;

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

		return stringFormat (
			"/admin/metafields.json");

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
				"metafield[owner_resource]",
				optionalMapRequired (
					optionalFromNullable (
						metafieldOwnerResource ()),
					ownerResource ->
						singletonList (
							ownerResource))),

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
