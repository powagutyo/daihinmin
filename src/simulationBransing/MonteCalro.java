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
import object.DataConstellation;
import object.InitSetting;
import object.ObjectPool;

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
			MakeHand mh, GameFieldTree gft, DataConstellation dc) {
		return new MonteCalro().play(bs, md, fd, mh, gft, dc);
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
			GameFieldTree gft, DataConstellation dc) {

		ArrayList<MeldData> arrayListMelds = searchOfMeld(bs);// 出せる役を探索

		if (arrayListMelds == null)
			return PASS;// ArrayListに何も入っていないならPASSを返す

		/** 変数の初期化始め **/

		mySeat = md.getSeat(); // 自分の座席番号

		final int arraySize = arrayListMelds.size(); // 出せる役の枚数

		GameField parentGF = ObjectPool.getGameField();
		GameField playGF = ObjectPool.getGameField();

		parentGF.firstInit(PLAYERS, bs, fd, md);
		if(InitSetting.randomInitHandMode){
			parentGF.initPLaceGameFiled(initHands(md, fd));// gfの場のカード情報とfirstGFの初期化
		}else{
			parentGF.initPLaceGameFiled(mh.getPlayersHands());// gfの場のカード情報とfirstGFの初期化
		}

		gft.init(parentGF);
		long number = 0;
		ArrayList<Long> cards = ObjectPool.getArrayLong();
		for (int i = 0; i < arraySize; i++) {
			number = 0;
			for (int num : arrayListMelds.get(i).getCards()) {
				if (num >= 64) {
					number = 0;
				} else {
					number = number | ((long) 1 << num);
				}
			}
			cards.add(number);
		}

		gft.firstInitChildren(gft.getParent(), cards, dc.getWd());// 子供の作成

		ObjectPool.releaseArrayLong(cards);

		int putRandom = 0; // 最初に出す手をランダムに選ぶ

		boolean growUpChildren = false;

		long start;

		/** MeldDataの出した順番を格納する 最後のサイズはそれ以上木が作られることがないため **/
		ArrayList<Integer> meldDataOrder = ObjectPool.getArrayInt();

		/** 変数の初期化終了 **/

		// メインループ
		for (int playout = 1; playout <= COUNT; playout++) {
			if(InitSetting.randomInitHandMode && playout % 2000 == 0){
				gft.realseChildrenOfAllNonParentGameFeild();
				parentGF.initPLaceGameFiled(initHands(md, fd));// gfの場のカード情報とfirstGFの初期化
			}

			if (InitSetting.DEBUGMODE) {
				System.out.println("playout" + playout);
			}
			start = System.currentTimeMillis();

			playGF = parentGF.clone();// gfを最初の状態に戻す

			meldDataOrder.clear(); // 1ゲームごとにMeldDataに出したものを記憶するために初期化

			learning = InitSetting.LEARNING_W;

			while (true) { // 1プレイ分のループ

				if (playGF.getHaveChildNumber() == 0) {// 子供を持っていないとき

					playGF.useSimulationBarancing(InitSetting.LEARNING, dc);

				} else {
					putRandom = gft.getUCBPos(playGF.getHaveChildNumber(), playGF, dc.getWd(), learning);

					meldDataOrder.add(putRandom);// 木の通った順番を記憶する
					System.out.println();
					playGF = gft.returnGameFeild(playGF.getHaveChildNumber(), putRandom).clone();

					if (playGF.checkGoalPlayer())// game終了木だった時
						break;

					growUpChildren = playGF.doGrowUpTree(); // 木が成長できるかどうかの探索

					continue;
				}
				if (playGF.checkGoalPlayer()) { // 上がった人の判定
					if (InitSetting.DEBUGMODE)
						System.out.println("時間は"
								+ (System.currentTimeMillis() - start) + "ms");
					break;// 自分上がった時

				}
				playGF.endTurn();// ターン等々の処理
			}
			gft.upDateTree(meldDataOrder, playGF.returnWinPoint());

			learning = learning * 0.99;

			if (growUpChildren) {
				gft.initChildren(meldDataOrder, dc.getWd());
				growUpChildren = false;
			}

		}
		int resultPos = gft.returnPutPos();

		if (InitSetting.GAMERECORD)
			parentGF.writeText(gft.childrenGameFeild.get(1).get(resultPos).getWinPoint()); // 棋譜データ作成

		gft.realseAllGameFeild();// 全てのゲーム
		ObjectPool.releaseArrayInt(meldDataOrder);

		return arrayListMelds.get(resultPos).getMeld();

	}

	/**
	 * 手札を生成するメソッド
	 *
	 * @return 手札の配列を返す
	 */
	private int[][] initHands(MyData md, FieldData fd) {
		int[][] resultHands = new int[5][53];

		ArrayList<Integer> notLookCards = ObjectPool.getArrayInt();// まだ見えていないカード群を格納

		int[] feild = md.getField();// 場の残っているカードの配列をコピってあげる

		// 初期化
		for (int i = 0; i < 53; i++) {
			for (int j = 0; j < 5; j++) {
				resultHands[j][i] = 0;
			}
			if (feild[i] == 1)// まだ見えていないカードだった場合
				notLookCards.add(i);
		}

		int arraySize = 0;
		int seatSize = 0;
		int random = 0;

		for (int i = 0; i < 5; i++) {
			if (i == mySeat) {// 自分の座席の時
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

		ObjectPool.releaseArrayInt(notLookCards);
		return resultHands;
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
		if (myhand.contains(Card.S3)
				&& placeMeld != null
				&& placeMeld.type() == Meld.Type.SINGLE
				&& (placeMeld.rank() == Rank.JOKER_HIGHEST || placeMeld.rank() == Rank.JOKER_LOWEST)) {// スペードの3を持っているか否か
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
