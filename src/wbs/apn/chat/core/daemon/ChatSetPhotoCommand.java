package wbs.apn.chat.core.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.entity.record.IdObject;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.command.logic.CommandLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatSetPhotoCommand")
public
class ChatSetPhotoCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CommandLogic commandLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String[] {
			"chat.set_photo",
			"chat_scheme.photo_set"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			ChatRec chat;
			ChatSchemeRec chatScheme;

			Object parent =
				objectManager.getParentRequired (
					command);

			if (parent instanceof ChatRec) {

				chat = (ChatRec) parent;
				chatScheme = null;

			} else if (parent instanceof ChatSchemeRec) {

				chatScheme = (ChatSchemeRec) parent;
				chat = chatScheme.getChat ();

			} else {

				throw new RuntimeException ();

			}

			ServiceRec defaultService =
				serviceHelper.findByCodeRequired (
					chat,
					"default");

			MessageRec message =
				inbox.getMessage ();

			ChatUserRec chatUser =
				chatUserHelper.findOrCreate (
					taskLogger,
					chat,
					message);

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatUser);

			// set chat scheme

			if (chatScheme != null) {

				chatUserLogic.setScheme (
					chatUser,
					chatScheme);

			}

			// try set photo

			Optional <ChatUserImageRec> chatUserImageOptional =
				chatUserLogic.setPhotoFromMessage (
					taskLogger,
					chatUser,
					message,
					false);

			if (
				optionalIsPresent (
					chatUserImageOptional)
			) {

				// send confirmation

				chatSendLogic.sendSystemMagic (
					taskLogger,
					chatUser,
					optionalAbsent (),
					"photo_confirm",
					commandHelper.findByCodeRequired (
						chat,
						"magic"),
					IdObject.objectId (
						commandHelper.findByCodeRequired (
							chat,
							"help")),
					TemplateMissing.error,
					emptyMap ());

				// auto join

				chatMiscLogic.userAutoJoin (
					taskLogger,
					chatUser,
					message,
					true);

			// try set video

			} else if (
				chatUserLogic.setVideo (
					taskLogger,
					chatUser,
					message,
					false)
			) {

				// send confirmation

				chatSendLogic.sendSystemRbFree (
					taskLogger,
					chatUser,
					optionalOf (
						message.getThreadId ()),
					"video_set_pending",
					TemplateMissing.error,
					emptyMap ());

				// auto join

				chatMiscLogic.userAutoJoin (
					taskLogger,
					chatUser,
					message,
					true);

			} else {

				// send error

				chatSendLogic.sendSystemMmsFree (
					taskLogger,
					chatUser,
					optionalOf (
						message.getThreadId ()),
					"photo_error",
					commandHelper.findByCodeRequired (
						chat,
						"set_photo"),
					TemplateMissing.error);

			}

			// process inbox

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					defaultService),
				optionalOf (
					affiliate),
				command);

		}

	}

}
