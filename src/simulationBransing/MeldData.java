package simulationBransing;

import static jp.ac.uec.daihinmin.card.MeldFactory.*;

import java.util.ArrayList;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import monteCalro.Utility;

/**
 * 出せる役のデータ
 */
public class MeldData implements Cloneable {
	/** 通った回数 **/
	private int n = 0;
	/** 勝った回数 */
	private int win = 0;
	/** カードの枚数 **/
	private int size = 0;
	/** PASSかどうか **/
	private boolean pass = false;
	/** 役の集合 **/
	private Meld meld = null;
	/** 役のポイント **/
	private int point;
	/** jokerを持っているか **/
	private boolean joker = false;
	/** ターンプレイヤー **/
	private int turnPlayer = 0;
	/** 親ノードかどうか **/
	private boolean isParent;
	/** 子ノードを持っているかどうか **/
	private boolean haveChildren = false;
	/** 子ノードを探索するかどうか　探索する時はtrue **/
	private boolean isSearchChildren = true;
	/** 自分のノードがPassのみの場合 **/
	private boolean PassOnly = false;

	/** UCBの値 **/
	private double UCB;

	/** 自分の子ノード **/
	private ArrayList<MeldData> children = null;
	/** 出したカードの集合 **/
	private int[] cards;
	/** 何で勝ったかを記憶する変数 ***/
	private int[] winNum;

	/** 学習率α **/
	private final double alpha = 1.0;

	/** その場にいるプレイしているプレイヤーの人数 **/
	private int playerNum = 0;

	/**
	 * コンストラクタ 親ノードの時
	 *
	 * @param cards
	 */
	public MeldData(Meld meld) {
		this.meld = meld;
		Cards cards = meld.asCards();
		int counter = 0;
		size = cards.size();
		this.cards = new int[size];
		int num = 0;
		for (Card card : cards) {// 役に使ったカードの抽出
			if (card == Card.JOKER) {
				joker = true;
			}
			num = Utility.cardParseInt(card);

			this.cards[counter] = num;
			counter++;
		}
		winNum = new int[5];
		point = 0;
		UCB = 0.1;
		isParent = true;
	}

	/**
	 * 子ノードを作る時のコンストラクタ
	 */
	public MeldData(int[] cards) {
		UCB = 0.1;
		if (cards[0] >= 64) {// PASSの時
			pass = true;
			meld = PASS;
		} else {
			size = cards.length;
			for (int card : cards) {
				if (card == 0) {// jokerの時
					joker = true;
				}

			}
		}
		winNum = new int[5];
		this.cards = cards.clone();
		point = 0;
	}

	/***
	 * MeldDataのクローンメソッド
	 *
	 * @param md
	 */
	public MeldData(MeldData md) {
		this.n = md.getN();
		this.win = md.getWin();
		this.size = md.getSize();
		this.pass = md.isPass();
		this.meld = md.getMeld();
		this.point = md.getPoint();
		this.joker = md.isJoker();
		this.isParent = md.isParent();
		this.haveChildren = md.isHaveChildren();
		this.isSearchChildren = md.isSearchChildren();

		this.UCB = md.getUCB();

		this.children = md.getChildren();

		int[] copyCards = md.getCards();

		System.arraycopy(copyCards, 0, this.cards, 0, copyCards.length);
		System.arraycopy(md.getWinNum(), 0, this.winNum, 0, 5);

	}

	/**
	 * PASS専用のコンストラクタ 親ノードの時
	 *
	 */
	public MeldData() {
		UCB = 0.1;
		point = 0;
		pass = true;
		isParent = true;
		meld = PASS;
		cards = new int[1];
		cards[0] = 256;
		winNum = new int[5];
	}

	public void init(int firstWonPlayer) {
		point = 0;
		haveChildren = false;
		isSearchChildren = true;
		n = 0;
		initUCB(firstWonPlayer);

		children.clear();

	}

	/**
	 * UCBの値を中央値にして初期化 また、プレイしているプレイヤーの数を調べる
	 *
	 * @param firstWonPlayer
	 */
	public void initUCB(int firstWonPlayer) {
		playerNum = 5 - Integer.bitCount(firstWonPlayer);
		// UCBの中央値にする
		for (int i = playerNum; i > 0; i--) {// 残りのプレイヤーの合計ポイントを計算
			UCB += i;
		}
		UCB = (UCB / playerNum) / playerNum;// 合計ポイントの平均を取り、中央値を取る

	}

	/**
	 * データの更新を行うメソッド
	 *
	 * @param point
	 *            得点
	 * @param orderT
	 *            子供を出した順番を格納してる配列
	 */
	public void updateData(int point, ArrayList<Integer> orderT) {

		if (haveChildren) {// 子供を持っている場合
			int size = orderT.size() - 1;
			upDateChild(point, orderT, 1, size, this);// 子ノードすべてのデータを更新

		}

	}

	/**
	 * 得点と訪問回数の更新を行うメソッド(子ノード用)
	 *
	 * @param point
	 *            　得点
	 */
	public void updateData(int point) {
		n++;
		this.point += point;
		winNum[point - 1]++;
	}

