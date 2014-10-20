package monteCalro;

import static jp.ac.uec.daihinmin.card.MeldFactory.*;

import java.util.ArrayList;

import jp.ac.uec.daihinmin.Place;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;

/***
 * 0 スペード 1.ハート 2.ダイヤ 3.クラブ
 *
 * 　単純な原始モンテカルロプレイヤー
 *
 * @author 飯田伸也
 *
 */
public class MonteCalro_01 {

	private final int count = 2000;// 回す数

	private final int players = 5;// プレイヤーの数

	private final int cardNum = 53;// カード大きさ

	private int mySeat;// 自分の座席番号

	private int myPlayer;// 自分のプレイヤー番号

	private enum Role {// 役の種類
		/** 初期状態 **/
		EMPTY,
		/** 新しく場に回ってきた状態 **/
		RENEW,
		/** 場に1枚出しが出されている状態 **/
		SINGLE,
		/** 場に複数のペア出しが出されている状態 **/
		GROUP,
		/** 場に階段出しが出されている状態 **/
		SEQUENCE
	}

	private Role role = Role.EMPTY;// 初期状態の変数

	private boolean lock; // 縛りがあるかどうか

	private boolean[] lockNumber = new boolean[4];// 縛られているマークの番号

	private boolean[] placeSuits = new boolean[4];// 場に出されているマーク

	private boolean[] wonPlayer = new boolean[players]; // 勝利しているプレイヤー
	/** 3～2の数を0～12で表したもの -1がrenew **/
	private int rank;// 場に出されているランク

	/** 革命している時はtrue **/
	private boolean reverse;// 革命しているか否か

	private int numberOfCardSize;// 場に出されているカードの枚数

	private int putLastPlayer;// 最後に出した人の座席番号

	private int turnPlayer; // ターンプレイヤー

	private boolean[] passPlayer;// PASSしたかどうかの判定用の変数

	private int[][] playerHands; // プレイヤーの手札 int[席順][カードの種類]= 0 or 1(持っているカード)

	private int[] playerHandsOfCards;// プレイヤーごとの手札の枚数 int[席順] = 枚数

	private int[][] playerTypeOfHandsofCards;// プレイヤーの種類によってのカード枚数
												// int[席順][カードの種類]= 枚数　0～13
												// JOKER 3 4の順番

	private ArrayList<Integer> cards = new ArrayList<Integer>(); // 実際に出されたカード群

	/**
	 * モンテカルロの実行メソッド
	 *
	 * @param bs
	 *            BotSkelton
	 * @param md
	 *            MyData
	 */
	public static Meld MonteCalroPlay(BotSkeleton bs, MyData md, FieldData fd) {
		return new MonteCalro_01().play(bs, md, fd);
	}

	/**
	 * 原始モンテカルロの実行
	 *
	 * @param bs
	 *            BotSkelton
	 * @param md
	 *            MyData
	 */
	private Meld play(BotSkeleton bs, MyData md, FieldData fd) {

		ArrayList<MeldData> arrayListMelds = searchOfMeld(bs);// 出せる役を探索

		if (arrayListMelds == null)
			return PASS;// ArrayListに何も入っていないならPASSを返す

		/** 変数の初期化始め **/

		final int arraySize = arrayListMelds.size(); // 出せる役の枚数

		passPlayer = new boolean[players];

		myPlayer = md.getPlayerNumber();

		mySeat = md.getSeat();

		/** 変数の初期化終了 **/

		int putRandom = 0; // 最初に出す手をランダムに選ぶ

		turnPlayer = 0;//

		boolean first = true; // 1回目の時だけ自分の役集合から出すので特殊である。

		// メインループ
		for (int playout = 0; playout < count; playout++) {

			playerHands = initHands(md, fd);

			playerTypeOfHandsofCards = getTypeOfHandsOfCards();// 自分と相手のカードランクの枚数を計算する。

			init(bs, fd);// 変数の初期化

			first = true; // 最初の1回目はtrue

			turnPlayer = mySeat;// ターンプレイヤーの更新

			while (true) { // 1プレイ分

				// sysout(playout);

				if (!first) {// 最初の1回目ではない時

					putToSeeStateOfField(); // 場の状態を確認し、出した手を場に反映させる

				} else {// 最初の一回目の時

					putRandom = (int) (Math.random() * arraySize); // ランダムで最初に選ぶ手を決める

					renewPlace_MeldData(arrayListMelds.get(putRandom));// 自分の役集合だけで選ぶ

					first = false;
				}

				if (checkGoalPlayer()) { // 上がった人の判定
					break;// 自分上がった時
				}
				// 8切りの判定
				if (checkEight(role, numberOfCardSize, rank))
					allPlayerDoPass();// すべてのプレイヤーをパスにする

				if (checkRenew()) {// renewするかどうかの判定
					renew();
				}

				updateTurnPlayer(); // ターンプレイヤーの更新

			}
			// 得点の計算等々
			arrayListMelds.get(putRandom).plusNumberOfTimes(); // nの個数を足してあげる
			arrayListMelds.get(putRandom).plusPoint(returnWinPoint()); // 勝ち点を渡してあげる

		}

		double point = -1;
		int resultPos = 0;
		double x = 0;
		for (int i = 0; i < arraySize; i++) {
			x = arrayListMelds.get(i).getPointDivideN();
			if (x > point) {
				point = x;
				resultPos = i;
			}

		}
		return arrayListMelds.get(resultPos).getMeld();

		// TODO 結局どの手を出すかどうか
	}

