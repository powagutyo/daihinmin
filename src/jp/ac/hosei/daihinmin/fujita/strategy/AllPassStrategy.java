package jp.ac.hosei.daihinmin.fujita.strategy;

import jp.ac.hosei.daihinmin.fujita.State;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

public class AllPassStrategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		// 最後のプレイヤが自分であることも確認
		// System.out.println("LastPlayer = " + state.lastPlayer + ", " + state.isOtherPlayerAllPassed(bot.number()));
		/*
		return (state.isOtherPlayerAllPassed(bot.number())
				&& state.lastPlayer == bot.number());
				*/
		// パスを見なくても、最後のプレイヤが自分であれば、他は皆パスのはず
		return (state.lastPlayer == bot.number());
	}

	public Meld execute(BotSkeleton bot, State state) {
		return PASS;
	}
}
