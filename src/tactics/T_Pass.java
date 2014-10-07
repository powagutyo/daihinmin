package tactics;

import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.MyState;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

public class T_Pass extends Tactics {

	@Override
	public boolean doAction(MyState state, BotSkeleton bs) {
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

	@Override
	public Meld discardMeld(Melds melds, MyState state, BotSkeleton bs) {
		// TODO 自動生成されたメソッド・スタブ
		return PASS;
	}

}
