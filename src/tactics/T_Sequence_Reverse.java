package tactics;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.MyState;
import object.Situation;

public class T_Sequence_Reverse extends Tactics {

	@Override
	public boolean doAction(MyState state, BotSkeleton bs) {
		if (!state.isRenew()) {// 相手ターンから始まったとき
			if (bs.place().lastMeld().type() == Meld.Type.SEQUENCE) {// 場に出されたカードが2枚以上のペアだった時
				if (state.isReverse())// 革命が起きている時
					return true;
			}
		}
		return false;
	}

	@Override
	public Meld discardMeld(Melds melds, MyState state, BotSkeleton bs) {
		Meld finalMeld = null;
		Cards cards = bs.hand();

		// ここから革命部が始め
		if (state.isDoReverse()) {// 革命を行う時
			boolean finish =false;
			for (Meld meld : state.getReverseMelds()) {
				cards = bs.hand();
				cards = cards.remove(meld.asCards());
				melds = Melds.parseSequenceMelds(cards);
				Rank rank = Rank.valueOf(state.getStrongestCard().rank()
						.toInt() + 2);
				melds = melds.extract(Melds.rankUnder(rank));
				if (melds.size() != 0) {
					finalMeld = melds.get(0);
					finish =true;
					break;
				}
			}
			if(finish)
				return finalMeld;
		}
		cards = bs.hand();

		if (state.getSituation() == Situation.Opening
				|| state.getSituation() == Situation.Middle) {
			cards = cards.remove(Card.JOKER);// JOKERのカード抜き出す
			cards = cards.remove(Card.C2);
			cards = cards.remove(Card.D2);
			cards = cards.remove(Card.H2);
			cards = cards.remove(Card.S2);
		}

		Meld lastMeld = bs.place().lastMeld();

		melds = melds.extract(Melds.typeOf(Meld.Type.SEQUENCE));// 階段の抽出

		melds = melds.extract(Melds.sizeOf(bs.place().size()));// 指定したサイズに変換する

		Rank lastCardRank = lastMeld.rank();// 最後に出されたカードのランク

		melds = melds.extract(Melds.rankUnder(lastCardRank));// 下のカードを取り出す

		if (melds.size() != 0) {
			finalMeld = melds.get(melds.size() - 1);
		}
		return finalMeld;
	}

}
