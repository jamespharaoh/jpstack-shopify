package wbs.integrations.shopify.model;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

public
interface ShopifyEventDaoMethods {

	List <Long> findIdsPendingLimit (
			Transaction parentTransaction,
			Long maxResults);

	Optional <ShopifyEventRec> findBySubjectTypeAndId (
			Transaction parentTransaction,
			ShopifyAccountRec account,
			ShopifyEventSubjectType subjectType,
			Long subjectId);

}
