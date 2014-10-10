package object;

public class InitSetting {
	/*** 重みの数 ***/
	public static final int WEIGHTNUMBER = 235;
	/*** デバッグモードかどうか **/
	public static final boolean DEBUGMODE = false;
	/** UCTを回す回数 **/
	public static final int COUNT = 2000;// 回す数
	/** 木の閾値 ***/
	public static final int THRESHOLD = 20;// 閾値
	/*** 読み切りプログラムを使うかどうか */
	public static final boolean ONYOMIKIRI = true;

	public static final long YOMIKIRITIMRLIMIT = 100;

	/** 学習させるかどうか **/
	public static final boolean LEARNING = false;

	public static final boolean GAMERECORD = false;

	public static final boolean DOREADWEIGHT = true;

	/*** 重みを読み込む時に使用する。0が訪問回数を割るやつ、1がそのまま重みを読み込むやつ ***/
	public static final int READMODE = 1;
	/*** 手の出し方を決める変数　0がランダム　1がシミュレーションバランシングの学習で用いる　2がデータのθを用いる ***/
	public static int putHandMode = 0;
	/** 手に対する重みの学習率 ***/
	public static final double LEARNING_W = 0.9;

	/*** 重みの取る範囲 ****/
	public static final int RANGE＿MYHANDS = 2;
	public static final int RANGE＿ALLHANDS = 10;

	/** θのテキスト原型 ****/
	public static final String TEXT＿PAISITA＿UP = "./pai_Sita/";

	public static final String TEXT＿PAISITA＿UNDER = "text_" + WEIGHTNUMBER + ".txt";
	/** UCB の学習率α **/
	public static final double ARUFA = 1.0;

	public static final boolean MODE_C = false;

}
