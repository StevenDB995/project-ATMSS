package ATMSS.CashDispenserHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

//======================================================================
// CashDispenserHandler
public class CashDispenserHandler extends AppThread {
	// ------------------------------------------------------------
	// CashDispenserHandler
	public CashDispenserHandler(String id, AppKickstarter appKickstarter) {
		super(id, appKickstarter);
	} // CashDispenserHandler

	// ------------------------------------------------------------
	// run
	public void run() {
		MBox atmss = appKickstarter.getThread("ATMSS").getMBox();
		log.info(id + ": starting...");

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case CD_UpdateCashDispenserSlot:
				handleUpdateDisplayOfOpenSlot(msg);
				break;

			case Poll:
				atmss.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
				break;

			case Terminate:
				quit = true;
				break;

			default:
				log.warning(id + ": unknown message type: [" + msg + "]");
			}
		}

		// declaring our departure
		appKickstarter.unregThread(this);
		log.info(id + ": terminating...");

	}// run

	// ------------------------------------------------------------
	// handleUpdateDisplayOfOpenSlot
	protected void handleUpdateDisplayOfOpenSlot(Msg msg) {
		log.info(id + ": " + msg.getDetails() + " and update display of cash dispenser.");
	} // handleUpdateDisplayOfOpenSlot
}// CashDispenserHandler
