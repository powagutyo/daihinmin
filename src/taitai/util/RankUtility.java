package taitai.util;

import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;

public class RankUtility {
	private static final Rank[] normalHigh = 
		{Rank.TWO, Rank.ACE, Rank.KING, Rank.QUEEN};
	private static final Rank[] normalMiddle = 
		{Rank.JACK, Rank.TEN, Rank.NINE, Rank.SEVEN};
	private static final Rank[] normalLow = 
		{Rank.SIX, Rank.FIVE, Rank.FOUR, Rank.THREE};
	private static final Rank[] reverseHigh = normalLow;
	private static final Rank[] reverseMiddle = normalMiddle;
	private static final Rank[] reverseLow = normalHigh;
	
	public static Rank[] rankList = {
		Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE,
		Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE, Rank.TWO
	};
	public static int getCardRankIndex(Card card) {
		switch(card.rank()) {
		case THREE : return 0;
		case FOUR : return 1;
		case FIVE : return 2;
		case SIX : return 3;
		case SEVEN : return 4;
		case EIGHT : return 5;
		case NINE : return 6;
		case TEN : return 7;
		case JACK : return 8;
		case QUEEN : return 9;
		case KING : return 10;
		case ACE : return 11;
		case TWO : return 12;
		}
		return -1;
	}
	public static Rank getNextRank(Rank rank) {
		switch(rank) {
		case THREE : return Rank.FOUR;
		case FOUR : return Rank.FIVE;
		case FIVE : return Rank.SIX;
		case SIX : return Rank.SEVEN;
		case SEVEN : return Rank.EIGHT;
		case EIGHT : return Rank.NINE;
		case NINE : return Rank.TEN;
		case TEN : return Rank.JACK;
		case JACK : return Rank.QUEEN;
		case QUEEN : return Rank.KING;
		case KING : return Rank.ACE;
		case ACE : return Rank.TWO;
		}
		return null;
	}
	
	public static Rank getNextRank(Card card) {
		return getNextRank(card.rank());
	}
	
	public static Rank getBeforeRank(Rank rank) {
		switch(rank) {
		case FOUR : return Rank.THREE;
		case FIVE : return Rank.FOUR;
		case SIX : return Rank.FIVE;
		case SEVEN : return Rank.SIX;
		case EIGHT : return Rank.SEVEN;
		case NINE : return Rank.EIGHT;
		case TEN : return Rank.NINE;
		case JACK : return Rank.TEN;
		case QUEEN : return Rank.JACK;
		case KING : return Rank.QUEEN;
		case ACE : return Rank.KING;
		case TWO : return Rank.ACE;
		}
		return null;
	}
	
	public static Rank getBeforeRank(Card card) {
		return getBeforeRank(card.rank());
	}
	
	/**
	 * rulesに従って，placeの状況からlastMeldのRankの次の値を返す．
	 * @param place
	 * @param rules
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Rank getNextRank(Place place, Rules rules) 
	throws IllegalArgumentException {
		Meld lastMeld = place.lastMeld();
		return lastMeld.type() == Meld.Type.SEQUENCE ? 
				rules.nextRankSequence(lastMeld.rank(), lastMeld.asCards().size(), place.order())
				: place.order() == Order.NORMAL ? 
						lastMeld.rank().higher() : lastMeld.rank().lower();
	}
	
	public static boolean useHighRank(Rank rank, Place place) {
		if(place.order() == Order.NORMAL) {
			for(Rank r : normalHigh)
				if(rank.equals(r)) return true;
		} else {
			for(Rank r : reverseHigh)
				if(rank.equals(r)) return true;
		}
		return false;
	}
	
	public static boolean useMiddleRank(Rank rank, Place place) {
		if(place.order() == Order.NORMAL) {
			for(Rank r : normalMiddle)
				if(rank.equals(r)) return true;
		} else {
			for(Rank r : reverseMiddle)
				if(rank.equals(r)) return true;
		}
		return false;
	}
	
	public static boolean useLowRank(Rank rank, Place place) {
		if(place.order() == Order.NORMAL) {
			for(Rank r : normalLow)
				if(rank.equals(r)) return true;
		} else {
			for(Rank r : reverseLow)
				if(rank.equals(r)) return true;
		}
		return false;
	}
	
	/**
	 * 通常の場のときのメソッド
	 * @param rank
	 * @return
	 */
	public static boolean useLowRank(Rank rank) {
		for(Rank r : normalLow)
			if(rank.equals(r)) return true;
		return false;
	}
}
