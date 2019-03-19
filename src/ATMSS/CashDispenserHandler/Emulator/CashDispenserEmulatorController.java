package ATMSS.CashDispenserHandler.Emulator;

import java.util.logging.Logger;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;

//======================================================================
//CashDispenserEmulatorController
public class CashDispenserEmulatorController {

	private String id;
	private AppKickstarter appKickstarter;
	private Logger log;
	private CashDispenserEmulator cashDispenserEmulator;
	private MBox cashDispenserMBox;

	// ------------------------------------------------------------
	// initialize
	public void initialize(String id, AppKickstarter appKickstarter, Logger log,
			CashDispenserEmulator cashDispenserEmulator) {
		this.id = id;
		this.appKickstarter = appKickstarter;
		this.log = log;
		this.cashDispenserEmulator = cashDispenserEmulator;
		this.cashDispenserMBox = appKickstarter.getThread("CashDispenserHandler").getMBox();
	} // initialize

}// CashDispenserEmulatorController
