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
	private MBox cashDispenserMBox;
	private MBox cashDepositCollectorMBox;
	private MBox advicePrinterMBox;
	private MBox buzzerMBox;

	private AdvancedBAMSHandler bams;
	private String currentCardNo;
	private String keypadInput; // Store keypad input by users
	private String accountNo;
	private String credential;

	// ------------------------------------------------------------
	// ATMSS
	public ATMSS(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
		// bams = new AdvancedBAMSHandler(url, logger);
		currentCardNo = "";
		keypadInput = "";
		accountNo = "";
		credential = "";
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

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case KP_KeyPressed:
				log.info("KeyPressed: " + msg.getDetails());
				processKeyPressed(msg);
				break;

			case KP_WithdrawKeyPressed:
				log.info("KeyPressed: " + msg.getDetails());
				withdrawProcessKeyPressed(msg);
				break;

			case KP_DepositKeyPressed:
				log.info("KeyPressed: " + msg.getDetails());
				depositProcessKeyPressed(msg);
				break;
				
			case KP_EnquiryKeyPressed:
				log.info("KeyPressed: " + msg.getDetails());
				
				break;

			case CR_CardInserted:
				currentCardNo = msg.getDetails();
				log.info("CardInserted: " + currentCardNo);
				break;

			case TD_ChooseWithdraw:
				break;

			case TD_ChooseDeposit:
				break;

			case TD_ChooseEnquiry:
				log.info("Enquiry your balance in your account.");
				enquiry(msg);
				break;

			case B_Sound:
				log.info("Time out! Buzzer is making sound.");
				break;

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
			keypadMBox.send(new Msg(id, mbox, Msg.Type.LogoutAck, "Logout"));
			cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
			currentCardNo = "";
		}

		else if (Character.isDigit(details.charAt(0))) { // Number key is pressed
			keypadInput += details;
		}

		else if (details.compareToIgnoreCase("Enter") == 0) { // "Enter" is pressed
			try {
				String feedback = bams.login(currentCardNo, keypadInput);
				if (feedback.equals("ERROR")) {

				} else
					keypadMBox.send(new Msg(id, mbox, Msg.Type.LoginAck, "Login success"));
			} catch (BAMSInvalidReplyException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			keypadInput = "";
		}
	} // processKeyPressed

	// ------------------------------------------------------------
	// withdrawProcessKeyPressed
	private void withdrawProcessKeyPressed(Msg msg) {
		String details = msg.getDetails();

		if (details.compareToIgnoreCase("Cancel") == 0) { // "Cancel" is pressed
			touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Cancel Withdraw"));
			log.info("Withdraw cancelled.");
			keypadInput = "";
		} else if (Character.isDigit(details.charAt(0))) { // Number key is pressed
			keypadInput += details;
		} else if (details.compareToIgnoreCase("Enter") == 0) { // "Enter" is pressed
			try {
				int withdrawAmount = bams.withdraw(currentCardNo, accountNo, credential, keypadInput);
				if (withdrawAmount < 0) {
					log.info("Withdraw failed.");
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Withdraw failed"));
				} else {
					log.info("Withdraw succeed, you have withdrawed " + withdrawAmount);
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Withdraw successed"));
					cashDispenserMBox
							.send(new Msg(id, mbox, Msg.Type.CD_UpdateCashDispenserSlot, "Open cash dispenser slot"));
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				e.printStackTrace();
			}
			keypadInput = "";
		}

	} // withdrawProcessKeyPressed

	// ------------------------------------------------------------
	// depositProcessKeyPressed
	private void depositProcessKeyPressed(Msg msg) {
		String details = msg.getDetails();

		if (details.compareToIgnoreCase("Cancel") == 0) { // "Cancel" is pressed
			touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Cancel Deposit"));
			log.info("Deposit cancelled.");
			cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
					"Close cash deposit collector slot"));
			keypadInput = "";
		} else if (Character.isDigit(details.charAt(0))) { // Number key is pressed
			keypadInput += details;
		} else if (details.compareToIgnoreCase("Enter") == 0) { // "Enter" is pressed
			try {
				double depositAmount = bams.deposit(currentCardNo, accountNo, credential, keypadInput);
				if (depositAmount < 0) {
					log.info("Deposit failed.");
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Deposit failed"));
				} else {
					log.info("Deposit succeed, you have deposit " + depositAmount);
					touchDisplayMBox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "Deposit successed"));
					cashDepositCollectorMBox.send(new Msg(id, mbox, Msg.Type.CDC_UpdateCashDepositCollectorSlot,
							"Close cash deposit collector slot"));
				}
			} catch (BAMSInvalidReplyException | IOException e) {
				e.printStackTrace();
			}
			keypadInput = "";
		}

	} // depositProcessKeyPressed

	// ------------------------------------------------------------
	// enquiry
	private void enquiry(Msg msg) {
		String details = msg.getDetails();
		
	} // enquiry
} // ATMSS
