package simulationBransing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.ac.uec.daihinmin.Place;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import monteCalro.FieldData;
import monteCalro.MyData;
import monteCalro.State;
import monteCalro.Utility;
import object.InitSetting;
import object.WeightData;

/***
 * ゲームのフィールドの状態を格納する場
 *
 * @author 伸也
 *
 */
public class GameField {

	private final int players = 5;// プレイヤーの数

	private final int cardNum = 53; // カード枚数

	private final int suitsNum = 4; // suitの数

	private final int sumPlayersHands = players * cardNum;// プレイヤー人数とカードの枚数

	private final int sumPlyersHandNum = players * 14;

	private int mySeat;// 自分の座席番号

	private State state = State.EMPTY;// 初期状態の変数

	private boolean lock; // 縛りがあるかどうか

	private boolean[] lockNumber = new boolean[suitsNum];// 縛られているマークの番号

	private boolean[] placeSuits = new boolean[suitsNum];// 場に出されているマーク

	private boolean[] wonPlayer = new boolean[players]; // 勝利しているプレイヤー

	/** 3～2の数を1～13で表したもの -1がrenew **/
	private int rank;// 場に出されているランク

	/** 革命している時はtrue **/
	private boolean reverse;// 革命しているか否か

	private int numberOfCardSize;// 場に出されているカードの枚数

	private int turnPlayer; // ターンプレイヤー

	private int putLastPlayer;// 最後に出した人の座席番号

	private int[] notLookCards = new int[cardNum];

	private boolean[] passPlayer = new boolean[players];// PASSしたかどうかの判定用の変数

	private int[] playerHands = new int[sumPlayersHands]; // プレイヤーの手札
															// int[席順][カードの種類]=
															// 0 or 1(持っているカード)

	private int[] playerHandsOfCards = new int[players];// プレイヤーごとの手札の枚数 int[席順]
														// = 枚数

	private int[] playerTypeOfHandsofCards = new int[sumPlyersHandNum];// プレイヤーの種類によってのカード枚数
	// int[席順][カードの種類]= 枚数　0～13
	// JOKER 3 4の順番

	private boolean[] firstWonPlayer = new boolean[players]; // 最初の勝ったプレイヤーを記憶しておく
	/** 座席のランク **/
	private int[] grade = new int[players];

	/** 試しに使ってみたMapクラス **/
	private Map<Integer, int[]> pair;
	/*** Mapに入っているカードの枚数を記憶する **/
	private int mapCounter = 0;
	/** PASSの時 **/
	private boolean pass;
	/*** 一番初めのGameFieldクラスを作成 ****/
	private GameField firstGf;
	/*** シミュレーションバランシング用変数 ***/
	private SimulationBalancing sb;

	/**
	 * コンストラクタ
	 *
	 * @param playerNum
	 * @param bs
	 * @param fd
	 * @param md
	 */
	public GameField(int playerNum, BotSkeleton bs, FieldData fd, MyData md) {

		wonPlayer = new boolean[players];
		passPlayer = new boolean[players];

		firstWonPlayer = fd.getWonPlayer(); // 最初に勝ったプレイヤーを保存しておく

		grade = fd.getGrade();

		mySeat = md.getSeat();

		turnPlayer = mySeat;

		init(bs, fd);

		sb = new SimulationBalancing();
	}

	/**
	 * コンストラクタ(clone用)
	 *
	 * @param gf
	 *            cloneするGameFieldクラス
	 */
	public GameField(GameField gf) {
		pair = new HashMap<Integer, int[]>();

		mySeat = gf.getMySeat();
		State s = gf.getState();
		if (s == State.RENEW) {
			state = State.RENEW;
		} else if (s == State.SINGLE) {
			state = State.SINGLE;
		} else if (s == State.GROUP) {
			state = State.GROUP;
		} else {
			state = State.SEQUENCE;
		}
		lock = gf.isLock();
		rank = gf.getRank();
		reverse = gf.isReverse();
		numberOfCardSize = gf.getNumberOfCardSize();
		putLastPlayer = gf.getPutLastPlayer();
		turnPlayer = gf.getTurnPlayer();

		System.arraycopy(gf.getGrade(), 0, grade, 0, players);
		System.arraycopy(gf.getNotLookCard(), 0, notLookCards, 0, cardNum);

		System.arraycopy(gf.getLockNumber(), 0, lockNumber, 0, suitsNum);
		System.arraycopy(gf.getPlaceSuits(), 0, placeSuits, 0, suitsNum);
		System.arraycopy(gf.getWonPlayer(), 0, wonPlayer, 0, players);
		System.arraycopy(gf.getPassPlayer(), 0, passPlayer, 0, players);
		System.arraycopy(gf.getPlayerHandsOfCards(), 0, playerHandsOfCards, 0,
				players);
		System.arraycopy(gf.getFirstWonPlayer(), 0, firstWonPlayer, 0, players);
		System.arraycopy(gf.getPlayerHands(), 0, playerHands, 0,
				sumPlayersHands);
		System.arraycopy(gf.getPlayerTypeOfHandsofCards(), 0,
				playerTypeOfHandsofCards, 0, players * 14);

		this.sb = gf.getSb();
	}

	/**
	 * 見えていないカードの初期化
	 */
	public void initNotLookCard() {
		for (int i = 0; i < cardNum; i++) {
			for (int j = 0; j < players; j++) {
				if (playerHands[i + j * 53] == 1) {
					notLookCards[i] = 1;
					break;
				}
			}
		}
	}

	/**
	 * 空のコンストラクタ
	 */
	public GameField() {
		pair = new HashMap<Integer, int[]>();
	}

	/**
	 *
	 * @param copyHands
	 *            自分の手札群
	 */
	public void initPLaceGameFiled(int[] copyHands) {
		setPlayerHands(copyHands);

		setPlayerTypeOfHandsofCards(getTypeOfHandsOfCards());// 自分と相手のカードランクの枚数を計算する。

		initNotLookCard();// 場に出ているカード以外を格納

		initFirstGF();
	}

	/**
	 * プレイヤーごとに種類別に分けたカードの枚数を返す
	 *
	 * @return　種類別に分けたカードの枚数を返す
	 */
	private int[] getTypeOfHandsOfCards() {
		int num = players * 14;
		int[] result = new int[num];

		int counter = 0;// カードの枚数をカウントする
		// 探索部分
		for (int i = 0; i < players; i++) {
			for (int j = 0; j < 14; j++) {// カードの数字の枚数
				num = i * cardNum + j;
				if (j != 0) {// 普通のカードの処理
					for (int l = 0; l < 4; l++) {// カードの種類
						if (playerHands[num + (l * 13)] == 1)// もしカードが存在する時
							counter++;
					}
				} else {// jokerの時の処理
					if (playerHands[num] == 1) {// jokerを持っている時
						counter++;
					}
				}
				result[i * 14 + j] = counter;// 枚数記憶させる
				counter = 0;
			}
		}
		return result;
	}

