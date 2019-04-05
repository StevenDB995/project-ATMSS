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
	private String[] availableFromAccounts;
	private String[] availableToAccounts;
	private String currentAccount;
	private String toAccount;
	private String keypadInput; // Store keypad input by users
	private String TD_StageId; // record the current stage of the touchDisplay
	// private String adviceContent; // store the content of the advice
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
		toAccount = "";
		credential = "";
		TD_StageId = "Welcome";
		// adviceContent = "";
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
				if (TD_StageId.compareToIgnoreCase("TouchDisplayWelocme") == 0) {
					currentCardNo = msg.getDetails();
					log.info("CardInserted: " + currentCardNo);
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
							"TouchDisplayEmulatorPasswordValidationP1_RequestPW"));
				}
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
			case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount":
				toAccount = "";
				// no break here!!!
			case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount":
			case "TouchDisplayEmulatorTransferP3_InputAmount":
				log.info("Transfer Cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Cancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				break;

			case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount":
			case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
				log.info("Withdraw cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Cancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				break;

			case "TouchDisplayDepositEmulatorP1_ChooseAccount":
			case "TouchDisplayDepositEmulatorP2_InputAmount":
				log.info("Deposit cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Cancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				break;

			case "TouchDisplayEmulator_accountEnquiry_ChooseAccount":
				log.info("Enquiry cancelled.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Cancelled"));
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				break;

			default:
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				currentCardNo = "";
			}
		}

		else if (Character.isDigit(details.charAt(0))) { // Number key is pressed
			switch (TD_StageId) {
			// case "TouchDisplayEmulator": // cases where keypad input should be recorded
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
				case "TouchDisplayEmulatorPasswordValidationP1_RequestPW": // Login state, type in password
					String feedback = bams.login(currentCardNo, keypadInput);
					log.info("logging in with card number " + currentCardNo);

					if (feedback.equals("ERROR")) {
						log.info(id + ": Wrong login password");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WrongPassword")); // to be
																												// revised
						TD_StageId = "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong";
					} else {
						log.info(id + ": Login successful");
						credential = feedback;
						String accounts = bams.getAccounts(currentCardNo, credential);
						// touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_GetAccounts,
						// accounts));
						availableFromAccounts = accounts.split("/");
						availableToAccounts = accounts.split("/");

						touchDisplayMBox.send(
								new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice")); // to
																													// be
																													// revised
						TD_StageId = "TouchDisplayEmulatorServiceChoice";
					}

					keypadInput = "";
					break;

				// case "TransferAccNo":
				// keypadInput += ":";
				// touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
				// "TransferAmount"));
				// TD_StageId = "TransferAmount";
				// break;

				case "TouchDisplayEmulatorTransferP3_InputAmount": // type in transfer amount
					// String[] tokens = keypadInput.split(":");
					// String toAcc = tokens[0];
					// String amount = tokens[1];
					double transferFeedback = bams.transfer(currentCardNo, credential, currentAccount, toAccount,
							keypadInput);
					if (transferFeedback < 0) {
						log.info("Failed to transfer from " + currentAccount + " to " + toAccount);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TransferFailed"));
						TD_StageId = "TransferFailed";
						currentAccount = "";
						toAccount = "";
					} else {
						log.info("Successfully transfered " + transferFeedback + " from" + currentAccount + " to "
								+ toAccount);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TransferSucceeded"));
						TD_StageId = "TransferSucceeded";
						currentAccount = "";
						toAccount = "";
					}

					keypadInput = "";
					break;

				case "TouchDisplayEmulatorwithdrawlP2_InputAmount": // type in withdraw amount
					int withdrawFeedback = bams.withdraw(currentCardNo, currentAccount, credential, keypadInput);
					if (withdrawFeedback < 0) {
						log.info("Withdraw failed");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WithdrawFailed"));
						TD_StageId = "WithdrawFailed";
						currentAccount = "";
						// adviceContent = "Withdraw failed/" + currentCardNo + "/" + currentAccount +
						// "/0";
					} else {
						log.info("Successfully withdrawed " + withdrawFeedback);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "WithdrawsSuccessed"));
						cashDispenserMBox
								.send(new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "OpenCashDispenserSlot"));
						TD_StageId = "WithdrawsSuccessed";
						currentAccount = "";
						// adviceContent = "Withdraw succeed/" + currentCardNo + "/" + currentAccount +
						// "/"
						// + withdrawFeedback;
					}

					keypadInput = "";
					break;

				case "TouchDisplayDepositEmulatorP2_InputAmount": // type in deposit amount
					double depositFeedback = bams.deposit(currentCardNo, currentAccount, credential, keypadInput);
					if (depositFeedback < 0) {
						log.info("Deposit failed.");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "DepositFailed"));
						TD_StageId = "DepositFailed";
						currentAccount = "";
						// adviceContent = "Deposit failed/" + currentCardNo + "/" + currentAccount +
						// "/0";
					} else {
						log.info("Successfully deposited " + depositFeedback);
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "DepositSuccessed"));
						cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
								"OpenCashDepositCollectorSlot"));
						TD_StageId = "DepositSuccessed";
						currentAccount = "";
						// adviceContent = "Deposit succeed/" + currentCardNo + "/" + currentAccount +
						// "/"
						// + depositFeedback;
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
		// case "TouchDisplayEmulator":
		// // if (x... y...)
		// // touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
		// "stage"));
		// // TD_StageId = "stage";
		// break;

		case "TouchDisplayEmulator_accountEnquiry_ChooseAccount": // choose from account in enquiry
			if (x < 0 && y < 0) { // case for account one
				currentAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			else if (x < 0 && y < 0) { // case for account two
				currentAccount = availableFromAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			else if (x < 0 && y < 0) { // case for account three
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			else if (x < 0 && y < 0) { // case for account four
				currentAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
			}

			break;

		case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount": // choose from account in withdraw
			if (x < 0 && y < 0) { // case for account one
				currentAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account two
				currentAccount = availableFromAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account three
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account four
				currentAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			}

			break;

		case "TouchDisplayDepositEmulatorP1_ChooseAccount": // choose from account in deposit
			if (x < 0 && y < 0) { // case for account one
				currentAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account two
				currentAccount = availableFromAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account three
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account four
				currentAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			}

			break;

		case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount": // choose from account in transfer
			if (x < 0 && y < 0) { // case for account one
				currentAccount = availableFromAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}

			else if (x < 0 && y < 0) { // case for account two
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}

			else if (x < 0 && y < 0) { // case for account three
				currentAccount = availableFromAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}

			else if (x < 0 && y < 0) { // case for account four
				currentAccount = availableFromAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			}
			break;

		case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount": // choose to account in transfer
			if (x < 0 && y < 0) { // case for account one
				toAccount = availableToAccounts[0];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account two
				toAccount = availableToAccounts[1];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account three
				toAccount = availableToAccounts[2];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}

			else if (x < 0 && y < 0) { // case for account four
				toAccount = availableToAccounts[3];
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseFromAccount, toAccount));
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			}
			break;

		case "TouchDisplayEmulatorServiceChoice": // choose four services
			if (x < 0 && y < 0) { // case for enquiry, to be revised
				log.info("You have choosed enquiry.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_ChooseAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_ChooseAccount";
			}

			else if (x < 0 && y < 0) { // case for withdraw, to be revised
				log.info("You have choosen withdraw.");
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP1_ChooseAccoun"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP1_ChooseAccoun";
			}

			else if (x < 0 && y < 0) { // case for deposit, to be revised
				log.info("You have choosen deposit.");
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP1_ChooseAccount"));
				TD_StageId = "TouchDisplayDepositEmulatorP1_ChooseAccount";
			}

			else if (x < 0 && y < 0) { // case for transfer, to be revised
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
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "EnquiryFailed"));
					TD_StageId = "EnquiryFailed";
					currentAccount = "";
					// adviceContent = "Enquiry failed/" + currentCardNo + "/" + currentAccount +
					// "/0";
				} else {
					log.info("Enquiry successfully, you have " + enquiryFeedback + " in your account.");
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "EnquirySuccessed"));
					TD_StageId = "EnquirySuccessed";
					currentAccount = "";
					// adviceContent = "Enquiry succeed/" + currentCardNo + "/" + currentAccount +
					// "/"
					// + enquiryFeedback;
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				e.printStackTrace();
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
				credential = "";
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayWelocme"));
				TD_StageId = "TouchDisplayWelocme";
			}

			else if (x < 0 && y < 0) { // case for print advice and eject card
				// advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter,
				// adviceContent));
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				// adviceContent = "";
				if (adviceCollected) {
					cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
					currentCardNo = "";
					credential = "";
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayWelocme"));
					TD_StageId = "TouchDisplayWelocme";
					advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "close"));
					adviceCollected = false;
				}
			}

			else if (x < 0 && y < 0) { // case for more transactions
				touchDisplayMBox
						.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
				TD_StageId = "TouchDisplayEmulatorServiceChoice";
			}

			else if (x < 0 && y < 0) { // case for print advice and more transactions
				// advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter,
				// adviceContent));
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "print"));
				// adviceContent = "";
				if (adviceCollected) {
					touchDisplayMBox
							.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
					TD_StageId = "TouchDisplayEmulatorServiceChoice";
				}
			}
			break;

		}
	} // processMouseClicked

} // ATMSS
