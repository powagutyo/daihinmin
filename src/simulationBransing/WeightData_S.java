package simulationBransing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import object.InitSetting;

public class WeightData_S {

	private ArrayList<HashMap<Integer, double[]>> text;

	private final String[] textNames = {
			"./pai_Sita/002text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/012text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/102text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/112text_" + InitSetting.WEIGHTNUMBER + ".txt"
			, "./pai_Sita/003text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/013text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/103text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/113text_" + InitSetting.WEIGHTNUMBER + ".txt"
			, "./pai_Sita/004text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/014text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/104text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/114text_" + InitSetting.WEIGHTNUMBER + ".txt"
			, "./pai_Sita/005text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/015text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/105text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/115text_" + InitSetting.WEIGHTNUMBER + ".txt"
	};

	/**
	 * コンストラクタ
	 */
	public WeightData_S() {
		int size = textNames.length;
		text = new ArrayList<HashMap<Integer, double[]>>(size);
		for (int i = 0; i < size; i++) {
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
		double[] result = null;
		if (text.get(num).containsKey(authenticationCode)) {
			result = text.get(num).remove(authenticationCode);
			int size = weight.length;
			for (int i = 0; i < size; i++) {
				weight[i] += result[i];
			}
			text.get(num).put(authenticationCode, weight);
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
				map = null;
				map = text.get(i);
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
				e.printStackTrace();
			} finally {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
