package ATMSS;

import AppKickstarter.timer.Timer;

import ATMSS.ATMSS.ATMSS;

import ATMSS.AdvicePrinterHandler.AdvicePrinterHandler;
import ATMSS.AdvicePrinterHandler.Emulator.AdvicePrinterEmulator;
import ATMSS.BuzzerHandler.BuzzerHandler;
import ATMSS.BuzzerHandler.Emulator.BuzzerEmulator;
import ATMSS.CardReaderHandler.Emulator.CardReaderEmulator;
import ATMSS.CardReaderHandler.CardReaderHandler;

import ATMSS.CashDepositCollectorHandler.CashDepositCollectorHandler;
import ATMSS.CashDepositCollectorHandler.Emulator.CashDepositCollectorEmulator;

import ATMSS.CashDispenserHandler.Emulator.CashDispenserEmulator;
import ATMSS.CashDispenserHandler.CashDispenserHandler;

import ATMSS.KeypadHandler.KeypadHandler;
import ATMSS.KeypadHandler.Emulator.KeypadEmulator;

import ATMSS.TouchDisplayHandler.Emulator.TouchDisplayEmulator;
import ATMSS.TouchDisplayHandler.TouchDisplayHandler;

//import ATMSS.WelcomPageHandler.Emulator.WelcomPageEmulator;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

//======================================================================
// ATMSSEmulatorStarter
public class ATMSSEmulatorStarter extends ATMSSStarter {
	// ------------------------------------------------------------
	// main
	public static void main(String[] args) {
		new ATMSSEmulatorStarter().startApp();
	} // main

	// ------------------------------------------------------------
	// startHandlers
	@Override
	protected void startHandlers() {
		Emulators.atmssEmulatorStarter = this;
		new Emulators().start();
	} // startHandlers

	// ------------------------------------------------------------
	// Emulators
	public static class Emulators extends Application {
		private static ATMSSEmulatorStarter atmssEmulatorStarter;

		// ----------------------------------------
		// start
		public void start() {
			launch();
		} // start

		// ----------------------------------------
		// start
		public void start(Stage primaryStage) {
			Timer timer = null;
			ATMSS atmss = null;
			CardReaderEmulator cardReaderEmulator = null;
			KeypadEmulator keypadEmulator = null;
			TouchDisplayEmulator touchDisplayEmulator = null;
			AdvicePrinterEmulator advicePrinterEmulator = null;
			CashDepositCollectorEmulator cashDepositCollectorEmulator = null;
			CashDispenserEmulator cashDispenserEmulator = null;
			BuzzerEmulator buzzerEmulator = null;
			
			//WelcomPageEmulator welcomPageEmulator=null;

			// create emulators
			try {
				timer = new Timer("timer", atmssEmulatorStarter);
				atmss = new ATMSS("ATMSS", atmssEmulatorStarter);
				cardReaderEmulator = new CardReaderEmulator("CardReaderHandler", atmssEmulatorStarter);
				keypadEmulator = new KeypadEmulator("KeypadHandler", atmssEmulatorStarter);
				touchDisplayEmulator = new TouchDisplayEmulator("TouchDisplayHandler", atmssEmulatorStarter);
				advicePrinterEmulator = new AdvicePrinterEmulator("AdvicePrinterHandler", atmssEmulatorStarter);
				cashDepositCollectorEmulator = new CashDepositCollectorEmulator("CashDepositCollectorHandler",
						atmssEmulatorStarter);
				cashDispenserEmulator = new CashDispenserEmulator("CashDispenserHandler", atmssEmulatorStarter);
				buzzerEmulator = new BuzzerEmulator("BuzzerHandler", atmssEmulatorStarter);

				//welcomPageEmulator=new WelcomPageEmulator("",atmssEmulatorStarter);
				
				// start emulator GUIs
				keypadEmulator.start();
				cardReaderEmulator.start();
				touchDisplayEmulator.start();
				advicePrinterEmulator.start();
				cashDepositCollectorEmulator.start();
				cashDispenserEmulator.start();
				buzzerEmulator.start();
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
			atmssEmulatorStarter.setAdvicePrinterHandler(advicePrinterEmulator);
			atmssEmulatorStarter.setCashDepositeCollectorHandler(cashDepositCollectorEmulator);
			atmssEmulatorStarter.setCashDispenserHandler(cashDispenserEmulator);
			atmssEmulatorStarter.setBuzzerHandler(buzzerEmulator);

			// start threads
			new Thread(timer).start();
			new Thread(atmss).start();
			new Thread(cardReaderEmulator).start();
			new Thread(keypadEmulator).start();
			new Thread(touchDisplayEmulator).start();
			new Thread(advicePrinterEmulator).start();
			new Thread(cashDepositCollectorEmulator).start();
			new Thread(cashDispenserEmulator).start();
			new Thread(buzzerEmulator).start();
			//new Thread(welcomPageEmulator).start();
		} // start
	} // Emulators

	// ------------------------------------------------------------
	// setters
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

	private void setAdvicePrinterHandler(AdvicePrinterHandler advicePrinterHandler) {
		this.advicePrinterHandler = advicePrinterHandler;
	}

	public void setCashDepositeCollectorHandler(CashDepositCollectorHandler cashDepositCollectorHandler) {
		this.cashDepositCollectorHandler = cashDepositCollectorHandler;
	}

	public void setCashDispenserHandler(CashDispenserHandler cashDispenserHandler) {
		this.cashDispenserHandler = cashDispenserHandler;
	};
	
	public void setBuzzerHandler(BuzzerHandler buzzerHandler) {
		this.buzzerHandler = buzzerHandler;
	}
} // ATMSSEmulatorStarter
