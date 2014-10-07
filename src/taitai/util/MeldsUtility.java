package taitai.util;

import java.util.*;

import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;

public class MeldsUtility {
	
	/**
	 * 一枚組のカードを出す．
	 * @param cards
	 * @return
	 */
	public static Melds createSingleMeldsNotWithJoker(Cards cards) {
		Melds melds = Melds.EMPTY_MELDS;
		for(Card c : cards) {
			if(c.equals(Card.JOKER)) continue;
			melds = melds.add(MeldFactory.createSingleMeld(c));
		}
		return melds;
	}
	
	
	/**
	 * cardsをもとに，size枚のGroup出しが可能な組み合わせを全て生成する
	 * @param cards
	 * @param size
	 * @return
	 */
	public static Melds createGroupAllMelds(Cards cards, int size) {
		Melds melds = Melds.EMPTY_MELDS;
		if(size < 2 || 5 < size) return melds;
		boolean hasJoker = cards.contains(Card.JOKER);
		for(Rank rank : RankUtility.rankList) {
			Cards cs = CardsUtility.and(cards, rank);
			if(size <= cs.size()) {
				for(Cards c : getCombinationDFS(new LinkedList<Cards>(), 
						cs, Cards.EMPTY_CARDS, size, 0)) {
					melds = melds.add(MeldFactory.createGroupMeld(c));
				}
			}
			if(hasJoker && size - 1 <= cs.size()) {
				if(size == 5) {
					melds = melds.add(MeldFactory.createGroupMeldOnlyWithJokers(
							cs.add(Card.JOKER), SuitsUtility.allSuits,rank));
				} else {
					for(Cards c : getCombinationDFS(new LinkedList<Cards>(), 
							cs, Cards.EMPTY_CARDS, size - 1, 0)) {
						for(Suit suit : SuitsUtility.and(
								SuitsUtility.allSuits, SuitsUtility.not(c.suits()))) {
//							System.out.println(c+" : "+suit+" "+cs);
							melds = melds.add(
									MeldFactory.createGroupMeldOnlyWithJokers(
											c.add(Card.JOKER), c.suits().add(suit),rank));
						}
					}
				}
			}
		}
		return melds;
	}
	
	static List<Cards> getCombinationDFS(
			List<Cards> result, Cards source, Cards cards, int size, int index) {
		if(size == cards.size()) { 
			result.add(cards);
			return result;
		}
		for(int i = index; i < source.size(); i++)
			getCombinationDFS(result, source, cards.add(source.get(i)), size, i+1);
		
		return result;
	}
	
	/**
	 * 指定したcardsをもとに，size枚の連続の組み合わせ全てを生成する
	 * 
	 * @param cards
	 * @param size 連続の枚数
	 * @return
	 */
	public static Melds createSequenceAllMelds(Cards cards, int size) {
		boolean hasJoker = cards.contains(Card.JOKER);
		Melds melds = Melds.EMPTY_MELDS;
		for(Card c : cards) {
			if(c.equals(Card.JOKER)) continue;
			melds = melds.add(createSequenceAllMeldDFS(
					cards,c, size, 1, Cards.EMPTY_CARDS.add(c), hasJoker, c.rank()));
			if(hasJoker && !c.rank().equals(Rank.TWO)) {
				Rank before = RankUtility.getBeforeRank(c);
				if(before != null) {
					Cards cs = Cards.EMPTY_CARDS.add(Card.JOKER);
					melds = melds.add(createSequenceAllMeldDFS(
							cards, c, size, 2, cs.add(c), false, before));
				}
			}
		}
		return melds;
	}
	
