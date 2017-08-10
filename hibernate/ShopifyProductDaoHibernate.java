package wbs.integrations.shopify.hibernate;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyProductDaoMethods;
import wbs.integrations.shopify.model.ShopifyProductRec;

public
class ShopifyProductDaoHibernate
	extends HibernateDaoLegacy
	implements ShopifyProductDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	Optional <ShopifyProductRec> findByShopifyId (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyAccountRec account,
			@NonNull Long id) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByShopifyId");

		) {

			return findOne (
				transaction,
				ShopifyProductRec.class,

				createCriteria (
					transaction,
					ShopifyProductRec.class,
					"_product")

				.add (
					Restrictions.eq (
						"_product.account",
						account))

				.add (
					Restrictions.eq (
						"_product.shopifyId",
						id))

			);

		}

	}

}
