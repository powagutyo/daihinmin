package jp.ac.hosei.daihinmin.fujita2.strategy;

import jp.ac.hosei.daihinmin.fujita2.State;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.MeldFactory;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suit;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

public class JokerLock extends Strategy {

	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		state.hand = bot.hand();
		return state.hand.contains(Card.JOKER) && bot.place().size() == 1 && bot.place().lockedSuits() != Suits.EMPTY_SUITS;
	}

	public Meld execute(BotSkeleton bot, State state) {
		Suits suits = bot.place().lockedSuits();
		Suit suit = suits.get(0);
		Rank rank;
		if(state.order) {
			rank = Rank.JOKER_HIGHEST;
		} else {
			rank = Rank.JOKER_LOWEST;
		}
		Meld meld = MeldFactory.createSingleMeldJoker(suit, rank);
		return meld;
	}
}
