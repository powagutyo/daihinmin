package jp.ac.hosei.daihinmin.fujita2.strategy;

import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.hosei.daihinmin.fujita2.Utils;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class StrongerMoreThanHalfStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
    	// 強さの閾値を、最初0.7 で最後は0.4まで下げる
    	double value = 0.4 + 0.3 * state.getNumberOfAllCards(bot) / 53;

    	int strongerCards = state.numberOfStrongerCards(bot.hand(), value, state.order);

    	// 強いカードが半数以上になっているので、出せるカードを低い順に出していけば
    	// 勝てるはず
    	//System.out.printf("strongerCards = %d, totalCards = %d%n", strongerCards, bot.handSize());
    	return strongerCards * 2.4 > bot.handSize();
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		Extractor<Meld,Melds> minRank = Melds.MIN_RANK;
		if(!state.order) {
			minRank = Melds.MAX_RANK;
		}
		Meld meld = Utils.selectJustSizeMeld(bot, state.melds);
		
		if(meld == PASS) {
			meld = state.melds.extract(minRank).get(0);
		}

		// return meld;
		
		int rank = 0;
		
		try {
			rank = bot.rank();
		} catch (Exception e) {
		}
		
		Cards cards = meld.asCards();
		
		if(state.hand.size() > 5 && state.numberOfMyStrongerCards(cards.get(0)) < 1) {
			return null;
		}

		if(rank == 1 && state.hand.size() > 6 && cards.size() > 1 && state.numberOfMyStrongerCards(cards.get(0)) < 2) {
			return null;
		} else if(rank == 2 && state.hand.size() > 7 && cards.size() > 1 && state.numberOfMyStrongerCards(cards.get(0)) < 2) {
			return null;
		} else if(rank == 3 && state.hand.size() > 8 && cards.size() > 1 && state.numberOfMyStrongerCards(cards.get(0)) < 2) {
			return null;
		} else if(rank == 4 && state.hand.size() > 9 && cards.size() > 0 && state.numberOfMyStrongerCards(cards.get(0)) < 2) {
			return null;
		} else if(rank == 5 && state.hand.size() > 10 && cards.size() > 0 && state.numberOfMyStrongerCards(cards.get(0)) < 2) {
			return null;
		} else {
			return meld;	
		}
		
		/*
		if(meld == PASS) {
			// PASS するくらいなら、最低ランクのカードを場に出す
			return state.melds.extract(minRank).get(0);
		} else {
			return meld;
		}
		*/
	}
}
