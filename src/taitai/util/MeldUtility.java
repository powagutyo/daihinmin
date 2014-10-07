package taitai.util;

import java.util.*;
import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;

import static jp.ac.uec.daihinmin.card.Meld.Type.*;

public class MeldUtility {
	public static Meld S3 = MeldFactory.createSingleMeld(Card.S3);
	
	/**
	 * jokerの一枚出しであるかどうかを判定
	 * @param meld
	 * @return
	 */
	public static boolean isOnlyJoker(Meld meld) {
		return meld.type() == Meld.Type.SINGLE 
			&& meld.asCards().size() == 1 
			&& meld.asCards().contains(Card.JOKER);
	}
	
	/**
	 * aのmeldからbのmeldが構成するカードを抜いた残りのカードで
	 * 新しいmeldの集合を生成して返す
	 * @param a
	 * @param b
	 * @return
	 */
//	public static Melds removeCardsCreateNewMelds(Meld a, Meld b) {
//		Cards cards = CardsUtility.remove(a.asCards(), b.asCards());
//		Melds melds = 
//	}
	
	/**
	 * bはaの部分集合であるかを調べる
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isSubset(Meld a, Meld b) {
		if(a.type() == Meld.Type.PASS)
			return b.type() == Meld.Type.PASS;
		else if(b.type() == Meld.Type.PASS) {
			return true;
		}
		for(Card c : b.asCards()) {
			if(!a.asCards().contains(c))
				return false;
		}
		return true;
	}
	
	/**
	 * cardsから作られる連続のMeldを返す．
	 * 
	 * @param cards
	 * @param startRank
	 * @return
	 */
	public static Meld createSequence(Cards cards, Rank startRank) {
		if(cards.contains(Card.JOKER)) {
			return MeldFactory.createSequenceMeldWithJokers(cards, startRank);
		} else {
			return MeldFactory.createSequenceMeld(cards);
		}
	}
	
	/**
	 * cardとそれに含まれていない1枚のjokerを使ってgroupを生成する
	 * @param cardNotWithJoker
	 * @return
	 */
	public static Meld createGroupWithJoker(Cards cardNotWithJoker) {
		if(cardNotWithJoker.size() == 4)
			return MeldFactory.createGroupMeldWithJokers(
					cardNotWithJoker, SuitsUtility.allSuits);
		
		Suits suits = Suits.EMPTY_SUITS;
		for(Card card : cardNotWithJoker) {
			suits = suits.add(card.suit());
		}
		a : for(Suit suit : SuitUtility.suiteList) {
			for(Suit cardSuit : suits) {
				if(suit.equals(cardSuit)) continue a;
			}

			return MeldFactory.createGroupMeldWithJokers(
					cardNotWithJoker.add(Card.JOKER), Suits.EMPTY_SUITS.add(suit));
		}
		
		return MeldFactory.createGroupMeldWithJokers(
				cardNotWithJoker, SuitsUtility.allSuits);
	}
	
	/**
	 * jokerの含まれていないmeldにjokerを一枚加えた役を生成する
	 * @param meld
	 * @return
	 */
	public static Meld createAddJokerMeld(Meld meld) {
		if(meld.type() == Meld.Type.SEQUENCE)
			return createSequence(meld.asCards().add(Card.JOKER), meld.rank());
		else return createGroupWithJoker(meld.asCards());
	}
	
	/**
	 * meldの中に一枚でもcardsの中のカードが含まれていればtrue
	 * @param meld
	 * @param cards
	 * @return
	 */
	public static boolean contain(Meld meld, Cards cards) {
		for(Card card : cards) {
			if(meld.asCards().contains(card)) return true;
		}
		return false;
	}
	
	public static boolean contains(Meld meld, Card card) {
		if(meld.type() == PASS) return false;
		else return meld.asCards().contains(card);
	}
	
	/**
	 * meldの中に8が含まれているか返す
	 * jokerが8としてふるまっていたらtrueを返す
	 * @param meld
	 * @return
	 */
	public static boolean containEight(Meld meld) {
		if(meld.type() == Meld.Type.SEQUENCE && 
				meld.asCards().contains(Card.JOKER)) {
			Rank rank = meld.rank();
			if(rank.equals(Rank.EIGHT)) return true;
			for(int i = 1; i < meld.asCards().size(); i++) {
				rank = RankUtility.getNextRank(rank);
				if(rank.equals(Rank.EIGHT))
					return true;
			}
		} else {
			for(Card c : meld.asCards()) {
				if(c.equals(Card.JOKER)) continue;
				if(c.rank().equals(Rank.EIGHT))
					return true;
			}
		} 
		return false;
	}
	
