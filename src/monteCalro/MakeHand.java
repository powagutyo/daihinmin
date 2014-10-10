package monteCalro;

import static jp.ac.uec.daihinmin.card.MeldFactory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.MeldFactory;

/**
 * それらしい手札を作成するプログラム
 *
 * @author 伸也
 *
 */
public class MakeHand {

	/** プレイヤーの人数 **/
	private final int players;

	/** それぞれの席順にしたプレイヤーの出したカード **/
	private ArrayList<ArrayList<Meld>> putHands;
	/** renew時に出した1枚出しの場に出したカードのランク **/
	private int[] renewSingleRank;
	/** renew時に出した2枚出しのカードのランク **/
	private int[] renewDoubleRank;
	/** プレイヤーの手札群 **/
	private int[][] playersHands;
	/** プレイヤーのそれぞれの手札のランクごとの枚数 **/
	private int[][] playersHandSize;
	/** プレイヤーが出したMeldを記憶する変数 **/
	private ArrayList<ArrayList<Meld>> playersPutMeld;
	/*** 0がランダムで1が重みから手札を生成 **/
	private int flag = 0;

	private boolean first = true;

	private final double[] weight = { 0.4013, 0.0661, 0.0799, 0.10145, 0.12915,
			0.1482, 0.19585, 0.1894, 0.20485, 0.22015, 0.24795, 0.30065,
			0.3586, 0.39585, 0.0659, 0.0834, 0.10775, 0.13475, 0.1546, 0.19525,
			0.18905, 0.20155, 0.22285, 0.24205, 0.2872, 0.3414, 0.39825, 0.185,
			0.08725, 0.1078, 0.1388, 0.1627, 0.19475, 0.1909, 0.20115, 0.2147,
			0.2256, 0.2622, 0.3066, 0.37395, 0.0709, 0.0879, 0.1087, 0.1306,
			0.1584, 0.19765, 0.1913, 0.19675, 0.21835, 0.2389, 0.27385, 0.3261,
			0.3808, 4.90265, 0.56635, 0.38625, 0.0831, 0.12625, 0.15675,
			0.1741, 0.17795, 0.19325, 0.18485, 0.1914, 0.1949, 0.20195, 0.2223,
			0.25975, 0.3627, 0.0832, 0.12575, 0.154, 0.17315, 0.1794, 0.19455,
			0.19025, 0.1964, 0.1947, 0.20125, 0.2154, 0.24805, 0.31985, 0.1799,
			0.12945, 0.1549, 0.1718, 0.18185, 0.1986, 0.18825, 0.19395, 0.1955,
			0.20055, 0.20735, 0.23335, 0.28345, 0.08705, 0.1271, 0.15915,
			0.16875, 0.17775, 0.19, 0.1901, 0.1954, 0.19215, 0.196, 0.21515,
			0.24115, 0.3071, 3.6573, 0.3546, 0.19235, 0.1936, 0.1883, 0.1857,
			0.1784, 0.18275, 0.1873, 0.188, 0.18195, 0.1864, 0.18695, 0.18995,
			0.19595, 0.19445, 0.18845, 0.18515, 0.1827, 0.18325, 0.1807,
			0.19145, 0.1852, 0.18475, 0.1863, 0.1871, 0.19025, 0.19475,
			0.19575, 0.19555, 0.1864, 0.19275, 0.1812, 0.1844, 0.18885,
			0.18195, 0.18305, 0.18145, 0.18955, 0.1955, 0.19315, 0.19, 0.1899,
			0.191, 0.19045, 0.1899, 0.18395, 0.18815, 0.17645, 0.18205,
			0.18925, 0.1858, 0.19115, 0.1883, 0.1918, 3.0095, 0.2816, 0.0,
			0.29735, 0.2461, 0.21375, 0.1969, 0.1921, 0.18005, 0.1739, 0.1791,
			0.175, 0.1744, 0.1546, 0.12085, 0.03315, 0.28575, 0.24765, 0.2168,
			0.1964, 0.191, 0.1809, 0.1791, 0.1736, 0.17295, 0.1781, 0.16265,
			0.13875, 0.06725, 0.21605, 0.24465, 0.2123, 0.198, 0.1837, 0.1798,
			0.17395, 0.17685, 0.17975, 0.177, 0.1719, 0.15235, 0.1077, 0.28955,
			0.23275, 0.2097, 0.201, 0.1887, 0.1811, 0.17615, 0.1759, 0.17885,
			0.1755, 0.16845, 0.1473, 0.0909, 3.338, 0.3045, 0.0, 0.29955,
			0.2789, 0.25445, 0.227, 0.20785, 0.1804, 0.16885, 0.1608, 0.15705,
			0.13395, 0.09305, 0.04135, 1.0E-4, 0.30545, 0.27685, 0.2502,
			0.2202, 0.1992, 0.1779, 0.1665, 0.16595, 0.1588, 0.13915, 0.10475,
			0.05385, 0.0050, 0.21405, 0.27245, 0.2456, 0.22045, 0.19365,
			0.17475, 0.17455, 0.16705, 0.16295, 0.151, 0.1222, 0.08515,
			0.02875, 0.29575, 0.27975, 0.24705, 0.22095, 0.19955, 0.1781,
			0.1718, 0.16925, 0.1565, 0.14995, 0.1123, 0.07035, 0.01545, 3.6634,
			0.39025 };

