package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class LastTwoStrategy extends Strategy {
	
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return state.melds.get(0).asCards().size() == state.hand.size() - 1;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		Melds minRank = state.melds.extract(state.order? Melds.MIN_RANK: Melds.MAX_RANK);
		Meld meld = minRank.get(0);
		Cards cards = meld.asCards();
		Cards remain = state.hand.remove(cards);
		
		int rank = 0;
		
		if(remain.get(0) == Card.JOKER) {
			return state.melds.get(0);
		} else if(cards.get(0) == Card.JOKER) {
			if(cards.size() == 1) {
				return null;
			} else {
				rank = cards.get(1).rank().toInt(); 
			}
		} else {
			rank = cards.get(0).rank().toInt(); 
		}
		int diff = rank - remain.get(0).rank().toInt();
		if(state.order && diff + 3 < 0 || !state.order && diff - 3 > 0) {
			return state.melds.get(0);
		}
		if(diff < 0) diff = - diff;
		if(diff > 3) {
			return PASS; 
		} else {
			return null;
		}
	}
}
