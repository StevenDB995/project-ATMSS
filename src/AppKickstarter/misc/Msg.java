package AppKickstarter.misc;

//======================================================================
// Msg
public class Msg {

	private String sender;
	private MBox senderMBox;
	private Type type;
	private String details;

	// ------------------------------------------------------------
	// Msg
	public Msg(String sender, MBox senderMBox, Type type, String details) {
		this.sender = sender;
		this.senderMBox = senderMBox;
		this.type = type;
		this.details = details;
	} // Msg

	// ------------------------------------------------------------
	// getters
	public String getSender() {
		return sender;
	}

	public MBox getSenderMBox() {
		return senderMBox;
	}

	public Type getType() {
		return type;
	}

	public String getDetails() {
		return details;
	}

	// ------------------------------------------------------------
	// toString
	public String toString() {
		return sender + " (" + type + ") -- " + details;
	} // toString

	// ------------------------------------------------------------
	// Msg Types
	public enum Type {
		Terminate, // Terminate the running thread
		SetTimer, // Set a timer
		CancelTimer, // Cancel a timer
		Tick, // Timer clock ticks
		TimesUp, // Time's up for the timer
		Poll, // Health poll
		PollAck, // Health poll acknowledgement
		IdleTimer,
		TD_UpdateDisplay, // Update Display
		TD_MouseClicked, // Mouse Clicked
		TD_UpdatePasswordField,
		TD_UpdateInputAmount,
		CR_CardInserted, // Card inserted
		CR_CardRemoved, // Card removed
		CR_CardRetained, // Card retained due to wrong password for three times
		CR_EjectCard, // Eject card
		KP_KeyPressed, // Key pressed
		CDC_UpdateCashDepositCollectorSlot, // Update display of slot of cash deposit collector
		CD_UpdateCashDispenserSlot, // Update display of slot of cash dispenser
		BZ_Sound, // Buzzer makes sound
		BAMS_ChooseAccount, // For touch display to display available account
		BAMS_FromAccount,
		BAMS_Balance,
		CDC_ButtonPressed, // Button pressed in cash deposit collector
		CD_ButtonPressed, // Button pressed in cash dispenser
		AP_UpdateAdvicePrinter, // Update display of advice printer
		AP_ButtonPressed, // Button pressed in advice printer
	} // Type

} // Msg