	public void sysout1(int playout) {
		System.out.println("ターンプレイヤー :" + turnPlayer);
		System.out.println("場のランク: " + rank);
		System.out.println("状態 :" + role);
		System.out.println("mySeat :" + mySeat);
		for (int i = 0; i < players; i++) {
			System.out.println(i + "プレイヤーの手札:" + playerHandsOfCards[i]
					+ "  勝ったかどうか:" + wonPlayer[i] + " PASSプレイヤー "
					+ passPlayer[i]);

		}
		System.out.println("プレイアウト :" + playout);

	}

	/**
	 * ゲーム終了時のポイントを返す
	 *
	 * @return point
	 */
	private int returnWinPoint() {
		int result = 5;
		for (int i = 0; i < players; i++) {
			if (i != mySeat && wonPlayer[i]) {
				result--;
			}
		}
		return result;
	}

	/**
	 * 初期化のメソッド
	 *
	 * @param bs
	 *            BotSkelton
	 */
	private void init(BotSkeleton bs, FieldData fd) {
		Place place = bs.place();
		Meld lastMeld = place.lastMeld();

		cards.clear();// カードの初期化

		role = getRole(lastMeld);// 役を判定する

		// 縛りが存在するかどうかのチェック
		if (place.lockedSuits() != Suits.EMPTY_SUITS) {
			lock = true;// 縛りが存在する
			// 縛っているマークを探索
			lockNumber = Utility.suitsParseArrayBoolean(place.lockedSuits());
		}
		// 場のマークを取り出す
		placeSuits = Utility.meldParseSuitsOfBoolean(lastMeld);
		// 革命しているかどうか
		reverse = place.isReverse();
		// 場に出されているカードのランク
		// 場に出されているカードの枚数の代入
		if (lastMeld != null) {
			rank = lastMeld.rank().toInt() - 2;
			numberOfCardSize = lastMeld.asCards().size();

			for (Card card : lastMeld.asCards()) {
				cards.add(Utility.cardParseInt(card));// カード1枚1枚の格納
			}
		} else {
			if (!reverse) {
				rank = 0;// rankがフィールドに存在しない時
			} else {
				rank = 14;
			}
			numberOfCardSize = 0;
		}

		// wonPlayerの更新
		wonPlayer = fd.getWonPlayer();
		// PASSしたプレイヤーをの初期状態の格納
		passPlayer = fd.getPassPlayer();
		/** 勝ったプレイヤーは必ずパスになる **/
		for (int i = 0; i < players; i++) {
			if (wonPlayer[i])
				passPlayer[i] = true;
		}
		// LastPLayerの座席番号を格納
		putLastPlayer = fd.getPutLastPlayer();

		// 座席ごとの手札の枚数を格納
		playerHandsOfCards = fd.getSeatsHandSize();
	}

	/**
	 * すべてのプレイヤーをPASSにする
	 */
	private void allPlayerDoPass() {
		for (int i = 0; i < players; i++) {
			passPlayer[i] = true;
		}
	}

	/**
	 * 勝ったプレイヤーを判定する 自分のプレイヤーが上がった時点で終了
	 *
	 * @returnゲームを終了するかどうか
	 */
	private boolean checkGoalPlayer() {
		boolean result = true;// 自分が上がったかどうか
		// 自分が上がっていない状態で他のプレイヤーが全員上がっている時
		if (!wonPlayer[mySeat]) {
			for (int i = 0; i < players; i++) {
				if (i != mySeat && !wonPlayer[i]) {// 自分のターンじゃなくかつ誰か上がっていない時
					result = false;
					break;
				}
			}
		}

		// プレイヤーの手札を見た時に1枚もなかった場合は勝ちプレイヤー
		for (int i = 0; i < players; i++) {
			if (playerHandsOfCards[i] <= 0) {// カードが存在しない時
				wonPlayer[i] = true;
				if (i == mySeat)// 自分が上がったなら
					result = true;
			}
		}

		return result;
	}

	/**
	 * 場が流れた時に呼び出すメソッド
	 */
	private void renew() {
		role = Role.RENEW;
		// 場に出ているカードのランクの初期化
		if (!reverse) {
			rank = 0;
		} else {
			rank = 14;
		}
		numberOfCardSize = 0;// カードの大きさを初期化
		lock = false;
		// PASSプレイヤーの更新
		for (int i = 0; i < players; i++) {
			if (wonPlayer[i]) {
				passPlayer[i] = true;
			} else {
				passPlayer[i] = false;
			}
		}
		// 場のマークと縛りのマークを初期化
		for (int i = 0; i < 4; i++) {
			lockNumber[i] = false;
			placeSuits[i] = false;
		}
	}

	/**
	 * renewするかの判定 trueの時はrenewする、falseの時はしない
	 *
	 * @return renewするかどうか
	 */
	private boolean checkRenew() {

		boolean result = false;
		int notPassPlayer = players; // PASSしたプレイヤーの数を数える
		for (int i = 0; i < players; i++) {
			if (passPlayer[i])// パスしたプレイヤーの時
				notPassPlayer--;
		}
		if (notPassPlayer <= 1)// パスしていないプレイヤーが1人以下の時
			result = true;

		return result;
	}

