package simulationBransing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import jp.ac.uec.daihinmin.Place;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import monteCalro.FieldData;
import monteCalro.MyData;
import monteCalro.State;
import monteCalro.Utility;
import object.DataConstellation;
import object.InitSetting;
import object.ObjectPool;

/***
 * ゲームのフィールドの状態を格納する場
 *
 * @author 伸也
 *
 */
public class GameField implements Cloneable {

	private static final int PLAYERS = 5;// プレイヤーの数

	private static final int CARDNUM = 53; // カード枚数

	private static final int SUITSNUM = 4; // suitの数

	private static long rank_3;// rank3のすべての数

	private static final long ONE = 1;

	private static final long THIRTEEN = 8191;

	private int mySeat;// 自分の座席番号

	private int myGrade; // 自分のランク

	/**** 木構造を取るための変数群 ***/
	/** UCBの値 ***/
	private double UCB;
	/** 訪問回数 ***/
	private int visit;
	/** 出したカードの役 **/
	private long yaku;

	/** 勝利した勝ち点 ***/
	private int won;

	/*** 何の子供を持っているかどうかの識別子　0 は持っていない ***/
	private int HaveChildNumber;

	private boolean canGrowUpTree = true;

	private State state = State.EMPTY;// 初期状態の変数

	/** 縛りのマーク 4bit */
	private int lockNumber;
	/*** 場のマーク　4bit **/
	private int placeSuits;
	/** 勝利したプレイヤー　5bit **/
	private int wonPlayer;
	/*** 最初に勝った状態になっているプレイヤー 5bit **/
	private int firstWonPlayer;
	/** PASSしたかどうかの判定用の変数 5bit ***/
	private int passPlayer;

	private int point_5;
	private int point_4;
	private int point_3;
	private int point_2;
	private int point_1;

	/** 座席のランク 25bit 今は使ってない **/
	private int grade;

	/*** 見えていないカード　long 53bit **/
	private long notLookCards;

	/** 3～2の数を1～13で表したもの -1がrenew **/
	private int rank;// 場に出されているランク

	/** 革命している時はtrue **/
	private boolean reverse;// 革命しているか否か

	private int numberOfCardSize;// 場に出されているカードの枚数

	private int turnPlayer; // ターンプレイヤー

	private int putLastPlayer;// 最後に出した人の座席番号

	// プレイヤーの手札 53bit× players
	private long[] playersHands;

	/**
	 * コンストラクタ
	 *
	 * @param playerNum
	 * @param bs
	 * @param fd
	 * @param md
	 */
	public GameField(int playerNum, BotSkeleton bs, FieldData fd, MyData md) {

		rank_3 = ONE;
		for (int i = 0; i < 3; i++) {
			rank_3 = rank_3 | rank_3 << (i * 13);
		}
		rank_3 = rank_3 << 1;

		playersHands = ObjectPool.getPLayersHands();
		int counter = 0;
		firstWonPlayer = 0;
		for (boolean flag : fd.getWonPlayer()) {
			if (flag)
				firstWonPlayer = firstWonPlayer | (1 << counter);
			counter++;
		}
		wonPlayer = firstWonPlayer;
		counter = 0;
		for (int num : fd.getGrade()) {
			if (mySeat == num)
				myGrade = num;
			grade = grade | 1 << (counter * 5 + num - 1);
			counter++;
		}

		mySeat = md.getSeat();

		turnPlayer = mySeat;

		init(bs, fd);
	}

	/**
	 * 一番最初に行う初期化
	 *
	 * @param playerNum
	 * @param bs
	 * @param fd
	 * @param md
	 */
	public void firstInit(int playerNum, BotSkeleton bs, FieldData fd, MyData md) {
		initChild();
		rank_3 = ONE;
		for (int i = 0; i < 3; i++) {
			rank_3 = rank_3 | rank_3 << (i * 13);
		}
		rank_3 = rank_3 << 1;
		playersHands = ObjectPool.getPLayersHands();
		int counter = 0;
		firstWonPlayer = 0;
		for (boolean flag : fd.getWonPlayer()) {
			if (flag)
				firstWonPlayer = firstWonPlayer | (1 << counter);
			counter++;
		}
		wonPlayer = firstWonPlayer;
		counter = 0;
		grade = 0;
		for (int num : fd.getGrade()) {
			if (mySeat == counter)
				myGrade = num;
			grade = grade | 1 << (counter * 5 + num - 1);
			counter++;
		}
		mySeat = md.getSeat();

		turnPlayer = mySeat;

		init(bs, fd);

	}

	/**
	 * UCBの値を更新する
	 *
	 * @param playout
	 *            プレイアウトの回数
	 * @param winPoint
	 *            勝ち点
	 * @param update
	 *            更新するかどうか
	 */
	public void upDateUCB(int playout, int winPoint, boolean update) {
		if (update) {
			visit++;
			won += winPoint;
			switch (winPoint) {
			case 1:
				point_1++;
				break;
			case 2:
				point_2++;
				break;
			case 3:
				point_3++;
				break;
			case 4:
				point_4++;
				break;
			case 5:
				point_5++;
				break;
			default:
				System.out.println("error発生");
				break;
			}
		}
		UCB = Caluculater.calcUCB_TUNED(playout, this);
	}

	/**
	 * 子供の時に行う初期化
	 */
	public void initChild() {
		int w = notWonPLayers();
		for (int i = 1; i <= w; i++) {
			UCB += i;
		}
		UCB = UCB / w;

		visit = 0;
		won = 0;
		HaveChildNumber = 0;
		point_1 = 0;
		point_2 = 0;
		point_3 = 0;
		point_4 = 0;
		point_5 = 0;
	}

	public double getWinPoint() {
		return (double) won / (double) visit;
	}

	/**
	 * 順位ごとの勝利した回数を返すメソッド
	 *
	 * @param num
	 * @return
	 */
	public int returnWinPoints(int num) {
		switch (num) {
		case 1:
			return point_1;
		case 2:
			return point_2;
		case 3:
			return point_3;
		case 4:
			return point_4;
		case 5:
			return point_5;
		default:
			break;
		}
		return 0;
	}

	/**
	 * 空のコンストラクタ
	 */
	public GameField() {

	}

