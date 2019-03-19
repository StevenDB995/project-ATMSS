package ATMSS.AdvicePrinterHandler.Emulator;

import java.util.logging.Logger;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;

//======================================================================
//AdvicePrinterEmulatorController
public class AdvicePrinterEmulatorController {

	private String id;
	private AppKickstarter appKickstarter;
	private Logger log;
	private AdvicePrinterEmulator advicePrinterEmulator;
	private MBox advicePrinterMBox;

	// ------------------------------------------------------------
	// initialize
	public void initialize(String id, AppKickstarter appKickstarter, Logger log,
			AdvicePrinterEmulator advicePrinterEmulator) {
		this.id = id;
		this.appKickstarter = appKickstarter;
		this.log = log;
		this.advicePrinterEmulator = advicePrinterEmulator;
		this.advicePrinterMBox = appKickstarter.getThread("AdvicePrinterHandler").getMBox();
	} // initialize

}// AdvicePrinterEmulatorController
