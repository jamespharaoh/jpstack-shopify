package wbs.integrations.shopify.fixture;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.time.TimeUtils.isoTimestampString;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.logging.LogContext;

import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;

import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

public
class ShopifyFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	private
	EventFixtureLogic eventFixtureLogic;

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	private
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	private
	ShopifyAccountObjectHelper shopifyAccountHelper;

	@SingletonDependency
	private
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	private
	TestAccounts testAccounts;

	@SingletonDependency
	private
	WbsConfig wbsConfig;

	// public implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			createMenuItems (
				transaction);

			createStores (
				transaction);

		}

	}

	// private implementation

	private
	void createMenuItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"integration"))

				.setCode (
					"shopify")

				.setName (
					"Shopify")

				.setDescription (
					"Shopify")

				.setLabel (
					"Shopify")

				.setTargetPath (
					"/shopifyAccounts")

				.setTargetFrame (
					"main")

			);

			transaction.flush ();

		}

	}

	private
	void createStores (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createStores");

		) {

			testAccounts.forEach (
				"shopify-account",
				suppliedParams -> {

				SliceRec slice =
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						mapItemForKeyRequired (
							suppliedParams,
							"slice"));

				Map <String, String> allParams =
					ImmutableMap.<String, String> builder ()

					.putAll (
						suppliedParams)

					.put (
						"code",
						simplifyToCodeRequired (
							mapItemForKeyRequired (
								suppliedParams,
								"name")))

					.put (
						"nextFullSynchronise",
						isoTimestampString (
							transaction.now ()))

					.build ()

				;

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"Shopify",
					shopifyAccountHelper,
					slice,
					allParams,
					emptySet ());

			});

			transaction.flush ();

		}

	}

}
