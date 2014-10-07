package monteCalro;


import monteCalro.State;

public class Weight {
	/** 重みの特徴の数 **/
	private final int weighyNumber = 20;

	/***** 上から順に重みを入れる *****/

	/** カードの種類 15 **/

	/** カードの3が含まれている時 ***/
	private int THREE;
	/** カードの4が含まれている時 ****/
	private int FOUR;
	/*** カードの5が含まれている時 ****/
	private int FIVE;
	/*** カードの6が含まれている時 *****/
	private int SIX;
	/*** カードの7が含まれている時 *****/
	private int SEVEN;
	/*** カードの8が含まれている時 *****/
	private int EIGHT;
	/*** カードの9が含まれている時 *****/
	private int NINE;
	/*** カードの10が含まれている時 *****/
	private int TEN;
	/*** カードのJが含まれている時 *****/
	private int JACK;
	/*** カードのQが含まれている時 *****/
	private int QUEEN;
	/*** カードのKが含まれている時 *****/
	private int KING;
	/*** カードのAが含まれている時 *****/
	private int ONE;
	/*** カードの2が含まれている時 *****/
	private int TWO;
	/*** カードにjokerが含まれている時 ***/
	private int JOKER;
	/*** PASSの時 ***/
	private int PASS;

	/*** 場の状態の変数 2 ***/

	/*** renewかどうか ***/
	private int isRenew;
	/*** reverseかどうか ***/
	private int isRevrse;

	/*** 自分の行動関係 2 ***/

	/*** 縛りを行える時 ***/
	private int canLock;
	/*** 革命が起こせる時 ***/
	private int canReverse;

	/*** 自分の手札状態 1 ***/

	/*** Jokerを持っている時 ***/
	private int haveJoker;

	/***
	 * コンストラクタ
	 *
	 * @param cards
	 *            カード集合
	 * @param gf
	 *            GameFeild
	 */
	public Weight(int[] cards, GameField gf) {
		searchCondition(cards, gf);
		;
	}

	/**
	 * 重みの特徴が存在するかどうかの判定
	 *
	 * @param cards
	 * @param gf
	 */
	public void searchCondition(int[] cards, GameField gf) {
		init(); // 変数の初期化
		searchTypeOfCards(cards);// カードの種類の重みを調べる
		examinePlace(gf);// 場の状態を調べる
		myPlayerAction(cards, gf);// 自分の行動に関数重みを調べる　　
		searchMyHandInformation(gf);// 自分の手札情報を調べる

	}

	/**
	 * 初期化メソッド
	 */
	public void init() {
		THREE = 0;
		FOUR = 0;
		FIVE = 0;
		SIX = 0;
		SEVEN = 0;
		EIGHT = 0;
		NINE = 0;
		TEN = 0;
		JACK = 0;
		QUEEN = 0;
		KING = 0;
		ONE = 0;
		TWO = 0;
		JOKER = 0;
		PASS = 0;

		isRenew = 0;
		isRevrse = 0;
		canLock = 0;
		canReverse = 0;

		haveJoker = 0;
	}

	/***
	 * 自分の手札関係の探索
	 */
	public void searchMyHandInformation(GameField gf) {
		if (gf.isHaveJoker_myHand()) {// jokerを持っている時の処理
			haveJoker = 1;
		}
	}

	/**
	 * 自分の行動関係の判定を行うメソッド
	 */
	public void myPlayerAction(int cards[], GameField gf) {
		canReverse(cards); // リバース出来るかの判定を行う
		canLock(cards,gf);//縛りの判定
	}

	/**
	 *
	 * @param cards
	 *            　革命できるかの判定を行うメソッド
	 */
	private void canReverse(int[] cards) {
		int size = cards.length;
		if (size >= 4) {
			int x = (cards[0] - 1) % 13 + 1;
			int y = (cards[1] - 1) % 13 + 1;
			int z = (cards[2] - 1) % 13 + 1;
			if (x != 0) {
				if (y != 0) {
					if (x != y) {// 階段
						if (size >= 5) {
							canReverse = 1;
						}
					} else {// Group
						canReverse = 1;

					}
				} else {
					if (x != z) {// 階段
						if (size >= 5) {
							canReverse = 1;
						}
					} else {// Group
						canReverse = 1;
					}
				}
			} else {
				if (y != z) {// 階段
					if (size >= 5) {
						canReverse = 1;
					}
				} else {// Group
					canReverse = 1;
				}
			}
		}
	}
	/**
	 * 縛りが出来るかの判定を行う
	 * @param cards
	 * @param gf
	 */
	private void canLock(int[] cards, GameField gf) {
		if(!gf.isLock()){//縛りではない時
			int size = cards.length;
			int num = 0;
			boolean[] place = gf.getPlaceSuits();
			canLock =1;
			for (int i = 0; i < size; i++) {
				if(num ==0){ //jokerの時の処理
					continue;
				}
				num = (cards[i]-1) /13;
				if(!place[num]){
					canLock=0;
					break;
				}
			}
		}
	}

