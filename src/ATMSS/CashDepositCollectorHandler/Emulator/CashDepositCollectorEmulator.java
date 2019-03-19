package ATMSS.CashDepositCollectorHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.CashDepositCollectorHandler.CashDepositCollectorHandler;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

//======================================================================
//CashDepositCollectorEmulator
public class CashDepositCollectorEmulator extends CashDepositCollectorHandler {
	private ATMSSStarter atmssStarter;
	private String id;
	private Stage myStage;
	private CashDepositCollectorEmulatorController cashDepositCollectorEmulatorController;

	// ------------------------------------------------------------
	// CashDepositCollectorEmulator
	public CashDepositCollectorEmulator(String id, ATMSSStarter atmssStarter) {
		super(id, atmssStarter);
		this.atmssStarter = atmssStarter;
		this.id = id;
	} // CashDepositCollectorEmulator

	// ------------------------------------------------------------
	// start
	public void start() throws Exception {
		Parent root;
		myStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		String fxmlName = "CashDepositCollectorEmulator.fxml";
		loader.setLocation(CashDepositCollectorEmulator.class.getResource(fxmlName));
		root = loader.load();
		cashDepositCollectorEmulatorController = (CashDepositCollectorEmulatorController) loader.getController();
		cashDepositCollectorEmulatorController.initialize(id, atmssStarter, log, this);
		myStage.initStyle(StageStyle.DECORATED);
		myStage.setScene(new Scene(root, 350, 470));
		myStage.setTitle("Cash Deposit Collector");
		myStage.setResizable(false);
		myStage.setOnCloseRequest((WindowEvent event) -> {
			atmssStarter.stopApp();
			Platform.exit();
		});
		myStage.show();
	} // CashDepositCollectorEmulator

}// CashDepositCollectorEmulator
