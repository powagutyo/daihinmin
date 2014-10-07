package utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;

public class MeldSort implements Comparator<Meld> {
	/**
	 * meldsをサイズが大きい順にソートする
	 *
	 * @param myMelds
	 * @return
	 */
	public static Melds meldsSizeSort(Melds myMelds) {
		if (myMelds.size() <= 1) {// ソートできない場合
			return myMelds;
		}
		ArrayList<Meld> resultMelds = new ArrayList<Meld>();
		for (Meld meld : myMelds) {
			resultMelds.add(meld);
		}
		Collections.sort(resultMelds, new MeldSort());
		myMelds = Melds.EMPTY_MELDS;
		int size = resultMelds.size();
		for (int i = 0; i < size; i++) {
			myMelds = myMelds.add(resultMelds.get(i));
		}
		return myMelds;
	}

	public int compare(Meld m1, Meld m2) {
		return m2.asCards().size() - m1.asCards().size();
	}
}
