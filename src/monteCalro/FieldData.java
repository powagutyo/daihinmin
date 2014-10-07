package monteCalro;

/**
 * フィールドの情報を管理する
 *
 * @author 伸也
 *
 */
public class FieldData {

	private final int players = 5;

	private boolean[] passPlayer;// passしたプレイヤーをtrue

	private boolean[] wonPlayer;//勝ったプレイヤー　trueが勝ったプレイヤー

	private int[] seatsHandSize;// 席順の手札の枚数を表す

	private int[] grade;// 席の順位

	private int[] bigMillionaire; // 大富豪のカード群

	private int millionaire; // 富豪のカード

	private int putLastPlayer; // 最後に出した人の座席番号を記憶する変数 　renewの時は-1

	/**
	 * コンストラクタ
	 */
	public FieldData() {

		passPlayer = new boolean[players];

		wonPlayer = new boolean[players];

		seatsHandSize = new int[players];

		grade = new int[players];

		bigMillionaire = new int[2];

		putLastPlayer = -1;

		for (int i = 0; i < players; i++) {// 初期状態は平民
			grade[i] = 3;
		}
		init();
	}

	/**
	 * 初期化
	 */
	public void init() {
		for (int i = 0; i < players; i++) {
			seatsHandSize[i] = 0;
		}
		initPlayerInformation();

		millionaire = -1;

		for (int i = 0; i < 2; i++) {
			bigMillionaire[i] = -1;
		}
	}

	/**
	 * PassPlayerの初期化
	 */
	public void initPlayerInformation() {
		for (int i = 0; i < players; i++) {
			passPlayer[i] = false;
			wonPlayer[i] = false;
		}
	}

	/***
	 * 大富豪と富豪の手札を補正する
	 */
	public void compensateHandSize() {
		for (int i = 0; i < players; i++) {
			if (grade[i] == 1) {// 大富豪の時
				seatsHandSize[i] -= 2;
			} else if (grade[i] == 2) {// 富豪の時
				seatsHandSize[i]--;
			}

		}
	}

	/**
	 * seatHandSizeの指定した座席のカード枚数を減らすメソッド
	 *
	 * @param seat
	 *            座席
	 * @param cards
	 *            出されたカード枚数
	 */
	public void takeOutHandCards(int seat, int cards) {
		seatsHandSize[seat] -= cards;
	}

	/**
	 * PASSしたプレイヤーを更新させる
	 *
	 * @param passPlayer
	 *            //パスしたプレイヤー番号
	 */
	public void setPassPlayer(int passPlayer) {
		this.passPlayer[passPlayer] = true;
	}

	/**
	 * 座席を指定し、手札の大きさを格納する
	 *
	 * @param seat
	 *            座席
	 * @param handSize
	 *            手札の大きさ
	 */
	public void setSeatsHandSIze(int seat, int handSize) {

		seatsHandSize[seat] = handSize;
	}

	/***
	 * 座席にランクを入れる
	 *
	 * @param seat
	 *            座席
	 * @param rank
	 *            ランク
	 */
	public void setGrade(int seat, int rank) {
		grade[seat] = rank;
	}


	/**
	 * 座席の位置のwinPlayerを勝ちにしてあげる
	 *
	 * @param pos
	 *            　座席のプレイヤーを勝ちにしてあげる
	 */
	public void setWonPlayer(int pos) {
		this.wonPlayer[pos] = true;
	}

	/**
	 * 座席のカード枚数を返すメソッド
	 *
	 * @param pos
	 *            座席番号
	 * @return その座席のカード枚数
	 */
	public int getSeatsHandSize(int pos) {
		return seatsHandSize[pos];
	}
	/**
	 * 大富豪の出したカードでランクが2より上のカードを記憶する
	 *
	 * @param num 大富豪が出した高いカード
	 */

	public void setBigMillionaire(int num) {
		for (int i = 0; i < 2; i++) {
			if(bigMillionaire[i] == -1){
				bigMillionaire[i] =num;
				break;
			}
		}
	}


	/** getter setter群 **/
	public boolean[] getPassPlayer() {
		return passPlayer.clone();
	}

	public int[] getGrade() {
		return grade.clone();
	}

	public int[] getSeatsHandSize() {
		return seatsHandSize.clone();
	}

	public int getPutLastPlayer() {
		return putLastPlayer;
	}

	public boolean[] getWonPlayer() {
		return wonPlayer.clone();
	}


	public void setPutLastPlayer(int putLastPlayer) {
		this.putLastPlayer = putLastPlayer;
	}

	public int[] getBigMillionaire() {
		return bigMillionaire.clone();
	}


	public int getMillionaire() {
		return millionaire;
	}

	public void setMillionaire(int num) {
		this.millionaire = num;
	}

}
