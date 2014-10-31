package object;

import java.util.ArrayList;
import java.util.HashMap;

public class WeightData {
	private ArrayList<HashMap<Integer, double[]>> text;

	private final static int textSize = 16;

	/** 読み込みが終了したかどうかの判定用 ***/
	private boolean finish = false;

	/**
	 * コンストラクタ
	 */
	public WeightData() {
		text = new ArrayList<HashMap<Integer, double[]>>();
		for (int i = 0; i < textSize; i++) {
			text.add(new HashMap<Integer, double[]>());
		}
	}

	/**
	 * 重みのHashMapを作成するメソッド
	 *
	 * @param num
	 *            　ナンバー
	 * @param authenticationCode
	 *            key
	 * @param weight
	 *            重さ
	 */
	public void setWeight(int num, int authenticationCode, double[] weight) {
		text.get(num).put(authenticationCode, weight.clone());
	}

	/**
	 * 自分のランクと革命から認証コードを用いて重さを返すメソッド
	 *
	 * @param myRank
	 *            自分のランク
	 * @param reverse
	 *            革命
	 * @param authenticationCode
	 *            認証コード
	 * @return
	 */
<<<<<<< Updated upstream
	public double[] getWeight(int key, int authenticationCode) {
=======
	public double[] getWeight(int myRank, int authenticationCode) {
>>>>>>> Stashed changes

		HashMap<Integer, double[]> map = null;
		map = text.get(key);
		double[] result = new double[InitSetting.WEIGHTNUMBER];

<<<<<<< Updated upstream
		if (!map.containsKey(authenticationCode)) {
=======

		switch (myRank) {
		case 1:
			map = text1_19;
			break;
		case 2:
			map = text2_19;
			break;
		case 3:
			map = text3_19;
			break;
		case 4:
			map = text4_19;
			break;
		case 5:
			map = text5_19;
			break;
		case 6:
			map = textr1_19;
			break;
		case 7:
			map = textr2_19;
			break;
		case 8:
			map = textr3_19;
			break;
		case 9:
			map = textr4_19;
			break;
		case 10:
			map = textr5_19;
			break;

		default:
			result = new double[InitSetting.WEIGHTNUMBER];
			for (int i = 0; i < InitSetting.WEIGHTNUMBER ; i++) {
				result[i] = 0;
			}
			return result;
		}
		if(!map.containsKey(authenticationCode)){
			result = new double[InitSetting.WEIGHTNUMBER];
			for (int i = 0; i < InitSetting.WEIGHTNUMBER; i++) {
				result[i] = 0;
			}
			return result;
>>>>>>> Stashed changes

			return null;
		}
		return map.get(authenticationCode);
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

}
