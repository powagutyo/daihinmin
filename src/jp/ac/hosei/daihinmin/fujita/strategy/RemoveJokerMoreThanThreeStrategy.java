package jp.ac.hosei.daihinmin.fujita.strategy;

import jp.ac.hosei.daihinmin.fujita.State;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class RemoveJokerMoreThanThreeStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return bot.hand().size() > 3;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
        Melds noJokerMelds = Melds.EMPTY_MELDS;
        for(Meld meld: state.melds) {
        	if(!meld.asCards().contains(Card.JOKER)) {
        		noJokerMelds = noJokerMelds.add(meld);
        	}
        }

        state.melds = noJokerMelds;

        return null;
	}
}