	private static Melds createSequenceAllMeldDFS(
			Cards source, Card now, int size, int deep, 
			Cards cards, boolean joker, Rank rank) {
		if(size == deep) {
			if(rank == null)
				return Melds.EMPTY_MELDS.add(MeldFactory.createSequenceMeld(cards));
			else 
				return Melds.EMPTY_MELDS.add(
						MeldFactory.createSequenceMeldWithJokers(cards, rank));
		}
		Card next = CardUtility.getNext(now);
		if(next == null) return Melds.EMPTY_MELDS;
		Melds melds = Melds.EMPTY_MELDS;
		if(source.contains(next)) {
			melds = melds.add(createSequenceAllMeldDFS(source, next, size, 
					deep+1, cards.add(next), joker, rank));
		}
		if(joker) {
			melds = melds.add(createSequenceAllMeldDFS(source, next, size, deep+1, 
					cards.add(Card.JOKER), false, rank));
		}
		return melds;
	}
	
	private static Comparator<Meld> meldComparator = new Comparator<Meld>(){
		public int compare(Meld o1, Meld o2) {
			return o1.rank().compareTo(o2.rank()) != 0 ? 
					o1.rank().compareTo(o2.rank()) :
						o1.asCards().size() - o2.asCards().size();
	}};
	
//	private static Comparator<Meld> meldLowerSortComparator = new Comparator<Meld>(){
//		public int compare(Meld o1, Meld o2) {
//			return o2.rank().compareTo(o1.rank()) != 0 ? 
//					o2.rank().compareTo(o1.rank()) :
//						o1.asCards().size() - o2.asCards().size();
//	}};
	
	public static Melds sort(Melds melds) {
		return Melds.sort(melds, meldComparator);
	}
	
	public static void sort(List<Meld> melds) {
		Collections.sort(melds, meldComparator);
	}
	
	/**
	 * meldが高い値のカード(通常:K,A,2 革命:3,4,5)を使っているかどうか判定
	 * jokerはこの強さの評価に入れない．
	 * @param meld
	 * @param isReverse
	 * @return
	 */
	public static boolean isHighCardUse(Meld meld, boolean isReverse) {
		for(Card c : meld.asCards()) {
			if(c.equals(Card.JOKER)) continue;
			if(isReverse) {
				if(c.rank().compareTo(Rank.QUEEN) > 0) return true;
			} else {
				if(c.rank().compareTo(Rank.SIX) < 0) return true;
			}
		}
		return false;
	}
	
	/**
	 * cardsから作れる4枚または3枚のSequenceのMeldの集合を生成する
	 * Queen以上のカードから始まるSequenceは生成しない
	 * @return 残ったカード
	 */
	public static Melds createSequenses(Cards cards) {
		Cards sortCards = Cards.sort(cards, new Comparator<Card>(){
			public int compare(Card o1, Card o2) {
				if(o1.equals(Card.JOKER)) return 1;
				if(o2.equals(Card.JOKER)) return -1;
				return o1.rank().compareTo(o2.rank());
			}});
		Melds melds = Melds.EMPTY_MELDS;
		if(sortCards.contains(Card.JOKER)) {
			Cards tmpCards = sortCards.remove(Card.JOKER);
			melds = createSequencesSubMethod(tmpCards,4,3);
			sortCards = CardsUtility.remove(sortCards, melds);
		}
		
		return melds.add(createSequencesSubMethod(sortCards, 4, 4));
	}
	
	private static Melds createSequencesSubMethod(Cards cards, int start, int end) {
		Melds melds = Melds.EMPTY_MELDS;
		
		// 連続は4枚または3枚組を構成する
		for(int i = start; i >= end; i--) {
			Melds tmpMelds = setSequencesDFS(i, cards);
			melds = melds.add(tmpMelds);
			cards = CardsUtility.remove(cards, tmpMelds);
		}
		return melds;
	}
	
