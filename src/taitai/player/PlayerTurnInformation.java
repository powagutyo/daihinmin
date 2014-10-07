package taitai.player;

import taitai.GameTimeCounter;
import taitai.PlaceState;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Suits;

public class PlayerTurnInformation {
	private Meld puttedMeld;
	private Cards handCards = Cards.EMPTY_CARDS;
	private PlaceState place;
	private int turnCount;
	private int amountGameTurnCount;
	
	public PlayerTurnInformation(
			Meld meld, Meld lastMeld, Suits SuitLock, 
			boolean isReverse, PlayerTurnState[] playerState, 
			GameTimeCounter timeCounter) {
//		System.out.println(lastMeld+" : "+SuitLock+" : "+meld);
		place = new PlaceState(lastMeld, isReverse, 
				lastMeld != null ? lastMeld.asCards().suits().equals(SuitLock) : false);
		puttedMeld = meld;
		turnCount = timeCounter.getTurnCount();
	}
	
	public void setHandCards(Cards handCards) {
		this.handCards = handCards;
		if(puttedMeld.type() != Meld.Type.PASS)
			this.handCards = handCards.add(puttedMeld.asCards());
	}
	
	public Cards getHandCards() {
		return handCards;
	}
	
	public Meld getPuttedMeld() {
		return puttedMeld;
	}
	
	public Cards getPuttedCards() {
		if(puttedMeld.type() == Meld.Type.PASS)
			return Cards.EMPTY_CARDS;
		else return puttedMeld.asCards();
	}
	
	public int getTurnCount() {
		return turnCount;
	}
	
	public int getAmountGameTurnCount() {
		return amountGameTurnCount;
	}
	
	public String toString() {
		return handCards.toString() + " : " + puttedMeld;
	}
}
