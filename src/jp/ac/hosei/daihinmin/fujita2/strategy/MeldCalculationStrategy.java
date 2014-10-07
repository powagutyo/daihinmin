package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.Order;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

/**
 * 役集合を計算するための戦略
 * @author fujita
 *
 */
public class MeldCalculationStrategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
    	//state.hand = bot.hand();
        state.melds = Melds.parseMelds(state.hand);
		state.order = (bot.place().order() == Order.NORMAL? true: false);

        // 常に false
        return false;
	}

	/**
	 * 呼び出されないメソッド
	 */
	@Override
	public Meld execute(BotSkeleton bot, State state) {
		return null;
	}

}
