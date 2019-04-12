package ATMSS.CardReaderHandler.Emulator;

import ATMSS.ATMSSStarter;
import ATMSS.CardReaderHandler.CardReaderHandler;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * The CardReaderEmulator class implements the GUI of CardReader
 *
 * 
 * @author Group4
 * @version 1.1
 * 
 *
 */

// ======================================================================
// CardReaderEmulator
public class CardReaderEmulator extends CardReaderHandler {
	private ATMSSStarter atmssStarter;
	private String id;
	private Stage myStage;
	private CardReaderEmulatorController cardReaderEmulatorController;

	// ------------------------------------------------------------
	// CardReaderEmulator
	public CardReaderEmulator(String id, ATMSSStarter atmssStarter) {
		super(id, atmssStarter);
		this.atmssStarter = atmssStarter;
		this.id = id;
	} // CardReaderEmulator

	/**
	 * This method is used to load the GUI of CardReader
	 * 
	 *
	 * @throws Exception
	 *             When GUI fail to load, throw Exception
	 */

	// ------------------------------------------------------------
	// start
	public void start() throws Exception {
		Parent root;
		myStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		String fxmlName = "CardReaderEmulator.fxml";
		loader.setLocation(CardReaderEmulator.class.getResource(fxmlName));
		root = loader.load();
		cardReaderEmulatorController = (CardReaderEmulatorController) loader.getController();
		cardReaderEmulatorController.initialize(id, atmssStarter, log, this);
		myStage.initStyle(StageStyle.DECORATED);
		myStage.setScene(new Scene(root, 350, 470));
		myStage.setTitle("Card Reader");
		myStage.setResizable(false);
		myStage.setOnCloseRequest((WindowEvent event) -> {
			atmssStarter.stopApp();
			Platform.exit();
		});
		myStage.show();
	} // CardReaderEmulator

	/**
	 * This method is used to show insert information in text area and update card
	 * statues after Card Inserted
	 */

	// ------------------------------------------------------------
	// handleCardInsert
	protected void handleCardInsert() {
		super.handleCardInsert();
		cardReaderEmulatorController.appendTextArea("Card Inserted");
		cardReaderEmulatorController.updateCardStatus("Card Inserted");
	} // handleCardInsert

	/**
	 * This method is used to show eject information in text area and update card
	 * statues after Card Ejected
	 */

	// ------------------------------------------------------------
	// handleCardEject
	protected void handleCardEject() {
		super.handleCardEject();
		cardReaderEmulatorController.appendTextArea("Card Ejected");
		cardReaderEmulatorController.updateCardStatus("Card Ejected");
	} // handleCardEject

	/**
	 * This method is used to show card removed information in text area and update
	 * card statues after Card Removed
	 */

	// ------------------------------------------------------------
	// handleCardRemove
	protected void handleCardRemove() {
		super.handleCardRemove();
		cardReaderEmulatorController.appendTextArea("Card Removed");
		cardReaderEmulatorController.updateCardStatus("Card Reader Empty");
	} // handleCardRemove

	/**
	 * This method is used to show card retained information in text area and update
	 * card statues after Card retained
	 */
	// ------------------------------------------------------------
	// handleCardRetained
	protected void handleCardRetained() {
		super.handleCardRetained();
		cardReaderEmulatorController.appendTextArea("Card Retained");
		cardReaderEmulatorController.updateCardStatus("Card Reader Empty");
	} // handleCardRetained
} // CardReaderEmulator
