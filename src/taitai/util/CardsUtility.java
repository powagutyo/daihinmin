package taitai.util;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suit;

public class CardsUtility {
	public static String[] suites = {"S","H","C","D"};
	public static String[] ranks = {"3","4","5","6","7","8","9","10","J","Q","K","A","2"};
	public static String joker = "JOKER";
	public static String all;
	public static Cards getAllCards() {
		if(all == null) {
			StringBuilder sb = new StringBuilder();
			for(String rank : ranks)
				for(String suite : suites)
					sb.append(suite+rank+" ");
			sb.append(joker);
			all = sb.toString();
		}
		return Cards.valueOf(all);
	}
	
	/**
	 * aとbの論理積となるCardsを返す．
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Cards and(Cards a, Cards b) {
		Cards ans = Cards.EMPTY_CARDS;
		Cards d = a.size() < b.size() ? a : b;
		Cards e = a.size() < b.size() ? b : a;
		for(Card c : d) {
			if(e.contains(c)) ans = ans.add(c);
		}
		return ans;
	}
	
	/**
	 * cardsの中からrankと一致するrankのみから構成されるcardsを返す．
	 * @param cards
	 * @param rank
	 * @return
	 */
	public static Cards and(Cards cards, Rank rank) {
		Cards results = Cards.EMPTY_CARDS;
		for(Suit suit : SuitUtility.suiteList) {
			Card c = Card.valueOf(suit, rank);
			if(cards.contains(c)) results = results.add(c);
		}
		return results;
	}
	
	/**
	 * cardsの中からsuitと一致するrankのみから構成されるcardsを返す．
	 * @param cards
	 * @param suit
	 * @return
	 */
	public static Cards and(Cards cards, Suit suit) {
		Cards results = Cards.EMPTY_CARDS;
		for(Rank rank : RankUtility.rankList) {
			Card c = Card.valueOf(suit, rank);
			if(cards.contains(c)) results = results.add(c);
		}
		return results;
	}
	
	public static Cards createCards(Card[] cards) {
		Cards ans = Cards.EMPTY_CARDS;
		for(Card c : cards)
			ans = ans.add(c);
		return ans;
	}
	
	/**
	 * cardsの中からranksのカードを削除したcardsを返す
	 * @param cards
	 * @param ranks
	 * @return
	 */
	public static Cards remove(Cards cards, Rank rank) {
		String rankString = rank.toString();
		for(String suite : suites)
			cards = cards.remove(Card.valueOf(suite+rankString));
		return cards;
	}
	
	/**
	 * aからbのカードを削除する
	 * @param a
	 * @param b
	 * @return
	 */
	public static Cards remove(Cards a, Cards b) {
		Cards result = Cards.EMPTY_CARDS;
		for(Card c : a) {
			if(!b.contains(c)) {
				result = result.add(c);
			}
		}
		return result;
	}
	
	/**
	 * aからbが構成しているカードを削除する
	 * @param a
	 * @param b
	 * @return
	 */
	public static Cards remove(Cards a, Melds b) {
		Cards result = a;
		for(Meld meld : b) {
			result = result.remove(meld.asCards());
		}
		return result;
	}
	
	public static boolean containsAll(Cards a, Cards b) {
		for(Card c : b) {
			if(!a.contains(c)) return false;
		}
		return true;
	}
}
