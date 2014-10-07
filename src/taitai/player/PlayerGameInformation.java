package taitai.player;

import jp.ac.uec.daihinmin.card.*;
import java.util.*;

import taitai.GameTimeCounter;

/**
 * プレイヤーごとに一回のゲームごとの状態を保存しておくクラス
 * @author tai
 *
 */
public class PlayerGameInformation {
	private int num;
	private int score;
	private int handSize;
	private int renewTurn;
	private Cards handCards = Cards.EMPTY_CARDS;
	private boolean isWon;
	private List<PlayerTurnInformation> playerTurnImformations;
	
	public PlayerGameInformation(int number) {
		playerTurnImformations = new LinkedList<PlayerTurnInformation>();
		num = number;
	}
	
	public int getScore() {
		return score;
	}
	
	public void playerWon(int score) {
		this.score = score;
		isWon = true;
		searchStrategy();
	}
	
	public int getNumber() {
		return num;
	}
	
	/**
	 * このプレイヤーの番が実行された直後に呼び出されるメソッド
	 * lastMeld,SuitLock,isReverseはそのプレイヤーの番に回ってきたときの場の状態．
	 * 
	 * @param meld
	 * @param lastMeld
	 * @param SuitLock
	 * @param isReverse
	 */
	public void play(
			Meld meld, Meld lastMeld, Suits SuitLock, 
			boolean isReverse, boolean isRenew, PlayerTurnState[] playerState, 
			GameTimeCounter timeCounter) {
		// TODO
		if(isRenew) renewTurn++;
		playerTurnImformations.add(new PlayerTurnInformation(
				meld,lastMeld, SuitLock, 
				isReverse, playerState, timeCounter));
	}
	
	public int getHandSize() {
		return handSize;
	}
	
	public void gameStarted(int handSize) {
		this.handSize = handSize;
	}
	
	/**
	 * leastCardsには，大貧民になったときに，手元に残っているカードが渡される
	 * プレイヤーがどんな手札を持っていて，どんな
	 * @param leastCards
	 */
	public void gameEnded(Cards leastCards) {
		// TODO
		if(!isWon) {
			handCards = handCards.add(leastCards);
			playerWon(1);
		}
	}
	
	private void searchStrategy() {
		for(int i = playerTurnImformations.size() - 1; i >= 0; i--) {
			playerTurnImformations.get(i).setHandCards(handCards);
			handCards = handCards.add(playerTurnImformations.get(i).getPuttedCards());
		}
//		for(int i = 0; i < playerTurnImformations.size(); i++) {
//			System.out.println(playerTurnImformations.get(i));
//		}
	}
	
	public int getRenewTurnCount() {
		return renewTurn;
	}
}
