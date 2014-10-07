package jp.ac.hosei.daihinmin.fujita2;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Extractor;
//import jp.ac.uec.daihinmin.card.Meld;
//import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;

public class Test {
	public static void main(String[] args) {
		test2();
		test3();
	}


	static void test2() {
		Cards cards = Cards.EMPTY_CARDS;
		cards = cards.add(Card.C9, Card.CJ, Card.HK);
		Utils.debug(""+ cards);
		Extractor<Card, Cards> extract = Cards.rankUnder(Rank.QUEEN);
		Cards cards3 = cards.extract(extract);
		Utils.debug("Cards.rankUnder(Rank.QUEEN) = " + cards3);
	}

	static void test3() {
		Cards cards = Cards.EMPTY_CARDS;
		cards = cards.add(Card.C9, Card.CJ, Card.HK);
		Extractor<Card, Cards> extract = Cards.rankOver(Rank.QUEEN);
		Cards cards2 = cards.extract(extract);
		Utils.debug("Cards.rankOver(Rank.QUEEN) = " + cards2);
	}
}
