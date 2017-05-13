package wbs.console.forms.text;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("json-field")
@PrototypeComponent ("jsonFormFieldSpec")
public
class JsonFormFieldSpec
	implements ConsoleModuleData {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

}