	public static Melds createGroup(Cards cards) {
		Melds melds = Melds.EMPTY_MELDS;
		Cards sortCards = Cards.sort(cards, new Comparator<Card>(){
			public int compare(Card o1, Card o2) {
				return o2.compareTo(o1);
			}});
		if(sortCards.contains(Card.JOKER)) {
			sortCards = sortCards.remove(Card.JOKER);
		}
		Iterator<Card> iterator = sortCards.iterator();
		if(!iterator.hasNext()) return melds;
		Cards list = Cards.EMPTY_CARDS.add(iterator.next());
		while(iterator.hasNext()) {
			Card now = iterator.next();
			if(now.rank() == list.get(0).rank())
				list = list.add(now);
			else {
				melds = melds.add(createMeld(list));
				list = Cards.EMPTY_CARDS.add(now);
			}
		}
		return melds.add(createMeld(list));
	}
	
	
	private static Melds setSequencesDFS(int size, Cards cards) {
		Melds melds = Melds.EMPTY_MELDS;

		Card[] list = new Card[size];
		a : for(Card card : cards) {
			if(card.equals(Card.JOKER)) continue;
			if(card.rank().compareTo(Rank.JACK) > 0) break;
			
			
			Card now = card;
			list[0] = now;
			int jokerUseCount = 0;
			for(int i = 1; i < size; i++) {
				Card next = CardUtility.getNext(now);
				if(next == null) continue a;
				if(!cards.contains(next)) {
					if(cards.contains(Card.JOKER)) {
						list[i] = Card.JOKER;
						jokerUseCount++;
					} else {
						continue a;
					}
				} else {
					list[i] = next;
				}
				now = next;
			}
			
			if(jokerUseCount > 1) continue;

			Cards tmpCards = CardsUtility.createCards(list);
			
			Meld meld = MeldUtility.createSequence(tmpCards, card.rank());
			melds = melds.add(meld);
			return melds.add(setSequencesDFS(size, cards.remove(tmpCards)));
		}
		return melds;
	}
	
	private static Meld createMeld(Cards cards) {
		Meld meld;
		if(cards.size() != 1)
			meld = MeldFactory.createGroupMeld(cards);
		else meld = MeldFactory.createSingleMeld(cards.get(0));
		return meld;
	}
	
	/**
	 * meldsからJokerを使った役を除いた役のリストを返す．
	 * @param melds
	 * @return
	 */
	public static List<Meld> removeJokerUseMeld(List<Meld> melds) {
		List<Meld> result = new LinkedList<Meld>();
		for(Meld meld : melds) {
			if(!meld.asCards().contains(Card.JOKER))
				result.add(meld);
		}
		return result;
	}
	
	public static List<Meld> removeLowCardsUseMeld(List<Meld> melds, Place place) {
		List<Meld> result = new LinkedList<Meld>();
		a:for(Meld meld : melds) {
			for(Card card : meld.asCards()) {
				if(card.equals(Card.JOKER)) continue;
				if(RankUtility.useLowRank(card.rank(), place))
					continue a;
			}
			result.add(meld);
		}
		return result;
	}
	
	public static List<Meld> leastLowerMeld(List<Meld> melds, Place place) {
		List<Meld> result = new LinkedList<Meld>();
		Rank rank;
		if(place.order() == Order.NORMAL) {
			rank = melds.get(0).rank();
		} else {
			rank = melds.get(melds.size() - 1).rank();
		}

		for(Meld meld : melds) {
			if(meld.rank().equals(rank)) {
				result.add(meld);
			}
		}
		return result;
	}
	
	public static Melds getReverseMelds(Cards cards) {
		Melds melds = Melds.parseMelds(cards);
		melds = Melds.sort(melds, new Comparator<Meld>(){
			public int compare(Meld o1, Meld o2) {
				return o2.asCards().size() - o1.asCards().size();
			}});
		Melds result = Melds.EMPTY_MELDS;
		for(Meld meld : melds) {
			if(meld.type() == Meld.Type.SEQUENCE) {
				if(meld.asCards().size() >= 5)
					result = result.add(meld);
			} else {
				if(meld.asCards().size() >= 4)
					result = result.add(meld);
			}
		}
		return result;
	}
}
