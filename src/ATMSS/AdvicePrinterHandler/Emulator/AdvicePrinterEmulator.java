package ATMSS.AdvicePrinterHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.AdvicePrinterHandler.AdvicePrinterHandler;
import AppKickstarter.misc.Msg;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

//======================================================================
//AdvicePrinterEmulator
public class AdvicePrinterEmulator extends AdvicePrinterHandler {
	private ATMSSStarter atmssStarter;
	private String id;
	private Stage myStage;
	private final int WIDTH = 250;
	private final int HEIGHT = 150;
	private AdvicePrinterEmulatorController advicePrinterEmulatorController;

	// ------------------------------------------------------------
	// AdvicePrinterEmulator
	public AdvicePrinterEmulator(String id, ATMSSStarter atmssStarter) {
		super(id, atmssStarter);
		this.atmssStarter = atmssStarter;
		this.id = id;
	} // AdvicePrinterEmulator

	// ------------------------------------------------------------
	// start
	public void start() throws Exception {
		Parent root;
		myStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		String fxmlName = "AdvicePrinterEmulator.fxml";
		loader.setLocation(AdvicePrinterEmulator.class.getResource(fxmlName));
		root = loader.load();
		advicePrinterEmulatorController = (AdvicePrinterEmulatorController) loader.getController();
		advicePrinterEmulatorController.initialize(id, atmssStarter, log, this);
		myStage.initStyle(StageStyle.DECORATED);
		myStage.setScene(new Scene(root, WIDTH, HEIGHT));
		myStage.setTitle("Advice Printer");
		myStage.setResizable(false);
		myStage.setOnCloseRequest((WindowEvent event) -> {
			atmssStarter.stopApp();
			Platform.exit();
		});
		myStage.show();
	} // AdvicePrinterEmulator

	// ------------------------------------------------------------
	// handleUpdateDisplayOfAdvicePrinter
	protected void handleUpdateDisplayOfAdvicePrinter(Msg msg) {
		// String tokens[] = msg.getDetails().split("/");
		// String transactionType = tokens[0];
		// String currentCardNo = tokens[1];
		// String currentAccount = tokens[2];
		// String amount = tokens[3];

		log.info(id + ": " + msg.getDetails() + " and update display of advice printer.");

		switch (msg.getDetails()) {
		case "print":
			reloadStage("AdvicePrinterEmulatorPrint.fxml");
			break;

		case "close":
			reloadStage("AdvicePrinterEmulator.fxml");
			break;

		default:
			log.severe(id + ": print advice with unknown type -- " + msg.getDetails());
			break;
		}
	} // handleUpdateDisplayOfAdvicePrinter

	// ------------------------------------------------------------
	// reloadStage
	private void reloadStage(String fxmlFName) {
		AdvicePrinterEmulator advicePrinterEmulator = this;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					log.info(id + ": loading fxml: " + fxmlFName);

					Parent root;
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(AdvicePrinterEmulator.class.getResource(fxmlFName));
					root = loader.load();
					advicePrinterEmulatorController = (AdvicePrinterEmulatorController) loader.getController();
					advicePrinterEmulatorController.initialize(id, atmssStarter, log, advicePrinterEmulator);
					myStage.setScene(new Scene(root, WIDTH, HEIGHT));
				} catch (Exception e) {
					log.severe(id + ": failed to load " + fxmlFName);
					e.printStackTrace();
				}
			}
		});
	} // reloadStage

}// AdvicePrinterEmulator
