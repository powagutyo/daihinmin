package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class GiveUpStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return bot.place().lockedSuits().equals(Suits.EMPTY_SUITS) && state.shoudGiveUp(bot);
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		return PASS;
	}
}
