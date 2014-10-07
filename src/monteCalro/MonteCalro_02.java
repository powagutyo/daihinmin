package monteCalro;

import static jp.ac.uec.daihinmin.card.MeldFactory.*;

import java.util.ArrayList;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;

/***
 * 0 スペード 1.ハート 2.ダイヤ 3.クラブ
 * UCTプレイヤー
 *
 * @author 飯田伸也
 *
 */
public class MonteCalro_02 {
	// TODO やること
	/**
	 * もし場に出ているカードが最高のカードランクの場合、探索せずにPASSにする
	 *
	 */

	private final int count = 2000;// 回す数

	private final int players = 5;// プレイヤーの数

	private final int cardNum = 53;// カード大きさ

	private final int threshold = 20;// 閾値

	private int mySeat = 0;

	/**
	 * モンテカルロの実行メソッド
	 *
	 * @param bs
	 *            BotSkelton
	 * @param md
	 *            MyData
	 */
	public static Meld MonteCalroPlay(BotSkeleton bs, MyData md, FieldData fd) {
		return new MonteCalro_02().play(bs, md, fd);
	}

	/**
	 * 原始モンテカルロの実行
	 *
	 * @param bs
	 *            BotSkelton
	 * @param md
	 *            MyData
	 */
	private Meld play(BotSkeleton bs, MyData md, FieldData fd) {

		ArrayList<MeldData> arrayListMelds = searchOfMeld(bs);// 出せる役を探索

		if (arrayListMelds == null)
			return PASS;// ArrayListに何も入っていないならPASSを返す

		/** 変数の初期化始め **/

		mySeat = md.getSeat(); // 自分の座席番号

		final int arraySize = arrayListMelds.size(); // 出せる役の枚数

		GameField gf = new GameField(players, bs, fd, md);

		/** 変数の初期化終了 **/

		int putRandom = 0; // 最初に出す手をランダムに選ぶ

		boolean first = true; // 1回目の時だけ自分の役集合から出すので特殊である。

		for (int i = 0; i < arraySize; i++) {// UCBの値を初期化させる
			arrayListMelds.get(i).setTurnPlayer(mySeat); // 自分の順番にする
			arrayListMelds.get(i).initUCB(gf.getFirstWonPlayer());
		}

		/** MeldDataの出した順番を格納する 最後のサイズはそれ以上木が作られることがないため **/
		ArrayList<Integer> meldDataOrder = new ArrayList<Integer>(count
				/ threshold + 1);

		MeldData placeMeldData = null;

		int[][] copyPlayerHands = initHands(md, fd, gf);

		gf.setPlayerHands(copyPlayerHands(copyPlayerHands));
		//gf.setPlayerHands(copyPlayerHands(mh.getPlayersHands()));

		gf.setPlayerTypeOfHandsofCards(getTypeOfHandsOfCards(gf
				.getPlayerHands()));// 自分と相手のカードランクの枚数を計算する。

		gf.initFirstGF();




		// メインループ
		for (int playout = 1; playout <= count; playout++) {


			gf.firstClone();

			meldDataOrder.clear(); // 1ゲームごとにMeldDataに出したものを記憶するために初期化

			first = true; // 最初の1回目はtrue
			while (true) { // 1プレイ分のループ

				if (!first) {// 最初の1回目ではない時

					if (!placeMeldData.isHaveChildren()) {// 場に出したMeldDataが子ノードを持っていない時
						gf.putToSeeStateOfField(); // 場の状態を確認し、出した手を場に反映させる

					} else {// 子ノードを持っている時
						putRandom = getUCBRandomMeldData(placeMeldData
								.getChildren());

						meldDataOrder.add(putRandom);// 木の通った順番を記憶する

						placeMeldData = new MeldData(placeMeldData
								.getChildren().get(putRandom));// 場のMeldDataのコピー

						gf.renewPlace_MeldData(placeMeldData);

					}
				} else {// 最初の一回目の時
					// ランダムで最初に選ぶ手を決める
					// putRandom = (int) (Math.random() * arraySize);
					// 自分の役集合だけで選ぶ
					// putRandom = getRandomMeldData(arrayListMelds);

					// UCBの値でランダムに木を選ぶ
					putRandom = getUCBRandomMeldData(arrayListMelds);

					meldDataOrder.add(putRandom);// 木の通った順番を記憶する

					placeMeldData = new MeldData(arrayListMelds.get(putRandom));// 場のMeldDataのコピー

					gf.renewPlace_MeldData(placeMeldData);

					first = false;// 最初の一回目じゃなくする
				}

				if (gf.checkGoalPlayer()) { // 上がった人の判定

					break;// 自分上がった時

				}
				// 8切りの判定
				if (gf.checkEight())
					gf.allPlayerDoPass();// すべてのプレイヤーをパスにする

				if (gf.checkRenew()) {// renewするかどうかの判定
					gf.renew();
				}

				gf.updateTurnPlayer(); // ターンプレイヤーの更新

			}

			oneGameUpdate(arrayListMelds, playout, gf, meldDataOrder); // 得点などの状態の処理

			MeldData md1 = arrayListMelds.get(meldDataOrder.get(0));

			if (md1.isSearchChildGroupUpTree(threshold, meldDataOrder, 0,
					arrayListMelds.get(meldDataOrder.get(0)))) { // 木が成長できるかどうかの探索

				growUpTree(arrayListMelds, new GameField(gf.getFirstGf()),
						meldDataOrder, bs);
			}

		}

		// 実際に出す手を返している
		double point = -10000000;
		int resultPos = 0;
		double x = 0;
		for (int i = 0; i < arraySize; i++) {
			x = arrayListMelds.get(i).getPointDivideN();
			if (x > point) {
				point = x;
				resultPos = i;
			}

		}

		return arrayListMelds.get(resultPos).getMeld();

	}

