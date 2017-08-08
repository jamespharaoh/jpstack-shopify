
package wbs.integrations.shopify.apiclient.product;

import static wbs.utils.collection.CollectionUtils.collectionSize;
import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

import wbs.web.exceptions.HttpNotFoundException;

@SingletonComponent ("shopifyProductApiClient")
public
class ShopifyProductApiClientImplementation
	implements ShopifyProductApiClient {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("shopifyHttpSender")
	ComponentProvider <GenericHttpSender <
		ShopifyApiRequest,
		ShopifyApiResponse
	>> shopifyHttpSenderProvider;

	// public implementation

	@Override
	public
	List <ShopifyProductResponse> listAll (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"listAll")

		) {

			ImmutableList.Builder <ShopifyProductResponse> builder =
				ImmutableList.builder ();

			for (
				long page = 0l;
				true;
				page ++
			) {

				ShopifyProductListResponse response =
					genericCastUnchecked (
						shopifyHttpSenderProvider.provide (
							taskLogger)

					.allInOne (
						taskLogger,
						new ShopifyProductListRequest ()

						.httpCredentials (
							credentials)

						.limit (
							250l)

						.page (
							page)

					)

				);

				builder.addAll (
					response.products ());

				if (
					lessThan (
						collectionSize (
							response.products ()),
						250l)
				) {
					break;
				}

			}

			return builder.build ();

		}

	}

	@Override
	public
	List <Long> listAllIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"listAllIds");

		) {

			ImmutableList.Builder <Long> builder =
				ImmutableList.builder ();

			for (
				long page = 0l;
				true;
				page ++
			) {

				ShopifyProductListResponse response =
					genericCastUnchecked (
						shopifyHttpSenderProvider.provide (
							taskLogger)

					.allInOne (
						taskLogger,
						new ShopifyProductListRequest ()

						.httpCredentials (
							credentials)

						.limit (
							250l)

						.page (
							page)

						.fields (
							singletonList (
								"id"))

					)

				);

				builder.addAll (
					iterableMap (
						response.products (),
						ShopifyProductResponse::id));

				if (
					lessThan (
						collectionSize (
							response.products ()),
						250l)
				) {
					break;
				}

			}

			return builder.build ();

		}

	}

	@Override
	public
	ShopifyProductResponse create (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyProductRequest product) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"create");

		) {

			ShopifyProductCreateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyProductCreateRequest ()

					.httpCredentials (
						credentials)

					.product (
						product)

				)

			);

			return response.product ();

		}

	}

	@Override
	public
	Optional <ShopifyProductResponse> get (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"get");

		) {

			ShopifyProductGetResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyProductGetRequest ()

					.httpCredentials (
						credentials)

					.id (
						id)

				)

			);

			return optionalOf (
				response.product ());

		} catch (HttpNotFoundException notFoundException) {

			return optionalAbsent ();

		}

	}

	@Override
	public
	ShopifyProductResponse update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyProductRequest product) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"update");

		) {

			ShopifyProductUpdateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyProductUpdateRequest ()

					.httpCredentials (
						credentials)

					.product (
						product)

				)

			);

			return response.product ();

		}

	}

	@Override
	public
	void remove (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"remove");

		) {

			shopifyHttpSenderProvider.provide (
				taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyProductRemoveRequest ()

					.httpCredentials (
						credentials)

					.id (
						id)

				)

			;

		}

	}

}
