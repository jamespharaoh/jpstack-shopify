package wbs.integrations.shopify.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ShopifyMetafieldDaoMethods
	extends ShopifyDaoMethods <ShopifyMetafieldRec> {

	List <ShopifyMetafieldRec> findByOwner (
			Transaction parentTransaction,
			ShopifyAccountRec account,
			ShopifyMetafieldOwnerResource ownerResource,
			Long ownerId);

}
