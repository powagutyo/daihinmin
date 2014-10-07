package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Extractor;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class KeepWeakestStrategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		int rank;
		try {
			rank = bot.rank();
		} catch (Exception e) {
			rank = 0;
		}
		// System.out.println("ランクは" + rank);
		return rank <= 2 && state.hand.size() > 5; 
	}

	public Meld execute(BotSkeleton bot, State state) {
		Extractor<Meld,Melds> minRank = Melds.MIN_RANK;
		Extractor<Meld,Melds> maxSize = Melds.MAX_SIZE;
		if(!state.order) {
			minRank = Melds.MAX_RANK;
		}
		
		Meld meld = state.melds.extract(minRank).extract(maxSize).get(0);
		
		if(meld.asCards().size() != state.hand.size()) {
			Cards cards = state.hand.remove(meld.asCards());
			state.melds = Melds.parseMelds(cards);
			state.weakestMeld = meld;
			state.hand = state.hand.remove(meld.asCards());
		}
		
		return null;
	}
}
