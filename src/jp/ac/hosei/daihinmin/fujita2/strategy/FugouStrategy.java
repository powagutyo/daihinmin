package jp.ac.hosei.daihinmin.fujita2.strategy;

import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.hosei.daihinmin.fujita2.Utils;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class FugouStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		if(state.botRank <= 2) {
			// 強さの閾値を、最初0.7 で最後は0.4まで下げる
			double value = 0.45 + 0.2 * state.getNumberOfAllCards(bot) / 53;
			
			int strongerCards = state.numberOfStrongerCards(bot.hand(), value, state.order);
			
			// 強いカードが半数以上になっているので、出せるカードを低い順に出していけば
			// 勝てるはず
			//System.out.printf("strongerCards = %d, totalCards = %d%n", strongerCards, bot.handSize());
			
			return strongerCards * 2.4 > bot.handSize();
		} else {
			return false;
		}
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		Extractor<Meld,Melds> minRank = Melds.MIN_RANK;
		if(!state.order) {
			minRank = Melds.MAX_RANK;
		}
		Meld meld = Utils.selectJustSizeMeld(bot, state.melds);
		
		if(meld == PASS) {
			if(state.hand.size() > 6) {
				return null;
			} else {
				meld = state.melds.extract(minRank).get(0);
			}
		}

		Cards cards = meld.asCards();

		if(state.hand.size() > 7 && cards.size() > 1 && state.numberOfMyStrongerCards(cards.get(0)) < cards.size() + 1) {
			return null;
		}

		//if(state.hand.size() > 6 && cards.size() > 1 && state.numberOfMyStrongerCards(cards.get(0)) < cards.size()) {
		if(cards.size() > 1 && state.numberOfMyStrongerCards(cards.get(0)) < cards.size()) {
			return null;
		} else {
			return meld;	
		}
	}
}
