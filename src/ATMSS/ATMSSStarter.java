package ATMSS;

import ATMSS.KeypadHandler.Emulator.KeypadEmulator;
import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.Msg;
import AppKickstarter.timer.Timer;

import ATMSS.ATMSS.ATMSS;
import ATMSS.CardReaderHandler.CardReaderHandler;
import ATMSS.KeypadHandler.KeypadHandler;
import ATMSS.TouchDisplayHandler.TouchDisplayHandler;

import javafx.application.Platform;

//======================================================================
// ATMSSStarter
public class ATMSSStarter extends AppKickstarter {
	protected Timer timer;
	protected ATMSS atmss;
	protected CardReaderHandler cardReaderHandler;
	protected KeypadHandler keypadHandler;
	protected TouchDisplayHandler touchDisplayHandler;

	// ------------------------------------------------------------
	// main
	public static void main(String[] args) {
		new ATMSSStarter().startApp();
	} // main

	// ------------------------------------------------------------
	// ATMStart
	public ATMSSStarter() {
		super("ATMSSStarter", "etc/ATM.cfg");
	} // ATMStart

	// ------------------------------------------------------------
	// startApp
	protected void startApp() {
		// start our application
		log.info("");
		log.info("");
		log.info("============================================================");
		log.info(id + ": Application Starting...");

		startHandlers();
	} // startApp

	// ------------------------------------------------------------
	// startHandlers
	protected void startHandlers() {
		// create handlers
		try {
			timer = new Timer("timer", this);
			atmss = new ATMSS("ATMSS", this);
			cardReaderHandler = new CardReaderHandler("CardReaderHandler", this);
			keypadHandler = new KeypadHandler("KeypadHandler", this);
			touchDisplayHandler = new TouchDisplayHandler("TouchDisplayHandler", this);
		} catch (Exception e) {
			System.out.println("AppKickstarter: startApp failed");
			e.printStackTrace();
			Platform.exit();
		}

		// start threads
		new Thread(timer).start();
		new Thread(atmss).start();
		new Thread(cardReaderHandler).start();
		new Thread(keypadHandler).start();
		new Thread(touchDisplayHandler).start();
	} // startHandlers

	// ------------------------------------------------------------
	// stopApp
	public void stopApp() {
		log.info("");
		log.info("");
		log.info("============================================================");
		log.info(id + ": Application Stopping...");
		atmss.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
		cardReaderHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
		keypadHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
		touchDisplayHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
		timer.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
	} // stopApp
} // ATM.ATMSSStarter
