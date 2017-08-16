package wbs.integrations.shopify.apiclient.productvariant;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductVariantUpdateRequest
	implements ShopifyApiRequest {

	ShopifyApiClientCredentials httpCredentials;

	@DataChild (
		name = "variant")
	ShopifyProductVariantRequest variant;

	// shopify api request implementation

	@Override
	public
	HttpMethod httpMethod () {
		return HttpMethod.put;
	}

	@Override
	public
	String httpPath () {

		return stringFormat (
			"/admin/variants/%s.json",
			integerToDecimalString (
				variant.id ()));

	}

	@Override
	public
	Class <? extends ShopifyApiResponse> httpResponseClass () {
		return ShopifyProductVariantUpdateResponse.class;
	}

}
