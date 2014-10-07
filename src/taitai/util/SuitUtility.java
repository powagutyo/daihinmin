package taitai.util;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Suit;

public class SuitUtility {
	public static Suit[] suiteList = {Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS};
	private static int getCardSuiteIndex(Card card) {
		switch(card.suit()) {
		case SPADES : return 0;
		case HEARTS : return 1;
		case CLUBS : return 2;
		case DIAMONDS : return 3;
		}
		return -1;
	}
}
