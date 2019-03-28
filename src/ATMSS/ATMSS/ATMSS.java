package ATMSS.ATMSS;

import java.io.IOException;

import ATMSS.BAMSHandler.AdvancedBAMSHandler;
import ATMSS.BAMSHandler.BAMSInvalidReplyException;
import ATMSS.TouchDisplayHandler.Emulator.TouchDisplayEmulator;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;

//======================================================================
// ATMSS
public class ATMSS extends AppThread {
	private MBox cardReaderMBox;
	private MBox keypadMBox;
	private MBox touchDisplayMBox;
	private MBox cashDispenserMBox;
	private MBox cashDepositCollectorMBox;
	private MBox advicePrinterMBox;
	private MBox buzzerMBox;
	private TouchDisplayEmulator touchDisplay;

	private AdvancedBAMSHandler bams;
	private String currentCardNo;
	private String keypadInput; // Store card PIN input by users

	// ------------------------------------------------------------
	// ATMSS
	public ATMSS(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
		// bams = new AdvancedBAMSHandler(url, logger);
		currentCardNo = "";
		keypadInput = "";
	} // ATMSS

	// ------------------------------------------------------------
	// run
	public void run() {
		Timer.setTimer(id, mbox, 60000);
		log.info(id + ": starting...");

		cardReaderMBox = appKickstarter.getThread("CardReaderHandler").getMBox();
		keypadMBox = appKickstarter.getThread("KeypadHandler").getMBox();
		// touchDisplayMBox = appKickstarter.getThread("TouchDisplayHandler").getMBox();
		cashDispenserMBox = appKickstarter.getThread("CashDispenserHandler").getMBox();
		cashDepositCollectorMBox = appKickstarter.getThread("CashDepositCollectorHandler").getMBox();
		advicePrinterMBox = appKickstarter.getThread("AdvicePrinterHandler").getMBox();
		buzzerMBox = appKickstarter.getThread("BuzzerHandler").getMBox();

		touchDisplay = (TouchDisplayEmulator) appKickstarter.getThread("TouchDisplayHandler");
		touchDisplayMBox = touchDisplay.getMBox();

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case TD_MouseClicked:
				log.info("MouseCLicked: " + msg.getDetails());
				processMouseClicked(msg);
				break;

			case KP_KeyPressed:
				log.info("KeyPressed: " + msg.getDetails());
				processKeyPressed(msg);
				break;

			case CR_CardInserted:
				currentCardNo = msg.getDetails();
				log.info("CardInserted: " + currentCardNo);
				break;

			case TimesUp:
				Timer.setTimer(id, mbox, 60000);
				log.info("Poll: " + msg.getDetails());
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				keypadMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				break;

			case PollAck:
				log.info("PollAck: " + msg.getDetails());
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
	// processKeyPressed
	private void processKeyPressed(Msg msg) {
		String details = msg.getDetails();
		String stageId = touchDisplay.getStageId();

		if (details.compareToIgnoreCase("Cancel") == 0) { // "Cancel" is pressed
			cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
			currentCardNo = "";
		}

		else if (Character.isDigit(details.charAt(0))) { // Number key is pressed
			switch (stageId) {
			case "TouchDisplayEmulator": // cases where keypad input should be recorded
				// case ...:
				// case ...:
				keypadInput += details;
				break;
			}
		}

		else if (details.compareToIgnoreCase("Erase") == 0) { // "Erase" is pressed
			keypadInput = "";
		}

		else if (details.compareToIgnoreCase("Enter") == 0) { // "Enter" is pressed
			try {
				switch (stageId) {
				case "TouchDisplayEmulator":
					String feedback = bams.login(currentCardNo, keypadInput);
					if (feedback.equals("ERROR")) {
						log.info(id + ": Wrong login password");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "")); // to be revised
					} else {
						log.info(id + ": Login sccessful");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayMainMenu")); // to be revised
					}
					break;
					
				// case ...:
				// case ...:
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			keypadInput = "";
		}
	} // processKeyPressed

	// ------------------------------------------------------------
	// processMouseClicked
	private void processMouseClicked(Msg msg) {
		String stageId = touchDisplay.getStageId();
		String[] details = msg.getDetails().split(" ");
		int x = Integer.parseInt(details[0]);
		int y = Integer.parseInt(details[1]);
		boolean triggered = false; // whether an action is triggered by the click

		switch (stageId) {
		case "TouchDisplayEmulator":
			// if (x... y...) {
			// triggered = true;
			// stageId = ...;
			break;

		case "TouchDisplayMainMenu":
			// if (x... y...) {
			// triggered = true;
			// stageId = ...;
			break;

		case "TouchDisplayConfirmation":
			// if (x... y...)
			// triggered = true;
			// stageId = ...;
			break;
		}

		if (triggered)
			touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, stageId));
	} // processMouseClicked
} // CardReaderHandler
