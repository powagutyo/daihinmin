package taitai.player;

import java.util.*;

import taitai.GameTimeCounter;

import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;

public class PlayerInformationImp {

	private int num;
	private int seat;
	private boolean isPassed = false;
	private boolean isWin = false;
	private LinkedList<PlayerGameInformation> gameImformations;
	private PlayerScore score;
	
	public PlayerInformationImp(int number) {
		num = number;
		score = new PlayerScore(number);
		gameImformations = new LinkedList<PlayerGameInformation>();
		
	}
	
	public int getSeat() {
		return seat;
	}
	
	public void setSeat(int seat) {
		this.seat = seat;
	}
	
	public boolean isWin() {
		return isWin;
	}
	
	public int getNumber() {
		return num;
	}
	
	public Boolean isPassed() {
		return isPassed;
	}
	
	public void placeRenewed() {
		isPassed = false;
	}
	
	public void gameStarted() {
		isWin = false;
		placeRenewed();
		gameImformations.add(new PlayerGameInformation(num));
	}
	
	public void gameEnd(Cards leastCards) {
		if(!isWin) {
			score.playerWon(1);
		}
		gameImformations.getLast().gameEnded(leastCards);
	}
	
	public void play(Place place, Meld meld, 
			GameTimeCounter timeCounter, PlayerTurnState[] playersState) {
		if(meld.type() == Meld.Type.PASS)
			isPassed = true;		
		gameImformations.getLast().play(
				meld, place.lastMeld(), place.lockedSuits(), 
				place.isReverse(), place.isRenew(), playersState ,timeCounter);
	}
	
	/*
	 * このプレイヤーがあがったときに呼び出されるメソッド
	 */
	public void playerWon(int score) {
		if(score == 1) return;
//		gameImformations.getLast();
		this.score.playerWon(score);
		isWin = true;
		gameImformations.getLast().playerWon(score);
	}
	
	public int getRenewTurnCout() {
		return gameImformations.getLast().getRenewTurnCount();
	}

	public String toString() {
		return score.toString();
	}
}