	@Override
	public GameField clone() {
		GameField gf;
		try {
			gf = (GameField) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
		gf.playersHands = ObjectPool.getPLayersHands();
		for (int i = 0; i < PLAYERS; i++) {
			gf.playersHands[i] = this.playersHands[i];
		}
		return gf;

	}

	/**
	 * 見えていないカードの初期化
	 */
	public void initNotLookCard() {
		long num = 0;
		for (int i = 0; i < PLAYERS; i++) {
			num = num | playersHands[i];
		}
		notLookCards = num;

	}

	/**
	 *
	 * @param copyHands
	 *            自分の手札群
	 */
	public void initPLaceGameFiled(int[][] copyHands) {
		long num;
		for (int i = 0; i < PLAYERS; i++) {
			num = 0;
			for (int j = 0; j < CARDNUM; j++) {
				if (copyHands[i][j] == 1)
					num = num | (ONE << j);
			}
			playersHands[i] = num;
		}
		initNotLookCard();// 場に出ているカード以外を格納

	}

	/**
	 *
	 * @param copyHands
	 *            自分の手札群
	 */
	public void initPLaceGameFiled(long[] copyHands) {
		playersHands = ObjectPool.getPLayersHands();
		for (int i = 0; i < PLAYERS; i++) {
			playersHands[i] = copyHands[i];
		}
		initNotLookCard();// 場に出ているカード以外を格納

	}

	/**
	 * プレイヤーの手札の枚数を返すメソッド
	 *
	 * @param playerNum
	 *            　プレイヤー番号
	 * @return
	 */
	public int turnPLayerHaveHand(int playerNum) {
		return Long.bitCount(playersHands[playerNum]);
	}

	/**
	 * プレイヤーの持っているカードのランクの枚数を返すメソッド
	 *
	 * @param rank
	 *            ランク
	 * @param playerum
	 *            プレイヤーの人数
	 * @return
	 */
	public int turnPLayerTypeOfHandsOfCards(int rank, int playerum) {
		long num = 0;
		if ((!reverse && rank == 14) || (reverse && rank == 0)) {// jokerの時
			num = 1;
		} else {
			num = rank_3 << (rank - 1);
		}
		num = num & playersHands[playerum];
		return Long.bitCount(num);
	}

	/**
	 * 棋譜データを復元するメソッド
	 *
	 * @param 棋譜データ
	 */
	public double restoreGameRecord(char[] data) {
		double point = 0;
		int counter = 2;
		mySeat = Character.getNumericValue(data[counter]);
		counter += 4;
		int num = 0;
		// 全員のカードの処理
		for (int i = 0; i < PLAYERS; i++) {
			playersHands[i] = 0;
		}
		notLookCards = 0;
		for (int i = 0; i < 53; i++) {
			num = Character.getNumericValue(data[counter]);
			if (num != 9) {
				playersHands[num] = playersHands[num] | (ONE << i);
				notLookCards = notLookCards | (ONE << i);
			}
			counter++;
		}
		// lockNumber
		lockNumber = 0;
		for (int i = 0; i < SUITSNUM; i++) {
			num = Character.getNumericValue(data[counter]);
			if (num == 1) {
				lockNumber = lockNumber | (1 << i);
			}
			counter++;
		}
		// PLaceSuits
		placeSuits = 0;
		for (int i = 0; i < SUITSNUM; i++) {
			num = Character.getNumericValue(data[counter]);
			if (num == 1) {
				placeSuits = placeSuits | (1 << i);
			}
			counter++;
		}
		// rank
		num = Character.getNumericValue(data[counter]);
		rank = num * 10;
		counter++;
		num = Character.getNumericValue(data[counter]);
		rank = num;
		counter++;
		// 革命
		num = Character.getNumericValue(data[counter]);
		if (num == 1) {
			reverse = true;
		} else {
			reverse = false;
		}
		counter++;
		// numberOfCard
		numberOfCardSize = Character.getNumericValue(data[counter]);
		counter++;
		// turnPLayer
		num = Character.getNumericValue(data[counter]);
		if (num == 9) {
			turnPlayer = -1;
		} else {
			turnPlayer = num;
		}
		counter++;
		// putLastPLayer
		num = Character.getNumericValue(data[counter]);
		if (num == 9) {
			putLastPlayer = -1;
		} else {
			putLastPlayer = num;
		}
		counter++;
		// state
		num = Character.getNumericValue(data[counter]);
		switch (num) {
		case 0:
			state = State.RENEW;
			if (!reverse) {
				rank = 0;
			} else {
				rank = 14;
			}

			lockNumber = 0;
			placeSuits = 0;
			numberOfCardSize = 0;
			break;
		case 1:
			state = State.SINGLE;
			break;
		case 2:
			state = State.GROUP;
			break;
		case 3:
			state = State.SEQUENCE;
			break;
		default:
			state = State.RENEW;
			break;
		}
		counter++;
		// passPLayer

		passPlayer = 0;
		for (int i = 0; i < PLAYERS; i++) {
			num = Character.getNumericValue(data[counter]);
			if (num == 1) {
				passPlayer = passPlayer | (1 << i);
			}
			counter++;
		}
		firstWonPlayer = 0;
		wonPlayer = 0;
		// firstWonPLayer
		for (int i = 0; i < PLAYERS; i++) {
			num = Character.getNumericValue(data[counter]);

			if (num == 1) {
				firstWonPlayer = firstWonPlayer | (1 << i);
			}
			counter++;
		}
		wonPlayer = firstWonPlayer;
		passPlayer = passPlayer | wonPlayer;
		// points
		for (int i = 0; i < 6; i++) {
			num = Character.getNumericValue(data[counter]);
			point += num / Math.pow(10, i);
			counter++;
		}

		return point;
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

		state = getState(lastMeld);// 役を判定する
		lockNumber = 0;
		int counter = 0;
		// 場のマークを取り出す
		counter = 0;
		placeSuits = 0;
		for (boolean flag : Utility.meldParseSuitsOfBoolean(lastMeld)) {
			if (flag) {
				placeSuits = placeSuits | (1 << counter);
			}
			counter++;
		}
		// 縛りが存在するかどうかのチェック
		if (place.lockedSuits() != Suits.EMPTY_SUITS) {
			lockNumber = placeSuits;
		}

		// 革命しているかどうか
		reverse = place.isReverse();
		// 場に出されているカードのランク
		// 場に出されているカードの枚数の代入
		if (lastMeld != null) {
			rank = lastMeld.rank().toInt() - 2;
			numberOfCardSize = lastMeld.asCards().size();

		} else {
			if (!reverse) {
				rank = 0;// rankがフィールドに存在しない時
			} else {
				rank = 14;
			}
			numberOfCardSize = 0;
		}

		// wonPlayerの更新
		counter = 0;
		wonPlayer = 0;
		for (boolean flag : fd.getWonPlayer()) {
			if (flag)
				wonPlayer = wonPlayer | (1 << counter);
			counter++;
		}

		// PASSしたプレイヤーをの初期状態の格納
		counter = 0;
		passPlayer = wonPlayer;
		for (boolean flag : fd.getPassPlayer()) {
			if (flag)
				passPlayer = passPlayer | (1 << counter);
			counter++;
		}

		// LastPLayerの座席番号を格納
		putLastPlayer = fd.getPutLastPlayer();

	}

	/**
	 * Meldクラスから場に出されている役の種類を更新する
	 *
	 * @param lastMeld
	 *            Meld
	 * @return その状態
	 */
	private State getState(Meld lastMeld) {
		State result = State.EMPTY;
		// 場のカードの状態を保存
		if (lastMeld == null) {// renewの時
			result = State.RENEW;
		} else if (lastMeld.type() == Meld.Type.SINGLE) {// 1枚出しの時
			result = State.SINGLE;
		} else if (lastMeld.type() == Meld.Type.GROUP) {// 複数枚出しの時
			result = State.GROUP;
		} else {// 階段の時
			result = State.SEQUENCE;
		}

		return result;
	}

	/**
	 * 場が流れた時に呼び出すメソッド
	 */
	public void renew() {
		state = State.RENEW;
		// 場に出ているカードのランクの初期化
		if (!reverse) {
			rank = 0;
		} else {
			rank = 14;
		}
		numberOfCardSize = 0;// カードの大きさを初期化
		// PASSプレイヤーの更新
		passPlayer = wonPlayer;

		placeSuits = 0;
		lockNumber = 0;
	}

	/**
	 * ゲーム終了時のポイントを返す
	 *
	 * @return point
	 */
	public int returnWinPoint() {
		int num = 6 - Integer.bitCount(wonPlayer);
		if ((wonPlayer & (1 << mySeat)) == 0)
			num = 1;
		return num;
	}

	/**
	 * すべてのプレイヤーをPASSにする
	 */
	public void allPlayerDoPass() {
		passPlayer = 31; // all 11111
	}

	/**
	 * 勝ったプレイヤーを判定する 自分のプレイヤーが上がった時点で終了
	 *
	 * @returnゲームを終了するかどうか
	 */
	public boolean checkGoalPlayer() {
		boolean result = false;// ゲームを終了するかどうかの判定
		int num = 0;
		// 勝ちプレイヤーの更新部
		for (int i = 0; i < PLAYERS; i++) {
			num = (1 << i);
			if ((turnPLayerHaveHand(i) == 0) && ((num & wonPlayer) == 0)) {
				wonPlayer = wonPlayer | num;
			}
		}
		passPlayer = passPlayer | wonPlayer;
		// 自分が勝った時の判定
		num = (1 << mySeat); // 自分の座席
		if ((num & wonPlayer) >= 1) {
			result = true;
		}
		// 自分以外が勝った時の判定
		num = 31 ^ num;
		if ((wonPlayer & num) == num)
			result = true;

		return result;
	}

	/**
	 * renewするかの判定 trueの時はrenewする、falseの時はしない
	 *
	 * @return renewするかどうか
	 */
	public boolean checkRenew() {
		boolean result = false;
		int notPassPlayer = PLAYERS - Integer.bitCount(passPlayer); // PASSしたプレイヤーの数を数える

		if (notPassPlayer <= 1)// パスしていないプレイヤーが1人以下の時
			result = true;

		return result;
	}

	/**
	 * ターンプレイヤーの更新 renewの時の処理を含む
	 */
	public void updateTurnPlayer() {
		if (state == State.RENEW) {// renewした時の判定
			/** renewした時は最後に置いたプレイヤーをターンプレイヤーにする **/
			if (((1 << turnPlayer) & wonPlayer) >= 1) {
				turnPlayer++;
				if (turnPlayer >= PLAYERS)// プレイヤーの人数把握
					turnPlayer = 0;
			} else {
				turnPlayer = putLastPlayer;
			}
		} else {// renew以外
			turnPlayer++;
			if (turnPlayer >= PLAYERS)// プレイヤーの人数把握
				turnPlayer = 0;
		}
	}

	/**
	 * 木の成長を行うかどうかの判定
	 *
	 * @return 木の成長を行うか否か
	 */
	public boolean doGrowUpTree() {
		if (!canGrowUpTree)// 木が成長できないような木構造の場合
			return false;
		if (HaveChildNumber != 0)// 子供を持っている時の場合
			return false;
		if (visit >= InitSetting.THRESHOLD - 1) {// 訪問回数が閾値を超えていた時
			return true;
		}
		return false;
	}

	/**
	 * ターンの終わりの処理を行うメソッド
	 */
	public void endTurn() {
		// 8切りの判定
		if (checkEight())
			allPlayerDoPass();// すべてのプレイヤーをパスにする

		if (checkRenew()) {// renewするかどうかの判定
			renew();
		}
		updateTurnPlayer(); // ターンプレイヤーの更新
	}

	/**
	 * 8切り出来るかどうかの判定
	 *
	 * @return
	 */
	public boolean checkEight() {
		boolean result = false;
		if (state != State.SEQUENCE) { // 階段以外の時
			if (rank == 6)
				result = true;
		} else {
			int num = rank;
			if (!reverse) {// 革命じゃない時
				for (int i = 0; i < numberOfCardSize; i++) {
					if (num == 6) {// 8のカードの時
						result = true;
						break;
					}
					num++;
				}
			} else {// 革命の時
				for (int i = 0; i < numberOfCardSize; i++) {
					if (num == 6) {// 8のカードの時
						result = true;
						break;
					}
					num--;
				}
			}
		}
		return result;
	}

	/**
	 * 場の更新を行うメソッド
	 *
	 * @param putHand
	 *            場に出された役
	 */
	public void updatePlace(long putHand) {
		yaku = putHand;
		if (putHand == 0) {
			passPlayer = passPlayer | (1 << turnPlayer);
			return;
		}
		int num = 0;
		// カードサイズの更新
		numberOfCardSize = Long.bitCount(putHand);
		// 最後に出した人の更新
		putLastPlayer = turnPlayer; // 最後に出した人を更新

		if (state != State.RENEW && (rank == 0 || rank == 14)) {
			putLastPlayer = turnPlayer;
			notLookCards = notLookCards ^ putHand;
			playersHands[turnPlayer] = playersHands[turnPlayer] ^ putHand;
			allPlayerDoPass();
			return;
		}

		// 役のランクを更新
		if (((putHand & (ONE << 0)) >= 1) && numberOfCardSize == 1) {// joker単体出しの時の処理
			if (!reverse) {
				rank = 14;
			} else {
				rank = 0;
			}
		} else {
			if (!reverse) {
				long number = 0;
				for (int i = 1; i < 14; i++) {
					number = rank_3 << (i - 1);
					number = number & putHand;
					if (Long.bitCount(number) >= 1) {
						rank = i;
						break;
					}
				}
			} else {
				long number = 0;
				for (int i = 13; i >= 1; i--) {
					number = rank_3 << (i - 1);
					number = number & putHand;
					if (Long.bitCount(number) >= 1) {
						rank = i;
						break;
					}
				}

			}
		}
		if (state == State.RENEW) {// Renewの時の更新部
			state = getState(putHand, numberOfCardSize, rank);

			boolean joker = false;

			long oneKindOfCard = THIRTEEN << 1;

			if ((putHand & (ONE << 0)) != 0) {
				joker = true;
			}
			long number = 0;
			placeSuits = 0;
			lockNumber = 0;
			for (int i = 0; i < 4; i++) {
				number = 0;
				number = oneKindOfCard & putHand;
				if (Long.bitCount(number) >= 1) {
					placeSuits = placeSuits | (1 << i);
				}
				oneKindOfCard = oneKindOfCard << 13;
			}
			// jokerがあった時はランダムでマークをあてる
			if (joker) {// jokerがあった時の処理
				for (int i = 0; i < 4; i++) {
					num = 1 << i;
					if ((placeSuits & num) == 0) {
						placeSuits = placeSuits | num;
						break;
					}
				}
			}
		} else {// それ以外の時の更新部
			if (lockNumber == 0) {// 縛りが存在しない時
				int cloneLock = 0;
				// 今の役のマークを格納する
				boolean joker = false;
				long oneKindOfCard = THIRTEEN << 1;

				if ((putHand & (ONE << 0)) != 0) {
					joker = true;
				}
				long number;
				for (int i = 0; i < 4; i++) {
					number = 0;
					number = oneKindOfCard & putHand;
					if (Long.bitCount(number) >= 1) {
						cloneLock = cloneLock | (1 << i);
					}
					oneKindOfCard = oneKindOfCard << 13;
				}
				boolean result = true;
				// 場の役と今の役を比べる
				for (int i = 0; i < 4; i++) {
					num = (1 << i);
					if ((placeSuits & num) != 0) { // 前の状態にその役が存在する時
						if ((cloneLock & num) != 0) { // その盤面にも存在する時
							continue;
						} else if (joker) {
							joker = false;
							cloneLock = cloneLock | num;
							continue;
						}
					}
					result = false;
					break;
				}
				if (result) { // 縛りが成立する場合
					lockNumber = placeSuits;
				}
				placeSuits = cloneLock;
			} else {// 縛りが存在する時
				placeSuits = lockNumber;
			}
		}

		// 革命の判定 (電通大のルール参照)
		if (state == State.GROUP) {
			if (numberOfCardSize >= 4)
				reverse = !reverse;
		} else if (state == State.SEQUENCE) {
			if (numberOfCardSize >= 5)
				reverse = !reverse;
		}
		putLastPlayer = turnPlayer;
		// 手札から指定した枚数とカードを抜く
		notLookCards = notLookCards ^ putHand;
		playersHands[turnPlayer] = playersHands[turnPlayer] ^ putHand;
	}

	/**
	 * 役の大きさと使ったカードから役の出し方を判定し返すメソッド
	 *
	 * @param putHand
	 *            　出したカードの配列
	 * @return 出された役の出し方を返す
	 */
	private State getState(long putHand, int size, int rank) {
		State r = State.EMPTY;
		if (size == 1) {// カードが一枚の時
			r = State.SINGLE;
		} else if (size == 2) {
			r = State.GROUP;
		} else {
			long num = rank_3 << (rank - 1);
			num = num & putHand;
			if (Long.bitCount(num) >= 2) {
				r = State.GROUP;
			} else {
				r = State.SEQUENCE;
			}
		}
		return r;
	}

	/**
	 * 単体出しの出せるカードを探索 (RENEWでも使える)
	 *
	 * @return 出せるすべての役
	 */
	private ArrayList<Long> searchSingleMeld(ArrayList<Long> list) {
		// スペ3の判定
		long playerHand = playersHands[turnPlayer];
		if (state != State.RENEW && (rank == 14 || rank == 0)) {// JOKERが場に出されている時
			if ((playerHand & (ONE << 1)) != 0) { // スぺ3を持っている時
				list.add((ONE << 1));
				return list;
			}
		}
		if ((playerHand & (ONE << 0)) != 0) {// jokerを持っている時
			list.add((ONE << 0));
		}
		long num = 0;
		if (!reverse) {// 普通の時
			for (int i = rank + 1; i < 14; i++) {
				if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) != 0)// 1枚以上存在する時
					for (int j = 0; j < 4; j++) {
						if (lockNumber != 0 && (lockNumber & (1 << j)) == 0)
							continue;
						num = ONE << (i + j * 13);
						if ((playerHand & num) != 0) {// もしそのカードを持っている時
							list.add(num);
						}
					}
			}
		} else {// 革命の時
			for (int i = rank - 1; i > 0; i--) {
				if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) != 0)// 1枚以上存在する時
					for (int j = 0; j < 4; j++) {
						if (lockNumber != 0 && (lockNumber & (1 << j)) == 0)
							continue;
						num = ONE << (i + j * 13);
						if ((playerHand & num) != 0) {// もしそのカードを持っている時
							list.add(num);
						}
					}
			}
		}
		return list;
	}

	/**
	 * ペア出しの役を探索するメソッド 役が入っていない時nullを返す
	 *
	 * @param size
	 *            Meldのサイズ
	 * @return 役の集合を返す int[必要なもの][出せる役]
	 */
	private ArrayList<Long> searchGroupMeld(ArrayList<Long> list, int size) {
		boolean joker = false; // jokerを持っているかどうか
		long playerHand = playersHands[turnPlayer];
		if ((playerHand & (ONE << 0)) != 0) {// jokerを持っている時
			joker = true;
		}
		boolean lock = false;
		if (lockNumber != 0) {
			lock = true;
		}
		long num = 0;
		if (!reverse) {// 革命が起きていない時
			for (int i = rank + 1; i < 14; i++) {
				if (!joker) {
					if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) >= size) {// ペア出し出来る
						num = 0;
						for (int j = 0; j < SUITSNUM; j++) {
							if (lock && (lockNumber & (1 << j)) == 0) {
								continue;
							}
							num = num | (ONE << (j * 13 + i));
						}
						num = num & playerHand;
						if (lock && Long.bitCount(num) != size)
							continue;
						list.add(num);
					}
				} else {
					if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) >= size) {// ペア出し出来る
						num = 0;
						for (int j = 0; j < SUITSNUM; j++) {
							if (lock && (lockNumber & (1 << j)) == 0) {
								continue;
							}
							num = num | (ONE << (j * 13 + i));
						}
						num = num & playerHand;
						if (lock && Long.bitCount(num) != size)
							continue;
						list.add(num);
					} else if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) >= size - 1) {
						num = ONE << 0;// jokerを表現
						for (int j = 0; j < SUITSNUM; j++) {
							if (lock && (lockNumber & (1 << j)) == 0) {
								continue;
							}
							num = num | (ONE << (j * 13 + i));
						}
						num = num & playerHand;
						if (lock && Long.bitCount(num) != size)
							continue;
						list.add(num);
					}
				}
			}
		} else {// 革命の時
			for (int i = rank - 1; i > 0; i--) {
				if (!joker) {
					if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) >= size) {// ペア出し出来る
						num = 0;
						for (int j = 0; j < SUITSNUM; j++) {
							if (lock && (lockNumber & (1 << j)) == 0) {
								continue;
							}
							num = num | (ONE << (j * 13 + i));
						}
						num = num & playerHand;
						if (lock && Long.bitCount(num) != size)
							continue;
						list.add(num);
					}
				} else {
					if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) >= size) {// ペア出し出来る
						num = 0;
						for (int j = 0; j < SUITSNUM; j++) {
							if (lock && (lockNumber & (1 << j)) == 0) {
								continue;
							}
							num = num | (ONE << (j * 13 + i));
						}
						num = num & playerHand;
						if (lock && Long.bitCount(num) != size)
							continue;
						list.add(num);
					} else if (turnPLayerTypeOfHandsOfCards(i, turnPlayer) >= size - 1) {
						num = ONE << 0;// jokerを表現
						for (int j = 0; j < SUITSNUM; j++) {
							if (lock && (lockNumber & (1 << j)) == 0) {
								continue;
							}
							num = num | (ONE << (j * 13 + i));
						}
						num = num & playerHand;
						if (lock && Long.bitCount(num) != size)
							continue;
						list.add(num);
					}
				}
			}
		}
		return list;
	}

	/**
	 * カード枚数から階段を探索するメソッド
	 *
	 * @param cardSize
	 *            カードの大きさ
	 * @return カードが入った配列を返す nullの場合PASS
	 */
	private ArrayList<Long> searchSequenceMeld(ArrayList<Long> list, int cardSize) {
		if (cardSize >= 7) {// 7枚出し以上は見ない
			return list;
		}
		boolean joker = false;
		long playerHand = playersHands[turnPlayer];
		if ((playerHand & (ONE << 0)) != 0) {// jokerを持っている時
			joker = true;
		}
		// 階段になりうるカードのペア群を作る
		long sequence = 0;
		for (int i = 0; i < cardSize; i++) {
			sequence = sequence | (ONE << i);
		}
		int defalt = 0;
		if (state == State.RENEW) {
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
		long num = 0;
		boolean lock = false;
		if (lockNumber != 0) {
			lock = true;
		}
		if (!reverse) {// 普通の時
			for (int i = defalt; i < 15 - cardSize; i++) {// カードでの探索
				for (int j = 0; j < SUITSNUM; j++) {
					if (lock && (lockNumber & (1 << j)) == 0)
						continue;
					num = 0;
					num = num | (sequence << (i + j * 13));
					num = num & playerHand;
					if (Long.bitCount(num) == cardSize) {
						list.add(num);
					}
					if (joker) {
						num = num | (ONE << 0);
						if (Long.bitCount(num) == cardSize)
							list.add(num);
					}
				}
			}
		} else {// 革命の時
			for (int i = defalt; i > cardSize - 1; i--) {// カードでの探索
				for (int j = 0; j < SUITSNUM; j++) {
					if (lock && (lockNumber & (1 << j)) == 0)
						continue;
					num = 0;
					num = num | (sequence << (i + j * 13 - cardSize + 1));
					num = num & playerHand;
					if (Long.bitCount(num) == cardSize) {
						list.add(num);
					}
					if (joker) {
						num = num | (ONE << 0);
						if (Long.bitCount(num) == cardSize) {
							list.add(num);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * renew時の出せる全ての役を返すメソッド
	 *
	 * @return 出せるすべての役の配列を返す
	 */
	private ArrayList<Long> returnAllResult_renewMeld(ArrayList<Long> list) {
		list = searchSingleMeld(list);// 出せる役の結果
		// ペア出しの格納
		for (int i = 2; i < 5; i++) {// ペア出しは2～5枚しか存在しないため
			list = searchGroupMeld(list, i);
		}
		// 階段出しの格納
		int num = turnPLayerHaveHand(turnPlayer);
		for (int i = 3; i < num; i++) {
			list = searchSequenceMeld(list, i);
		}
		return list;
	}

	/**
	 * その場で出せる手を返すメソッド
	 *
	 * @return
	 */
	public ArrayList<Long> getPutHand() {
		ArrayList<Long> list = ObjectPool.getPutHand();
		if (((1 << turnPlayer) & passPlayer) == 0) {// そのプレイヤーがパスの状態だった場合
			switch (state) {
			case SINGLE:// 単体出しの時
				list = searchSingleMeld(list);// 単体出しで出す役を受け取る\
				list.add((long) 0); // PASS
				break;
			case GROUP:// ペア出しの時
				list = searchGroupMeld(list, numberOfCardSize);// ペア出しで出す役を受け取る
				list.add((long) 0); // PASS
				break;
			case SEQUENCE:// 階段出しの時
				list = searchSequenceMeld(list, numberOfCardSize);// ペア出しで出す役を受け取る
				list.add((long) 0); // PASS
				break;
			case RENEW:// renewの時
				list = returnAllResult_renewMeld(list);// renew時に出す役を受ける
				break;
			default:
				System.out
						.println("エラー発生　MonteCalro.java putToSeeStateOfFieldメソッド　：");
				break;
			}
		} else {
			list.add((long) 0); // PASS
			yaku = 0;// PASSの役を入れる
		}
		// debugPlace(list);
		return list;

	}

	public void debugPlace(ArrayList<Long> array) {
		System.out.println("ワンゲーム");
		System.out.println(" rank : " + rank + "状態 : " + state + " numberOfCards : " + numberOfCardSize + "turnPlayer" + turnPlayer);
		int size = array.size();
		long num = 0;
		for (int i = 0; i < size; i++) {
			num = array.get(i);
			System.out.println("num" + Long.toBinaryString(num));
			if (num == 0) {
				System.out.print("PASS");
			}
			for (int j = 0; j < 53; j++) {
				if ((num & (ONE << j)) != 0)
					System.out.print("Card : " + j + " rank : " + ((j - 1) % 13 + 1));
			}
			System.out.println();

		}
	}

	public void debug() {
		System.out.println(" rank : " + rank + "状態 : " + state + " numberOfCards : " + numberOfCardSize + "turnPlayer" + turnPlayer);
	}

	/**
	 * シミュレーションバランシングで手を決定する
	 *
	 * @param leraning
	 *            　学習フェーズを使用するかどうか
	 * @param
	 */
	public void useSimulationBarancing(boolean leraning, DataConstellation dc) {
		ArrayList<Long> list = getPutHand(); // 複数の候補手を探す
		if (list.get(0) == 0) {
			passPlayer = passPlayer | (1 << turnPlayer);

		} else {// PASS以外の時
			if (leraning) {// 学習フェーズを使用する時
				ObjectPool.sb.learningPhase(this, dc.getGrd()); // 学習フェーズ
				// sb.displaySita(); //Θを学習させたものを表示する
			}
			int pos = 0;
			switch (InitSetting.putHandMode) {
			case 0:
				pos = randomPutHand(list.size());
				break;
			case 1:
				pos = ObjectPool.sb.putHand(list, this);// シミュレーションバランシングで手を決定する
				break;
			case 2:
				if (InitSetting.DEBUGMODE_W) {
					debug();
				}
				pos = ObjectPool.sb.putHand_simulataion(list, this, dc.getWd());// シミュレーションバランシングで手を決定する
				break;
			default:
				break;
			}
			updatePlace(list.get(pos));
		}

		ObjectPool.releasePutHand(list);

	}

	/**
	 * ランダムで手を選択するメソッド
	 *
	 * @param randomで手を出す
	 */
	public int randomPutHand(int size) {
		return (int) (Math.random() * size);
	}

	/**
	 * シミュレーションバランシングで手を決定する(平均勾配用のメソッド)
	 *
	 * @param meanGradient
	 *            平均勾配
	 * @param vidit
	 *            訪問回数
	 * @return 訪問回数
	 */
	public double useSimulationBarancing_m(double[] meanGradient, double visit) {
		ArrayList<Long> list = getPutHand(); // 複数の候補手を探す
		if (list.get(0) == 0) {
			passPlayer = passPlayer | (1 << turnPlayer);
		} else {// PASS以外の時
			int pos = ObjectPool.sb.putHand_m(list, this, meanGradient);// シミュレーションバランシングで手を決定する
			visit++;
			updatePlace(list.get(pos));
		}
		ObjectPool.releasePutHand(list);

		return visit;
	}

	/**
	 * 自分の手札にjokerが存在するかどうかの判定
	 *
	 * @return Jokerが存在する
	 */
	public boolean isHaveJoker_myHand() {
		boolean result = false;
		if ((playersHands[mySeat] & (ONE << 0)) != 0) {// 自分の手札にjokerが存在する場合
			result = true;
		}
		return result;
	}

	/**
	 * 棋譜の情報をテキストデータにする
	 *
	 * @param point
	 *            出した手の点数
	 *
	 */
	public void writeText(double point) {
		char[] charMes = new char[256];
		int myHands = turnPLayerHaveHand(mySeat);// 自分の手札枚数
		File file;
		int num = 0;
		int counter = 0;
		/** 自分の手札の枚数 **/
		if (myHands >= 10) {// サイズの大きさが10以上の時
			file = new File("./gameRecord/myHand_" + myHands + "_"
					+ myGrade + ".txt");
			charMes[counter] = intChangeChar(myHands / 10);
			counter++;
			charMes[counter] = intChangeChar(myHands % 10);
			counter++;

		} else {// サイズの大きさが一桁の時
			file = new File("./gameRecord/myHand_0" + myHands + "_"
					+ myGrade + ".txt");
			charMes[counter] = '0';
			counter++;
			charMes[counter] = intChangeChar(myHands);
			counter++;
		}

		char[] cards = new char[CARDNUM];// カードの枚数
		num = 0;
		for (int i = 0; i < CARDNUM; i++) {
			cards[i] = '9';
		}
		for (int i = 0; i < CARDNUM; i++) { // カードの種類
			for (int j = 0; j < PLAYERS; j++) {// プレイヤーの人数
				num = (j * CARDNUM) + i;
				if ((playersHands[j] & (ONE << num)) != 0) { // プレイヤーの手札を持っている時
					cards[i] = intChangeChar(j);
					break;
				}
			}
		}

		charMes[counter] = intChangeChar(mySeat); // mySeatの格納
		counter++;
		num = 0;
		for (int i = 0; i < PLAYERS; i++) {
			num += turnPLayerHaveHand(i);
		}

		if (num >= 10) {
			charMes[counter] = intChangeChar(num / 10);
			counter++;
			charMes[counter] = intChangeChar(num % 10);
			counter++;
		} else {
			charMes[counter] = '0';
			counter++;
			charMes[counter] = intChangeChar(num);
			counter++;
		}
		charMes[counter] = intChangeChar(5 - Integer.bitCount(wonPlayer)); // 現在いるプレイヤー
		counter++;

		/** すべてのカードを入れる部分 **/
		for (int i = 0; i < CARDNUM; i++) {
			charMes[counter] = cards[i];
			counter++;
		}
		/** LockNumberの格納 **/
		for (int i = 0; i < 4; i++) {
			if ((lockNumber & (1 << i)) != 0) {
				charMes[counter] = '1';
			} else {
				charMes[counter] = '0';
			}
			counter++;
		}
		/** PlaceSuitsの格納 **/
		for (int i = 0; i < 4; i++) {
			if ((placeSuits & (1 << i)) != 0) {
				charMes[counter] = '1';
			} else {
				charMes[counter] = '0';
			}
			counter++;
		}
		/** rankの処理 */
		num = rank;
		if (rank <= 0)
			num = 0;
		if (num >= 10) {// numが二ケタの時
			charMes[counter] = intChangeChar(num / 10);
			counter++;
			charMes[counter] = intChangeChar(num % 10);
			counter++;
		} else {
			charMes[counter] = '0';
			counter++;
			charMes[counter] = intChangeChar(num);
			counter++;
		}
		/** reverseの処理 **/
		if (reverse) {
			charMes[counter] = '1';
		} else {
			charMes[counter] = '0';
		}
		counter++;

		/** numberOfCardの処理 ***/
		charMes[counter] = intChangeChar(numberOfCardSize);
		counter++;
		/** turnPlayerの処理 **/
		if (turnPlayer <= -1) {
			charMes[counter] = '9';
		} else {
			charMes[counter] = intChangeChar(turnPlayer);
		}
		counter++;
		/** putLastPlayerの処理 **/
		if (putLastPlayer <= -1) {
			charMes[counter] = '9';
		} else {
			charMes[counter] = intChangeChar(putLastPlayer);
		}
		counter++;
		/** stateの処理 **/
		if (state == State.RENEW) {
			charMes[counter] = '0';
		} else if (state == State.SINGLE) {
			charMes[counter] = '1';
		} else if (state == State.GROUP) {
			charMes[counter] = '2';
		} else {
			charMes[counter] = '3';
		}
		counter++;
		/** PASSPLayerの処理 **/
		for (int i = 0; i < PLAYERS; i++) {
			if ((passPlayer & (1 << i)) != 0) {
				charMes[counter] = '1';
			} else {
				charMes[counter] = '0';
			}
			counter++;
		}
		/*** firstWonPlayerの処理 **/
		for (int i = 0; i < PLAYERS; i++) {
			if ((wonPlayer & (1 << i)) != 0) {
				charMes[counter] = '1';
			} else {
				charMes[counter] = '0';
			}
			counter++;
		}
		// 自分のポイント
		num = (int) ((point * 100000) % 1000000);

		for (int i = 6; i >= 1; i--) {
			charMes[counter] = intChangeChar(intDigit(num, i));
			counter++;
		}
		try {
			FileWriter fl = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fl);
			BufferedReader br = new BufferedReader(new FileReader(file));
			if (br.readLine() != null) {
				bw.newLine();
			}
			bw.write(String.valueOf(charMes));
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int intDigit(int num, int digit) {
		return (int) (num % Math.pow(10, digit) / Math.pow(10, digit - 1));
	}

	public char intChangeChar(int num) {
		switch (num) {
		case 0:
			return '0';
		case 1:
			return '1';
		case 2:
			return '2';
		case 3:
			return '3';
		case 4:
			return '4';
		case 5:
			return '5';
		case 6:
			return '6';
		case 7:
			return '7';
		case 8:
			return '8';
		case 9:
			return '9';
		default:
			return '0';

		}
	}

	/**
	 * ゲームに参加している勝利したプレイヤーの数を探索する
	 *
	 * @return ゲームに参加しているプレイヤー
	 */
	public int notWonPLayers() {
		return 5 - Integer.bitCount(wonPlayer);
	}

	/**
	 * プレイヤーのすべての手札枚数を返すメソッド
	 *
	 * @return すべての手札
	 */
	public int allPLayersHands() {
		int result = 0;
		for (int i = 0; i < PLAYERS; i++) {
			result += Long.bitCount(playersHands[i]);
		}
		return result;
	}

	/**
	 * turnプレイヤーをPassに変更する
	 */
	public void turnPLayerDoPass() {
		passPlayer = passPlayer | (1 << turnPlayer);
	}

	/**
	 * 認証用のコードを返す
	 *
	 * @return　認証コード
	 */
	public int getAuthenticationCode_i() {
		int authenticationCode = 0;
		int num = turnPLayerHaveHand(turnPlayer); // ターンプレイヤーの手札の枚数を取得
		authenticationCode += num * 1000;
		authenticationCode += allPLayersHands() * 10;
		authenticationCode += notWonPLayers();
		return authenticationCode;

	}

	/**
	 * ターンプレイヤーがjokerを持ってるかどうか
	 *
	 * @return
	 */
	public boolean turnPlayerHaveJoker() {
		boolean result = false;
		if ((playersHands[turnPlayer] & (ONE << 1)) == 1)
			result = true;
		return result;
	}

	/**
	 * 重みベクトルの配列を返すメソッド 3～joker PASS 縛りを行える時 グループが出来るかどうか 階段が出来る時(joker抜き)
	 * 階段が出来る時(jokerあり)
	 *
	 * @return weight
	 */
	public int[] getWeight(int[] weight, long num) {
		int counter = 0;
		for (int i = 0; i < InitSetting.WEIGHTNUMBER; i++) {
			weight[i] = 0;
		}
		counter = 0;
		// カードの特性
		counter = searchTypeOfCards(weight, num, counter);
		counter = canLock(weight, num, counter);
		counter = canReverse(weight, num, counter);
		counter = haveJoker(weight, num, counter);
		counter = cardsSize(weight, num, counter);
		// 場の特性 53 * 2
		counter = weightPlaceCards(weight, num, counter);
		return weight;
	}

	/**
	 * ペア出しを崩したかどうかの判定
	 *
	 * @param weight
	 * @param num
	 * @param counter
	 * @return
	 */
	public int breakThePair(int[] weight, long num, int counter) {
		if (state != State.SEQUENCE) { // 階段の時のみは省略
			if (state == State.SINGLE && (num & ONE) != 0) {
				weight[counter] = 1;
				counter++;
				return counter;
			}
			int size = Long.bitCount(num);
			if ((num & ONE) != 0) {// jokerを抜く
				size--;
			}
			long pair = rank_3;
			for (int i = 0; i < 13; i++) {
				if ((num & pair) != 0) {
					if (size < Long.bitCount(playersHands[turnPlayer] & pair)) {
						weight[counter] = 1;
						break;
					}
				}
				pair = pair << 1;
			}
		}
		counter++;
		return counter;
	}

	/**
	 * 階段を崩したかどうか
	 *
	 * @param weight
	 *            重み
	 * @param num
	 *            　出したカード
	 * @param counter
	 * @return
	 */
	public int breakTheSequence(int[] weight, long num, int counter) {
		if (state != State.SEQUENCE) {
			long sequcene = 7;
			sequcene = sequcene << 1;
			long n = 0;
			boolean result = false;
			for (int i = 0; i < 11; i++) {// ランク
				for (int j = 0; j < SUITSNUM; j++) {
					n = sequcene << (i + j * 13);
					if ((num & n) != 0) {
						n = n & playersHands[turnPlayer];
						if (Long.bitCount(n) >= 3) {
							weight[counter]++;
							result = true;
							break;
						}
					}
				}
				if (result)
					break;
				sequcene = sequcene << 1;
			}
		}
		counter++;
		return counter;
	}

	/**
	 * そのカードが一番強いカードかどうかを調べる
	 *
	 * @param weight
	 *            重み
	 * @param num
	 *            　カード
	 * @param counter
	 * @return
	 */
	public int isStrongCard(int[] weight, long num, int counter) {
		long n = rank_3 << 13;
		long playerHand = playersHands[turnPlayer];
		for (int i = 0; i < 13; i++) {
			if ((n & playerHand) != 0) {
				if ((n & num) != 0) {
					weight[counter] = 1;
				}
				break;
			}
			n = n >> 1;
		}
		counter++;
		return counter;
	}
	/**
	 * 自分の出した役が一番小さい役かどうか調べるメソッド
	 * @param weight
	 * @param num
	 * @param counter
	 * @return
	 */
	public int isWeekCard(int[] weight, long num, int counter) {
		long n = rank_3;
		long playerHand = playersHands[turnPlayer];
		for (int i = 0; i < 13; i++) {
			if ((n & playerHand) != 0) {
				if ((n & num) != 0) {
					weight[counter] = 1;
				}
				break;
			}
			n = n << 1;
		}
		counter++;
		return counter;
	}

	/**
	 * 場の出ていないカード情報と自分の持っているカード情報を重みint[]を更新して返すメソッド
	 *
	 * @param weight
	 *            重み
	 * @param size
	 *            カードの枚数
	 * @param counter
	 *            重みを入れる最初の場所
	 * @return weight
	 */
	public int weightPlaceCards(int[] weight, long num, int counter) {
		long nc = notLookCards & (~playersHands[turnPlayer]);
		int result = counter + (53 * 2);
		long bit = 0;
		long ph = playersHands[turnPlayer] & (~num);
		for (int i = 0; i < CARDNUM; i++) {
			bit = (ONE << i);
			if ((nc & bit) != 0) {
				weight[counter] = 0;
			}
			if ((ph & bit) != 0) {
				weight[counter + 53] = 1;
			}
			counter++;
		}
		return result;
	}

	/**
	 * 出した役のカードサイズを返す
	 *
	 * @param weight
	 * @param cards
	 * @param size
	 * @param counter
	 * @return
	 */
	public int cardsSize(int[] weight, long num, int counter) {
		int size = Long.bitCount(num);
		if (size > 0) {
			if (size >= 5) {
				size = 5;
			}
			size--;
			weight[counter - size] = 1;
		}
		counter += 5;
		return counter;
	}

	/**
	 * jokerを役に使ったかどうかの判定
	 *
	 * @param weight
	 * @param cards
	 * @param size
	 * @param counter
	 * @return
	 */
	public int haveJoker(int[] weight, long num, int counter) {
		if ((num & (ONE << 0)) != 0) {
			weight[counter] = 1;
		}
		counter++;
		return counter;
	}

	/**
	 * 場の状態のデータの重み
	 *
	 * @param weight
	 * @param size
	 * @param counter
	 * @return
	 */
	public int setPlaceData_w(int[] weight, int size, int counter) {

		int num = rank;
		if (!(reverse && rank == num) || (!reverse && num == 0)) {// renewじゃない時
			if (reverse)
				num = 14 - num;
			num--;
			weight[counter + num] = 1;
		}
		counter += 14;
		if (state == State.RENEW) {
			weight[counter] = 1;
		} else if (state == State.SINGLE) {
			weight[counter + 1] = 1;
		} else if (state == State.GROUP) {
			weight[counter + 2] = 1;
		} else {
			weight[counter + 3] = 1;
		}

		counter += 4;
		return counter;
	}

	/**
	 * 革命出来るかどうかの判定
	 *
	 * @param weight
	 * @param cards
	 * @param size
	 * @param counter
	 * @return
	 */
	public int canReverse(int[] weight, long num, int counter) {
		int size = Long.bitCount(num);
		if (size >= 4) {
			long n = THIRTEEN << 1;
			boolean seq = true;
			for (int i = 0; i < SUITSNUM; i++) {
				if (Long.bitCount(n & num) == 1) {// ペア出しの判定
					seq = false;
					break;
				}
			}
			if (seq) {
				if (size >= 5) {
					weight[counter] = 1;
				}
			} else {
				weight[counter] = 1;
			}
		}
		counter++;
		return counter;
	}

	/**
	 * 縛りが出来るかの判定を行う
	 *
	 * @parm weight
	 * @param cards
	 *
	 */
	private int canLock(int[] weight, long num, int counter) {
		if (lockNumber != 0 && state != State.RENEW && num != 0) {// 縛りではない時
			int place = 0;
			long suit = THIRTEEN << 1;
			// 自分のマークを調べる
			for (int i = 0; i < 4; i++) {
				if ((num & suit) != 0) {
					place = (1 << i);
				}
			}
			// マークが一致した時の判定
			if (place == placeSuits) {
				weight[counter] = 1;
			} else {
				if ((num & (ONE << 0)) != 0) {// jokerを持っている時
					int n = 0;
					boolean result = true;
					for (int i = 0; i < 4; i++) {
						if ((place & (1 << i)) != 0 && (placeSuits & (1 << i)) == 0) {
							result = false;
							break;
						}
						if ((place & (1 << i)) == 0 && (placeSuits & (1 << i)) != 0) {
							n++;
						}
						if (n >= 2) {
							result = false;
							break;
						}
					}
					if (result) {
						weight[counter] = 1;
					}
				}
			}
		}
		counter++;
		return counter;
	}

	/**
	 * 出したカードの種類を探索する
	 *
	 * @param weight
	 *            重みベクトル
	 * @param cards
	 *            　カード
	 */
	public int searchTypeOfCards(int[] weight, long num,
			int counter) {
		counter = 15;
		if (num == 0) {// PASSの時
			weight[14] = 1;
			return counter;
		}
		if ((num & (ONE << 0)) != 0) {// jokerの時
			weight[13] = 1;
		}
		long rankPos = rank_3;
		for (int i = 0; i < 13; i++) {
			if ((rankPos & num) != 0) {
				weight[i] = 1;
			}
			rankPos = (rankPos << 1);
		}
		return counter;
	}

	/**
	 * 全ての配列をObjectPoolにリリースするメソッド
	 */
	public void release() {
		ObjectPool.releasePLayersHands(playersHands);
		playersHands = null;
	}

	// getter setter
	public final int getPlayers() {
		return PLAYERS;
	}

	public final int getMySeat() {
		return mySeat;
	}

	public final State getState() {
		return state;
	}

	public int getLockNumber() {
		return lockNumber;
	}

	public int getPlaceSuits() {
		return placeSuits;
	}

	public int getWonPlayer() {
		return wonPlayer;
	}

	public final int getRank() {
		return rank;
	}

	public final boolean isReverse() {
		return reverse;
	}

	public final int getNumberOfCardSize() {
		return numberOfCardSize;
	}

	public final int getPutLastPlayer() {
		return putLastPlayer;
	}

	public final int getTurnPlayer() {
		return turnPlayer;
	}

	public int getPassPlayer() {
		return passPlayer;
	}

	public int getFirstWonPlayer() {
		return firstWonPlayer;
	}

	public final void setMySeat(int mySeat) {
		this.mySeat = mySeat;
	}

	public long getNotLookCard() {
		return notLookCards;
	}

	public int getMyGrade() {
		return myGrade;
	}

	public long getYaku() {
		return yaku;
	}

	public boolean isCanGrowUpTree() {
		return canGrowUpTree;
	}

	public void setCanGrowUpTree(boolean canGrowUpTree) {
		this.canGrowUpTree = canGrowUpTree;
	}

	public int getHaveChildNumber() {
		return HaveChildNumber;
	}

	public void setHaveChildNumber(int haveChildNumber) {
		HaveChildNumber = haveChildNumber;
	}

	public double getUCB() {
		return UCB;
	}

	public void setUCB(double uCB) {
		UCB = uCB;
	}

	public int getVisit() {
		return visit;
	}

	public void setVisit(int visit) {
		this.visit = visit;
	}

	public int getWon() {
		return won;
	}

	public void setWon(int won) {
		this.won = won;
	}

	public void setWonPlayer(int wonPlayer) {
		this.wonPlayer = wonPlayer;
	}

	public void setFirstWonPlayer(int firstWonPlayer) {
		this.firstWonPlayer = firstWonPlayer;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}
}
