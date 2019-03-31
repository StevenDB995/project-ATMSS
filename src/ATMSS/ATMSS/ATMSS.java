package ATMSS.ATMSS;

import java.io.IOException;

import ATMSS.BAMSHandler.AdvancedBAMSHandler;
import ATMSS.BAMSHandler.BAMSInvalidReplyException;
//import ATMSS.TouchDisplayHandler.Emulator.TouchDisplayEmulator;
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
	// private TouchDisplayEmulator touchDisplay;

	private AdvancedBAMSHandler bams;
	private String currentCardNo;

	private String credential;
	private String[] availableAccounts;
	private String currentAccount;

	private String keypadInput; // Store keypad input by users
	private String TD_StageId; // record the current stage of the touchDisplay
	private String adviceContent; // store the content of the advice
	private boolean adviceCollected; // whether the user has collect the advice (pressed the button in advice
										// printer)
	// TD_StageId is designed to be the same as the FXML filename

	// ------------------------------------------------------------
	// ATMSS
	public ATMSS(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
		// bams = new AdvancedBAMSHandler(url, logger);
		currentCardNo = "";
		credential = "";
		keypadInput = "";
		currentAccount = "";
		credential = "";
		TD_StageId = "TouchDisplayEmulator";
		adviceContent = "";
		adviceCollected = false;
	} // ATMSS

	// ------------------------------------------------------------
	// run
	public void run() {
		Timer.setTimer(id, mbox, 60000);
		log.info(id + ": starting...");

		cardReaderMBox = appKickstarter.getThread("CardReaderHandler").getMBox();
		keypadMBox = appKickstarter.getThread("KeypadHandler").getMBox();
		touchDisplayMBox = appKickstarter.getThread("TouchDisplayHandler").getMBox();
		cashDispenserMBox = appKickstarter.getThread("CashDispenserHandler").getMBox();
		cashDepositCollectorMBox = appKickstarter.getThread("CashDepositCollectorHandler").getMBox();
		advicePrinterMBox = appKickstarter.getThread("AdvicePrinterHandler").getMBox();
		buzzerMBox = appKickstarter.getThread("BuzzerHandler").getMBox();

		// touchDisplay = (TouchDisplayEmulator)
		// appKickstarter.getThread("TouchDisplayHandler");
		// touchDisplayMBox = touchDisplay.getMBox();

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

			case CDC_ButtonPressed:
				cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
						"CloseCashDepositCollectorSlot"));
				break;

			case CD_ButtonPressed:
				cashDispenserMBox
						.send(new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "CloseCashDispenserSlot"));
				break;

			case AP_ButtonPressed:
				adviceCollected = true;

			case TimesUp:
				Timer.setTimer(id, mbox, 60000);
				log.info("Poll: " + msg.getDetails());
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				keypadMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				cashDispenserMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				buzzerMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
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

			case "WithdrawAmount":
				log.info("Withdraw cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WithdrawCancelled"));
				keypadInput = "";
				break;

			case "DepositAmount":
				log.info("Deposit cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "DepositCancelled"));
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
			case "WithdrawAmount":
			case "DepositAmount":
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
				case "TouchDisplayEmulator": // Login state
					String feedback = bams.login(currentCardNo, keypadInput);
					log.info("logging in with card number " + currentCardNo);

					if (feedback.equals("ERROR")) {
						log.info(id + ": Wrong login password");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WrongPassword")); // to be
																												// revised
						TD_StageId = "WrongPassword";
					} else {
						log.info(id + ": Login successful");
						credential = feedback;
						String accounts = bams.getAccounts(currentCardNo, credential);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_GetAccounts, accounts));
						availableAccounts = accounts.split("/");

						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "ChooseAccount")); // to be
																												// revised
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
						TD_StageId = "TransferFailed";
					} else {
						log.info("Successfully transfered " + transferFeedback + " from" + currentAccount + " to "
								+ toAcc);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TransferSucceeded"));
						TD_StageId = "TransferSucceeded";
					}

					keypadInput = "";
					break;

				case "WithdrawAmount":
					int withdrawFeedback = bams.withdraw(currentCardNo, currentAccount, credential, keypadInput);
					if (withdrawFeedback < 0) {
						log.info("Withdraw failed");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WithdrawFailed"));
						TD_StageId = "WithdrawFailed";
						adviceContent = "Withdraw failed/" + currentCardNo + "/" + currentAccount + "/0";
					} else {
						log.info("Successfully withdrawed " + withdrawFeedback);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WithdrawsSuccessed"));
						cashDispenserMBox
								.send(new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "OpenCashDispenserSlot"));
						TD_StageId = "WithdrawsSuccessed";
						adviceContent = "Withdraw succeed/" + currentCardNo + "/" + currentAccount + "/"
								+ withdrawFeedback;
					}

					keypadInput = "";
					break;

				case "DepositAmount":
					double depositFeedback = bams.deposit(currentCardNo, currentAccount, credential, keypadInput);
					if (depositFeedback < 0) {
						log.info("Deposit failed.");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "DepositFailed"));
						TD_StageId = "DepositFailed";
						adviceContent = "Deposit failed/" + currentCardNo + "/" + currentAccount + "/0";
					} else {
						log.info("Successfully deposited " + depositFeedback);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "DepositSuccessed"));
						cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
								"OpenCashDepositCollectorSlot"));
						TD_StageId = "DepositSuccessed";
						adviceContent = "Deposit succeed/" + currentCardNo + "/" + currentAccount + "/"
								+ depositFeedback;
					}

					keypadInput = "";
					break;
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				e.printStackTrace();
			}
		}
	} // processKeyPressed

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
			if (x < 0 && y < 0) { // case for enquiry, to be revised
				log.info("Enquiry your balance in your account.");
				try {
					double enquiryFeedback = bams.enquiry(currentCardNo, currentAccount, credential);
					if (enquiryFeedback < 0) {
						log.info("Enquiry failed.");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "EnquiryFailed"));
						TD_StageId = "EnquiryFailed";
						adviceContent = "Enquiry failed/" + currentCardNo + "/" + currentAccount + "/0";
					} else {
						log.info("Enquiry successfully, you have " + enquiryFeedback + " in your account.");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "EnquirySuccessed"));
						TD_StageId = "EnquirySuccessed";
						adviceContent = "Enquiry succeed/" + currentCardNo + "/" + currentAccount + "/"
								+ enquiryFeedback;
					}
				} catch (BAMSInvalidReplyException | IOException e) {
					e.printStackTrace();
				}
			}

			else if (x < 0 && y < 0) { // case for withdraw, to be revised
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WithdrawAmount"));
				TD_StageId = "WithdrawAmount";
			}

			else if (x < 0 && y < 0) { // case for deposit, to be revised
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "DepositAmount"));
				TD_StageId = "DepositAmount";
			}

			break;

		case "TouchDisplayConfirmation":
			// if (x... y...)
			// touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "stage"));
			// TD_StageId = "stage";
			break;

		case "EnquiryFailed":

			break;

		case "DepositFailed":

			break;

		case "WithdrawFailed":

			break;

		case "EnquirySuccessed":
		case "DepositSuccessed":
		case "WithdrawsSuccessed":
			if (x < 0 && y < 0) { // case for eject card
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				currentCardNo = "";
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator"));
				TD_StageId = "TouchDisplayEmulator";
			}

			else if (x < 0 && y < 0) { // case for print advice and eject card
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, adviceContent));
				adviceContent = "";
				if (adviceCollected) {
					cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
					currentCardNo = "";
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator"));
					TD_StageId = "TouchDisplayEmulator";
					adviceCollected = false;
				}
			}

			else if (x < 0 && y < 0) { // case for more transactions
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayMainMenu"));
				TD_StageId = "TouchDisplayMainMenu";
			}

			else if (x < 0 && y < 0) { // case for print advice and more transactions
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, adviceContent));
				adviceContent = "";
				if (adviceCollected) {
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayMainMenu"));
					TD_StageId = "TouchDisplayMainMenu";
				}
			}
			break;

		}
	} // processMouseClicked

} // ATMSS