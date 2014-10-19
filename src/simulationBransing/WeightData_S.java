package simulationBransing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import object.InitSetting;

public class WeightData_S {

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
	private final String[] textNames = {
			"./pai_Sita/1text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/2text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/3text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/4text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/5text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/r1text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/r2text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/r3text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/r4text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/r5text_" + InitSetting.WEIGHTNUMBER + ".txt"
	};

	/**
	 * コンストラクタ
	 */
	public WeightData_S() {
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
		double[] result = null;
		switch (num) {
		case 0:
			if (text1_19.containsKey(authenticationCode)) {
				result = text1_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			text1_19.put(authenticationCode, weight);
			break;
		case 1:
			if (text2_19.containsKey(authenticationCode)) {
				result = text2_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			text2_19.put(authenticationCode, weight);
			break;
		case 2:
			if (text3_19.containsKey(authenticationCode)) {
				result = text3_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			text3_19.put(authenticationCode, weight);
			break;
		case 3:
			if (text4_19.containsKey(authenticationCode)) {
				result = text4_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			text4_19.put(authenticationCode, weight);
			break;
		case 4:
			if (text5_19.containsKey(authenticationCode)) {
				result = text5_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			text5_19.put(authenticationCode, weight);
			break;
		case 5:
			if (textr1_19.containsKey(authenticationCode)) {
				result = textr1_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			textr1_19.put(authenticationCode, weight);
			break;
		case 6:
			if (textr2_19.containsKey(authenticationCode)) {
				result = textr2_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			textr2_19.put(authenticationCode, weight);
			break;
		case 7:
			if (textr3_19.containsKey(authenticationCode)) {
				result = textr3_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			textr3_19.put(authenticationCode, weight);
			break;
		case 8:
			if (textr4_19.containsKey(authenticationCode)) {
				result = textr4_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
			textr4_19.put(authenticationCode, weight);
			break;
		case 9:
			if (textr5_19.containsKey(authenticationCode)) {
				result = textr5_19.remove(authenticationCode);
				int size = weight.length;
				for (int i = 0; i < size; i++) {
					weight[i] += result[i];
				}
			}
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
		 * int allHands = authenticationCode % 100;
		 * int players = authenticationCode / 100 % 10;
		 * int myHands = authenticationCode / 1000;
		 */
		HashMap<Integer, double[]> map = null;
		double[] result = new double[InitSetting.WEIGHTNUMBER];

		if (reverse)
			myRank += 5;
		map = returnMap(myRank);
		if (!map.containsKey(authenticationCode)) {
			return result;
		}
		return map.get(authenticationCode);
	}

	public void writeText() {
		int size = textNames.length;
		StringBuffer sb;
		File file;FileWriter fw= null;
		BufferedWriter bw= null;
		HashMap<Integer, double[]> map;
		for (int i = 0; i < size; i++) {
			file = new File(textNames[i]);
			try {
				bw = new BufferedWriter(new FileWriter(file)) ;
				map = returnMap(i + 1);
				for(int num: map.keySet()){
					sb = new StringBuffer();
					sb.append(num);
					for(double d : map.get(num)){
						sb.append(",");
						sb.append(d);
					}
					bw.write(sb.toString());
					bw.newLine();
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}finally{
				try {
					bw.close();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}
	}

	public HashMap<Integer, double[]> returnMap(int num) {
		HashMap<Integer, double[]> map = null;
		switch (num) {
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

			return null;
		}
		return map;
	}

}
