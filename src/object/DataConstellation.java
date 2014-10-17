package object;

import simulationBransing.ReadWeight;

public class DataConstellation {
	/** 重みのデータ群 **/
	private WeightData wd;
	/*** 重みを読み込むメソッド **/
	private ReadWeight rw;
	/*** 棋譜データを格納するクラス ***/
	private GameRecordData grd;

	public DataConstellation(){
		wd = new WeightData();
		if (InitSetting.LEARNING) {
			grd = new GameRecordData();
		}
		if (InitSetting.DOREADWEIGHT) { // 重みの読み込み
			rw = new ReadWeight(wd);
			rw.start();
		}
	}

	public WeightData getWd() {
		return wd;
	}

	public ReadWeight getRw() {
		return rw;
	}

	public GameRecordData getGrd() {
		return grd;
	}

}
