package ATMSS.CashDepositCollectorHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

//======================================================================
// CashDepositCollectorHandler
public class CashDepositCollectorHandler extends AppThread {
	// ------------------------------------------------------------
	// CashDepositCollectorHandler
	public CashDepositCollectorHandler(String id, AppKickstarter appKickstarter) {
		super(id, appKickstarter);
	} // CashDepositCollectorHandler

	// ------------------------------------------------------------
	// run
	public void run() {
		MBox atmss = appKickstarter.getThread("ATMSS").getMBox();
		log.info(id + ": starting...");

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case CDC_UpdateCashDepositCollectorSlot:
				handleUpdateDisplayOfCashDepositCollectorSlot(msg);
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
	// handleUpdateDisplayOfCashDepositCollectorSlot
	protected void handleUpdateDisplayOfCashDepositCollectorSlot(Msg msg) {
		log.info(id + ": " + msg.getDetails() + " and update display of cash deposit collector.");
	} // handleUpdateDisplayOfCashDepositCollectorSlot
}// CashDepositCollectorHandler
