package wbs.sms.tracker.logic;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

public
class SmsUtilsTrackerTest
	extends TestCase {

	static
	MessageRec createMessage (
			int days,
			MessageStatus status) {

		Calendar calendar =
			new GregorianCalendar ();

		MessageRec message =
			new MessageRec ();

		calendar.add (
			Calendar.DATE,
			- days);

		message.setCreatedTime (
			calendar.getTime ());

		message.setStatus (
			status);

		return message;

	}

	static
	long
		ms = 1,
		second = ms * 1000,
		minute = second * 60,
		hour = minute * 60,
		day = hour * 24;

	/*
	public
	void testSimpleTrackerScanCore () {

		SmsLogic smsUtils =
			new SmsLogicImpl ();

		SmsLogicImpl.SimpleTrackerResult result;

		// notOk

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.undelivered),
					msg (60, MessageStatus.undelivered)),
				3,
				7 * day,
				30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.notOk,
			result);

		// ok: a message is successful

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.delivered),
					msg (60, MessageStatus.undelivered)),
					3,
					7 * day,
					30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.ok,
			result);

		// okSoFar: there aren't enough messages

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.undelivered),
					msg (60, MessageStatus.undelivered)),
					4,
					7 * day,
					30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.okSoFar,
			result);

		// notOk: the two close messages are counted separate now

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.undelivered),
					msg (60, MessageStatus.undelivered)),
					4,
					6 * hour,
					30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.notOk,
			result);

		// notOk: this one includes a "submitted" as well and some "delivered" a
		// while ago

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (6, MessageStatus.undelivered),
					msg (88, MessageStatus.submitted),
					msg (108, MessageStatus.undelivered),
					msg (282, MessageStatus.undelivered),
					msg (310, MessageStatus.undelivered),
					msg (319, MessageStatus.undelivered),
					msg (322, MessageStatus.undelivered),
					msg (338, MessageStatus.delivered),
					msg (366, MessageStatus.delivered)),
					3,
					7 * day,
					90 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.notOk,
			result);
	}
	*/

}