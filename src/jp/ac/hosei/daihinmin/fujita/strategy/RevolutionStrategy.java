package jp.ac.hosei.daihinmin.fujita.strategy;

import jp.ac.hosei.daihinmin.fujita.State;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class RevolutionStrategy extends Strategy {
	private Meld meld;

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		// 場が流れた後で、自分が貧民以下の時だけ適用
		if(bot.place().isRenew()) {
			Integer rank = bot.rank();
			if(rank != null && rank >= 4) {
				Extractor<Meld,Melds> maxSize = Melds.MAX_SIZE;
				Melds melds = state.melds.extract(maxSize);
				int size = melds.get(0).asCards().size();
				if(size >= 5) {
					this.meld = melds.get(0);
					return true;
				} else if(size >= 4) {
					for(Meld meld: melds) {
						if(meld.type() == Meld.Type.GROUP) {
							this.meld = meld;
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		return meld;
	}

}
