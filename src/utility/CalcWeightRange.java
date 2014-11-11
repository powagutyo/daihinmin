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

	public static void main(String[] args) {
		new CalcWeightRange().start();
	}

	public void start() {
		WeightData wd = new WeightData();
		ReadWeight re = new ReadWeight(wd);
		re.start();

		while (true) {
			if (wd.isFinish()) {
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

		int range_m = InitSetting.RANGE＿MYHANDS / 2;
		int range_a = InitSetting.RANGE＿ALLHANDS / 2;
		double visit = 0.0;

		try {
			for (int i = 0; i < size; i++) {
				first = true;
				f = new File(textNames_2[i]);
				fw = new FileWriter(f);
				for (int myHand = 1; myHand <= 11; myHand++) {
					for (int allPlayersHands = 2; allPlayersHands <= 53; allPlayersHands++) {
						for (int se = 0; se <= 17; se++) {

							message = "";
							authenticationCode = myHand * 10000 + allPlayersHands * 100 + se;
							System.out.println(authenticationCode);
							message += "" + authenticationCode;
							visit = 0.0;
							authenticationCode = 0;
							for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {
								weight[j] = 0;
							}

							for (int m = myHand - range_m; m < myHand + range_m; m++) {
								if ((m < 1 || m > 12))
									continue;

								for (int a = allPlayersHands - range_a; a < allPlayersHands + range_a; a++) {
									if ((a < 2 || a > 53))
										continue;
									authenticationCode = m * 10000 + a * 100 + se;

									result = wd.getWeight(i, authenticationCode);
									if (result != null) {
										visit += 1.0;
										for (int j = 0; j < InitSetting.WEIGHTNUMBER; j++) {
											weight[j] += result[j];
										}
									}

								}
							}
							if (visit >= 5) {
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
				if (bf != null)
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
