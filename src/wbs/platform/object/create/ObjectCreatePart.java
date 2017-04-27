package wbs.platform.object.create;

import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.RootObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectCreatePart")
public
class ObjectCreatePart <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	String parentPrivCode;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

	// state

	Optional <UpdateResultSet> updateResultSet;

	ConsoleHelper <ParentType> parentHelper;
	List <ParentType> parents;
	ParentType parent;

	ObjectType object;

	Map <String, Object> hints =
		new LinkedHashMap<> ();

	// implementation

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		Set <ScriptRef> scriptRefs =
			new LinkedHashSet<> ();

		scriptRefs.addAll (
			formFieldSet.scriptRefs ());

		return scriptRefs;

	}

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare");

		) {

			prepareParents ();

			// if a field provider was provided

			if (formFieldsProvider != null) {

				prepareFieldSet (
					taskLogger);

			}

			// get update results

			updateResultSet =
				optionalCast (
					UpdateResultSet.class,
					requestContext.request (
						"objectCreateUpdateResultSet"));

			// create dummy instance

			object =
				consoleHelper.createInstance ();

			// set parent

			if (
				parent != null
				&& consoleHelper.canGetParent ()
			) {

				consoleHelper.setParent (
					object,
					parent);

			}

		}

	}

	void prepareParents () {

		ConsoleHelper <ParentType> parentHelperTemp =
			objectManager.findConsoleHelperRequired (
				consoleHelper.parentClass ());

		parentHelper =
			parentHelperTemp;

		if (parentHelper.isRoot ()) {

			ParentType parentTemp =
				genericCastUnchecked (
					rootHelper.findRequired (
						0l));

			parent =
				parentTemp;

			return;

		}

		Optional <Long> parentIdOptional =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (
			optionalIsPresent (
				parentIdOptional)
		) {

			// use specific parent

			parent =
				parentHelper.findRequired (
					optionalGetRequired (
						parentIdOptional));

			return;

		}

		ConsoleHelper <?> grandParentHelper =
			objectManager.findConsoleHelperRequired (
				parentHelper.parentClass ());

		Optional <Long> grandParentIdOptional =
			requestContext.stuffInteger (
				grandParentHelper.idKey ());

		if (
			optionalIsPresent (
				grandParentIdOptional)
		) {

			// show parents based on grand parent

			parents =
				parentHelper.findByParent (
					new GlobalId (
						grandParentHelper.objectTypeId (),
						optionalGetRequired (
							grandParentIdOptional)));

			// set grandparent hints

			Record <?> grandparent =
				grandParentHelper.findRequired (
					optionalGetRequired (
						grandParentIdOptional));

			hints.put (
				"grandparent",
				grandparent);

			hints.put (
				stringFormat (
					"%s.parent",
					consoleHelper.parentFieldName ()),
				grandparent);

			hints.put (
				stringFormat (
					"parent.%s",
					parentHelper.parentFieldName ()),
				grandparent);

			hints.put (
				stringFormat (
					"%s.%s",
					consoleHelper.parentFieldName (),
					parentHelper.parentFieldName ()),
				grandparent);

			return;

		}

		// show all parents

		parents =
			parentHelper.findAll ();

	}

	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			parent != null
				? formFieldsProvider.getFieldsForParent (
					parentTaskLogger,
					parent)
				: formFieldsProvider.getStaticFields ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderHtmlBodyContent");

		) {

			htmlParagraphWriteFormat (
				"Please enter the details for the new %h",
				consoleHelper.shortName ());

			formFieldLogic.outputFormTable (
				taskLogger,
				requestContext,
				formatWriter,
				formFieldSet,
				updateResultSet,
				object,
				hints,
				"post",
				requestContext.resolveLocalUrl (
					"/" + localFile),
				stringFormat (
					"create %h",
					consoleHelper.shortName ()),
				FormType.create,
				"create");

		}

	}

}
