package taitai;

import jp.ac.uec.daihinmin.Order;
import jp.ac.uec.daihinmin.card.*;
/**
 * 通常時
 * high : 2,A,K,Q
 * middle : J,10,9,7
 * low : 6,5,4,3
 * 
 * 革命時
 * high : 3,4,5,6
 * middle : 7,9,10,J
 * low : Q,K,A,2
 * @author tai
 *
 */
public class CardsOriginal {
	private Cards high = Cards.EMPTY_CARDS;
	private Cards middle = Cards.EMPTY_CARDS;
	private Cards low = Cards.EMPTY_CARDS;
	private Cards eight = Cards.EMPTY_CARDS;
	private boolean joker;
	private final Order order;
	
	private final Rank[] normalHigh = {Rank.TWO, Rank.ACE, Rank.KING, Rank.QUEEN};
	private final Rank[] normalMiddle = {Rank.JACK, Rank.TEN, Rank.NINE, Rank.SEVEN};
	private final Rank[] normalLow = {Rank.SIX, Rank.FIVE, Rank.FOUR, Rank.THREE};
	private final Rank[] reverseHigh = normalLow;
	private final Rank[] reverseMiddle = normalMiddle;
	private final Rank[] reverseLow = normalHigh;
	
	public CardsOriginal(Cards cards, boolean isReverse) {
		if(isReverse) {
			order = Order.REVERSED;
		} else {
			order = Order.NORMAL;
		}
		add(cards);
	}
	
	public void add(Card card) {
		if(card.equals(Card.JOKER))
			joker = true;
		else if(card.rank().equals(Rank.EIGHT))
			eight = eight.add(card);
		else if(isHighCard(card))
			high = high.add(card);
		else if(isMiddleCard(card))
			middle = middle.add(card);
		else
			low = low.add(card);
	}
	
	public void add(Cards cards) {
		for(Card card : cards) {
			add(card);
		}
	}
	
	public void remove(Card card) {
		if(card.equals(Card.JOKER))
			joker = false;
		else if(card.rank().equals(Rank.EIGHT))
			eight = eight.remove(card);
		else if(isHighCard(card))
			high = high.remove(card);
		else if(isMiddleCard(card))
			middle = middle.remove(card);
		else
			low = low.remove(card);
	}
	
	public void remove(Cards cards) {
		for(Card card : cards) {
			remove(card);
		}
	}
	
	public boolean contains(Card card) {
		if(card.equals(Card.JOKER))
			return joker;
		return high.contains(card) || middle.contains(card) || 
			low.contains(card) || eight.contains(card);
	}
	
	/**
	 * cardsのカードが一枚でも含まれているか否か
	 * @param cards
	 * @return
	 */
	public boolean contains(Cards cards) {
		for(Card card : cards) {
			if(contains(card)) return true;
		}
		return false;
	}
	
	/**
	 * cardsのすべてのカードが含まれているか否か
	 * @param cards
	 * @return
	 */
	public boolean containsAll(Cards cards) {
		for(Card card : cards) {
			if(!contains(card)) return false;
		}
		return true;
	}
	
	public boolean containsHigh(Card card) {
		return high.contains(card);
	}
	
	public boolean containsMiddle(Card card) {
		return middle.contains(card);
	}
	
	public boolean containsLow(Card card) {
		return low.contains(card);
	}
	
	public boolean contaisEight(Card card) {
		return eight.contains(card);
	}
	
	public Order getOrder() {
		return order;
	}
	
	public boolean isHighCard(Card card) {
		if(order == Order.NORMAL) {
			return isRankCard(card, normalHigh);
		} else {
			return isRankCard(card, reverseHigh);
		}
	}
	
	public boolean isMiddleCard(Card card) {
		if(order == Order.NORMAL) {
			return isRankCard(card, normalMiddle);
		} else {
			return isRankCard(card, reverseMiddle);
		}
	}
	
	public boolean isLowCard(Card card) {
		if(order == Order.NORMAL) {
			return isRankCard(card, normalLow);
		} else {
			return isRankCard(card, reverseLow);
		}
	}
	
	private boolean isRankCard(Card card, Rank[] ranks) {
		for(Rank rank : ranks)
			if(rank.equals(card.rank()))
				return true;
		return false;
	}
}
