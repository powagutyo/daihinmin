package object;

public class InitSetting {
	/*** 重みの数 ***/
	public static int WEIGHTNUMBER = 235;
	/*** デバッグモードかどうか **/
	public static boolean DEBUGMODE = false;
	/** UCTを回す回数 **/
	public static int COUNT = 2000;// 回す数
	/** 木の閾値 ***/
	public static int THRESHOLD = 20;// 閾値
	/*** 読み切りプログラムを使うかどうか */
	public static boolean onYomikiri = true;

	public static long yomikiriTimeLimit = 500;

	/** 学習させるかどうか **/
	public static boolean learning = false;

	public static boolean gameRecord = false;

	public static boolean doReadWeight = true;

	/*** 重みを読み込む時に使用する。0が訪問回数を割るやつ、1がそのまま重みを読み込むやつ ***/
	public static int readMode = 1;
	/*** 手の出し方を決める変数　0がランダム　1がシミュレーションバランシングの学習で用いる　2がデータのθを用いる ***/
	public static int putHandMode = 0;
	/** 手に対する重みの学習率 ***/
	public static double learning_w = 0.9;

	/*** 重みの取る範囲 ****/
	public static int range_myHands = 2;
	public static int range_allHands = 10;

	/** θのテキスト原型 ****/
	public static String text_PaiSita_up = "./pai_Sita/";

	public static String text_PaiSita_under = "text_" + WEIGHTNUMBER + ".txt";
	/** UCB の学習率α **/
	public static double arufa = 1.0;

	public static boolean modeC = false;

}