	/**
	 * 一枚でもlowなRankのカードが入っているかを判定
	 * @param meld
	 * @param place
	 * @return
	 */
	public static boolean containsLowCard(Meld meld, Place place) {
		for(Card card : meld.asCards()) {
			if(card.equals(Card.JOKER)) continue;
			if(RankUtility.useLowRank(card.rank(), place))
				return true;
		}
		return false;
	}

	public static boolean containsLowMiddle(Meld meld, Place place) {
		for(Card card : meld.asCards()) {
			if(card.equals(Card.JOKER)) continue;
			if(RankUtility.useMiddleRank(card.rank(), place))
				return true;
		}
		return false;
	}
	
	public static boolean containsHighCard(Meld meld, Place place) {
		for(Card card : meld.asCards()) {
			if(card.equals(Card.JOKER)) continue;
			if(RankUtility.useHighRank(card.rank(), place))
				return true;
		}
		return false;
	}
	
	public static boolean containsRankCard(Meld meld, Rank rank) {
		for(Card card : meld.asCards()) {
			if(card.equals(Card.JOKER)) continue;
			if(card.rank().equals(rank)) return true;
		}
		return false;
	}
	
	/**
	 * targetのMeldを分割して,場に出すことが可能であれば，
	 * 分割したMeldを返す．
	 * 存在しない場合nullを返す．
	 * 
	 * 戦略
	 * ・縛れるのであれば，縛る
	 * ・もっとも弱いカードから構成される組み合わせを返す．
	 * 
	 * このメソッドにはJokerを含んだmeldを渡さないようにしよう！
	 * 革命時は実行しない・・・
	 * 
	 * @param target
	 * @param place
	 * @return
	 */
	public static List<Meld> extracteMeldWithoutJoker(Meld target, Place place) {
		// targetはgroupまたはsequenceであり，場よりも枚数が多い
		if(place.lastMeld().asCards().size() >= target.asCards().size())
			return new LinkedList<Meld>();
		
		Meld lastMeld = place.lastMeld();
		Cards cards = target.asCards();
		int size = lastMeld.asCards().size();
		boolean hasJoker = target.asCards().contains(Card.JOKER);
		
		if(place.type() == SEQUENCE) {
			if(target.type() != SEQUENCE) return new LinkedList<Meld>();
			if(!place.lockedSuits().equals(Suits.EMPTY_SUITS) && 
					!place.lockedSuits().equals(target.suits()))
				return new LinkedList<Meld>();
			List<Meld> melds = Utility.getAcceptMelds(
					MeldsUtility.createSequenceAllMelds(cards, size), place);
			return melds;
		} else if(place.type() == GROUP) {
			if(target.type() == SEQUENCE) return new LinkedList<Meld>();
			if(place.order() == Order.NORMAL && 
					lastMeld.rank().compareTo(target.rank()) >= 0)
				return new LinkedList<Meld>();
			if(place.order() == Order.REVERSED && 
					lastMeld.rank().compareTo(target.rank()) <= 0)
				return new LinkedList<Meld>();
			
			List<Meld> melds = Utility.getAcceptMelds(
					MeldsUtility.createGroupAllMelds(cards, size),place);
			
			return melds;
		} else {
			List<Meld> melds = Utility.getAcceptMelds(
					MeldsUtility.createSingleMeldsNotWithJoker(cards), place);
			
			if(hasJoker) {
				melds.add(getSingleMeldWithOnlyJoker(place));
			}
			return melds;
		}
	}
	
//	/**
//	 * targetのMeldから, rankで始まり，size個のSequenceを生成する
//	 * @param target
//	 * @param start
//	 * @param size
//	 * @return
//	 */
//	private static Meld createSequenceMeld(Meld target, Rank rank, int size) {
//		Cards cards = Cards.sort(target.asCards());
////		boolean hasJoker = cards.contains(Card.JOKER);
//		Card[] list = new Card[size];
//		Rank now = rank;
//		for(int i = 0; i < size; i++) {
//			for(Card c : cards) {
//				if(c.equals(Card.JOKER)) continue;
//				if(c.rank().equals(now)) {
//					list[i] = c; break;
//				}
//			}
//			if(list[i] == null) list[i] = Card.JOKER;
//			now = RankUtility.getNextRank(now);
//		}
//		Cards tmpCards = CardsUtility.createCards(list);
//		return MeldUtility.createSequence(tmpCards, rank);
//	}
	
