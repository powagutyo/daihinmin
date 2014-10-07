package jp.ac.hosei.daihinmin.fujita.strategy;

import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.hosei.daihinmin.fujita.State;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class FittingStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return true;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
    	//next_rank := 場に出されている役のランクの,直ぐ上のランク
    	Rank next_rank;
    	Rank current = bot.place().rank();
    	if(bot.place().type() == Meld.Type.SEQUENCE) {
    		try {
    			next_rank = bot.rules().nextRankSequence(current,bot.place().size(),bot.place().order());
    		} catch (Exception e) {
    			// 次のランクが設定できない時に、ここにくる
    			return PASS;
    		}
    	} else if(current == Rank.JOKER_HIGHEST || current == Rank.JOKER_LOWEST) {
    		if(bot.hand().contains(Card.S3)) {
    			return MeldFactory.createSingleMeld(Card.S3);
    		} else {
    			return PASS;
    		}
    	} else {
    		next_rank = (state.order? bot.place().rank().higher(): bot.place().rank().lower());
    	}

	    //場に出されている役の,タイプ,枚数,ランク,革命中か否か,に合わせて,「出すことができる」候補に絞る．
    	state.melds = state.melds.extract(Melds.typeOf(bot.place().type()).and(
    			Melds.sizeOf(bot.place().size()).and(
    					Melds.rankOf(next_rank).or(
    							state.order? Melds.rankOver(next_rank): Melds.rankUnder(next_rank)))));

        // 候補がない場合は、強制的にPASS
    	if(state.melds.isEmpty()) {
        	return PASS;
        }

    	// 候補が残っている場合は、次の戦略に引き継ぐ
		return null;
	}
}
