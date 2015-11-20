package wbs.platform.event.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.FormFieldAccessor;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.forms.FormFieldNativeMapping;
import wbs.console.forms.FormFieldPluginManagerImplementation;
import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.HtmlFormFieldRenderer;
import wbs.console.forms.IdentityFormFieldAccessor;
import wbs.console.forms.IdentityFormFieldNativeMapping;
import wbs.console.forms.ReadOnlyFormField;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("eventDetailsFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class EventDetailsFormFieldBuilder {

	// dependencies

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<EventDetailsFormFieldInterfaceMapping>
	eventDetailsFormFieldInterfaceMappingProvider;

	@Inject
	Provider<HtmlFormFieldRenderer>
	htmlFormFieldRendererProvider;

	@Inject
	Provider<IdentityFormFieldAccessor>
	identityFormFieldAccessorProvider;

	@Inject
	Provider<IdentityFormFieldNativeMapping>
	identityFormFieldNativeMappingProvider;

	@Inject
	Provider<ReadOnlyFormField>
	readOnlyFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	EventDetailsFormFieldSpec spec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			"details";

		String label =
			"Details";

		// accessor

		FormFieldAccessor accessor =
			identityFormFieldAccessorProvider.get ();

		// native mapping

		FormFieldNativeMapping<?,?> nativeMapping =
			identityFormFieldNativeMappingProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			eventDetailsFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			htmlFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label);

		// form field

		formFieldSet.formFields ().add (
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

			.renderer (
				renderer)

		);

	}

}