	public MakeHand(int playerNum) {
		this.players = playerNum;
		putHands = new ArrayList<ArrayList<Meld>>(playerNum);
		renewSingleRank = new int[playerNum];
		renewDoubleRank = new int[playerNum];
		playersHands = new int[playerNum][];
		playersHandSize = new int[playerNum][14];
		playersPutMeld = new ArrayList<ArrayList<Meld>>(5);

		for (int i = 0; i < playerNum; i++) {
			playersPutMeld.add(new ArrayList<Meld>());
		}

		for (int i = 0; i < playerNum; i++) {
			putHands.add(new ArrayList<Meld>());
			renewSingleRank[i] = -1;
			renewDoubleRank[i] = -1;
		}
		first = true;
	}

	/***
	 * 初期化用のメソッド
	 */
	public void init() {
		renewSingleRank = new int[players];
		renewDoubleRank = new int[players];
		playersHands = new int[players][];
		playersHandSize = new int[players][14];

		for (int i = 0; i < players; i++) {
			playersPutMeld.add(new ArrayList<Meld>());
		}
		for (int i = 0; i < players; i++) {
			putHands.get(i).clear();
			renewSingleRank[i] = -1;
			renewDoubleRank[i] = -1;
		}
		first = true;
	}

	/**
	 * プレイヤーの出したカードを記憶するメソッド
	 *
	 * @param meld
	 *            プレイヤーが出した役
	 * @param seat
	 *            プレイヤーの座席番号
	 * @param renew
	 *            renewかどうか
	 */
	public void setPlayerCards(Meld meld, int seat, boolean renew) {
		if (meld != MeldFactory.PASS) {// PASSではない時
			playersPutMeld.get(seat).add(meld); // プレイヤーの出した役を格納
			int size;
			putHands.get(seat).add(meld);// Meldを格納

			if (renew) {// renewの時
				size = meld.asCards().size();
				if (size == 1) {// 単体出しの時
					if (meld.asCards().get(0) == Card.JOKER) {
						renewSingleRank[seat] = 14;
					} else {
						renewSingleRank[seat] = meld.rank().toInt() - 2;
					}
				} else if (size == 2) {// 2枚出しの時
					renewDoubleRank[seat] = meld.rank().toInt() - 2;
				}
			}
		}
	}

	/**
	 * 手札を生成するメソッド
	 *
	 * @return 手札の配列を返す
	 */
	public void initHands(MyData md, FieldData fd) {

		first = false;

		int[] feild = md.getField();// 場の残っているカードの配列
		int[] myHand = md.getMyHand(); // 自分の手札配列

		swapAllHands(feild, myHand, fd, md);

	}

	public boolean isFirst() {
		return first;
	}

