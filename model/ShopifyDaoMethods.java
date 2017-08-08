package wbs.integrations.shopify.model;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface ShopifyDaoMethods <RecordType extends Record <RecordType>> {

	Optional <RecordType> findByShopifyId (
			Transaction parentTransaction,
			ShopifyAccountRec account,
			Long id);

}
