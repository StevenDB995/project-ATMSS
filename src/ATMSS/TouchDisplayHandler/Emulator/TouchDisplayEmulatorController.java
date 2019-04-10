package ATMSS.TouchDisplayHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.util.logging.Logger;

//======================================================================
// TouchDisplayEmulatorController
public class TouchDisplayEmulatorController {
	private String id;
	private AppKickstarter appKickstarter;
	private Logger log;
	private TouchDisplayEmulator touchDisplayEmulator;
	private MBox touchDisplayMBox;

	@FXML
	Label label1;
	@FXML
	Label label2;
	@FXML
	Label label3;
	@FXML
	Label label4;

	// ------------------------------------------------------------
	// initialize
	public void initialize(String id, AppKickstarter appKickstarter, Logger log,
			TouchDisplayEmulator touchDisplayEmulator, String fxmlName) {
		this.id = id;
		this.appKickstarter = appKickstarter;
		this.log = log;
		this.touchDisplayEmulator = touchDisplayEmulator;
		this.touchDisplayMBox = appKickstarter.getThread("TouchDisplayHandler").getMBox();
		Platform.runLater(() -> {
			switch (fxmlName) {
			case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount.fxml":
			case "TouchDisplayDepositEmulatorP1_ChooseAccount.fxml":
			case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount.fxml":
			case "TouchDisplayEmulator_accountEnquiry_ChooseAccount.fxml":
				label1.setText(this.touchDisplayEmulator.accounts[0]);
				label2.setText(this.touchDisplayEmulator.accounts[1]);
				label3.setText(this.touchDisplayEmulator.accounts[2]);
				label4.setText(this.touchDisplayEmulator.accounts[3]);
				break;
				
			case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount.fxml":
				if (!this.touchDisplayEmulator.accounts[0].equals(this.touchDisplayEmulator.fromAccount))
					label1.setText(this.touchDisplayEmulator.accounts[0]);
				if (!this.touchDisplayEmulator.accounts[1].equals(this.touchDisplayEmulator.fromAccount))
					label2.setText(this.touchDisplayEmulator.accounts[1]);
				if (!this.touchDisplayEmulator.accounts[2].equals(this.touchDisplayEmulator.fromAccount))
					label3.setText(this.touchDisplayEmulator.accounts[2]);
				if (!this.touchDisplayEmulator.accounts[3].equals(this.touchDisplayEmulator.fromAccount))
					label4.setText(this.touchDisplayEmulator.accounts[3]);
				break;
				
			case "TouchDisplayEmulator_accountEnquiry_DisplayAccount.fxml":
				label1.setText(this.touchDisplayEmulator.balance);
				label2.setText(this.touchDisplayEmulator.balance);
				break;
			}
		});
	} // initialize

	// ------------------------------------------------------------
	// td_mouseClick
	public void td_mouseClick(MouseEvent mouseEvent) {
		int x = (int) mouseEvent.getX();
		int y = (int) mouseEvent.getY();

		log.fine(id + ": mouse clicked: -- (" + x + ", " + y + ")");
		touchDisplayMBox.send(new Msg(id, touchDisplayMBox, Msg.Type.TD_MouseClicked, x + " " + y));
	} // td_mouseClick

} // TouchDisplayEmulatorController
