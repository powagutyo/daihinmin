package jp.ac.hosei.daihinmin.fujita;

import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import static jp.ac.uec.daihinmin.card.MeldFactory.createSingleMeldJoker;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

/**
 * ユーティリティメソッド群を定義
 * @author satoru
 *
 */
public class Utils {
	/**
	 * カードのランク差を計算
	 * @param c1 カード1
	 * @param c2 カード2
	 * @return カードのランク差
	 */
	public static int diffOfRank(Card c1, Card c2) {
		return c1.rank().toInt() - c2.rank().toInt();
	}

	/**
	 * 最終候補が JOKER の単騎だった時の対処
	 * @param meld 最終候補
	 * @return JOKER の単騎の時には、最強のJOKERを返却
	 */
	public static Meld checkJoker(Meld meld, Place place) {
		if(meld.type() == Meld.Type.SINGLE && meld.asCards().get(0) == Card.JOKER){
			//場のスートに合わせた,最大のランクを持つ役に変更して,それを出す．
			Suit suit;
			if(place.suits().size() == 0) {
				// 取り合えず、スペードにしておく
				suit = Suit.SPADES;
			} else {
				suit = place.suits().get(0);
			}

			Rank rank;
			if(place.order() == Order.NORMAL) {
				rank = Rank.JOKER_HIGHEST;
			} else {
				rank = Rank.JOKER_LOWEST;
			}
			return createSingleMeldJoker(suit, rank);
		} else {
			return meld;
		}
	}

    public static Meld selectJustSizeMeld(BotSkeleton bot, Melds melds) {
    	int lowest = 17;
    	boolean order = bot.place().order() == Order.NORMAL? true: false;
    	if(!order) {
    		lowest = 1;
    	}
    	boolean first = true;
    	Meld candidate = PASS;

    	for(Meld meld: melds) {
    		if(!isJustSize(bot, meld)) {
    			continue;
    		}
    		Rank rank = meld.rank();
    		if(first || (order && rank.toInt() < lowest) || (!order && rank.toInt() > lowest)) {
    			first = false;
    			candidate = meld;
    			lowest = rank.toInt();
    		}
    	}

    	return candidate;
    }

    public static boolean isJustSize(BotSkeleton bot, Meld meld) {
    	Cards cards = meld.asCards();
    	if(cards.size() == 1 && cards.get(0) == Card.JOKER) {
    		return true;
    	}
		Rank rank = meld.rank();
		// キング以上の時, 8の時には、常に  true を返す
		int r = rank.toInt();

		if(r >= 14 || r == 8) {
			return true;
		}

		Cards same = bot.hand().remove(Card.JOKER).extract(Cards.rankOf(rank));
		if(same.size() == bot.place().size()) {
			return true;
		} else {
			return false;
		}
    }

    public static boolean isJustSize(BotSkeleton bot, Meld meld, State state) {
    	boolean just = isJustSize(bot, meld);
    	if(just) {
    		Cards cards = meld.asCards();
    		if(state.sequence && cards.size() == 1) {
    			// System.out.println("Sequence Check!!");
    			Card card = cards.get(0);
    			Melds melds = Melds.parseSequenceMelds(bot.hand().remove(Card.JOKER));
    			if(melds.isEmpty()) {
    				state.sequence = false;
    				return just;
    			} else {
    				for(Meld m: melds) {
    					if(m.asCards().contains(card)) {
    						return false;
    					}
    				}
    			}
    		}
    	}

    	return just;
    }
    
    public static void debug(String mesg) {
    	// System.out.println(mesg);
    }
}