	/**
	 * 手札を生成し、自分の手をスワップさせる
	 *
	 * @param feild
	 *            場に残っているカード群
	 * @param myHand
	 *            自分の手札のカード集合
	 * @param fd
	 *            FeildDataクラス
	 * @param md
	 *            Mydataクラス
	 */
	private void swapAllHands(int[] feild, int[] myHand, FieldData fd, MyData md) {
		int[][] resultHands = new int[players][53];

		ArrayList<Integer> exchangeMyHand = new ArrayList<Integer>(15); // 交換しなければならない自分の手札のカード群
		ArrayList<Integer> exchangeHand = new ArrayList<Integer>(15); // 交換する相手の手札のカード群

		// 初期化
		int arraySize = 0;
		int mySeat = md.getSeat();// 自分の座席番号

		switch (flag) {
		case 0:
			resultHands = randomInit(fd, feild, myHand);
			flag = 1;
			break;
		case 1:
			resultHands = weughtInit(fd, md);
			break;
		default:
			break;
		}

		int size = resultHands[mySeat].length;

		for (int i = 0; i < size; i++) {
			if (resultHands[mySeat][i] == 1) {
				exchangeHand.add(i); // 交換する自分のカードを格納
			}
		}
		for (int i = 0; i < 53; i++) {// myHandのカードを全て入れる
			if (myHand[i] == 1) {
				exchangeMyHand.add(i);
			}
		}
		// ここからはmyHandと交換するカードが被っている時の判定
		size = myHand.length;
		for (int i = 0; i < size; i++) {
			if (exchangeHand.contains(i)) { // もしこのカードが含まれていた場合
				if (myHand[i] == 1) {// 自分の手札に存在する時
					arraySize = exchangeHand.size();
					for (int j = 0; j < arraySize; j++) {// numのカードを抜く
						if (exchangeHand.get(j) == i) {
							exchangeHand.remove(j);
							break;
						}
					}
					arraySize = exchangeMyHand.size();
					for (int j = 0; j < arraySize; j++) {// numのカードを抜く
						if (exchangeMyHand.get(j) == i) {
							exchangeMyHand.remove(j);
							break;
						}
					}
				}
			}
		}
		// ここでRank順にソートを行う
		Collections.sort(exchangeHand, new RankSort());
		Collections.sort(exchangeMyHand, new RankSort());
		// ここからまで
		size = exchangeHand.size();

		for (int i = 0; i < size; i++) {// swapしてあげる
			swapMyHand(resultHands, exchangeHand.remove(0),
					exchangeMyHand.remove(0), md.getSeat());
		}

		/** ここでplayerHandsと PlayersHandSize を計算 **/
		int counter = 0;

		for (int i = 0; i < players; i++) {
			playersHands[i] = resultHands[i].clone();
			for (int j = 0; j < 14; j++) {
				if (j == 0) {// jokerの処理
					if (playersHands[i][0] == 1) {// joker
						playersHandSize[i][0] = 1;
					}
				} else {
					for (int l = 0; l < 4; l++) { // Rankごとに持っているカードを計測
						if (playersHands[i][j + l * 13] == 1) {
							counter++;
						}
					}
					playersHandSize[i][j] = counter;// 持っている枚数を格納
					counter = 0;
				}
			}
		}

	}

	/***
	 * 場に残っているカードからランダムに手札を生成するメソッド
	 *
	 * @param fd
	 *            FeildDataクラス
	 * @param feild
	 *            場に残っているカード群
	 * @param myHand
	 *            自分の手札のカード群
	 * @return ランダムに分けた時の手札群
	 */
	private int[][] randomInit(FieldData fd, int[] feild, int[] myHand) {
		int[][] resultHands = new int[players][53];

		ArrayList<Integer> notLookCards = new ArrayList<Integer>(53);// まだ見えていないカード群を格納

		// 初期化
		for (int i = 0; i < 53; i++) {
			for (int j = 0; j < players; j++) {
				resultHands[j][i] = 0;
			}
			if (feild[i] == 1 || myHand[i] == 1)// 場に出されたカード以外の場合
				notLookCards.add(i);
		}
		int arraySize;
		int seatSize;
		int random;
		int num;
		// 全ての手札をランダムに分ける
		for (int i = 0; i < players; i++) {
			seatSize = fd.getSeatsHandSize(i);// 座席のカード枚数を登録
			// カードの枚数分ランダムに入れてあげる
			for (int j = 0; j < seatSize; j++) {
				arraySize = notLookCards.size();// 見えていないカードの合計枚数
				random = (int) (Math.random() * arraySize);// ランダムで抜き出すカードを入れる
				num = notLookCards.remove(random);
				resultHands[i][num] = 1;

			}
		}
		return resultHands;
	}

