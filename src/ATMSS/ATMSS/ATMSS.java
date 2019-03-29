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
	private String credential;
	private String[] availableAccounts;
	private String currentAccount;
	
	private String keypadInput; // Store card PIN input by users
	private String TD_StageId; // record the current stage of the touchDisplay
	// TD_StageId is designed to be the same as the FXML filename

	// ------------------------------------------------------------
	// ATMSS
	public ATMSS(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
		// bams = new AdvancedBAMSHandler(url, logger);
		currentCardNo = "";
		credential = "";
		keypadInput = "";
		TD_StageId = "TouchDisplayEmulator";
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

		if (details.compareToIgnoreCase("Cancel") == 0) { // "Cancel" is pressed
			switch (TD_StageId) {
			case "TransferAccNo":
			case "TransferAmount":
				log.info("Transfer Cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TransferCancelled"));
				keypadInput = "";
				break;
				
			default:
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				currentCardNo = "";
			}
		}

		else if (Character.isDigit(details.charAt(0))) { // Number key is pressed
			switch (TD_StageId) {
			case "TouchDisplayEmulator": // cases where keypad input should be recorded
			case "TransferAccNo":
			case "TransferAmount":
				keypadInput += details;
				break;
			}
		}

		else if (details.compareToIgnoreCase("Clear") == 0) { // "Clear" is pressed
			keypadInput = "";
		}

		else if (details.compareToIgnoreCase("Enter") == 0) { // "Enter" is pressed
			try {
				switch (TD_StageId) {
				case "TouchDisplayEmulator":
					String feedback = bams.login(currentCardNo, keypadInput);
					log.info("logging in with card number " + currentCardNo);
					
					if (feedback.equals("ERROR")) {
						log.info(id + ": Wrong login password");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WrongPassword")); // to be revised
						TD_StageId = "WrongPassword";
					} else {
						log.info(id + ": Login successful");
						credential = feedback;
						String accounts = bams.getAccounts(currentCardNo, credential);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_GetAccounts, accounts));
						availableAccounts = accounts.split("/");
						
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "ChooseAccount")); // to be revised
						TD_StageId = "ChooseAccount";
					}
					
					keypadInput = "";
					break;
					
				case "TransferAccNo":
					keypadInput += ":";
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TransferAmount"));
					TD_StageId = "TransferAmount";
					break;
					
				case "TransferAmount":
					String[] tokens = keypadInput.split(":");
					String toAcc = tokens[0];
					String amount = tokens[1];
					double transferFeedback = bams.transfer(currentCardNo, credential, currentAccount, toAcc, amount);
					if (transferFeedback < 0) {
						log.info("Failed to transfer from " + currentAccount + " to " + toAcc);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TransferFailed"));
					} else {
						log.info("Successfully transfered " + transferFeedback
								+ " from" + currentAccount + " to " + toAcc);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TransferSucceeded"));
					}
					
					keypadInput = "";
					break; 
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} // processKeyPressed

	// ------------------------------------------------------------
	// processMouseClicked
	private void processMouseClicked(Msg msg) {
		String[] details = msg.getDetails().split(" ");
		int x = Integer.parseInt(details[0]);
		int y = Integer.parseInt(details[1]);

		switch (TD_StageId) {
		case "TouchDisplayEmulator":
			// if (x... y...)
			// touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "stage"));
			// TD_StageId = "stage";
			break;
			
		case "ChooseAccount":
			if (x < 0 && y < 0) { // to be revised
				currentAccount = availableAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayMainMenu"));
				TD_StageId = "TouchDisplayMainMenu";
			}
			break;

		case "TouchDisplayMainMenu":
			// if (x... y...)
			// touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "stage"));
			// TD_StageId = "stage";
			break;

		case "TouchDisplayConfirmation":
			// if (x... y...)
			// touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "stage"));
			// TD_StageId = "stage";
			break;
		}
	} // processMouseClicked
} // CardReaderHandler
