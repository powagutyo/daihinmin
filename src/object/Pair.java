package object;

import java.util.ArrayList;

/**
 * 2枚以上のカードの集合のクラス また階段のみにjokerを含むペアを考えている
 */
public class Pair implements Cloneable {
	/*
	 * jokerを持っている時のランクは14とする
	 */

	/** JOKERを含んでいるかどうか **/
	public boolean joker = false;
	/** 使用しているカード **/
	public ArrayList<Integer> cards = new ArrayList<Integer>();

	/** 初期状態はfirst 複数枚だしまたは階段 */
	public enum Status {
		FIRST, Single, PAIR, STAIR, PASS
	}

	Status status = Status.FIRST;
	/** 役の中で一番下のランク */
	public int minCard = 0;
	/** この手札の枚数出しの数 */
	public int cardSize = 0;
	/*
	 * これらは自分のマークを返す 0. スペード. 1 ハート 2. ダイヤ ,3 クラブ
	 */
	public boolean spade = false;

	public boolean heart = false;

	public boolean daiya = false;

	public boolean club = false;

	/**
	 * 1枚出しの時のコンストラクタ
	 *
	 * @param mark
	 *            　マークのint型
	 * @parm rank そのカードのランク
	 * @parm joker jokerを用いているかどうか
	 */
	public Pair(int mark, int rank, boolean joker) {
		this.status = Status.Single;
		this.cardSize = 1;
		this.minCard = rank;
		this.joker = joker;
		if (!joker)
			intChangeMark(mark);
		addCards();

	}

	/**
	 * 複数枚出しの時のコンストラクタ
	 *
	 * @param mark
	 *            　0～3の配列でそれぞれのマークを表しているもの
	 * @param cardSize
	 *            カードの枚数
	 * @param rank
	 *            　出したカードのランク
	 * @parm joker jokerを用いているかどうか
	 */
	public Pair(boolean[] mark, int cardSize, int rank, boolean joker) {
		status = Status.PAIR;
		this.cardSize = cardSize;
		this.minCard = rank;
		this.joker = joker;
		booleanChangeMark(mark);
		addCards();
	}

	/**
	 * 階段の時のコンストラクタ
	 *
	 * @param mark
	 *            　使用したマーク
	 * @param cardSize
	 *            カードの枚数
	 * @param cards
	 *            　使用したカード群
	 * @param joker
	 *            　ジョーカーを使用したかどうか
	 */
	public Pair(int mark, int cardSize, ArrayList<Integer> cards, boolean joker) {
		status = Status.STAIR;
		this.cardSize = cardSize;
		this.joker = joker;
		this.cards = cards;
		if (cards.get(0) == 0) {// Jokerの時
			minCard = (cards.get(1)) % 13;
		} else {
			minCard = (cards.get(0)) % 13;
		}
		intChangeMark(mark);
	}

	/***
	 * パスの時のコンストラクタ
	 */
	public Pair() {
		this.status = Status.PASS;
	}

	/**
	 * int型からマークに変換
	 *
	 * @parm mark int型で表したマーク
	 */
	public void intChangeMark(int mark) {
		if (mark == 0) {
			spade = true;
		} else if (mark == 1) {
			heart = true;
		} else if (mark == 2) {
			daiya = true;
		} else if (mark == 3) {
			spade = true;
		}
	}

	/**
	 * markとmincardからcardにカードを格納する （SingleとPair専用）
	 */
	public void addCards() {
		if (spade) {
			cards.add(minCard);
		}
		if (heart) {
			cards.add(minCard + 13);
		}
		if (daiya) {
			cards.add(minCard + 26);
		}
		if (club) {
			cards.add(minCard + 39);
		}
		if (joker)
			cards.add(0);
	}

	@Override
	/**
	 * cloneメソッド
	 */
	public Pair clone() {
		try {
			return (Pair) super.clone();

		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	/**
	 * boolean[]のマークからbooleanひとつひとつのマークに変換
	 *
	 * @parm mark マークの集合
	 */
	public void booleanChangeMark(boolean[] mark) {
		int size = mark.length;
		for (int i = 0; i < size; i++) {
			if (i == 0) {
				if (mark[i])
					spade = true;
			} else if (i == 1) {
				if (mark[i])
					heart = true;
			} else if (i == 2) {
				if (mark[i])
					daiya = true;
			} else if (i == 3) {
				if (mark[i])
					club = true;
			}
		}
	}

	/**
	 * そのカードが存在するかどうかの判定
	 *
	 * @parm 調べたい数字
	 * @return
	 */
	public boolean check_Card(int num) {
		boolean result = false;
		for (int i = 0; i < cardSize; i++) {
			if (cards.get(i) == num)
				result = true;
			break;
		}
		return result;
	}

	/**
	 * 送られてきたmarkに対してこのペアの集合が同じマークかどうかを判定するメソッド
	 *
	 * @param mark
	 *            boolean[4]のみ可能 マークをbooleanで表したもの
	 * @return trueならマークが完全一致しているもの
	 */
	public boolean checkCardMark(boolean[] mark) {
		try {
			if (mark[0] == spade)
				if (mark[1] == heart)
					if (mark[2] == daiya)
						if (mark[3] == club)
							return true;
			return false;
		} catch (Exception e) {
			System.out.println("エラー発生　Pairクラス　checkCardMarkメソッド");
		}
		return false;

	}
}