	/***
	 * 子ノードの状態(訪問回数と得点)を更新する
	 *
	 * @param point
	 *            得点
	 * @param orderT
	 *            出した順番の配列
	 * @param depth
	 *            自分のいる深さ
	 * @param maxDepth
	 *            見るべき深さ
	 * @parm myMeldData 自分の今いるMeldData
	 */
	public void upDateChild(int point, ArrayList<Integer> orderT, int depth,
			int maxDepth, MeldData myMeldData) {

		if (depth > maxDepth || !myMeldData.isHaveChildren() ) {
			return;
		}

		int num = orderT.get(depth);

		myMeldData.getChildren().get(num).updateData(point); // ここで選ばれた木のデータを更新する

		int visitNum = 0; // 子ノード全体の訪問回数を調べる

		for (MeldData child : myMeldData.getChildren()) {
			visitNum += child.getN();
		}
		for (MeldData child : myMeldData.getChildren()) {// 全ての子ノードのUCBの値を計算する
			child.setUCB(Caluculater.calcUCB_TUNED(visitNum, child));
		}

		MeldData md = myMeldData.getChildren().get(num);

		depth++;// 次の深さにする

		md.upDateChild(point, orderT, depth, maxDepth, md);// 次の選ばれた子ノードを計算する

	}

	/**
	 * それを出した役で勝てる場合、もう子ノードの検索をできないようにするメソッド
	 *
	 * @param order
	 *            出す順番を格納したもの
	 * @param depth
	 *            出す手の深さ
	 */
	public void putWinNode(ArrayList<Integer> order, int depth) {
		if (!haveChildren) {// 子ノードを持っていない時
			isSearchChildren = false;// このノードを探索させないようにする。
			return;
		}
		int num = order.get(depth);
		depth++;
		this.children.get(num).putWinNode(order, depth);

	}

	/**
	 * 親にするノードを決めてあげる
	 *
	 * @param order
	 *            出す手の順番
	 * @param depth
	 *            　出す手の深さ
	 * @return
	 */
	public MeldData searchParentMeldData(ArrayList<Integer> order, int depth) {
		MeldData m = null;
		if (!haveChildren) { // 子ノードを持っていない時自身を返す
			return this;
		}
		int num = order.get(depth);
		depth++;
		m = this.children.get(num).searchParentMeldData(order, depth);// 子ノードの探索
		return m;
	}

	/**
	 * UCBを計算するメソッド
	 *
	 * @param logn
	 *            logの定数項
	 */
	public void calcUCB(double logn) {

		double num = (double) n;
		double result = 0.0;

		if (num == 0) {// niが0の時は0.5とする
			num = 0.5;
			result = UCB;
		} else {
			result = (double) point / ((double) playerNum * (double) num);
		}
		/**
		 * UCBの計算式
		 */
		UCB = result + (alpha * (logn / Math.sqrt((double) num)));

	}

	/**
	 * 子ノードが木を作れるかどうかの探索
	 *
	 * @param threshold
	 *            　閾値
	 * @param order
	 *            　通る順番
	 * @param depth
	 *            　自分のいる木の階層
	 * @param child
	 *            　調べたい子ノード
	 * @return
	 */
	public boolean isSearchChildGroupUpTree(int threshold,
			ArrayList<Integer> order, int depth, MeldData child) {
		boolean result = false;
		if (!child.isHaveChildren()) {// 子ノードを持ってない時
			if ((child.isSearchChildren && child.getN() >= threshold)
					|| (child.isSearchChildren && child.isPassOnly())) {// 閾値以上の時且つその役で上がりの時じゃない時
				result = true;
			}
		} else { // 子ノードを持っている時
			depth++;
			result = isSearchChildGroupUpTree(threshold, order, depth,
					child.children.get(order.get(depth))); // 子ノードを探索してあげる
		}

		return result;
	}

	/**
	 * 子供を加えるメソッド
	 *
	 * @param md
	 */
	public void addChildren(MeldData md, int turnPlayer,int firstWonPLayer) {
		if (children == null) {
			children = new ArrayList<MeldData>(64);
			this.haveChildren = true;
		}
		md.setTurnPlayer(turnPlayer);
		md.initUCB(firstWonPLayer);

		children.add(md);
	}

	public void plusNumberOfTimes() {
		n++;
	}

	public void plusPoint(int num) {
		point += num;
	}

	public double getPointDivideN() {
		return (double) point / (double) n;
	}

	// getter
	public int getN() {
		return n;
	}

	public int getWin() {
		return win;
	}

	public int getPoint() {
		return point;
	}

	public boolean isJoker() {
		return joker;
	}

	public boolean isParent() {
		return isParent;
	}

	public double getAlpha() {
		return alpha;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public int getSize() {
		return size;
	}

	public boolean isPass() {
		return pass;
	}

	public Meld getMeld() {
		return meld;
	}

	public double getEveluation() {
		double result = 1.0;
		if (joker) {
			result = 0.2;
		}
		return result;
	}

	public boolean isHaveChildren() {
		return haveChildren;
	}

	public double getUCB() {
		return UCB;
	}

	public ArrayList<MeldData> getChildren() {
		return children;
	}

	public boolean isSearchChildren() {
		return isSearchChildren;
	}

	public int getTurnPlayer() {
		return turnPlayer;
	}

	public boolean isPassOnly() {
		return PassOnly;
	}

	public int[] getCards() {
		return cards;
	}

	// setter
	public void setHaveChildren(boolean haveChildren) {
		this.haveChildren = haveChildren;
	}

	public void setSearchChildren(boolean isSearchChildren) {
		this.isSearchChildren = isSearchChildren;
	}

	public void setChildren(ArrayList<MeldData> children) {
		this.children = children;
	}

	public void setTurnPlayer(int turnPlayer) {
		this.turnPlayer = turnPlayer;
	}

	public void setPassOnly(boolean passOnly) {
		PassOnly = passOnly;
	}

	public int[] getWinNum() {
		return winNum;
	}

	public void setUCB(double UCB) {
		this.UCB = UCB;
	}
}
