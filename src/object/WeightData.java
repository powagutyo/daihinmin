package object;

import java.util.HashMap;

public class WeightData {

	private HashMap<Integer, double[]> text1_19;
	private HashMap<Integer, double[]> text2_19;
	private HashMap<Integer, double[]> text3_19;
	private HashMap<Integer, double[]> text4_19;
	private HashMap<Integer, double[]> text5_19;
	private HashMap<Integer, double[]> textr1_19;
	private HashMap<Integer, double[]> textr2_19;
	private HashMap<Integer, double[]> textr3_19;
	private HashMap<Integer, double[]> textr4_19;
	private HashMap<Integer, double[]> textr5_19;

	/** 読み込みが終了したかどうかの判定用 ***/
	private boolean finish = false;

	/**
	 * コンストラクタ
	 */
	public WeightData() {
		text1_19 = new HashMap<Integer, double[]>();
		text2_19 = new HashMap<Integer, double[]>();
		text3_19 = new HashMap<Integer, double[]>();
		text4_19 = new HashMap<Integer, double[]>();
		text5_19 = new HashMap<Integer, double[]>();

		textr1_19 = new HashMap<Integer, double[]>();
		textr2_19 = new HashMap<Integer, double[]>();
		textr3_19 = new HashMap<Integer, double[]>();
		textr4_19 = new HashMap<Integer, double[]>();
		textr5_19 = new HashMap<Integer, double[]>();
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
		switch (num) {
		case 0:
			text1_19.put(authenticationCode, weight);
			break;
		case 1:
			text2_19.put(authenticationCode, weight);
			break;
		case 2:
			text3_19.put(authenticationCode, weight);
			break;
		case 3:
			text4_19.put(authenticationCode, weight);
			break;
		case 4:
			text5_19.put(authenticationCode, weight);
			break;
		case 5:
			textr1_19.put(authenticationCode, weight);
			break;
		case 6:
			textr2_19.put(authenticationCode, weight);
			break;
		case 7:
			textr3_19.put(authenticationCode, weight);
			break;
		case 8:
			textr4_19.put(authenticationCode, weight);
			break;
		case 9:
			textr5_19.put(authenticationCode, weight);
			break;

		default:
			break;
		}
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
	public double[] getWeight(int myRank, boolean reverse, int authenticationCode) {
		/*
		int allHands = authenticationCode % 100;
		int players = authenticationCode / 100 % 10;
		int myHands = authenticationCode / 1000;
		 */
		HashMap<Integer, double[]> map = null;
		double[] result = new double[InitSetting.WEIGHTNUMBER];

		if (reverse)
			myRank += 5;
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
