package utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import object.InitSetting;
import object.WeightData;
import simulationBransing.ReadWeight;

public class CalcWeightRange extends Thread {
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

	public static void main(String[] args) {
		new CalcWeightRange().start();
	}

	public void start() {
		WeightData wd = new WeightData();
		ReadWeight re = new ReadWeight(wd);
		re.start();

		while (true) {

			if (wd.isFinish()){
				try {
					re.join();
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				break;
			}
		}

		File f;
		FileWriter fw = null;
		BufferedWriter bf = null;
		String message = "";
		int size = textNames.length;
		double[] result = null;
		boolean first = true;
		double[] weight = new double[InitSetting.WEIGHTNUMBER];
		int authenticationCode = 0;

		int range_m = InitSetting.range_myHands / 2;
		int range_a = InitSetting.range_allHands / 2;
		double visit = 0.0;

		try {
			for (int i = 0; i < size; i++) {
				first = true;
				f = new File(textNames_2[i]);
				fw = new FileWriter(f);
				for (int myHand = 1; myHand <= 11; myHand++) {
					for (int players = 2; players <= 5; players++) {
						for (int allPlayersHands = 2; allPlayersHands <= 53; allPlayersHands++) {
							message = returnMessage(myHand, players, allPlayersHands);// 認証コードの作成
							visit = 0.0;
							authenticationCode = 0;
							for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {
								weight[j] = 0;
							}

							for (int m = myHand - range_m; m < myHand + range_m; m++) {
								if ((m < 1 || m > 12))
									continue;

								for (int a = allPlayersHands - range_a; a < allPlayersHands + range_a ; a++) {
									if((a < 2 || a > 53))
										continue;

									authenticationCode = m * 1000 + players * 100 + a;
									System.out.println(authenticationCode);

									result = wd.getWeight(i +1, false, authenticationCode);

									visit += 1.0;
									for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {
										weight[j] += result[j];
									}
								}

							}
							if (visit != 0) {
								for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {
									weight[j] = weight[j] / visit;
									message += "," + weight[j];
								}
								bf = new BufferedWriter(new FileWriter(f, true));
								if (!first)
									bf.newLine();
								bf.write(message);
								bf.close();
								first = false;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "バグ発生により緊急終了");

		} finally {
			try {
				fw.close();
				bf.close();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		System.out.println("finish");
	}

	/**
	 * 認証コードの作製
	 *
	 * @param myHand
	 * @param players
	 * @param allPlayersHands
	 * @return
	 */
	private String returnMessage(int myHand, int players, int allPlayersHands) {
		String message = "";
		// 認証コードの設定用の変数
		if (myHand < 10) {
			message += 0;
		}
		message += myHand;
		message += players;
		if (allPlayersHands < 10) {
			message += 0;
		}
		message += allPlayersHands;
		return message;
	}

}
