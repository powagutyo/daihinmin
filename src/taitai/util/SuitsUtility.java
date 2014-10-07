package taitai.util;

import jp.ac.uec.daihinmin.card.Suit;
import jp.ac.uec.daihinmin.card.Suits;

public class SuitsUtility {
	public static Suits allSuits = Suits.valueOf(SuitUtility.suiteList);
	
	public static Suits not(Suits suits) {
		Suits result = Suits.EMPTY_SUITS;
		for(Suit s : allSuits) {
			if(!suits.contains(s))
				result = result.add(s);
		}
		return result;
	}
	
	public static Suits and(Suits a, Suits b) {
		Suits result = Suits.EMPTY_SUITS;
		for(Suit s : a) {
			if(b.contains(s)) result = result.add(s);
		}
		return result;
	}
}
