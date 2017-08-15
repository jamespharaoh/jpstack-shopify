package wbs.integrations.shopify.apiclient.collect;

import static wbs.utils.collection.CollectionUtils.collectionSize;
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

@SingletonComponent ("shopifyCollectApiClient")
public
class ShopifyCollectApiClientImplementation
	implements ShopifyCollectApiClient {

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
	List <ShopifyCollectResponse> listAll (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"listAll");

		) {

			ImmutableList.Builder <ShopifyCollectResponse> builder =
				ImmutableList.builder ();

			for (
				long page = 0l;
				true;
				page ++
			) {

				ShopifyCollectListResponse response =
					genericCastUnchecked (
						shopifyHttpSenderProvider.provide (
							taskLogger)

					.allInOne (
						taskLogger,

						new ShopifyCollectListRequest ()

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
					response.collects ());

				if (
					lessThan (
						collectionSize (
							response.collects ()),
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
	Optional <ShopifyCollectResponse> get (
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

			ShopifyCollectGetResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,

					new ShopifyCollectGetRequest ()

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
				response.collect ());

		} catch (HttpNotFoundException notFoundException) {

			return optionalAbsent ();

		}

	}

	@Override
	public
	ShopifyCollectResponse create (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull ShopifyCollectRequest request)
		throws InterruptedException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"create");

		) {

			ShopifyCollectCreateResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,

					new ShopifyCollectCreateRequest ()

						.httpCredentials (
							credentials)

						.collect (
							request),

					shopifyApiLogic.createOutboundLog (
						taskLogger,
						credentials.accountId ())

				)

			);

			return response.collect ();

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
					new ShopifyCollectRemoveRequest ()

						.httpCredentials (
							credentials)

						.id (
							id),

					shopifyApiLogic.createOutboundLog (
						taskLogger,
						credentials.accountId ())

				)

			;

		}

	}

}
