package monteCalro;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jp.ac.uec.daihinmin.Place;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;

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
	/** 3～2の数を0～12で表したもの -1がrenew **/
	private int rank;// 場に出されているランク

	/** 革命している時はtrue **/
	private boolean reverse;// 革命しているか否か

	private int numberOfCardSize;// 場に出されているカードの枚数

	private int turnPlayer; // ターンプレイヤー

	private int putLastPlayer;// 最後に出した人の座席番号

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

	/*** 一番初めのGameFieldクラスを作成 ****/
	private GameField firstGf;







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

		mySeat = md.getSeat();

		turnPlayer = mySeat;

		init(bs, fd);


	}

	/**
	 * コンストラクタ(clone用)
	 *
	 * @param gf
	 *            cloneするGameFieldクラス
	 */
	public GameField(GameField gf) {
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

		System.arraycopy(gf.getLockNumber(), 0, lockNumber, 0, suitsNum);
		System.arraycopy(gf.getPlaceSuits(), 0, placeSuits, 0, suitsNum);
		System.arraycopy(gf.getWonPlayer(), 0, wonPlayer, 0, players);
		System.arraycopy(gf.getPassPlayer(), 0, passPlayer, 0, players);
		System.arraycopy(gf.getPlayerHandsOfCards(), 0, playerHandsOfCards, 0, players);
		System.arraycopy(gf.getFirstWonPlayer(), 0, firstWonPlayer, 0, players);
		System.arraycopy(gf.getPlayerHands(), 0, playerHands, 0, sumPlayersHands);
		int num = players * 14;
		System.arraycopy(gf.getPlayerTypeOfHandsofCards(), 0, playerTypeOfHandsofCards, 0, num);

	}

	/**
	 * 空のコンストラクタ
	 */
	public GameField() {
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
		}
		pos++;
		// numberOfCardSize
		numberOfCardSize = Integer.parseInt(gameRecord.substring(pos, pos + 1));
		pos++;
		// turmPlayer
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
				wonPlayer[i]= false;
			}
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

		System.arraycopy(gf.getLockNumber(), 0, lockNumber, 0, suitsNum);
		System.arraycopy(gf.getPlaceSuits(), 0, placeSuits, 0, suitsNum);
		System.arraycopy(gf.getWonPlayer(), 0, wonPlayer, 0, players);
		System.arraycopy(gf.getPassPlayer(), 0, passPlayer, 0, players);
		System.arraycopy(gf.getPlayerHandsOfCards(), 0, playerHandsOfCards, 0, players);
		System.arraycopy(gf.getFirstWonPlayer(), 0, firstWonPlayer, 0, players);
		System.arraycopy(gf.getPlayerHands(), 0, playerHands, 0, sumPlayersHands);
		int num = players * 14;
		System.arraycopy(gf.getPlayerTypeOfHandsofCards(), 0, playerTypeOfHandsofCards, 0, num);

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
		} else {// それ以外
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

	public boolean checkEight() {
		boolean result = false;
		if (state != State.SEQUENCE) { // 階段以外の時
			if (rank == 6)
				result = true;
		} else {
			if (!reverse) {// 革命じゃない時
				if (rank - 1 + numberOfCardSize >= 6)
					result = true;
			} else {// 革命の時
				if (rank + 1 - numberOfCardSize <= 6)
					result = true;
			}
		}
		return result;
	}

	/**
	 * 場の状態を見て自分の出す手を決めて、場の更新を行う
	 */
	public void putToSeeStateOfField() {
		int[] putHand = null; // 出した手を格納
		if (!passPlayer[turnPlayer]) {// そのプレイヤーがパスの状態だった場合
			switch (state) {
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
			// PASSプレイヤーの更新
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
	public void updatePlace(int[] putHand) {

		int size = putHand.length;
		int num = 0;
		// カードサイズの更新
		numberOfCardSize = size;
		// 最後に出した人の更新
		putLastPlayer = turnPlayer; // 最後に出した人を更新

		if (state == State.RENEW) {// Renewの時の更新部

			state = getState(putHand);

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
				boolean joker = false;
				for (int i = 0; i < size; i++) {
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
			if (putHand[0] != 0) {
				rank = (putHand[0] - 1) % 13 + 1;
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
		int x = 0;
		int y = 0;
		State r = State.EMPTY;
		if (size == 1) {// カードが一枚の時
			r = State.SINGLE;
		} else {
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
		if (r == State.EMPTY) {
			System.out.println("");
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
		//TODO renewと革命の時に何故かrankが0になっている
		if(state == State.RENEW){
			if(reverse){
				rank = 14;
			}else{
				rank=0;
			}
		}
		// スペ3の判定
		if (state != State.RENEW && (rank == 14 || rank == 0)) {// JOKERが場に出されている時
			if (playerHands[turnPlayer * cardNum + 1] == 1) { // スぺ3を持っている時
				array = new int[1];
				array[0] = 1;
				return array;// jokerを出されてスぺ3のカードがない場合は出すカードが存在しないので
			}
		}
		int firstPos = turnPlayer * 14;
		int x = 53 / players + 1;
		array = new int[x]; // 自分の手札に持てる数分の配列を作る
		for (int i = 0; i < x; i++) {
			array[i] = 512;// ありえない数
		}
		int counter = 0;
		if (playerTypeOfHandsofCards[firstPos] == 1) {// jokerを持っている時
			array[counter] = 0;
			counter++;
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
							array[counter] = num;
							counter++;
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
							array[counter] = num;
							counter++;
						}
					}
			}
		}
		if (counter == 0 ) {// 出せる役がない時
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
		int[][] resultArray = searchGroupMeld(numberOfCardSize);// グループのペアを探す
		if (resultArray == null)
			return null;// PASS
		int size = resultArray.length;

		size++;// PASSを追加

		int random = (int) (Math.random() * size);

		if (random == size - 1) {// PASSの時
			return null;
		}
		return resultArray[random].clone();

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
		int firstPos = turnPlayer * cardNum;
		int pthc = turnPlayer * 14;
		if (playerTypeOfHandsofCards[pthc] == 1) {// jokerを持っている時
			joker = true;
			jk = 1;
		}
		int counter = 0;// 配列の番号を記憶する
		int resultCounter = 0;
		int num = 0;
		int x = 0;// 計算用の変数
		int[] conbine = new int[5];// カード枚数
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
					resultCounter = combination(resultArray, meld, conbine, c,
							1, num, size, resultCounter);

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
		boolean result = true;
		int size = meld.length;

		int num = 0;
		for (int i = 0; i < size; i++) {
			if (meld[i] == 0) {// jokerの時
				continue;
			}
			num = (meld[i] - 1) / 13;// markを抽出
			if (!lockNumber[num]) {// 縛りの数の時
				result = false;
				break;
			}
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
							continue;// ここは見ない

						resultCounter = searchSequence(resultArray, meld,
								counter, num, cardSize, joker, resultCounter,
								false, firstPos);// 階段の探索を行う
						meld = new int[cardSize];
					}
				}
			}
		} else {// 革命の時
			for (int i = defalt; i > cardSize - 1; i--) {// カードでの探索
				for (int j = 0; j < 4; j++) {
					num = i + j * 13;// カードを表現
					if (playerHands[turnPlayer * cardNum + num] == 1) {// そのカードを持っている時
						if (lock && !lockNumber[j])// 縛りが存在しており、縛られているカードではない場合
							continue;// ここは見ない

						resultCounter = searchSequence(resultArray, meld,
								counter, num, cardSize, joker, resultCounter,
								false, firstPos);// 階段の探索を行う
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
	 * @param firstPos
	 *            playerHandsなどの最初の位置を記憶
	 * @return　resultCounnter
	 */
	private int searchSequence(int[][] result, int[] meld, int counter,
			int num, int size, boolean joker, int resultCounter, boolean dojoker, int firstPos) {
		if (!dojoker) { // jokerを使わな勝った時
			meld[counter] = num;

		} else {
			meld[counter] = 0;
		}

		counter++;

		if (counter == size) {// 役が成立した時
			result[resultCounter] = meld.clone();
			resultCounter++;
			return resultCounter;
		}

		if (!reverse) {// 普通の時
			num++;
			if (playerHands[firstPos + num] == 1) {// 2より上は見ない
				meld[counter] = num;
				resultCounter = searchSequence(result, meld, counter, num,
						size, joker, resultCounter, false, firstPos);
			}
			num--;
		} else {// 革命が起きている時
			num--;
			if (playerHands[firstPos + num] == 1) {// 3より下は見ない
				meld[counter] = num;
				resultCounter = searchSequence(result, meld, counter, num,
						size, joker, resultCounter, false, firstPos);
			}
			num++;
		}

		if (joker) {// jokerを持っている時
			joker = false;
			resultCounter = searchSequence(result, meld, counter, num, size,
					joker, resultCounter, true, firstPos);
		}

		return resultCounter;
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
					num = (md.getArrayCards(i) - 1) / 13;// markを取る
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
				num = md.getArrayCards(i);
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
			int x = 0;
			for (int i = 0; i < numberOfCardSize; i++) {
				num = md.getArrayCards(i);

				playerHands[firstPos + num]--;
				if (num != 0)
					num = (num - 1) % 13 + 1;
				x = ptch + num;
				playerTypeOfHandsofCards[ptch + num]--;
			}

			playerHandsOfCards[turnPlayer] -= numberOfCardSize;

		} else {// PASSした時
			passPlayer[turnPlayer] = true;
		}
	}

	/**
	 * renew時の決定した役を返すメソッド
	 *
	 * @return 出す手を返す
	 */
	private int[] returnResult_renewMeld() {

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
		for (int i = 2; i <= 5; i++) {// ペア出しは2～5枚しか存在しないため
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
		int num = 53 / players + 1;
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
	 * renew時の出せる全ての役を返すメソッド
	 *
	 * @return 出せるすべての役の配列を返す
	 */
	private int[][] returnAllResult_renewMeld() {

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
		int num = 53 / players + 1;
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
		int[][] result1 = new int[arrayCounter][];
		for (int i = 0; i < arrayCounter; i++) {
			result1[i] = resultArray[i];
		}

		return result1;
	}

	/**
	 * 出せる全てのSingleMeldを返す またPASSの手を256で返す
	 *
	 * @return resultArray 出せるすべての単体出しの2次元配列
	 */
	public int[][] returnAllSingleMeld() {
		int[] result = searchSingleMeld();// 出せる役の結果
		int size;
		if (result == null) {
			size = 0;
		} else {
			size = result.length;
		}
		int arrayCounter = 0;
		int[][] resultArray = new int[size + 1][1];
		/** それぞれの単体出し、ペア出し、階段出しの役を格納していく **/
		// 単体出し格納
		for (int i = 0; i < size; i++) {
			resultArray[arrayCounter][0] = result[i]; // 単体出しのカードを格納
			arrayCounter++;
		}
		resultArray[size][0] = 256; // PASSを入れてあげる

		return resultArray;

	}

	/**
	 * その場で出せる手を返すメソッド
	 *
	 * @return
	 */
	public int[][] getPutHand() {
		int[][] resultArray = null;
		int[][] cloneResult = null;
		int size = 0;
		// 今回PASSは一番最後の配列が256の時とした
		if (!passPlayer[turnPlayer]) {// そのプレイヤーがパスの状態だった場合
			switch (state) {
			case SINGLE:// 単体出しの時
				resultArray = returnAllSingleMeld();// 単体出しで出す役を受け取る
				break;
			case GROUP:// ペア出しの時
				cloneResult = searchGroupMeld(numberOfCardSize);// ペア出しで出す役を受け取る
				if (cloneResult != null) {
					size = cloneResult.length;
				} else {
					size = 0;
				}
				resultArray = new int[size + 1][];
				for (int i = 0; i < size; i++) {// コピーしてあげる
					resultArray[i] = cloneResult[i];
				}
				resultArray[size] = new int[] { 256 };

				break;
			case SEQUENCE:// 階段出しの時
				cloneResult = searchSequenceMeld(numberOfCardSize);// ペア出しで出す役を受け取る
				if (cloneResult == null) {
					size = 0;
				} else {
					size = cloneResult.length;
				}
				resultArray = new int[size + 1][];
				for (int i = 0; i < size; i++) {// コピーしてあげる
					resultArray[i] = cloneResult[i];
				}
				resultArray[size] = new int[] { 256 };

				break;
			case RENEW:// renewの時
				resultArray = returnAllResult_renewMeld();// renew時に出す役を受ける
				break;
			default:
				System.out
						.println("エラー発生　MonteCalro.java putToSeeStateOfFieldメソッド　：");
				break;
			}
		}

		if (resultArray == null) {// PASSPlayerの時
			resultArray = new int[1][1];
			resultArray[0][0] = 256;
			return resultArray;
		}
		return resultArray;
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
			file = new File("./gameRecord/myHand_" + myHands + ".txt");
			message += myHands;
		} else {// サイズの大きさが一桁の時
			file = new File("./gameRecord/myHand_0" + myHands + ".txt");
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


}
