package taitai.util;

import jp.ac.uec.daihinmin.card.*;

public class CardUtility {
	public static Card getJoker() {
		return Card.JOKER;
	}
	
	public static Card getCard(int suite, int rank) {
		return Card.valueOf(CardsUtility.suites[suite]+CardsUtility.ranks[rank]);
	}
	
	/**
	 * 同一スートで次の値のカードを返す
	 * @param card
	 * @return
	 */
	public static Card getNext(Card card) {
		Rank nextRank = RankUtility.getNextRank(card);
		if(nextRank == null) return null;
		return Card.valueOf(card.suit(), nextRank);
	}
	
	public static Card getBefore(Card card) {
		Rank beforeRank = RankUtility.getBeforeRank(card);
		if(beforeRank == null) return null;
		return Card.valueOf(card.suit(), beforeRank);
	}
}
