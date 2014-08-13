package wbs.apn.chat.core.logic;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.help.logic.ChatHelpTemplateLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic.UserDistance;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSessionObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserSessionRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.apn.chat.user.info.model.ChatUserNameObjectHelper;
import wbs.apn.chat.user.info.model.ChatUserNameRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.locator.logic.LocatorManager;
import wbs.sms.locator.model.LongLat;
import wbs.sms.magicnumber.logic.MagicNumberLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.outbox.logic.MessageSender;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Log4j
@SingletonComponent ("chatMiscLogic")
public
class ChatMiscLogicImpl
	implements ChatMiscLogic {

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatDateLogic chatDateLogic;

	@Inject
	ChatHelpLogic chatHelpLogic;

	@Inject
	ChatNumberReportLogic chatNumberReportLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatHelpTemplateLogic chatTemplateLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserNameObjectHelper chatUserNameHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserSessionObjectHelper chatUserSessionHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	LocatorManager locatorManager;

	@Inject
	MagicNumberLogic magicNumberLogic;

	@Inject
	MediaLogic mediaUtils;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueLogic queueLogic;

	@Inject
	Random random;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Provider<MessageSender> messageSender;

	/**
	 * Gets all online monitors who are candidates for a random outbound message
	 * (either in response to a "join" outbound or a "quiet" outbound).
	 *
	 * Monitors considered must have a picture, not be blocked, be compatible
	 * and never previously sent a message to this user.
	 *
	 * @param thisUser
	 *            User message is to be sent to
	 * @return All online monitors who qualify.
	 */
	@Override
	public List<ChatUserRec> getOnlineMonitorsForOutbound (
			ChatUserRec thisUser) {

		ChatRec chat = thisUser.getChat ();

		Collection<ChatUserRec> onlineUsers =
			chatUserHelper.findOnline (
				chat);

		List<ChatUserRec> ret = new ArrayList<ChatUserRec> ();

		for (ChatUserRec chatUser : onlineUsers) {

			// ignore non-monitors
			if (chatUser.getType () != ChatUserType.monitor)
				continue;

			// ignore blocked users
			if (thisUser.getBlocked ().containsKey (chatUser.getId ()))
				continue;

			// if we aren't suitable gender/orients for each other skip it

			if (! chatUserLogic.compatible (
					thisUser,
					chatUser))
				continue;

			// ignore users we have previously had a message from

			ChatContactRec chatContact =
				thisUser.getFromContacts ().get (
					chatUser.getId ());

			if (chatContact != null
					&& chatContact.getLastDeliveredMessageTime () != null)
				continue;

			// ignore users with no info or pic

			if (chatUser.getInfoText () == null)
				continue;

			if (chatUser.getMainChatUserImage () == null)
				continue;

			// ignore users according to monitor cap

			if (thisUser.getMonitorCap () != null
					&& chatUser.getType () == ChatUserType.monitor
					&& (chatUser.getCode ().charAt (2) - '0')
						< thisUser.getMonitorCap ())
				continue;

			ret.add (chatUser);

		}

		return ret;

	}

	/**
	 * Get the closest online monitor suitable to use for an outbound message.
	 *
	 * @see getOnlineMoniorsForOutbound for detailed criteria.
	 *
	 * @param thisUser
	 *            User message is to be sent to
	 * @return Monitor to send
	 */
	@Override
	public ChatUserRec getOnlineMonitorForOutbound (
			ChatUserRec thisUser) {

		List<ChatUserRec> monitors =
			getOnlineMonitorsForOutbound (thisUser);

		List<UserDistance> distances =
			chatUserLogic.getUserDistances (thisUser, monitors);

		Collections.sort (distances);

		return distances.size () > 0
			? distances.get (0).user ()
			: null;

	}

	@Override
	public
	void blockAll (
			ChatUserRec chatUser,
			MessageRec message) {

		// log them off

		chatUserLogic.logoff (
			chatUser,
			message == null);

		// block all allMessages and ads

		chatUser.setBlockAll (true);
		chatUser.setNextAd (null);

		// turn off dating

		chatDateLogic.userDateStuff (
			chatUser,
			null,
			message,
			null,
			false);

		// send message

		chatSendLogic.sendSystemRbFree (
			chatUser,
			Optional.<Integer>absent (),
			"block_all_confirm",
			Collections.<String,String>emptyMap ());

	}

	@Override
	public
	void userAutoJoin (
			ChatUserRec chatUser,
			MessageRec message) {

		ChatRec chat =
			chatUser.getChat ();

		// join chat

		if (chat.getAutoJoinChat ()
				&& ! chatUser.getOnline ()) {

			userJoin (
				chatUser,
				true,
				message.getThreadId (),
				ChatMessageMethod.sms);

		}

		// join date

		if (chat.getAutoJoinDate ()
				&& chatUser.getDateMode () == ChatUserDateMode.none) {

			chatDateLogic.userDateStuff (
				chatUser,
				null,
				message,
				chatUser.getMainChatUserImage () != null ?
					ChatUserDateMode.photo : ChatUserDateMode.text,
				true);

		}

	}

	@Override
	public
	void userJoin (
			ChatUserRec chatUser,
			boolean sendMessage,
			Integer threadId,
			ChatMessageMethod deliveryMethod) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// if they're already online do nothing

		if (
			chatUser.getOnline ()
			&& chatUser.getDeliveryMethod () == deliveryMethod
		) {
			return;
		}

		boolean wasOnline =
			chatUser.getOnline ();

		// log the user on

		chatUser

			.setOnline (
				true)

			.setBlockAll (
				false)

			.setLastJoin (
				instantToDate (
					transaction.now ()))

			.setLastAction (
				instantToDate (
					transaction.now ()))

			.setFirstJoin (
				ifNull (
					chatUser.getFirstJoin (),
					instantToDate (
						transaction.now ())))

			.setNextRegisterHelp (
				null)

			.setDeliveryMethod (
				deliveryMethod);

		// schedule an ad

		chatUserLogic.scheduleAd (
			chatUser);

		// create session

		if (! wasOnline) {

			chatUserSessionHelper.insert (
				new ChatUserSessionRec ()

				.setChatUser (
					chatUser)

				.setStartTime (
					instantToDate (
						transaction.now ()))

			);

		}

		// send message

		if (sendMessage) {

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.fromNullable (
					threadId),
				"logon",
				commandHelper.findByCode (
					chat,
					"magic"),
				commandHelper.findByCode (
					chat,
					"help"
				).getId (),
				Collections.<String,String>emptyMap ());

		}

		// lookup location for web users

		if (
			deliveryMethod == ChatMessageMethod.web
			&& (
				chatUser.getLocTime () == null
				|| chatUser.getLocTime ().getTime ()
					< System.currentTimeMillis () - 1000 * 60 * 60
			)
		) {

			final int chatUserId =
				chatUser.getId ();

			locatorManager.locate (
				chat.getLocator ().getId (),
				chatUser.getNumber ().getId (),
				serviceHelper.findByCode (chat, "default").getId (),
				chatUserLogic.getAffiliateId (chatUser),
				new LocatorManager.AbstractCallback () {

				@Override
				public
				void success (
						LongLat longLat) {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite ();

					ChatUserRec chatUser =
						chatUserHelper.find (
							chatUserId);

					chatUser

						.setLocLongLat (
							longLat)

						.setLocTime (
							transaction.timestamp ());

					transaction.commit ();

					log.info (
						stringFormat (
							"Got location for %s: %s",
							chatUser.getCode (),
							longLat));

				}

			});

		}

	}

	@Override
	public
	void userLogoffWithMessage (
			ChatUserRec chatUser,
			Integer threadId,
			boolean automatic) {

		ChatRec chat = chatUser.getChat ();

		// if they aren't online possibly send them a stop dating hint

		if (! chatUser.getOnline ()) {

			if (chatUser.getDateMode () != ChatUserDateMode.none) {

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						threadId),
					"date_stop_hint",
					commandHelper.findByCode (chat, "help"),
					0,
					Collections.<String,String>emptyMap ());

			}

			return;

		}

		// log the user off

		chatUserLogic.logoff (
				chatUser,
				automatic);

		// send a message

		if (chatUser.getNumber () != null) {

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.fromNullable (
					threadId),
				"logoff_confirm",
				Collections.<String,String>emptyMap ());

		}

	}

	@Override
	public
	void monitorsToTarget (
			ChatRec chat,
			Gender gender,
			Orient orient,
			int target) {

		// fetch all appropriate monitors

		List<ChatUserRec> allMonitors =
			chatUserHelper.find (
				chat,
				ChatUserType.monitor,
				orient,
				gender);

		// now sort into online and offline ones

		List<ChatUserRec> onlineMonitors =
			new ArrayList<ChatUserRec> ();

		List<ChatUserRec> offlineMonitors =
			new ArrayList<ChatUserRec> ();

		for (ChatUserRec monitor : allMonitors) {

			if (monitor.getOnline ()) {
				onlineMonitors.add (monitor);
			} else {
				offlineMonitors.add (monitor);
			}
		}

		// put monitors online
		while (onlineMonitors.size () < target
				&& offlineMonitors.size () > 0) {

			ChatUserRec monitor =
				offlineMonitors.remove (
					random.nextInt (offlineMonitors.size ()));

			monitor.setOnline (true);
			onlineMonitors.add (monitor);
		}

		// take monitors offline
		while (onlineMonitors.size () > target) {

			ChatUserRec monitor =
				onlineMonitors.remove (
					random.nextInt (onlineMonitors.size ()));

			monitor.setOnline (false);
			offlineMonitors.add (monitor);
		}
	}

	@Override
	public
	void chatUserSetName (
			ChatUserRec chatUser,
			String name,
			Integer threadId) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// create the chat user name


		ChatUserNameRec chatUserName =
			chatUserNameHelper.insert (
				new ChatUserNameRec ()

			.setChatUser (
				chatUser)

			.setCreationTime (
				transaction.timestamp ())

			.setOriginalName (
				name)

			.setEditedName (
				name)

			.setStatus (
				ChatUserInfoStatus.moderatorPending)

			.setThreadId (
				threadId)

		);

		chatUser.getChatUserNames ().add (chatUserName);

		chatUser
			.setNewChatUserName (chatUserName);

		// create the queue item

		if (chatUser.getQueueItem () == null) {

			QueueItemRec qi =
				queueLogic.createQueueItem (
					queueLogic.findQueue (chat, "user"),
					chatUser,
					chatUser,
					chatUser.getPrettyName (),
					"Name to approve");

			chatUser.setQueueItem (qi);

		}

		// send reply

		if (threadId != null)

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (threadId),
				"name_confirm",
				commandHelper.findByCode (chat, "magic"),
				commandHelper.findByCode (chat, "name").getId (),
				ImmutableMap.<String,String>builder ()
					.put ("newName", name)
					.build ());

	}

	@Override
	public
	void setServiceId (
			ReceivedMessage receivedMessage,
			Record<?> object,
			String code) {

		ServiceRec service =
			serviceHelper.findByCode (
				object,
				code);

		receivedMessage

			.setServiceId (
				service.getId ());

	}

}