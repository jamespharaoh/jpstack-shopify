package wbs.integrations.shopify.apiclient;

import wbs.framework.database.Transaction;

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

}
