package wbs.integrations.shopify.apiclient;

import java.util.function.Consumer;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogEvent;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.model.ShopifyAccountRec;

public
interface ShopifyApiLogic {

	ShopifyApiClientCredentials getApiCredentials (
			Transaction parentTransaction,
			ShopifyAccountRec shopifyAccount);

	Object responseToLocal (
			Transaction parentTransaction,
			Object responseValue,
			Class <?> localClass);

	void createOutboundLog (
			TaskLogger parentTaskLogger,
			TaskLogEvent taskLogEvent,
			Long accountId,
			GenericHttpSender <?, ?> httpSender);

	default <Request, Response>
	Consumer <GenericHttpSender <Request, Response>> createOutboundLog (
			@NonNull TaskLogger taskLogger,
			@NonNull Long accountId) {

		return httpRequest ->
			createOutboundLog (
				taskLogger,
				taskLogger,
				accountId,
				httpRequest);

	}

	String normaliseHtml (
			String source);

}
