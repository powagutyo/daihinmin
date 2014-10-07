package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Extractor;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.MeldFactory;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class UseSpade3Strategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		if(bot.place().isRenew() || state.start) {
			return false;
		} else {
			Rank current = bot.place().rank();
			return current == Rank.JOKER_HIGHEST || current == Rank.JOKER_LOWEST;
		}
	}

	public Meld execute(BotSkeleton bot, State state) {
		if(bot.hand().contains(Card.S3)) {
			return MeldFactory.createSingleMeld(Card.S3);
		} else {
			return MeldFactory.PASS;
		}
	}
}
