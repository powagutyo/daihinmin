package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.hosei.daihinmin.fujita2.Utils;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

public class LessThanHalfStrategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return true;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		Cards sorted = Cards.sort(state.hand);
		int size = state.hand.size();
		//int middle = size / 2 + (state.order? size % 2: 0) - 1;
		// 少しだけ強気にする
		//int middle = size / 2 + (state.order? size % 2: 0);
		int middle = size / 2 + (state.order? 0: size % 2 - 1);

		if(size > 8) {
			middle += (state.order? 1: -1);
		}

		if(middle >= state.hand.size()) {
			middle = middle - 1;
		} else if(middle < 0) {
			middle = 0;
		}
		
		
		Card middleCard = sorted.get(middle);
		int rank;
		if(middleCard == Card.JOKER) {
			rank = (state.order? 16: 2); 
		} else {
			rank = middleCard.rank().toInt();															
		}
		Melds melds = Melds.sort(state.melds);
		
		if(state.order) {
			for(int i = 0; i < melds.size(); i++) {
				int now = melds.get(i).rank().toInt();
				if(state.hand.size() - melds.get(i).asCards().size() == 1) {
					if(now < rank && 
							Utils.isJustSize(bot, melds.get(i))) {
						return melds.get(i);
					} else {
						return null;
					}
				}
				if(now <= rank && 
						Utils.isJustSize(bot, melds.get(i))) {
					return melds.get(i);
				}
			}
		} else {
			for(int i = melds.size() - 1; i >= 0; i--) {
				int now = melds.get(i).rank().toInt();
				if(state.hand.size() - melds.get(i).asCards().size() == 1) {
					if(now > rank && 
							Utils.isJustSize(bot, melds.get(i))) {
						return melds.get(i);
					} else {
						return null;
					}
				}
				if(now >= rank && 
						Utils.isJustSize(bot, melds.get(i))) {
					return melds.get(i);
				}
			}
		}
				
		return null;
	}
}
