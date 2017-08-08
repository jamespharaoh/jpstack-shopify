package wbs.integrations.shopify.apiclient.event;

import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiRequest;
import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

@SingletonComponent ("shopifyEventApiClient")
public
class ShopifyEventApiClientImplementation
	implements ShopifyEventApiClient {

	// singleton depdendencies

	@SingletonDependency
	private
	Database database;

	@ClassSingletonDependency
	private
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("shopifyHttpSender")
	private
	ComponentProvider <GenericHttpSender <
		ShopifyApiRequest,
		ShopifyApiResponse
	>> shopifyHttpSenderProvider;

	// public implementation

	@Override
	public
	List <ShopifyEventResponse> findSinceIdLimit (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ShopifyApiClientCredentials credentials,
			@NonNull Long eventId,
			@NonNull Long maxItems) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"findSinceIdLimit");

		) {

			if (
				moreThan (
					maxItems,
					250l)
			) {
				throw new RuntimeException ();
			}

			ShopifyEventListResponse response =
				genericCastUnchecked (
					shopifyHttpSenderProvider.provide (
						taskLogger)

				.allInOne (
					taskLogger,
					new ShopifyEventListRequest ()

					.httpCredentials (
						credentials)

					.limit (
						maxItems)

					.sinceId (
						eventId)

				)

			);

			return response.events ();

		}

	}

}
