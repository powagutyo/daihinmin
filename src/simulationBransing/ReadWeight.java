package simulationBransing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import object.InitSetting;
import object.WeightData;

/** 重さを読み込むメソッド **/
public class ReadWeight extends Thread {

	public WeightData wd;
	/** テキストの名前 */
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
	private final String[] textNames_2 = {
			"./pai_Sita/x002text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x012text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/x102text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x112text_" + InitSetting.WEIGHTNUMBER + ".txt"
			, "./pai_Sita/x003text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x013text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/x103text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x113text_" + InitSetting.WEIGHTNUMBER + ".txt"
			, "./pai_Sita/x004text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x014text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/x104text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x114text_" + InitSetting.WEIGHTNUMBER + ".txt"
			, "./pai_Sita/x005text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x015text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/x105text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x115text_" + InitSetting.WEIGHTNUMBER + ".txt"
	};

	public ReadWeight(WeightData wd) {// コンストラクタ
		// TODO 自動生成されたコンストラクター・スタブ
		this.wd = wd;
	}

	public void run() {
		if (InitSetting.READMODE == 0) {
			int size = textNames.length;
			File file;
			BufferedReader bf;
			String[] splitWeight;
			String message = "";
			double visit;
			double[] weight = new double[InitSetting.WEIGHTNUMBER];

			for (int i = 0; i < size; i++) {
				try {
					file = new File(textNames[i]);
					bf = new BufferedReader(new FileReader(file));
					while (true) {
						message = bf.readLine();
						if (message == null) {
							break;
						}
						splitWeight = message.split(",");
						visit = Double.parseDouble(splitWeight[1]);
						for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {
							weight[j] = Double.parseDouble(splitWeight[j + 2]) / visit;
						}
						// Caluculater.scailingPai_sita(weight,
						// InitSetting.WEIGHTNUMBER);
						wd.setWeight(i, Integer.parseInt(splitWeight[0])
								, weight.clone());
					}
					bf.close();
				} catch (IOException e) {
					System.out.println();
				}
			}
			wd.setFinish(true);// 全ての読み込みが完了
		} else if (InitSetting.READMODE == 1) {
			int size = textNames.length;
			File file;
			BufferedReader bf;
			String[] splitWeight;
			String message = "";

			double[] weight = new double[InitSetting.WEIGHTNUMBER];

			for (int i = 0; i < size; i++) {
				try {
					file = new File(textNames_2[i]);
					bf = new BufferedReader(new FileReader(file));
					while (true) {
						message = bf.readLine();
						if (message == null) {
							break;
						}
						splitWeight = message.split(",");

						for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {

							weight[j] = Double.parseDouble(splitWeight[j + 1]);

						}
						wd.setWeight(i, Integer.parseInt(splitWeight[0])
								, weight.clone());
					}
					bf.close();
				} catch (IOException e) {
					System.out.println();
				}
			}
			wd.setFinish(true);// 全ての読み込みが完了
		}
	}

}
