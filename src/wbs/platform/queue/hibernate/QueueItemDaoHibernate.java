package wbs.platform.queue.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.model.QueueItemDao;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemSearch;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.UserQueueReport;
import wbs.platform.user.model.UserRec;

public
class QueueItemDaoHibernate
	extends HibernateDao
	implements QueueItemDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Criteria searchCriteria (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemSearch search) {

		Criteria criteria =
			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.createAlias (
				"_queueItem.queueSubject",
				"_queueSubject")

			.createAlias (
				"_queueSubject.queue",
				"_queue")

			.createAlias (
				"_queueItem.processedUser",
				"_processedUser",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"_processedUser.slice",
				"_processedUserSlice",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"_queueItem.queueItemClaim",
				"_queueItemClaim",
				JoinType.LEFT_OUTER_JOIN)

			.createAlias (
				"_queueItemClaim.user",
				"_claimedUser",
				JoinType.LEFT_OUTER_JOIN);

		if (
			isNotNull (
				search.sliceId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_queue.slice.id",
					search.sliceId ()));

		}

		if (
			isNotNull (
				search.queueParentTypeId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_queue.parentType.id",
					search.queueParentTypeId ()));

		}

		if (
			isNotNull (
				search.queueTypeId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_queue.queueType.id",
					search.queueTypeId ()));

		}

		if (
			isNotNull (
				search.queueId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_queue.id",
					search.queueId ()));

		}

		if (
			isNotNull (
				search.createdTime ())
		) {

			criteria.add (
				Restrictions.ge (
					"_queueItem.createdTime",
					search.createdTime ().start ()));

			criteria.add (
				Restrictions.lt (
					"_queueItem.createdTime",
					search.createdTime ().end ()));

		}

		if (
			isNotNull (
				search.claimedUserId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_claimedUser.id",
					search.claimedUserId ()));

		}

		if (
			isNotNull (
				search.processedUserId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_processedUser.id",
					search.processedUserId ()));

		}

		if (
			isNotNull (
				search.state ())
		) {

			criteria.add (
				Restrictions.eq (
					"_queueItem.state",
					search.state ()));

		}

		if (search.filter ()) {

			List <Criterion> filterCriteria =
				new ArrayList<> ();

			if (
				collectionIsNotEmpty (
					search.filterQueueIds ())
			) {

				filterCriteria.add (
					Restrictions.in (
						"_queue.id",
						search.filterQueueIds ()));

			}

			criteria.add (
				Restrictions.or (
					filterCriteria.toArray (
						new Criterion [] {})));

		}

		return criteria;

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemSearch search) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"searchIds");

		) {

			Criteria criteria =
				searchCriteria (
					taskLogger,
					search);

			criteria.addOrder (
				Order.desc (
					"_queueItem.createdTime"));

			criteria.setProjection (
				Projections.id ());

			return findMany (
				"searchIds (search)",
				Long.class,
				criteria);

		}

	}

	@Override
	public
	List <QueueItemRec> find (
			@NonNull List <QueueItemState> states) {

		return findMany (
			"findActive ()",
			QueueItemRec.class,

			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.add (
				Restrictions.in (
					"_queueItem.state",
					states))

		);

	}

	@Override
	public
	List<QueueItemRec> findByCreatedTime (
			@NonNull Interval createdTime) {

		return findMany (
			"findByCreatedTime (createdTime)",
			QueueItemRec.class,

			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.add (
				Restrictions.ge (
					"_queueItem.createdTime",
					createdTime.getStart ()))

			.add (
				Restrictions.lt (
					"_queueItem.createdTime",
					createdTime.getEnd ()))

		);

	}

	@Override
	public
	List <QueueItemRec> findByCreatedTime (
			@NonNull QueueRec queue,
			@NonNull Interval createdTime) {

		return findMany (
			"findByCreatedTime (queue, createdTime)",
			QueueItemRec.class,

			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.createAlias (
				"_queueItem.queueSubject",
				"_queueSubject")

			.add (
				Restrictions.eq (
					"_queueSubject.queue",
					queue))

			.add (
				Restrictions.ge (
					"_queueItem.createdTime",
					createdTime.getStart ()))

			.add (
				Restrictions.lt (
					"_queueItem.createdTime",
					createdTime.getEnd ()))

		);

	}

	@Override
	public
	List<QueueItemRec> findByProcessedTime (
			@NonNull Interval processedTime) {

		return findMany (
			"findByProcessedTime (processedTime)",
			QueueItemRec.class,

			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.add (
				Restrictions.ge (
					"_queueItem.processedTime",
					processedTime.getStart ()))

			.add (
				Restrictions.lt (
					"_queueItem.processedTime",
					processedTime.getEnd ()))

		);

	}

	@Override
	public
	List<QueueItemRec> findByProcessedTime (
			@NonNull UserRec user,
			@NonNull Interval processedTime) {

		return findMany (
			"findByProcessedTime (user, processedTime)",
			QueueItemRec.class,

			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.add (
				Restrictions.eq (
					"_queueItem.processedUser",
					user))

			.add (
				Restrictions.ge (
					"_queueItem.processedTime",
					processedTime.getStart ()))

			.add (
				Restrictions.lt (
					"_queueItem.processedTime",
					processedTime.getEnd ()))

		);

	}

	@Override
	public
	Criteria searchUserQueueReportCriteria (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemSearch search) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"searchUserQueueReportCriteria");

		) {

			Criteria criteria =
				searchCriteria (
					taskLogger,
					search);

			criteria.add (
				Restrictions.isNotNull (
					"_queueItem.processedUser"));

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.property (
						"_queueItem.processedUser"),
					"user")

				.add (
					Projections.count (
						"_queueItem.id"),
					"messageCount")

				.add (
					Projections.min (
						"_queueItem.createdTime"),
					"firstMessage")

				.add (
					Projections.max (
						"_queueItem.createdTime"),
					"lastMessage")

				.add (
					Projections.sqlProjection (
						stringFormat (
							"avg (CASE WHEN {alias}.processed_time IS NULL THEN ",
							"NULL ELSE EXTRACT (EPOCH FROM ({alias}.",
							"processed_time - {alias}.created_time)) END) AS ",
							"time_to_process"),
						new String [] {
							"time_to_process",
						},
						new Type [] {
							LongType.INSTANCE,
						}),
					"timeToProcess")

				.add (
					Projections.groupProperty (
						"_queueItem.processedUser"))

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					UserQueueReport.class));

			return criteria;

		}

	}

	@Override
	public
	List <Long> searchUserQueueReportIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemSearch search) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"searchUserQueueReportIds");

		) {

			Criteria criteria =
				searchCriteria (
					taskLogger,
					search);

			criteria.add (
				Restrictions.isNotNull (
					"_queueItem.processedUser"));

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.distinct (
						Projections.property (
							"_queueItem.processedUser.id")))

				.add (
					Projections.groupProperty (
						"_queueItem.processedUser"))

				.add (
					Projections.groupProperty (
						"_processedUserSlice.code"))

				.add (
					Projections.groupProperty (
						"_processedUser.username"))

			);

			criteria.addOrder (
				Order.asc (
					"_processedUserSlice.code"));

			criteria.addOrder (
				Order.asc (
					"_processedUser.username"));

			return findIdsOnly (
				criteria.list ());

		}

	}

	@Override
	public
	List <Optional <UserQueueReport>> searchUserQueueReports (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemSearch search,
			@NonNull List<Long> objectIds) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"searchUserQueueReports");

		) {

			Criteria criteria =
				searchUserQueueReportCriteria (
					taskLogger,
					search);

			criteria.add (
				Restrictions.in (
					"_processedUser.id",
					objectIds));

			return findOrdered (
				taskLogger,
				UserQueueReport.class,
				objectIds,
				criteria.list ());

		}

	}

}
