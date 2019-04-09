package ATMSS.BuzzerHandler.Emulator;

import java.util.logging.Logger;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;

//======================================================================
//BuzzerEmulatorController
public class BuzzerEmulatorController {

	private String id;
	private AppKickstarter appKickstarter;
	private Logger log;
	private BuzzerEmulator buzzerEmulator;
	private MBox buzzerMBox;

	// ------------------------------------------------------------
	// initialize
	public void initialize(String id, AppKickstarter appKickstarter, Logger log, BuzzerEmulator buzzerEmulator) {
		this.id = id;
		this.appKickstarter = appKickstarter;
		this.log = log;
		this.buzzerEmulator = buzzerEmulator;
		this.buzzerMBox = appKickstarter.getThread("BuzzerHandler").getMBox();
	} // initialize

	public void sound() {

		String s = Text.class.getResource("/sound/sound.mp3").toString();
		Media media = new Media(s);
		MediaPlayer mp2 = new MediaPlayer(media);

	}

}// BuzzerEmulatorController
