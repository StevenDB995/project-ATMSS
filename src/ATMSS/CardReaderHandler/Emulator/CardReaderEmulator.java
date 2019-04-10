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

//======================================================================
// CardReaderEmulator
/**
 * The CardReaderEmulator class implements the GUI of CardReader
 *
 * 
 * @author Group4
 * @version 1.1
 * 
 *
 */

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

	// ------------------------------------------------------------
	// start
	/**
	 * This method is used to load the GUI of CardReader
	 * 
	 *
	 * @throws Exception
	 *             When GUI fail to load, throw Exception
	 */

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

	// ------------------------------------------------------------
	// handleCardInsert
	/**
	 * This method is used to show insert information in text area and update card
	 * statues after Card Inserted
	 */

	protected void handleCardInsert() {
		super.handleCardInsert();
		cardReaderEmulatorController.appendTextArea("Card Inserted");
		cardReaderEmulatorController.updateCardStatus("Card Inserted");
	} // handleCardInsert

	// ------------------------------------------------------------
	// handleCardEject
	protected void handleCardEject() {
		/**
		 * This method is used to show eject information in text area and update card
		 * statues after Card Ejected
		 */
		super.handleCardEject();
		cardReaderEmulatorController.appendTextArea("Card Ejected");
		cardReaderEmulatorController.updateCardStatus("Card Ejected");
	} // handleCardEject

	// ------------------------------------------------------------
	// handleCardRemove
	protected void handleCardRemove() {
		/**
		 * This method is used to show card removed information in text area and update
		 * card statues after Card Removed
		 */
		super.handleCardRemove();
		cardReaderEmulatorController.appendTextArea("Card Removed");
		cardReaderEmulatorController.updateCardStatus("Card Reader Empty");
	} // handleCardRemove
} // CardReaderEmulator
