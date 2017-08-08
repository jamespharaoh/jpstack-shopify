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
import wbs.integrations.shopify.model.ShopifyEventSubjectDaoMethods;
import wbs.integrations.shopify.model.ShopifyEventSubjectRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectType;

public
class ShopifyEventSubjectDaoHibernate
	extends HibernateDao
	implements ShopifyEventSubjectDaoMethods {

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
					ShopifyEventSubjectRec.class,
					"_shopifyEventSubject")

				.add (
					Restrictions.eq (
						"_shopifyEventSubject.pending",
						true))

				.addOrder (
					Order.asc (
						"_shopifyEventSubject.id"))

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
	List <ShopifyEventSubjectRec> findBySubjectType (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyAccountRec account,
			@NonNull ShopifyEventSubjectType subjectType) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findBySubjectType");

		) {

			return findMany (
				transaction,
				ShopifyEventSubjectRec.class,

				createCriteria (
					transaction,
					ShopifyEventSubjectRec.class,
					"_shopifyEventSubject")

				.add (
					Restrictions.eq (
						"_shopifyEventSubject.account",
						account))

				.add (
					Restrictions.eq (
						"_shopifyEventSubject.subjectType",
						subjectType))

			);

		}

	}

	@Override
	public
	Optional <ShopifyEventSubjectRec> findBySubjectTypeAndId (
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
				ShopifyEventSubjectRec.class,

				createCriteria (
					transaction,
					ShopifyEventSubjectRec.class,
					"_shopifyEventSubject")

				.add (
					Restrictions.eq (
						"_shopifyEventSubject.account",
						account))

				.add (
					Restrictions.eq (
						"_shopifyEventSubject.subjectType",
						subjectType))

				.add (
					Restrictions.eq (
						"_shopifyEventSubject.subjectId",
						subjectId))

			);

		}

	}

}
