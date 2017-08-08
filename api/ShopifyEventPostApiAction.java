package wbs.integrations.shopify.api;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitSlash;

import java.util.List;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;
import wbs.integrations.shopify.model.ShopifyAccountRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectObjectHelper;
import wbs.integrations.shopify.model.ShopifyEventSubjectRec;
import wbs.integrations.shopify.model.ShopifyEventSubjectType;

import wbs.platform.media.logic.MediaLogic;

import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.exceptions.HttpUnprocessableEntityException;
import wbs.web.responder.TextResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("shopifyEventPostApiAction")
public
class ShopifyEventPostApiAction
	implements ApiAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RequestContext requestContext;

	@SingletonDependency
	ShopifyAccountObjectHelper shopifyAccountHelper;

	@SingletonDependency
	ShopifyEventSubjectObjectHelper shopifyEventSubjectHelper;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <TextResponder> textResponderProvider;

	// public implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			// lookup account

			Long shopifyAccountId =
				parseIntegerRequired (
					requestContext.requestStringRequired (
						"shopify-account-id"));

			Optional <ShopifyAccountRec> shopifyAccountOptional =
				shopifyAccountHelper.find (
					transaction,
					shopifyAccountId);

			if (
				optionalIsNotPresent (
					shopifyAccountOptional)
			) {
				throw new HttpNotFoundException ();
			}

			ShopifyAccountRec shopifyAccount =
				optionalGetRequired (
					shopifyAccountOptional);

			String shopifyAccountName =
				objectManager.objectPathMini (
					transaction,
					shopifyAccount);

			// interpret topic header

			Optional <String> shopifyTopicOptional =
				requestContext.header (
					"x-shopify-topic");

			if (
				optionalIsNotPresent (
					shopifyTopicOptional)
			) {

				throw new HttpUnprocessableEntityException (
					"Header missing: X-Shopify-Topic");

			}

			String shopifyTopic =
				optionalGetRequired (
					shopifyTopicOptional);

			List <String> topicParts =
				stringSplitSlash (
					shopifyTopic);

			if (
				collectionDoesNotHaveTwoElements (
					topicParts)
			) {

				throw new HttpUnprocessableEntityException (
					"Header contents invalid: X-Shopify-Topic");

			}

			String topicSubjectType =
				listFirstElementRequired (
					topicParts);

			// interpret request body

			JsonElement requestJsonElement;

			try {

				requestJsonElement =
					new JsonParser ().parse (
						requestContext.requestBodyString ());

			} catch (JsonSyntaxException jsonSyntaxException) {

				throw new HttpUnprocessableEntityException (
					stringFormat (
						"Unable to interpret request body as JSON: %s",
						jsonSyntaxException.getMessage ()));

			}

			if (! requestJsonElement.isJsonObject ()) {

				throw new HttpUnprocessableEntityException (
					"Request body is not a JSON object");

			}

			JsonObject requestJsonObject =
				requestJsonElement.getAsJsonObject ();

			JsonElement idJsonElement =
				requestJsonObject.get (
					"id");

			if (
				isNull (
					idJsonElement)
			) {

				throw new HttpUnprocessableEntityException (
					"Request body does not contain 'id'");

			}

			if (! idJsonElement.isJsonPrimitive ()) {

				throw new HttpUnprocessableEntityException (
					"Request body 'id' is not an integer");

			}

			JsonPrimitive idJsonPrimitive =
				idJsonElement.getAsJsonPrimitive ();

			if (! idJsonPrimitive.isNumber ()) {

				throw new HttpUnprocessableEntityException (
					"Request body 'id' is not an integer");

			}

			Long subjectId;

			try {

				subjectId =
					idJsonPrimitive.getAsLong ();

			} catch (ClassCastException classCastException) {

				throw new HttpUnprocessableEntityException (
					"Request body 'id' is not an integer");

			}

			// store event

			Optional <ShopifyEventSubjectRec> shopifyEventSubjectOptional =
				shopifyEventSubjectHelper.findBySubjectTypeAndId (
					transaction,
					shopifyAccount,
					ShopifyEventSubjectType.fromTopic (
						topicSubjectType),
					subjectId);

			ShopifyEventSubjectRec shopifyEventSubject;

			if (
				optionalIsPresent (
					shopifyEventSubjectOptional)
			) {

				shopifyEventSubject =
					optionalGetRequired (
						shopifyEventSubjectOptional);

			} else {

				shopifyEventSubject =
					shopifyEventSubjectHelper.createInstance ()

					.setAccount (
						shopifyAccount)

					.setSubjectType (
						ShopifyEventSubjectType.fromTopic (
							topicSubjectType))

					.setSubjectId (
						subjectId)

					.setDeleted (
						false)

					.setFirstEventTime (
						transaction.now ())

				;

			}

			shopifyEventSubject

				.setPending (
					true)

				.setLastEventTime (
					transaction.now ())

				.setNumEvents (
					1 + shopifyEventSubject.getNumEvents ())

			;

			if (
				optionalIsPresent (
					shopifyEventSubjectOptional)
			) {

				shopifyEventSubjectHelper.update (
					transaction,
					shopifyEventSubject);

			} else {

				shopifyEventSubjectHelper.insert (
					transaction,
					shopifyEventSubject);

			}

			// commit and return

			transaction.commit ();

			transaction.noticeFormat (
				"Event %s %s %s",
				shopifyAccountName,
				shopifyTopic,
				integerToDecimalString (
					subjectId));

			return optionalOf (
				textResponderProvider.provide (
					transaction)

				.text (
					"Event queued")

			);

		}

	}

}