	/**
	 * Meldクラスから場に出されている役の種類を更新する
	 *
	 * @param lastMeld
	 *            Meld
	 * @return その状態
	 */
	private Role getRole(Meld lastMeld) {
		Role result = Role.EMPTY;
		// 場のカードの状態を保存
		if (lastMeld == null) {// renewの時
			result = Role.RENEW;
		} else if (lastMeld.type() == Meld.Type.SINGLE) {// 1枚出しの時
			result = Role.SINGLE;
		} else if (lastMeld.type() == Meld.Type.GROUP) {// 複数枚出しの時
			result = Role.GROUP;
		} else {// 階段の時
			result = Role.SEQUENCE;
		}
		return result;
	}

	/**
	 * ターンプレイヤーの更新 renewの時の処理を含む
	 */
	private void updateTurnPlayer() {
		if (role == Role.RENEW) {// renewした時の判定
			/** renewした時は最後に置いたプレイヤーをターンプレイヤーにする **/
			if (wonPlayer[turnPlayer]) {
				turnPlayer++;
				if (turnPlayer >= players)// プレイヤーの人数把握
					turnPlayer = 0;
			} else {
				turnPlayer = putLastPlayer;
			}
		} else {// それ以外
			turnPlayer++;
			if (turnPlayer >= players)// プレイヤーの人数把握
				turnPlayer = 0;
		}

	}

	/**
	 * 場の状態を見て自分の出す手を決めて、場の更新を行う
	 */
	private void putToSeeStateOfField() {
		int[] putHand = null; // 出した手を格納

		if (!passPlayer[turnPlayer]) {// そのプレイヤーがパスの状態だった場合
			switch (role) {
			case SINGLE:// 単体出しの時
				putHand = returnResult_SingleMeld();// 単体出しで出す役を受け取る
				break;
			case GROUP:// ペア出しの時
				putHand = returnResult_Group();// ペア出しで出す役を受け取る
				break;
			case SEQUENCE:// 階段出しの時
				putHand = returnResult_Sequence();// 階段出しで出す役を受け取る
				break;
			case RENEW:// renewの時
				putHand = returnResult_renewMeld();// renew時に出す役を受ける
				break;
			default:
				System.out
						.println("エラー発生　MonteCalro.java putToSeeStateOfFieldメソッド　：");
				break;
			}
		}

		if (putHand == null) {// PASSの時
			// PASSプレイヤーに更新
			passPlayer[turnPlayer] = true;
		} else {
			// 場の更新
			updatePlace(putHand);
		}

	}

	/**
	 * 場の更新を行うメソッド
	 *
	 * @param putHand
	 *            場に出された役
	 */
	private void updatePlace(int[] putHand) {

		int size = putHand.length;
		int num = 0;
		// カードサイズの更新
		numberOfCardSize = size;
		// 最後に出した人の更新
		putLastPlayer = turnPlayer; // 最後に出した人を更新

		if (role == Role.RENEW) {// Renewの時の更新部

			role = getRole(size, putHand);

			boolean joker = false;
			// マークの探索
			for (int i = 0; i < size; i++) {
				num = putHand[i];
				if (num == 0) {// jokerの時の処理
					joker = true;
					continue;
				}
				placeSuits[(num - 1) / 13] = true; // 役にあったカードをtrueにする
			}
			// jokerがあった時はランダムでマークをあてる
			if (joker) {// jokerがあった時の処理
				for (int i = 0; i < 4; i++) {
					if (!placeSuits[i]) {
						placeSuits[i] = true;
						break;
					}
				}

			}

		} else {// それ以外の時の更新部

			if (!lock) {
				// 縛りが存在するかの確認
				boolean[] cloneLock = new boolean[4];
				// 今の役のマークを格納する
				for (int i = 0; i < size; i++) {
					num = (putHand[i] - 1) / 13;
					cloneLock[i] = true;

				}
				boolean result = true;
				// 場の役と今の役を比べる
				for (int i = 0; i < 4; i++) {
					if (placeSuits[i] != cloneLock[i]) {
						result = false;
						break;
					}
				}
				if (result) { // 縛りが成立する場合
					lock = true;
					lockNumber = cloneLock.clone();

				}
				placeSuits = cloneLock;

			} else {// 縛りが存在する時
				placeSuits = lockNumber.clone();
			}

		}

		// 役のランクを更新
		if (putHand[0] == 0 && role == Role.SINGLE) {// joker単体出しの時の処理
			if (!reverse) {
				rank = 14;
			} else {
				rank = 0;
			}
		} else {
			if (putHand[0] != 0) {
				rank = (putHand[0] - 1) % 13 + 1;
			} else {
				rank = (putHand[1] - 1) % 13 + 1;
			}
		}

		// 革命の判定 (電通大のルール参照)
		if (role == Role.GROUP) {
			if (numberOfCardSize >= 4)
				reverse = !reverse;
		} else if (role == Role.SEQUENCE) {
			if (numberOfCardSize >= 5)
				reverse = !reverse;
		}

		// 手札から指定した枚数とカードを抜く

		for (int i = 0; i < numberOfCardSize; i++) {
			num = putHand[i];
			playerHands[turnPlayer][num] = 0;
			if (num == 0) {// jokerの時の処理
				playerTypeOfHandsofCards[turnPlayer][0] = 0;
			} else {
				num = (num - 1) % 13 + 1;
				playerTypeOfHandsofCards[turnPlayer][num] -= 1;
			}
		}
		playerHandsOfCards[turnPlayer] -= numberOfCardSize;

	}

