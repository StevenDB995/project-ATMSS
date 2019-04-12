package ATMSS.CardReaderHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;

/**
 * The CardReaderHandler class implements an application that handle different
 * cases of CardReader
 * 
 * @author Group4
 * @version 1.1
 * 
 *
 */

// ======================================================================
// CardReaderHandler
public class CardReaderHandler extends AppThread {
	// ------------------------------------------------------------
	// CardReaderHandler
	public CardReaderHandler(String id, AppKickstarter appKickstarter) {
		super(id, appKickstarter);
	} // CardReaderHandler

	/**
	 * This method is used to run different cases CR_CardInserted, CR_EjectCard,
	 * CR_CardRemoved, CR_CardRetained
	 *
	 * 
	 *
	 */

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
				handleCardInsert();
				break;

			case CR_EjectCard:
				handleCardEject();
				break;

			case CR_CardRemoved:
				handleCardRemove();
				break;

			case CR_CardRetained:
				handleCardRetained();
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

	/**
	 * This method is used to show the information when card is inserted
	 */

	// ------------------------------------------------------------
	// handleCardInsert
	protected void handleCardInsert() {
		log.info(id + ": card inserted");
	} // handleCardInsert

	/**
	 * This method is used to show the information when card is ejected
	 */

	// ------------------------------------------------------------
	// handleCardEject
	protected void handleCardEject() {
		log.info(id + ": card ejected");
	} // handleCardEject

	/**
	 * This method is used to show the information when card is removed
	 */

	// ------------------------------------------------------------
	// handleCardRemove
	protected void handleCardRemove() {
		log.info(id + ": card removed");
	} // handleCardRemove

	/**
	 * This method is used to show the information when card is retained
	 */

	// ------------------------------------------------------------
	// handleCardRetained
	protected void handleCardRetained() {
		log.info(id + ": card retained");
	} // handleCardRemove
} // CardReaderHandler
