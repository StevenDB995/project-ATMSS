package ATMSS.CashDepositCollectorHandler.Emulator;

import java.util.logging.Logger;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import javafx.event.ActionEvent;

//======================================================================
//CashDepositCollectorEmulatorController
public class CashDepositCollectorEmulatorController {

	private String id;
	private AppKickstarter appKickstarter;
	private Logger log;
	private CashDepositCollectorEmulator cashDepositCollectorEmulator;
	private MBox cashDepositCollectorMBox;

	// ------------------------------------------------------------
	// initialize
	public void initialize(String id, AppKickstarter appKickstarter, Logger log,
			CashDepositCollectorEmulator cashDepositCollectorEmulator) {
		this.id = id;
		this.appKickstarter = appKickstarter;
		this.log = log;
		this.cashDepositCollectorEmulator = cashDepositCollectorEmulator;
		this.cashDepositCollectorMBox = appKickstarter.getThread("CashDepositCollectorHandler").getMBox();
	} // initialize
	
	// buttonPressed
			public void buttonPressedInput(ActionEvent actionEvent) {
				String btnTxt = "CashInput";
				cashDepositCollectorMBox.send(new Msg(id, cashDepositCollectorMBox, Msg.Type.TD_UpdateDisplay, btnTxt));
			} // buttonPressed

}// CashDepositCollectorEmulatorController
