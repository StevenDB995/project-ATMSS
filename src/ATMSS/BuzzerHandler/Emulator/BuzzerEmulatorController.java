package ATMSS.BuzzerHandler.Emulator;

import java.util.logging.Logger;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;

//======================================================================
//BuzzerEmulatorController
public class BuzzerEmulatorController {

	private String id;
	private AppKickstarter appKickstarter;
	private Logger log;
	private BuzzerEmulator buzzerEmulator;
	private MBox buzzerMBox;

	// ------------------------------------------------------------
	// initialize
	public void initialize(String id, AppKickstarter appKickstarter, Logger log, BuzzerEmulator buzzerEmulator) {
		this.id = id;
		this.appKickstarter = appKickstarter;
		this.log = log;
		this.buzzerEmulator = buzzerEmulator;
		this.buzzerMBox = appKickstarter.getThread("BuzzerHandler").getMBox();
	} // initialize

}// BuzzerEmulatorController
