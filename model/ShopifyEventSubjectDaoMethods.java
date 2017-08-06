package wbs.integrations.shopify.model;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

public
interface ShopifyEventSubjectDaoMethods {

	List <Long> findIdsPendingLimit (
			Transaction parentTransaction,
			Long maxResults);

	List <ShopifyEventSubjectRec> findBySubjectType (
			Transaction parentTransaction,
			ShopifyAccountRec account,
			ShopifyEventSubjectType subjectType);

	Optional <ShopifyEventSubjectRec> findBySubjectTypeAndId (
			Transaction parentTransaction,
			ShopifyAccountRec account,
			ShopifyEventSubjectType subjectType,
			Long subjectId);

}
