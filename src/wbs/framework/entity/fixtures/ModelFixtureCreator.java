package wbs.framework.entity.fixtures;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class ModelFixtureCreator {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory <?, Transaction>> builderFactoryProvider;

	@PrototypeDependency
	@ModelMetaBuilderHandler
	Map <Class <?>, ComponentProvider <Object>> modelMetaBuilderProviders;

	// state

	Builder <Transaction> fixtureBuilder;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			createFixtureBuilder (
				taskLogger);

		}

	}

	// implementation

	private
	void createFixtureBuilder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtureBuilder");

		) {

			fixtureBuilder =
				builderFactoryProvider.get ()

				.contextClass (
					Transaction.class)

				.addBuilders (
					taskLogger,
					modelMetaBuilderProviders)

				.create (
					taskLogger);

		}

	}

	public
	void runModelFixtureCreators (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					"runModelFixtureCreators");

		) {

			transaction.noticeFormat (
				"About to create model fixtures");

			for (
				ModelMetaSpec spec
					: modelMetaLoader.modelMetas ().values ()
			) {

				Model <?> model =
					entityHelper.modelsByName ().get (
						spec.name ());

				fixtureBuilder.descend (
					transaction,
					spec,
					spec.children (),
					model,
					MissingBuilderBehaviour.error);

			}

			transaction.commit ();

			transaction.noticeFormat (
				"All model fixtures created successfully");

		}

	}

}