	/**
	 * 棋譜データを復元するメソッド
	 *
	 * @param 棋譜データ
	 */
	public double restoreGameRecord(String gameRecord) {
		double point = 0;
		int pos = 4; // 手札の位置
		int num = 0;
		// 初期化
		for (int i = 0; i < sumPlayersHands; i++) {
			playerHands[i] = 0;
		}
		for (int i = 0; i < sumPlyersHandNum; i++) {
			playerTypeOfHandsofCards[i] = 0;
		}
		for (int i = 0; i < players; i++) {
			playerHandsOfCards[i] = 0;
		}

		// プレイヤー達の手札を復元
		for (int i = 0; i < cardNum; i++) {
			num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
			if (num != 9) {
				playerHands[cardNum * num + i]++;// プレイヤーのカードを加えてあげる
				playerHandsOfCards[num]++;// プレイヤーの手札枚数を増やす
				playerTypeOfHandsofCards[num * 14 + ((i - 1) % 13 + 1)]++;// プレイヤーのカードの種類の増加
				notLookCards[i] = 1;
			}
			pos++;
		}
		// LockNumberの復元
		lock = false;
		for (int i = 0; i < suitsNum; i++) {
			num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
			if (num == 1) {
				lockNumber[i] = true;
				lock = true;
			} else {
				lockNumber[i] = false;

			}
			pos++;
		}
		// PLaceSuitsの復元
		for (int i = 0; i < suitsNum; i++) {
			num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
			if (num == 1) {
				placeSuits[i] = true;
			} else {
				placeSuits[i] = false;
			}
			pos++;
		}
		// rankの復元
		rank = Integer.parseInt(gameRecord.substring(pos, pos + 2));
		pos += 2;
		// reverse
		num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
		if (num == 1) {
			reverse = true;
		} else {
			reverse = false;
		}
		pos++;
		// numberOfCardSize
		numberOfCardSize = Integer.parseInt(gameRecord.substring(pos, pos + 1));
		pos++;
		// turnPlayer
		turnPlayer = Integer.parseInt(gameRecord.substring(pos, pos + 1));
		mySeat = turnPlayer;
		pos++;
		// putLastPlayer
		num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
		if (num == 9) {
			putLastPlayer = -1;
		} else {
			putLastPlayer = num;
		}
		pos++;
		// state
		num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
		if (num == 0) {
			state = State.RENEW;
		} else if (num == 1) {
			state = State.SINGLE;
		} else if (num == 2) {
			state = State.GROUP;
		} else if (num == 3) {
			state = State.SEQUENCE;
		}
		pos++;
		// PassPlayer
		for (int i = 0; i < players; i++) {
			num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
			if (num == 1) {
				passPlayer[i] = true;
			} else {
				passPlayer[i] = false;
			}
			pos++;
		}
		// firstWonPlayer
		for (int i = 0; i < players; i++) {
			num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
			if (num == 1) {
				firstWonPlayer[i] = true;
				wonPlayer[i] = true;
			} else {
				firstWonPlayer[i] = false;
				wonPlayer[i] = false;
			}
			pos++;
		}
		// grade
		for (int i = 0; i < players; i++) {
			num = Integer.parseInt(gameRecord.substring(pos, pos + 1));
			grade[i] = num;
			pos++;
		}
		num = Integer.parseInt(gameRecord.substring(pos, pos + 4));
		point = (double) num / 1000.0;
		return point;

	}

	/**
	 * 初期状態のGameFiledを作成するメソッド
	 *
	 * @param bs
	 */
	public void initFirstGF() {
		firstGf = new GameField(this); // 一番最初のGameFirldクラスを作成
		/** 引き継がないデータ群を引き継がせる **/
		firstGf.setMySeat(this.mySeat);

		firstGf.setFirstGf(firstGf);// GameFieldクラスにfirstGameクラスをセット

		firstGf.setSb(this.sb);

	}

	/**
	 * firstCloneメソッドで一番初めのcloneを作成するためのメソッド
	 *
	 * @param gf
	 *            GameFeildクラス　firstGFを使用
	 * @param bs
	 *            BotSkwltonクラス
	 */
	public void clone(GameField gf) {
		State s = gf.getState();
		if (s == State.RENEW) {
			state = State.RENEW;
		} else if (s == State.SINGLE) {
			state = State.SINGLE;
		} else if (s == State.GROUP) {
			state = State.GROUP;
		} else {
			state = State.SEQUENCE;
		}
		lock = gf.isLock();
		rank = gf.getRank();
		reverse = gf.isReverse();
		numberOfCardSize = gf.getNumberOfCardSize();
		putLastPlayer = gf.getPutLastPlayer();
		turnPlayer = gf.getTurnPlayer();

		System.arraycopy(gf.getGrade(), 0, grade, 0, players);
		System.arraycopy(gf.getNotLookCard(), 0, notLookCards, 0, cardNum);

		System.arraycopy(gf.getLockNumber(), 0, lockNumber, 0, suitsNum);
		System.arraycopy(gf.getPlaceSuits(), 0, placeSuits, 0, suitsNum);
		System.arraycopy(gf.getWonPlayer(), 0, wonPlayer, 0, players);
		System.arraycopy(gf.getPassPlayer(), 0, passPlayer, 0, players);
		System.arraycopy(gf.getPlayerHandsOfCards(), 0, playerHandsOfCards, 0,
				players);
		System.arraycopy(gf.getFirstWonPlayer(), 0, firstWonPlayer, 0, players);
		System.arraycopy(gf.getPlayerHands(), 0, playerHands, 0,
				sumPlayersHands);
		System.arraycopy(gf.getPlayerTypeOfHandsofCards(), 0,
				playerTypeOfHandsofCards, 0, players * 14);

	}

	/**
	 * 一番最初のクラスに変更する
	 *
	 * @param bs
	 *            BotSkelton
	 */
	public void firstClone() {
		clone(firstGf);
	}

