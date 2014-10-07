package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.player.BotSkeleton;

/**
 * 戦略を表現するクラス
 * abstract クラスでもよいかもしれないので、今後要検討
 * @author fujita
 *
 */
public class Strategy {

	/**
	 * 戦略を実行するための条件判定
	 * @param bot 対象のボット
	 * @return 条件を満たしたかいなか
	 */
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return false;
	}

	/**
	 * 戦略の実行
	 * @param bot 対象のボット
	 */
	public Meld execute(BotSkeleton bot, State state) {
		return null;
	}

}
