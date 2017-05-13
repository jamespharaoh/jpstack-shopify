package wbs.console.forms.object;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("id-field")
@PrototypeComponent ("idFormFieldSpec")
public
class IdFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute
	String label;

	@DataAttribute
	String name;

}