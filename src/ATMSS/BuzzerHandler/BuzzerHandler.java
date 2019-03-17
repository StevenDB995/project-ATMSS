package ATMSS.BuzzerHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

//======================================================================
// BuzzerHandler
public class BuzzerHandler extends AppThread {
	// ------------------------------------------------------------
	// BuzzerHandler
	public BuzzerHandler(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
	} // BuzzerHandler

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
}// BuzzerHandler
