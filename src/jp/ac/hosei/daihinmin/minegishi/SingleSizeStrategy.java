package jp.ac.hosei.daihinmin.minegishi;

import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;

public class SingleSizeStrategy extends Strategy{
    @Override
	public Meld execute(BotSkeleton bot){
    Melds melds;
    if(bot.place().size()==1){
    	 melds = Utils.selectSingleCandidates(bot);
    	 melds = Utils.adjustSizeType(bot, melds);
    	 melds = Utils.adjustRank(bot, melds);
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
