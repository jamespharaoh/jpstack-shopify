package wbs.integrations.shopify.apiclient;

import static wbs.utils.collection.MapUtils.emptyMap;

import java.util.List;
import java.util.Map;

import wbs.web.misc.HttpMethod;

public
interface ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials ();

	HttpMethod httpMethod ();

	String httpPath ();

	default
	Map <String, List <String>> httpParameters () {
		return emptyMap ();
	}

	Class <? extends ShopifyApiResponse> httpResponseClass ();

}
