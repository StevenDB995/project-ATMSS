package ATMSS.BAMSHandler;

import java.util.logging.Logger;

public class AdvancedBAMSHandler extends BAMSHandler {
	
	public AdvancedBAMSHandler(String url) {
		super(url);
	}
	
	public AdvancedBAMSHandler(String url, Logger log) {
		super(url, log);
	}
	
	public double processPayment() {
		return 0;
	}
	
	public String changeCardPIN() {
		return "FAILED";
	}
	
	public String setOverseaWithdrawalLimit() {
		return "FAILED";
	}
}
