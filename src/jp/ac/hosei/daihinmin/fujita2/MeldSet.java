package jp.ac.hosei.daihinmin.fujita2;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;

public class MeldSet implements Comparable<MeldSet> {
	public Melds sequence = Melds.EMPTY_MELDS;
	public Card[][] cards = new Card[4][13];
	
	public int size() {
		int size = sequence.size();
		for(int i = 0; i < 13; i++) {
			boolean found = false;
			for(int j = 0; j < 4; j++) {
				if(cards[j][i] != null) {
					found = true;
					break;
				}
			}
			if(found) {
				size++;
			}
		}
		return size;
	}
	
	public int compareTo(MeldSet other) {
		return size() - other.size();	
	}
	
	/**
	 * MeldSet から、Meld を削除する
	 * 結果として　MeldSetが不要になった場合、true を返却
	 * @param meld
	 * @return
	 */
	public boolean remove(Meld meld) {
		if(meld.type() == Meld.Type.SEQUENCE) {
			for(Meld seq: sequence) {
				// ひとつでも、sequence に含まれていたら、この MeldSetは不要
				if(containMeld(meld, seq)) {
					return true;
				}
			}
		}
		
		for(Card card: meld.asCards()) {
			if(card == Card.JOKER) {
				continue;
			}
			int rank = card.rank().toInt() - 3;
			int suit = card.suit().ordinal();
			
			cards[suit][rank] = null;
		}
		
		return false;
	}
	
	/**
	 * 第一の Meld の要素が、第二の Meld の要素に含まれているかどうかを確認
	 * @param m0 
	 * @param m1
	 * @return
	 */
	public boolean containMeld(Meld m0, Meld m1) {
		Cards c0 = m0.asCards();
		Cards c1 = m1.asCards();

		for(Card card: c0) {
			if(c1.contains(card)) {
				return true;
			}
		}
		
		return false;
	}

	public boolean equalMeld(Meld m0, Meld m1) {
		Cards c0 = m0.asCards();
		Cards c1 = m1.asCards();
		if(c0.size() != c1.size()) {
			return false;
		} else {
			for(Card card: c0) {
				if(!c1.contains(card)) {
					return false;
				}
			}
			return true;
		}
	}
}
