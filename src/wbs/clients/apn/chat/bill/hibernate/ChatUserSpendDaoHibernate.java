package wbs.clients.apn.chat.bill.hibernate;

import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import wbs.clients.apn.chat.bill.model.ChatUserSpendDao;
import wbs.clients.apn.chat.bill.model.ChatUserSpendRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserSpendDaoHibernate
	extends HibernateDao
	implements ChatUserSpendDao {

	@Override
	public
	ChatUserSpendRec findByDate (
			ChatUserRec chatUser,
			LocalDate date) {

		return findOne (
			ChatUserSpendRec.class,

			createCriteria (
				ChatUserSpendRec.class)

			.add (
				Restrictions.eq (
					"chatUser",
					chatUser))

			.add (
				Restrictions.eq (
					"date",
					date))

			.list ());

	}

}