	/***
	 * 重みを基準にして手を生成するメソッド
	 *
	 * @param fd
	 *            FeildDataクラス
	 * @param md
	 *            Mydataクラス
	 */
	public int[][] weughtInit(FieldData fd, MyData md) {
		int[][] resultHands = new int[players][53];
		ArrayList<ArrayList<Integer>> arrayHand = new ArrayList<ArrayList<Integer>>(
				players);

		int[] handSize = fd.getSeatsHandSize();
		int[] grade = fd.getGrade();
		int num = 0;
		boolean flag = false;
		for (int i = 0; i < players; i++) {// ArrayListの初期化
			arrayHand.add(new ArrayList<Integer>());
		}
		while (true) {
			for (int i = 0; i < 53; i++) {// 全てのカード枚数を格納
				num = weightSelect(i);// ここでランクを格納
				for (int j = 0; j < players; j++) {
					if (grade[j] == num) {// 地位が同じ時
						arrayHand.get(j).add(i); // 手札を格納
						break;
					}
				}
			}
			flag = true;
			for (int i = 0; i < players; i++) {
				if (handSize[i] != arrayHand.get(i).size()) {
					flag = false;
					break;
				}
			}
			if (flag) {
				break;
			}
			for (int i = 0; i < players; i++) {
				arrayHand.get(i).clear();
			}
		}
		int size;
		// resultHandsに格納
		for (int i = 0; i < players; i++) {
			size = handSize[i];
			for (int j = 0; j < size; j++) {
				resultHands[i][arrayHand.get(i).get(j)] = 1;
			}
		}

		return resultHands;

	}

	/***
	 * weightの重さから地位を返すメソッド
	 *
	 * @param weightNumber
	 *            weightの参照する番号
	 *
	 * @return 地位
	 */
	public int weightSelect(int weightNumber) {
		int result = 0;

		double weightSum = 0;

		for (int i = 0; i < players; i++) {// 重さの合計を取得
			weightSum += weight[weightNumber + i * 55];
		}

		weightSum = Math.random() * weightSum; // ランダムで分ける

		for (int i = 0; i < players; i++) {// 合計のランダムから出した人の合計を知る

			weightSum -= weight[weightNumber + i * 55];

			if (weightSum <= 0) { // この条件を出す手と仮定する
				result = i + 1;// Rankの高い順のプレイヤー番号を格納
				break;
			}
		}

		return result;
	}

	/****
	 * 自分の手札と相手の手札をswapする
	 *
	 * @param resultHands
	 * @parm num swapするカード
	 * @parm myNum swapするカード
	 * @parm mySeat 自分の座席番号
	 * @return
	 */
	private int[][] swapMyHand(int[][] resultHands, int num, int myNum,
			int mySeat) {
		// 自分の手札のswap
		resultHands[mySeat][myNum] = 1;
		resultHands[mySeat][num] = 0;
		for (int i = 0; i < players; i++) {
			if (i == mySeat) {// 自分手札の時
				continue;
			}
			if (resultHands[i][myNum] == 1) {// swapする対象を見つけた時
				resultHands[i][myNum] = 0;
				resultHands[i][num] = 1;
				break;
			}
		}
		return resultHands;
	}

	/**
	 * 自分の手札からカードをスワップして、
	 *
	 * @param resultHands
	 *            カード配列
	 * @param mySeat
	 *            　交換する側の座席
	 * @param putCard
	 *            場に出したカード
	 * @param exchangeCard
	 *            swapするカード
	 * @return
	 */
	private void swapHand(int mySeat, int putCard,int exchangeCard) {
		// 自分の手札のswap
		playersHands[mySeat][exchangeCard] = 0;
		playersHandSize[mySeat][(exchangeCard - 1) % 13 + 1]--;
		for (int i = 0; i < players; i++) {
			if (i == mySeat) {// 自分手札の時
				continue;
			}
			if (playersHands[i][putCard] == 1) {// swapする対象を見つけた時
				playersHands[i][putCard] = 0;
				playersHands[i][exchangeCard] = 1;
				playersHandSize[i][(exchangeCard - 1) % 13 + 1]++;
				playersHandSize[i][(putCard - 1) % 13 + 1]--;
				break;
			}
		}
	}

