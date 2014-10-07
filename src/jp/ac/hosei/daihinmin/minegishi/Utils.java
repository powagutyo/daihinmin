package jp.ac.hosei.daihinmin.minegishi;


//import StrategyBot;
import jp.ac.uec.daihinmin.Order;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class Utils {
//	static Meld createRenewMeld(BotSkeleton bot) {
//		Cards all = bot.hand();
//		Cards cards = bot.hand();
//		Melds melds = Melds.parseMelds(all);
//		for(Meld meld: melds){
//			if(meld.asCards().size()==all.size()){
//				return checkJoker(bot,meld);
//			}
//		}
//		if(cards.size() > 2) {
//			cards.remove(Card.JOKER);
//		}
//        
//		Melds melds = Melds.parseMelds(cards);
//
//		if(bot.place().order() == Order.NORMAL) {
//			melds = melds.extract(Melds.MIN_RANK);
//		} else {
//			melds = melds.extract(Melds.MAX_RANK);
//		}
//		melds = melds.extract(Melds.MAX_SIZE);
//		return checkJoker(bot,melds.get(0));
//	}
	
	static Meld checkJoker(BotSkeleton bot,Meld meld) {
		if(meld.type() == Meld.Type.SINGLE && meld.asCards().get(0) == Card.JOKER){
			return createJokerMeld(bot);
		} else {
			return meld;
		}
	}

	static Meld createJokerMeld(BotSkeleton bot) {
		//場のスートに合わせた,最大のランクを持つ役に変更して,それを出す．
		Suit suit;
		if(bot.place().suits() == Suits.EMPTY_SUITS) {
			// 取り合えず、スペードにしておく
			suit = Suit.SPADES;
		} else {
			suit = bot.place().suits().get(0);
		}

		Rank rank;
		if(bot.place().order() == Order.NORMAL) {
			rank = Rank.JOKER_HIGHEST;
		} else {
			rank = Rank.JOKER_LOWEST;
		}
		return MeldFactory.createSingleMeldJoker(suit, rank);
	}
	static Melds selectSingleCandidates(BotSkeleton bot) {
		Cards cards = bot.hand();
		Cards strongerCards = Cards.EMPTY_CARDS;
		for(Card card: cards) {
			if(card == Card.JOKER) {
				// JOKER は加えない
				// strongerCards = strongerCards.add(card);
			} else if(bot.place().order() == Order.NORMAL && card.rank().toInt() > 13) {
				strongerCards = strongerCards.add(card);
			} else if(bot.place().order() == Order.REVERSED && card.rank().toInt() < 5) {
				strongerCards = strongerCards.add(card);
			}
		}

		cards = singleCards(bot,cards);
		for(Card card: strongerCards) {
			if(!cards.contains(card)) {
				cards = cards.add(card);
			}
		}

		return Melds.parseMelds(cards);
	}
	static Cards singleCards(BotSkeleton bot,Cards cards) {
		// JOKER は対象外にする
		cards = cards.remove(Card.JOKER);

		// Group 型の対象カードを削除
		Melds groupMelds = Melds.parseGroupMelds(cards);
		for(Meld meld: groupMelds) {
			cards = cards.remove(meld.asCards());
		}

		// Sequence 型の対象カードを削除
		Melds sequenceMelds = Melds.parseSequenceMelds(cards);
		for(Meld meld: sequenceMelds) {
			cards = cards.remove(meld.asCards());
		}

		return cards;
	}
	static Melds adjustSizeType(BotSkeleton bot,Melds melds) {
		Extractor<Meld, Melds> sizeExtractor = Melds.sizeOf(bot.place().size());
		Extractor<Meld, Melds> typeExtractor = Melds.typeOf(bot.place().type());
		return melds.extract(sizeExtractor.and(typeExtractor));
	}
	static Melds adjustRank(BotSkeleton bot,Melds melds) {
		// 革命を考慮しながら、出せるランクのカードに絞り込む
		Rank rank = bot.place().rank(); // 現在の場のランク
		Extractor<Meld, Melds> rankExtractor;
		if(bot.place().order() == Order.NORMAL) {
			// JOKER が出ている時は、勝てないのであきらめる
			if(rank != Rank.JOKER_HIGHEST) {
				Rank oneRankHigh = rank.higher();
				rankExtractor = Melds.rankOf(oneRankHigh).or(Melds.rankOver(oneRankHigh));
				melds = melds.extract(rankExtractor);
				melds = melds.extract(Melds.MIN_RANK);
			} else if(bot.hand().contains(Card.S3)) {
				// スペードの3を出す
				Meld meld = MeldFactory.createSingleMeld(Card.S3);
				melds = Melds.EMPTY_MELDS;
				return melds.add(meld);
			} else {
				melds = Melds.EMPTY_MELDS;
			}
		} else {
			if(rank != Rank.JOKER_LOWEST) {
				Rank oneRankHigh = rank.lower();
				rankExtractor = Melds.rankOf(oneRankHigh).or(Melds.rankUnder(oneRankHigh));
				melds = melds.extract(rankExtractor);
				melds = melds.extract(Melds.MAX_RANK);
			} else if(bot.hand().contains(Card.S3)) {
				// スペードの3を出す
				Meld meld = MeldFactory.createSingleMeld(Card.S3);
				melds = Melds.EMPTY_MELDS;
				return melds.add(meld);
			} else {
				melds = Melds.EMPTY_MELDS;
			}
		}

		return melds;
	}

	public static boolean isStrongestCard(StrategyBot bot,Card card) {
		// 自分のカードより強いカードが出ているか判断する
		if (card == Card.JOKER) {
			return true;
		}

		if (bot.playedCards[0][0] == false) {
			// ジョーカーが未だ出ていなければ、最強ではない
			return false;
		}

		int rankNum = card.rank().toInt();
		boolean order = bot.place().order() == Order.NORMAL;

		for (int i = rankNum + (order? 1: -1); (order? i <= 15: i >= 3); i = i + (order? 1: -1)) {
			int num;

			if (i >= 14) {
				num = i - 13;
			} else {
				num = i;
			}

			// jはスートを調べる
			for (int j = 0; j < 4; j++) {
				if (!bot.playedCards[j][num]) {
					return false;
				}
			}
		}

		return true;
	}

}
