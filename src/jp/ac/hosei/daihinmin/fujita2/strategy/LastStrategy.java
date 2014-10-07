package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class LastStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return state.melds.get(0).asCards().size() == state.hand.size();
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		return state.melds.get(0);
	}
}
