package jp.ac.hosei.daihinmin.fujita.strategy;

import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.hosei.daihinmin.fujita.State;
import jp.ac.hosei.daihinmin.fujita.Utils;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class StrongerMoreThanHalfStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
    	// 強さの閾値を、最初0.8 で最後は0.4まで下げる
    	double value = 0.5 + 0.3 * state.getNumberOfAllCards(bot) / 53;

    	int strongerCards = state.numberOfStrongerCards(bot.hand(), value, state.order);

    	// 強いカードが半数以上になっているので、出せるカードを低い順に出していけば
    	// 勝てるはず
    	//System.out.printf("strongerCards = %d, totalCards = %d%n", strongerCards, bot.handSize());
    	return strongerCards * 2 > bot.handSize();
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		Extractor<Meld,Melds> minRank = Melds.MIN_RANK;
		if(!state.order) {
			minRank = Melds.MAX_RANK;
		}
		Meld meld = Utils.selectJustSizeMeld(bot, state.melds);
		if(meld == PASS) {
			// PASS するくらいなら、最低ランクのカードを場に出す
			return state.melds.extract(minRank).get(0);
		} else {
			return meld;
		}
	}
}
