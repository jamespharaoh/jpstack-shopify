package wbs.apn.chat.core.logic;

import java.util.List;

import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.ReceivedMessage;

public
interface ChatMiscLogic {

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
	List<ChatUserRec> getOnlineMonitorsForOutbound (
			ChatUserRec thisUser);

	/**
	 * Get the closest online monitor suitable to use for an outbound message.
	 *
	 * @see getOnlineMoniorsForOutbound for detailed criteria.
	 *
	 * @param thisUser
	 *            User message is to be sent to
	 * @return Monitor to send
	 */
	ChatUserRec getOnlineMonitorForOutbound (
			ChatUserRec thisUser);

	void blockAll (ChatUserRec chatUser, MessageRec message);

	void userJoin (
			ChatUserRec chatUser,
			boolean sendMessage,
			Integer threadId,
			ChatMessageMethod deliveryMethod);

	void userLogoffWithMessage (
			ChatUserRec chatUser,
			Integer threadId,
			boolean automatic);

	void monitorsToTarget (
			ChatRec chat,
			Gender gender,
			Orient orient,
			int target);

	void setServiceId (
			ReceivedMessage receivedMessage,
			Record<?> object,
			String code);

	void userAutoJoin (
			ChatUserRec chatUser,
			MessageRec message);

	void chatUserSetName (
			ChatUserRec chatUser,
			String name,
			Integer threadId);

}