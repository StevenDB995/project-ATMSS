package ATMSS.CardReaderHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

//======================================================================
// CardReaderHandler
public class CardReaderHandler extends AppThread {
	// ------------------------------------------------------------
	// CardReaderHandler
	public CardReaderHandler(String id, AppKickstarter appKickstarter) {
		super(id, appKickstarter);
	} // CardReaderHandler

	// ------------------------------------------------------------
	// run
	public void run() {
		MBox atmss = appKickstarter.getThread("ATMSS").getMBox();
		log.info(id + ": starting...");

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case CR_CardInserted:
				atmss.send(new Msg(id, mbox, Msg.Type.CR_CardInserted, msg.getDetails()));
				break;

			case CR_EjectCard:
				handleCardEject();
				break;

			case CR_CardRemoved:
				atmss.send(new Msg(id, mbox, Msg.Type.CR_CardRemoved, msg.getDetails()));
				handleCardRemove();
				break;

			case CR_CardRetained:

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
	// handleCardInsert
	protected void handleCardInsert() {
		log.info(id + ": card inserted");
	} // handleCardInsert

	// ------------------------------------------------------------
	// handleCardEject
	protected void handleCardEject() {
		log.info(id + ": card ejected");
	} // handleCardEject

	// ------------------------------------------------------------
	// handleCardRemove
	protected void handleCardRemove() {
		log.info(id + ": card removed");
	} // handleCardRemove

	// ------------------------------------------------------------
	// handleCardRetained
	protected void handleCardRetained() {
		log.info(id + ": card retained");
	} // handleCardRemove
} // CardReaderHandler
