package ATMSS.AdvicePrinterHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

//======================================================================
// AdvicePrinterHandler
public class AdvicePrinterHandler extends AppThread {
	// ------------------------------------------------------------
	// AdvicePrinterHandler
	public AdvicePrinterHandler(String id, AppKickstarter appKickstarter){
		super(id, appKickstarter);
	} // AdvicePrinterHandler

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
}// AdvicePrinterHandler