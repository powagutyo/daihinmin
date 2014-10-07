package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.hosei.daihinmin.fujita2.Utils;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.card.Meld.Type;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class LockingStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return bot.place().lockedSuits().equals(Suits.EMPTY_SUITS) && bot.place().type() == Type.SINGLE;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		//場を縛っているスート集合に適合する役を抽出して,候補とする．
		Melds melds = state.melds.extract(Melds.suitsOf(bot.place().lastMeld().suits()));
		if(melds.isEmpty()) {
			return null;
		}
		
		if(state.order) {
			melds = melds.extract(Melds.MIN_RANK);
		} else {
			melds = melds.extract(Melds.MAX_RANK);
		}
		
		for(int i = 0; i < melds.size(); i++) {
			Meld meld = melds.get(i);
			Card card = meld.asCards().get(0);
			if(card == Card.JOKER) continue;
			if(Utils.isJustSize(bot, meld)) {
				if(card.rank() != Rank.EIGHT && state.numberOfMyStrongerCards(card) > 3) {
					return meld;
				}
			}
		}
		
		return null;
	}
}
