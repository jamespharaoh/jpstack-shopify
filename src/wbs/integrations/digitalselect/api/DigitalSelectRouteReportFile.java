package wbs.integrations.digitalselect.api;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.digitalselect.model.DigitalSelectRouteOutObjectHelper;
import wbs.integrations.digitalselect.model.DigitalSelectRouteOutRec;

import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.report.logic.SmsDeliveryReportLogic;

import wbs.web.context.RequestContext;
import wbs.web.file.AbstractWebFile;

@SingletonComponent ("digitalSelectRouteReportFile")
public
class DigitalSelectRouteReportFile
	extends AbstractWebFile {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DigitalSelectRouteOutObjectHelper digitalSelectRouteOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	SmsDeliveryReportLogic reportLogic;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doPost");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"DigitalSelectRouteReportFile.doPost ()",
					this);

		) {

			Long routeId =
				requestContext.requestIntegerRequired (
					"routeId");

			String msgidParam =
				requestContext.parameterRequired (
					"msgid");

			String statParam =
				requestContext.parameterRequired (
					"stat");

			// debugging

			requestContext.debugDump (
				taskLogger);

			// sanity checks

			if (! messageStatusCodes.containsKey (statParam)) {

				throw new RuntimeException (
					stringFormat (
						"Unrecognised result: %s",
						statParam));

			}

			MessageStatus newMessageStatus =
				messageStatusCodes.get (statParam);

			DigitalSelectRouteOutRec digitalSelectRouteOut =
				digitalSelectRouteOutHelper.findRequired (
					routeId);

			// store report

			try {

				reportLogic.deliveryReport (
					taskLogger,
					digitalSelectRouteOut.getRoute (),
					msgidParam,
					newMessageStatus,
					optionalOf (
						statParam),
					optionalAbsent (),
					optionalAbsent (),
					optionalAbsent ());

			} catch (NoSuchMessageException exception) {

				// handle regular unrecognised message ids with a log warning and
				// custom HTTP response code

				// TODO expose frequent errors like this better somehow

				taskLogger.warningFormat (
					"Received delivery report for unknown message %s",
					msgidParam);

				requestContext.sendError (
					409l,
					"Message does not exist");

				return;

			}

			// commit

			transaction.commit ();

		}

	}

	// data

	final static
	Map<String,MessageStatus> messageStatusCodes =
		ImmutableMap.<String,MessageStatus>builder ()
			.put ("acked", MessageStatus.submitted)
			.put ("buffered phone", MessageStatus.submitted)
			.put ("buffered smsc", MessageStatus.submitted)
			.put ("Delivered", MessageStatus.delivered)
			.put ("Undelivered", MessageStatus.undelivered)
			.put ("Non Delivered", MessageStatus.undelivered)
			.put ("Lost Notification", MessageStatus.undelivered)
			.build ();

}
