package wbs.applications.imchat.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatMessageRec
	implements CommonRecord<ImChatMessageRec>{

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ImChatConversationRec imChatConversation;

	@IndexField (
		counter = "numMessages")
	Integer index;

	// details

	@SimpleField
	String messageText;

	@ReferenceField (
		nullable = true)
	UserRec senderUser;

	@SimpleField
	Instant timestamp;

	@SimpleField (
		nullable = true)
	Integer price;

	// state

	@ReferenceField (
		nullable = true)
	QueueItemRec queueItem;

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatMessageRec> otherRecord) {

		ImChatMessageRec other =
			(ImChatMessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getImChatConversation (),
				other.getImChatConversation ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();

	}

}