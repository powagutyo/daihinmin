package jp.ac.hosei.daihinmin.fujita2.strategy;

import javax.swing.JOptionPane;

import jp.ac.hosei.daihinmin.fujita2.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class RenewStrategy extends Strategy {
	@Override
	public boolean satisfyCondition(BotSkeleton bot, State state) {
		return bot.place().isRenew() || state.start;
	}

	@Override
	public Meld execute(BotSkeleton bot, State state) {
		Cards all = state.hand;
		boolean order = state.order;
		Melds melds = state.melds;

		Meld meld;
		// yomokiri の中で、all を再考慮するので問題なし
		//if(state.weakestMeld != null) {
		//	meld = state.yomikiri(all, melds.add(state.weakestMeld));
		//} else {
			meld = state.yomikiri(bot.hand(), melds);
		//}

		if(meld != null) {
			return Utils.checkJoker(meld, bot.place());
		}

		Extractor<Meld,Melds> maxSize = Melds.MAX_SIZE;

		/*
		Melds maxMelds = melds.extract(maxSize);

		if(maxMelds.get(0).asCards().size() == all.size()) {
			// 一手で終了して、勝利
			return Utils.checkJoker(maxMelds.get(0), bot.place());
		} else {
			for(Meld maxMeld: maxMelds) {
				Cards remain = all.remove(maxMeld.asCards());
				Melds remainMelds = Melds.parseMelds(remain);
				Melds maxRemainMelds = remainMelds.extract(maxSize);

				// 後2役で終わる場合に、勝てるかどうかを判定
				if(maxRemainMelds.get(0).asCards().size() == remain.size()) {
					// いずれかの最大サイズ役が最強ならば、勝ちを確信
					if(state.isStrongestMeld(maxMeld, order)) {
						return Utils.checkJoker(maxMeld, bot.place());
					}
					for(Meld meld: maxRemainMelds) {
						if(state.isStrongestMeld(meld, order)) {
							return Utils.checkJoker(meld, bot.place());
						}
					}
				}
			}
		}
		*/

    	Extractor<Meld,Melds> minRank = Melds.MIN_RANK;
    	if(!order) {
        	minRank = Melds.MAX_RANK;
    	}

    	// Joker は温存する
    	if(all.contains(Card.JOKER)) {
    		all = all.remove(Card.JOKER);
    		melds = Melds.parseMelds(all);
    	}
    	Melds m1 = melds.extract(minRank).extract(maxSize);
    	
    	if(m1.isEmpty()) {
    		melds = Melds.parseMelds(bot.hand().remove(Card.JOKER));
    		m1 = melds.extract(minRank).extract(maxSize);
    	}

    	// 最後の2人の競い合いの時には最弱カードは出さない
    	if(state.numberOfPlayers() == 2 &&
    			m1.get(0).asCards().size() != all.size()) {
    		Cards cards = m1.get(0).asCards();
    		cards = all.remove(cards);
    		melds = Melds.parseMelds(cards);
    		m1 = melds.extract(minRank).extract(maxSize);
    	}
    	
    	Melds m2 = melds.extract(maxSize).extract(minRank);
    	
    	int n1 = m1.get(0).asCards().size();
    	int n2 = m2.get(0).asCards().size();
    	int r1 = m1.get(0).rank().toInt();
    	int r2 = m2.get(0).rank().toInt();
    	
    	// sequence meld も候補に入れる
    	if(state.seqMelds != null && !state.seqMelds.isEmpty()) {
    		for(Meld m: state.seqMelds) {
    			Cards cards = bot.hand();
    			boolean notFound = false;
    			for(Card card: m.asCards()) {
    				if(!cards.contains(card)) {
    					notFound = true;
    					break;
    				}
    			}
    			
    			if(notFound) {
    				state.seqMelds = state.seqMelds.remove(m);
    				continue;
    			}
    			
    			int sz = m.asCards().size();
    			if(sz > n2) {
    				n2 = sz;
    				m2 = Melds.EMPTY_MELDS.add(m);
    				r2 = m2.get(0).rank().toInt();
    			}
    		}
    	}
    	
    	if(m1.get(0).type() != Meld.Type.SEQUENCE &&
    			m2.get(0).type() == Meld.Type.SEQUENCE) {
    		int diff = m1.get(0).asCards().get(0).rank().toInt() - 
    				m2.get(0).asCards().get(0).rank().toInt();
    		
    		if(order) {
    			if(diff > 0) {
    	    		if(n2 >= 5) {
    	    			Cards cards = m2.get(0).asCards();
    	    			Melds melds1 = Melds.parseSequenceMelds(cards);
    	    			melds1 = melds1.extract(Melds.sizeOf(4)); 
    	    			return melds1.get(0);
    	    		} else {
    	    			return m2.get(0);
    	    		}
    			}
    		} else {
    			if(diff < 0) {
    				diff = -diff;
    			} else {
    	    		if(n2 >= 5) {
    	    			Cards cards = m2.get(0).asCards();
    	    			Melds melds1 = Melds.parseSequenceMelds(cards);
    	    			melds1 = melds1.extract(Melds.sizeOf(4)); 
    	    			return melds1.get(0);
    	    		} else {
    	    			return m2.get(0);
    	    		}
    			}
    		}
    		
    		if(diff < 0) diff = - diff;
    		if(diff > 1) {
    			return m1.get(0);
    		}
    	}
    	
    	if(n1 >= 4) {
    		if(n2 >= 5) {
    			if(m2.get(0).type() == Meld.Type.SEQUENCE) {
    				Cards cards = m2.get(0).asCards();
    				Melds melds1 = Melds.parseSequenceMelds(cards);
    				melds1 = melds1.extract(Melds.sizeOf(4)); 
    				return melds1.get(0);
    			} else {
    				Cards cards = m2.get(0).asCards();
    				Melds melds1 = Melds.parseGroupMelds(cards);
    				melds1 = melds1.extract(Melds.sizeOf(3)); 
    				return melds1.get(0);
    			}
    		} else if(n2 == 4) {
    			if(m2.get(0).type() == Meld.Type.SEQUENCE) {
    				return m2.get(0);
    			} else {
    				Cards cards = m2.get(0).asCards();
    				Melds melds1 = Melds.parseGroupMelds(cards);
    				melds1 = melds1.extract(Melds.sizeOf(3)); 
    				return melds1.get(0);
    			}
    		} else {
    			return m2.get(0);
    		}
    	}
    	if(order) {
    		if(n2 - n1 >= r2 - r1) {
    			// JOKER を含まず、size の差が rank の差より大きい場合は大きいsizeを優先
    			
    			return checkThreeCards(bot, state, m1.get(0), m2.get(0));
    		}
    	} else {
    		if(n2 - n1 >= r1 - r2) {
    			return checkThreeCards(bot, state, m1.get(0), m2.get(0));
    		}
    	}
    	return m1.get(0);
	}
	
	Meld checkThreeCards(BotSkeleton bot, State state, Meld m1, Meld m2) {
		return m2;
		/*
		if(m2.asCards().size() == 3 && m2.type() == Meld.Type.GROUP
				&& !state.placeThreeCards
				&& bot.hand().size() > 8
				&& state.possibilityThreeCards(m2.rank().toInt()) > 4) {
			//JOptionPane.showMessageDialog(null, "3枚組出し惜しみ");
			return m1;
		} else {
			return m2;
		}
		*/
	}
}

