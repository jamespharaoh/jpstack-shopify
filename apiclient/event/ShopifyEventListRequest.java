package wbs.integrations.shopify.apiclient.event;

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
class ShopifyEventListRequest
	implements ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials;

	Long limit;
	Long page;

	List <Long> ids;
	Long sinceId;

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
			"/admin/events.json");

	}

	@Override
	public
	Map <String, List <String>> httpParameters () {

		return presentInstancesMap (

			// range and limit

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
				"limit",
				optionalMapRequired (
					optionalFromNullable (
						limit ()),
					limit ->
						singletonList (
							integerToDecimalString (
								limit)))),

			// selectors

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
				"since_id",
				optionalMapRequired (
					optionalFromNullable (
						sinceId ()),
					sinceId ->
						singletonList (
							integerToDecimalString (
								sinceId)))),

			// fields

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
		return ShopifyEventListResponse.class;
	}

}
