package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Extractor;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class KeepSequenceStrategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		if(bot.place().isRenew() || state.start) {
			return !state.seqMelds.isEmpty() && state.hand.size() > 6;
		} else {
			return bot.place().type() != Meld.Type.SEQUENCE && !state.seqMelds.isEmpty() && state.hand.size() > 6;
		}
	}

	public Meld execute(BotSkeleton bot, State state) {
		Cards hand = state.hand;
		
		for(Meld meld: state.seqMelds) {
			Cards cards = meld.asCards(); 
			int rank = meld.rank().toInt();
			
			if(state.order? rank + cards.size() - 1 > 13: rank < 5) {
				continue;
			}
			
			boolean fail = false;
			Cards tmp = hand;
			for(Card card: cards) {
				if(tmp.contains(card)) {
					tmp = tmp.remove(card);
				} else {
					fail = true;
					break;
				}
			}
			
			if(!fail) {
				hand = tmp;
			}
		}
		
		if(!hand.isEmpty()) {
			state.hand = hand;
		}
		
		return null;
	}
}
