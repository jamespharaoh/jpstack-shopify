package wbs.console.forms.core;

import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.Misc.eitherGetLeft;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.isRight;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentWithClass;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.ResultUtils.getError;
import static wbs.utils.etc.ResultUtils.isError;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringSplitColon;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.feature.FeatureChecker;
import wbs.console.forms.context.FormContext;
import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldConstraintValidator;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldNativeMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormFieldValueValidator;
import wbs.console.forms.types.FormType;
import wbs.console.forms.types.FormUpdateResult;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("updatableFormField")
@DataClass ("updatablesform-field")
public
class UpdatableFormField <Container, Generic, Native, Interface>
	implements FormField <Container, Generic, Native, Interface> {

	// singleton dependencies

	@SingletonDependency
	FeatureChecker featureChecker;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	@Getter
	Boolean virtual = false;

	@Getter
	Boolean group = false;

	@DataAttribute
	@Getter @Setter
	Boolean large = false;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String label;

	@DataAttribute
	@Getter @Setter
	Supplier <Optional <Generic>> defaultValueSupplier;

	@DataAttribute
	@Getter @Setter
	String viewPriv;

	@DataAttribute
	@Getter @Setter
	String managePriv;

	@DataAttribute
	@Getter @Setter
	String featureCode;

	@Getter @Setter
	Set <ScriptRef> scriptRefs =
		new LinkedHashSet<> ();

	@Getter @Setter
	FormFieldAccessor <Container, Native> accessor;

	@Getter @Setter
	FormFieldNativeMapping <Container, Generic, Native> nativeMapping;

	@Getter @Setter
	List<FormFieldValueValidator <Generic>> valueValidators;

	@Getter @Setter
	FormFieldConstraintValidator <Container, Native> constraintValidator;

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, Interface> interfaceMapping;

	@Getter @Setter
	Map <String, FormFieldInterfaceMapping <Container, Generic, String>>
	shortcutInterfaceMappings;

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, String> csvMapping;

	@Getter @Setter
	FormFieldRenderer <Container, Interface> renderer;

	@Getter @Setter
	FormFieldUpdateHook <Container, Generic, Native> updateHook;

	// implementation

	@Override
	public
	Boolean fileUpload () {
		return renderer.fileUpload ();
	}

	@Override
	public
	boolean canView (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canView");

		) {

			// check feature

			if (

				isNotNull (
					featureCode)

				&& ! featureChecker.checkFeatureAccess (
					transaction,
					privChecker,
					featureCode)

			) {
				return false;
			}

			// check view priv

			if (
				isNull (
					viewPriv)
			) {
				return true;
			}

			List <String> privParts =
				stringSplitColon (
					viewPriv);

			if (
				collectionHasOneElement (
					privParts)
			) {

				String privCode =
					privParts.get (0);

				return privChecker.canRecursive (
					transaction,
					(Record<?>) container,
					privCode);

			} else if (
				collectionHasTwoElements (
					privParts)
			) {

				String delegatePath =
					privParts.get (0);

				String privCode =
					privParts.get (1);

				Record <?> delegate =
					genericCastUnchecked (
						objectManager.dereferenceRequired (
							transaction,
							container,
							delegatePath,
							context.hints ()));

				return privChecker.canRecursive (
					transaction,
					delegate,
					privCode);

			} else {

				throw new RuntimeException ();

			}

		}

	}

	@Override
	public
	void init (
			String fieldSetName) {

	}

	@Override
	public
	void setDefault (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setDefault");

		) {

			if (
				isNotNull (
					defaultValueSupplier)
			) {

				Optional <Native> nativeValue =
					nativeMapping.genericToNative (
						transaction,
						container,
						defaultValueSupplier.get ());

				accessor.write (
					transaction,
					container,
					nativeValue);

			}

		}

	}

	@Override
	public
	void renderTableCellList (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container,
			@NonNull Boolean link,
			@NonNull Long columnSpan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderTableCellList");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			renderer.renderHtmlTableCellList (
				transaction,
				context.formatWriter (),
				container,
				context.hints (),
				interfaceValue,
				link,
				columnSpan);

		}

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container,
			@NonNull Long columnSpan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderTableCellProperties");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			renderer.renderHtmlTableCellProperties (
				transaction,
				context.formatWriter (),
				container,
				context.hints (),
				interfaceValue,
				true,
				columnSpan);

		}

	}

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormTemporarilyHidden");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			renderer.renderFormTemporarilyHidden (
				context.submission (),
				context.formatWriter (),
				container,
				context.hints (),
				interfaceValue,
				context.formType (),
				context.formName ());

		}

	}

	@Override
	public
	void renderFormRow (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container,
			@NonNull Optional <String> error) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormRow");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			htmlTableRowOpen (
				context.formatWriter ());

			htmlTableHeaderCellWrite (
				context.formatWriter (),
				label ());

			htmlTableCellOpen (
				context.formatWriter (),
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"text-align",
						renderer.propertiesAlign ().name ())));

			renderer.renderFormInput (
				transaction,
				context.submission (),
				context.formatWriter (),
				container,
				context.hints (),
				interfaceValue,
				context.formType (),
				context.formName ());

			if (
				optionalIsPresent (
					error)
			) {

				context.formatWriter ().writeLineFormat (
					"<br>");

				context.formatWriter ().writeLineFormat (
					"%h",
					error.get ());

			}

			htmlTableCellClose (
				context.formatWriter ());

			htmlTableRowClose (
				context.formatWriter ());

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormReset");

		) {

			if (

				enumInSafe (
					context.formType (),
					FormType.create,
					FormType.perform,
					FormType.search)

				&& isNotNull (
					defaultValueSupplier)

			) {

				Optional <Interface> interfaceValue =
					requiredValue (
						eitherGetLeft (
							interfaceMapping.genericToInterface (
								transaction,
								container,
								context.hints (),
								defaultValueSupplier.get ())));

				renderer.renderFormReset (
					transaction,
					context.formatWriter (),
					container,
					interfaceValue,
					context.formName ());

			} else {

				Optional <Native> nativeValue =
					requiredValue (
						accessor.read (
							transaction,
							container));

				Optional <Generic> genericValue =
					requiredValue (
						nativeMapping.nativeToGeneric (
							transaction,
							container,
							nativeValue));

				Optional <Interface> interfaceValue =
					requiredValue (
						eitherGetLeft (
							interfaceMapping.genericToInterface (
								transaction,
								container,
								context.hints (),
								genericValue)));


				renderer.renderFormReset (
					transaction,
					context.formatWriter (),
					container,
					interfaceValue,
					context.formName ());

			}

		}

	}

	@Override
	public
	void renderCsvRow (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderCsvRow");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			String csvValue =
				optionalOr (
					eitherGetLeft (
						csvMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)),
					"");

			context.formatWriter ().writeFormat (
				"\"%s\"",
				csvValue.replace (
					"\"",
					"\"\""));

		}

	}

	@Override
	public
	FormUpdateResult <Generic, Native> update (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update");

		) {

			// do nothing if no value present in form

			if (
				! renderer.formValuePresent (
					context.submission (),
					context.formName ())
			) {

				return new FormUpdateResult <Generic, Native> ()

					.updated (
						false)

					.error (
						optionalAbsent ());

			}

			// get interface value from form

			Either <Optional <Interface>, String> newInterfaceValue =
				requiredValue (
					renderer.formToInterface (
						transaction,
						context.submission (),
						context.formName ()));

			if (
				isError (
					newInterfaceValue)
			) {

				return new FormUpdateResult <Generic, Native> ()

					.updated (
						false)

					.error (
						optionalOf (
							getError (
								newInterfaceValue)));

			}

			// convert to generic

			Either <Optional <Generic>, String> interfaceToGenericResult =
				interfaceMapping.interfaceToGeneric (
					transaction,
					container,
					context.hints (),
					resultValueRequired (
						newInterfaceValue));

			if (
				isRight (
					interfaceToGenericResult)
			) {

				return new FormUpdateResult <Generic, Native> ()

					.updated (
						false)

					.error (
						optionalOfFormat (
							interfaceToGenericResult.right ().value ()));

			}

			Optional <Generic> newGenericValue =
				interfaceToGenericResult.left ().value ();

			// perform value validation

			for (
				FormFieldValueValidator <Generic> valueValidator
					: valueValidators
			) {

				Optional <String> valueError =
					valueValidator.validate (
						newGenericValue);

				if (
					optionalIsPresent (
						valueError)
				) {

					return new FormUpdateResult <Generic, Native> ()

						.updated (
							false)

						.error (
							optionalOf (
								valueError.get ()));

				}

			}

			// convert to native

			Optional <Native> newNativeValue =
				requiredValue (
					nativeMapping.genericToNative (
						transaction,
						container,
						newGenericValue));

			// check new value

			Optional <String> constraintError =
				constraintValidator.validate (
					transaction,
					container,
					newNativeValue);

			if (
				optionalIsPresent (
					constraintError)
			) {

				return new FormUpdateResult <Generic, Native> ()

					.updated (
						false)

					.error (
						constraintError);

			}

			// get the current value, if it is the same, do nothing

			Optional <Native> oldNativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> oldGenericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						oldNativeValue));

			if (
				optionalEqualOrNotPresentWithClass (
					Object.class,
					oldGenericValue,
					newGenericValue)
			) {

				return new FormUpdateResult <Generic, Native> ()

					.updated (
						false)

					.error (
						optionalAbsent ());

			}

			// set the new value

			accessor.write (
				transaction,
				container,
				newNativeValue);

			return new FormUpdateResult <Generic, Native> ()

				.updated (
					true)

				.oldGenericValue (
					oldGenericValue)

				.newGenericValue (
					newGenericValue)

				.oldNativeValue (
					oldNativeValue)

				.newNativeValue (
					newNativeValue)

				.error (
					optionalAbsent ());

		}

	}

	@Override
	public
	void runUpdateHook (
			@NonNull Transaction parentTransaction,
			@NonNull FormContext <Container> context,
			@NonNull Container container,
			@NonNull FormUpdateResult <Generic, Native> updateResult,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		updateHook.onUpdate (
			parentTransaction,
			context.requestContext (),
			updateResult,
			container,
			linkObject,
			objectRef,
			objectType);

	}

}