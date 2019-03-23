package ATMSS.ATMSS;

//import ATMSS.BAMSHandler.BAMSHandler;
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
	//private BAMSHandler bams;
	//private String urlPrefix = "http://cslinux0.comp.hkbu.edu.hk/~comp4107/test/";

	// ------------------------------------------------------------
	// ATMSS
	public ATMSS(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
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
		
		//bams = new BAMSHandler(urlPrefix);
		

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case KP_KeyPressed:
				log.info("KeyPressed: " + msg.getDetails());
				processKeyPressed(msg);
				break;

			case CR_CardInserted:
				log.info("CardInserted: " + msg.getDetails());
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
		// *** The following is an example only!! ***
		if (msg.getDetails().compareToIgnoreCase("Cancel") == 0) {
			cardReaderMBox.send(new Msg(id, mbox, Msg.Type.CR_EjectCard, ""));
		}
	} // processKeyPressed
} // CardReaderHandler
