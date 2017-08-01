package wbs.integrations.shopify.hibernate;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyMetafieldDaoMethods;
import wbs.integrations.shopify.model.ShopifyMetafieldOwnerResource;
import wbs.integrations.shopify.model.ShopifyMetafieldRec;

public
class ShopifyMetafieldDaoHibernate
	extends HibernateDao
	implements ShopifyMetafieldDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	Optional <ShopifyMetafieldRec> findByShopifyId (
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
				ShopifyMetafieldRec.class,

				createCriteria (
					transaction,
					ShopifyMetafieldRec.class,
					"_metafield")

				.add (
					Restrictions.eq (
						"_metafield.account",
						account))

				.add (
					Restrictions.eq (
						"_metafield.shopifyId",
						id))

			);

		}

	}

	@Override
	public
	List <ShopifyMetafieldRec> findByOwner (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyAccountRec account,
			@NonNull ShopifyMetafieldOwnerResource ownerResource,
			@NonNull Long ownerId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByOwner");

		) {

			return findMany (
				transaction,
				ShopifyMetafieldRec.class,

				createCriteria (
					transaction,
					ShopifyMetafieldRec.class,
					"_metafield")

				.add (
					Restrictions.eq (
						"_metafield.account",
						account))

				.add (
					Restrictions.eq (
						"_metafield.ownerResource",
						ownerResource))

				.add (
					Restrictions.eq (
						"_metafield.ownerId",
						ownerId))

			);

		}

	}

}
