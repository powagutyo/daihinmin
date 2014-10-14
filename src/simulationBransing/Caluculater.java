package simulationBransing;

import object.InitSetting;

public class Caluculater {
	/**
	 * πΘの計算
	 *
	 * @param wegiht
	 * @return
	 */

	public static double arufa = 1.0;

	/**
	 * Bの配列をAの配列に分ける
	 *
	 * @param A
	 * @param B
	 * @param parsent
	 *            比率
	 * @return
	 */
	public static double[] ratioAB(double[] A, double[] B, double parsent) {
		double a = 0;
		double b = 0;
		int size = B.length;
		for (int i = 0; i < size; i++) {
			a += A[i];
			b += B[i];
		}
		for (int i = 0; i < size; i++) {
			B[i] = B[i] * a * parsent / b;
		}

		return B;
	}

	public static double calcPai_sita(double[] sita, int[] wegiht) {

		double result = 0;
		for (int i = 0; i < InitSetting.WEIGHTNUMBER; i++) {
			result += sita[i] * wegiht[i]; // ベクトルの内積を計算
		}
		return result;
	}

	public static double[] scailingPai_sita(double[] pai_sita) {
		double max = -1024;
		double min = 1024;
		int size = pai_sita.length;
		for (int i = 0; i < size; i++) {
			if (max < pai_sita[i])
				max = pai_sita[i];
			if (min > pai_sita[i])
				min = pai_sita[i];
		}
		if (size == 1)
			return pai_sita;
		if (max == min) {
			return pai_sita;
		}
		// スケーリング
		for (int i = 0; i < size; i++) {
			pai_sita[i] = scailing(pai_sita[i], max, min, (double) size);
		}

		return pai_sita;
	}

	public static double scailing(double f, double maxF, double minF, double size) {

		return f * arufa + ((maxF - minF * size) / (size - 1.0));
	}

	/**
	 *
	 * @param playout
	 *            　現在のプレイアウト数
	 * @param visit
	 *            　訪問回数
	 * @param point
	 *            勝ち点
	 * @param playerNum
	 *            今いるプレイヤーの人数
	 * @return
	 */
	public static double calcUCB_TUNED(double playout, GameField gf) {
		double UCB = 0.0;
		double visit = (double)gf.getVisit();
		double point = (double)gf.getWon();
		double num = 0.0;
		double winPro;
		if (visit == 0) {// niが0の時は0.5とする
			return gf.getUCB();
		} else {
			winPro = point / (5.0 * visit);
		}

		for (int i = 0; i < 5; i++) {
			num += Math.pow((i + 1) / 5.0, 2) * gf.returnWinPoints(i+1);
		}
		
		double x = num / visit;

		double y = Math.pow(winPro, 2);

		double z = Math.sqrt(2.0 * Math.log(playout) / visit);

		num = x - y + z;
		if (num > 0.25) {
			num = 0.25;
		}
		/**
		 * UCBの計算式
		 */
		UCB = winPro + Math.sqrt(num *  Math.log(playout) / visit);
		if(Double.isNaN(UCB)){
			System.out.println();
		}
		return UCB;
	}
}
