package jp.ac.hosei.daihinmin.HiroshiIwasaki3;
import javax.swing.JOptionPane;

import jp.ac.uec.daihinmin.Rules;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class TeruteruTradeCard {

	public static Cards tradeCard(BotSkeleton bot) {
		
		int tradeCardNum = Rules.sizeGivenCards(bot.rules(), bot.rank());

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
		
		// JokerとS3を両方持っているなら、S3は除外
		if (cards.contains(Card.JOKER, Card.S3)) {
			cards = cards.remove(Card.S3);
		}
		
		//もし、2のカードを持っているとき、2のカードは全て除外
		if (containsTwo(cards)) {
			cards = removeTwo(cards);
		}
		
		//もし、Aceのカードを持っているとき、Aceのカードは全て除外
		if (containsAce(cards)) {
			cards = removeAce(cards);
		}
		
		//もし、Kingのカードを持っているとき、Kingのカードは全て除外
//		if (containsKing(cards)) {
//			cards = removeKing(cards);
//		}
		
		// 階段があったら除外
		cards = cards.remove(Melds.project(Melds.parseSequenceMelds(cards)));

		testCards = cards;

		// ペアがあったら除外
		testCards = testCards.remove(Melds.project(Melds
				.parseGroupMelds(testCards)));

		// ペアを除外して、交換に必要な枚数に足りないときはペア除外をやめる
		if (testCards.size() >= tradeCardNum) {
			cards = testCards;
		}

		//このルールではheininRankは３、大富豪は１、富豪は２、・・・
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
	
	public static boolean containsTwo(Cards cards) {
		// Cards...カード集合を指定すると、８を含むか判断
		if (cards.contains(Card.C2) || cards.contains(Card.S2)
				|| cards.contains(Card.D2) || cards.contains(Card.H2)) {
			return true;
		} else
			return false;
	}
	
	public static boolean containsAce(Cards cards) {
		// Cards...カード集合を指定すると、８を含むか判断
		if (cards.contains(Card.CA) || cards.contains(Card.SA)
				|| cards.contains(Card.DA) || cards.contains(Card.HA)) {
			return true;
		} else
			return false;
	}
	
	public static boolean containsKing(Cards cards) {
		// Cards...カード集合を指定すると、８を含むか判断
		if (cards.contains(Card.CK) || cards.contains(Card.SK)
				|| cards.contains(Card.DK) || cards.contains(Card.HK)) {
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
	
	public static Cards removeTwo(Cards cards) {
		Cards removeHand = cards;

		if (removeHand.contains(Card.C2)) {
			removeHand = cards.remove(Card.C2);
		}
		if (removeHand.contains(Card.H2)) {
			removeHand = removeHand.remove(Card.H2);
		}
		if (removeHand.contains(Card.D2)) {
			removeHand = removeHand.remove(Card.D2);
		}
		if (removeHand.contains(Card.S2)) {
			removeHand = removeHand.remove(Card.S2);
		}
		return removeHand;
	}
	
	public static Cards removeAce(Cards cards) {
		Cards removeHand = cards;

		if (removeHand.contains(Card.CA)) {
			removeHand = cards.remove(Card.CA);
		}
		if (removeHand.contains(Card.HA)) {
			removeHand = removeHand.remove(Card.HA);
		}
		if (removeHand.contains(Card.DA)) {
			removeHand = removeHand.remove(Card.DA);
		}
		if (removeHand.contains(Card.SA)) {
			removeHand = removeHand.remove(Card.SA);
		}
		return removeHand;
	}
	
	public static Cards removeKing(Cards cards) {
		Cards removeHand = cards;

		if (removeHand.contains(Card.CK)) {
			removeHand = cards.remove(Card.CK);
		}
		if (removeHand.contains(Card.HK)) {
			removeHand = removeHand.remove(Card.HK);
		}
		if (removeHand.contains(Card.DK)) {
			removeHand = removeHand.remove(Card.DK);
		}
		if (removeHand.contains(Card.SK)) {
			removeHand = removeHand.remove(Card.SK);
		}
		return removeHand;
	}
}
