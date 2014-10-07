package tactics;

import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suit;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.MyState;

/**
 * スペ3を必ず出す戦略
 * 
 * @author admin
 * 
 */
public class T_Sp3 extends Tactics {

	@Override
	public boolean doAction(MyState state, BotSkeleton bs) {
		boolean result = false;
		if (bs.place().size() == 1) {
			if (bs.place().rank() == Rank.JOKER_HIGHEST
					|| bs.place().rank() == Rank.JOKER_LOWEST) {
				result = true;
			}
		}
		return result;

	}

	@Override
	public Meld discardMeld(Melds melds, MyState state, BotSkeleton bs) {
		if (state.isHave_Spade_3()) {// スペ3を持っている時
			Melds myMelds = Melds.parseMelds(bs.hand());
			myMelds = myMelds.extract(Melds.rankOf(Rank.THREE));
			myMelds = myMelds.extract(Melds.typeOf(Meld.Type.SINGLE));
			for (Meld meld : myMelds) {
				if (meld.suits().get(0) == Suit.SPADES)
					return meld;
			}
		}
		return null;
	}

}
