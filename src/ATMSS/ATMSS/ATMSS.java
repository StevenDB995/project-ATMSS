package ATMSS.ATMSS;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
	String urlPrefix = "http://cslinux0.comp.hkbu.edu.hk/comp4107_18-19_grp04/";

	private AdvancedBAMSHandler bams;

	private String currentCardNo;
	private String credential;
	private String[] availableAccounts;
	private String currentAccount;
	private String toAccount;
	private String keypadInput; // Store keypad input by users
	private String TD_StageId; // record the current stage of the touchDisplay
	private int wrongPasswordTimes; // store the number of wrong password
	private int idleTimerId;

	// TD_StageId is designed to be the same as the FXML filename

	// ------------------------------------------------------------
	// ATMSS
	public ATMSS(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
		bams = new AdvancedBAMSHandler(urlPrefix, initLogger());
		currentCardNo = "";
		credential = "";
		keypadInput = "";
		currentAccount = "";
		toAccount = "";
		credential = "";
		TD_StageId = "TouchDisplayEmulator(Welcome)";
		wrongPasswordTimes = 0;
		// adviceContent = "";
	} // ATMSS

	// ------------------------------------------------------------
	// initLogger
	static Logger initLogger() {
		// init our logger
		ConsoleHandler logConHdr = new ConsoleHandler();
		logConHdr.setFormatter(new LogFormatter());
		Logger log = Logger.getLogger("ATMSS");
		log.setUseParentHandlers(false);
		log.setLevel(Level.ALL);
		log.addHandler(logConHdr);
		logConHdr.setLevel(Level.ALL);
		return log;
	} // initLogger

	static class LogFormatter extends Formatter {
		// ------------------------------------------------------------
		// format
		public String format(LogRecord rec) {
			Calendar cal = Calendar.getInstance();
			String str = "";

			// get date
			cal.setTimeInMillis(rec.getMillis());
			str += String.format("%02d%02d%02d-%02d:%02d:%02d ", cal.get(Calendar.YEAR) - 2000,
					cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
					cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

			// level of the log
			str += "[" + rec.getLevel() + "] -- ";

			// message of the log
			str += rec.getMessage();
			return str + "\n";
		} // format
	} // LogFormatter

	// ------------------------------------------------------------
	// run
	public void run() {
		Timer.setTimer(id, mbox, Timer.POLL_SLEEPTIME, Timer.POLL_RANGE);
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
				log.info("MouseClicked: " + msg.getDetails());
				switch (TD_StageId) {
				case "TouchDisplayDepositEmulatorP1_ChooseAccount":
				case "TouchDisplayEmulator_accountEnquiry_ChooseAccount":
				case "TouchDisplayEmulator_accountEnquiry_DisplayAccount":
				case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount":
				case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount":
				case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount":
				case "TouchDisplayEmulatorServiceChoice":
				case "TouchDisplayEmulatorSuccessful":
				case "TouchDisplayEmulatorFailed":
					Timer.cancelTimer(id, mbox, idleTimerId);
					break;
				}
				processMouseClicked(msg);
				break;

			case KP_KeyPressed:
				log.info("KeyPressed: " + msg.getDetails());
				switch (TD_StageId) {
				case "TouchDisplayEmulatorPasswordValidationP1_RequestPW":
				case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
				case "TouchDisplayEmulatorTransferP3_InputAmount":
				case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
				case "TouchDisplayDepositEmulatorP2_InputAmount":
					Timer.cancelTimer(id, mbox, idleTimerId);
					break;
				}
				processKeyPressed(msg);
				break;

			case CR_CardInserted:
				if (TD_StageId.compareToIgnoreCase("TouchDisplayEmulator(Welcome)") == 0) {
					currentCardNo = msg.getDetails();
					log.info("CardInserted: " + currentCardNo);
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
							"TouchDisplayEmulatorPasswordValidationP1_RequestPW"));
					TD_StageId = "TouchDisplayEmulatorPasswordValidationP1_RequestPW";
					buzzerMBox.send(new Msg(id, mbox, Msg.Type.BZ_Sound, "SoundOne"));
				}
				break;

			case CDC_ButtonPressed:
				cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
						"CloseCashDepositCollectorSlot"));
				Timer.cancelTimer(id, mbox, idleTimerId);

				try {
					double depositFeedback = bams.deposit(currentCardNo, currentAccount, credential, keypadInput);
					if (depositFeedback < 0) {
						log.info("Deposit failed.");
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
						TD_StageId = "TouchDisplayEmulatorFailed";
					} else {
						log.info("Successfully deposited " + depositFeedback);
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorSuccessful"));
						TD_StageId = "TouchDisplayEmulatorSuccessful";
					}
				} catch (BAMSInvalidReplyException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				currentAccount = "";
				keypadInput = "";
				break;

			case CD_ButtonPressed:
				cashDispenserMBox
						.send(new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "CloseCashDispenserSlot"));
				break;

			case AP_ButtonPressed:
				advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.AP_UpdateAdvicePrinter, "close"));
				buzzerMBox.send(new Msg(id, mbox, Msg.Type.BZ_Sound, "SoundOne"));
				break;

			case IdleTimer:
				idleTimerId = Integer.parseInt(msg.getDetails());
				break;

			case TimesUp:
				String timerIdStr = msg.getDetails().substring(1, 6);
				int timerId = Integer.parseInt(timerIdStr);

				if (timerId < Timer.POLL_RANGE) {
					Timer.setTimer(id, mbox, Timer.POLL_SLEEPTIME, Timer.POLL_RANGE);
					log.info("Poll: " + msg.getDetails());
					cardReaderMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
					keypadMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
					cashDispenserMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
					cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
					advicePrinterMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
					buzzerMBox.send(new Msg(id, mbox, Msg.Type.Poll, ""));
				}

				else if (timerId >= Timer.CANCEL_RANGE && timerId < Timer.IDLE_RANGE) {
					log.info("Idle: " + msg.getDetails());
					keypadInput = "";
					touchDisplayMBox
							.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
					cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
					TD_StageId = "TouchDisplayEmulator(Welcome)";
					buzzerMBox.send(new Msg(id, mbox, Msg.Type.BZ_Sound, "SoundLoop"));
				}

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
		boolean validKP = false;

		if (details.compareToIgnoreCase("Cancel") == 0) { // "Cancel" is pressed
			switch (TD_StageId) {
			case "TouchDisplayEmulatorPasswordValidationP1_RequestPW":
			case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
				wrongPasswordTimes = 0;
			case "TouchDisplayEmulatorServiceChoice":
				log.info("Cancelled.");
				validKP = true;
				break;

			case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount":
			case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount":
			case "TouchDisplayEmulatorTransferP3_InputAmount":
				log.info("Transfer Cancelled.");
				validKP = true;
				break;

			case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount":
			case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
				log.info("Withdraw cancelled.");
				validKP = true;
				break;

			case "TouchDisplayDepositEmulatorP1_ChooseAccount":
			case "TouchDisplayDepositEmulatorP2_InputAmount":
				log.info("Deposit cancelled.");
				validKP = true;
				break;

			case "TouchDisplayEmulator_accountEnquiry_ChooseAccount":
				log.info("Enquiry cancelled.");
				validKP = true;
				break;
			}

			if (validKP) {
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorCancelled"));
				keypadInput = "";
				currentCardNo = "";
				currentAccount = "";
				credential = "";
				cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
				TD_StageId = "TouchDisplayEmulator(Welcome)";
			}
		}

		else if (Character.isDigit(details.charAt(0))) {
			switch (TD_StageId) {
			case "TouchDisplayEmulatorTransferP3_InputAmount": // cases where keypad input should be recorded
			case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
			case "TouchDisplayDepositEmulatorP2_InputAmount":
				keypadInput += details;
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateInputAmount, details));
				validKP = true;
				break;

			case "TouchDisplayEmulatorPasswordValidationP1_RequestPW": // keypad input for password
			case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
				if (details.compareTo("00") != 0) {
					keypadInput += details;
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdatePasswordField, "Append"));
				}
				validKP = true;
				break;
			}

			if (validKP)
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);
		}

		else if (details.compareTo(".") == 0) {
			switch (TD_StageId) {
			case "TouchDisplayEmulatorTransferP3_InputAmount": // cases where "." should be recorded
			case "TouchDisplayDepositEmulatorP2_InputAmount":
				if (!keypadInput.contains(".")) {
					keypadInput += details;
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateInputAmount, details));
				}
				validKP = true;
				break;
			}

			if (validKP)
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);
		}

		else if (details.compareToIgnoreCase("Clear") == 0) { // "Clear" is pressed
			switch (TD_StageId) {
			case "TouchDisplayEmulatorTransferP3_InputAmount":
			case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
			case "TouchDisplayDepositEmulatorP2_InputAmount":
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateInputAmount, "Clear"));
				validKP = true;
				break;

			case "TouchDisplayEmulatorPasswordValidationP1_RequestPW":
			case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdatePasswordField, "Clear"));
				validKP = true;
				break;
			}

			keypadInput = "";

			if (validKP)
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);
		}

		else if (details.compareToIgnoreCase("Enter") == 0) { // "Enter" is pressed
			try {
				switch (TD_StageId) {
				case "TouchDisplayEmulatorPasswordValidationP1_RequestPW": // Login state, type in password
				case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
					String feedback = bams.login(currentCardNo, keypadInput);
					log.info("logging in with card number " + currentCardNo);

					if (feedback.compareToIgnoreCase("ERROR") == 0) {
						wrongPasswordTimes++;
						log.info(id + ": Wrong login password (Wrong password times: " + wrongPasswordTimes + ")");

						if (wrongPasswordTimes < 3) {
							touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
									"TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong"));
							TD_StageId = "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong";
						} else {
							log.info(id + ": Wrong login password exceed the limit.");
							touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
									"TouchDisplayEmulatorPasswordValidationP3_CardEaten"));
							cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_CardRetained, ""));
							TD_StageId = "TouchDisplayEmulator(Welcome)";
							wrongPasswordTimes = 0;
						}
					}

					else {
						log.info(id + ": Login successful");
						wrongPasswordTimes = 0;
						credential = feedback;
						String accounts = bams.getAccounts(currentCardNo, credential);
						availableAccounts = accounts.split("/");

						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_ChooseAccount, accounts));
						touchDisplayMBox.send(
								new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorServiceChoice"));
						TD_StageId = "TouchDisplayEmulatorServiceChoice";
					}

					keypadInput = "";
					break;

				case "TouchDisplayEmulatorTransferP3_InputAmount": // type in transfer amount
					if (!keypadInput.isEmpty()) {
						double transferFeedback = bams.transfer(currentCardNo, credential, currentAccount, toAccount,
								keypadInput);
						if (transferFeedback < 0) {
							log.info("Failed to transfer from " + currentAccount + " to " + toAccount);
							touchDisplayMBox
									.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
							TD_StageId = "TouchDisplayEmulatorFailed";
						} else {
							log.info("Successfully transfered " + transferFeedback + " from " + currentAccount + " to "
									+ toAccount);
							touchDisplayMBox.send(
									new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorSuccessful"));
							TD_StageId = "TouchDisplayEmulatorSuccessful";
						}

						currentAccount = "";
						toAccount = "";
						keypadInput = "";
					} else
						idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

					break;

				case "TouchDisplayEmulatorwithdrawlP2_InputAmount": // type in withdraw amount
					if (!keypadInput.isEmpty()) {
						int withdrawFeedback = bams.withdraw(currentCardNo, currentAccount, credential, keypadInput);
						if (withdrawFeedback < 0) {
							log.info("Withdraw failed");
							touchDisplayMBox
									.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
							TD_StageId = "TouchDisplayEmulatorFailed";
						} else {
							log.info("Successfully withdrawed " + withdrawFeedback);
							touchDisplayMBox.send(
									new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorSuccessful"));
							cashDispenserMBox.send(
									new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "OpenCashDispenserSlot"));
							TD_StageId = "TouchDisplayEmulatorSuccessful";
						}

						currentAccount = "";
						keypadInput = "";
					} else
						idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

					break;

				case "TouchDisplayDepositEmulatorP2_InputAmount": // type in deposit amount
					if (!keypadInput.isEmpty())
						cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
								"OpenCashDepositCollectorSlot"));
					else
						idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);
					
					break;
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				e.printStackTrace();
			}
		}

		buzzerMBox.send(new Msg(id, mbox, Msg.Type.BZ_Sound, "SoundOne"));
	} // processKeyPressed

	// processMouseClicked
	private void processMouseClicked(Msg msg) {
		String[] details = msg.getDetails().split(" ");
		int x = Integer.parseInt(details[0]);
		int y = Integer.parseInt(details[1]);
		boolean validClick = false;

		switch (TD_StageId) {
		case "TouchDisplayEmulator_accountEnquiry_ChooseAccount": // choose from account in enquiry
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				currentAccount = availableAccounts[0];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				currentAccount = availableAccounts[1];
				validClick = true;
			} else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				currentAccount = availableAccounts[2];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				currentAccount = availableAccounts[3];
				validClick = true;
			}

			if (validClick) {
				try {
					double enquiryFeedback = bams.enquiry(currentCardNo, currentAccount, credential);
					if (enquiryFeedback < 0) {
						log.info("Enquiry failed.");
						touchDisplayMBox
								.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorFailed"));
						TD_StageId = "TouchDisplayEmulatorFailed";
					} else {
						log.info("Enquiry successful, you have " + enquiryFeedback + " in your account.");
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_Balance, "" + enquiryFeedback));
						touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
								"TouchDisplayEmulator_accountEnquiry_DisplayAccount"));
						TD_StageId = "TouchDisplayEmulator_accountEnquiry_DisplayAccount";
					}
				} catch (BAMSInvalidReplyException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

			break;

		case "TouchDisplayEmulator_accountEnquiry_DisplayAccount": // step two for enquiry, display balance
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

			else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

			currentAccount = "";
			break;

		case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount": // choose from account in withdraw
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				currentAccount = availableAccounts[0];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				currentAccount = availableAccounts[1];
				validClick = true;
			} else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				currentAccount = availableAccounts[2];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				currentAccount = availableAccounts[3];
				validClick = true;
			}

			if (validClick) {
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP2_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP2_InputAmount";
			} else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

			break;

		case "TouchDisplayDepositEmulatorP1_ChooseAccount": // choose from account in deposit
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				currentAccount = availableAccounts[0];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				currentAccount = availableAccounts[1];
				validClick = true;
			} else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				currentAccount = availableAccounts[2];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				currentAccount = availableAccounts[3];
				validClick = true;
			}

			if (validClick) {
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP2_InputAmount"));
				TD_StageId = "TouchDisplayDepositEmulatorP2_InputAmount";
			} else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

			break;

		case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount": // choose from account in transfer
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				currentAccount = availableAccounts[0];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				currentAccount = availableAccounts[1];
				validClick = true;
			} else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				currentAccount = availableAccounts[2];
				validClick = true;
			} else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				currentAccount = availableAccounts[3];
				validClick = true;
			}

			if (validClick) {
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.BAMS_FromAccount, currentAccount));
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount";
			} else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

			break;

		case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount": // choose to account in transfer
			if (x > 0 && x < 300 && y > 270 && y < 340) { // case for account one
				if (!currentAccount.equals(availableAccounts[0])) {
					toAccount = availableAccounts[0];
					validClick = true;
				}
			} else if (x > 340 && x < 640 && y > 270 && y < 340) { // case for account two
				if (!currentAccount.equals(availableAccounts[1])) {
					toAccount = availableAccounts[1];
					validClick = true;
				}
			} else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for account three
				if (!currentAccount.equals(availableAccounts[2])) {
					toAccount = availableAccounts[2];
					validClick = true;
				}
			} else if (x > 340 && x < 640 && y > 410 && y < 480) { // case for account four
				if (!currentAccount.equals(availableAccounts[3])) {
					toAccount = availableAccounts[3];
					validClick = true;
				}
			}

			if (validClick) {
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorTransferP3_InputAmount"));
				TD_StageId = "TouchDisplayEmulatorTransferP3_InputAmount";
			} else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

			break;

		case "TouchDisplayEmulatorServiceChoice": // choose four services
			if (x > 340 && x < 640 && y > 270 && y < 340) { // case for enquiry
				log.info("You have chosen enquiry.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulator_accountEnquiry_ChooseAccount"));
				TD_StageId = "TouchDisplayEmulator_accountEnquiry_ChooseAccount";
			}

			else if (x > 0 && x < 300 && y > 270 && y < 340) { // case for withdraw
				log.info("You have chosen withdraw.");
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulatorwithdrawlP1_ChooseAccount"));
				TD_StageId = "TouchDisplayEmulatorwithdrawlP1_ChooseAccount";
			}

			else if (x > 0 && x < 300 && y > 340 && y < 410) { // case for deposit
				log.info("You have chosen deposit.");
				touchDisplayMBox.send(
						new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayDepositEmulatorP1_ChooseAccount"));
				TD_StageId = "TouchDisplayDepositEmulatorP1_ChooseAccount";
			}

			else if (x > 0 && x < 300 && y > 410 && y < 480) { // case for transfer
				log.info("You have chosen transfer.");
				touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay,
						"TouchDisplayEmulatorTransferP1_ChooseSendingAccount"));
				TD_StageId = "TouchDisplayEmulatorTransferP1_ChooseSendingAccount";
			}

			else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

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

			else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

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

			else
				idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE);

			break;

		}
		buzzerMBox.send(new Msg(id, mbox, Msg.Type.BZ_Sound, "SoundOne"));
	} // processMouseClicked

} // ATMSS