	/**
	 * 一回のゲームが終わるごとに呼び出されるメソッド
	 *
	 * @param arrayListMelds
	 *            すべての手のMeldData
	 * @param cloneData
	 *            コピーするためのMeldData
	 * @param playout
	 *            プレイアウトの回数
	 * @parm cloneGameField 実際の場のGameField
	 *
	 * @parm order MeldDataを出した順番を保存
	 *
	 */
	public void oneGameUpdate(ArrayList<MeldData> arrayListMelds, int playout,
			GameField cloneGF, ArrayList<Integer> order) {

		double logn = 0.0;
		int point = cloneGF.returnWinPoint();
		// 得点の計算等々
		arrayListMelds.get(order.get(0)).updateData(point); // 自身のデータを更新させる

		logn = Math.sqrt(2 * (Math.log(playout) / Math.log(2)));

		for (MeldData md : arrayListMelds) {// すべてのMeldDataを更新する
			md.calcUCB(logn);
		}
		arrayListMelds.get(order.get(0)).updateData(point, order);// 子ノードのデータを更新
	}

	/**
	 * 木を成長させるメソッド
	 *
	 * @param arrayListMelds
	 *            すべてのMeldDataクラス
	 * @param gf
	 *            新しく作られたgameFieldクラス
	 * @param meldDataOrder
	 *            出した順番を記憶した配列
	 */
	public void growUpTree(ArrayList<MeldData> arrayListMelds, GameField gf,
			ArrayList<Integer> meldDataOrder, BotSkeleton bs) {
		boolean win = false; // 自分が勝利したかどうか

		GameField cloneGf = new GameField(gf);

		int size = meldDataOrder.size();
		MeldData md = null;
		for (int i = 0; i < size; i++) {// meldDataのorderの数で回している
			if (i == 0) {// 最初のMeldDataの時
				md = new MeldData(arrayListMelds.get(meldDataOrder.get(0)));
			} else {// 最初以外は子ノードから選択
				md = new MeldData(md.getChildren().get(meldDataOrder.get(i)));
			}
			gf.renewPlace_MeldData(md);

			if (gf.checkGoalPlayer()) { // 上がった人の判定
				win = true;
				break;// 自分上がった時
			}
			if (gf.checkEight())// 8切りの判定
				gf.allPlayerDoPass();// すべてのプレイヤーをパスにする

			if (gf.checkRenew()) {// renewするかどうかの判定
				gf.renew();
			}
			gf.updateTurnPlayer(); // ターンプレイヤーの更新

		}
		if (win) {// 上がりの時は木を成長させない

			arrayListMelds.get(meldDataOrder.get(0)).putWinNode(meldDataOrder,
					1);// これ以上この木を成長させないようにする

		} else {// 木を成長させる

			MeldData parent = null;

			parent = arrayListMelds.get(meldDataOrder.get(0))
					.searchParentMeldData(meldDataOrder, 1);// 親ノード

			ArrayList<MeldData> children = new ArrayList<MeldData>();// 出せる子ノードを格納する場所

			int[][] putCards = gf.getPutHand(); // 出せる手の候補が返ってくる

			for (int[] cards : putCards) {// 全ての手に対して子供を作る

				children.add(new MeldData( cards)); // 子供を生成
			}
			size = children.size();

			for (int i = 0; i < size; i++) { // 子供に初期UCBを計算させる
				if (size == 1) {// PASSのみしかない時
					children.get(i).setPassOnly(true);
				}
				children.get(i).setTurnPlayer(gf.getTurnPlayer());
				children.get(i).initUCB(gf.getWonPlayer());
			}

			parent.setHaveChildren(true);// 親ノードに子供が持っている情報をtrueにしてあげる

			parent.setChildren(children);// 子供をセットする

		}

	}

