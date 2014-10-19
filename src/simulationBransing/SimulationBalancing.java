package simulationBransing;

import java.io.IOException;
import java.util.ArrayList;

import object.GameRecordData;
import object.InitSetting;
import object.ObjectPool;
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
	private static final int GAMERECORDDATA = 50;
	/** 学習の回数 **/
	private static final int LEARNINGNUMBER = 100;
	/** 平均勾配の算出計算回数 **/
	private static final int MEANGRADIENTNUMBER = 100;
	/** 学習率 **/
	private static final double ARUFA = 1.0;

	private static int visitCounter = 0;
	/** 重みを格納用のメソッド **/
	private int[] weight = new int[InitSetting.WEIGHTNUMBER]; // 重み
	/***重みのでーた群**/
	private WeightData_S wd = new WeightData_S();
	/**set用の変数***/
	private double[] wd_weight = new double[InitSetting.WEIGHTNUMBER +1];
	/**
	 * 学習フェーズ
	 *
	 * @param bf
	 *            BufferedReaderクラス
	 * @param authenticationCode
	 *            認証コード
	 * @throws IOException
	 */
	public void learningPhase(GameField nowGF, GameRecordData grd) {
		GameField gf = nowGF.clone();
		int counter = 0;
		// 方策パラメータΘの初期化
		for (int i = 0; i < weightNumber; i++) {
			weight_sita[i] = 0;
		}
		char[] c;
		int myGrade = nowGF.getMyGrade();
		int code = nowGF.getAuthenticationCode_i();
		int myHands = nowGF.turnPLayerHaveHand(nowGF.getTurnPlayer());
		int allPlyerHands = nowGF.allPLayersHands();
		int wonPLayer = nowGF.notWonPLayers();
		int size = grd.size(myGrade);
		double miniMax = 0.0; // minMaxの値
		double expectedReward = 0.0;// 期待報酬
		double[] meanGradient = null; // 平均勾配
		visitCounter++;
		/*** 棋譜データの読み込みとゲームを行う ***/
		for (int i = 0; i < size; i++) {
			if (counter >= GAMERECORDDATA) {
				break;
			}
			c = grd.getGameRecord(i, myGrade, myHands,wonPLayer,allPlyerHands);
			if (c != null) {
				miniMax = gf.restoreGameRecord(c);

				expectedReward = calcExpectedReward(gf.clone(), gf); // 期待報酬を求め

				meanGradient = calcMeanGradient(gf.clone(), gf); // 平均勾配を計算

				update_sita(miniMax, expectedReward, meanGradient);
				//System.out.println(counter);
				ObjectPool.releaseArrayDouble(meanGradient);
				counter++;
			}
		}
		if (InitSetting.LEARNING ) {
			wd_weight[0] = 1.0;
			for(int i = 1 ;i<=InitSetting.WEIGHTNUMBER;i++){
				wd_weight[i] = weight_sita[i -1];
			}
			int pos = 31 << (5 * nowGF.getTurnPlayer());
			int grade  = nowGF.getGrade() & pos;
			grade = grade >> nowGF.getTurnPlayer() * 5;
			for(int i= 0;i<5;i++){
				if((grade & (1<<i) ) != 0){
					pos = i;
					break;
				}
			}
			if(nowGF.isReverse()){
				pos += 5;
			}
			System.out.println("vist" + visitCounter);
			wd.setWeight(pos, code, wd_weight);
			if(visitCounter % 10 ==0){
				wd.writeText();
			}
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
	public double calcExpectedReward(GameField gf, GameField nowGF) {
		double result = 0.0;
		int counter = 0;
		while (counter <= LEARNINGNUMBER) {
			gf = nowGF.clone();// gfを最初の状態に戻す
			if (gf.checkGoalPlayer()) { // 上がった人の判定
				break;// 自分上がった時
			}
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
		return result / LEARNINGNUMBER;
	}

	/**
	 * 平均勾配の計算部
	 *
	 * @param gf
	 *            　GameFeild
	 * @return
	 */
	public double[] calcMeanGradient(GameField gf, GameField nowGF) {
		int counter = 0;
		double[] meanGradient = ObjectPool.getArrayDouble();

		double[] result = ObjectPool.getArrayDouble();
		for(int i= 0;i<weightNumber;i++){
			meanGradient[i] = 0;
			result[i]= 0;
		}

		double visit = 1;

		while (counter <= MEANGRADIENTNUMBER) {
			gf = nowGF.clone();
			if (gf.checkGoalPlayer()) { // 上がった人の判定
				break;// 自分上がった時
			}
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
				meanGradient[i] += ((gf.returnWinPoint() * result[i]) / (visit * MEANGRADIENTNUMBER));
				result[i] = 0;
			}
		}
		ObjectPool.releaseArrayDouble(result);
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
		double result = ARUFA * (miniMax - expectedReward);
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
	public int putHand(ArrayList<Long> list, GameField gf) {
		int size = list.size();
		double[] points =ObjectPool.getArrayDouble();
		double result = 0;
		double sum = 0;
		boolean first = true;
		boolean flag = true;

		for (int i = 0; i < size; i++) {// 一手一手の特徴を求める
			weight = gf.getWeight(weight, list.get(i), first);
			result = Caluculater.calcPai_sita(this.weight_sita, weight); // 全てに対するπΘを計算
			points[i] = result;
			if(result != 0 && flag)
				flag =false;
			first = false;
		}

		if (flag) {
			for (int i = 0; i < size; i++) {
				points[i] = 1;
			}
		} else {
			points = Caluculater.scailingPai_sita(points, size);
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
		ObjectPool.releaseArrayDouble(points);
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
	public int putHand_m(ArrayList<Long> map, GameField gf, double[] meanGradient) {
		int size = map.size();
		double[] result =ObjectPool.getArrayDouble();
		for(int i = 0;i<weightNumber;i++){
			result[i] = 0;
		}
		double[] points = ObjectPool.getArrayDouble();
		double pai_Sita = 0;
		double sum = 0;
		double num = 0;
		int pos = 0;
		boolean flag = true;// 全ての手が0の場合の処理
		boolean first = true;// 重みの計算が最初の処理かどうか計算

		for (int i = 0; i < size; i++) {// 一手一手の特徴を求める
			weight = gf.getWeight(weight, map.get(i), first);
			pai_Sita = Caluculater.calcPai_sita(this.weight_sita, weight); // 全てに対するπΘを計算
			points[i] = pai_Sita;
			first = false;
			if (pai_Sita != 0 && flag) {
				flag = false;
			}
		}

		if (flag) {
			for (int i = 0; i < size; i++) {
				points[i] = 1;
				num += points[i];
			}
		} else {
			points = Caluculater.scailingPai_sita(points,size);
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
			weight = gf.getWeight(weight, map.get(pos), first);// 自分の手の特徴ベクトルを求める

			meanGradient[j] += weight[j] - result[j]; // (s,a)における特徴ベクトル
		}

		ObjectPool.releaseArrayDouble(result);
		ObjectPool.releaseArrayDouble(points);

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
	public int putHand_simulataion(ArrayList<Long> map, GameField gf, WeightData wd) {
		int size = map.size();

		double[] points = ObjectPool.getArrayDouble();
		double[] sita = ObjectPool.getArrayDouble(); // 重みの特徴ベクトル
		double pai_Sita = 0;
		double sum = 0;
		boolean first = true; // 重みの最初の計算かどうかの判定
		boolean flag = true; // 全ての重みが何も入っていない時の処理用変数

		int pos = 0;
		int grade = gf.getMyGrade();
		boolean reverse = gf.isReverse();
		int authenticationCode = gf.getAuthenticationCode_i();

		for (int i = 0; i < size; i++) {// 一手一手の特徴を求める
			weight = gf.getWeight(weight, map.get(i), first);// 重みの計算
			sita = wd.getWeight(grade, reverse, authenticationCode);// 特徴ベクトルの計算
			pai_Sita = Caluculater.calcPai_sita(sita, weight); // 全てに対するπΘを計算
			points[i] = pai_Sita;
			if(pai_Sita != 0 && flag){
				flag =false;
			}
			first = false;
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
		ObjectPool.releaseArrayDouble(points);
		ObjectPool.releaseArrayDouble(sita);
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


}
