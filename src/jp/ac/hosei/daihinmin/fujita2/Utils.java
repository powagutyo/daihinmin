package jp.ac.hosei.daihinmin.fujita2;

import java.util.ArrayList;
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
    		if(first || (order && rank.toInt() < lowest) 
    				// ここに縛りのstrategy を入れよう!!
    				|| (!order && rank.toInt() > lowest)) {
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
    	boolean order = bot.place().order() == Order.NORMAL? true: false;
    	
		Rank rank = meld.rank();
		// キング以上の時, 8の時には、常に  true を返す
		int r = rank.toInt();

		if((order && r >= 14) || (!order && r <= 4)) {
			if(cards.size() == 1) {
				return true;
			} else {
				// A や 2 をペアで出さないための苦肉の策...失敗か...
				return true;
			}
		}
		
		if(r == 8) {
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
    			// debug("Sequence Check!!");
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
    
    public static void debug(String message) {
    	// System.out.println(message);
    }
    
	/**
	 * 可能な全 MeldSet を計算して求める
	 * @param cards カード集合
	 * @return MeldSet の List
	 */
	public static ArrayList<MeldSet> parseMelds(Cards cards) {
		Melds sequence = Melds.parseSequenceMelds(cards);
		// group meld
		MeldSet meldSet = parseGroupMelds(cards);
		
		if(sequence.isEmpty()) {
			ArrayList<MeldSet> list = new ArrayList<MeldSet>();
			list.add(meldSet);
			return list;
		} else {
			Meld meld = sequence.get(0);

			cards = cards.remove(meld.asCards());
			
			// sequence を採用した MeldSet のリスト
			ArrayList<MeldSet> list = parseMelds(cards);
			
			// sequence をそれぞれの要素に加える
			for(MeldSet set: list) {
				set.sequence = set.sequence.add(meld);
			}
			
			// sequence を採用しない  MeldSet もリストに加える
			list.add(meldSet);
			
			return list;
		}
	}
	
	/**
	 * GroupMelds を見つけ出す
	 * 最大枚数の GoupMeld だけを見つける
	 * @param cards
	 * @return Groupが設定されたMeldSet 
	 */
	public static MeldSet parseGroupMelds(Cards cards) {
		// JOKER があった場合、取り除く
		cards = cards.remove(Card.JOKER);
		
		MeldSet set = new MeldSet();
		
		for(Card card: cards) {
			int rank = card.rank().toInt() - 3;
			int suit = card.suit().ordinal();
			set.cards[suit][rank] = card;
		}
		
		return set;
	}
}
