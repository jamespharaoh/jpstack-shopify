package wbs.integrations.shopify.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface ShopifyAccountDaoMethods {

	List <Long> findPendingFullSynchroniseIds (
			Transaction parentTransaction,
			Instant timestamp);

}
