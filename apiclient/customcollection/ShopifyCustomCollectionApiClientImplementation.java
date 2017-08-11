package wbs.integrations.shopify.apiclient.customcollection;

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
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiLogic;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

import wbs.web.exceptions.HttpNotFoundException;

@SingletonComponent ("shopifyCustomCollectionApiClient")
public
class ShopifyCustomCollectionApiClientImplementation
	implements ShopifyCustomCollectionApiClient {

	// singleton dependencies

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	ShopifyApiLogic shopifyApiLogic;

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
	List <ShopifyCustomCollectionResponse> listAll (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"listAll");

		) {

			ImmutableList.Builder <ShopifyCustomCollectionResponse> builder =
				ImmutableList.builder ();

			for (
				long page = 0l;
				true;
				page ++
			) {

				ShopifyCustomCollectionListResponse response =
					genericCastUnchecked (
						shopifyHttpSenderProvider.provide (
							taskLogger)

					.allInOne (
						taskLogger,
						new ShopifyCustomCollectionListRequest ()

							.httpCredentials (
								credentials)

							.limit (
								250l)

							.page (
								page),

						shopifyApiLogic.createOutboundLog (
							taskLogger,
							credentials.accountId ())

					)

				);

				builder.addAll (
					response.collections ());

				if (
					lessThan (
						collectionSize (
							response.collections ()),
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
			@NonNull ShopifyApiClientCredentials credentials)
		throws InterruptedException {

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

				ShopifyCustomCollectionListResponse response =
					genericCastUnchecked (
						shopifyHttpSenderProvider.provide (
							taskLogger)

					.allInOne (
						taskLogger,
						new ShopifyCustomCollectionListRequest ()

							.httpCredentials (
								credentials)

							.limit (
								250l)

							.page (
								page)

							.fields (
								singletonList (
									"id")),

						shopifyApiLogic.createOutboundLog (
							taskLogger,
							credentials.accountId ())

					)

				);

				builder.addAll (
					iterableMap (
						response.collections (),
						ShopifyCustomCollectionResponse::id));

				if (
					lessThan (
						collectionSize (
							response.collections ()),
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
	Optional <ShopifyCustomCollectionResponse> get (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"get");

		) {

			ShopifyCustomCollectionGetResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyCustomCollectionGetRequest ()

						.httpCredentials (
							credentials)

						.id (
							id),

					shopifyApiLogic.createOutboundLog (
						taskLogger,
						credentials.accountId ())

				)

			);

			return optionalOf (
				response.customCollection ());

		} catch (HttpNotFoundException notFoundException) {

			return optionalAbsent ();

		}

	}

	@Override
	public
	ShopifyCustomCollectionResponse create (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCustomCollectionRequest collection)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"create");

		) {

			ShopifyCustomCollectionCreateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,

					new ShopifyCustomCollectionCreateRequest ()

						.httpCredentials (
							credentials)

						.collection (
							collection),

					shopifyApiLogic.createOutboundLog (
						taskLogger,
						credentials.accountId ())

				)

			);

			return response.collection ();

		}

	}

	@Override
	public
	ShopifyCustomCollectionResponse update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCustomCollectionRequest collection)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"update");

		) {

			ShopifyCustomCollectionUpdateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyCustomCollectionUpdateRequest ()

					.httpCredentials (
						credentials)

					.customCollection (
						collection)

				)

			);

			return response.customCollection ();

		}

	}

	@Override
	public
	void remove (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long id)
		throws InterruptedException {

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
					new ShopifyCustomCollectionRemoveRequest ()

					.httpCredentials (
						credentials)

					.id (
						id)

				)

			;

		}

	}

}
