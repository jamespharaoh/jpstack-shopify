package wbs.integrations.shopify.daemon;

import static wbs.utils.collection.IterableUtils.iterableFilterByClass;
import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.PropertyUtils.propertyClassForObject;
import static wbs.utils.etc.PropertyUtils.propertyGetSimple;
import static wbs.utils.etc.PropertyUtils.propertySetAuto;
import static wbs.utils.etc.TypeUtils.classInSafe;
import static wbs.utils.etc.TypeUtils.dynamicCastRequired;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.RecordSpec;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.shopify.apiclient.ShopifyApiClient;
import wbs.integrations.shopify.apiclient.ShopifyApiClientCredentials;
import wbs.integrations.shopify.apiclient.ShopifyApiLogic;
import wbs.integrations.shopify.apiclient.ShopifyApiResponseItem;
import wbs.integrations.shopify.apiclient.product.ShopifyProductApiClient;
import wbs.integrations.shopify.metamodel.ShopifySynchronisationSpec;
import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;
import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectObjectHelper;
import wbs.integrations.shopify.model.ShopifyEventSubjectRec;
import wbs.integrations.shopify.model.ShopifyObjectHelper;
import wbs.integrations.shopify.model.ShopifyProductObjectHelper;
import wbs.integrations.shopify.model.ShopifyRecord;

import wbs.platform.daemon.ObjectDaemon;

