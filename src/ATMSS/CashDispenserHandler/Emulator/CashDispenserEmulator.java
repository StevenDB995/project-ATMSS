package ATMSS.CashDispenserHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.CashDispenserHandler.CashDispenserHandler;
import AppKickstarter.misc.Msg;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

//======================================================================
//CashDispenserEmulator
public class CashDispenserEmulator extends CashDispenserHandler {
	private final int WIDTH = 680;
	private final int HEIGHT = 520;
	private ATMSSStarter atmssStarter;
	private String id;
	private Stage myStage;
	private CashDispenserEmulatorController cashDispenserEmulatorController;

	// ------------------------------------------------------------
	// CashDispenserEmulator
	public CashDispenserEmulator(String id, ATMSSStarter atmssStarter) {
		super(id, atmssStarter);
		this.atmssStarter = atmssStarter;
		this.id = id;
	} // CashDispenserEmulator

	// ------------------------------------------------------------
	// start
	public void start() throws Exception {
		Parent root;
		myStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		String fxmlName = "CashDispenserEmulator.fxml";
		loader.setLocation(CashDispenserEmulator.class.getResource(fxmlName));
		root = loader.load();
		cashDispenserEmulatorController = (CashDispenserEmulatorController) loader.getController();
		cashDispenserEmulatorController.initialize(id, atmssStarter, log, this);
		myStage.initStyle(StageStyle.DECORATED);
		myStage.setScene(new Scene(root, 350, 470));
		myStage.setTitle("Cash Dispenser");
		myStage.setResizable(false);
		myStage.setOnCloseRequest((WindowEvent event) -> {
			atmssStarter.stopApp();
			Platform.exit();
		});
		myStage.show();
	} // CashDispenserEmulator

	// ------------------------------------------------------------
	// handleUpdateDisplayOfCashDisperserSlot
	protected void handleUpdateDisplayOfCashDisperserSlot(Msg msg) {
		log.info(id + ": " + msg.getDetails() + " and update display of cash dispenser.");

		switch (msg.getDetails()) {
		case "OpenCashDispenserSlot":
			reloadStage("////////.fxml");
			break;

		case "CloseCashDispenserSlot":
			reloadStage("////////.fxml");
			break;

		default:
			log.severe(id + ": update cash dispenser display with unknown display type -- " + msg.getDetails());
			break;
		}
	} // handleUpdateDisplayOfCashDisperserSlot

	// ------------------------------------------------------------
	// reloadStage
	private void reloadStage(String fxmlFName) {
		CashDispenserEmulator cashDispenserEmulator = this;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					log.info(id + ": loading fxml: " + fxmlFName);

					Parent root;
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(CashDispenserEmulator.class.getResource(fxmlFName));
					root = loader.load();
					cashDispenserEmulatorController = (CashDispenserEmulatorController) loader.getController();
					cashDispenserEmulatorController.initialize(id, atmssStarter, log, cashDispenserEmulator);
					myStage.setScene(new Scene(root, WIDTH, HEIGHT));
				} catch (Exception e) {
					log.severe(id + ": failed to load " + fxmlFName);
					e.printStackTrace();
				}
			}
		});
	} // reloadStage

}// CashDispenserEmulator
