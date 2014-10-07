package jp.ac.hosei.daihinmin.HiroshiIwasaki;

import jp.ac.uec.daihinmin.Rules;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class TeruteruTradeCard {

	public static Cards tradeCard(BotSkeleton bot) {

		Cards result = Cards.EMPTY_CARDS;
		Cards cards = Cards.sort(bot.hand());
		Cards testCards = Cards.sort(bot.hand());
		
		// D3があったら除外
		if (cards.contains(Card.D3)) {
			cards = cards.remove(Card.D3);
		}

		// ８があったら除外
		if (containsEight(cards)) {
			cards = removeEight(cards);
		}
		
		// Jokerがあったら除外
		if (cards.contains(Card.JOKER)) {
			cards = cards.remove(Card.JOKER);
		}

		// JokerとS3を両方持っているなら、S3は除外
		if (cards.contains(Card.JOKER, Card.S3)) {
			cards = cards.remove(Card.S3);
		}
		
		// 階段があったら除外
		cards = cards.remove(Melds.project(Melds.parseSequenceMelds(cards)));

		int tradeCardNum = Rules.sizeGivenCards(bot.rules(), bot.rank());

		testCards = cards;

		// ペアがあったら除外
		testCards = testCards.remove(Melds.project(Melds
				.parseGroupMelds(testCards)));

		// ペアを除外して、交換に必要な枚数に足りないときはペア除外をやめる
		if (testCards.size() >= tradeCardNum) {
			cards = testCards;
		}

		if (Rules.heiminRank(bot.rules()) > bot.rank()) {
			if(cards.size()>=tradeCardNum){
				for (int i = 0; i < tradeCardNum; i++) {
					result = result.add(cards.get(i));
				}
			}else{
				for (int i = 0; i < tradeCardNum; i++) {
					result = result.add(bot.hand().get(i));
				}
			}
		} else {
			for (int i = 0; i < tradeCardNum; i++) {
				result = result.add(bot.hand().get(cards.size()-1-i));
			}
		}

		return result;
	}

	public static boolean containsEight(Cards cards) {
		// Cards...カード集合を指定すると、８を含むか判断
		if (cards.contains(Card.C8) || cards.contains(Card.S8)
				|| cards.contains(Card.D8) || cards.contains(Card.H8)) {
			return true;
		} else
			return false;
	}

	public static boolean containsJoker(Cards cards) {
		// Cards...カード集合を指定すると、JOKERを含むか判断
		if (cards.contains(Card.JOKER)) {
			return true;
		} else
			return false;
	}

	public static boolean containsEight(Melds melds) {
		// Melds...役集合を指定すると、８を含むか判断
		boolean frag = false;
		for (Meld oneMeld : melds) {
			if (containsEight(oneMeld.asCards())) {
				frag = true;
				return frag;
			}
		}
		return frag;
	}

	public static boolean containsJoker(Melds melds) {
		// Melds...役集合を指定すると、JOKERを含むか判断
		boolean frag = false;
		for (Meld oneMeld : melds) {
			if (containsJoker(oneMeld.asCards())) {
				frag = true;
				return frag;
			}
		}
		return frag;
	}

	public static Cards removeEight(Cards cards) {
		Cards removeHand = cards;

		if (removeHand.contains(Card.C8)) {
			removeHand = cards.remove(Card.C8);
		}
		if (removeHand.contains(Card.H8)) {
			removeHand = removeHand.remove(Card.H8);
		}
		if (removeHand.contains(Card.D8)) {
			removeHand = removeHand.remove(Card.D8);
		}
		if (removeHand.contains(Card.S8)) {
			removeHand = removeHand.remove(Card.S8);
		}
		return removeHand;
	}
}
