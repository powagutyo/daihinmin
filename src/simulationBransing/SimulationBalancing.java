package simulationBransing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Map;

import object.InitSetting;
import object.WeightData;

/**
 * 棋譜データの保存
 * 自分の手札枚数×2,自分の席番号×1,プレイヤーの人数×1,合計手札×2,すべての手札がどこに入っているか×53,
 * lock×4,placeSUits×4,rank×2,reverse×1,numberOfCard×1,turnPlayer×1,
 * PutLastPLayer×1,passPLayer×5,
 * firstWOnplayer×5,grade×5,勝率×4
 *
 * @author shinya
 */
public class SimulationBalancing {
	/*** 特徴の数 ***/
	private final int weightNumber = InitSetting.WEIGHTNUMBER;

	/*** 重みの特徴ベクトル ***/
	private double[] weight_sita = new double[weightNumber];

	/** 棋譜の読み込みデータの数 ***/
	private final int gameRecordData = 100;
	/** 学習の回数 **/
	private final int learningNumber = 100;
	/** 平均勾配の算出計算回数 **/
	private final int meanGradientNumber = 100;
	/** 学習率 **/
	private final int arufa = 1;
	/** 重みを格納用のメソッド **/
	int[] weight = new int[InitSetting.WEIGHTNUMBER]; //重み

