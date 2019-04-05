package ATMSS.CashDepositCollectorHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.CashDepositCollectorHandler.CashDepositCollectorHandler;
import AppKickstarter.misc.Msg;
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
	private final int WIDTH = 350;
	private final int HEIGHT = 350;
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
		myStage.setScene(new Scene(root, 350, 350));
		myStage.setTitle("Cash Deposit Collector");
		myStage.setResizable(false);
		myStage.setOnCloseRequest((WindowEvent event) -> {
			atmssStarter.stopApp();
			Platform.exit();
		});
		myStage.show();
	} // CashDepositCollectorEmulator

	// ------------------------------------------------------------
	// handleUpdateDisplayOfCashDepositCollectorSlot
	protected void handleUpdateDisplayOfCashDepositCollectorSlot(Msg msg) {
		log.info(id + ": " + msg.getDetails() + " and update display of cash deposit collector.");

		switch (msg.getDetails()) {
		case "OpenCashDepositCollectorSlot":
			reloadStage("CashDepositCollectorEmulatorOpen.fxml");
			break;

		case "CloseCashDepositCollectorSlot":
			reloadStage("CashDepositCollectorEmulator.fxml");
			break;

		default:
			log.severe(id + ": update cash dispenser display with unknown display type -- " + msg.getDetails());
			break;
		}
	} // handleUpdateDisplayOfCashDepositCollectorSlot

	// ------------------------------------------------------------
	// reloadStage
	private void reloadStage(String fxmlFName) {
		CashDepositCollectorEmulator cashDepositCollectorEmulator = this;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					log.info(id + ": loading fxml: " + fxmlFName);

					Parent root;
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(CashDepositCollectorEmulator.class.getResource(fxmlFName));
					root = loader.load();
					cashDepositCollectorEmulatorController = (CashDepositCollectorEmulatorController) loader
							.getController();
					cashDepositCollectorEmulatorController.initialize(id, atmssStarter, log,
							cashDepositCollectorEmulator);
					myStage.setScene(new Scene(root, WIDTH, HEIGHT));
				} catch (Exception e) {
					log.severe(id + ": failed to load " + fxmlFName);
					e.printStackTrace();
				}
			}
		});
	} // reloadStage

}// CashDepositCollectorEmulator
