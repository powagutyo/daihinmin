package taitai.util;

import java.util.*;

import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;

public class Utility {
	
	public static boolean isHighCardUse(Cards cards) {
		for(int i = 10; i < 13; i++)
			for(int j = 0; j < 4; j++)
				if(cards.contains(CardUtility.getCard(j,i)))
					return true;
		return false;
	}
	
	public static boolean isLockedSuits(Place place, Meld meld) {
		return isLockedSuits(place.lastMeld(), meld);
	}
	
	public static boolean isLockedSuits(Meld lastMeld, Meld meld) {
		if(lastMeld.asCards().size() != meld.asCards().size())
			return false;
		if(lastMeld.type() != meld.type()) 
			return false;
		Cards lastMeldCards = Cards.sort(lastMeld.asCards());
		Cards meldCards = Cards.sort(meld.asCards());
		if(meld.type() == Meld.Type.SINGLE && meld.asCards().contains(Card.JOKER))
			return true;
		if(lastMeld.type() == Meld.Type.SEQUENCE) {
			return lastMeldCards.get(0).suit() == meldCards.get(0).suit();
		} else if(lastMeld.type() == Meld.Type.SINGLE) {
			if(lastMeldCards.contains(Card.JOKER) || meldCards.contains(Card.JOKER))
				return false;
			return lastMeldCards.get(0).suit() == meldCards.get(0).suit();
		} else {
			return lastMeldCards.suits().contains(meldCards.suits());
		}
	}
	

	/**
	 * meldsの中からrulesに従って，placeに出せないカードを除去する
	 * @param place
	 * @param melds
	 * @param rules
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Melds removeNotAcceptMelds(Place place, Melds melds, Rules rules) 
	throws IllegalArgumentException {
		
		// 場が縛られていれば
		if (!place.lockedSuits().equals(Suits.EMPTY_SUITS)) {
			// 場を縛っているスート集合に適合する役を抽出して, 候補とする．
			melds = melds.extract(Melds.suitsOf(place.lockedSuits()));
		}
		
		// next_rank := 場に出されている役のランクの, 直ぐ上のランク
		Rank  next_rank = RankUtility.getNextRank(place, rules);
		
		// 場に出されている役の, タイプ, 枚数, ランク, 革命中か否か, に合わせて,
		// 「出すことができる」候補に絞る．
		melds = melds.extract(Melds.typeOf(place.type())
							.and( Melds.sizeOf(place.size())
									.and( Melds.rankOf(next_rank)
											.or(place.order() == Order.NORMAL ? 
													Melds.rankOver(next_rank): 
													Melds.rankUnder(next_rank)))));
		return melds;
	}

	
	/**
	 * placeに従って，場に出すことができるMeldのListを返す．
	 * @param melds
	 * @param place
	 * @return
	 */
	public static List<Meld> getAcceptMelds(Melds melds, Place place) {
		return getAcceptMelds(melds, place.lastMeld(), 
				place.isReverse(), place.lockedSuits());
	}
	
	public static List<Meld> getAcceptMelds(List<Meld> melds, Place place) {
		return getAcceptMelds(melds, place.lastMeld(), 
				place.isReverse(), place.lockedSuits());
	}
	
	public static List<Meld> getAcceptMelds(Melds melds, 
			Meld lastMeld, boolean isReverse, Suits lockedSuits) {
		List<Meld> result = new LinkedList<Meld>();
		if(melds != null) for(Meld meld : melds) {
			if(isAcceptMeld(meld, lastMeld, isReverse, lockedSuits)) {
				result.add(meld);
			}
		}
		return result;
	}
	
	public static List<Meld> getAcceptMelds(List<Meld> melds, 
			Meld lastMeld, boolean isReverse, Suits lockedSuits) {
		List<Meld> result = new LinkedList<Meld>();
		if(melds != null) for(Meld meld : melds) {
			if(isAcceptMeld(meld, lastMeld, isReverse, lockedSuits)) {
				result.add(meld);
			}
		}
		return result;
	}
	
	/**
	 * rulesとplaceに従って，meldが場に出すことが可能かを返す．
	 * @param meld
	 * @param place
	 * @param rules
	 * @return
	 */
	public static boolean isAcceptMeld(Meld meld, 
			Meld lastMeld, boolean isReverse, Suits lockedSuits) {
		// 場に何もカードが出ていなければ，何でも出せる！
		if(lastMeld == null) {
			return true;
		}
		
		// 役が異なっている，またはカードの枚数が異なっているなら，出せない
		if(!lastMeld.type().equals(meld.type()) || 
				lastMeld.asCards().size() != meld.asCards().size()) {
			return false;
		}
		
		// 縛りが発生中で，同じスートでないなら，出せない
		if(!lockedSuits.equals(Suits.EMPTY_SUITS)) {
			if(!lastMeld.suits().equals(meld.suits())) {
				return false;
			}
		}
		
		// TODO
		if(lastMeld.type() != Meld.Type.SEQUENCE) {
			if(!isReverse) {
				return lastMeld.rank().compareTo(meld.rank()) < 0;	
			} else {
				// 革命中
				return meld.rank().compareTo(lastMeld.rank()) < 0;
			}
		} else {
			Rank rank = lastMeld.rank();
			for(int i = 0; i < lastMeld.asCards().size() && rank != null; i++) {
				if(!isReverse)
					rank = RankUtility.getNextRank(rank);
				else 
					rank = RankUtility.getBeforeRank(rank);
			}
			if(rank == null)
				return false;
			if(!isReverse)
				return rank.compareTo(meld.rank()) <= 0;
			else {
//				if(rank.compareTo(meld.rank()) >= 0) {
//					System.out.println(meld+" / "+lastMeld);
//				}
				return rank.compareTo(meld.rank()) >= 0;
			}
		}
	}

	public static boolean isAcceptMeld(Meld meld, Place place) {
		return isAcceptMeld(meld, place.lastMeld(), 
				place.isReverse(), place.lockedSuits());
	}
}
