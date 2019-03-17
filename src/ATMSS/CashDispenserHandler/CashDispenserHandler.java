package ATMSS.CashDispenserHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

//======================================================================
// CashDispenserHandler
public class CashDispenserHandler extends AppThread {
	// ------------------------------------------------------------
	// CashDispenserHandler
	public CashDispenserHandler(String id, AppKickstarter appKickstarter) throws Exception {
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
			

			default:
				log.warning(id + ": unknown message type: [" + msg + "]");
			}
		}
		
		
		// declaring our departure
		appKickstarter.unregThread(this);
		log.info(id + ": terminating...");

	}// run
}// CashDispenserHandler
