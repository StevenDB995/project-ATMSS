package ATMSS.AdvicePrinterHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.AdvicePrinterHandler.AdvicePrinterHandler;
import ATMSS.TouchDisplayHandler.Emulator.TouchDisplayEmulator;
import ATMSS.TouchDisplayHandler.Emulator.TouchDisplayEmulatorController;
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
	private final int WIDTH = 350;
	private final int HEIGHT = 470;
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
		myStage.setScene(new Scene(root, 350, 470));
		myStage.setTitle("Advice Printer");
		myStage.setResizable(false);
		myStage.setOnCloseRequest((WindowEvent event) -> {
			atmssStarter.stopApp();
			Platform.exit();
		});
		myStage.show();
	} // AdvicePrinterEmulator

	// handleUpdateDisplay
	protected void handleUpdateDisplay(Msg msg) {
		log.info(id + ": update display -- " + msg.getDetails());

		switch (msg.getDetails()) {
		case "AdviceTake":
			reloadStage("TouchDisplayEmulator.fxml");
			break;

		default:
			log.severe(id + ": update display with unknown display type -- " + msg.getDetails());
			break;
		}
	} // handleUpdateDisplay
	
	// reloadStage
		void reloadStage(String fxmlFName) {
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
