package wbs.integrations.shopify.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ShopifyOutboundLogDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			ShopifyOutboundLogSearch search);

}
