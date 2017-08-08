package wbs.integrations.shopify.model;

import wbs.framework.entity.record.Record;

public
interface ShopifyRecord <RecordType extends ShopifyRecord <RecordType>>
	extends Record <RecordType> {

	ShopifyAccountRec getAccount ();

	RecordType setAccount (
			ShopifyAccountRec account);

	Long getShopifyId ();

	RecordType setShopifyId (
			Long shopifyId);

	Boolean getDeleted ();

	RecordType setDeleted (
			Boolean deleted);

}
