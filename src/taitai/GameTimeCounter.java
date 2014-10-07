package taitai;

public class GameTimeCounter {
	private int turnCount;
	private int amountTurnCount;
	private int renewedCount;
	
	public void reset() {
		turnCount = 0;
		amountTurnCount = 0;
		renewedCount = 0;
	}
	
	public int getTurnCount() {
		return turnCount;
	}
	
	public int getAmountTurnCount() {
		return amountTurnCount;
	}
	
	public int getRenewedCount() {
		return renewedCount;
	}
	
	public void played() {
		turnCount++;
		amountTurnCount++;
	}
	
	public void renewed() {
		turnCount = 0;
		renewedCount++;
	}
}
