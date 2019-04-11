package ATMSS.TouchDisplayHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.TouchDisplayHandler.TouchDisplayHandler;
import AppKickstarter.misc.Msg;
import AppKickstarter.timer.Timer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

//======================================================================
// TouchDisplayEmulator
public class TouchDisplayEmulator extends TouchDisplayHandler {
	private int WIDTH;
	private int HEIGHT;
	private ATMSSStarter atmssStarter;
	private String id;
	private Stage myStage;
	private TouchDisplayEmulatorController touchDisplayEmulatorController;

	String[] accounts;
	String fromAccount;
	String balance;

	// ------------------------------------------------------------
	// TouchDisplayEmulator
	public TouchDisplayEmulator(String id, ATMSSStarter atmssStarter) throws Exception {
		super(id, atmssStarter);
		this.atmssStarter = atmssStarter;
		this.id = id;
		WIDTH = Integer.parseInt(appKickstarter.getProperty("TouchDisplayEmulator.WIDTH"));
		HEIGHT = Integer.parseInt(appKickstarter.getProperty("TouchDisplayEmulator.HEIGHT"));
	} // TouchDisplayEmulator

	// ------------------------------------------------------------
	// start
	public void start() throws Exception {
		Parent root;
		myStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		String fxmlName = "TouchDisplayEmulator(Welcome).fxml";
		loader.setLocation(TouchDisplayEmulator.class.getResource(fxmlName));
		root = loader.load();
		touchDisplayEmulatorController = (TouchDisplayEmulatorController) loader.getController();
		touchDisplayEmulatorController.initialize(id, atmssStarter, log, this, fxmlName);
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

	// ------------------------------------------------------------
	// handleUpdateDisplay
	protected void handleUpdateDisplay(Msg msg) {
		String details = msg.getDetails();
		reloadStage(details + ".fxml");
		log.info(id + ": update display -- " + details);

		int idleTimerId;
		switch (details) {
		case "TouchDisplayEmulatorCancelled":
		case "TouchDisplayEmulatorPasswordValidationP3_CardEaten":
			Timer.setTimer(id, mbox, Timer.CANCEL_SLEEPTIME, Timer.CANCEL_RANGE);
			break;

		case "TouchDisplayEmulatorPasswordValidationP1_RequestPW":
		case "TouchDisplayEmulatorPasswordValidationP2_RequestAgainifwrong":
		case "TouchDisplayEmulatorTransferP3_InputAmount":
		case "TouchDisplayEmulatorwithdrawlP2_InputAmount":
		case "TouchDisplayDepositEmulatorP2_InputAmount":

		case "TouchDisplayDepositEmulatorP1_ChooseAccount":
		case "TouchDisplayEmulator_accountEnquiry_ChooseAccount":
		case "TouchDisplayEmulator_accountEnquiry_DisplayAccount":
		case "TouchDisplayEmulatorTransferP1_ChooseSendingAccount":
		case "TouchDisplayEmulatorTransferP2_ChooseAcceptingAccount":
		case "TouchDisplayEmulatorwithdrawlP1_ChooseAccount":
		case "TouchDisplayEmulatorServiceChoice":
		case "TouchDisplayEmulatorSuccessful":
		case "TouchDisplayEmulatorFailed":
			idleTimerId = Timer.setTimer(id, mbox, Timer.IDLE_SLEEPTIME, Timer.IDLE_RANGE); // simulation
			mbox.send(new Msg(id, mbox, Msg.Type.IdleTimer, "" + idleTimerId));
			break;
		}
	} // handleUpdateDisplay

	protected void td_showAccountNo(Msg msg) {
		accounts = msg.getDetails().split("/");
	}

	protected void td_disableAccount(Msg msg) {
		fromAccount = msg.getDetails();
	}

	protected void td_displayBalance(Msg msg) {
		balance = msg.getDetails();
	}

	protected void td_updatePasswordField(Msg msg) {
		Platform.runLater(() -> {
			if (msg.getDetails().compareToIgnoreCase("Clear") == 0)
				touchDisplayEmulatorController.label1.setText("");
			else
				touchDisplayEmulatorController.label1.setText(touchDisplayEmulatorController.label1.getText() + '*');
		});
	}

	protected void td_updateInputAmount(Msg msg) {
		Platform.runLater(() -> {
			if (msg.getDetails().compareToIgnoreCase("Clear") == 0)
				touchDisplayEmulatorController.label1.setText("");
			else
				touchDisplayEmulatorController.label1
						.setText(touchDisplayEmulatorController.label1.getText() + msg.getDetails());
		});
	}

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
					touchDisplayEmulatorController.initialize(id, atmssStarter, log, touchDisplayEmulator, fxmlFName);
					myStage.setScene(new Scene(root, WIDTH, HEIGHT));
				} catch (Exception e) {
					log.severe(id + ": failed to load " + fxmlFName);
					e.printStackTrace();
				}
			}
		});
	} // reloadStage

} // TouchDisplayEmulator
