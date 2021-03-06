package AppKickstarter.timer;

import AppKickstarter.misc.*;
import AppKickstarter.AppKickstarter;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


//======================================================================
// Timer
public class Timer extends AppThread {
    private static int simulationSpeed = 1000;
    private final int ticks;
    private static MBox timerMBox = null;
    private Ticker ticker = null;
    private ArrayList<ActiveTimer> timerList = null;
    private static long systemStartTime;
    
    public static long POLL_SLEEPTIME;
    public static long CANCEL_SLEEPTIME;
    public static long IDLE_SLEEPTIME;
    
    public static int POLL_RANGE;
    public static int CANCEL_RANGE;
    public static int IDLE_RANGE;

    //------------------------------------------------------------
    // Timer
    public Timer(String id, AppKickstarter appKickstarter) {
	super(id, appKickstarter);
	ticker = new Ticker(getMBox());
	timerMBox = getMBox();
	timerList = new ArrayList<ActiveTimer>();
	ticks = Integer.parseInt(appKickstarter.getProperty("Timer.MSecPerTick"));
	simulationSpeed = Integer.parseInt(appKickstarter.getProperty("Timer.SimulationSpeed"));
	
	POLL_SLEEPTIME = Long.parseLong(appKickstarter.getProperty("Timer.POLL_SLEEPTIME"));
	CANCEL_SLEEPTIME = Long.parseLong(appKickstarter.getProperty("Timer.CANCEL_SLEEPTIME"));
	IDLE_SLEEPTIME = Long.parseLong(appKickstarter.getProperty("Timer.IDLE_SLEEPTIME"));
	
	POLL_RANGE = Integer.parseInt(appKickstarter.getProperty("Timer.POLL_RANGE"));
	CANCEL_RANGE = Integer.parseInt(appKickstarter.getProperty("Timer.CANCEL_RANGE"));
	IDLE_RANGE = Integer.parseInt(appKickstarter.getProperty("Timer.IDLE_RANGE"));
    } // Timer


    //------------------------------------------------------------
    // run
    public void run() {
	boolean quit = false;
	log.info(id + ": starting...");
	systemStartTime = System.currentTimeMillis();
	new Thread(ticker).start();

	while (!quit) {
	    Msg msg = mbox.receive();

	    switch (msg.getType()) {
		case Tick:
		    chkTimeout();
		    break;

		case SetTimer:
		    set(msg);
		    break;

		case CancelTimer:
		    cancel(msg);
		    break;

		case Terminate:
		    log.info(id + ": received terminate!");
		    ticker.stopTicker();
		    quit = true;
		    break;

		default:
		    String eMsg = "Invalid command for Timer: "+msg;
		    throw (new RuntimeException(eMsg));
	    }
	}

	// declaring our departure
	appKickstarter.unregThread(this);
	log.info(id + ": terminating...");
    } // run


    //------------------------------------------------------------
    // chkTimeout
    private void chkTimeout() {
	long currentTime = (new Date()).getTime();
	ArrayList<ActiveTimer> timeoutTimers = new ArrayList<ActiveTimer>();
	log.finest("Timer chk...");

	for (ActiveTimer timer : timerList) {
	    if (timer.timeout(currentTime)) {
		timeoutTimers.add(timer);
	    }
	}

	for (ActiveTimer timer : timeoutTimers) {
	    int timerID = timer.getTimerID();
	    String caller = timer.getCaller();
	    MBox mbox = timer.getMBox();
	    mbox.send(new Msg("Timer", null, Msg.Type.TimesUp, "["+String.format("%05d", timerID)+"]: Time's up!"));
	    timerList.remove(timer);
	}
    } // chkTimeout


    //------------------------------------------------------------
    // Ticker
    private class Ticker implements Runnable {
	private MBox timerMBox = null;
	private boolean quit = false;

	//----------------------------------------
	// ticker
	private Ticker(MBox timerMBox) {
	    this.timerMBox = timerMBox;
	} // Ticker


	//----------------------------------------
	// run
	public void run() {
	    while (!quit) {
		try {
		    Thread.sleep(ticks);
		} catch (Exception e) {};
		mbox.send(new Msg("Ticker", null, Msg.Type.Tick, "tick"));
	    }
	} // run


	//----------------------------------------
	// stopTicker
	private void stopTicker() {
	    quit = true;
	} // stopTicker
    } // Ticker


