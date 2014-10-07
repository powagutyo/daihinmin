package jp.ac.hosei.daihinmin.minegishi;

//import StrategyBot;
import jp.ac.uec.daihinmin.Order;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;

//場が流れた直後か
public class RenewStrategy extends Strategy{
    @Override
	public Meld execute(BotSkeleton bot){
     if(!bot.place().isRenew()){
        return null;
     }
     
    	Cards all = bot.hand();
		Cards cards = bot.hand();
		Melds melds = Melds.parseMelds(all);
		for(Meld meld: melds){
			if(meld.asCards().size()==all.size()){
				return Utils.checkJoker(bot,meld);
			}
			if(all.size()-meld.asCards().size() == 1){
				if(Utils.isStrongestCard((StrategyBot)bot,meld.asCards().get(0))){
					return Utils.checkJoker(bot,meld);
				}
			}
		}

		if(cards.size() > 3) {
			cards.remove(Card.JOKER);
		}
		if(bot.place().order() == Order.NORMAL) {
			melds = melds.extract(Melds.MIN_RANK);
		} else {
			melds = melds.extract(Melds.MAX_RANK);
		}
		melds = melds.extract(Melds.MAX_SIZE);
		return Utils.checkJoker(bot,melds.get(0));

	  
    }
}
