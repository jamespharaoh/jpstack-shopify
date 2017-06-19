package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.LogicUtils.booleanToString;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlTableCheckWriter;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

@PrototypeComponent ("chatUserAdminBarringPart")
public
class ChatUserAdminBarringPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <HtmlTableCheckWriter> htmlTableCheckWriterProvider;

	// state

	ChatUserRec chatUser;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/wbs.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (
				enumEqualSafe (
					chatUser.getType (),
					ChatUserType.monitor)
			) {

				htmlParagraphWrite (
					formatWriter,
					"This is a monitor and cannot be barred.");

				return;

			}

			// form open

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatUser.admin.barring"));

			// table open

			htmlTableOpenDetails (
				formatWriter);

			// table content

			htmlTableDetailsRowWrite (
				formatWriter,
				"Status",
				booleanToString (
					chatUser.getBarred (),
					"barred",
					"not barred"));

			if (requestContext.canContext ("chat.userAdmin")) {

				htmlTableDetailsRowWriteRaw (
					formatWriter,
					"Action",
					() -> {

					if (chatUser.getBarred ()) {

						htmlTableCheckWriterProvider.provide (
							transaction)

							.name (
								"bar_off")

							.label (
								"remove bar")

							.value (
								false)

							.write (
								formatWriter);

					} else {

						htmlTableCheckWriterProvider.provide (
							transaction)

							.name (
								"bar_on")

							.label (
								"bar user")

							.value (
								false)

							.write (
								formatWriter);

					}

				});

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Reason",
					() -> formatWriter.writeFormat (
						"<textarea",
						" rows=\"4\"",
						" cols=\"48\"",
						" name=\"reason\"",
						"></textarea>"));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Action",
					() -> formatWriter.writeFormat (
						"<input",
						" type=\"submit\"",
						" value=\"save changes\"",
						">"));

			}

			// table close

			htmlTableClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

			// flush scripts

			requestContext.flushScripts (
				formatWriter);

		}

	}

}
