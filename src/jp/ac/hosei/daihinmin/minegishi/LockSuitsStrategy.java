package jp.ac.hosei.daihinmin.minegishi;

import jp.ac.uec.daihinmin.card.*;

import jp.ac.uec.daihinmin.player.BotSkeleton;
public class LockSuitsStrategy extends Strategy{
	public Meld execute(BotSkeleton bot){
		Melds melds = Melds.parseMelds(bot.hand());
		if(!bot.place().lockedSuits().equals(Suits.EMPTY_SUITS)){
			
			melds = melds.extract(Melds.suitsOf(bot.place().lockedSuits()));
			if(melds.size()!=0){
			return Utils.checkJoker(bot, melds.get(0));
			}else{
				return null;
			}
		}else{
		return null;
		}
	  }
}
