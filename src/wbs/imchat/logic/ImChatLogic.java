package wbs.imchat.logic;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserRec;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;

public
interface ImChatLogic {

	void conversationEnd (
			TaskLogger parentTaskLogger,
			ImChatConversationRec conversation);

	void conversationEmailSend (
			TaskLogger parentTaskLogger,
			ImChatConversationRec conversation);

	void customerPasswordGenerate (
			TaskLogger parentTaskLogger,
			ImChatCustomerRec customer,
			Optional <UserRec> consoleUser);

}
