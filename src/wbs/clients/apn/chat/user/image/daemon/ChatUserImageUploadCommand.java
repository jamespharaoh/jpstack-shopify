package wbs.clients.apn.chat.user.image.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.clients.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.etc.Misc;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Accessors (fluent = true)
@PrototypeComponent ("chatUserImageUploadCommand")
public
class ChatUserImageUploadCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.image_upload"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		MessageRec messageIn =
			inbox.getMessage ();

		ChatRec chat =
			(ChatRec)
			(Object)
			objectManager.getParent (
				command);

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				messageIn);

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		// generate token and url

		String token =
			Misc.generateTenCharacterToken ();

		String url =
			stringFormat (
				"http://txtit.com/iu/%u",
				token);

		// send message

		CommandRec magicCommand =
			commandHelper.findByCode (
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCode (
				chat,
				"help");

		MessageRec messageOut =
			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					messageIn.getThreadId ()),
				"image_upload_link",
				magicCommand,
				helpCommand.getId (),
				ImmutableMap.<String,String>builder ()
					.put ("url", url)
					.build ());

		// save token in database

		chatUserImageUploadTokenHelper.insert (
			new ChatUserImageUploadTokenRec ()

			.setChatUser (
				chatUser)

			.setIndex (
				chatUser.getNumImageUploadTokens ())

			.setToken (
				token)

			.setMessageIn (
				messageIn)

			.setMessageOut (
				messageOut)

			.setCreatedTime (
				transaction.now ())

			.setExpiryTime (
				transaction.now ().plus (
					Duration.standardHours (24)))

		);

		// update chat user

		chatUser

			.setNumImageUploadTokens (
				chatUser.getNumImageUploadTokens () + 1);

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);


	}

}