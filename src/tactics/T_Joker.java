package tactics;

import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.MyState;

public class T_Joker extends Tactics {

	@Override
	public boolean doAction(MyState state, BotSkeleton bs) {
		if (state.isJoker() && bs.hand().size() == 1) {
			if (state.isRenew() || bs.place().lastMeld().type() == Meld.Type.SINGLE )
				return true;
		}
		return false;
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public Meld discardMeld(Melds melds, MyState state, BotSkeleton bs) {
		// TODO 自動生成されたメソッド・スタブ
		Melds finalmelds = Melds.parseSingleMelds(bs.hand());
		if(!state.isRenew()){
			if(!state.isReverse()){
				finalmelds = finalmelds.extract(Melds.rankOver(bs.place().rank()));	
			}else{
				finalmelds = finalmelds.extract(Melds.rankUnder(bs.place().rank()));
			}
			finalmelds = finalmelds.extract(Melds.suitsOf(bs.place().suits()));	
		}
		return finalmelds.get(0);
	}

}
