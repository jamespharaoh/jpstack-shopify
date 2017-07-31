package wbs.integrations.shopify.model;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

public
interface ShopifyEventSubjectDaoMethods {

	List <Long> findIdsPendingLimit (
			Transaction parentTransaction,
			Long maxResults);

	Optional <ShopifyEventSubjectRec> findBySubjectTypeAndId (
			Transaction parentTransaction,
			ShopifyAccountRec account,
			String subjectType,
			Long subjectId);

}
