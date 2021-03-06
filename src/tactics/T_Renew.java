package tactics;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.MyState;
import object.Situation;

/**
 * 自分の手で場が流れた時に最初に何を出すかの戦略 今のところ、出せる枚数の多い役かつ最小の役を出すようにしている
 */
public class T_Renew extends Tactics {

	@Override
	public boolean doAction(MyState state, BotSkeleton bs) {
		if (state.isRenew() && !state.isReverse()) {// 場が流れていないかつ革命が起きていないとき
			return true;
		}
		return false;
	}

	@Override
	public Meld discardMeld(Melds melds, MyState state, BotSkeleton bs) {
		// ここから読みきり部が始め
		int size = state.finshMelds.size();
		if (size > 0) {
			return state.finshMelds.remove(0);
		}
		state.finshMelds.clear();

		if (state.oneTurnKill(melds, bs.hand(), state.isReverse())) {

			return state.finshMelds.remove(0);
		}
		// ここから読みきり部が終了
		Meld finalMeld = null;
		Cards cards = bs.hand();

		// ここから革命部が始め
		if (state.isDoReverse()) {// 革命を行う時
			boolean finish = false;
			for (Meld meld : state.getReverseMelds()) {
				cards = bs.hand();
				cards = cards.remove(Card.JOKER);
				cards.remove(meld.asCards());
				melds = Melds.parseMelds(cards);
				melds = melds.extract(Melds.rankOver(state.getStrongestCard()
						.rank()));
				if (melds.size() != 0) {
					finalMeld = melds.get(0);
				} else {// 階段の時
					melds = Melds.parseSequenceMelds(cards);
					Rank rank = Rank.valueOf(state.getStrongestCard().rank()
							.toInt() - 2);
					melds = melds.extract(Melds.rankOver(rank));
					if (melds.size() != 0) {
						finalMeld = melds.get(0);
						finish =true;
						break;
					}
				}
			}
			if(finish)
				return finalMeld;
			cards = bs.hand();
			melds = Melds.parseMelds(cards);
			melds = melds.extract(Melds.sizeOver(4));
			for (Meld meld : melds) {
				if (meld.type() == Meld.Type.GROUP) {// ペア出しの時
					if (meld.asCards().size() >= 4) {
						finalMeld = meld;
						break;
					}
				} else {// 階段の時
					if (meld.asCards().size() >= 5) {
						finalMeld = meld;
						break;
					}
				}
			}
		}
		// ここから革命部が終了
		if (finalMeld == null) {
			cards = cards.remove(Card.JOKER);// JOKERのカード抜き出す

			cards = cards.remove(Card.C2);
			cards = cards.remove(Card.D2);
			cards = cards.remove(Card.H2);
			cards = cards.remove(Card.S2);

			melds = Melds.parseMelds(cards);

			melds = melds.extract(Melds.MIN_RANK); // 一番小さい役を取り出す

			melds = melds.extract(Melds.MAX_SIZE);// 一番出す枚数の多い役を取り出す

			if (melds.size() != 0) {
				finalMeld = melds.get(0);
			}

		}
		return finalMeld;
	}
}