	/**
	 * targetのMeldをJokerを組み合わせて分割し,場に出すことが可能であれば，
	 * 分割したMeldを返す．
	 * 存在しない場合nullを返す．
	 * 
	 * 戦略
	 * ・縛れるのであれば，縛る
	 * ・もっとも弱いカードから構成される組み合わせを返す．
	 * 
	 * @param target
	 * @param place
	 * @return
	 */
//	public static Meld 出せるMeldの抽出withJoker(Meld target, Place place) {
//		
//	}
	
	/**
	 * meldをjokerを含めて連続の順にソートする
	 * @param meld
	 * @return
	 */
	public static Cards getSequenceMeldSortCard(Meld meld) {
		Cards cards = meld.asCards();
		Rank rank = meld.rank();
		Suit suit = meld.suits().get(0);
		Card now = Card.valueOf(suit, rank);
		int size = meld.asCards().size();
		Card[] list = new Card[size];
		for(int i = 0; i < size; i++) {
			if(cards.contains(now)) {
				list[i] = now;
			} else {
				list[i] = Card.JOKER;
			}
			now = CardUtility.getNext(now);
		}
		return CardsUtility.createCards(list);
	}
	
	/**
	 * placeに従って，jokerの一枚出しの役を生成する
	 * @param place
	 * @return
	 */
	public static Meld getSingleMeldWithOnlyJoker(Place place) {
		Suit suit = place.lastMeld() != null && place.lastMeld().suits() != null? 
				place.lastMeld().suits().get(0) : Suit.SPADES;
		if(place.order() == Order.NORMAL) {
			return MeldFactory.createSingleMeldJoker(suit, Rank.JOKER_HIGHEST);
		} else {
			return MeldFactory.createSingleMeldJoker(suit, Rank.JOKER_LOWEST);
		}
	}
	
	/**
	 * meldの評価用メソッド
	 * @param meld
	 * @return
	 */
	public static boolean isHighMeld(Meld meld) {
		// 1,2,K,8が含まれているかどうか
		// J以上のSequenceか
		// その上に出せるカードが存在しないか
		if(MeldUtility.containsRankCard(meld, Rank.TWO))
			return true;
		if(MeldUtility.containsRankCard(meld, Rank.ACE))
			return true;
		if(MeldUtility.containsRankCard(meld, Rank.KING))
			return true;
		if(MeldUtility.containsRankCard(meld, Rank.EIGHT))
			return true;
		if(meld.type() == Meld.Type.SEQUENCE) {
			if(meld.rank().compareTo(Rank.TEN) > 0)
				return true;
		}
		return false;
	}
	
	public static boolean isLowMeld(Meld meld) {
		if(MeldUtility.containsRankCard(meld, Rank.THREE))
			return true;
		if(MeldUtility.containsRankCard(meld, Rank.FOUR))
			return true;
		if(MeldUtility.containsRankCard(meld, Rank.FIVE))
			return true;
		if(MeldUtility.containsRankCard(meld, Rank.SIX))
			return true;
		return false;
	}
	
	public static boolean isMiddleMeld(Meld meld) {
		if(!isHighMeld(meld) && !isLowMeld(meld))
			return true;
		return false;
	}
	
	public static Meld createSingleJoker(Place place) {
		if(place.isReverse())
			return MeldFactory.createSingleMeldJoker(
					place.isRenew() ? Suit.SPADES : 
						place.lastMeld().asCards().get(0).suit(), 
						Rank.JOKER_LOWEST);
		else
			return MeldFactory.createSingleMeldJoker(
					place.isRenew() ? Suit.SPADES : 
						place.lastMeld().asCards().get(0).suit(), 
						Rank.JOKER_HIGHEST);
	}
}