	private boolean checkEight(Role r, int size, int rank) {
		boolean result = false;
		if (r != Role.SEQUENCE) { // 階段以外の時
			if (rank == 6)
				result = true;
		} else {
			if (!reverse) {// 革命じゃない時
				if (rank - 1 + size >= 6)
					result = true;
			} else {// 革命の時
				if (rank + 1 - size <= 6)
					result = true;

			}
		}
		return result;
	}

	/**
	 * 役の大きさと使ったカードから役の出し方を判定し返すメソッド
	 *
	 * @param size
	 *            役の大きさ
	 * @param putHand
	 *            　出したカードの配列
	 * @return 出された役の出し方を返す
	 */
	private Role getRole(int size, int[] putHand) {
		Role r = Role.EMPTY;
		if (size == 1) {// カードが一枚の時
			r = Role.SINGLE;
		} else {
			if (size == 2) {// カードが2枚の時
				r = Role.GROUP;
			} else {// カードが3枚以上の時
				int x = 0;
				int y = 0;
				for (int i = 0; i < 3; i++) {// 3枚見ればカード役がわかるため
					if (putHand[i] == 0)// jokerの時
						continue;

					if (x == 0) {
						x = (putHand[i] - 1) % 13;
					} else {
						y = (putHand[i] - 1) % 13;
						break;
					}
					if (x == y) {// rankが同じ場合
						r = Role.GROUP;
					} else {// 　rankが違う場合
						r = Role.SEQUENCE;
					}

				}
			}

		}

		return r;
	}

	/**
	 * 単体出しの出せる手を返す
	 *
	 * @return 場に最終的に出す役
	 */
	private int[] returnResult_SingleMeld() {
		int[] resultArray = searchSingleMeld();// 出せる役の結果
		if (resultArray == null)
			return null;// PASS
		int[] result = new int[1];
		/** 出すことの出来る役の数 **/
		int canPutNumber = resultArray.length + 1; // PASSの時の判定
		int random = (int) (Math.random() * canPutNumber);

		if (random == canPutNumber - 1) {// PASSの時
			return null;// PASSを返す null
		}
		result[0] = resultArray[random];
		return result;
	}

