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
			"./pai_Sita/00text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/01text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/10text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/11text_" + InitSetting.WEIGHTNUMBER + ".txt"
	};
	private final String[] textNames_2 = {
			"./pai_Sita/x00text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x01text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/x10text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x11text_" + InitSetting.WEIGHTNUMBER + ".txt"
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
						//Caluculater.scailingPai_sita(weight, InitSetting.WEIGHTNUMBER);
						wd.setWeight(i, Integer.parseInt(splitWeight[0])
								, weight.clone());
					}
					bf.close();
				} catch (IOException e) {
					System.out.println();
				}
			}
			wd.setFinish(true);// 全ての読み込みが完了
		}else if(InitSetting.READMODE == 1){
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
							weight[j] = Double.parseDouble(splitWeight[j + 1]) ;
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
