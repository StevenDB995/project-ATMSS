package ATMSS.ATMSS;

import java.io.IOException;

import ATMSS.BAMSHandler.AdvancedBAMSHandler;
import ATMSS.BAMSHandler.BAMSInvalidReplyException;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;

//======================================================================
// ATMSS
public class ATMSS extends AppThread {
	private MBox cardReaderMBox;
	private MBox keypadMBox;
	private MBox touchDisplayMBox;
	private MBox advicePrinterMBox;
	private MBox cashDepositCollectorMBox;
	private MBox cashDispenserMBox;
	private MBox buzzerMBox;
	String urlPrefix = "http://cslinux0.comp.hkbu.edu.hk/~comp4107/test/";

	private AdvancedBAMSHandler bams;

	private String currentCardNo;
	private String credential;
	private String[] availableFromAccounts;
	private String[] availableToAccounts;
	private String currentAccount;
	private String toAccount;
	private String keypadInput; // Store keypad input by users
	private String TD_StageId; // record the current stage of the touchDisplay

	// TD_StageId is designed to be the same as the FXML filename

	// ------------------------------------------------------------
	// ATMSS
	public ATMSS(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
		bams = new AdvancedBAMSHandler(urlPrefix);
		currentCardNo = "";
		credential = "";
		keypadInput = "";
		currentAccount = "";
		toAccount = "";
		credential = "";
		TD_StageId = "TouchDisplayEmulator(Welcome)";
		// adviceContent = "";
	} // ATMSS

