package jp.ac.hosei.daihinmin.fujita.strategy;

import jp.ac.hosei.daihinmin.fujita.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class RenewStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return bot.place().isRenew();
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		Cards all = bot.hand();
		boolean order = state.order;
		Melds melds = state.melds;

		Meld meld = state.yomikiri(all, melds);

		if(meld != null) {
			return Utils.checkJoker(meld, bot.place());
		}

		Extractor<Meld,Melds> maxSize = Melds.MAX_SIZE;

		/*
		Melds maxMelds = melds.extract(maxSize);

		if(maxMelds.get(0).asCards().size() == all.size()) {
			// 一手で終了して、勝利
			return Utils.checkJoker(maxMelds.get(0), bot.place());
		} else {
			for(Meld maxMeld: maxMelds) {
				Cards remain = all.remove(maxMeld.asCards());
				Melds remainMelds = Melds.parseMelds(remain);
				Melds maxRemainMelds = remainMelds.extract(maxSize);

				// 後2役で終わる場合に、勝てるかどうかを判定
				if(maxRemainMelds.get(0).asCards().size() == remain.size()) {
					// いずれかの最大サイズ役が最強ならば、勝ちを確信
					if(state.isStrongestMeld(maxMeld, order)) {
						return Utils.checkJoker(maxMeld, bot.place());
					}
					for(Meld meld: maxRemainMelds) {
						if(state.isStrongestMeld(meld, order)) {
							return Utils.checkJoker(meld, bot.place());
						}
					}
				}
			}
		}
		*/

    	Extractor<Meld,Melds> minRank = Melds.MIN_RANK;
    	if(!order) {
        	minRank = Melds.MAX_RANK;
    	}

    	// Joker は温存する
    	if(all.contains(Card.JOKER)) {
    		melds = Melds.parseMelds(all.remove(Card.JOKER));
    	}
    	Melds m1 = melds.extract(minRank).extract(maxSize);
    	Melds m2 = melds.extract(maxSize).extract(minRank);
    	int n1 = m1.get(0).asCards().size();
    	int n2 = m2.get(0).asCards().size();
    	int r1 = m1.get(0).rank().toInt();
    	int r2 = m2.get(0).rank().toInt();
    	if(order) {
    		if(n2 - n1 >= r2 - r1) {
    			// JOKER を含まず、size の差が rank の差より大きい場合は大きいsizeを優先
    			return m2.get(0);
    		}
    	} else {
    		if(n2 - n1 >= r1 - r2) {
    			return m2.get(0);
    		}
    	}
    	return m1.get(0);
	}
}
