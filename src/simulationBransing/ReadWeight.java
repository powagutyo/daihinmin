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
			"./pai_Sita/1text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/2text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/3text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/4text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/5text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/r1text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/r2text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/r3text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/r4text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/r5text_" + InitSetting.WEIGHTNUMBER + ".txt"
	};
	private final String[] textNames_2 = {
			"./pai_Sita/x1text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x2text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x3text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/x4text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/x5text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/xr1text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/xr2text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/xr3text_" + InitSetting.WEIGHTNUMBER + ".txt", "./pai_Sita/xr4text_" + InitSetting.WEIGHTNUMBER + ".txt",
			"./pai_Sita/xr5text_" + InitSetting.WEIGHTNUMBER + ".txt"
	};

	public ReadWeight(WeightData wd) {// コンストラクタ
		// TODO 自動生成されたコンストラクター・スタブ
		this.wd = wd;
	}

	public void run() {
		if (InitSetting.readMode == 0) {
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
						if(splitWeight[0].equals("05530")){
							System.out.println();
						}
						for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {
							weight[j] = Double.parseDouble(splitWeight[j + 2]) / visit;				
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
		}else if(InitSetting.readMode == 1){
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
