package wbs.integrations.shopify.apiclient.customcollection;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@Data
public
class ShopifyCustomCollectionGetRequest
	implements ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials;

	Long id;

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
			"/admin/custom_collections/%s.json",
			integerToDecimalString (
				id));

	}

	@Override
	public
	Map <String, List <String>> httpParameters () {
		return emptyMap ();
	}

	@Override
	public
	Class <? extends ShopifyApiResponse> httpResponseClass () {
		return ShopifyCustomCollectionGetResponse.class;
	}

}
