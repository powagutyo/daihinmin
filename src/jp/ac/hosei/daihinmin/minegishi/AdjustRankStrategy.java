package jp.ac.hosei.daihinmin.minegishi;

import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;
public class AdjustRankStrategy extends Strategy{
    @Override
    public Meld execute(BotSkeleton bot){
    	Melds melds = Melds.parseMelds(bot.hand());
    	melds = Utils.adjustSizeType(bot, melds);
    	melds = Utils.adjustRank(bot, melds);
    	
    	if(melds.isEmpty()){
    		Cards cards = bot.hand();
    		if(bot.place().size()==1 && cards.size()< 5 && cards.contains(Card.JOKER)){
    			return Utils.createJokerMeld(bot);
    		}else{
    			return null;
    		}
    	}else{
    		return melds.get(0);
    	}

      }
}
