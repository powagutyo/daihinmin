package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class RevolutionStrategy extends Strategy {
	private Meld meld;

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		// 場が流れた後で、自分が貧民以下の時だけ適用
		if(bot.place().isRenew()) {
			int rank;
			try {
				rank = bot.rank();
			} catch (Exception e) {
				rank = -1;
			}
			if(rank > 2) {
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
		int total = 0;
		int value = 0;
		
		for(int i = 0; i < 4; i++) {
			for(int j = 1; j <= 13; j++) {
				if(!state.playedCards[i][j]) {
					if(j < 3) {
						value += j + 13;
					} else {
						value += j;
					}
				}
				total++;
			}
		}
		
		double average = (double)value / total;
		
		int myTotal = 0;
		int myValue = 0;
		
		Cards cards = state.hand.remove(meld.asCards());
		for(Card card: state.hand) {
			if(card == Card.JOKER) continue;
			myValue += card.rank().toInt();
			myTotal++;
		}
		
		double myAverage = (double)myValue / myTotal;
		
		if(state.order) {
			if(myAverage < average) {
				return meld;
			} else {
				return null;
			}
		} else {
			if(myAverage > average) {
				return meld;
			} else {
				return null;
			}
		}
	}

}
