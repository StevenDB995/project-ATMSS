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

			case CR_CardInserted:
				currentCardNo = msg.getDetails();
				log.info("CardInserted: " + currentCardNo);
				break;
				
			case TD_ChooseWithdraw:
				//need key in amount
				//log.info("Withdraw: " + msg.getDetails());
				break;
				
			case TD_ChooseDeposit:
				break;
				
			case TD_ChooseEnquiry:
				break;
				
			case CDC_OpenSlot:
				log.info("Opend the slot, please insert money.");
				break;
				
			case CDC_CloseSlot:
				log.info("Closed the slot, please wait.");
				break;
			
			case CD_OpenSlot:
				log.info("Opend the slot, please collect your money.");
				break;
				
			case CD_CloseSlot:
				log.info("Closed the slot, please wait.");
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
} // CardReaderHandler