    //------------------------------------------------------------
    // ActiveTimer
    private static class ActiveTimer {
	private int  timerID;
	private long wakeupTime;
	private String caller;
	private MBox mbox;

	//----------------------------------------
	// ActiveTimer
	public ActiveTimer(int tid, long wakeupTime, String caller, MBox mbox) {
	    this.timerID = tid;
	    this.wakeupTime = wakeupTime;
	    this.caller = caller;
	    this.mbox = mbox;
	} // ActiveTimer

	//----------------------------------------
	// getters
	public int    getTimerID() { return this.timerID; }
	public String getCaller()  { return this.caller; }
	public MBox   getMBox()    { return this.mbox; }

	//----------------------------------------
	// timeout
	public boolean timeout(long currentTime) {
	    return currentTime > wakeupTime;
	} // timeout
    } // ActiveTimer


    //------------------------------------------------------------
    // setTimer (sleep time based on wall clock)
    public static int setTimer(String id, MBox mbox, long sleepTime, int timerIdRange) {
	int timerId = timerIdRange - (new Random()).nextInt(10000) - 1;
	timerMBox.send(new Msg(id, mbox, Msg.Type.SetTimer,"set timer, "+sleepTime+", "+timerId));
	return timerId;
    } // setTimer


    //------------------------------------------------------------
    // setSimulationTimer (sleep time based on simulation time)
    public static int setSimulationTimer(String id, MBox mbox, long simulationSleepTimeInSeconds, int timerIdRange) {
	return setTimer(id, mbox, simulationSleepTimeInSeconds*simulationSpeed, timerIdRange);
    } // setSimulationTimer


    //------------------------------------------------------------
    // getTimesUpMsgTimerId: returns timerId of a timeout msg (returns -1 on error)
    public static int getTimesUpMsgTimerId(Msg msg) {
	// chk msg sender
	if (!msg.getSender().equals("Timer") || msg.getType() != Msg.Type.TimesUp || !msg.getDetails().endsWith("]: Time's up!")) {
	    return -1;
	}
	String msgDetails = msg.getDetails();
	return Integer.parseInt(msgDetails.substring(1, msgDetails.indexOf(']')));
    } // getTimesUpMsgTimerId


    //------------------------------------------------------------
    // getSimulationTime (in seconds)
    public static long getSimulationTime() {
	return (System.currentTimeMillis()-systemStartTime)/simulationSpeed;
    } // getSimulationTime


    //------------------------------------------------------------
    // set
    private void set(Msg msg) {
	String details = msg.getDetails().substring(11);

	// get timerID
	String timerIDStr = details.substring(details.indexOf(", ")+2);
	int timerID = Integer.parseInt(timerIDStr);

	// get wakeup time
	String sleepTimeStr = details.substring(0, details.indexOf(", "));
	long sleepTime = Long.parseLong(sleepTimeStr);
	long wakeupTime = (new Date()).getTime() + sleepTime;

	// get caller & mbox
	String caller = msg.getSender();
	MBox mbox = msg.getSenderMBox();

	// add this new timer to timer list
	timerList.add(new ActiveTimer(timerID, wakeupTime, caller, mbox));
	log.finest(id+": "+caller+" setting timer: "+ "["+sleepTime+"], ["+timerID+"]");
    } // set


    //------------------------------------------------------------
    // cancelTimer
    public static void cancelTimer(String id, MBox mbox, int timerID) {
	timerMBox.send(new Msg(id, mbox, Msg.Type.CancelTimer, "cancel timer, "+timerID));
    } // cancelTimer

    //------------------------------------------------------------
    // cancel
    private void cancel(Msg msg) {
	// get timerID
	String details = msg.getDetails();
	String timerIDStr = details.substring(details.indexOf(", ")+2);
	int timerID = Integer.parseInt(timerIDStr);

	// get caller
	String caller = msg.getSender();

	ActiveTimer cancelTimer = null;

	for (ActiveTimer timer : timerList) {
	    if (timer.getTimerID() == timerID) {
		    cancelTimer = timer;
		    break;
	    }
	}

	if (cancelTimer != null) {
	    timerList.remove(cancelTimer);
	    log.finest(id+": "+caller+" cancelling timer: "+"["+timerID+"]");
	} else {
	    log.warning(id+": "+caller+" cancelling timer: "+"["+timerID+"]"+ " TIMER NOT FOUND!!");
	}
    } // cancel
} // Timer
