package wbs.platform.background.fixture;

import static wbs.utils.etc.NetworkUtils.runHostname;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringReplaceAllSimple;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.background.metamodel.BackgroundProcessSpec;
import wbs.platform.background.model.BackgroundProcessObjectHelper;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.utils.time.DurationFormatter;

@PrototypeComponent ("backgroundProcessBuilder")
@ModelMetaBuilderHandler
public
class BackgroundProcessBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	BackgroundProcessObjectHelper backgroundProcessHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	DurationFormatter durationFormatter;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	BackgroundProcessSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			try {

				taskLogger.noticeFormat (
					"Create background process %s.%s",
					hyphenToCamel (
						spec.objectTypeCode ()),
					simplifyToCodeRequired (
						spec.name ()));

				createBackgroundProcess (
					taskLogger);

			} catch (Exception exception) {

				throw new RuntimeException (
					stringFormat (
						"Error creating background process %s.%s",
						spec.objectTypeCode (),
						simplifyToCodeRequired (
							spec.name ())));

			}

		}

	}

	private
	void createBackgroundProcess (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createBackgroundProcess");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"BackgroundProcessBuilder.createBackgroundProcess ()",
					this);

		) {

			// lookup parent type

			String parentTypeCode =
				hyphenToUnderscore (
					spec.objectTypeCode ());

			ObjectTypeRec parentType =
				objectTypeHelper.findByCodeRequired (
					GlobalId.root,
					parentTypeCode);

			// create background process

			backgroundProcessHelper.insert (
				taskLogger,
				backgroundProcessHelper.createInstance ()

				.setParentType (
					parentType)

				.setCode (
					simplifyToCodeRequired (
						stringReplaceAllSimple (
							"${hostname}",
							runHostname (),
							spec.name ())))

				.setName (
					stringReplaceAllSimple (
						"${hostname}",
						runHostname (),
						spec.name ()))

				.setDescription (
					stringReplaceAllSimple (
						"${hostname}",
						runHostname (),
						spec.description ()))

				.setFrequency (
					optionalOrNull (
						durationFormatter.stringToDuration (
							spec.frequency ())))


			);

			// commit transaction

			transaction.commit ();

		}

	}

}
