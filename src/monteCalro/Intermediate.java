package monteCalro;

import java.util.ArrayList;

/**
 * 中間評価値を作成するメソッド
 *
 * @author 伸也
 *
 */
public class Intermediate {
	public static void main(String[] args) {
		new Intermediate().start();
	}

	public void start() {

		ArrayList<Integer> cards = new ArrayList<Integer>();// 手札を格納するリスト

		for (int myHand = 7; myHand > 0; myHand--) { // 自分の手札枚数

			myHandsSearch(myHand, cards, 0);// 全てのカードを探索する

		}

	}

	/***
	 * 自分の手札を作成するプログラム
	 *
	 * @param size
	 * @param cards
	 * @param num
	 */
	public void myHandsSearch(int size, ArrayList<Integer> cards, int num) {
		if (cards.size() >= size) {
			// 次のメソッド呼び出し

			return;
		}
		for (int i = num; i < 53; i++) {
			cards.add(i);
			num = i + 1;
			myHandsSearch(size, cards, num);
			cards.remove(i);
		}
	}

	/**
	 * 相手の手札を作成するプログラム
	 */
	public void makeOpponentHands(ArrayList<Integer> myHands) {
		int[] field = new int[53];// 全てのカードを表す集合
		int[] rankSize = new int[14];// rankによって持っている枚数を保持

		for (int i = 0; i < 53; i++) {
			if (!myHands.contains(i))// 自分の手札をから抜く
				field[i] = 1;
		}

		if (field[0] == 1) {
			rankSize[0] = 1;
		} else {
			rankSize[0] = 0;
		}
		int num = 0;
		for (int i = 1; i < 14; i++) {
			for (int j = 0; j < 4; j++) {
				num = i + j * 13;
				if (field[num] == 1) {
					rankSize[i]++;
				}

			}
		}
		/***** ここまでが自分のカード状態を場に更新部 ***/

		for (int playerNum = 1; playerNum <= 4; playerNum++) {

		}

	}
}
