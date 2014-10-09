package object;

public class BitData {

	/** Suits関係のbitの数 **/
	public static final int SPADE = 0x01;
	public static final int HEART = 0x02;
	public static final int DAIYA = 0x04;
	public static final int CLOVER = 0x08;

	/*** player関係のbitの数 **/
	public static int PLAYER1 = 0x01;
	public static int PLAYER2 = 0x02;
	public static int PLAYER3 = 0x04;
	public static int PLAYER4 = 0x08;
	public static int PLAYER5 = 0x10;

	/**
	 * 対応した番号のSuitsを返すメソッド
	 *
	 * @param i
	 *            Suitsの番号
	 * @return Suitsの値
	 */
	public static int getLockNumber_SuitsNumber(int i) {
		switch (i) {
		case 0:// スペード
			return BitData.SPADE;
		case 1:// ハート
			return BitData.HEART;
		case 2:// ダイヤ
			return BitData.DAIYA;
		default:// クローバー
			return BitData.CLOVER;
		}
	}

	/**
	 * SuitNum のbitが入っているかどうかの判定
	 *
	 * @param suits
	 *            調べたいsuits
	 * @param SuitNum
	 *            Suitsの番号
	 * @return Suitsが入っているかどうかの判定
	 */
	public static boolean checkSuits_num(int suits, int SuitNum) {
		int num = suits & getLockNumber_SuitsNumber(SuitNum);
		if (num != 0) {
			return true;
		}
		return false;
	}

	/**
	 * 対応したプレイヤーの番号返すメソッド
	 *
	 * @param i
	 *            プレイヤーの番号
	 * @return プレイヤーの値
	 */
	public static int getPLayersNumber_i(int i) {
		switch (i) {
		case 0:
			return PLAYER1;
		case 1:

			return PLAYER2;
		case 2:

			return PLAYER3;
		case 3:

			return PLAYER4;

		default:
			return PLAYER5;
		}
	}

	/**
	 *　playersにnumのbitが入っているかどうかの判定
	 * @param players 調べたいプレイヤー
	 * @param num プレイヤー番号
	 * @return
	 */
	public static boolean checkPlayer_num(int players, int num) {
		int result = players & getPLayersNumber_i(num);
		if (result != 0) {
			return true;
		}
		return false;

	}

}
