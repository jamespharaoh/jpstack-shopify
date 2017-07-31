package wbs.integrations.shopify.apiclient.customcollection;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyCustomCollectionRemoveRequest
	implements ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials;

	Long id;

	// shopify api request implementation

	@Override
	public
	HttpMethod httpMethod () {
		return HttpMethod.delete;
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
	Class <? extends ShopifyApiResponse> httpResponseClass () {
		return ShopifyCustomCollectionRemoveResponse.class;
	}

}
