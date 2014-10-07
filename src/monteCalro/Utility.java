package monteCalro;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suit;
import jp.ac.uec.daihinmin.card.Suits;

public class Utility {
	/**
	 * card型からint型に変換
	 *
	 * @param card
	 * @return cardをintに変換した数
	 */
	public static int cardParseInt(Card card) {
		if (card.equals(Card.JOKER)) {// jokerの時は0を返す
			return 0;
		}
		int mark = markParseInt(card.suit());
		int rank = rankParseInt(card.rank());

		return mark * 13 + rank;

	}

	/**
	 * suitクラスをint型の0 スペード 1.ハート 2.ダイヤ 3.クラブ に変換
	 *result -1の時エラー
	 * @param suit
	 *            変換したいカードの役
	 * @return markをint型に変換したもの
	 */
	public static int markParseInt(Suit suit) {
		int result = -1;
		try {
			if (suit == Suit.SPADES) {// スペードの時
				result = 0;
			} else if (suit == Suit.HEARTS) {// ハートの時
				result = 1;
			} else if (suit == Suit.DIAMONDS) {// ダイヤの時
				result = 2;
			} else {// クラブの時
				result = 3;
			}
		} catch (Exception e) {
			System.out.println("error Utility: " + e.getMessage());
		}

		return result;
	}

	/**
	 * Ranクラスをint型に変換
	 *
	 * @param rank
	 * @return
	 */
	public static int rankParseInt(Rank rank) {
		int result = 0;
		try {
			result = rank.toInt() - 2;
		} catch (Exception e) {
			System.out.println("error Utility: " + e.getMessage());
		}
		return result;
	}

	/**
	 * Suitsを0 スペード 1.ハート 2.ダイヤ 3.クラブ のマークに変換し、boolean配列に格納
	 *
	 * @param suits
	 *            マークの集合
	 * @return boolean配列でマークの場所をtrueで返す
	 */
	public static boolean[] suitsParseArrayBoolean(Suits suits) {
		boolean[] arrayboolean = new boolean[4];
		// 初期化
		try {
			for (int i = 0; i < 4; i++) {
				arrayboolean[i] = false;
			}

			for(Suit suit:suits){//マークごとの判定

				if(suit == Suit.SPADES){//スペードの時
					arrayboolean[0] = true;
				}else if(suit == Suit.HEARTS){//ハートの時
					arrayboolean[1] = true;
				}else if(suit == Suit.DIAMONDS){//ダイヤの時
					arrayboolean[2] = true;
				}else{//クラブの時
					arrayboolean[3] = true;
				}
			}
		} catch (Exception e) {
			System.out.println("error Utility: " + e.getMessage());
		}
		return arrayboolean;

	}
	/**
	 * Meldからマークのboolean[]を返すで返す 0. スペード. 1 ハート 2. ダイヤ ,3 クラブ
	 *
	 * @return meldに使用されているSuitsのboolean
	 */
	public static boolean[] meldParseSuitsOfBoolean(Meld meld) {
		boolean[] arrayboolean = new boolean[4];
		for (int i = 0; i < 4; i++) {
			arrayboolean[i] = false;
		}
		if(meld != null){
			for(Suit suit: meld.suits()){

				if (suit == Suit.SPADES) {// スペード
					arrayboolean[0]=true;
				} else if (suit == Suit.HEARTS) {// ハート
					arrayboolean[1]=true;
				} else if (suit == Suit.DIAMONDS) {// ダイヤ
					arrayboolean[2]=true;
				} else {// クローバー
					arrayboolean[3]=true;
				}
			}
		}

		return arrayboolean;
	}
	/***
	 * int整数からRankを返すメソッド
	 * nullの時出せる役がないことを示す
	 * @param num　rankに該当する番号
	 * @return Rankクラス
	 */
	public static Rank changeInttoRank(int num){
		Rank rank = null;
		switch (num) {
		case 2:
			rank = Rank.JOKER_LOWEST;
			break;
		case 3:
			rank = Rank.THREE;
			break;
		case 4:
			rank = Rank.FOUR;
			break;
		case 5:
			rank = Rank.FIVE;
			break;
		case 6:
			rank = Rank.SIX;
			break;
		case 7:
			rank = Rank.SEVEN;
			break;
		case 8:
			rank = Rank.EIGHT;
			break;
		case 9:
			rank = Rank.NINE;
			break;
		case 10:
			rank = Rank.TEN;
			break;
		case 11:
			rank = Rank.JACK;
			break;
		case 12:
			rank = Rank.QUEEN;
			break;
		case 13:
			rank = Rank.KING;
			break;
		case 14:
			rank = Rank.ACE;
			break;
		case 15:
			rank = Rank.TWO;
			break;
		case 16:
			rank = Rank.JOKER_HIGHEST;
			break;

		default:
			break;
		}
		return rank;
	}
}