	/**
	 * 単体出しの出せるカードを探索 (RENEWでも使える)
	 *
	 * @return 出せるすべての役
	 */
	private int[] searchSingleMeld() {
		int[] array = null;
		// スペ3の判定
		if ((rank == 14 || rank == 0) && role != Role.RENEW) {// JOKERが場に出されている時
			if (playerHands[turnPlayer][1] == 1) { // スぺ3を持っている時
				array = new int[1];
				array[0] = 1;

			}
			array = new int[1];
			array[0] = 1;
			return array;// jokerを出されてスぺ3のカードがない場合は出すカードが存在しないので
		}
		int x = cardNum / players + 1;
		array = new int[x]; // 自分の手札に持てる数分の配列を作る
		for (int i = 0; i < x; i++) {
			array[i] = 100;// ありえない数
		}
		int counter = 0;
		if (playerTypeOfHandsofCards[turnPlayer][0] == 1) {// jokerを持っている時
			array[counter] = 0;
			counter++;
		}
		int num = 0;
		if (!reverse) {// 普通の時
			for (int i = rank + 1; i < 14; i++) {
				if (playerTypeOfHandsofCards[turnPlayer][i] != 0)// 1枚以上存在する時
					for (int j = 0; j < 4; j++) {
						if (lock) {// 縛りが存在する場合
							if (!lockNumber[j]) {// 縛られているマークの時
								continue;
							}
						}
						num = i + j * 13;
						if (playerHands[turnPlayer][num] == 1) {// もしそのカードを持っている時
							array[counter] = num;
							counter++;
						}
					}
			}
		} else {// 革命の時
			for (int i = rank - 1; i > 0; i--) {
				if (playerTypeOfHandsofCards[turnPlayer][i] != 0)// 1枚以上存在する時
					for (int j = 0; j < 4; j++) {
						if (lock) {// 縛りが存在する場合
							if (!lockNumber[j]) {// 縛られているマークの時
								continue;
							}
						}
						num = i + j * 13;
						if (playerHands[turnPlayer][num] == 1) {// もしそのカードを持っている時
							array[counter] = num;
							counter++;
						}
					}
			}
		}
		if (counter == 0) {// 出せる役がない時
			return null;
		}
		int[] result = new int[counter];
		for (int i = 0; i < counter; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/**
	 * Groupの結果を返す
	 */
	private int[] returnResult_Group() {
		int[][] resultAray = searchGroupMeld(numberOfCardSize);// グループのペアを探す
		if (resultAray == null)
			return null;// PASS
		int size = resultAray.length;

		size++;// PASSを追加

		int random = (int) (Math.random() * size);

		if (random == size - 1) {// PASSの時
			return null;
		}
		return resultAray[random].clone();

	}

	/**
	 * ペア出しの役を探索するメソッド 役が入っていない時nullを返す
	 *
	 * @param size
	 *            Meldのサイズ
	 * @return 役の集合を返す int[必要なもの][出せる役]
	 */
	private int[][] searchGroupMeld(int size) {
		int[][] resultArray = new int[64][];
		int[] meld = new int[size];// ひとつの役
		int[] c = new int[size + 1];
		// 配列の初期化
		for (int l = 0; l < size; l++) {
			meld[l] = 0;
			c[l] = 0;
		}
		c[size] = 0;

		boolean joker = false; // jokerを持っているかどうか
		int jk = 0;// ジョーカーの枚数分足すための変数

		if (playerTypeOfHandsofCards[turnPlayer][0] == 1) {// jokerを持っている時
			joker = true;
			jk = 1;
		}
		int counter = 0;// 配列の番号を記憶する
		int resultCounter = 0;
		int num = 0;
		int x = 0;// 計算用の変数
		int[] conbine = new int[5];// カード枚数
		for (int l = 0; l < 5; l++) {
			conbine[l] = 100;// ありえない数
		}
		if (!reverse) {// 革命が起きていない時
			for (int i = rank + 1; i < 14; i++) {
				num = playerTypeOfHandsofCards[turnPlayer][i] + jk;
				counter = 0;
				if (num >= size) {// ペア出し出来る
					if (joker) {
						conbine[counter] = 0;
						counter++;
					}
					for (int j = 0; j < 4; j++) {
						x = i + j * 13;
						if (playerHands[turnPlayer][x] == 1) {
							conbine[counter] = x;
							counter++;
						}
					}
					resultCounter = combination(resultArray, meld, conbine, c,
							1, num, size, resultCounter);
				}
			}
		} else {// 革命の時
			for (int i = rank - 1; i > 0; i--) {
				num = playerTypeOfHandsofCards[turnPlayer][i] + jk;
				counter = 0;
				if (num >= size) {// ペア出し出来る
					if (joker) {
						conbine[counter] = 0;
						counter++;
					}
					for (int j = 0; j < 4; j++) {
						x = i + j * 13;
						if (playerHands[turnPlayer][x] == 1) {
							conbine[counter] = x;
							counter++;
						}
					}
					resultCounter = combination(resultArray, meld, conbine, c,
							1, num, size, resultCounter);
				}
			}
		}
		if (resultCounter == 0) {// 役が入っていない時
			return null;
		}
		// 規定数に変換している
		int[][] result = new int[resultCounter][];
		for (int i = 0; i < resultCounter; i++) {
			result[i] = resultArray[i];
		}
		return result;
	}

	/**
	 * コンビネーションで出せる役を探索する
	 *
	 * @param result
	 *            最終的の役
	 * @param resultMeld
	 *            一時的に入れる役
	 * @param meld
	 *            入っているものmeld
	 * @param c
	 *            よくわからん配列
	 * @param m
	 *            初期値1
	 * @param n
	 *            nCrのn
	 * @param r
	 *            nCrのr
	 * @param counter
	 *            resultに入れる配列
	 * @return
	 */
	private int combination(int[][] result, int[] resultMeld, int[] meld,
			int[] c, int m, int n, int r, int counter) {
		if (m <= r) {
			for (int i = c[m - 1] + 1; i <= n - r + m; i++) {
				resultMeld[m - 1] = meld[i - 1];
				c[m] = i;
				counter = combination(result, resultMeld, meld, c, m + 1, n, r,
						counter);
				if (m == r) {// 配列の最後まで埋まった時
					if (checkLock_Group(resultMeld)) {// 縛りをチェックする
						result[counter] = resultMeld.clone();
						counter++;
					}
				}
			}
		} else {
			return counter;
		}
		return counter;

	}

	/**
	 * ペア出しの役を縛りの状況を見て判定する
	 *
	 * @param meld
	 *            判定する役
	 * @return 出せるかどうか
	 */
	private boolean checkLock_Group(int[] meld) {
		if (!lock)// 縛りが存在しない時
			return true;
		boolean result = false;
		int size = meld.length;
		int counter = 0;
		for (int i = 0; i < 4; i++) {
			if (lockNumber[i])// 縛りのある数を数える
				counter++;
		}
		int num = 0;
		for (int i = 0; i < size; i++) {
			if (meld[i] == 0) {// jokerの時
				counter--;
			}
			num = (meld[i] - 1) / 13;// markを抽出
			if (lockNumber[num]) {// 縛りの数の時
				counter--;
			}
		}
		if (counter == 0) {// もし縛りが全て大丈夫な時
			result = true;
		}
		return result;
	}

	private int[] returnResult_Sequence() {
		int[][] resultAray = searchSequenceMeld(numberOfCardSize);// グループのペアを探す
		if (resultAray == null)
			return null;// PASS

		int size = resultAray.length;

		size++;// PASSを追加

		int random = (int) (Math.random() * size);

		if (random == size - 1) {// PASSの時
			return null;
		}
		return resultAray[random].clone();
	}

	/**
	 * カード枚数から階段を探索するメソッド
	 *
	 * @param cardSize
	 *            カードの大きさ
	 * @return カードが入った配列を返す nullの場合PASS
	 */
	private int[][] searchSequenceMeld(int cardSize) {
		int[][] resultArray = new int[256][];
		int[] meld = new int[cardSize];
		int resultCounter = 0;
		int counter = 0;
		boolean joker = false;

		if (playerHands[turnPlayer][0] == 1) {// jokerを持っている時
			joker = true;
		}
		int defalt = 0;
		if (role == Role.RENEW) {
			if (!reverse) {
				defalt = 1;
			} else {
				defalt = 13;
			}
		} else {
			if (!reverse) {
				defalt = rank + cardSize;
			} else {
				defalt = rank - cardSize;
			}
		}
		int num = 0;
		if (!reverse) {// 普通の時
			for (int i = defalt; i < 15 - cardSize; i++) {// カードでの探索
				for (int j = 0; j < 4; j++) {
					num = i + j * 13;// カードを表現
					if (playerHands[turnPlayer][num] == 1) {// そのカードを持っている時
						if (lock && !lockNumber[j])// 縛りが存在しており、縛られているカードではない場合
							continue;// ここは見ない

						resultCounter = searchSequence(resultArray, meld,
								counter, num, cardSize, joker, resultCounter,
								false);// 階段の探索を行う
						meld = new int[cardSize];
					}
				}
			}
		} else {// 革命の時
			for (int i = defalt; i >= cardSize - 1; i--) {// カードでの探索
				for (int j = 0; j < 4; j++) {
					num = i + j * 13;// カードを表現
					if (playerHands[turnPlayer][num] == 1) {// そのカードを持っている時
						if (lock && !lockNumber[j])// 縛りが存在しており、縛られているカードではない場合
							continue;// ここは見ない

						resultCounter = searchSequence(resultArray, meld,
								counter, num, cardSize, joker, resultCounter,
								false);// 階段の探索を行う
						meld = new int[cardSize];
					}
				}
			}
		}
		if (resultCounter == 0) {// 役がない時
			return null;
		}
		int[][] result = new int[resultCounter][];
		for (int i = 0; i < resultCounter; i++) {
			result[i] = resultArray[i];
		}

		return result;

	}

	/***
	 * あるランクからの階段ができるかの判定、探索を行うメソッド
	 *
	 * @param result
	 *            結果を入れる配列
	 * @param meld
	 *            　役を格納する配列
	 * @param counter
	 *            階段が今何個入ったかを計算する数
	 * @param num
	 *            　階段のはじめのランク
	 * @param size
	 *            役の大きさ
	 * @param joker
	 *            jokerが存在するかの有無
	 * @param resultCounter
	 *            結果を何番目に入れるかの配列
	 * @param dojoker
	 *            jokerを使った時
	 * @return　resultCounnter
	 */
	private int searchSequence(int[][] result, int[] meld, int counter,
			int num, int size, boolean joker, int resultCounter, boolean dojoker) {

		if (!dojoker) { // jokerを使わな勝った時
			meld[counter] = num;

		} else {
			meld[counter] = 0;
		}

		meld[counter] = num;

		counter++;

		if (counter == size) {// 役が成立した時
			result[resultCounter] = meld.clone(); // 結果に格納
			resultCounter++;
			return resultCounter;
		}
		if (!reverse) {// 普通の時
			num++;
			if (playerHands[turnPlayer][num] == 1) {// 2より上は見ない
				meld[counter] = num;
				resultCounter = searchSequence(result, meld, counter, num,
						size, joker, resultCounter, false);
			}
			num--;
		} else {// 革命が起きている時
			num--;
			if (playerHands[turnPlayer][num] == 1) {// 3より下は見ない
				meld[counter] = num;
				resultCounter = searchSequence(result, meld, counter, num,
						size, joker, resultCounter, false);
			}
			num++;
		}

		if (joker) {// jokerを持っている時
			joker = false;
			resultCounter = searchSequence(result, meld, counter, num, size,
					joker, resultCounter, true);
		}

		return resultCounter;
	}

	/**
	 * MeldDataから場の更新を行う
	 *
	 * @param md
	 *            MeldData
	 */

	private void renewPlace_MeldData(MeldData md) {
		int num = -1;

		if (!md.isPass()) {// MeldDataがパスでは無い時
			// TODO 革命の判定
			if (role != Role.RENEW && !lock) {// renewじゃない時かつ縛りが無い時
				// 探索
				boolean result = true;
				for (int i = 0; i < numberOfCardSize; i++) {
					if (num == 0)// Jokerの時はcontinue
						continue;
					num = (md.getArrayCards(i) - 1) / 13;// markを取る
					if (!placeSuits[num]) {
						result = false;
						break;
					}
				}
				if (result) {// 縛りの発生
					lock = true;
					lockNumber = placeSuits.clone();
				}
			}

			// 初期化
			for (int i = 0; i < 4; i++) {
				placeSuits[i] = false;
			}
			numberOfCardSize = md.getSize();// 場に出ているカード枚数を記憶
			boolean joker = false;
			// placeSuitsの更新
			for (int i = 0; i < numberOfCardSize; i++) {
				num = md.getArrayCards(i);
				if (num == 0) {// Jokerの時はcontinue
					joker = true;
					continue;
				}
				num = (num - 1) / 13;// markを取る
				placeSuits[num] = true;
			}

			if (joker) {// jokerの時、適当にマークを割り当てる
				for (int i = 0; i < 4; i++) {
					if (!placeSuits[i])
						placeSuits[i] = true;
				}
			}

			role = getRole(md.getMeld());// 場の状態の更新

			putLastPlayer = turnPlayer;// 最後に出したプレイヤーを自分にする

			rank = md.getMeld().rank().toInt() - 2;

			// 手札から指定した枚数とカードを抜く

			for (int i = 0; i < numberOfCardSize; i++) {
				num = md.getArrayCards(i);
				playerHands[turnPlayer][num]--;
				if (num == 0) {
					playerTypeOfHandsofCards[turnPlayer][0]--;
				} else {
					num = (num - 1) % 13 + 1;
					playerTypeOfHandsofCards[turnPlayer][num]--;
				}
			}
			playerHandsOfCards[turnPlayer] -= numberOfCardSize;

		} else {// PASSした時
			passPlayer[mySeat] = true;
		}
	}

	private int[] returnResult_renewMeld() {
		// TODO もしかしたら　ペア出しと階段がバグる可能性があるかも
		int[] result = null;
		int[][] resultArray = new int[256][];
		int[][] cloneArray = null;
		result = searchSingleMeld();// 出せる役の結果
		int arrayCounter = 0;
		int size = result.length;
		int[] x = new int[1];
		/** それぞれの単体出し、ペア出し、階段出しの役を格納していく **/
		// 単体出し格納
		for (int i = 0; i < size; i++) {
			x[0] = result[i];
			resultArray[arrayCounter] = x.clone(); // 単体出しのカードを格納
			arrayCounter++;
		}
		// ペア出しの格納
		for (int i = 2; i < 5; i++) {// ペア出しは2～5枚しか存在しないため
			cloneArray = searchGroupMeld(i);
			if (cloneArray == null) {// 何も入っていない場合
				break;
			}
			size = cloneArray.length;
			// resultArrayに格納してあげる
			for (int j = 0; j < size; j++) {
				resultArray[arrayCounter] = cloneArray[j];
				arrayCounter++;
			}
		}
		// 階段出しの格納
		int num = cardNum / players + 1;
		for (int i = 3; i < num; i++) {
			cloneArray = searchSequenceMeld(i);
			if (cloneArray == null)// 役が何も入っていない場合
				break;
			size = cloneArray.length;
			for (int j = 0; j < size; j++) {
				resultArray[arrayCounter] = cloneArray[j];
				arrayCounter++;
			}
		}
		num = (int) (Math.random() * arrayCounter);// ランダムで出す手を考慮する

		return resultArray[num].clone();
	}

	/**
	 * 手札を生成するメソッド
	 *
	 * @return 手札の配列を返す
	 */
	private int[][] initHands(MyData md, FieldData fd) {
		int[][] resultHands = new int[players][cardNum];

		ArrayList<Integer> notLookCards = new ArrayList<Integer>();// まだ見えていないカード群を格納

		int[] feild = md.getField();// 場の残っているカードの配列をコピってあげる

		// 初期化
		for (int i = 0; i < cardNum; i++) {
			for (int j = 0; j < players; j++) {
				resultHands[j][i] = 0;
			}
			if (feild[i] == 1)// まだ見えていないカードだった場合
				notLookCards.add(i);
		}
		int arraySize = 0;
		int seatSize = 0;
		int random = 0;

		for (int i = 0; i < players; i++) {
			if (i == mySeat) {// 自分の座席の時
				resultHands[i] = md.getMyHand();// 自分の手をそのまま入れてあげる
			} else {// それ以外
				seatSize = fd.getSeatsHandSize(i);// 座席のカード枚数を登録
				// カードの枚数分ランダムに入れてあげる
				for (int j = 0; j < seatSize; j++) {
					arraySize = notLookCards.size();// 見えていないカードの合計枚数
					random = (int) (Math.random() * arraySize);// ランダムで抜き出すカードを入れる

					resultHands[i][notLookCards.remove(random)] = 1;
				}
			}
		}
		return resultHands;
	}

	/**
	 * プレイヤーごとに種類別に分けたカードの枚数を返す
	 *
	 * @return　種類別に分けたカードの枚数を返す
	 */
	private int[][] getTypeOfHandsOfCards() {
		int[][] result = new int[players][14];
		// 初期化
		for (int i = 0; i < players; i++) {
			for (int j = 0; j < 14; j++) {
				result[i][j] = 0;
			}
		}
		int counter = 0;// カードの枚数をカウントする
		// 探索部分
		for (int i = 0; i < players; i++) {
			if (playerHands[i][0] == 1)// JOKERを持っている時
				result[i][0] = 1;

			for (int j = 1; j < 14; j++) {// カードの数字の枚数
				for (int l = 0; l < 4; l++) {// カードの種類
					if (playerHands[i][j + l * 13] == 1)// もしカードが存在する時
						counter++;
				}
				result[i][j] = counter;// 枚数記憶させる
				counter = 0;
			}
		}

		return result;
	}

	/**
	 * 出せる手の初期化
	 *
	 * @return　初期化された配列
	 */
	private int[] initPutNumberOfHand() {
		// TODO いらない気がする
		int[] result = new int[4];
		for (int i = 0; i < 4; i++) {
			if (i == 3) {
				result[i] = 1;// PASSの時
			} else {
				result[i] = 0;
			}
		}
		return result;
	}

	/**
	 * 出せる役があるかどうかの探索し、ある場合はその役のArrayListを返す
	 *
	 * @param BotSkelton
	 *            bs
	 * @return ArrayList<MeldData> 出せるMeldData群を返す
	 */
	private static ArrayList<MeldData> searchOfMeld(BotSkeleton bs) {

		Meld placeMeld = bs.place().lastMeld();

		Cards myhand = bs.hand();

		ArrayList<MeldData> arrayMeld = new ArrayList<MeldData>();

		// JOKER単体出しの時にスぺ3を出す
		if (myhand.contains(Card.S3)) {// スペードの3を持っているか否か
			if (placeMeld != null) {// renewじゃない時
				if (placeMeld.type() == Meld.Type.SINGLE) {// 1枚出しの時
					if (placeMeld.rank() == Rank.JOKER_HIGHEST
							|| placeMeld.rank() == Rank.JOKER_LOWEST) {// JOKERの時
						Cards C3 = Cards.EMPTY_CARDS;
						C3 = C3.add(Card.C3);
						Melds meldC3 = Melds.parseSingleMelds(C3);
						arrayMeld.add(new MeldData(meldC3.get(0)));
						arrayMeld.add(new MeldData());
						return arrayMeld;
					}
				}
			}
		}

		Melds resultMelds = Melds.parseMelds(myhand);

		Suits lockSuits = bs.place().lockedSuits();// 縛りの役

		if (myhand.contains(Card.JOKER)) {// 自分の手札にJOKERが存在する時

			// jokerの時の複数枚のカードの束を除去

			Melds jokerSingleMelds = null;// jokerの一枚出しのカード群

			Melds jokerGroupMelds = Melds.parseGroupMelds(myhand);// group出しのjoker群の処理

			Cards jokers = Cards.EMPTY_CARDS;

			jokers = jokers.add(Card.JOKER);

			jokerSingleMelds = Melds.parseSingleMelds(jokers);

			resultMelds = resultMelds.remove(jokerSingleMelds);

			resultMelds = resultMelds.remove(jokerGroupMelds);

			if (!bs.place().isReverse()) { // 革命しているかどうか
				jokerSingleMelds = jokerSingleMelds.extract(Melds.MAX_RANK);
			} else {
				jokerSingleMelds = jokerSingleMelds.extract(Melds.MIN_RANK);
			}

			if (placeMeld != null && placeMeld.type() == Meld.Type.SINGLE) {// 場にカードが出されていてかつ一枚出しの時
				jokerSingleMelds = jokerSingleMelds.extract(Melds
						.suitsOf(placeMeld.suits()));
			}
			if (placeMeld != null && placeMeld.type() == Meld.Type.GROUP
					&& lockSuits != Suits.EMPTY_SUITS) {// 場にカードが出されていてかつ複数枚出しの時かつ縛りの時
				jokerGroupMelds = jokerGroupMelds.extract(Melds
						.suitsOf(lockSuits));
				if (jokerGroupMelds != Melds.EMPTY_MELDS)// 出せる役がある場合
					resultMelds = resultMelds.add(jokerGroupMelds);
			} else {// 縛りとかが一切ない場合
				int counter = 0;
				for (Meld meld : jokerGroupMelds) {
					if (counter % 4 == 0)
						resultMelds = resultMelds.add(meld);
					counter++;
				}
			}
			resultMelds = resultMelds.add(jokerSingleMelds.get(0));// jokerの役を入れてあげる
		}

		if (placeMeld != null) {// 場にカードが置かれている時
			// 役のサイズ比較
			resultMelds = resultMelds.extract(Melds.sizeOf(placeMeld.asCards()
					.size()));
			// 役の形比較
			resultMelds = resultMelds.extract(Melds.typeOf(placeMeld.type()));

			// 役の大小比較
			if (!bs.place().isReverse()) { // 革命しているかどうか
				resultMelds = resultMelds.extract(Melds.rankOver(placeMeld
						.rank()));
			} else {
				resultMelds = resultMelds.extract(Melds.rankUnder(placeMeld
						.rank()));
			}
			// 縛りの有無の確認
			if (lockSuits != Suits.EMPTY_SUITS) {
				resultMelds = resultMelds.extract(Melds.suitsOf(lockSuits));
			}

		}
		if (resultMelds.size() == 0) {// 出せる役が存在しない時
			return null;
		}
		for (Meld meld : resultMelds) {// それぞれの役データを格納
			arrayMeld.add(new MeldData(meld));
		}
		/** 場にカードが存在する時はPASSは入れるがrenew時はPASSを入れない **/
		if (placeMeld != null)// 場にカードが存在する時
			arrayMeld.add(new MeldData());// PASSを格納

		return arrayMeld;
	}
}