	/**
	 * 初期化のメソッド
	 *
	 * @param bs
	 *            BotSkelton
	 */
	private void init(BotSkeleton bs, FieldData fd) {
		pair = new HashMap<Integer, int[]>();

		Place place = bs.place();
		Meld lastMeld = place.lastMeld();

		state = getState(lastMeld);// 役を判定する

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
		lock = false;
		// PASSプレイヤーの更新
		System.arraycopy(wonPlayer, 0, passPlayer, 0, players);

		// 場のマークと縛りのマークを初期化
		for (int i = 0; i < 4; i++) {
			lockNumber[i] = false;
			placeSuits[i] = false;
		}
	}

	/**
	 * ゲーム終了時のポイントを返す
	 *
	 * @return point
	 */
	public int returnWinPoint() {
		int result = 5;
		for (int i = 0; i < players; i++) {
			if (i != mySeat && wonPlayer[i]) {
				result--;
			}
		}
		return result;
	}

	/**
	 * ゲーム終了時のポイントを返す
	 *
	 * @return point
	 */
	public int returnWinPoint_2() {
		int result = -1;
		if (wonPlayer[mySeat]) {
			result = 1;
		}
		return result;
	}

	/**
	 * すべてのプレイヤーをPASSにする
	 */
	public void allPlayerDoPass() {
		for (int i = 0; i < players; i++) {
			passPlayer[i] = true;
		}
	}

	/**
	 * 勝ったプレイヤーを判定する 自分のプレイヤーが上がった時点で終了
	 *
	 * @returnゲームを終了するかどうか
	 */
	public boolean checkGoalPlayer() {
		boolean result = true;// 自分が上がったかどうか
		// 自分が上がっていない状態で他のプレイヤーが全員上がっている時
		if (!wonPlayer[mySeat]) {
			for (int i = 0; i < players; i++) {
				if (!wonPlayer[i] && i != mySeat) {// 自分のターンじゃなくかつ誰か上がっていない時
					result = false;
					break;
				}
			}
		}

		// プレイヤーの手札を見た時に1枚もなかった場合は勝ちプレイヤー
		for (int i = 0; i < players; i++) {
			if (playerHandsOfCards[i] <= 0 && !wonPlayer[i]) {// カードが存在しない時
				wonPlayer[i] = true;
				passPlayer[i] = true;
				if (i == mySeat)// 自分が上がったなら
					result = true;
			}
		}

		return result;
	}

