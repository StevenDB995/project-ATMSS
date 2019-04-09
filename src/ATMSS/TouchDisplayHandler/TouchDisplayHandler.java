package ATMSS.TouchDisplayHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;

//======================================================================
// TouchDisplayHandler
public class TouchDisplayHandler extends AppThread {
	// ------------------------------------------------------------
	// TouchDisplayHandler
	public TouchDisplayHandler(String id, AppKickstarter appKickstarter) throws Exception {
		super(id, appKickstarter);
	} // TouchDisplayHandler

	// ------------------------------------------------------------
	// run
	public void run() {
		MBox atmss = appKickstarter.getThread("ATMSS").getMBox();
		log.info(id + ": starting...");

		for (boolean quit = false; !quit;) {
			Msg msg = mbox.receive();

			log.fine(id + ": message received: [" + msg + "].");

			switch (msg.getType()) {
			case TD_MouseClicked:
				atmss.send(new Msg(id, mbox, Msg.Type.TD_MouseClicked, msg.getDetails()));
				break;

			case TD_UpdateDisplay:
				handleUpdateDisplay(msg);
				break;
				
			case TD_UpdatePasswordField:
				td_updatePasswordField(msg);
				break;
				
			case TD_UpdateInputAmount:
				td_updateInputAmount(msg);
				break;

			case Poll:
				atmss.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
				break;

			case BAMS_ChooseAccount:
				// let touch display show the from account
				td_showAccountNo(msg);
				break;
				
			case BAMS_FromAccount:
				td_disableAccount(msg);
				break;
				
			case BAMS_Balance:
				td_displayBalance(msg);
				break;
				
			case IdleTimer:
				atmss.send(new Msg(id, mbox, Msg.Type.IdleTimer, msg.getDetails()));
				break;
				
			case TimesUp:
				String timerIdStr = msg.getDetails().substring(1, 6);
				int timerId = Integer.parseInt(timerIdStr);
				
				if (timerId >= Timer.POLL_RANGE && timerId < Timer.CANCEL_RANGE) {
					log.info("Cancel: " + msg.getDetails());
					mbox.send(new Msg(id, mbox, Msg.Type.TD_UpdateDisplay, "TouchDisplayEmulator(Welcome)"));
				} else if (timerId < Timer.IDLE_RANGE) {
					atmss.send(new Msg(id, mbox, Msg.Type.TimesUp, msg.getDetails()));
				}
				
				break;
				
			case Terminate:
				quit = true;
				break;

			default:
				log.warning(id + ": unknown message type: [" + msg + "]");
			}
		}

		// declaring our departure
		appKickstarter.unregThread(this);
		log.info(id + ": terminating...");
	} // run

	// ------------------------------------------------------------
	// handleUpdateDisplay
	protected void handleUpdateDisplay(Msg msg) {
		log.info(id + ": update display -- " + msg.getDetails());
	} // handleUpdateDisplay

	protected void td_showAccountNo(Msg msg) {
	}
	
	protected void td_disableAccount(Msg msg) {
	}
	
	protected void td_displayBalance(Msg msg) {
	}
	
	protected void td_updatePasswordField(Msg msg) {
	}
	
	protected void td_updateInputAmount(Msg msg) {
	}

} // TouchDisplayHandler
