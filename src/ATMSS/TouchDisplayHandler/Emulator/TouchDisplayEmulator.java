package ATMSS.TouchDisplayHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.TouchDisplayHandler.TouchDisplayHandler;
import AppKickstarter.misc.Msg;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

//======================================================================
// TouchDisplayEmulator
public class TouchDisplayEmulator extends TouchDisplayHandler {
	private final int WIDTH = 680;
	private final int HEIGHT = 520;
	private ATMSSStarter atmssStarter;
	private String id;
	private Stage myStage;
	private TouchDisplayEmulatorController touchDisplayEmulatorController;
	private String stageId; // record the current stage of the touchDisplay
	// stageId is designed to be the same as the FXML filename

	// ------------------------------------------------------------
	// TouchDisplayEmulator
	public TouchDisplayEmulator(String id, ATMSSStarter atmssStarter) throws Exception {
		super(id, atmssStarter);
		this.atmssStarter = atmssStarter;
		this.id = id;
		stageId = "TouchDisplayEmulator";
	} // TouchDisplayEmulator

	// ------------------------------------------------------------
	// start
	public void start() throws Exception {
		Parent root;
		myStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		String fxmlName = "TouchDisplayEmulator.fxml";
		loader.setLocation(TouchDisplayEmulator.class.getResource(fxmlName));
		root = loader.load();
		touchDisplayEmulatorController = (TouchDisplayEmulatorController) loader.getController();
		touchDisplayEmulatorController.initialize(id, atmssStarter, log, this);
		myStage.initStyle(StageStyle.DECORATED);
		myStage.setScene(new Scene(root, WIDTH, HEIGHT));
		myStage.setTitle("Touch Display");
		myStage.setResizable(false);
		myStage.setOnCloseRequest((WindowEvent event) -> {
			atmssStarter.stopApp();
			Platform.exit();
		});
		myStage.show();
	} // TouchDisplayEmulator

	public String getStageId() {
		return stageId;
	}

	// ------------------------------------------------------------
	// handleUpdateDisplay
	protected void handleUpdateDisplay(Msg msg) {
		stageId = msg.getDetails();
		log.info(id + ": update display -- " + stageId);
		reloadStage(stageId + ".fxml");
	} // handleUpdateDisplay

	// ------------------------------------------------------------
	// reloadStage
	private void reloadStage(String fxmlFName) {
		TouchDisplayEmulator touchDisplayEmulator = this;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					log.info(id + ": loading fxml: " + fxmlFName);

					Parent root;
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(TouchDisplayEmulator.class.getResource(fxmlFName));
					root = loader.load();
					touchDisplayEmulatorController = (TouchDisplayEmulatorController) loader.getController();
					touchDisplayEmulatorController.initialize(id, atmssStarter, log, touchDisplayEmulator);
					myStage.setScene(new Scene(root, WIDTH, HEIGHT));
				} catch (Exception e) {
					log.severe(id + ": failed to load " + fxmlFName);
					e.printStackTrace();
				}
			}
		});
	} // reloadStage
} // TouchDisplayEmulator
