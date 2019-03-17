package ATMSS;

import AppKickstarter.timer.Timer;

import ATMSS.ATMSS.ATMSS;
import ATMSS.CardReaderHandler.Emulator.CardReaderEmulator;
import ATMSS.KeypadHandler.KeypadHandler;
import ATMSS.TouchDisplayHandler.Emulator.TouchDisplayEmulator;
import ATMSS.CardReaderHandler.CardReaderHandler;
import ATMSS.KeypadHandler.Emulator.KeypadEmulator;
import ATMSS.TouchDisplayHandler.TouchDisplayHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

//======================================================================
// ATMSSEmulatorStarter
public class ATMSSEmulatorStarter extends ATMSSStarter {
    //------------------------------------------------------------
    // main
    public static void main(String [] args) {
	new ATMSSEmulatorStarter().startApp();
    } // main


    //------------------------------------------------------------
    // startHandlers
    @Override
    protected void startHandlers() {
        Emulators.atmssEmulatorStarter = this;
        new Emulators().start();
    } // startHandlers


    //------------------------------------------------------------
    // Emulators
    public static class Emulators extends Application {
        private static ATMSSEmulatorStarter atmssEmulatorStarter;

	//----------------------------------------
	// start
        public void start() {
            launch();
	} // start

	//----------------------------------------
	// start
        public void start(Stage primaryStage) {
	    Timer timer = null;
	    ATMSS atmss = null;
	    CardReaderEmulator cardReaderEmulator = null;
	    KeypadEmulator keypadEmulator = null;
	    TouchDisplayEmulator touchDisplayEmulator = null;

	    // create emulators
	    try {
	        timer = new Timer("timer", atmssEmulatorStarter);
	        atmss = new ATMSS("ATMSS", atmssEmulatorStarter);
	        cardReaderEmulator = new CardReaderEmulator("CardReaderHandler", atmssEmulatorStarter);
	        keypadEmulator = new KeypadEmulator("KeypadHandler", atmssEmulatorStarter);
	        touchDisplayEmulator = new TouchDisplayEmulator("TouchDisplayHandler", atmssEmulatorStarter);

		// start emulator GUIs
		keypadEmulator.start();
		cardReaderEmulator.start();
		touchDisplayEmulator.start();
	    } catch (Exception e) {
		System.out.println("Emulators: start failed");
		e.printStackTrace();
		Platform.exit();
	    }
	    atmssEmulatorStarter.setTimer(timer);
	    atmssEmulatorStarter.setATMSS(atmss);
	    atmssEmulatorStarter.setCardReaderHandler(cardReaderEmulator);
	    atmssEmulatorStarter.setKeypadHandler(keypadEmulator);
	    atmssEmulatorStarter.setTouchDisplayHandler(touchDisplayEmulator);

	    // start threads
	    new Thread(timer).start();
	    new Thread(atmss).start();
	    new Thread(cardReaderEmulator).start();
	    new Thread(keypadEmulator).start();
	    new Thread(touchDisplayEmulator).start();
	} // start
    } // Emulators


    //------------------------------------------------------------
    //  setters
    private void setTimer(Timer timer) {
        this.timer = timer;
    }
    private void setATMSS(ATMSS atmss) {
        this.atmss = atmss;
    }
    private void setCardReaderHandler(CardReaderHandler cardReaderHandler) {
        this.cardReaderHandler = cardReaderHandler;
    }
    private void setKeypadHandler(KeypadHandler keypadHandler) {
        this.keypadHandler = keypadHandler;
    }
    private void setTouchDisplayHandler(TouchDisplayHandler touchDisplayHandler) {
        this.touchDisplayHandler = touchDisplayHandler;
    }
} // ATMSSEmulatorStarter
