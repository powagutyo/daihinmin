package jp.ac.hosei.daihinmin.fujita2.strategy;

import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class NoCandidateStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return state.melds == null || state.melds.isEmpty();
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		return PASS;
	}
}