	/**
	 * 場の状態に関数の情報を探索する
	 *
	 * @param gf
	 *            GameFeild
	 */
	public void examinePlace(GameField gf) {
		if (gf.getState() == State.RENEW) {// 状態が ReNewの時
			isRenew = 1;
		}
		if (gf.isReverse()) {// 革命状態かどうか
			isRevrse = 1;
		}
	}

	/**
	 * 出したカードの種類を探索する
	 *
	 * @param cards
	 */
	public void searchTypeOfCards(int[] cards) {
		int size = cards.length; // カードの枚数
		int rank = 0;// カードのランク

		for (int i = 0; i < size; i++) {
			rank = cards[i];
			if (!(rank > 64)) {// PASSではない時
				if (rank != 0) {// jokerで無いとき
					rank = (rank - 1) % 13 + 1;
				}
			}
			changeTypeOfCard(rank);// カードのランク判定
		}
	}

	/**
	 * カードのランクからどのカードを使用したのかを判定する
	 *
	 * @param rank
	 */
	public void changeTypeOfCard(int rank) {
		switch (rank) {
		case 0:// Jokerの時
			JOKER = 1;
			break;
		case 1:// 3のランクの時
			THREE = 1;
			break;
		case 2:// 4のランクの時
			FOUR = 1;
			break;
		case 3:// 5のランクの時
			FIVE = 1;
			break;
		case 4:// 6のランクの時
			SIX = 1;
			break;
		case 5:// 7のランクの時
			SEVEN = 1;
			break;
		case 6:// 8のランクの時
			EIGHT = 1;
			break;
		case 7:// 9のランクの時
			NINE = 1;
			break;
		case 8:// 10のランクの時
			TEN = 1;
			break;
		case 9:// jのランクの時
			JACK = 1;
			break;
		case 10:// Qのランクの時
			QUEEN = 1;
			break;
		case 11:// Kのランクの時
			KING = 1;
			break;
		case 12:// Aの時
			ONE = 1;
			break;
		case 13:// 2のランクの時
			TWO = 1;
			break;
		default:// PASSの時
			PASS = 1;
			break;
		}

	}

	/**
	 * ある場所の重さの特徴を返す
	 *
	 * @param pos
	 *            見たい特徴の場所
	 * @return
	 */
	public int returnWeight(int pos) {
		switch (pos) {
		case 0:
			return getTHREE();
		case 1:
			return getFOUR();
		case 2:
			return getFIVE();
		case 3:
			return getSIX();
		case 4:
			return getSEVEN();
		case 5:
			return getEIGHT();
		case 6:
			return getNINE();
		case 7:
			return getTEN();
		case 8:
			return getJACK();
		case 9:
			return getQUEEN();
		case 10:
			return getKING();
		case 11:
			return getONE();
		case 12:
			return getTWO();
		case 13:
			return getJOKER();
		case 14:
			return getPASS();
		case 15:
			return getIsRenew();
		case 16:
			return getIsRevrse();
		case 17:
			return getCanLock();
		case 18:
			return getCanReverse();
		case 19:
			return getHaveJoker();
		default:
			break;
		}
		return 0;
	}

	public int getWeighyNumber() {
		return weighyNumber;
	}

	public int getTHREE() {
		return THREE;
	}

	public int getFOUR() {
		return FOUR;
	}

	public int getFIVE() {
		return FIVE;
	}

	public int getSIX() {
		return SIX;
	}

	public int getSEVEN() {
		return SEVEN;
	}

	public int getEIGHT() {
		return EIGHT;
	}

	public int getNINE() {
		return NINE;
	}

	public int getTEN() {
		return TEN;
	}

	public int getJACK() {
		return JACK;
	}

	public int getQUEEN() {
		return QUEEN;
	}

	public int getKING() {
		return KING;
	}

	public int getONE() {
		return ONE;
	}

	public int getTWO() {
		return TWO;
	}

	public int getJOKER() {
		return JOKER;
	}

	public int getPASS() {
		return PASS;
	}

	public int getIsRenew() {
		return isRenew;
	}

	public int getIsRevrse() {
		return isRevrse;
	}

	public int getCanLock() {
		return canLock;
	}

	public int getCanReverse() {
		return canReverse;
	}

	public int getHaveJoker() {
		return haveJoker;
	}

}