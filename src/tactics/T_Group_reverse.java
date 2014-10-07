package tactics;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.MyState;
import object.Situation;

/**
 * 相手が2枚出し以上を出してきた時の戦略(革命が起きているとき)
 * 
 */
public class T_Group_reverse extends Tactics {

	@Override
	public boolean doAction(MyState state, BotSkeleton bs) {
		if (!state.isRenew()) {// 相手ターンから始まったとき
			if (bs.place().lastMeld().type() == Meld.Type.GROUP) {// 場に出されたカードが2枚以上のペアだった時
				if (state.isReverse())// 革命が起きている時
					return true;
			}
		}
		return false;
	}

	@Override
	public Meld discardMeld(Melds melds, MyState state, BotSkeleton bs) {
		Meld finalMeld = null;

		Meld lastMeld = bs.place().lastMeld();

		Cards cards = bs.hand();

		// ここから革命部が始め
		if (state.isDoReverse()) {// 革命を行う時
			boolean finish = false;
			for (Meld meld : state.getReverseMelds()) {
				cards = bs.hand();
				cards = cards.remove(meld.asCards());
				melds = Melds.parseGroupMelds(cards);
				melds = melds.extract(Melds.rankUnder(state.getStrongestCard()
						.rank()));
				if (melds.size() != 0) {
					finalMeld = melds.get(0);
					finish = true;
					break;
				}
			}
			if (finish)
				return finalMeld;
		}
		cards = bs.hand();

		cards = cards.remove(Card.JOKER);// JOKERのカード抜き出す
		if (state.getSituation() == Situation.Opening
				|| state.getSituation() == Situation.Middle) {
			cards = cards.remove(Card.C3);
			cards = cards.remove(Card.D3);
			cards = cards.remove(Card.H3);
			cards = cards.remove(Card.S3);
			cards = cards.remove(Card.C8);
			cards = cards.remove(Card.D8);
			cards = cards.remove(Card.H8);
			cards = cards.remove(Card.S8);
		}
		Melds sequence = Melds.parseSequenceMelds(cards);

		int size = sequence.size();
		// 階段になり得る数の除去
		if (!state.isLapPair()) {
			for (int i = 0; i < size; i++) {
				for (Card card : sequence.get(i).asCards()) {
					cards = cards.remove(card);
				}
			}
		}
		Melds myMelds = Melds.parseGroupMelds(cards);

		myMelds = myMelds.extract(Melds.rankUnder(lastMeld.rank()));// 出せるカードより上のカード抽出

		int cardSize = bs.place().size();// カードの大きさ
		Cards removeCards;
		/* 出したいカードの大きさと比較して、もし違っていたらcardsの集合から削除する */
		for (Meld meld : myMelds) {
			removeCards = meld.asCards();
			if (removeCards.size() != cardSize
					&& removeCards.size() >= cardSize) {// 求められいるカードの大きさじゃない時
				for (Card card : removeCards) {
					cards = cards.remove(card);
				}
			}
		}

		myMelds = Melds.parseGroupMelds(cards);// 新しくペアとなる役を抽出

		myMelds = myMelds.extract(Melds.rankUnder(lastMeld.rank()));// 指定したランクに変更

		myMelds = myMelds.extract(Melds.sizeOf(cardSize));// 指定したサイズに変換する

		if (myMelds.size() != 0) {
			Melds copyMyMelds = myMelds;

			copyMyMelds = copyMyMelds
					.extract(Melds.suitsOf(bs.place().suits()));

			if (copyMyMelds.size() != 0) {
				finalMeld = copyMyMelds.get(myMelds.size() - 1);
			} else {
				finalMeld = myMelds.get(myMelds.size() - 1);
			}
		}
		return finalMeld;
	}
}
