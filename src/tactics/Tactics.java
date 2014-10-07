package tactics;

import object.MyState;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;


/**
 * 自分の戦術を行う親クラス
 */
public abstract class Tactics {
	/**
	 * このプログラムを実行するかどうかを判定するメソッド
	 * trueだと　discardMeldメソッドを呼ばせるようにする。
	 * @param state botSkeltonクラス内のStateクラス
	 * @param bs BotSkeltonクラス
	 * @return このプログラムを実行するかどうか tureの時実行する
	 */
	public abstract boolean doAction(MyState state, BotSkeleton bs);
	/**
	 * クラスに求められる手で最善手を返す
	 * 
	 * @param melds 自分の手札
 	 * @param state botSkeltonクラス内のStateクラス
	 * @param bs BotSkeltonクラス
	 * @return　場に出す役　nullの時は出す役が無い時
	 */
	public abstract Meld discardMeld(Melds melds, MyState state, BotSkeleton bs);
}
