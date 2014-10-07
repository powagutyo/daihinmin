package simulationBransing;

import static jp.ac.uec.daihinmin.card.MeldFactory.*;

import java.util.ArrayList;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import monteCalro.FieldData;
import monteCalro.MakeHand;
import monteCalro.MyData;
import monteCalro.Utility;
import object.InitSetting;
import object.WeightData;

/***
 * 0 スペード 1.ハート 2.ダイヤ 3.クラブ
 *
 *
 * @author 飯田伸也
 *
 */
public class MonteCalro {
	/**
	 * もし場に出ているカードが最高のカードランクの場合、探索せずにPASSにする
	 *
	 */

	private final int count = InitSetting.COUNT;// 回す数

	private final int players = 5;// プレイヤーの数

	private final int cardNum = 53;// カード大きさ

	private int mySeat = 0;

	double learning;

	/**
	 * モンテカルロの実行メソッド
	 *
	 * @param bs
	 *            BotSkelton
	 * @param md
	 *            MyData
	 */
	public static Meld MonteCalroPlay(BotSkeleton bs, MyData md, FieldData fd,
			MakeHand mh, WeightData wd) {
		return new MonteCalro().play(bs, md, fd, mh, wd);
	}

	/**
	 * UCTの実行
	 *
	 * @param bs
	 *            BotSkelton
	 * @param md
	 *            MyData
	 */
	private Meld play(BotSkeleton bs, MyData md, FieldData fd, MakeHand mh, WeightData wd) {

		ArrayList<MeldData> arrayListMelds = searchOfMeld(bs);// 出せる役を探索

		if (arrayListMelds == null)
			return PASS;// ArrayListに何も入っていないならPASSを返す

		/** 変数の初期化始め **/

		mySeat = md.getSeat(); // 自分の座席番号

		final int arraySize = arrayListMelds.size(); // 出せる役の枚数

		GameField gf = new GameField(players, bs, fd, md);

		// GameField cloneGF = null;

		/** 変数の初期化終了 **/

		int putRandom = 0; // 最初に出す手をランダムに選ぶ

		boolean first = true; // 1回目の時だけ自分の役集合から出すので特殊である。

		for (int i = 0; i < arraySize; i++) {// UCBの値を初期化させる
			arrayListMelds.get(i).setTurnPlayer(mySeat); // 自分の順番にする
			arrayListMelds.get(i).initUCB(gf.getFirstWonPlayer());
		}

		/** MeldDataの出した順番を格納する 最後のサイズはそれ以上木が作られることがないため **/
		ArrayList<Integer> meldDataOrder = new ArrayList<Integer>(1024);

		MeldData placeMeldData = null;

		gf.setPlayerHands(copyPlayerHands(mh.getPlayersHands()));

		gf.setPlayerTypeOfHandsofCards(getTypeOfHandsOfCards(gf
				.getPlayerHands()));// 自分と相手のカードランクの枚数を計算する。
		gf.initNotLookCard();// 場に出ているカード以外を格納

		gf.initFirstGF();

		boolean growUpChildren = false;

		GameField childGf = null;

		int size = 0;

		// メインループ
		for (int playout = 1; playout <= count; playout++) {
			if (InitSetting.DEBUGMODE) {
				System.out.println("playout" + playout);
			}
			long start = System.currentTimeMillis();

			gf.firstClone();// gfを最初の状態に戻す

			meldDataOrder.clear(); // 1ゲームごとにMeldDataに出したものを記憶するために初期化

			first = true; // 最初の1回目はtrue

			childGf = null;

			placeMeldData = null;

			size = 0;

			learning = 0.0;

			while (true) { // 1プレイ分のループ

				if (!first) {// 最初の1回目ではない時

					if (!placeMeldData.isHaveChildren()) {// 場に出したMeldDataが子ノードを持っていない時
						// 木を成長させるかの判定
						if (placeMeldData.getN() >= InitSetting.THRESHOLD - 1 && placeMeldData.isSearchChildren() && !growUpChildren) { // 木を成長させる時
							growUpChildren = true;
							childGf = new GameField(gf);
						}

						gf.useSimulationBarancing(InitSetting.learning, wd);

					} else {// 子ノードを持っている時
						putRandom = getUCBRandomMeldData(placeMeldData
								.getChildren(), gf, wd);

						meldDataOrder.add(putRandom);// 木の通った順番を記憶する

						placeMeldData = placeMeldData
								.getChildren().get(putRandom);// 場のMeldDataのコピー
						gf.renewPlace_MeldData(placeMeldData);
					}
				} else {// 最初の一回目の時

					// UCBの値でランダムに木を選ぶ
					putRandom = getUCBRandomMeldData(arrayListMelds, gf, wd);

					meldDataOrder.add(putRandom);// 木の通った順番を記憶する

					placeMeldData = arrayListMelds.get(putRandom);// 場のMeldDataのコピー

					gf.renewPlace_MeldData(placeMeldData);

				}

				if (gf.checkGoalPlayer()) { // 上がった人の判定
					if (InitSetting.DEBUGMODE)
						System.out.println("時間は" + (System.currentTimeMillis() - start) + "ms");

					break;// 自分上がった時

				}
				gf.endTurn();// ターン等々の処理

				first = false;// 最初の一回目じゃなくする

			}

			oneGameUpdate(arrayListMelds, playout, gf, meldDataOrder); // 得点などの状態の処理
			//Weightの学習率の変更
		//	learning = learning * 0.99;
			/** ここから木を成長させる **/
			if (growUpChildren) {

				// ArrayList<MeldData> children = new ArrayList<MeldData>();//
				// 出せる子ノードを格納する場所

				childGf.getPutHand(); // 出せる手の候補が返ってくる

				size = childGf.getPair().size();

				while (true) {

					for (int i = 0; i < size; i++) {
						placeMeldData.addChildren(new MeldData(childGf.getPair().get(i).clone()), childGf.getTurnPlayer(), childGf.getWonPlayer());
					}
					if (size == 1 && childGf.getPair().get(0)[0] == 256) {// PASSしかない時
						childGf.turnPLayerDoPass();

						childGf.checkGoalPlayer();

						childGf.endTurn();// ターン等々の処理

						childGf.getPutHand(); // 出せる手の候補が返ってくる

						size = childGf.getPair().size();

						placeMeldData = placeMeldData.getChildren().get(0); // PASSの子供に変更する
					} else {

						break;
					}
				}
				growUpChildren = false;
			}

		}

		// 実際に出す手を返している
		double point = -1024;
		int resultPos = 0;
		double x = 0;
		for (int i = 0; i < arraySize; i++) {
			x = arrayListMelds.get(i).getPointDivideN();
			if (x > point) {
				point = x;
				resultPos = i;
			}

		}
		if (InitSetting.gameRecord)
			gf.getFirstGf().writeText(point); // 棋譜データ作成
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

		int point = cloneGF.returnWinPoint();
		// 得点の計算等々
		arrayListMelds.get(order.get(0)).updateData(point); // 自身のデータを更新させる

		for (MeldData md : arrayListMelds) {// すべてのMeldDataを更新する
			md.setUCB(Caluculater.calcUCB_TUNED(playout, md));
		}
		arrayListMelds.get(order.get(0)).updateData(point, order);// 子ノードのデータを更新
	}
	/***
	 * UCBの計算データから
	 *
	 * @param array
	 * @return
	 */
	private int getUCBRandomMeldData(ArrayList<MeldData> array, GameField gf, WeightData wd) {
		int size = array.size();
		if (size == 1)
			return 0;

		double result = 0.0;
		int pos = 0;
		double[] points = new double[size];
		int grade = 0;
		boolean reverse = false;
		int authenticationCode = 0;
		double[] weight = new double[size];
		double[] d;
		MeldData md = null;
		boolean doWeight = false;
		
		if (InitSetting.putHandMode == 2 && !gf.isReverse()) {
			grade = gf.getGrade()[gf.getTurnPlayer()];
			reverse = gf.isReverse();
			authenticationCode = gf.getAuthenticationCode_i();
		}

		for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
			md = array.get(i);
			points[i] += md.getUCB();
			if (InitSetting.putHandMode == 2 && !gf.isReverse()) {// 重みを用いる場合の判定
				d = wd.getWeight(grade, reverse, authenticationCode);
				weight[i] = Caluculater.calcPai_sita(d, gf.getWeight(md.getCards()));
				if(weight[i] != 0)
					doWeight =true;
			}
		}
		if (InitSetting.putHandMode == 2 && !gf.isReverse() && doWeight) {// 重みを用いる場合の判定
			Caluculater.ratioAB(points, weight, learning);//UCBの値と相互対応
			for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
				points[i] += (weight[i]);
			}
		}

		for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
			result += points[i];
		}

		result = result * Math.random(); // ランダムをかけてあげる

		for (int i = 0; i < size; i++) {
			result = result - points[i];
			if (result <= 0) {// resultが0以下になった手を返してあげる
				pos = i;
				break;
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
	 * プレイヤーごとに種類別に分けたカードの枚数を返す
	 *
	 * @return　種類別に分けたカードの枚数を返す
	 */
	private int[] getTypeOfHandsOfCards(int[] array) {
		int num = players * 14;
		int[] result = new int[num];

		int counter = 0;// カードの枚数をカウントする
		// 探索部分
		for (int i = 0; i < players; i++) {
			for (int j = 0; j < 14; j++) {// カードの数字の枚数
				num = i * cardNum + j;
				if (j != 0) {// 普通のカードの処理
					for (int l = 0; l < 4; l++) {// カードの種類
						if (array[num + (l * 13)] == 1)// もしカードが存在する時
							counter++;
					}
				} else {// jokerの時の処理
					if (array[num] == 1) {// jokerを持っている時
						counter++;
					}
				}
				result[i * 14 + j] = counter;// 枚数記憶させる
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

		Melds resultMelds = Melds.EMPTY_MELDS;

		Suits lockSuits = bs.place().lockedSuits();// 縛りの役

		if (myhand.contains(Card.JOKER)) {// 自分の手札にJOKERが存在する時

			// jokerの時の複数枚のカードの束を除去

			Melds jokerSingleMelds = null;// jokerの一枚出しのカード群

			Melds jokerGroupMelds = Melds.parseGroupMelds(myhand);// group出しのjoker群の処理

			Cards jokers = Cards.EMPTY_CARDS;

			Cards notjoker = myhand.remove(Card.JOKER);

			resultMelds = Melds.parseSingleMelds(notjoker);

			resultMelds = resultMelds.add(Melds.parseGroupMelds(notjoker));

			resultMelds = resultMelds.add(Melds.parseSequenceMelds(myhand));

			jokers = jokers.add(Card.JOKER);

			jokerSingleMelds = Melds.parseSingleMelds(jokers);

			Melds notJokerGroupMelds = Melds.parseGroupMelds(notjoker);

			if (!bs.place().isReverse()) { // 革命しているかどうか
				jokerSingleMelds = jokerSingleMelds.extract(Melds.MAX_RANK);
			} else {
				jokerSingleMelds = jokerSingleMelds.extract(Melds.MIN_RANK);
			}

			if (placeMeld != null && placeMeld.type() == Meld.Type.SINGLE) {// 場にカードが出されていてかつ一枚出しの時
				jokerSingleMelds = jokerSingleMelds.extract(Melds
						.suitsOf(placeMeld.suits()));
			}

			if (lockSuits != Suits.EMPTY_SUITS) {
				jokerGroupMelds = jokerGroupMelds.extract(Melds
						.suitsOf(lockSuits));
				notJokerGroupMelds = notJokerGroupMelds.extract(Melds.suitsOf(lockSuits));
			}

			if (jokerGroupMelds != Melds.EMPTY_MELDS) {// 出せる役がある場合
				int size = 0;
				Rank rank;
				int meldsSize = 0;
				boolean flag = false;
				for (Meld meld : jokerGroupMelds) {

					if (!meld.asCards().contains(Card.JOKER)) {
						continue;
					}
					meldsSize = notJokerGroupMelds.size();
					rank = meld.rank();
					size = meld.asCards().size();

					for (int i = 0; i < meldsSize; i++) {
						if (rank == notJokerGroupMelds.get(i).rank() && size == notJokerGroupMelds.get(i).asCards().size()) {
							flag = true;
							break;
						}

					}
					if (!flag) {
						notJokerGroupMelds = notJokerGroupMelds.add(meld);
						resultMelds = resultMelds.add(meld);
					}
					flag = false;

				}
			}
			resultMelds = resultMelds.add(jokerSingleMelds.get(0));// jokerの役を入れてあげる

		} else {
			resultMelds = Melds.parseMelds(myhand);
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
