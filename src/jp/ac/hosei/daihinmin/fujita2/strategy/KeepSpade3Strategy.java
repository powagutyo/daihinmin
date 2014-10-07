package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Extractor;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class KeepSpade3Strategy extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		int rank; 
		try {
			rank = bot.rank();
		} catch(Exception e) {
			rank = 0;
		}
		return rank > 2 && !state.playedCards[0][0] && state.hand.contains(Card.S3) && !state.hand.contains(Card.JOKER) && state.hand.size() > 3;
	}

	public Meld execute(BotSkeleton bot, State state) {
		state.hand = state.hand.remove(Card.S3);
		return null;
	}
}
