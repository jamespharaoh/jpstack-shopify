package wbs.imchat.console;

import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.imchat.logic.ImChatLogic;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatCustomerSettingsPasswordAction")
public
class ImChatCustomerSettingsPasswordAction
	extends ConsoleAction {

	// implementation

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatLogic imChatLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"imChatCustomerSettingsPasswordResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goReal");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"ImChaCustomerSettingsPasswordAction.goReal ()",
					this);

		) {

			// find customer

			ImChatCustomerRec customer =
				imChatCustomerHelper.findFromContextRequired ();

			// generate new password

			imChatLogic.customerPasswordGenerate (
				taskLogger,
				customer,
				Optional.of (
					userConsoleLogic.userRequired ()));

			// complete transaction

			transaction.commit ();

			requestContext.addNotice (
				stringFormat (
					"New password generated: %s",
					customer.getPassword ()));

			requestContext.addNotice (
				stringFormat (
					"This password has been automatically emailed to: %s",
					customer.getEmail ()));

			requestContext.addWarning (
				stringFormat (
					"This password cannot be retrieved, please make a note of it ",
					"immediately and communicate it to the customer."));

			requestContext.addWarning (
				stringFormat (
					"This new password replaces all previous passwords for this ",
					"customer account, none of which will function from this ",
					"moment."));

			return null;

		}

	}

}
