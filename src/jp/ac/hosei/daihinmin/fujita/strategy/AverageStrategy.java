package jp.ac.hosei.daihinmin.fujita.strategy;

import jp.ac.hosei.daihinmin.fujita.State;
import jp.ac.hosei.daihinmin.fujita.Utils;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

/**
 * 役集合を計算するための戦略
 * @author fujita
 *
 */
public class AverageStrategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return true;
	}

	/**
	 * 呼び出されないメソッド
	 */
	@Override
	public Meld execute(BotSkeleton bot, State state) {
        int total = 0;
    	for(Card card: bot.hand()) {
    		if(card.equals(Card.JOKER)) {
    			//joker = true;
    			if(state.order) {
    				total += 16;
    			} else {
    				total += 2;
    			}
    		} else {
    			total += card.rank().toInt();
    		}
    	}
    	double average = (double)total / bot.hand().size();
    	double threshold = 0.7;
    	boolean order = state.order;

    	//候補のうち１つを最終候補とする．ここでは，melds.get(0)
    	Meld candidate = null;
    	int min = 100;
    	if(!order) min = 0;

		for(Meld meld: state.melds) {
    		// 残り20枚までは、ペアを崩さない
    		if(!Utils.isJustSize(bot, meld, state) && state.getNumberOfAllCards(bot) > 15) {
    			continue;
    		}
    		int sum = 0;
    		for(Card card: meld.asCards()) {
    			if(card == Card.JOKER) {
    				if(order) {
    					sum += 16;
    				} else {
    					sum += 2;
    				}
    			} else {
    				sum += card.rank().toInt();
    			}
    		}

    		sum = total - sum;
    		double av = (double)sum / (bot.handSize() - meld.asCards().size());
    		int rank = meld.rank().toInt();
    		if(order) {
    			if(av > average - threshold || bot.handSize() <= 4) {
    				if(rank < min) {
    					min = rank;
    					candidate = meld;
    				}
    			}
    		} else {
    			if(av < average + threshold || bot.handSize() <= 4) {
    				if(rank > min) {
    					min = rank;
    					candidate = meld;
    				}
    			}
    		}
    	}

    	if(candidate == null) {
    		return PASS;
    	} else {
    		if(bot.handSize() >= 8) {
    			int count = 0;
    			int rank = candidate.rank().toInt();
    			for(Card card: bot.hand()) {
    				if(card == Card.JOKER) {
    					count++;
    				} else if(order && card.rank().toInt() > rank) {
    					count++;
    				} else if(!order && card.rank().toInt() < rank) {
    					count++;
    				}
    			}

    			if(count < 2) {
    				return PASS;
    			}
    		}
    		return Utils.checkJoker(candidate, bot.place());
    	}
	}
}
