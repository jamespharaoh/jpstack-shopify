package wbs.integrations.shopify.model;

import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

public
interface ShopifyObjectHelper <RecordType extends Record <RecordType>>
	extends
		ObjectHelper <RecordType>,
		ShopifyDaoMethods <RecordType> {

}
