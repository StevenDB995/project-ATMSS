package ATMSS.BuzzerHandler.Emulator;

import java.util.logging.Logger;
import java.io.File;

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

	// ------------------------------------------------------------
	// sound
	public void sound() {

		String uriString = new File("sound/sound.mp3").toURI().toString();
		MediaPlayer player = new MediaPlayer(new Media(uriString));
		player.play();
	} // sound

}// BuzzerEmulatorController
