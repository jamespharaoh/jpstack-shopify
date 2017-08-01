package wbs.integrations.shopify.hibernate;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyCustomCollectionDaoMethods;
import wbs.integrations.shopify.model.ShopifyCustomCollectionRec;

public
class ShopifyCustomCollectionDaoHibernate
	extends HibernateDao
	implements ShopifyCustomCollectionDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	Optional <ShopifyCustomCollectionRec> findByShopifyId (
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
				ShopifyCustomCollectionRec.class,

				createCriteria (
					transaction,
					ShopifyCustomCollectionRec.class,
					"_customCollection")

				.add (
					Restrictions.eq (
						"_customCollection.account",
						account))

				.add (
					Restrictions.eq (
						"_customCollection.shopifyId",
						id))

			);

		}

	}

}
