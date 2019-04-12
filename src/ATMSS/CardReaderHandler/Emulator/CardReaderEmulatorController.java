package ATMSS.CardReaderHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * The CardReaderEmulator class controls the GUI of CardReader
 *
 * 
 * @author Group4
 * @version 1.1
 * 
 *
 */

// ======================================================================
// CardReaderEmulatorController
public class CardReaderEmulatorController {
	private String id;
	private AppKickstarter appKickstarter;
	private Logger log;
	private CardReaderEmulator cardReaderEmulator;
	private MBox cardReaderMBox;
	public TextField cardNumField;
	public TextField cardStatusField;
	public TextArea cardReaderTextArea;

	/**
	 * This method is used to initialize the variable
	 *
	 * 
	 * @param id
	 *            This is the parameter of thread id
	 * @param appKickstarter
	 *            This is the parameter of AppKickstarter to start application
	 * @param log
	 *            This is the parameter of logger to record operation
	 * @param cardReaderEmulator
	 *            This is the parameter of CardReaderEmulator to use CardReader GUI
	 */

	// ------------------------------------------------------------
	// initialize
	public void initialize(String id, AppKickstarter appKickstarter, Logger log,
			CardReaderEmulator cardReaderEmulator) {
		this.id = id;
		this.appKickstarter = appKickstarter;
		this.log = log;
		this.cardReaderEmulator = cardReaderEmulator;
		this.cardReaderMBox = appKickstarter.getThread("CardReaderHandler").getMBox();
	} // initialize

	/**
	 * This method is used to handle the button pressed on CardReader Page
	 * 
	 *
	 * @param actionEvent
	 *            This is the parameter of selected action
	 */

	// ------------------------------------------------------------
	// buttonPressed
	public void buttonPressed(ActionEvent actionEvent) {
		Button btn = (Button) actionEvent.getSource();

		switch (btn.getText()) {
		case "Card 1":
			cardNumField.setText(appKickstarter.getProperty("CardReader.Card1"));
			break;

		case "Card 2":
			cardNumField.setText(appKickstarter.getProperty("CardReader.Card2"));
			break;

		case "Card 3":
			cardNumField.setText(appKickstarter.getProperty("CardReader.Card3"));
			break;

		case "Reset":
			cardNumField.setText("");
			break;

		case "Insert Card":
			if (cardStatusField.getText().compareTo("Card Reader Empty") == 0 && cardNumField.getText().length() != 0) {
				cardReaderTextArea.appendText("Sending " + cardNumField.getText() + "\n");
				cardReaderMBox.send(new Msg(id, cardReaderMBox, Msg.Type.CR_CardInserted, cardNumField.getText()));
			}
			break;

		case "Remove Card":
			if (cardStatusField.getText().compareTo("Card Ejected") == 0) {
				cardReaderTextArea.appendText("Removing card\n");
				cardReaderMBox.send(new Msg(id, cardReaderMBox, Msg.Type.CR_CardRemoved, cardNumField.getText()));
			}
			break;

		default:
			log.warning(id + ": unknown button: [" + btn.getText() + "]");
			break;
		}
	} // buttonPressed

	/**
	 * This method is used to update the card statues in corresponding area
	 * 
	 *
	 * @param status
	 *            This is the parameter of card statue
	 */

	// ------------------------------------------------------------
	// updateCardStatus
	public void updateCardStatus(String status) {
		cardStatusField.setText(status);
	} // updateCardStatus

	/**
	 * This method is used to show the card statues in TextArea
	 * 
	 *
	 * @param status
	 *            This is the parameter of card statue
	 */

	// ------------------------------------------------------------
	// appendTextArea
	public void appendTextArea(String status) {
		cardReaderTextArea.appendText(status + "\n");
	} // appendTextArea
} // CardReaderEmulatorController