	/**
	 * 誰かが勝ち抜けしたプレイヤーが出たかどうかを判定する。 勝ち抜けたプレイヤー出た時点で終了
	 *
	 * @returnゲームを終了するかどうか
	 */
	public boolean checkGoalPlayer_2() {
		boolean result = false;// 誰かが上がった時
		for (int i = 0; i < players; i++) {
			if (!firstWonPlayer[i]) {// 最初の勝ったプレイヤーをはじく
				if (playerHandsOfCards[i] <= 0) { // 誰かが勝った時
					result = true;
					wonPlayer[i] = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * renewするかの判定 trueの時はrenewする、falseの時はしない
	 *
	 * @return renewするかどうか
	 */
	public boolean checkRenew() {
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
	 * ターンプレイヤーの更新 renewの時の処理を含む
	 */
	public void updateTurnPlayer() {
		if (state == State.RENEW) {// renewした時の判定
			/** renewした時は最後に置いたプレイヤーをターンプレイヤーにする **/
			if (wonPlayer[turnPlayer]) {
				turnPlayer++;
				if (turnPlayer >= players)// プレイヤーの人数把握
					turnPlayer = 0;
			} else {
				turnPlayer = putLastPlayer;
			}
		} else {// renew以外
			turnPlayer++;
			if (turnPlayer >= players)// プレイヤーの人数把握
				turnPlayer = 0;
		}

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
					if(num == 6){//8のカードの時
						result = true;
						break;
					}
					num++;
				}
			} else {// 革命の時
				for (int i = 0; i < numberOfCardSize; i++) {
					if(num == 6){//8のカードの時
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
	public void updatePlace(int[] putHand) {

		int num = 0;
		// カードサイズの更新
		numberOfCardSize = putHand.length;
		// 最後に出した人の更新
		putLastPlayer = turnPlayer; // 最後に出した人を更新

		if (state == State.RENEW) {// Renewの時の更新部

			state = getState(putHand);

			boolean joker = false;
			// マークの探索
			for (int i = 0; i < numberOfCardSize; i++) {
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
				boolean joker = false;
				for (int i = 0; i < numberOfCardSize; i++) {
					if (putHand[i] == 0) {
						joker = true;
						continue;
					}
					num = (putHand[i] - 1) / 13;
					cloneLock[num] = true;
				}
				boolean result = true;
				// 場の役と今の役を比べる
				for (int i = 0; i < 4; i++) {
					if (placeSuits[i] != cloneLock[i]) {
						if (joker) {
							joker = false;
							cloneLock[i] = true;
							continue;
						}
						result = false;
						break;
					}
				}
				if (result) { // 縛りが成立する場合
					lock = true;
					System.arraycopy(cloneLock, 0, lockNumber, 0, suitsNum);
				}
				placeSuits = cloneLock;

			} else {// 縛りが存在する時
				System.arraycopy(lockNumber, 0, placeSuits, 0, suitsNum);
			}
		}

		// 役のランクを更新
		if (putHand[0] == 0 && state == State.SINGLE) {// joker単体出しの時の処理
			if (!reverse) {
				rank = 14;
			} else {
				rank = 0;
			}
		} else {
			if (putHand[0] != 0) { // joker含みの階段とペアの判定
				if (state == State.SEQUENCE) {
					rank = (putHand[1] - 1) % 13;
				} else {
					rank = (putHand[0] - 1) % 13 + 1;
				}
			} else {
				rank = (putHand[1] - 1) % 13 + 1;
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
		// 手札から指定した枚数とカードを抜く

		for (int i = 0; i < numberOfCardSize; i++) {
			num = putHand[i];
			notLookCards[num] = 0;
			playerHands[turnPlayer * cardNum + num] = 0;
			if (num != 0) {// jokerの時の処理
				num = (num - 1) % 13 + 1;
			}
			playerTypeOfHandsofCards[turnPlayer * 14 + num]--;
		}
		playerHandsOfCards[turnPlayer] -= numberOfCardSize;
	}

	/**
	 * 役の大きさと使ったカードから役の出し方を判定し返すメソッド
	 *
	 *
	 * @param putHand
	 *            　出したカードの配列
	 * @return 出された役の出し方を返す
	 */
	private State getState(int[] putHand) {
		int size = putHand.length;
		State r = State.EMPTY;
		if (size == 1) {// カードが一枚の時
			r = State.SINGLE;
		} else {
			int x = 0;
			int y = 0;
			if (size == 2) {// カードが2枚の時
				r = State.GROUP;
			} else {// カードが3枚以上の時
				for (int i = 0; i < 3; i++) {// 3枚見ればカード役がわかるため
					if (putHand[i] == 0) {
						continue;
					}
					if (x == 0) {
						x = (putHand[i] - 1) % 13 + 1;
						continue;
					} else {
						y = (putHand[i] - 1) % 13 + 1;
					}
					if (x == y) {// rankが同じ場合
						r = State.GROUP;
					} else {// 　rankが違う場合
						r = State.SEQUENCE;
					}
					break;
				}
			}
		}
		return r;
	}

	/**
	 * Mapの初期化
	 */
	public void initMap() {
		pair.clear();
		pass = false;
		mapCounter = 0;
	}

	/**
	 * Mapにaddを行うメソッド
	 *
	 * @param array
	 */
	public void addMap(int[] array) {
		pair.put(mapCounter, array);
		mapCounter++;
	}

	/**
	 * MapにPASSを入れる
	 */
	public void mapAddPass() {
		pair.put(mapCounter, new int[] { 256 });
		if (mapCounter == 0)
			pass = true;
		mapCounter++;
	}

	/**
	 * 単体出しの出せるカードを探索 (RENEWでも使える)
	 *
	 * @return 出せるすべての役
	 */
	private void searchSingleMeld() {
		// スペ3の判定
		if (state != State.RENEW && (rank == 14 || rank == 0)) {// JOKERが場に出されている時
			if (playerHands[turnPlayer * cardNum + 1] == 1) { // スぺ3を持っている時
				addMap(new int[] { 1 });
				return;
			}
		}
		int firstPos = turnPlayer * 14;
		if (playerTypeOfHandsofCards[firstPos] == 1) {// jokerを持っている時
			addMap(new int[] { 0 });
		}
		int num = 0;
		if (!reverse) {// 普通の時
			for (int i = rank + 1; i < 14; i++) {
				if (playerTypeOfHandsofCards[firstPos + i] != 0)// 1枚以上存在する時
					for (int j = 0; j < 4; j++) {
						if (lock && !lockNumber[j]) {// 縛られているマークの時
							continue;
						}
						num = i + j * 13;
						if (playerHands[turnPlayer * cardNum + num] == 1) {// もしそのカードを持っている時
							addMap(new int[] { num });
						}
					}
			}
		} else {// 革命の時
			for (int i = rank - 1; i > 0; i--) {
				if (playerTypeOfHandsofCards[firstPos + i] != 0)// 1枚以上存在する時
					for (int j = 0; j < 4; j++) {
						if (lock && !lockNumber[j]) {// 縛られているマークの時
							continue;
						}
						num = i + j * 13;
						if (playerHands[turnPlayer * cardNum + num] == 1) {// もしそのカードを持っている時
							addMap(new int[] { num });
						}
					}
			}
		}
	}

	/**
	 * ペア出しの役を探索するメソッド 役が入っていない時nullを返す
	 *
	 * @param size
	 *            Meldのサイズ
	 * @return 役の集合を返す int[必要なもの][出せる役]
	 */
	private void searchGroupMeld(int size) {
		int[] meld = new int[size];// ひとつの役
		int[] c = new int[size + 1];
		int[] conbine = new int[5];// カード枚数

		boolean joker = false; // jokerを持っているかどうか
		int jk = 0;// ジョーカーの枚数分足すための変数
		int firstPos = turnPlayer * cardNum;
		int pthc = turnPlayer * 14;
		if (playerTypeOfHandsofCards[pthc] == 1) {// jokerを持っている時
			joker = true;
			jk = 1;
		}
		int counter = 0;// 配列の番号を記憶する

		int num = 0;
		int x = 0;// 計算用の変数
		for (int l = 0; l < 5; l++) {
			conbine[l] = 512;// ありえない数
		}
		if (!reverse) {// 革命が起きていない時
			for (int i = rank + 1; i < 14; i++) {
				num = playerTypeOfHandsofCards[pthc + i] + jk;
				counter = 0;
				if (num >= size) {// ペア出し出来る
					if (joker) {
						conbine[counter] = 0;
						counter++;
					}
					for (int j = 0; j < 4; j++) {
						x = i + j * 13;
						if (playerHands[firstPos + x] == 1) {
							conbine[counter] = x;
							counter++;
						}
					}
					combination(meld, conbine, c, 1, num, size);
				}
			}
		} else {// 革命の時
			for (int i = rank - 1; i > 0; i--) {
				num = playerTypeOfHandsofCards[pthc + i] + jk;
				counter = 0;
				if (num >= size) {// ペア出し出来る
					if (joker) {
						conbine[counter] = 0;
						counter++;
					}
					for (int j = 0; j < 4; j++) {
						x = i + j * 13;
						if (playerHands[firstPos + x] == 1) {
							conbine[counter] = x;
							counter++;
						}
					}
					if (joker) {
						if (counter == playerTypeOfHandsofCards[pthc + i]) {
							continue;
						}
					}
					combination(meld, conbine, c, 1, num, size);
				}
			}
		}
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
	 *
	 * @return
	 */
	private void combination(int[] resultMeld, int[] meld, int[] c, int m,
			int n, int r) {
		if (m <= r) {
			for (int i = c[m - 1] + 1; i <= n - r + m; i++) {
				resultMeld[m - 1] = meld[i - 1];
				c[m] = i;
				combination(resultMeld, meld, c, m + 1, n, r);
				if (m == r) {// 配列の最後まで埋まった時
					if (checkLock_Group(resultMeld)
							&& checkEffecticalJoker(resultMeld, m, r)) {// 縛りをチェックする
						addMap(resultMeld.clone());
					}
				}
			}
		}
	}

	public boolean checkEffecticalJoker(int[] meld, int m, int c) {
		boolean result = true;
		boolean joker = false;
		int size = meld.length;
		for (int i = 0; i < size; i++) {
			if (meld[i] == 0) {
				joker = true;
			}
		}
		if (joker && m != c) {
			result = false;
		}
		return result;
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
		boolean result = true;
		int size = meld.length;

		int num = 0;
		for (int i = 0; i < size; i++) {
			if (meld[i] == 0) {// jokerの時
				continue;
			}
			num = (meld[i] - 1) / 13;// markを抽出
			if (!lockNumber[num]) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * カード枚数から階段を探索するメソッド
	 *
	 * @param cardSize
	 *            カードの大きさ
	 * @return カードが入った配列を返す nullの場合PASS
	 */
	private void searchSequenceMeld(int cardSize) {
		int[] meld = new int[cardSize];
		int counter = 0;
		int firstPos = turnPlayer * cardNum; // playerHandsの始めの位置を記録
		boolean joker = false;

		if (playerHands[firstPos] == 1) {// jokerを持っている時
			joker = true;
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
		int num = 0;
		if (!reverse) {// 普通の時
			for (int i = defalt; i < 15 - cardSize; i++) {// カードでの探索
				for (int j = 0; j < 4; j++) {
					num = i + j * 13;// カードを表現
					if (playerHands[firstPos + num] == 1) {// そのカードを持っている時
						if (lock && !lockNumber[j])// 縛りが存在しており、縛られているカードではない場合
							continue;
						searchSequence(meld, counter, num, cardSize, joker,
								false, firstPos);// 階段の探索を行う
					}
				}
			}
		} else {// 革命の時
			for (int i = defalt; i > cardSize - 1; i--) {// カードでの探索
				for (int j = 0; j < 4; j++) {
					num = i + j * 13;// カードを表現
					if (playerHands[turnPlayer * cardNum + num] == 1) {// そのカードを持っている時
						if (lock && !lockNumber[j])// 縛りが存在しており、縛られているカードではない場合
							continue;
						searchSequence(meld, counter, num, cardSize, joker,
								false, firstPos);// 階段の探索を行う
					}
				}
			}
		}
	}

	/***
	 * あるランクからの階段ができるかの判定、探索を行うメソッド
	 *
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
	 *
	 * @param firstPos
	 *            playerHandsなどの最初の位置を記憶
	 * @return　resultCounnter
	 */
	private void searchSequence(int[] meld, int counter, int num, int size,
			boolean joker, boolean dojoker, int firstPos) {
		if (!dojoker) { // jokerを使わな勝った時
			meld[counter] = num;

		} else {
			meld[counter] = 0;
		}

		counter++;

		if (counter == size) {// 役が成立した時
			addMap(meld.clone());
			return;
		}

		if (!reverse) {// 普通の時
			num++;
			if (playerHands[firstPos + num] == 1) {// 2より上は見ない
				meld[counter] = num;
				searchSequence(meld, counter, num, size, joker, false, firstPos);
			}
			num--;
		} else {// 革命が起きている時
			num--;
			if (playerHands[firstPos + num] == 1) {// 3より下は見ない
				meld[counter] = num;
				searchSequence(meld, counter, num, size, joker, false, firstPos);
			}
			num++;
		}
		if (joker) {// jokerを持っている時
			joker = false;
			searchSequence(meld, counter, num, size, joker, true, firstPos);
		}
	}

	/**
	 * MeldDataから場の更新を行う
	 *
	 * @param md
	 *            MeldData
	 */

	public void renewPlace_MeldData(MeldData md) {
		int num = -1;
		if (!md.isPass()) {// MeldDataがパスでは無い時
			int firstPos = turnPlayer * cardNum;
			int ptch = turnPlayer * 14;

			if (state != State.RENEW && !lock) {// renewじゃない時かつ縛りが無い時
				boolean result = true;
				for (int i = 0; i < numberOfCardSize; i++) {
					if (num == 0)// Jokerの時はcontinue
						continue;
					num = (md.getCards()[i] - 1) / 13;// markを取る
					if (!placeSuits[num]) {
						result = false;
						break;
					}
				}
				if (result) {// 縛りの発生
					lock = true;
					System.arraycopy(placeSuits, 0, lockNumber, 0, suitsNum);
				}
			}

			// 初期化
			for (int i = 0; i < 4; i++) {
				placeSuits[i] = false;
			}

			numberOfCardSize = md.getSize();// 場に出ているカード枚数を記憶

			boolean joker = false;
			// placeSuitsの更新
			int[] putHand = new int[numberOfCardSize];

			for (int i = 0; i < numberOfCardSize; i++) {
				num = md.getCards()[i];
				putHand[i] = num;
				if (num == 0) {// Jokerの時はcontinue
					joker = true;
					continue;
				}
				num = (num - 1) / 13;// markを取る
				placeSuits[num] = true;
			}

			if (joker) {// jokerの時、適当にマークを割り当てる
				for (int i = 0; i < 4; i++) {
					if (!placeSuits[i]) {
						placeSuits[i] = true;
						joker = false;
						break;
					}
				}
			}

			state = getState(putHand);// 場の状態の更新

			putLastPlayer = turnPlayer;// 最後に出したプレイヤーを自分にする

			// 役のランクを更新
			if (putHand[0] == 0 && state == State.SINGLE) {// joker単体出しの時の処理
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

			// 革命の判定
			if (state == State.GROUP) {
				if (numberOfCardSize >= 4) {
					reverse = !reverse;
				}
			} else if (state == State.SEQUENCE) {
				if (numberOfCardSize >= 5) {
					reverse = !reverse;
				}
			}

			// 手札から指定した枚数とカードを抜く
			for (int i = 0; i < numberOfCardSize; i++) {
				num = md.getCards()[i];
				notLookCards[num] = 0;
				playerHands[firstPos + num]--;
				if (num != 0)
					num = (num - 1) % 13 + 1;

				playerTypeOfHandsofCards[ptch + num]--;
			}

			playerHandsOfCards[turnPlayer] -= numberOfCardSize;

		} else {// PASSした時
			passPlayer[turnPlayer] = true;
		}
	}

	/**
	 * renew時の出せる全ての役を返すメソッド
	 *
	 * @return 出せるすべての役の配列を返す
	 */
	private void returnAllResult_renewMeld() {
		searchSingleMeld();// 出せる役の結果
		// ペア出しの格納
		for (int i = 2; i < 5; i++) {// ペア出しは2～5枚しか存在しないため
			searchGroupMeld(i);
		}
		// 階段出しの格納
		int num = 53 / players + 1;
		for (int i = 3; i < num; i++) {
			searchSequenceMeld(i);
		}
	}

	/**
	 * その場で出せる手を返すメソッド
	 *
	 * @return
	 */
	public void getPutHand() {
		initMap();// mapの初期化
		mapCounter = 0;
		// 今回PASSは一番最後の配列が256の時とした
		if (!passPlayer[turnPlayer]) {// そのプレイヤーがパスの状態だった場合
			switch (state) {
			case SINGLE:// 単体出しの時
				searchSingleMeld();// 単体出しで出す役を受け取る\
				mapAddPass();
				break;
			case GROUP:// ペア出しの時
				searchGroupMeld(numberOfCardSize);// ペア出しで出す役を受け取る
				mapAddPass();
				break;
			case SEQUENCE:// 階段出しの時
				searchSequenceMeld(numberOfCardSize);// ペア出しで出す役を受け取る
				mapAddPass();
				break;
			case RENEW:// renewの時
				returnAllResult_renewMeld();// renew時に出す役を受ける
				break;
			default:
				System.out
						.println("エラー発生　MonteCalro.java putToSeeStateOfFieldメソッド　：");
				break;
			}
		} else {
			mapAddPass();
		}
	}

	/**
	 * シミュレーションバランシングで手を決定する
	 *
	 * @param leraning
	 *            　学習フェーズを使用するかどうか
	 * @param
	 */
	public void useSimulationBarancing(boolean leraning, WeightData wd) {
		getPutHand(); // 複数の候補手を探す
		if (pass) {
			passPlayer[turnPlayer] = true;

		} else {// PASS以外の時
			if (leraning) {// 学習フェーズを使用する時
				sb.preparelearning(this); // 学習フェーズ
				// sb.displaySita(); //Θを学習させたものを表示する
			}
			int pos = 0;
			switch (InitSetting.putHandMode) {
			case 0:
				pos = randomPutHand();
				break;
			case 1:
				pos = sb.putHand(pair, this);// シミュレーションバランシングで手を決定する
				break;
			case 2:
				pos = sb.putHand_simulataion(pair, this, wd);// シミュレーションバランシングで手を決定する
				break;
			default:
				break;
			}
			if (pair.get(pos)[0] == 256) { // PASSの時
				passPlayer[turnPlayer] = true;
			} else {
				updatePlace(pair.get(pos));
			}
		}

	}

	/**
	 * ランダムで手を選択するメソッド
	 *
	 * @param randomで手を出す
	 */
	public int randomPutHand() {
		return (int) (Math.random() * mapCounter);
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
	public int useSimulationBarancing_m(double[] meanGradient, int visit) {
		getPutHand(); // 複数の候補手を探す
		if (pass) {
			passPlayer[turnPlayer] = true;
		} else {// PASS以外の時
			int pos = sb.putHand_m(pair, this, meanGradient);// シミュレーションバランシングで手を決定する
			visit++;
			if (pair.get(pos)[0] == 256) { // PASSの時
				passPlayer[turnPlayer] = true;
			} else {
				updatePlace(pair.get(pos));
			}
		}
		return visit;
	}

	/**
	 * 自分の手札にjokerが存在するかどうかの判定
	 *
	 * @return Jokerが存在する
	 */
	public boolean isHaveJoker_myHand() {
		boolean result = false;
		if (playerHands[mySeat * cardNum] == 1) {// 自分の手札にjokerが存在する場合
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

		int myHands = playerHandsOfCards[mySeat];// 自分の手札枚数
		File file;
		String message = "";
		/** 自分の手札の枚数 **/
		if (myHands >= 10) {// サイズの大きさが10以上の時
			file = new File("./gameRecord/myHand_" + myHands + "_"
					+ grade[mySeat] + ".txt");
			message += myHands;
		} else {// サイズの大きさが一桁の時
			file = new File("./gameRecord/myHand_0" + myHands + "_"
					+ grade[mySeat] + ".txt");
			message += "0" + myHands;
		}

		int[] cards = new int[cardNum];// カードの枚数
		int num = 0;
		for (int i = 0; i < cardNum; i++) {
			cards[i] = 9;
		}
		for (int i = 0; i < cardNum; i++) { // カードの種類
			for (int j = 0; j < players; j++) {// プレイヤーの人数
				num = (j * cardNum) + i;
				if (playerHands[num] == 1) { // プレイヤーの手札を持っている時
					cards[i] = j;
					break;
				}
			}
		}
		/** mySeatの格納 **/
		message += mySeat;
		/** プレイヤーの人数 **/
		num = 0;
		for (int i = 0; i < players; i++) {
			if (!firstWonPlayer[i]) {
				num++;
			}
		}
		message += num;

		/** すべてのカードを入れる部分 **/
		for (int i = 0; i < cardNum; i++) {
			message += cards[i];
		}
		/** LockNumberの格納 **/
		for (int i = 0; i < suitsNum; i++) {
			if (lockNumber[i]) {
				message += 1;
			} else {
				message += 0;
			}
		}
		/** PlaceSuitsの格納 **/
		for (int i = 0; i < suitsNum; i++) {
			if (placeSuits[i]) {
				message += 1;
			} else {
				message += 0;
			}
		}
		/** rankの処理 */
		num = rank;
		if (rank <= 0)
			num = 0;

		if (num >= 10) {// numが二ケタの時
			message += num;
		} else {
			message += 0 + "" + num;
		}
		/** reverseの処理 **/
		if (reverse) {
			message += 1;
		} else {
			message += 0;
		}
		/** numberOfCardの処理 ***/
		message += numberOfCardSize;
		/** turnPlayerの処理 **/
		if (turnPlayer <= -1) {
			message += 9;
		} else {
			message += turnPlayer;
		}

		/** putLastPlayerの処理 **/
		if (putLastPlayer <= -1) {
			message += 9;
		} else {
			message += putLastPlayer;
		}
		/** stateの処理 **/
		if (state == State.RENEW) {
			message += 0;
		} else if (state == State.SINGLE) {
			message += 1;
		} else if (state == State.GROUP) {
			message += 2;
		} else if (state == State.SEQUENCE) {
			message += 3;
		}

		/** PASSPLayerの処理 **/
		for (int i = 0; i < players; i++) {
			if (passPlayer[i]) {
				message += 1;
			} else {
				message += 0;
			}
		}
		/*** firstWonPlayerの処理 **/
		for (int i = 0; i < players; i++) {
			if (firstWonPlayer[i]) {
				message += 1;
			} else {
				message += 0;
			}
		}
		/*** gradeの処理 **/
		// grade
		for (int i = 0; i < players; i++) {
			message += grade[i];
		}
		System.out.println(point);
		num = (int) ((point * 1000) % 10000);

		message += num;
		try {
			FileWriter fl = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fl);
			bw.newLine();
			bw.write(message);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ゲームに参加しているプレイヤーの数を探索する
	 *
	 * @return ゲームに参加しているプレイヤー
	 */
	public int notWonPLayers() {
		int result = 0;
		for (int i = 0; i < players; i++) {
			if (!wonPlayer[i]) {// プレイヤーがまだ勝利していない時
				result++;
			}
		}
		return result;
	}

	/**
	 * プレイヤーのすべての手札枚数を返すメソッド
	 *
	 * @return すべての手札
	 */
	public int allPLayersHands() {
		int result = 0;
		for (int i = 0; i < players; i++) {
			result += playerHandsOfCards[i];
		}
		return result;
	}

	/**
	 * turnプレイヤーをPassに変更する
	 */
	public void turnPLayerDoPass() {
		passPlayer[turnPlayer] = true;
	}

	/**
	 * 認証用のコードを返す
	 *
	 * @return　認証コード
	 */
	public String getAuthenticationCode() {
		String authenticationCode = "";
		int num = getPlayerHandsOfCards()[getTurnPlayer()]; // ターンプレイヤーの手札の枚数を取得

		if (num >= 10) {
			authenticationCode += num;
		} else {
			authenticationCode += "0" + num;
		}
		num = allPLayersHands();
		// プレイヤーの人数を格納
		authenticationCode += notWonPLayers();
		if (num >= 10) {
			authenticationCode += num;
		} else {
			authenticationCode += "0" + num;
		}

		return authenticationCode;
	}

	/**
	 * 認証用のコードを返す
	 *
	 * @return　認証コード
	 */
	public int getAuthenticationCode_i() {
		int authenticationCode = 0;
		int num = getPlayerHandsOfCards()[getTurnPlayer()]; // ターンプレイヤーの手札の枚数を取得

		authenticationCode += num * 1000;

		num = allPLayersHands();
		// プレイヤーの人数を格納
		authenticationCode += notWonPLayers() * 100;
		if (num >= 10) {
			authenticationCode += num;
		} else {
			authenticationCode = authenticationCode * 10 + num;
		}

		return authenticationCode;
	}

	/**
	 * ターンプレイヤーがjokerを持ってるかどうか
	 *
	 * @return
	 */
	public boolean turnPlayerHaveJoker() {
		boolean result = false;
		if (playerHands[turnPlayer * cardNum] == 1)
			result = true;
		return result;
	}

	/**
	 * 重みベクトルの配列を返すメソッド 3～joker PASS 縛りを行える時 グループが出来るかどうか 階段が出来る時(joker抜き)
	 * 階段が出来る時(jokerあり)
	 *
	 * @return weight
	 */
	public int[] getWeight(int[] weight, int[] cards, boolean first) {
		int counter = 0;
		if (!first) {
			int size = -53 * 4 + InitSetting.WEIGHTNUMBER;
			for (int i = 0; i < size; i++) {
				weight[i] = 0;
			}
		}
		int size = cards.length;
		// カードの特性
		counter = searchTypeOfCards(weight, cards, size, counter);
		counter = canLock(weight, cards, size, counter);
		counter = canReverse(weight, cards, size, counter);
		counter = haveJoker(weight, cards, size, counter);
		// counter = cardsSize(weight, cards, size, counter);
		counter += 5;
		// 場の特性 53 * 4
		if (first) {
			counter = weightPlaceCards(weight, size, counter);
		}
		// counter = setPlaceData_w(weight, size, counter);
		/**
		 * searchAnotherStateCards(weight, cards, 16, size); checkS3(weight,
		 * cards, 20, size); cardIsStrongest(weight, cards, 21, size);
		 */
		return weight;
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
	public int weightPlaceCards(int[] weight, int size, int counter) {
		int num = 0;
		if (reverse) {
			num++;
		}
		if (weight[16] == 1) {
			num++;
		}
		num = num % 2;
		for (int i = 0; i < cardNum; i++) {
			if (num == 0) {
				if (notLookCards[i] == 1
						&& playerHands[i + cardNum * turnPlayer] == 0) {
					weight[counter] = 1;
				}
				counter++;
			}
		}
		for (int i = 0; i < cardNum; i++) {
			if (num == 1) {
				if (notLookCards[i] == 1
						&& playerHands[i + cardNum * turnPlayer] == 0) {
					weight[counter] = 1;
				}
			}
			counter++;
		}
		for (int i = 0; i < cardNum; i++) {
			if (num == 1) {
				if (playerHands[i + cardNum * turnPlayer] == 1) {
					weight[counter] = 1;
				}
			}
			counter++;
		}
		for (int i = 0; i < cardNum; i++) {
			if (num == 0) {
				if (playerHands[i + cardNum * turnPlayer] == 1) {
					weight[counter] = 1;
				}
			}
			counter++;
		}
		return counter;
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
	public int cardsSize(int[] weight, int[] cards, int size, int counter) {
		if (!(cards[0] >= 64)) {// PASS以外の時
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
	public int haveJoker(int[] weight, int[] cards, int size, int counter) {

		for (int i = 0; i < size; i++) {
			if (cards[i] == 0) {
				weight[counter] = 1;
			}
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
	public int canReverse(int[] weight, int[] cards, int size, int counter) {

		if (size >= 4) {
			if ((cards[0] - 1) % 13 + 1 == (cards[1] - 1) % 13 + 1) {// ペア
				weight[counter] = 1;
			} else if ((cards[1] - 1) % 13 + 1 == (cards[2] - 1) % 13 + 1) {// ペア
				weight[counter] = 1;
			} else {// 階段
				if (size >= 5)
					weight[counter] = 1;
			}

		}

		counter++;
		return counter;
	}

	/**
	 * 単体出しとgroupのみに適応
	 *
	 * @param weight
	 * @param cards
	 * @param counter
	 */
	public void cardIsStrongest(int[] weight, int[] cards, int counter, int size) {

		if (cards[0] >= 64)// PASS
			return;
		int rank = 14;
		boolean flag = true;
		for (int i = 0; i < size; i++) {
			if (cards[i] != 0) {
				rank = (cards[i] - 1) / 13 + 1;
				break;
			}
		}
		if (!reverse) {
			for (int i = rank + 1; i < 14; i++) {
				if (playerTypeOfHandsofCards[turnPlayer * 14 + i] >= 1) {
					flag = false;
					break;
				}
			}
		} else {
			for (int i = rank - 1; i > 0; i--) {
				if (playerTypeOfHandsofCards[turnPlayer * 14 + i] >= 1) {
					flag = false;
					break;
				}
			}
		}
		if (flag) {
			weight[counter] = 1;
		}
	}

	/**
	 * joker単体出しの時にスぺ3が残っているかの判定
	 *
	 * @param weight
	 * @param cards
	 * @param counter
	 */
	public void checkS3(int[] weight, int[] cards, int counter, int size) {
		if (size == 1 && cards[0] == 0) {
			for (int i = 0; i < players; i++) {
				if (turnPlayer != i) {
					if (playerHands[i * 53 + 1] == 1) {// スぺ3が残っている時
						weight[counter] = 1;
						break;
					}
				}
			}
		}
	}

	/**
	 * 縛りが出来るかの判定を行う
	 *
	 * @parm weight
	 * @param cards
	 *
	 */
	private int canLock(int[] weight, int[] cards, int size, int counter) {
		if (!isLock() && state != State.RENEW && cards[0] <= 64) {// 縛りではない時
			int num = 0;
			boolean[] place = getPlaceSuits();
			boolean flag = true;
			for (int i = 0; i < size; i++) {
				num = cards[i];
				if (num == 0) { // jokerの時の処理
					continue;
				}
				num = (num - 1) / 13;
				if (!place[num]) {
					flag = false;
					break;
				}
			}
			if (flag) {
				weight[counter] = 1;
			}
		}
		counter++;
		return counter;
	}

	/**
	 * Weightの階段やダブルなどの手が重複するかどうかを調べるメソッド
	 *
	 * @param weight
	 *            重みベクトル
	 * @param cards
	 *            　出したカード
	 * @parm counter どこから始めるかどうかの判定用の引数
	 */
	public void searchAnotherStateCards(int[] weight, int[] cards, int counter,
			int size) {
		if (cards[0] >= 64)
			return;
		boolean joker = false;
		for (int i = 0; i < size; i++) {
			if (cards[i] == 0) {
				joker = true;
				break;
			}
		}
		int check = size;
		if (joker) {
			check--;
		}
		int rank = 0;
		boolean sequence = false;
		for (int i = 0; i < size; i++) {
			rank = (cards[i] - 1) % 13 + 1;
			// Groupのチェック
			if (state != State.SEQUENCE) {
				if (playerTypeOfHandsofCards[turnPlayer * 14 + rank] <= check) {
					weight[counter] = 1;
				}
			} else {
				if (playerTypeOfHandsofCards[turnPlayer * 14 + rank] <= 1) {
					weight[counter + 1] = 1;
				}
			}

			// SEQUUENCEの判定
			if (!checkSequence(rank, cards[i], false)) {
				weight[counter + 2] = 1;
				sequence = true;
			}
			if (!turnPlayerHaveJoker() && !sequence) {
				if (checkSequence(rank, cards[i], true)) {
					weight[counter + 3] = 1;
				}
			}

		}
	}

	/**
	 * 出したカードの種類を探索する
	 *
	 * @param weight
	 *            重みベクトル
	 * @param cards
	 *            　カード
	 */
	public int searchTypeOfCards(int[] weight, int[] cards, int size,
			int counter) {

		int rank = 0;// カードのランク

		for (int i = 0; i < size; i++) {
			rank = cards[i];
			if (!(rank > 64)) {// PASSではない時
				if (rank != 0) {// jokerで無いとき
					rank = (rank - 1) % 13 + 1;
				}
			}
			checkTypeOfCards(weight, rank);// カードのランク判定
		}
		return 15;
	}

	/**
	 * ランクごとの重みベクトルを格納
	 *
	 * @param weight
	 *            　重みの特徴ベクトル
	 * @param rank
	 *            　ランク
	 */
	private void checkTypeOfCards(int[] weight, int rank) {
		if (rank == 0) { // jokerの処理
			rank = 14;
		}
		rank--;
		switch (rank) {
		case 0:
			weight[rank] = 1;
			break;
		case 1:
			weight[rank] = 1;
			break;
		case 2:
			weight[rank] = 1;
			break;
		case 3:
			weight[rank] = 1;
			break;
		case 4:
			weight[rank] = 1;
			break;
		case 5:
			weight[rank] = 1;
			break;
		case 6:
			weight[rank] = 1;
			break;
		case 7:
			weight[rank] = 1;
			break;
		case 8:
			weight[rank] = 1;
			break;
		case 9:
			weight[rank] = 1;
			break;
		case 10:
			weight[rank] = 1;
			break;
		case 11:
			weight[rank] = 1;
			break;
		case 12:
			weight[rank] = 1;
			break;
		case 13:
			weight[rank] = 1;
			break;
		default:
			weight[14] = 1;
			break;
		}

	}

	/**
	 * 階段がつくれるかどうか
	 *
	 * @param rank
	 *            ランク
	 * @param card
	 *            　出したカード
	 * @param joker
	 *            jokerを持っているかどうか
	 * @return
	 */
	public boolean checkSequence(int rank, int card, boolean joker) {
		int counter = 0;
		boolean result = false;
		if (rank + 1 < 14 && playerHands[turnPlayer * cardNum + card + 1] >= 1) {
			counter++;
			if (rank + 2 < 14
					&& playerHands[turnPlayer * cardNum + card + 2] >= 1) {
				counter++;
			}
		}
		if (rank - 1 > 0 && playerHands[turnPlayer * cardNum + card - 1] >= 1) {
			counter++;
			if (rank - 2 > 0
					&& playerHands[turnPlayer * cardNum + card - 2] >= 1) {
				counter++;
			}
		}
		if (joker) {
			counter++;
		}
		if (counter >= 2) {
			result = true;
		}
		return result;
	}

	// getter setter
	public final int getPlayers() {
		return players;
	}

	public final int getMySeat() {
		return mySeat;
	}

	public final State getState() {
		return state;
	}

	public final boolean isLock() {
		return lock;
	}

	public final boolean[] getLockNumber() {
		return lockNumber;
	}

	public final boolean[] getPlaceSuits() {
		return placeSuits;
	}

	public final boolean[] getWonPlayer() {
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

	public final boolean[] getPassPlayer() {
		return passPlayer;
	}

	public final int[] getPlayerHands() {
		return playerHands;
	}

	public final int[] getPlayerHandsOfCards() {
		return playerHandsOfCards;
	}

	public final int[] getPlayerTypeOfHandsofCards() {
		return playerTypeOfHandsofCards;
	}

	public final boolean[] getFirstWonPlayer() {
		return firstWonPlayer;
	}

	public final void setPlayerHands(int[] playerHands) {
		this.playerHands = playerHands;
	}

	public final void setPlayerTypeOfHandsofCards(int[] playerTypeOfHandsofCards) {
		this.playerTypeOfHandsofCards = playerTypeOfHandsofCards;
	}

	public final GameField getFirstGf() {
		return firstGf;
	}

	public final void setFirstGf(GameField firstGf) {
		this.firstGf = firstGf;
	}

	public final void setMySeat(int mySeat) {
		this.mySeat = mySeat;
	}

	public void setSb(SimulationBalancing sb) {
		this.sb = sb;
	}

	public SimulationBalancing getSb() {
		return sb;
	}

	public Map<Integer, int[]> getPair() {
		return pair;
	}

	public int[] getGrade() {
		return grade;
	}

	public int[] getNotLookCard() {
		return notLookCards;
	}
}
