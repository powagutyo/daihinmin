package jp.ac.hosei.daihinmin.fujita.strategy;

import jp.ac.hosei.daihinmin.fujita.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class YomikiriStrategy extends Strategy {
	Meld meld = null;

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		Meld meld = state.yomikiri(bot.hand(), state.melds);

		if(meld != null) {
			this.meld = Utils.checkJoker(meld, bot.place());
			return true;
		}

		/*
		int size = bot.handSize();

		if(state.melds.get(0).asCards().size() == size) {
			// 勝利
			this.meld = Utils.checkJoker(state.melds.get(0), bot.place());
			return true;
		} else {
			Extractor<Meld,Melds> maxSize = Melds.MAX_SIZE;

			for(Meld meld: state.melds) {
				if(state.isStrongestMeld(meld, state.order)) {
					Cards remain = bot.hand().remove(meld.asCards());
					Melds remainMelds = Melds.parseMelds(remain);
					// 残りカードでできる最大枚数の役
					Melds maxRemainMelds = remainMelds.extract(maxSize);

					// 残り2役で終わる場合に、勝てるかどうかを判定
					if(maxRemainMelds.get(0).asCards().size() == remain.size()) {
						this.meld = meld;
						return true;
					}
				}
			}
		}
		*/

		return false;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		return meld;
	}
}
