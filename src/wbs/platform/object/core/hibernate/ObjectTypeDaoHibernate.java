package wbs.platform.object.core.hibernate;

import static wbs.utils.collection.MapUtils.mapContainsKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.MapUtils.mapWithDerivedKeyAndValue;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectTypeRegistry;

import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.scaffold.model.RootRec;

@SingletonComponent ("objectTypeDao")
public
class ObjectTypeDaoHibernate
	extends HibernateDao
	implements
		ObjectTypeDao,
		ObjectTypeRegistry {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	Map <String, Long> typeIdsByCode;
	Map <Long, String> codesByTypeId;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

			OwnedTransaction transaction =
				database.beginReadOnly (
					taskLogger,
					"setup (taskLogger)",
					this);

		) {

			List <ObjectTypeRec> objectTypes =
				findAll ();

			typeIdsByCode =
				mapWithDerivedKeyAndValue (
					objectTypes,
					ObjectTypeRec::getCode,
					ObjectTypeRec::getId);

			codesByTypeId =
				mapWithDerivedKeyAndValue (
					objectTypes,
					ObjectTypeRec::getId,
					ObjectTypeRec::getCode);

		}

	}

	// public interface

	@Override
	public
	ObjectTypeRec findById (
			@NonNull Long id) {

		return get (
			ObjectTypeRec.class,
			id);

	}

	@Override
	public
	ObjectTypeRec findByCode (
			@NonNull String code) {

		return findOneOrNull (
			"findByCode (code)",
			ObjectTypeRec.class,

			createCriteria (
				ObjectTypeRec.class,
				"_objectType")

			.add (
				Restrictions.eq (
					"_objectType.code",
					code))

			.setCacheable (
				true)

		);

	}

	@Override
	public
	List <ObjectTypeRec> findAll () {

		return findMany (
			"findAll ()",
			ObjectTypeRec.class,

			createCriteria (
				ObjectTypeRec.class)

		);

	}

	@Override
	public
	Class<? extends Record<?>> objectTypeRecordClass () {

		return ObjectTypeRec.class;

	}

	@Override
	public
	Class<? extends Record<?>> rootRecordClass () {

		return RootRec.class;

	}

	@Override
	public
	boolean codeExists (
			@NonNull String code) {

		return mapContainsKey (
			typeIdsByCode,
			code);

	}

	@Override
	public
	Long typeIdForCodeRequired (
			@NonNull String code) {

		return mapItemForKeyRequired (
			typeIdsByCode,
			code);

	}

	@Override
	public
	boolean typeIdExists (
			@NonNull Long typeId) {

		return mapContainsKey (
			codesByTypeId,
			typeId);

	}

	@Override
	public
	String codeForTypeIdRequired (
			@NonNull Long typeId) {

		return mapItemForKeyRequired (
			codesByTypeId,
			typeId);

	}

}