	/****
	 * カード交換した時に相手に渡したカードをスワップするメソッド
	 *
	 * @param cards
	 *            交換したカード群
	 * @param seat
	 *            カードをもらった座席の人
	 */
	public void updateToExchange(Cards cards, int seat) {
		int num = 0;

		for (Card card : cards) {
			num = Utility.cardParseInt(card);
			for (int i = 0; i < players; i++) {
				if (playersHands[i][num] == 1) {
					if (i != seat) {// 交換するカードをもう持っていない場合
						// TODO なんかのカードとswapする
					}
					break;
				}
			}

		}
	}

	public void sysHand() {
		for (int i = 0; i < players; i++) {
			System.out.println("プレイヤー番号:" + i);
			for (int j = 0; j < 53; j++) {
				if (playersHands[i][j] == 1) {
					System.out.print(j + ",");
				}
			}
			System.out.println();
		}
	}

	/**
	 * 場にカードを出した時の処理
	 *
	 * @param meld
	 *            出した役
	 * @param seat
	 *            出した人の座席番号
	 * @param fd
	 *            FeildDataクラス
	 * @param mySeat
	 *            　自分の座席番号
	 */
	public void dealingPutCards(Meld meld, int seat, FieldData fd, int mySeat) {

		if (meld != PASS && meld != null) {
			int num = 0;
			if (mySeat == seat) {// 自分の番だった時
				for (Card card : meld.asCards()) {
					num = Utility.cardParseInt(card); // カードに変更する

					playersHands[mySeat][num] = 0;
					playersHandSize[mySeat][(num - 1) % 13 + 1]--;
				}
			} else {// 相手の手番の時
				swapCards(meld, seat, fd);
			}
		}

	}

	/**
	 * 出した座席と役から出したカードをswapさせる
	 *
	 * @param meld
	 *            　場に出した役
	 * @param seat
	 *            出したプレイヤー座席番号
	 * @param fd
	 *            FeildDataクラス
	 */
	private void swapCards(Meld meld, int seat, FieldData fd) {
				int num = 0;
		ArrayList<Integer> arrayInt = new ArrayList<Integer>();

		int rank = 0;
		int exchangeCard = 0;
		for (Card card : meld.asCards()) {// 全ての手に対してカードをswapする
			arrayInt.clear();
			for (int i = 0; i < 53; i++) {// 出した人のプレイヤーの手札を全て抜き出す
				if (playersHands[seat][i] == 1) {
					arrayInt.add(i);
				}
			}
			num = Utility.cardParseInt(card); // カード番号を格納
			rank = (num - 1) % 13 + 1;
			exchangeCard = searchToLookLikeRank(arrayInt, rank);
			swapHand(seat, num, exchangeCard);// ここでカードをswapする
		}
	}

	/**
	 * ランクが一番近いカードのナンバーを返すメソッド
	 *
	 * @param arrayInt
	 * @param Rank
	 * @return
	 */
	private int searchToLookLikeRank(ArrayList<Integer> arrayInt, int rank) {

		Collections.sort(arrayInt, new RankSort());
		int arrayPos = 0;
		int size = arrayInt.size();
		int num = 0;
		int abs = 100;
		// 受け取ったリストからrankとの差の絶対値が近い数を探す
		for (int i = 0; i < size; i++) {
			num = (arrayInt.get(i) - 1) % 13 + 1;
			num = Math.abs(num - rank);
			if (abs > num) {// ランクが一番近い数字を抜き出す
				abs = num;
				arrayPos = i;
			}
		}

		return arrayInt.remove(arrayPos);
	}

	// getter
	public int[][] getPlayersHands() {
		return playersHands;
	}

	/***
	 * ランク順にListをソートするメソッド+
	 *
	 * @author 伸也
	 *
	 */
	private class RankSort implements Comparator<Object> {

		public int compare(Object o1, Object o2) {
			int x = (int) Integer.parseInt(o1.toString());
			int y = (int) Integer.parseInt(o2.toString());

			if (x == 0) {// jokerの時
				x = 14;
			} else {// jokerではない時
				x = (x - 1) % 13 + 1;// ランクの算出
			}

			if (y == 0) {// jokerの時
				y = 14;
			} else {// jokerではない時
				y = (y - 1) % 13 + 1;// ランクの算出
			}
			if (x < y) {
				return 1;
			} else {
				return -1;
			}
		}

	}

}