	/**
	 * MeldDataからjokerにバイアスをかけて抜き出す
	 *
	 * @param array
	 *            出せる役データ群
	 */
	private int getRandomMeldData(ArrayList<MeldData> array) {
		double result = 0.0;
		int size = array.size();
		int pos = 0;
		for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
			result += array.get(i).getEveluation();
		}
		result = result * Math.random(); // ランダムをかけてあげる
		for (int i = 0; i < size; i++) {
			result = result - array.get(i).getEveluation();
			if (result <= 0) {// resultが0以下になった手を返してあげる
				pos = i;
				break;
			}
		}
		return pos;
	}

	/***
	 * UCBの計算データから
	 *
	 * @param array
	 * @return
	 */
	private int getUCBRandomMeldData(ArrayList<MeldData> array) {

		double result = 0.0;
		int size = array.size();
		int pos = 0;
		if (array.get(0).getTurnPlayer() == mySeat) {
			for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
				result += array.get(i).getUCB();
			}
			result = result * Math.random(); // ランダムをかけてあげる
			for (int i = 0; i < size; i++) {
				result = result - array.get(i).getUCB();
				if (result <= 0) {// resultが0以下になった手を返してあげる
					pos = i;
					break;
				}
			}
		} else {
			double x = 50;
			for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
				result += (x - array.get(i).getUCB());
			}
			result = result * Math.random(); // ランダムをかけてあげる
			for (int i = 0; i < size; i++) {
				result = result - (x - array.get(i).getUCB());
				if (result <= 0) {// resultが0以下になった手を返してあげる
					pos = i;
					break;
				}
			}
		}

		return pos;
	}

	/***
	 * 手札の配列をコピーするメソッド
	 *
	 * @param copyHands
	 *            コピーしたい手札配列
	 * @return コピーされた配列
	 */
	private int[] copyPlayerHands(int[][] copyHands) {
		int[] result = new int[players * cardNum];

		for (int i = 0; i < players; i++) {
			for (int j = 0; j < cardNum; j++) {
				result[i * cardNum + j] = copyHands[i][j];
			}
		}

		return result;
	}


	/**
	 * 手札を生成するメソッド
	 *
	 * @return 手札の配列を返す
	 */
	private int[][] initHands(MyData md, FieldData fd, GameField gf) {
		int[][] resultHands = new int[players][cardNum];

		ArrayList<Integer> notLookCards = new ArrayList<Integer>();// まだ見えていないカード群を格納

		int[] feild = md.getField();// 場の残っているカードの配列をコピってあげる

		// 初期化
		for (int i = 0; i < cardNum; i++) {
			for (int j = 0; j < players; j++) {
				resultHands[j][i] = 0;
			}
			if (feild[i] == 1)// まだ見えていないカードだった場合
				notLookCards.add(i);
		}
		int arraySize = 0;
		int seatSize = 0;
		int random = 0;

		// 手札を分ける
		for (int i = 0; i < players; i++) {
			if (i == gf.getMySeat()) {// 自分の座席の時
				resultHands[i] = md.getMyHand();// 自分の手をそのまま入れてあげる
			} else {// それ以外
				seatSize = fd.getSeatsHandSize(i);// 座席のカード枚数を登録
				// カードの枚数分ランダムに入れてあげる
				for (int j = 0; j < seatSize; j++) {
					arraySize = notLookCards.size();// 見えていないカードの合計枚数
					random = (int) (Math.random() * arraySize);// ランダムで抜き出すカードを入れる

					resultHands[i][notLookCards.remove(random)] = 1;
				}
			}
		}
		return resultHands;
	}

	/**
	 * プレイヤーごとに種類別に分けたカードの枚数を返す
	 *
	 * @return　種類別に分けたカードの枚数を返す
	 */
	private int[] getTypeOfHandsOfCards(int[] array) {
		int num = players * 14;
		int[] result = new int[num];
		// 初期化
		int counter = 0;// カードの枚数をカウントする
		// 探索部分
		for (int i = 0; i < players; i++) {
			for (int j = 0; j < 14; j++) {// カードの数字の枚数
				num = i * cardNum + j ;
				if(j != 0){//普通のカードの処理
					for (int l = 0; l < 4; l++) {// カードの種類
						if (array[num + (l * 13 )] == 1)// もしカードが存在する時
							counter++;
					}
				}else{//jokerの時の処理
					if(array[num] ==1){//jokerを持っている時
						counter++;
					}
				}
				result[i *  14 + j] = counter;// 枚数記憶させる
				counter = 0;
			}
		}

		return result;
	}
	/**
	 * 出せる役があるかどうかの探索し、ある場合はその役のArrayListを返す
	 *
	 * @param BotSkelton
	 *            bs
	 * @return ArrayList<MeldData> 出せるMeldData群を返す
	 */
	private static ArrayList<MeldData> searchOfMeld(BotSkeleton bs) {

		Meld placeMeld = bs.place().lastMeld();

		Cards myhand = bs.hand();

		ArrayList<MeldData> arrayMeld = new ArrayList<MeldData>();

		// JOKER単体出しの時にスぺ3を出す
		if (myhand.contains(Card.S3)) {// スペードの3を持っているか否か
			if (placeMeld != null) {// renewじゃない時
				if (placeMeld.type() == Meld.Type.SINGLE) {// 1枚出しの時
					if (placeMeld.rank() == Rank.JOKER_HIGHEST
							|| placeMeld.rank() == Rank.JOKER_LOWEST) {// JOKERの時
						Cards S3 = Cards.EMPTY_CARDS;
						S3 = S3.add(Card.S3);
						Melds meldC3 = Melds.parseSingleMelds(S3);
						arrayMeld.add(new MeldData(meldC3.get(0)));
						arrayMeld.add(new MeldData());
						return arrayMeld;
					}
				}
			}
		}

		Melds resultMelds = Melds.parseMelds(myhand);

		Suits lockSuits = bs.place().lockedSuits();// 縛りの役

		if (myhand.contains(Card.JOKER)) {// 自分の手札にJOKERが存在する時

			// jokerの時の複数枚のカードの束を除去

			Melds jokerSingleMelds = null;// jokerの一枚出しのカード群

			Melds jokerGroupMelds = Melds.parseGroupMelds(myhand);// group出しのjoker群の処理

			Cards jokers = Cards.EMPTY_CARDS;

			jokers = jokers.add(Card.JOKER);

			jokerSingleMelds = Melds.parseSingleMelds(jokers);

			resultMelds = resultMelds.remove(jokerSingleMelds);

			resultMelds = resultMelds.remove(jokerGroupMelds);

			if (!bs.place().isReverse()) { // 革命しているかどうか
				jokerSingleMelds = jokerSingleMelds.extract(Melds.MAX_RANK);
			} else {
				jokerSingleMelds = jokerSingleMelds.extract(Melds.MIN_RANK);
			}

			if (placeMeld != null && placeMeld.type() == Meld.Type.SINGLE) {// 場にカードが出されていてかつ一枚出しの時
				jokerSingleMelds = jokerSingleMelds.extract(Melds
						.suitsOf(placeMeld.suits()));
			}
			if (placeMeld != null && placeMeld.type() == Meld.Type.GROUP
					&& lockSuits != Suits.EMPTY_SUITS) {// 場にカードが出されていてかつ複数枚出しの時かつ縛りの時
				jokerGroupMelds = jokerGroupMelds.extract(Melds
						.suitsOf(lockSuits));
				if (jokerGroupMelds != Melds.EMPTY_MELDS)// 出せる役がある場合
					resultMelds = resultMelds.add(jokerGroupMelds);
			} else {// 縛りとかが一切ない場合
				int counter = 0;
				for (Meld meld : jokerGroupMelds) {
					if (counter % 4 == 0)
						resultMelds = resultMelds.add(meld);
					counter++;
				}
			}
			resultMelds = resultMelds.add(jokerSingleMelds.get(0));// jokerの役を入れてあげる
		}

		if (placeMeld != null) {// 場にカードが置かれている時
			// 役のサイズ比較
			resultMelds = resultMelds.extract(Melds.sizeOf(placeMeld.asCards()
					.size()));
			// 役の形比較
			resultMelds = resultMelds.extract(Melds.typeOf(placeMeld.type()));

			// 役の大小比較
			if (!bs.place().isReverse()) { // 革命しているかどうか
				if (placeMeld.type() == Meld.Type.SEQUENCE) {// 階段の時の処理

					Rank rank = Utility.changeInttoRank(placeMeld.rank()
							.toInt() + placeMeld.asCards().size());
					if (rank != null) {
						resultMelds = resultMelds.extract(Melds.rankOver(rank));
					} else {
						resultMelds = resultMelds.extract(Melds
								.rankUnder(Rank.JOKER_HIGHEST));
					}
				} else {// 階段以外の時の処理
					resultMelds = resultMelds.extract(Melds.rankOver(placeMeld
							.rank()));
				}

			} else {
				if (placeMeld.type() == Meld.Type.SEQUENCE) {// 階段の時の処理
					Rank rank = Utility.changeInttoRank(placeMeld.rank()
							.toInt() - placeMeld.asCards().size());
					if (rank != null) {
						resultMelds = resultMelds
								.extract(Melds.rankUnder(rank));
					} else {
						resultMelds = resultMelds.extract(Melds
								.rankUnder(Rank.JOKER_LOWEST));
					}

				} else { // 階段以外の時の処理
					resultMelds = resultMelds.extract(Melds.rankUnder(placeMeld
							.rank()));
				}
			}
			// 縛りの有無の確認
			if (lockSuits != Suits.EMPTY_SUITS) {
				resultMelds = resultMelds.extract(Melds.suitsOf(lockSuits));
			}

		}
		if (resultMelds.size() == 0) {// 出せる役が存在しない時
			return null;
		}
		for (Meld meld : resultMelds) {// それぞれの役データを格納
			arrayMeld.add(new MeldData(meld));
		}
		/** 場にカードが存在する時はPASSは入れるがrenew時はPASSを入れない **/
		if (placeMeld != null)// 場にカードが存在する時
			arrayMeld.add(new MeldData());// PASSを格納

		return arrayMeld;
	}
}
