package wbs.integrations.shopify.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.integrations.shopify.model.ShopifyAccountRec;

public
class ShopifyAccountHooks
	implements ObjectHooks <ShopifyAccountRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShopifyAccountRec account) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			account

				.setNextFullSynchronise (
					transaction.now ())

			;

		}

	}

}
