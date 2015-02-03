package wbs.imchat.core.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public class ImChatMessageListSuccess {

	@DataAttribute
	String status = "success";

	@DataAttribute
	List<ImChatMessageData> messages = new ArrayList<ImChatMessageData> ();

}