	/**
	 * 学習の準備を行うメソッド
	 *
	 * @param gameField
	 */
	public void preparelearning(GameField gameField) {
		File file;// ファイル
		int num = gameField.getPlayerHandsOfCards()[gameField.getTurnPlayer()]; // ターンプレイヤーの手札の枚数を取得
		String authenticationCode = ""; // 認証用のString
		int grade = gameField.getGrade()[gameField.getMySeat()];
		// 自分の手札
		if (num >= 10) {
			file = new File("./gameRecord/myHand_" + num + "_" + grade + ".txt");
			authenticationCode += num;
		} else {
			file = new File("./gameRecord/myHand_0" + num + "_" + grade + ".txt");
			authenticationCode += "0" + num;
		}
		// myseat (そのプレイヤーだから)
		authenticationCode += gameField.getTurnPlayer();
		// プレイヤーの人数を格納
		authenticationCode += gameField.notWonPLayers();

		BufferedReader bf = null;

		try {
			bf = new BufferedReader(new FileReader(file));

		} catch (FileNotFoundException e) {
			System.out.println("ファイル読み込み失敗");
			e.printStackTrace();

		}
		learningPhase(bf, authenticationCode, gameField); // 学習フェーズに移行する
		try {
			bf.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if (InitSetting.learning)
			writePai_Sita(gameField);// Pai_Sitaの情報をテキストに書き込む

	}

	/**
	 * 学習フェーズ
	 *
	 * @param bf
	 *            BufferedReaderクラス
	 * @param authenticationCode
	 *            認証コード
	 * @throws IOException
	 */
	public void learningPhase(BufferedReader bf, String authenticationCode, GameField nowGF) {
		GameField gf = new GameField();
		gf.setSb(nowGF.getSb());
		int counter = 0;
		// 方策パラメータΘの初期化
		for (int i = 0; i < weightNumber; i++) {
			weight_sita[i] = 0;
		}
		String gameRecord = "";// 棋譜データ中身
		String message = "";
		double miniMax = 0.0; // minMaxの値
		double expectedReward = 0.0;// 期待報酬
		double[] meanGradient = null; // 平均勾配
		int size = authenticationCode.length();
		/*** 棋譜データの読み込みとゲームを行う ***/
		while (true) {

			try {
				gameRecord = bf.readLine();// 棋譜データの読み込み
			} catch (IOException e) {
				System.out.println("");
			}
			if (gameRecord == null || counter >= gameRecordData) {
				break;
			}
			message = gameRecord.substring(0, size);// 認証コード
			/*** 認証コードは最低限の場が一致している時の条件 **/
			if (message.equals(authenticationCode)) {// 認証コードと一致した場合
				// 学習に入る

				miniMax = gf.restoreGameRecord(gameRecord);// 棋譜データから盤面を復元
				gf.initFirstGF(); // 一番最初GameFeildクラスを作成


				expectedReward = calcExpectedReward(gf); // 期待報酬を求め

				meanGradient = calcMeanGradient(gf); // 平均勾配を計算

				update_sita(miniMax, expectedReward, meanGradient);

				counter++;

			}
			gameRecord = "";
		}
	}

	/**
	 * 期待報酬を計算するメソッド
	 * モンテカルロで実装
	 *
	 * @param gf
	 *            GameFeild
	 * @return 期待報酬
	 */
	public double calcExpectedReward(GameField gf) {
		double result = 0.0;
		int counter = 0;
		while (counter <= learningNumber) {
			gf.firstClone();// gfを最初の状態に戻す

			while (true) {
				gf.useSimulationBarancing(false, null); // 場の状態を確認し、出した手を場に反映させる

				if (gf.checkGoalPlayer()) { // 上がった人の判定
					break;// 自分上がった時
				}
				gf.endTurn();// ターンなどの処理
			}
			counter++;
			result += gf.returnWinPoint();
		}
		return result / learningNumber;
	}

	/**
	 * 平均勾配の計算部
	 *
	 * @param gf
	 *            　GameFeild
	 * @return
	 */
	public double[] calcMeanGradient(GameField gf) {
		int counter = 0;
		double[] meanGradient = new double[weightNumber];

		double[] result = new double[weightNumber];

		int visit = 0;

		while (counter <= meanGradientNumber) {
			gf.firstClone();// gfを最初の状態に戻す
			visit = 0;
			while (true) {
				visit += gf.useSimulationBarancing_m(result, 0); // 場の状態を確認し、出した手を場に反映させる

				if (gf.checkGoalPlayer()) { // 上がった人の判定
					break;// 自分上がった時
				}
				gf.endTurn();// ターンなどの処理
			}
			counter++;
			for (int i = 0; i < weightNumber; i++) {
				meanGradient[i] += ((gf.returnWinPoint() * result[i]) / (visit * meanGradientNumber));
				result[i] = 0;
			}
		}
		return meanGradient;

	}

	/**
	 * 方策パラメータΘの更新
	 *
	 * @param minmax
	 *            minMaxの値
	 * @param expectedReward
	 *            期待報酬
	 * @param meanGradient
	 *            平均勾配
	 */
	public void update_sita(double miniMax, double expectedReward, double[] meanGradient) {
		double result = arufa * (miniMax - expectedReward);
		for (int i = 0; i < weightNumber; i++) {
			weight_sita[i] += result * meanGradient[i];
		}
	}

	/**
	 * 方策πΘを用いて手を選択する
	 *
	 * @param putHand
	 *            場に出せるカード群
	 * @param gf
	 *            GameFeild
	 * @return
	 */
	public int putHand(Map<Integer, int[]> map, GameField gf) {
		int size = map.size();
		double[] points = new double[size];
		double result = 0;
		double sum = 0;
		boolean first = true;
		for (int i = 0; i < size; i++) {// 一手一手の特徴を求める
			weight = gf.getWeight(weight, map.get(i), first);
			result = Caluculater.calcPai_sita(this.weight_sita, weight); // 全てに対するπΘを計算
			points[i] = result;
			first = false;
		}
		boolean flag = true;
		for (int i = 0; i < size; i++) {
			if (points[i] != 0) {
				flag = false;
				break;
			}
		}
		if (flag) {
			for (int i = 0; i < size; i++) {
				points[i] = 1;
			}
		} else {
			points = Caluculater.scailingPai_sita(points);
		}

		for (int i = 0; i < size; i++) {
			sum += points[i];
		}

		for (int i = 0; i < size; i++) {
			points[i] = points[i] / sum;
		}

		sum = 0;
		for (int i = 0; i < size; i++) { // 全ての合計を取得
			sum += points[i];
		}
		sum = sum * Math.random(); // ランダムで変数を入れる
		int pos = 0;

		for (int i = 0; i < size; i++) {
			sum -= (points[i]);
			if (sum <= 0) {
				pos = i;// サイズをposに変更
				break;
			}
		}
		return pos;
	}

	/**
	 * 方策πΘを用いて手を選択し、平均勾配を求める
	 *
	 * @param putHand
	 *            場に出せるカード群
	 * @param gf
	 *            GameFeild
	 * @return
	 */
	public int putHand_m(Map<Integer, int[]> map, GameField gf, double[] meanGradient) {
		int size = map.size();
		double[] result = new double[weightNumber];
		double[] points = new double[size];
		double pai_Sita = 0;
		double sum = 0;
		double num = 0;
		int pos = 0;
		boolean flag = true;//全ての手が0の場合の処理
		boolean first = true;//重みの計算が最初の処理かどうか計算

		for (int i = 0; i < size; i++) {// 一手一手の特徴を求める
			weight = gf.getWeight(weight, map.get(i), first);
			pai_Sita = Caluculater.calcPai_sita(this.weight_sita, weight); // 全てに対するπΘを計算
			points[i] = pai_Sita;
			first = false;
			if (pai_Sita != 0) {
				flag = false;
			}
		}

		if (flag) {
			for (int i = 0; i < size; i++) {
				points[i] = 1;
				num += points[i];
			}
		} else {
			points = Caluculater.scailingPai_sita(points);
			for (int i = 0; i < size; i++) {
				num += points[i];
			}
		}

		for (int i = 0; i < size; i++) {// Pai_Sitaの完成
			points[i] = points[i] / num;
			sum += points[i];
		}

		double average = sum * Math.random(); // ランダムで変数を入れる
		for (int i = 0; i < size; i++) {
			average -= points[i];
			if (average <= 0) {
				pos = i;// サイズをposに変更
				break;
			}
		}
		// 平均勾配を求める
		for (int j = 0; j < weightNumber; j++) {// 重みの特徴ベクトル
			for (int i = 0; i < size; i++) {// 手を出せる数
				weight = gf.getWeight(weight, map.get(i), first);

				result[j] -= points[i] * weight[j];
			}
			weight = gf.getWeight(weight, map.get(pos), first);//自分の手の特徴ベクトルを求める

			meanGradient[j] += weight[j] - result[j]; // (s,a)における特徴ベクトル
		}
		return pos;
	}

	/**
	 * 方策πΘを用いて手を選択し、平均勾配を求める
	 *
	 * @param putHand
	 *            場に出せるカード群
	 * @param gf
	 *            GameFeild
	 * @return
	 */
	public int putHand_simulataion(Map<Integer, int[]> map, GameField gf, WeightData wd) {
		int size = map.size();

		double[] points = new double[size];
		double[] sita; //重みの特徴ベクトル
		double pai_Sita = 0;
		double sum = 0;
		boolean first = true; //重みの最初の計算かどうかの判定
		boolean flag = true; //全ての重みが何も入っていない時の処理用変数

		int pos = 0;
		int grade = gf.getGrade()[gf.getTurnPlayer()];
		boolean reverse = gf.isReverse();
		int authenticationCode = gf.getAuthenticationCode_i();

		for (int i = 0; i < size; i++) {// 一手一手の特徴を求める
			weight = gf.getWeight(weight, map.get(i), first);//重みの計算
			sita  =wd.getWeight(grade, reverse, authenticationCode);//特徴ベクトルの計算
			pai_Sita = Caluculater.calcPai_sita(sita, weight); // 全てに対するπΘを計算
			points[i] = pai_Sita;
			first = false;
		}

		for (int i = 0; i < size; i++) { // ここで重みベクトルが存在しない場合はランダムの判定を行う
			if (points[i] != 0|| Double.isNaN(points[i])) {
				flag = false;
				break;
			}
		}
		if (flag) {
			for (int i = 0; i < size; i++) {
				points[i] = 1;
			}
		}

		for (int i = 0; i < size; i++) {
			sum += points[i];
		}
		double average = sum * Math.random(); // ランダムで変数を入れる
		for (int i = 0; i < size; i++) {
			average -= points[i];
			if (average <= 0) {
				pos = i;// サイズをposに変更
				break;
			}
		}
		return pos;
	}

	/**
	 * Θコンソールに表示するプログラム
	 */
	public void displaySita() {
		for (int i = 0; i < weightNumber; i++) {
			System.out.println("重み" + i + ":" + weight_sita[i]);
		}
		System.out.println();
	}

	/**
	 * 学習で求めたπθを保存するメソッド
	 *
	 * @param GameField
	 */
	public void writePai_Sita(GameField gf) {
		String r = "";
		if (gf.isReverse()) {
			r = "r";
		}
		r += gf.getGrade()[gf.getTurnPlayer()];
		File file = new File(InitSetting.text_PaiSita_up + r + InitSetting.text_PaiSita_under);
		String authenticationCode = gf.getAuthenticationCode(); // 認証用のString
		String result = "";
		String message = "";
		String mes = "";
		ArrayList<String> arrayString = new ArrayList<String>();
		boolean onWrite = true;
		BufferedWriter bw = null;
		BufferedReader br = null;
		BufferedWriter pw = null;
		LineNumberReader lnr = null;
		boolean first = true;
		Caluculater.scailingPai_sita(weight_sita);

		try {
			bw = new BufferedWriter(new FileWriter(file, true));
			br = new BufferedReader(new FileReader(file));
			lnr = new LineNumberReader(new BufferedReader(new FileReader(file)));

			while (true) {

				message = br.readLine();
				if (message == null) {
					break;
				}

				mes = message.substring(0, 5);
				if (mes.equals(authenticationCode)) {
					onWrite = false;
					String message_1[] = message.split(",");
					mes += "," + (Integer.parseInt(message_1[1]) + 1);
					for (int i = 0; i < weightNumber; i++) {
						mes += ","+ (weight_sita[i]+  Double.parseDouble(message_1[i +2])) ;
					}
					message = "";
					message = mes;
				}
				arrayString.add(message);
				message = "";
				first = false;

			}
			if (onWrite) {
				onWrite = false;
				authenticationCode += "," + 1;
				for (int i = 0; i < weightNumber; i++) {
					authenticationCode += ","+weight_sita[i];
					if (weight_sita[i] != 0) {
						onWrite = true;
					}
				}
				if (onWrite) {
					if (!first)
						bw.newLine();
					bw.write(authenticationCode);
				}
			} else {
				int size = arrayString.size();
				for (int i = 0; i < size; i++) {
					result += arrayString.get(i);
					if (i != size - 1)
						result += "\r\n";
				}
				pw = new BufferedWriter(new FileWriter(file));

				pw.write(result);

			}

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				br.close();
				if (pw != null)
					pw.close();
				lnr.close();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

}
