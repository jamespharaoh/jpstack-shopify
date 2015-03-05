package wbs.ticket.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.ticket.model.TicketManagerObjectHelper;
import wbs.ticket.model.TicketManagerRec;
import wbs.ticket.model.TicketNoteObjectHelper;
import wbs.ticket.model.TicketNoteRec;
import wbs.ticket.model.TicketObjectHelper;
import wbs.ticket.model.TicketRec;

@PrototypeComponent ("ticketFixtureProvider")
public class TicketFixtureProvider
	implements FixtureProvider {
	
	// dependencies
	
	@Inject
	MenuGroupObjectHelper menuGroupHelper;
	
	@Inject
	MenuObjectHelper menuHelper;
	
	@Inject
	TicketManagerObjectHelper ticketManagerHelper;
	
	@Inject
	TicketObjectHelper ticketHelper;
	
	@Inject
	TicketNoteObjectHelper ticketNoteHelper;
	
	@Inject
	SliceObjectHelper sliceHelper;
	
	// implementation
	
	@Override
	public
	void createFixtures () {
	
		menuHelper.insert (
			new MenuRec ()
	
			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"facility"))
	
			.setCode (
				"ticket_manager")
	
			.setLabel (
				"Ticket Manager")
	
			.setPath (
				"/ticketManagers")
	
		);
	
		TicketManagerRec ticketManager =
			ticketManagerHelper.insert (
				new TicketManagerRec ()
	
			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))
	
			.setCode (
					ticketManagerHelper.generateCode ())
	
		);
		
		TicketRec ticket =
			ticketHelper.insert (
				new TicketRec ()
	
			.setTicketManager (
				ticketManager)
	
			.setCode (
				ticketHelper.generateCode ())
	
		);
		
		ticketNoteHelper.insert (
			new TicketNoteRec ()

		.setTicket (
			ticket)

		.setIndex (
			ticket.getNumNotes ())
	
		);
		
		ticket
			.setNumNotes (
					ticket.getNumNotes () + 1);
	
	}

}
