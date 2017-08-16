package wbs.integrations.shopify.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyOutboundLogDaoMethods;
import wbs.integrations.shopify.model.ShopifyOutboundLogRec;
import wbs.integrations.shopify.model.ShopifyOutboundLogSearch;

public
class ShopifyOutboundLogDaoHibernate
	extends HibernateDaoLegacy
	implements ShopifyOutboundLogDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyOutboundLogSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					ShopifyOutboundLogRec.class,
					"_outboundLog");

			if (
				isNotNull (
					search.accountId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_outboundLog.account.id",
						search.accountId ()));

			}

			if (
				isNotNull (
					search.timestamp ())
			) {

				if (search.timestamp ().hasStart ()) {

					criteria.add (
						Restrictions.ge (
							"_outboundLog.timestamp",
							search.timestamp ().start ()));

				}

				if (search.timestamp ().hasEnd ()) {

					criteria.add (
						Restrictions.lt (
							"_outboundLog.timestamp",
							search.timestamp ().end ()));

				}

			}

			if (
				isNotNull (
					search.method ())
			) {

				criteria.add (
					Restrictions.eq (
						"_outboundLog.method",
						search.method ()));

			}

			if (
				isNotNull (
					search.path ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_outboundLog.path",
						stringFormat (
							"%%%s%%",
							search.path ())));

			}

			if (
				isNotNull (
					search.success ())
			) {

				criteria.add (
					Restrictions.eq (
						"_outboundLog.success",
						search.success ()));

			}

			criteria.addOrder (
				Order.desc (
					"_outboundLog.timestamp"));

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
