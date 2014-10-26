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
	private final String[] textNames;

	/**
	 * コンストラクタ
	 */
	public WeightData_S() {
		textNames = new String[4];
		textNames[0] = "./pai_Sita/00text_" + InitSetting.WEIGHTNUMBER + ".txt";
		textNames[1] = "./pai_Sita/01text_" + InitSetting.WEIGHTNUMBER + ".txt";
		textNames[2] = "./pai_Sita/10text_" + InitSetting.WEIGHTNUMBER + ".txt";
		textNames[3] = "./pai_Sita/11text_" + InitSetting.WEIGHTNUMBER + ".txt";

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


	public void writeText() {
		int size = textNames.length;
		StringBuffer sb;
		File file;
		BufferedWriter bw = null;
		HashMap<Integer, double[]> map;
		for (int i = 0; i < size; i++) {
			file = new File(textNames[i]);
			try {
				bw = new BufferedWriter(new FileWriter(file));
				map = returnMap(i + 1);
				for (int num : map.keySet()) {
					sb = new StringBuffer();
					sb.append(num);
					for (double d : map.get(num)) {
						sb.append(",");
						sb.append(d);
					}
					bw.write(sb.toString());
					bw.newLine();
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} finally {
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
