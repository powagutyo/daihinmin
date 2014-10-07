package tactics;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.MyState;
import object.Situation;

/**
 * 相手の手に対する一枚出しの戦略
 */
public class T_Single extends Tactics {

	@Override
	public boolean doAction(MyState state, BotSkeleton bs) {

		if (!state.isRenew()) {// 相手ターンから始まったとき
			if (bs.place().lastMeld().type() == Meld.Type.SINGLE) {// 場に出されたカードが一枚のみだった時
				if (!state.isReverse())// 革命が起きていない時
					return true;
			}
		}
		return false;
	}

	@Override
	public Meld discardMeld(Melds melds, MyState state, BotSkeleton bs) {
		boolean bind = false; // 縛りが存在する時

		Cards cards = bs.hand();
		
		if(cards.size() ==2 && state.isJoker()){//自分の手札が2枚でかつjokerを含む時
			Melds finalMelds = Melds.parseSingleMelds(cards);
			finalMelds = finalMelds.extract(Melds.rankOf(Rank.JOKER_HIGHEST));
			return finalMelds.get(0);
		}

		cards = cards.remove(Card.JOKER);

		Meld finalMeld = null;

		Meld lastMeld = bs.place().lastMeld();

		melds = melds.extract(Melds.typeOf(Meld.Type.SINGLE));// 単体出しの抽出

		melds = melds.extract(Melds.rankUnder(Rank.EIGHT));

		int week = melds.size();// 弱いカードの数を記録する

		// ここから革命部が始め
		if (state.isDoReverse()) {// 革命を行う時
			boolean finish = false;
			for (Meld meld : state.getReverseMelds()) {
				cards = bs.hand();
				cards = cards.remove(Card.JOKER);
				cards = cards.remove(meld.asCards());
				// 強いカードの判定
				melds = Melds.parseSingleMelds(cards);
				if (bs.place().lockedSuits() != Suits.EMPTY_SUITS) {// 縛りの色に対応
					melds = melds.extract(Melds.suitsOf(bs.place()
							.lockedSuits()));
				}
				melds = melds.extract(Melds.rankOver(state.getStrongestCard()
						.rank()));
				if (melds.size() != 0) {
					finalMeld = melds.get(0);
					finish = true;
					break;
				}
				// 8のカード判定
				melds = Melds.parseSingleMelds(cards);

				if (lastMeld.rank().toInt() <= 7 && state.isExistEight()) {
					melds = melds.extract(Melds.rankOf(Rank.EIGHT));
					if (bs.place().lockedSuits() != Suits.EMPTY_SUITS) {// 縛りの色に対応
						melds = melds.extract(Melds.suitsOf(bs.place()
								.lockedSuits()));
					}
					if (melds.size() != 0) {
						finalMeld = melds.get(0);
						finish = true;
						break;
					}
				}
			}
			if (finish)
				return finalMeld;
		}
		// ここから革命部が終了
		cards = bs.hand();
		cards = cards.remove(Card.JOKER);

		melds = state.getSingleMelds();// 単体出ししかできないカード群
		
		if (state.getSituation() == Situation.Opening
				|| state.getSituation() == Situation.Middle) {// 序盤と中盤は強いカードを温存する
			Card strongCard = Card.C3;
			Rank rank = Rank.THREE;
			for (Card card : cards) {
				if (card == Card.JOKER)
					continue;
				if (rank.toInt() < card.rank().toInt()) {
					rank = card.rank();
					strongCard = card;
				}
			}
			melds = melds.extract(Melds.rankUnder(strongCard.rank()));// 一番強いのカードを温存する
		}
	
		if (state.getTwoAndOver() < 2) {
			/*自分が平民以下の時に縛りを意識させる*/
			if (state.getMyRank() <= 0) {
				Melds bindMelds = melds;
				bindMelds = bindMelds.extract(Melds.suitsOf(bs.place().suits()));
				if(bindMelds.size() != 0){
					melds = bindMelds;
				}
			}
		} else {
			cards = cards.remove(Card.JOKER);// JOKERのカード抜き出す

			Melds sequence = Melds.parseSequenceMelds(cards);
			
			int size = sequence.size();
			
			// 階段になり得る数の除去
			for (int i = 0; i < size; i++) {
				for (Card card : sequence.get(i).asCards()) {
					cards = cards.remove(card);
				}
			}
			
			Melds myMelds = Melds.parseSingleMelds(cards);

			myMelds = myMelds.extract(Melds.rankOf(Rank.TWO));// 2の数のみ抜粋

			melds = melds.add(myMelds);
		}
		
		if (bs.place().lockedSuits() != Suits.EMPTY_SUITS) {// 縛りの色に対応

			bind = true;// 縛りが存在する

			melds = melds.extract(Melds.suitsOf(bs.place().lockedSuits()));// 縛りのカード抜き出す

		}
		
		/* 自分の手札に8のカードが存在しかつ単体出しで8のカードより弱いカードが2つ以上存在し、かつ場のカードが8のカード以下の数字の時 */
		if (state.isExistEight() && lastMeld.rank().toInt() < 8) {
			if (state.getWeekSingleMelds().size() >= 2 || week >= 4) {
				/* 行っていることは階段に使われていない8のカード抽出して、8切れるなら8切る */
				cards = cards.remove(Card.JOKER);// JOKERのカード抜き出す

				Melds sequence = Melds.parseSequenceMelds(cards);

				int size = sequence.size();
				// 階段になり得る数の除去
				for (int i = 0; i < size; i++) {
					for (Card card : sequence.get(i).asCards()) {
						cards = cards.remove(card);
					}
				}
				Melds myMelds = Melds.parseSingleMelds(cards);

				if (bind) {// 縛りが存在する時
					myMelds = myMelds.extract(Melds.suitsOf(bs.place()
							.lockedSuits()));// 縛りのマーク 抽出
				}

				myMelds = myMelds.extract(Melds.rankOf(Rank.EIGHT));// 8のカードを抽出
				if (myMelds.size() != 0) {// もし、8が存在する時
					return myMelds.get(0);
				}
			}
		}

		melds = melds.extract(Melds.rankOver(lastMeld.rank()));

		if (melds.size() != 0) {
			if (melds.get(0).rank() == Rank.EIGHT
					&& state.getSituation() != Situation.Last) {
				if (melds.size() != 1)
					finalMeld = melds.get(1);
			} else {
				finalMeld = melds.get(0);
			}
		}
		return finalMeld;
	}
}