@SingletonComponent ("shopifyAccountEventSubjectProcessObjectDaemonHelper")
public
class ShopifyAccountEventSubjectProcessObjectDaemonHelper
	implements ObjectDaemon <Long> {

	// singleton dependencies

	@SingletonDependency
	private
	Database database;

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	ModelMetaLoader modelMetaLoader;

	@SingletonDependency
	private
	ShopifyAccountObjectHelper shopifyAccountHelper;

	@SingletonDependency
	private
	ShopifyApiLogic shopifyApiLogic;

	@SingletonDependency
	private
	ShopifyEventSubjectObjectHelper shopifyEventSubjectHelper;

	@SingletonDependency
	private
	ShopifyProductApiClient shopifyProductApiClient;

	@SingletonDependency
	private
	ShopifyProductObjectHelper shopifyProductHelper;

	// details

	@Override
	public
	String backgroundProcessName () {
		return "shopify-event-subject.process";
	}

	// object daemon implementation

	@Override
	public
	List <Long> findObjectIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"findObjectIds");

		) {

			return shopifyEventSubjectHelper.findIdsPendingLimit (
				transaction,
				16384l);

		}

	}

	@Override
	public
	void processObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long eventSubjectId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processObject",
					keyEqualsDecimalInteger (
						"eventSubjectId",
						eventSubjectId));

		) {

			EventSubjectData eventData =
				getEventSubjectData (
					taskLogger,
					eventSubjectId);

			switch (eventData.subjectType ()) {

			case "Product":

				updateRecord (
					taskLogger,
					eventData,
					shopifyProductHelper,
					shopifyProductApiClient);

				break;

			default:

				processUnrecognisedEventSubjectType (
					taskLogger,
					eventData);

				taskLogger.warningFormat (
					"Ignoring event for unknown subject type: %s",
					eventData.subjectType ());

			}

		}

	}

	// private implementation

	private
	EventSubjectData getEventSubjectData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long eventSubjectId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getEventSubjectType")

		) {

			ShopifyEventSubjectRec shopifyEventSubject =
				shopifyEventSubjectHelper.findRequired (
					transaction,
					eventSubjectId);

			return new EventSubjectData ()

				.accountId (
					shopifyEventSubject.getAccount ().getId ())

				.eventSubjectId (
					eventSubjectId)

				.subjectType (
					shopifyEventSubject.getSubjectType ())

				.subjectId (
					shopifyEventSubject.getSubjectId ())

				.eventId (
					shopifyEventSubject.getLastEventId ())

				.eventVerb (
					shopifyEventSubject.getLastEventVerb ())

				.credentials (
					shopifyApiLogic.getApiCredentials (
						transaction,
						shopifyEventSubject.getAccount ()))

			;

		}

	}

	private <
		RecordType extends ShopifyRecord <RecordType>,
		ResponseType extends ShopifyApiResponseItem
	>
	void updateRecord (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventSubjectData eventData,
			@NonNull ShopifyObjectHelper <RecordType> objectHelper,
			@NonNull ShopifyApiClient <?, ResponseType> apiClient) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"updateRecord");

		) {

			Optional <ResponseType> responseOptional;

			if (
				stringNotEqualSafe (
					eventData.eventVerb (),
					"destroy")
			) {

				responseOptional =
					apiClient.get (
						taskLogger,
						eventData.credentials (),
						eventData.subjectId ());

			} else {

				responseOptional =
					optionalAbsent ();

			}

			updateRecordLocal (
				taskLogger,
				eventData,
				objectHelper,
				responseOptional);

		}

	}

	private <
		RecordType extends ShopifyRecord <RecordType>,
		ResponseType extends ShopifyApiResponseItem
	>
	void updateRecordLocal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventSubjectData eventSubjectData,
			@NonNull ShopifyObjectHelper <RecordType> objectHelper,
			@NonNull Optional <ResponseType> responseOptional) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"updateRecord");

		) {

			ShopifyEventSubjectRec shopifyEventSubject =
				shopifyEventSubjectHelper.findRequired (
					transaction,
					eventSubjectData.eventSubjectId ());

			if (! shopifyEventSubject.getPending ()) {
				return;
			}

			ShopifyAccountRec shopifyAccount =
				shopifyEventSubject.getAccount ();

			Optional <RecordType> localOptional =
				objectHelper.findByShopifyId (
					transaction,
					shopifyAccount,
					eventSubjectData.subjectId ());

			if (

				optionalIsNotPresent (
					responseOptional)

				&& optionalIsPresent (
					localOptional)

			) {

				// delete object

				RecordType local =
					optionalGetRequired (
						localOptional);

				local.setDeleted (
					true);

				// TODO events

			} else if (

				optionalIsPresent (
					responseOptional)

			) {

				// create or update object

				ResponseType response =
					optionalGetRequired (
						responseOptional);

				RecordType object;

				if (
					optionalIsPresent (
						localOptional)
				) {

					object =
						optionalGetRequired (
							localOptional);

				} else {

					object =
						objectHelper.createInstance ()

						.setAccount (
							shopifyAccount)

						.setShopifyId (
							response.id ())

					;

				}

				RecordSpec recordSpec =
					mapItemForKeyRequired (
						modelMetaLoader.modelMetas (),
						objectHelper.objectName ());

				ShopifySynchronisationSpec synchronisationSpec =
					iterableOnlyItemRequired (
						iterableFilterByClass (
							recordSpec.children (),
							ShopifySynchronisationSpec.class));

				for (
					String fieldName
						: synchronisationSpec.scalarFieldNames ()
				) {

					Optional <Object> fieldValueOptional =
						optionalFromNullable (
							propertyGetSimple (
								response,
								hyphenToCamel (
									fieldName)));

					if (
						optionalIsPresent (
							fieldValueOptional)
					) {

						Object fieldValue =
							optionalGetRequired (
								fieldValueOptional);

						Class <?> targetClass =
							propertyClassForObject (
								object,
								hyphenToCamel (
									fieldName));

						// convert string to instant

						if (

							classInSafe (
								fieldValue.getClass (),
								String.class)

							&& classInSafe (
								targetClass,
								Instant.class,
								ReadableInstant.class)

						) {

							fieldValue =
								Instant.parse (
									dynamicCastRequired (
										String.class,
										fieldValue));

						}

						fieldValueOptional =
							optionalOf (
								fieldValue);

					}

					propertySetAuto (
						object,
						hyphenToCamel (
							fieldName),
						optionalOrNull (
							fieldValueOptional));

					// TODO events

				}

				// TODO collections

				// TODO metadata

				if (
					optionalIsPresent (
						localOptional)
				) {

					objectHelper.update (
						transaction,
						object);

				} else {

					objectHelper.insert (
						transaction,
						object);

				}

			} else {

				doNothing ();

			}

			// clear pending and commit transaction

			if (
				integerEqualSafe (
					eventSubjectData.eventId (),
					shopifyEventSubject.getLastEventId ())
			) {

				shopifyEventSubject.setPending (
					false);

			}

			transaction.commit ();

		}

	}

	private
	void processUnrecognisedEventSubjectType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull EventSubjectData eventSubjectData) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"processUnrecognisedEventSubjectType");

		) {

			ShopifyEventSubjectRec shopifyEventSubject =
				shopifyEventSubjectHelper.findRequired (
					transaction,
					eventSubjectData.eventSubjectId ());

			if (
				integerEqualSafe (
					eventSubjectData.eventId (),
					shopifyEventSubject.getLastEventId ())
			) {

				shopifyEventSubject.setPending (
					false);

			}

			transaction.commit ();

		}

	}

	// data classes

	@Accessors (fluent = true)
	@Data
	public final static
	class EventSubjectData {

		Long accountId;
		Long eventSubjectId;

		String subjectType;
		Long subjectId;

		Long eventId;
		String eventVerb;

		ShopifyApiClientCredentials credentials;

	}

}
