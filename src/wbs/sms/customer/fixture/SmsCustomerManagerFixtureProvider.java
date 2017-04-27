package wbs.sms.customer.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.priv.model.PrivObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivObjectHelper;

import wbs.sms.customer.model.SmsCustomerManagerObjectHelper;

@PrototypeComponent ("smsCustomerManagerFixtureProvider")
public
class SmsCustomerManagerFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	PrivObjectHelper privHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	SmsCustomerManagerObjectHelper smsCustomerManagerHelper;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
	UserPrivObjectHelper userPrivHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OwnedTransaction transaction) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtures");

		) {

			smsCustomerManagerHelper.insert (
				taskLogger,
				smsCustomerManagerHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						GlobalId.root,
						"test"))

				.setCode (
					"customer_manager")

				.setName (
					"Customer manager")

				.setDescription (
					"Test customer manager")

			);

			menuItemHelper.insert (
				taskLogger,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"sms"))

				.setCode (
					"customer")

				.setName (
					"Customer")

				.setDescription (
					"")

				.setLabel (
					"Customers")

				.setTargetPath (
					"/smsCustomerManagers")

				.setTargetFrame (
					"main")

			);

		}

	}

}
