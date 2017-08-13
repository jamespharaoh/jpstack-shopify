package wbs.integrations.shopify.apiclient;

import static wbs.utils.etc.BinaryUtils.bytesToBase64;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.apiclient.GenericHttpSenderHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.data.tools.DataToJson;

import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@PrototypeComponent ("shopifyHttpSenderHelper")
public
class ShopifyHttpSenderHelper
	implements GenericHttpSenderHelper <
		ShopifyApiRequest,
		ShopifyApiResponse
	> {

	// properties

	@Getter @Setter
	ShopifyApiRequest request;

	@Getter @Setter
	ShopifyApiResponse response;

	@Getter @Setter
	Long responseStatusCode;

	@Getter @Setter
	String responseStatusReason;

	@Getter @Setter
	Map <String, List <String>> responseHeaders;

	@Getter @Setter
	String requestBody;

	@Getter @Setter
	String responseBody;

	// details

	@Override
	public
	Duration defaultTimeout () {
		return Duration.standardSeconds (60l);
	}

	@Override
	public
	Long tooManyRequestsMaxTries () {
		return 10l;
	}

	@Override
	public
	HttpMethod method () {
		return request.httpMethod ();
	}

	@Override
	public
	String url () {

		return stringFormat (
			"https://%s.myshopify.com%s",
			request.httpCredentials ().storeName (),
			request.httpPath ());

	}

	@Override
	public
	Map <String, List <String>> requestParameters () {
		return request.httpParameters ();
	}

	@Override
	public
	Map <String, String> requestHeaders () {

		if (
			enumInSafe (
				request.httpMethod (),
				HttpMethod.post,
				HttpMethod.put)
		) {

			return ImmutableMap.<String, String> builder ()

				.put (
					"Authorization",
					stringFormat (
						"Basic %s",
						bytesToBase64 (
							stringToUtf8 (
								stringFormat (
									"%s:%s",
									request.httpCredentials ().username (),
									request.httpCredentials ().password ())))))

				.put (
					"Content-Type",
					"application/json; charset=utf-8")

				.build ();

		} else {

			return ImmutableMap.<String, String> builder ()

				.put (
					"Authorization",
					stringFormat (
						"Basic %s",
						bytesToBase64 (
							stringToUtf8 (
								stringFormat (
									"%s:%s",
									request.httpCredentials ().username (),
									request.httpCredentials ().password ())))))

				.build ();

		}

	}

	@Override
	public
	void verify () {

		doNothing ();

	}

	@Override
	public
	void encode () {

		if (
			enumNotInSafe (
				request.httpMethod (),
				HttpMethod.post,
				HttpMethod.put)
		) {
			return;
		}

		DataToJson dataToJson =
			new DataToJson ();

		JsonElement jsonValue =
			dataToJson.toJson (
				request);

		requestBody =
			jsonValue.toString ();

	}

	@Override
	public
	void decode () {

		DataFromJson dataFromJson =
			new DataFromJson ();

		response =
			dataFromJson.fromJson (
				request.httpResponseClass (),
				responseBody);

	}

}
