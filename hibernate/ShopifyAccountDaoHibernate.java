package wbs.integrations.shopify.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountDaoMethods;
import wbs.integrations.shopify.model.ShopifyAccountRec;

public
class ShopifyAccountDaoHibernate
	extends HibernateDaoLegacy
	implements ShopifyAccountDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	List <Long> findPendingFullSynchroniseIds (
			@NonNull Transaction parentTransaction,
			@NonNull Instant timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findPendingFullSynchroniseIds");

		) {

			return findMany (
				transaction,
				Long.class,

				createCriteria (
					transaction,
					ShopifyAccountRec.class,
					"_account")

				.add (
					Restrictions.eq (
						"_account.deleted",
						false))

				.add (
					Restrictions.le (
						"_account.nextFullSynchronise",
						timestamp))

				.setProjection (
					Projections.id ())

			);

		}

	}

}
