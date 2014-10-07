package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class LockedStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return !bot.place().lockedSuits().equals(Suits.EMPTY_SUITS);
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		//場を縛っているスート集合に適合する役を抽出して,候補とする．
		state.melds = state.melds.extract(Melds.suitsOf(bot.place().lockedSuits()));
		return null;
	}
}
