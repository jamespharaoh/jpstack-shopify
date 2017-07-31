package wbs.integrations.shopify.apiclient;

import static wbs.utils.etc.Misc.unsupportedOperation;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface ShopifyApiClient <
	Request extends ShopifyApiRequestItem,
	Response extends ShopifyApiResponseItem
> {

	default
	List <Response> listAll (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials) {

		throw unsupportedOperation (
			this,
			"listAll");

	}

	default
	Optional <Response> get (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id) {

		throw unsupportedOperation (
			this,
			"get");

	}

	default
	Response create (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Request product) {

		throw unsupportedOperation (
			this,
			"create");

	}

	default
	Response update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Request product) {

		throw unsupportedOperation (
			this,
			"update");

	}

	default
	void remove (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id) {

		throw unsupportedOperation (
			this,
			"remove");

	}

}
