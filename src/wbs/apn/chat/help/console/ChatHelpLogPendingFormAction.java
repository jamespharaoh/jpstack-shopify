package wbs.apn.chat.help.console;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

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

import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.gsm.GsmUtils;

import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatHelpLogPendingFormAction")
public
class ChatHelpLogPendingFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@SingletonDependency
	ChatHelpLogic chatHelpLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatHelpLogPendingFormResponder");

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

		) {

			// get params

			String text =
				requestContext.parameterRequired (
					"text");

			boolean ignore =
				optionalIsPresent (
					requestContext.parameter (
						"ignore"));

			// check params

			if (! ignore) {

				if (text.length () == 0) {

					requestContext.addError (
						"Please type a message");

					return null;

				}

				if (! GsmUtils.gsmStringIsValid (text)) {

					requestContext.addError (
						"Reply contains invalid characters");

					return null;

				}

				/*
				if (Gsm.length(text) > 149) {
					requestContext.addError("Text is too long!");
					return null;
				}
				*/

			}

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						taskLogger,
						"ChatHelpLogPendingFormAction.goReal ()",
						this);

			) {

				// load objects from database

				ChatHelpLogRec helpRequest =
					chatHelpLogHelper.findFromContextRequired ();

				ChatUserRec chatUser =
					helpRequest.getChatUser ();

				// send message

				if (! ignore) {

					chatHelpLogic.sendHelpMessage (
						taskLogger,
						userConsoleLogic.userRequired (),
						chatUser,
						text,
						optionalOf (
							helpRequest.getMessage ().getThreadId ()),
						optionalOf (
							helpRequest));

				}

				// unqueue the request

				queueLogic.processQueueItem (
					taskLogger,
					helpRequest.getQueueItem (),
					userConsoleLogic.userRequired ());

				transaction.commit ();

				requestContext.addNotice (
					ignore
						? "Request ignored"
						: "Reply sent");

				return responder (
					"queueHomeResponder");

			}

		}

	}

}