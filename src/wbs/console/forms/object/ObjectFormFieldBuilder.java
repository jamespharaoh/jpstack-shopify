package wbs.console.forms.object;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualSafe;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.basic.IdentityFormFieldInterfaceMapping;
import wbs.console.forms.basic.IdentityFormFieldNativeMapping;
import wbs.console.forms.basic.RequiredFormFieldValueValidator;
import wbs.console.forms.basic.SimpleFormFieldAccessor;
import wbs.console.forms.core.FormFieldBuilderContext;
import wbs.console.forms.core.FormFieldSetImplementation;
import wbs.console.forms.core.ReadOnlyFormField;
import wbs.console.forms.core.UpdatableFormField;
import wbs.console.forms.logic.FormFieldPluginManagerImplementation;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldNativeMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("objectFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectFormFieldBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	FormFieldPluginManagerImplementation formFieldPluginManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <DereferenceFormFieldAccessor>
	dereferenceFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <ObjectCsvFormFieldInterfaceMapping>
	objectCsvFormFieldInterfaceMappingProvider;

	@PrototypeDependency
	Provider <ObjectFormFieldRenderer>
	objectFormFieldRendererProvider;

	@PrototypeDependency
	Provider <ObjectIdFormFieldNativeMapping>
	objectIdFormFieldNativeMappingProvider;

	@PrototypeDependency
	Provider <ReadOnlyFormField>
	readOnlyFormFieldProvider;

	@PrototypeDependency
	Provider <RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@PrototypeDependency
	Provider <SimpleFormFieldAccessor>
	simpleFormFieldAccessorProvider;

	@PrototypeDependency
	Provider <ObjectFormFieldConstraintValidator>
	objectFormFieldConstraintValidatorProvider;

	@PrototypeDependency
	Provider <UpdatableFormField>
	updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	ObjectFormFieldSpec spec;

	@BuilderTarget
	FormFieldSetImplementation formFieldSet;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			String name =
				spec.name ();

			String fieldName =
				ifNull (
					spec.fieldName (),
					name);

			String label =
				ifNull (
					spec.label (),
					capitalise (
						camelToSpaces (
							name.endsWith ("Id")
								? name.substring (
									0,
									name.length () - 2)
								: name)));

			Boolean nullable =
				ifNull (
					spec.nullable (),
					false);

			Boolean readOnly =
				ifNull (
					spec.readOnly (),
					false);

			/*
			Boolean dynamic =
				ifNull (
					spec.dynamic(),
					false);
			*/

			Optional <ConsoleHelper <?>> consoleHelper;

			if (
				isNotNull (
					spec.objectTypeName ())
			) {

				consoleHelper =
					optionalOf (
						objectManager.findConsoleHelperRequired (
							spec.objectTypeName ()));

				if (
					optionalIsNotPresent (
						consoleHelper)
				) {

					throw new RuntimeException (
						stringFormat (
							"Console helper does not exist: %s",
							spec.objectTypeName ()));

				}

			} else {

				consoleHelper =
					optionalAbsent ();

			}

			String rootFieldName =
				spec.rootFieldName ();

			// field type

			Optional <Class <?>> propertyClassOptional =
				objectManager.dereferenceType (
					taskLogger,
					optionalOf (
						context.containerClass ()),
					optionalOf (
						fieldName));

			// accessor

			FormFieldAccessor accessor;

			if (
				isNotNull (
					spec.fieldName ())
			) {

				accessor =
					dereferenceFormFieldAccessorProvider.get ()

					.path (
						spec.fieldName ());

			} else {

				accessor =
					simpleFormFieldAccessorProvider.get ()

					.name (
						name)

					.nativeClass (
						propertyClassOptional.get ());

			}

			// native mapping

			FormFieldNativeMapping nativeMapping;

			if (
				optionalValueEqualSafe (
					propertyClassOptional,
					Long.class)
			) {

				nativeMapping =
					objectIdFormFieldNativeMappingProvider.get ()

					.consoleHelper (
						consoleHelper.orNull ());

			} else {

				nativeMapping =
					identityFormFieldNativeMappingProvider.get ();

			}

			// value validator

			List <FormFieldValueValidator> valueValidators =
				new ArrayList<> ();

			if (! nullable) {

				valueValidators.add (
					requiredFormFieldValueValidatorProvider.get ());

			}

			// constraint validator

			FormFieldConstraintValidator constraintValidator =
				objectFormFieldConstraintValidatorProvider.get ();

			// interface mapping

			FormFieldInterfaceMapping interfaceMapping =
				identityFormFieldInterfaceMappingProvider.get ();

			// csv mapping

			FormFieldInterfaceMapping csvMapping =
				objectCsvFormFieldInterfaceMappingProvider.get ()

				.rootFieldName (
					rootFieldName);

			// renderer

			FormFieldRenderer renderer =
				objectFormFieldRendererProvider.get ()

				.name (
					name)

				.label (
					label)

				.nullable (
					nullable)

				.rootFieldName (
					rootFieldName)

				.entityFinder (
					consoleHelper.orNull ())

				.mini (
					isNotNull (
						spec.objectTypeName ()));

			// update hook

			FormFieldUpdateHook updateHook =
				formFieldPluginManager.getUpdateHook (
					context,
					context.containerClass (),
					name);

			// field

			if (! readOnly) {

				formFieldSet.addFormItem (
					updatableFormFieldProvider.get ()

					.name (
						name)

					.label (
						label)

					.accessor (
						accessor)

					.nativeMapping (
						nativeMapping)

					.valueValidators (
						valueValidators)

					.constraintValidator (
						constraintValidator)

					.interfaceMapping (
						interfaceMapping)

					.csvMapping (
						csvMapping)

					.renderer (
						renderer)

					.updateHook (
						updateHook)

					.viewPriv (
						spec.viewPrivCode ())

					.featureCode (
						spec.featureCode ())

				);

			} else {

				formFieldSet.addFormItem (
					readOnlyFormFieldProvider.get ()

					.name (
						name)

					.label (
						label)

					.accessor (
						accessor)

					.nativeMapping (
						nativeMapping)

					.interfaceMapping (
						interfaceMapping)

					.csvMapping (
						csvMapping)

					.renderer (
						renderer)

					.viewPriv (
						spec.viewPrivCode ())

					.featureCode (
						spec.featureCode ())

				);

			}

		}

	}

}