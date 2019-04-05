package ATMSS.TouchDisplayHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;


//======================================================================
// TouchDisplayHandler
public class TouchDisplayHandler extends AppThread {
	// ------------------------------------------------------------
	// TouchDisplayHandler
	public TouchDisplayHandler(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
	} // TouchDisplayHandler

	// ------------------------------------------------------------
	// run
	public void run() {
		MBox atmss = appKickstarter.getThread("ATMSS").getMBox();
		log.info(id + ": starting...");

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case TD_MouseClicked:
				atmss.send(new Msg(id, mbox, Msg.Type.TD_MouseClicked, msg.getDetails()));
				break;

			case TD_UpdateDisplay:
				handleUpdateDisplay(msg);
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
	} // run

	// ------------------------------------------------------------
	// handleUpdateDisplay
	protected void handleUpdateDisplay(Msg msg) {
		log.info(id + ": update display -- " + msg.getDetails());
	} // handleUpdateDisplay


} // TouchDisplayHandler
