package wbs.console.forms.core;

import static wbs.utils.etc.TypeUtils.classForNameOrThrow;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormItem;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("formFieldSetBuilder")
@ConsoleModuleBuilderHandler
public
class FormFieldSetBuilder <Container>
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	FormFieldSetSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			if (
				spec.objectName () == null
				&& spec.className () == null
			) {

				throw new RuntimeException (
					stringFormat (
						"Form field set %s ",
						spec.name (),
						"in console module %s ",
						spec.consoleModule ().name (),
						"has neither object name nor class"));

			}

			if (
				spec.objectName () != null
				&& spec.className () != null
			) {

				throw new RuntimeException (
					stringFormat (
						"Form field set %s ",
						spec.name (),
						"in console module %s ",
						spec.consoleModule ().name (),
						"has both object name and class"));

			}

			ConsoleHelper <?> consoleHelper;
			Class <Container> containerClass;

			if (spec.objectName () != null) {

				consoleHelper =
					objectManager.findConsoleHelperRequired (
						spec.objectName ());

				Class <Container> containerClassTemp =
					genericCastUnchecked (
						consoleHelper.objectClass ());

				containerClass =
					containerClassTemp;

			} else {

				consoleHelper = null;

				@SuppressWarnings ("unchecked")
				Class <Container> containerClassTemp =
					(Class <Container>)
					classForNameOrThrow (
						spec.className (),
						() -> new RuntimeException (
							stringFormat (
								"Error getting object class %s ",
								spec.className (),
								"for form field set %s ",
								spec.name (),
								"in console module %s",
								spec.consoleModule ().name ())));

				containerClass =
					containerClassTemp;

			}

			FormFieldBuilderContext formFieldBuilderContext =
				new FormFieldBuilderContextImplementation ()

				.containerClass (
					containerClass)

				.consoleHelper (
					consoleHelper);

			FormFieldSetImplementation <Container> formFieldSet =
				new FormFieldSetImplementation <Container> ()

				.name (
					joinWithFullStop (
						spec.consoleModule ().name (),
						spec.name ()))

				.containerClass (
					containerClass);

			builder.descend (
				taskLogger,
				formFieldBuilderContext,
				spec.formFieldSpecs (),
				formFieldSet,
				MissingBuilderBehaviour.error);

			String fullName =
				joinWithFullStop (
					spec.consoleModule ().name (),
					spec.name ());

			for (
				FormItem <?> formItem
					: formFieldSet.formItems ()
			) {

				formItem.init (
					fullName);

			}

			for (
				FormField <?, ?, ?, ?> formField
					: formFieldSet.formFields ()
			) {

				if (formField.fileUpload ()) {
					formFieldSet.fileUpload (true);
				}

			}

			if (formFieldSet.fileUpload () == null)
				formFieldSet.fileUpload (false);

			consoleModule.addFormFieldSet (
				spec.name (),
				formFieldSet);

		}

	}

}