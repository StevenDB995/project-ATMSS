package ATMSS.KeypadHandler;

import ATMSS.ATMSSStarter;
import AppKickstarter.misc.*;

//======================================================================
// KeypadHandler
public class KeypadHandler extends AppThread {
	// ------------------------------------------------------------
	// KeypadHandler
	public KeypadHandler(String id, ATMSSStarter atmssStarter) {
		super(id, atmssStarter);
	} // KeypadHandler

	// ------------------------------------------------------------
	// run
	public void run() {
		MBox atmss = appKickstarter.getThread("ATMSS").getMBox();
		log.info(id + ": starting...");

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case KP_KeyPressed:
				atmss.send(new Msg(id, mbox, Msg.Type.KP_KeyPressed, msg.getDetails()));
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
} // KeypadHandler
