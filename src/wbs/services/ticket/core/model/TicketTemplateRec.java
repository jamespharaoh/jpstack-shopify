package wbs.services.ticket.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class TicketTemplateRec
	implements MinorRecord<TicketTemplateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	TicketManagerRec ticketManager;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@ReferenceField (
			nullable = true)
	TicketStateRec ticketState;

	@SimpleField (
			nullable = true)
	Integer timestamp;

	@DeletedField
	Boolean deleted = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<TicketTemplateRec> otherRecord) {

		TicketTemplateRec other =
			(TicketTemplateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getTicketManager (),
				other.getTicketManager ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
