package ATMSS.BuzzerHandler;

import java.io.File;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

//======================================================================
// BuzzerHandler
public class BuzzerHandler extends AppThread {
	// ------------------------------------------------------------
	// BuzzerHandler
	public BuzzerHandler(String id, AppKickstarter appKickstarter) {
		super(id, appKickstarter);
	} // BuzzerHandler

	// ------------------------------------------------------------
	// run
	public void run() {
		MBox atmss = appKickstarter.getThread("ATMSS").getMBox();
		log.info(id + ": starting...");

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case Poll:
				atmss.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
				break;

			case Terminate:
				quit = true;
				break;

			case BZ_Sound:
				sound(msg);
				break;

			default:
				log.warning(id + ": unknown message type: [" + msg + "]");
			}
		}

		// declaring our departure
		appKickstarter.unregThread(this);
		log.info(id + ": terminating...");

	}// run

	protected void sound(Msg msg) {
		log.info(id + ": sound command -- " + msg.getDetails());
		switch (msg.getDetails()) {
		case "SoundOne":
			String uriString = new File("sound/sound.mp3").toURI().toString();
			MediaPlayer player = new MediaPlayer(new Media(uriString));
			player.play();
			break;
		}
	}

}// BuzzerHandler
