package utility;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Suit;

public class Changer {

	/**
	 * カードからマークをint型で返す 0. スペード. 1 ハート 2. ダイヤ ,3 クラブ
	 * 
	 * @return markのナンバー
	 */
	public static int changeIntMark(Card card) {
		int mark;
		if (card.suit() == Suit.SPADES) {// スペード
			mark = 0;
		} else if (card.suit() == Suit.HEARTS) {// ハート
			mark = 1;
		} else if (card.suit() == Suit.DIAMONDS) {// ダイヤ
			mark = 2;
		} else {// クローバー
			mark = 3;
		}
		return mark;
	}
	/**
	 * Suitからマークをint型で返す 0. スペード. 1 ハート 2. ダイヤ ,3 クラブ
	 * 
	 * @return markのナンバー
	 */
	public static int changeIntMark(Suit suit) {
		int mark;
		if (suit == Suit.SPADES) {// スペード
			mark = 0;
		} else if (suit == Suit.HEARTS) {// ハート
			mark = 1;
		} else if (suit == Suit.DIAMONDS) {// ダイヤ
			mark = 2;
		} else {// クローバー
			mark = 3;
		}
		return mark;
	}

	/**
	 * カードからマークをint型で返す 0. スペード. 1 ハート 2. ダイヤ ,3 クラブ
	 * 
	 * @return markのナンバー
	 */
	public static int changeIntMark(String suit) {
		int mark;
		if (suit.equals("S")) {// スペード
			mark = 0;
		} else if (suit.equals("H")) {// ハート
			mark = 1;
		} else if (suit.equals("D")) {// ダイヤ
			mark = 2;
		} else {// クローバー
			mark = 3;
		}
		return mark;
	}

	/**
	 * 入れた全てのカードのboolean[][]からmeldのカード抜くメソッド
	 * 
	 * @param copyLookCard
	 *            カードのboolean集合
	 * @param meld
	 *            出された役
	 * @return
	 */
	public static boolean[][] examine_copyLookCard(boolean[][] copyLookCard,
			Meld meld) {
		for (Card card : meld.asCards()) {
			copyLookCard[ Changer.changeIntMark(card)][card.rank().toInt() - 2] = true;
		}
		return copyLookCard.clone();
	}

	/**
	 * カードのマークと数字からcardを返す
	 * 
	 * @param mark
	 *            マークの数字 0. スペード. 1 ハート 2. ダイヤ ,3 クラブ
	 * @param number
	 *            1～13→3～2
	 * @return card
	 */
	public static Card changwMarkInt(int mark, int number) {
		Card card = null;
		String stringCard = "";
		try {
			if (mark == 0) {// スペード
				stringCard += "S";
			} else if (mark == 1) {// ハート
				stringCard += "H";
			} else if (mark == 2) {// ダイヤ
				stringCard += "D";
			} else {// クラブ
				stringCard += "C";
			}

			/* number 1～13 を3～15に変換 */
			number += 2;
			if (number > 13) {// 14 15　を 1 2に変換
				number -= 13;
			}
			if (number == 11) {// Jの場合
				stringCard += "J";
			} else if (number == 12) {// Qの場合
				stringCard += "Q";
			} else if (number == 13) {// kの場合
				stringCard += "K";
			} else if (number == 1) {// Aの場合
				stringCard += "A";
			} else {// それ以外
				stringCard += number;
			}

			card = Card.valueOf(stringCard);
		} catch (Exception e) {
			System.out.println("エラー発生: changeMarkIntメソッド");
		}
		return card;
	}

	/**
	 * 自分の手札から役を除くメソッド
	 * 
	 * @param myHand
	 *            自分の手札にあるカード
	 * @param meld
	 *            除く役
	 * @return 役を除いた自分の手札
	 */
	public static Cards removeMelds(Cards myHand, Meld meld) {
		for (Card card : meld.asCards()) {
			myHand = myHand.remove(card);
		}

		return myHand;
	}

}
