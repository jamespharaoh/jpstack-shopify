package wbs.integrations.shopify.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyEventDaoMethods;
import wbs.integrations.shopify.model.ShopifyEventRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectType;

public
class ShopifyEventDaoHibernate
	extends HibernateDao
	implements ShopifyEventDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	List <Long> findIdsPendingLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findPendingLimit");

		) {

			return findMany (
				transaction,
				Long.class,
				createCriteria (
					transaction,
					ShopifyEventRec.class,
					"_shopifyEvent")

				.add (
					Restrictions.eq (
						"_shopifyEvent.pending",
						true))

				.addOrder (
					Order.asc (
						"_shopifyEvent.id"))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

				.setProjection (
					Projections.id ())

			);

		}

	}

	@Override
	public
	Optional <ShopifyEventRec> findBySubjectTypeAndId (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyAccountRec account,
			@NonNull ShopifyEventSubjectType subjectType,
			@NonNull Long subjectId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findBySubjectTypeAndId");

		) {

			return findOne (
				transaction,
				ShopifyEventRec.class,

				createCriteria (
					transaction,
					ShopifyEventRec.class,
					"_shopifyEvent")

				.add (
					Restrictions.eq (
						"_shopifyEvent.account",
						account))

				.add (
					Restrictions.eq (
						"_shopifyEvent.subjectType",
						subjectType))

				.add (
					Restrictions.eq (
						"_shopifyEvent.subjectId",
						subjectId))

			);

		}

	}

}
