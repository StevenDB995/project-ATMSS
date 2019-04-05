package ATMSS.AdvicePrinterHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

//======================================================================
// AdvicePrinterHandler
public class AdvicePrinterHandler extends AppThread {
	// ------------------------------------------------------------
	// AdvicePrinterHandler
	public AdvicePrinterHandler(String id, AppKickstarter appKickstarter) {
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
			case Poll:
				atmss.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
				break;

			case Terminate:
				quit = true;
				break;

			case AP_UpdateAdvicePrinter:
				handleUpdateDisplayOfAdvicePrinter(msg);
				
			case AP_ButtonPressed:
				atmss.send(new Msg(id, mbox, Msg.Type.AP_ButtonPressed, msg.getDetails()));

			default:
				log.warning(id + ": unknown message type: [" + msg + "]");
			}
		}

		// declaring our departure
		appKickstarter.unregThread(this);
		log.info(id + ": terminating...");

	}// run

	// ------------------------------------------------------------
	// handleUpdateDisplayOfAdvicePrinter
	protected void handleUpdateDisplayOfAdvicePrinter(Msg msg) {
//		String tokens[] = msg.getDetails().split("/");
//		String transactionType = tokens[0];
//		String currentCardNo = tokens[1];
//		String currentAccount = tokens[2];
//		String amount = tokens[3];

		log.info("Print advice.");
	} // handleUpdateDisplayOfAdvicePrinter
}// AdvicePrinterHandler