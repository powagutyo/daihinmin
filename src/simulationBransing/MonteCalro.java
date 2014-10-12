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
import object.ObjectPool;
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

	private static final int COUNT = InitSetting.COUNT;// 回す数

	private static final int PLAYERS = 5;// プレイヤーの数

	private int mySeat = 0;// 自分の座席番号

	private double learning = 0.0;

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
	private Meld play(BotSkeleton bs, MyData md, FieldData fd, MakeHand mh,
			WeightData wd) {

		ArrayList<MeldData> arrayListMelds = searchOfMeld(bs);// 出せる役を探索

		if (arrayListMelds == null)
			return PASS;// ArrayListに何も入っていないならPASSを返す

		/** 変数の初期化始め **/

		mySeat = md.getSeat(); // 自分の座席番号

		final int arraySize = arrayListMelds.size(); // 出せる役の枚数

		GameField parentGF = new GameField(PLAYERS, bs, fd, md);

		GameField playGF = null;

		int putRandom = 0; // 最初に出す手をランダムに選ぶ

		boolean first = true; // 1回目の時だけ自分の役集合から出すので特殊である。

		boolean growUpChildren = false;

		GameField childGf = null;

		int size = 0;

		long start;

		/** MeldDataの出した順番を格納する 最後のサイズはそれ以上木が作られることがないため **/
		ArrayList<Integer> meldDataOrder = new ArrayList<Integer>(256);

		MeldData placeMeldData = null;

		/** 変数の初期化終了 **/

		int firstWonPlayer = parentGF.getFirstWonPlayer();

		for (int i = 0; i < arraySize; i++) {// UCBの値を初期化させる
			arrayListMelds.get(i).setTurnPlayer(mySeat); // 自分の順番にする
			arrayListMelds.get(i).initUCB(firstWonPlayer);
		}

		parentGF.initPLaceGameFiled(mh.getPlayersHands());// gfの場のカード情報とfirstGFの初期化

		// メインループ
		for (int playout = 1; playout <= COUNT; playout++) {

			if (InitSetting.DEBUGMODE) {
				System.out.println("playout" + playout);
			}
			start = System.currentTimeMillis();
			if (!first) {
				playGF.release();
			}
			playGF = parentGF.clone();// gfを最初の状態に戻す

			meldDataOrder.clear(); // 1ゲームごとにMeldDataに出したものを記憶するために初期化

			first = true; // 最初の1回目はtrue

			childGf = null;

			placeMeldData = null;

			size = 0;

			learning = InitSetting.LEARNING_W;

			while (true) { // 1プレイ分のループ

				if (!first) {// 最初の1回目ではない時

					if (!placeMeldData.isHaveChildren()) {// 場に出したMeldDataが子ノードを持っていない時
						// 木を成長させるかの判定
						if (placeMeldData.getN() >= InitSetting.THRESHOLD - 1
								&& placeMeldData.isSearchChildren()
								&& !growUpChildren) { // 木を成長させる時
							growUpChildren = true;
							childGf = playGF.clone();
						}

						playGF.useSimulationBarancing(InitSetting.LEARNING, wd);

					} else {// 子ノードを持っている時
						putRandom = getUCBRandomMeldData(
								placeMeldData.getChildren(), playGF, wd);

						meldDataOrder.add(putRandom);// 木の通った順番を記憶する

						placeMeldData = placeMeldData.getChildren().get(
								putRandom);// 場のMeldDataのコピー
						playGF.renewPlace_MeldData(placeMeldData);
					}
				} else {// 最初の一回目の時

					// UCBの値でランダムに木を選ぶ
					putRandom = getUCBRandomMeldData(arrayListMelds, playGF, wd);

					meldDataOrder.add(putRandom);// 木の通った順番を記憶する

					placeMeldData = arrayListMelds.get(putRandom);// 場のMeldDataのコピー

					playGF.renewPlace_MeldData(placeMeldData);

					first = false;
				}

				if (playGF.checkGoalPlayer()) { // 上がった人の判定
					if (InitSetting.DEBUGMODE)
						System.out.println("時間は"
								+ (System.currentTimeMillis() - start) + "ms");

					break;// 自分上がった時

				}
				playGF.endTurn();// ターン等々の処理

			}
			oneGameUpdate(arrayListMelds, playout, playGF, meldDataOrder); // 得点などの状態の処理
			// Weightの学習率の変更
			learning = learning * 0.99;
			/** 木の成長部分 **/

			if (growUpChildren) {
				ArrayList<Long> list = childGf.getPutHand(); // 出せる手の候補を探索
				size = list.size();
				int[] cards;
				int counter = 0;
				int cardSize = 0;
				long num = 0;
				while (true) {
					if (size == 1 && list.get(0) == 0) {// PASSの処理
						cards = ObjectPool.getArrayInt(1);
						cards[0] = 256;
						placeMeldData
								.addChildren(new MeldData(cards.clone()),
										childGf.getTurnPlayer(),
										childGf.getWonPlayer());

						childGf.turnPLayerDoPass();

						childGf.checkGoalPlayer();

						childGf.endTurn();// ターン等々の処理

						list.clear();

						list = childGf.getPutHand(); // 出せる手の候補が返ってくる

						size = list.size();

						placeMeldData = placeMeldData.getChildren().get(0); // PASSの子供に変更する

						ObjectPool.releaseArrayInt(cards);

					} else {

						for (int i = 0; i < size; i++) {
							counter = 0;
							num = list.get(i);
							cardSize = Long.bitCount(num);
							if (cardSize == 0) {
								cards = ObjectPool.getArrayInt(1);
								cards[0] = 256;
							} else {
								cards = ObjectPool.getArrayInt(cardSize);
								for (int j = 0; j < 53; j++) {
									if(cardSize == counter)
										break;

									if ((num & (long) 1 << j) != 0) {
										cards[counter] = j;
										counter++;
									}
								}
							}
							placeMeldData
									.addChildren(new MeldData(cards.clone()), childGf.getTurnPlayer(), childGf.getWonPlayer());
							ObjectPool.releaseArrayInt(cards);
						}
						break;

					}
				}
				growUpChildren = false;
			}

		}

		// 実際に出す手を返す
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
		if (InitSetting.GAMERECORD)
			parentGF.writeText(point); // 棋譜データ作成
		if (childGf != null) {
			childGf.release();
		}
		playGF.release();
		parentGF.release();

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
	private int getUCBRandomMeldData(ArrayList<MeldData> array, GameField gf,
			WeightData wd) {
		int size = array.size();
		if (size == 1)
			return 0;

		double result = 0.0;
		int pos = 0;
		double[] points = new double[size];
		MeldData md = null;
		boolean reverse = gf.isReverse(); // 革命か否か

		if (InitSetting.putHandMode == 2) {// 重みを使う時
			boolean doWeight = false; // 重みの計算を行うかどうかの判定用
			boolean first = true; // 重みの計算が最初かどうかの計算

			int grade = 0;
			int authenticationCode = 0;

			double[] pai_sita = new double[size];
			int[] weight = new int[InitSetting.WEIGHTNUMBER];
			double[] sita;// 重みの特徴
			grade = gf.getMyGrade();// 自分のランク
			authenticationCode = gf.getAuthenticationCode_i();// 認証コード

			for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
				md = array.get(i);
				points[i] += md.getUCB();
				sita = wd.getWeight(grade, reverse, authenticationCode);// sitaの読み込み
				weight = gf.getWeight(weight, md.getCards(), first); // 重みの計算
				pai_sita[i] = Caluculater.calcPai_sita(sita, weight);// pai_Sitaの計算
				first = false;
				if (!doWeight && pai_sita[i] != 0)
					doWeight = true;
			}

			if (doWeight) {// 重みが存在する時
				Caluculater.ratioAB(points, pai_sita, learning);// UCBの値と相互対応
				for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
					points[i] += (pai_sita[i]);
				}
			}
			for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
				result += points[i];
			}

		} else {
			for (int i = 0; i < size; i++) {// すべての評価値を足し合わせる
				md = array.get(i);
				points[i] += md.getUCB();
				result += points[i];
			}
		}

		result = result * Math.random();

		for (int i = 0; i < size; i++) {
			result = result - points[i];
			if (result <= 0) {// resultが0以下の場所を次の手
				pos = i;
				break;
			}
		}

		return pos;
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
		if (myhand.contains(Card.S3) && placeMeld != null && placeMeld.type() == Meld.Type.SINGLE && (placeMeld.rank() == Rank.JOKER_HIGHEST
				|| placeMeld.rank() == Rank.JOKER_LOWEST)) {// スペードの3を持っているか否か
			Cards S3 = Cards.EMPTY_CARDS;
			S3 = S3.add(Card.S3);
			Melds meldC3 = Melds.parseSingleMelds(S3);
			arrayMeld.add(new MeldData(meldC3.get(0)));
			arrayMeld.add(new MeldData());
			return arrayMeld;
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
				notJokerGroupMelds = notJokerGroupMelds.extract(Melds
						.suitsOf(lockSuits));
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
						if (rank == notJokerGroupMelds.get(i).rank()
								&& size == notJokerGroupMelds.get(i).asCards()
										.size()) {
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