	// ------------------------------------------------------------
	// run
	public void run() {
		Timer.setTimer(id, mbox, 60000);
		log.info(id + ": starting...");

		cardReaderMBox = appKickstarter.getThread("CardReaderHandler").getMBox();
		keypadMBox = appKickstarter.getThread("KeypadHandler").getMBox();
		touchDisplayMBox = appKickstarter.getThread("TouchDisplayHandler").getMBox();
		advicePrinterMBox = appKickstarter.getThread("AdvicePrinterHandler").getMBox();
		buzzerMBox = appKickstarter.getThread("BuzzerHandler").getMBox();
		cashDepositCollectorMBox = appKickstarter.getThread("CashDepositCollectorHandler").getMBox();
		cashDispenserMBox = appKickstarter.getThread("CashDispenserHandler").getMBox();

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
				if (TD_StageId.compareToIgnoreCase("TouchDisplayEmulator(Welcome)") == 0) {
					currentCardNo = msg.getDetails();
					log.info("CardInserted: " + currentCardNo);
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
							"TouchDisplayEmulatorPasswordValidationP1_RequestPW"));
					TD_StageId = "TouchDisplayEmulatorPasswordValidationP1_RequestPW";

				}
				break;

			case CDC_ButtonPressed:
				cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
						"CloseCashDepositCollectorSlot"));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorSuccessful"));
				TD_StageId = "TouchDisplayEmulatorSuccessful";
				break;

			case CD_ButtonPressed:
				cashDispenserMBox
						.send(new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "CloseCashDispenserSlot"));
				break;

			case AP_ButtonPressed:
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "close"));

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
			case "TouchDisplayEmulatorPasswordValidationP1_RequestPW":
			case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
				log.info("Cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				TD_StageId = "TouchDisplayEmulatorCancelled";
				break;

			case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount":
				toAccount = "";
				// no break here!!!
			case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount":
			case "TouchDisplayEmulatorTransferP3_InputAmount":
				log.info("Transfer Cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				TD_StageId = "TouchDisplayEmulatorCancelled";
				break;

			case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount":
			case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
				log.info("Withdraw cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				TD_StageId = "TouchDisplayEmulatorCancelled";
				break;

			case "TouchDisplayDepositEmulatorP1_ChooseAccount":
			case "TouchDisplayDepositEmulatorP2_InputAmount":
				log.info("Deposit cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				TD_StageId = "TouchDisplayEmulatorCancelled";
				break;

			case "TouchDisplayEmulator_accountEnquiry_ChooseAccount":
				log.info("Enquiry cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				TD_StageId = "TouchDisplayEmulatorCancelled";
				break;

			default:
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				TD_StageId = "TouchDisplayEmulatorCancelled";
			}
		}

		else if (Character.isDigit(details.charAt(0))) {
			switch (TD_StageId) {
			case "TouchDisplayEmulatorTransferP3_InputAmount": // cases where keypad input should be recorded
			case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
			case "TouchDisplayDepositEmulatorP2_InputAmount":
				keypadInput += details;
				break;

			case "TouchDisplayEmulatorPasswordValidationP1_RequestPW":
			case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
				if (details.compareTo("00") != 0) {
					keypadInput += details;
				}
				break;
			}

		} else if (details.compareToIgnoreCase(".") == 0) {
			switch (TD_StageId) {
			case "TouchDisplayEmulatorTransferP3_InputAmount": // cases where "." should be recorded
			case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
			case "TouchDisplayDepositEmulatorP2_InputAmount":
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
				case "TouchDisplayEmulatorPasswordValidationP1_RequestPW": // Login state, type in password
					String feedback = bams.login(currentCardNo, keypadInput);
					log.info("logging in with card number " + currentCardNo);

					if (feedback.compareToIgnoreCase("ERROR") == 0) {
						log.info(id + ": Wrong login password");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
								"TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong"));
						TD_StageId = "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong";
					} else {
						log.info(id + ": Login successful");
						credential = feedback;
						// String accounts = bams.getAccounts(currentCardNo, credential); // to be
						// revised
						String accounts = "1/1/1/1"; // to be revised
						availableFromAccounts = accounts.split("/");
						availableToAccounts = accounts.split("/");

						touchDisplayMBox.send(
								new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
						TD_StageId = "TouchDisplayEmulatorServiceChoice";
					}

					keypadInput = "";
					break;

				case "TouchDisplayEmulatorTransferP3_InputAmount": // type in transfer amount
					double transferFeedback = bams.transfer(currentCardNo, credential, currentAccount, toAccount,
							keypadInput);
					if (transferFeedback < 0) {
						log.info("Failed to transfer from " + currentAccount + " to " + toAccount);
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
						TD_StageId = "TouchDisplayEmulatorFailed";
						currentAccount = "";
						toAccount = "";
					} else {
						log.info("Successfully transfered " + transferFeedback + " from " + currentAccount + " to "
								+ toAccount);
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorSuccessful"));
						TD_StageId = "TouchDisplayEmulatorSuccessful";
						currentAccount = "";
						toAccount = "";
					}

					keypadInput = "";
					break;

				case "TouchDisplayEmulatorwithdrawlP2_InputAmount": // type in withdraw amount
					int withdrawFeedback = bams.withdraw(currentCardNo, currentAccount, credential, keypadInput);
					if (withdrawFeedback < 0) {
						log.info("Withdraw failed");
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
						TD_StageId = "TouchDisplayEmulatorFailed";
						currentAccount = "";
					} else {
						log.info("Successfully withdrawed " + withdrawFeedback);
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorSuccessful"));
						cashDispenserMBox
								.send(new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "OpenCashDispenserSlot"));
						TD_StageId = "TouchDisplayEmulatorSuccessful";
						currentAccount = "";
					}

					keypadInput = "";
					break;

				case "TouchDisplayDepositEmulatorP2_InputAmount": // type in deposit amount
					double depositFeedback = bams.deposit(currentCardNo, currentAccount, credential, keypadInput);
					if (depositFeedback < 0) {
						log.info("Deposit failed.");
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
						TD_StageId = "TouchDisplayEmulatorFailed";
						currentAccount = "";
					} else {
						log.info("Successfully deposited " + depositFeedback);
						cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
								"OpenCashDepositCollectorSlot"));
						currentAccount = "";
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

		case "TouchDisplayEmulator_accountEnquiry_ChooseAccount": // choose from account in enquiry
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				currentAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				currentAccount = availableFromAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				currentAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			break;

		case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount": // choose from account in withdraw
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				toAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				toAccount = availableFromAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				toAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				toAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			break;

		case "TouchDisplayDepositEmulatorP1_ChooseAccount": // choose from account in deposit
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				currentAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				currentAccount = availableFromAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				currentAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			break;

		case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount": // choose from account in transfer
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				currentAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}

			else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				currentAccount = availableFromAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}

			else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				currentAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}
			break;

		case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount": // choose to account in transfer
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				toAccount = availableToAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseToAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}

			else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				toAccount = availableToAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseToAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				toAccount = availableToAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseToAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}

			else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				toAccount = availableToAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseToAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}
			break;

		case "TouchDisplayEmulatorServiceChoice": // choose four services
			if (x > 340 && x < 640 && y > 270 && y < 340) { // case for enquiry
				log.info("You have choosed enquiry.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_ChooseAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_ChooseAccount";
			}

			else if (x > 0 && x < 300 && y > 270 && y < 340) { // case for withdraw
				log.info("You have choosen withdraw.");
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP1_ChooseAccount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP1_ChooseAccount";
			}

			else if (x > 0 && x < 300 && y > 340 && y < 410) { // case for deposit
				log.info("You have choosen deposit.");
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP1_ChooseAccount"));
				TD_StageId = "TouchDisplayDepositEmulatorP1_ChooseAccount";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for transfer
				log.info("You have choosen transfer.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP1_ChooseSendingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP1_ChooseSendingAccount";
			}

			break;

		case "TouchDisplayEmulator_accountEnquiry_DisplayAccount": // step two for enquiry, display balance
			try {
				double enquiryFeedback = bams.enquiry(currentCardNo, currentAccount, credential);
				if (enquiryFeedback < 0) {
					log.info("Enquiry failed.");
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
					TD_StageId = "TouchDisplayEmulatorFailed";
					currentAccount = "";
				} else {
					log.info("Enquiry successfully, you have " + enquiryFeedback + " in your account.");
					if (x > 340 && x < 640 && y > 410 && y < 480) { // case for eject card
						cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
						currentCardNo = "";
						credential = "";
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator(Welcome)"));
						TD_StageId = "TouchDisplayEmulator(Welcome)";
					}

					else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for print advice and eject card
						advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
						cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
						currentCardNo = "";
						credential = "";
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator(Welcome)"));
						TD_StageId = "TouchDisplayEmulator(Welcome)";
					}

					else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for more transactions
						touchDisplayMBox.send(
								new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
						TD_StageId = "TouchDisplayEmulatorServiceChoice";
					}

					else if (x > 0 && x < 300 && y > 270 && y < 340) { // case for print advice and more transactions
						advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
						touchDisplayMBox.send(
								new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
						TD_StageId = "TouchDisplayEmulatorServiceChoice";
					}

					currentAccount = "";
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				e.printStackTrace();
			}
			break;

		case "TouchDisplayEmulatorFailed":
			if (x > 340 && x < 640 && y > 410 && y < 480) { // case for eject card
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				currentCardNo = "";
				credential = "";
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator(Welcome)"));
				TD_StageId = "TouchDisplayEmulator(Welcome)";
			}

			else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for print advice and eject card
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				currentCardNo = "";
				credential = "";
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator(Welcome)"));
				TD_StageId = "TouchDisplayEmulator(Welcome)";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for more transactions
				touchDisplayMBox
						.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
				TD_StageId = "TouchDisplayEmulatorServiceChoice";
			}

			else if (x > 0 && x < 300 && y > 270 && y < 340) { // case for print advice and more transactions
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				touchDisplayMBox
						.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
				TD_StageId = "TouchDisplayEmulatorServiceChoice";
			}

			break;

		case "TouchDisplayEmulatorSuccessful":
			if (x > 340 && x < 640 && y > 410 && y < 480) { // case for eject card
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				currentCardNo = "";
				credential = "";
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator(Welcome)"));
				TD_StageId = "TouchDisplayEmulator(Welcome)";
			}

			else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for print advice and eject card
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				currentCardNo = "";
				credential = "";
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator(Welcome)"));
				TD_StageId = "TouchDisplayEmulator(Welcome)";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for more transactions
				touchDisplayMBox
						.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
				TD_StageId = "TouchDisplayEmulatorServiceChoice";
			}

			else if (x > 0 && x < 300 && y > 270 && y < 340) { // case for print advice and more transactions
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				touchDisplayMBox
						.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
				TD_StageId = "TouchDisplayEmulatorServiceChoice";
			}
			break;

		}
	} // processMouseClicked

} // ATMSS
