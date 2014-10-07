package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.hosei.daihinmin.fujita2.Utils;
import jp.ac.uec.daihinmin.card.Extractor;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class DefeatStrongestStrategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		// 最後のプレイヤが自分であることも確認
		// System.out.println("LastPlayer = " + state.lastPlayer + ", " + state.isOtherPlayerAllPassed(bot.number()));
		/*
		return (state.isOtherPlayerAllPassed(bot.number())
				&& state.lastPlayer == bot.number());
				*/
		// パスを見なくても、最後のプレイヤが自分であれば、他は皆パスのはず
		int rank;
		try {
			rank = bot.rank();
		} catch (Exception e) {
			rank = 0;
		}
		return rank == 1 && state.lastPlayer == state.strongestPartner(bot.number());
	}

	public Meld execute(BotSkeleton bot, State state) {
		Extractor<Meld,Melds> minRank = Melds.MIN_RANK;
		if(!state.order) {
			minRank = Melds.MAX_RANK;
		}
		
		Meld meld = state.melds.extract(minRank).get(0);
		
		if((state.order && meld.rank().toInt() < 8)
				|| (!state.order && meld.rank().toInt() > 11)) {
			return Utils.checkJoker(meld, bot.place());
		} else {
			return null;
		}
	}
}
