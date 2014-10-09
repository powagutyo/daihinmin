package monteCalro;

import java.util.ArrayList;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Meld.Type;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import object.InitSetting;
import utility.MeldSort;

/** 手牌昇順ソート 同じ数の時は 1. スペード2. ハート3. ダイヤ4. クラブ */
/**
 *
 * MonteCalroPlayerのデータクラス
 *
 * @author 飯田伸也
 */
public class MyData {
	/** トランプの枚数 **/
	final int cardNum = 53;

	/** 自分の手札 1が持っているカード **/
	private int[] myHand = new int[cardNum];

	/** 場に出ているカード 1がまだ見えていないカード **/
	private int[] field = new int[cardNum];

	/** 自分の座席番号 **/
	private int seat = 0;

	/** 自分のランク **/
	private int rank = 0;

	/** プレイヤー番号 **/
	private int playerNumber = 0;

	private boolean yomikiri = false;

	private ArrayList<Meld> yomikiriMelds = new ArrayList<Meld>();

	/** コンストラクタ **/
	public MyData(BotSkeleton bs) {
		init(bs);
	}

	/**
	 * 初期化用のメソッド
	 *
	 * @param bs
	 *            BotSkelton
	 */
	public void init(BotSkeleton bs) {
		for (int i = 0; i < cardNum; i++) {
			myHand[i] = 0;// myHandsの初期化
			field[i] = 1;// fieldの初期化
		}

		seat = bs.seat();// 自分の座席番号の更新
		if (bs.rank() == null) {// 初回限定
			rank = 3;// 平民
		} else {
			rank = bs.rank();// 自分のランクの更新
		}

		playerNumber = bs.number(); // プレイヤー番号
		Cards cards = bs.hand();
		int size = cards.size();
		int num = 0;
		for (int i = 0; i < size; i++) {// 自分の手札から更新
			num = Utility.cardParseInt(cards.get(i));
			myHand[num] = 1;
			field[num] = 0;
		}
		yomikiriInit();// 読み切り関係の変数を初期化

	}

	public void yomikiriInit() {
		yomikiri = false;
		yomikiriMelds.clear();
	}

	/**
	 * 出されたカードから自分の手番なら自分の手札のカード除去、相手の手番ならfieldのカードを除去する。
	 *
	 * @param player
	 * @param cards
	 */
	public void removeCards(int player, Cards cards) {
		int num = 0;
		int size = cards.size();

		if (player == this.playerNumber) {// 自分が出したカードの時
			for (int i = 0; i < size; i++) {
				num = Utility.cardParseInt(cards.get(i));
				myHand[num] = 0;

			}
		} else {// 相手が出したカードの時
			for (int i = 0; i < size; i++) {
				num = Utility.cardParseInt(cards.get(i));
				field[num] = 0;

			}
		}
	}

	/**
	 * 読みきり部
	 *
	 * @param bs
	 *            　BotSkelton
	 * @parm mh MakeHandクラス
	 */
	public void darkForce(BotSkeleton bs) {
		if (!yomikiri) {// 読み切った状態の時
			Cards cards = bs.hand();

			Melds resultMelds;

			Meld meld = bs.place().lastMeld();

			Suits lock = bs.place().lockedSuits();

			if (meld != null) {// renew以外の時
				Type type = meld.type();
				// 場の形
				if (type == Meld.Type.SINGLE) {
					resultMelds = Melds.parseSingleMelds(cards);
				} else if (type == Meld.Type.GROUP) {
					resultMelds = Melds.parseGroupMelds(cards);
				} else {
					resultMelds = Melds.parseSequenceMelds(cards);
				}
				// 場の大きさ
				if (!bs.place().isReverse()) {// 通常時
					resultMelds = resultMelds.extract(Melds.rankOver(meld
							.rank()));

				} else {// 革命時
					resultMelds = resultMelds.extract(Melds.rankUnder(meld
							.rank()));
				}
				// 場のサイズ
				resultMelds = resultMelds.extract(Melds.sizeOf(meld.asCards()
						.size()));
				// 縛り
				if (lock != Suits.EMPTY_SUITS) {// 縛りが存在する時
					resultMelds = resultMelds.extract(Melds.suitsOf(lock));
				}
			} else {
				resultMelds = Melds.parseMelds(cards);
			}

			if (resultMelds.size() == 0) {// 出せるカードが存在しない時
				return; // 読み切りを行わない

			}
			long startTime = System.currentTimeMillis();
			if (doYomikiri(bs)) { // 読み切りを行うかどうかの判定

				playYomikiri(bs, resultMelds, lock, startTime); // 読み切りを実行

			}

		}

	}

	/**
	 * 読み切りの実行部
	 *
	 * @param bs
	 *            BotSkelton
	 * @param resultMelds
	 *            出せる役
	 * @param lock
	 *            縛りのマーク(Suits)
	 */
	private void playYomikiri(BotSkeleton bs, Melds resultMelds, Suits lock, long startTime) {
		if (System.currentTimeMillis() - startTime > InitSetting.YOMIKIRITIMRLIMIT)
			return;

		boolean result = false;
		boolean[] placeMark = new boolean[4];// 場のマークを判定
		if (bs.place().lastMeld() == null) {// lastMeldがnullの時
			for (int i = 0; i < 4; i++) {
				placeMark[i] = false;
			}
		} else {
			placeMark = Utility.meldParseSuitsOfBoolean(bs.place().lastMeld());
		}


		Cards cards = bs.hand();
		boolean reverse = bs.place().isReverse();

		ArrayList<Meld> yomikiriMelds = new ArrayList<Meld>();
		Cards c;

		resultMelds = MeldSort.meldsSizeSort(resultMelds);// サイズの大きい順に変換する

		for (Meld m : resultMelds) {
			if (isFlowMeld_P(m, lock, bs.place().isReverse(), placeMark)) { // 場に出ているカードで流せる時
				c = m.asCards();
				cards = cards.remove(c);
				yomikiriMelds.add(m);
				// 革命の処理
				if (m.type() == Meld.Type.GROUP && c.size() >= 4) {
					reverse = !reverse;
				} else if (m.type() == Meld.Type.SEQUENCE && c.size() >= 5) {
					reverse = !reverse;
				}
				result = checkOneTurnKill(cards, reverse, yomikiriMelds);
				if (result) {// 読み切りに成功した時
					break;
				}
				cards = cards.add(c);
				yomikiriMelds.remove(0);
			}
		}

		yomikiri = result; // 読み切りの結果を変数で保持
		this.yomikiriMelds = yomikiriMelds;// 読み切りの手順を格納したLIstをコピー

	}

	/**
	 * ワンターンキルできるかどうかの判定を行うメソッド
	 *
	 * @param cards
	 *            手札集合
	 * @param reverse
	 *            革命かどうか
	 * @param yomikiriMelds
	 *            格納するカード配列
	 * @return trueの時、ワンターンキルができる
	 */
	private boolean checkOneTurnKill(Cards cards, boolean reverse,
			ArrayList<Meld> yomikiriMelds) {
		boolean result = false;
		Melds melds = Melds.parseMelds(cards);
		int size = cards.size();
		melds = MeldSort.meldsSizeSort(melds);

		for (Meld meld : melds) {
			if (size == meld.asCards().size()) {// 読み切りの終了条件
				yomikiriMelds.add(meld); // 最後の役を格納
				return true;
			}
		}
		Cards c;
		for (Meld m : melds) {
			if (isFlowMeld(m, reverse)) {// もしその手で場が流れるなら
				c = m.asCards();
				cards = cards.remove(c);
				yomikiriMelds.add(m);
				// 革命の処理
				if (m.type() == Meld.Type.GROUP && c.size() >= 4) {
					reverse = !reverse;
				} else if (m.type() == Meld.Type.SEQUENCE && c.size() >= 5) {
					reverse = !reverse;
				}
				result = checkOneTurnKill(cards, reverse, yomikiriMelds);
				if (result) {// 読み切りに成功した時
					break;
				}
				cards = cards.add(c);
				yomikiriMelds.remove(yomikiriMelds.size() - 1);
			}
		}
		return result;
	}

	/**
	 * 読み切りを行うかどうかの判定
	 *
	 * @parm bs BotSkelton
	 * @return
	 */
	private boolean doYomikiri(BotSkeleton bs) {
		boolean result = false;

		boolean[] placeMark = new boolean[4];// 場のマークを判定
		if (bs.place().lastMeld() == null) {// lastMeldがnullの時
			for (int i = 0; i < 4; i++) {
				placeMark[i] = false;
			}
		} else {
			placeMark = Utility.meldParseSuitsOfBoolean(bs.place().lastMeld());
		}
		/***
		 * 10,10,10,j,j,j,q,qなどは考慮していない
		 */

		int[] hand = myHand.clone();
		ArrayList<int[]> myHands = new ArrayList<int[]>();

		Cards cards = bs.hand();

		restSequenceHands(hand, cards, myHands);// myHandに全ての通りの階段を除外

		ArrayList<int[]> handSize = sendArrayHandsSize(myHands);// 手札の枚数表記に変更
		// 変数群の生成
		int size = handSize.size();

		boolean reverse = bs.place().isReverse();// 革命の判定

		int strongestSingle = searchStrongestSingleRank(reverse);

		int strongestDouble = searchStrongestDoubleRank(reverse);

		for (int i = 0; i < size; i++) {
			if (checkYomikiri(handSize.get(i), strongestSingle,
					strongestDouble, reverse)) {// 読み切りできるかどうかの判定
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * 読み切りできる手札かどうかを確認するメソッド
	 *
	 * @param handSize
	 *            ランクごとのカード枚数
	 * @param strongestSingle
	 *            一番強い単体出しカード
	 * @param strongestDouble
	 *            一番強い２枚出しカード
	 * @param reverse
	 *            革命かどうか
	 * @return 読み切りできるかどうか
	 */
	private boolean checkYomikiri(int[] handSize, int strongestSingle,
			int strongestDouble, boolean reverse) {
		boolean result = false;

		boolean joker = false;

		if (handSize[0] == 1) {
			joker = true;
			handSize[0] = 0;
		}
		if (strongestSingle == -1)
			return result;

		handSize[6] = 0;// 8切りの処理

		if (!reverse) {// 通常時
			for (int i = strongestSingle; i < 14; i++) {
				handSize[i] = 0;
			}
			for (int i = 0; i < 14; i++) {
				if (handSize[i] >= 3) {// 3枚以上の時
					handSize[i] = 0;
				} else if (handSize[i] == 2) {// 2枚出しの時
					if (i >= strongestDouble) {// ダブルの最強より強い場合
						handSize[i] = 0;
					}
				}
			}
		} else {
			for (int i = strongestSingle; i > 0; i--) {
				handSize[i] = 0;
			}
			for (int i = 13; i > 0; i--) {
				if (handSize[i] >= 3) {// 3枚以上の時
					handSize[i] = 0;
				} else if (handSize[i] == 2) {// 2枚出しの時
					if (i <= strongestDouble) {// ダブルの最強より強い場合
						handSize[i] = 0;
					}
				}
			}
		}

		int counter = 0;
		if (joker) {
			counter = -1;
		}
		for (int i = 0; i < 14; i++) {
			if (handSize[i] >= 1) {// カードが存在する時
				counter++;
			}
		}
		if (counter <= 1) {// 最後に出すカードのみとなった時
			result = true;
		}

		return result;
	}

	/**
	 * 　一枚出しの一番強いカードを抜き出すメソッド
	 *
	 * @param reverse
	 *            革命かどうか
	 * @return
	 */
	private int searchStrongestSingleRank(boolean reverse) {
		int result = -1;
		if (!reverse) {// 通常時
			for (int i = 13; i > 0; i--) {
				for (int l = 0; l < 4; l++) {
					if (field[i + l * 13] == 1) {// もしそのランクが存在する時
						result = i;
						break;
					}
				}
				if (result != -1) {// resultが上書きされた時
					break;
				}
			}

		} else {// 革命時

			for (int i = 0; i < 14; i++) {
				for (int l = 0; l < 4; l++) {
					if (field[i + l * 13] == 1) {// もしそのランクが存在する時
						result = i;
						break;
					}
				}
				if (result != -1) {
					break;

				}
			}
		}
		return result;
	}

	/**
	 * 2枚出しの一番強いカードを抜きだすメソッド
	 *
	 * @param reverse
	 *            革命かどうか
	 * @return
	 */
	private int searchStrongestDoubleRank(boolean reverse) {
		// ちょっとゆるく設定している
		int result = -1;
		int counter = 0;
		boolean joker = false;
		if (field[0] == 1) {// jokerが場に出ていない時
			joker = true;
			counter = 1;
		}
		if (!reverse) {// 通常時
			for (int i = 13; i > 0; i--) {
				for (int l = 0; l < 4; l++) {
					if (field[i + l * 13] == 1) {// もしそのランクが存在する時
						counter++;
					}
				}
				if (counter >= 2) {
					result = i;
					break;
				}

				counter = 0;
				if (joker)
					counter = 1;
			}

		} else {// 革命時

			for (int i = 0; i < 14; i++) {
				for (int l = 0; l < 4; l++) {
					if (field[i + l * 13] == 1) {// もしそのランクが存在する時
						counter++;
					}
				}
				if (counter >= 2) {
					result = i;
					break;
				}

				counter = 0;
				if (joker)
					counter = 1;

			}
		}
		return result;
	}

	/**
	 * 階段の全ての役に対しての自分の手札集合考える。この時にmyHandsに格納していくメソッド
	 *
	 * @param myHand
	 *            手札
	 * @param cards
	 *            自分の持っているカード
	 * @param myHands
	 *            手札を格納するArrayList<int[]>
	 */
	private void restSequenceHands(int[] myHand, Cards cards,
			ArrayList<int[]> myHands) {
		Melds melds = Melds.parseSequenceMelds(cards);

		if (melds.size() == 0) {// 階段の役が存在しない時
			myHands.add(myHand.clone());
			return;
		}
		for (Meld meld : melds) {
			for (Card card : meld.asCards()) {
				cards = cards.remove(card);
				myHand[Utility.cardParseInt(card)] = 0;
			}

			restSequenceHands(myHand, cards, myHands); // int[]の束を生成

			for (Card card : meld.asCards()) {
				cards = cards.remove(card);
				myHand[Utility.cardParseInt(card)] = 0;
			}
		}

	}

	/**
	 * (ArrayListrに格納されている)手札の配列を手札の枚数表記の配列に変更するメソッド
	 *
	 * @param myHands
	 *            手札の配列をたくさん格納したArrayLIst配列
	 * @return 手札を枚数表記に変更したArrayList配列
	 */
	private ArrayList<int[]> sendArrayHandsSize(ArrayList<int[]> myHands) {
		ArrayList<int[]> result = new ArrayList<int[]>();

		int size = myHands.size();
		for (int i = 0; i < size; i++) {
			result.add(sendHandSize(myHands.get(i)));
		}

		return result;
	}

	/**
	 * 手札の配列を手札の枚数表記の配列に変更するメソッド
	 *
	 * @param myHands
	 *            　手札の配列
	 * @return　手札の枚数表記の配列
	 *
	 */
	private int[] sendHandSize(int[] myHands) {
		int[] result = new int[14];

		for (int i = 0; i < 14; i++) {// 初期化
			result[i] = 0;
		}

		if (myHands[0] == 1) {// jokerの処理
			result[0] = 1;
		}

		int counter = 0;
		for (int i = 1; i < 14; i++) {
			for (int l = 0; l < 4; l++) {
				if (myHands[i + l * 13] == 1) {// そのランクのカードを持っている時
					counter++;
				}
			}
			result[i] = counter;
			counter = 0;
		}

		return result;
	}

	/**
	 * 場が流れるカードかどうかの判定(場にカードがある時の判定)
	 *
	 * @param meld
	 *            自分が今出せる全ての役
	 * @param lockSuits
	 *            縛りの集合
	 * @param reverse
	 *            革命している時true
	 * @param placeMark
	 *            場のマークを記憶
	 * @return
	 */
	private boolean isFlowMeld_P(Meld meld, Suits lockSuits, boolean reverse,
			boolean[] placeMark) {
		/***
		 * 単体出しは絶対に厳守する 2枚出しの時には縛りの時は必ず通ると仮定する 3枚出し以上の時は仮定する。
		 **/
		if (meld.type() == Meld.Type.SINGLE
				&& meld.asCards().get(0) == Card.JOKER) {// Joker単騎の処理
			if (field[1] == 1) {// スペ3がある時
				return false;
			} else {
				return true;
			}
		}
		int rank = meld.rank().toInt() - 2;// ランク
		int num = 0;
		boolean result = true;

		if (rank == 6) {// 8切りの処理
			return result;
		}

		boolean[] suit = new boolean[4]; // lockされているsuitを格納

		for (int i = 0; i < 4; i++) {// suitの初期化
			suit[i] = true;
		}
		/**
		 * if (lockSuits != Suits.EMPTY_SUITS) {// 場の状態が縛りの時 suit =
		 * Utility.suitsParseArrayBoolean(lockSuits);// 縛られている役の配列を格納 }
		 **/
		boolean doLock = true;// 自分の手で縛りが発生するかどうかの判定用変数
		boolean[] mySuits = Utility.meldParseSuitsOfBoolean(meld);
		for (int i = 0; i < 4; i++) {
			if (mySuits[i] != placeMark[i]) {// マークが同じではない時
				doLock = false;
				break;
			}
		}
		if (doLock) {// 自分の手で縛りが発生する時
			suit = mySuits; // 縛りのマークに更新
		}

		if (meld.type() == Meld.Type.SINGLE) {// 単体出しの時
			if (!reverse) {// 通常時
				for (int i = rank + 1; i < 14; i++) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						if (suit[j]) { // 縛りも考慮
							num = i + j * 13;
							if (field[num] == 1) {
								return false;
							}
						}
					}
				}
			} else {// 革命の時
				for (int i = rank - 1; i > 0; i--) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						if (suit[j]) { // 縛りも考慮
							num = i + j * 13;
							if (field[num] == 1) {
								return false;
							}
						}
					}
				}
			}
		} else if (meld.type() == Meld.Type.GROUP && meld.asCards().size() == 2) {// 2枚出しの判定

			int joker = 0;

			if (field[0] == 1) {// jokerが場に見えていない時
				joker = -1;
			}
			int counter = 0; // 枚数をカウントする変数
			if (!reverse) {
				for (int i = rank + 1; i < 14; i++) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						num = i + j * 13;
						if (suit[j] && field[num] == 1) {
							counter++;
						}
					}
					if (counter >= 2 + joker) {
						return false;
					}
				}
			} else {
				for (int i = rank - 1; i > 0; i--) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						num = i + j * 13;
						if (suit[j] && field[num] == 1) {
							counter++;
						}
					}
					if (counter >= 2 + joker) {
						return false;
					}
				}
			}

		} else {// 3枚出し
			int meldSize = meld.asCards().size();
			if (meldSize == 3) {// 3枚出しの時
				if (meld.type() == Meld.Type.GROUP) {// Groupの三枚出しの時
					int joker = 0;

					if (field[0] == 1) {// jokerが場に見えていない時
						joker = -1;
					}
					int counter = 0; // 枚数をカウントする変数
					if (!reverse) {
						for (int i = rank + 1; i < 14; i++) {// rank
							for (int j = 0; j < 4; j++) {// Suit
								num = i + j * 13;
								if (suit[j] && field[num] == 1) {
									counter++;
								}
							}
							if (counter >= 3 + joker) {
								return false;
							}
						}
					} else {
						for (int i = rank - 1; i > 0; i--) {// rank
							for (int j = 0; j < 4; j++) {// Suit
								num = i + j * 13;
								if (suit[j] && field[num] == 1) {
									counter++;
								}
							}
							if (counter >= 3 + joker) {
								return false;
							}
						}
					}
				} else if (meld.type() == Meld.Type.SEQUENCE) {// 3枚出しのカードの通るかどうかの判定
					boolean joker = false;

					if (field[0] == 1) {// jokerが場に見えていない時の判定
						joker = true;
					}

					if (!reverse) {// 通常時
						if (rank >= 4 && rank <= 6) {// 8切りの判定
							return true;
						} else if (rank >= 9) {// kを含む階段のペア
							return true;
						}

						for (int i = 0; i < 4; i++) { // Suits
							for (int j = rank + 3; j < 14; j++) {// Rank
								if (field[j + i * 13] == 1)
									if (checkSequence(0, 3, rank + 3, i, joker,
											reverse)) {// 階段の判定
										return false;
									}
							}
						}
					} else { // 革命時
						if (rank >= 6 && rank <= 8) {// 8切りの判定
							return true;
						} else if (rank <= 5) {
							return true;
						}
						for (int i = 0; i < 4; i++) { // Suits
							for (int j = rank - 3; j > 0; j--) {// Rank
								if (field[j + i * 13] == 1) {// 見えていないカード
									if (checkSequence(0, 3, rank - 3, i, joker,
											reverse)) {// 階段の判定
										return false;
									}
								}
							}
						}
					}
				}
			}
			// 4枚出しは通ると仮定
			/*** 3枚出しは通ると仮定する ***/
		}
		return result;
	}

	/**
	 * 場が流れるカードかどうかの判定(renewの時専用j)
	 *
	 * @param meld
	 *            自分が今出せる全ての役
	 * @param reverse
	 *            革命している時true
	 * @return
	 */
	private boolean isFlowMeld(Meld meld, boolean reverse) {
		/***
		 * 単体出しは絶対に厳守する 2枚出しの時には縛りの時は必ず通ると仮定する 3枚出し以上の時は仮定する。
		 **/
		if (meld.type() == Meld.Type.SINGLE
				&& meld.asCards().get(0) == Card.JOKER) {// Joker単騎の処理
			if (field[1] == 1) {// スペ3がある時
				return false;
			} else {
				return true;
			}
		}
		int rank = meld.rank().toInt() - 2;// ランク
		int num = 0;
		boolean result = true;

		if (rank == 6) {// 8切りの処理
			return result;
		}

		/**
		 * if (lockSuits != Suits.EMPTY_SUITS) {// 場の状態が縛りの時 suit =
		 * Utility.suitsParseArrayBoolean(lockSuits);// 縛られている役の配列を格納 }
		 **/
		boolean doLock = true;// 自分の手で縛りが発生するかどうかの判定用変数

		if (meld.type() == Meld.Type.SINGLE) {// 単体出しの時
			if (!reverse) {// 通常時
				for (int i = rank + 1; i < 14; i++) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						num = i + j * 13;
						if (field[num] == 1) {
							return false;
						}
					}
				}
			} else {// 革命の時
				for (int i = rank - 1; i > 0; i--) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						num = i + j * 13;
						if (field[num] == 1) {
							return false;
						}
					}
				}
			}
		} else if (meld.type() == Meld.Type.GROUP && meld.asCards().size() == 2) {// 2枚出しの判定
			int joker = 0;

			if (field[0] == 1) {// jokerが場に見えていない時
				joker = -1;
			}
			int counter = 0; // 枚数をカウントする変数
			if (!reverse) {
				for (int i = rank + 1; i < 14; i++) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						num = i + j * 13;
						if (field[num] == 1) {
							counter++;
						}
					}
					if (counter >= 2 + joker) {
						return false;
					}
				}
			} else {
				for (int i = rank - 1; i > 0; i--) {// rank
					for (int j = 0; j < 4; j++) {// Suit
						num = i + j * 13;
						if (field[num] == 1) {
							counter++;
						}
					}
					if (counter >= 2 + joker) {
						return false;
					}
				}
			}

		} else {// 3枚出し
			int meldSize = meld.asCards().size();
			if (meldSize == 3) {// 3枚出しの時
				if (meld.type() == Meld.Type.GROUP) {// Groupの三枚出しの時
					int joker = 0;

					if (field[0] == 1) {// jokerが場に見えていない時
						joker = -1;
					}
					int counter = 0; // 枚数をカウントする変数
					if (!reverse) {// 通常時
						for (int i = rank + 1; i < 14; i++) {// rank
							for (int j = 0; j < 4; j++) {// Suit
								num = i + j * 13;
								if (field[num] == 1) {
									counter++;
								}
							}
							if (counter >= 3 + joker) {
								return false;
							}
						}
					} else { // 革命時
						for (int i = rank - 1; i > 0; i--) {// rank
							for (int j = 0; j < 4; j++) {// Suit
								num = i + j * 13;
								if (field[num] == 1) {
									counter++;
								}
							}
							if (counter >= 3 + joker) {
								return false;
							}
						}
					}
				} else if (meld.type() == Meld.Type.SEQUENCE) {// 3枚出しのカードの通るかどうかの判定
					boolean joker = false;

					if (field[0] == 1) {// jokerが場に見えていない時の判定
						joker = true;
					}

					if (!reverse) {// 通常時
						if (rank >= 4 && rank <= 6) {// 8切りの判定
							return true;
						} else if (rank >= 9) {// kを含む階段のペア
							return true;
						}

						for (int i = 0; i < 4; i++) { // Suits
							for (int j = rank + 3; j < 14; j++) {// Rank
								if (checkSequence(0, 3, rank + 3, i, joker,
										reverse)) {// 相手が階段を作れる時
									return false;
								}
							}
						}
					} else { // 革命時
						if (rank >= 6 && rank <= 8) {// 8切りの判定
							return true;
						} else if (rank <= 5) {
							return true;
						}
						for (int i = 0; i < 4; i++) { // Suits
							for (int j = rank - 3; j > 0; j--) {// Rank
								if (checkSequence(0, 3, rank - 3, i, joker,
										reverse)) {// 相手が階段を作れる時
									return false;
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * 階段役で自分の出した役より強いかどうかを判定するメソッド
	 *
	 * @param counter
	 *            counyer
	 * @param size
	 *            カードの枚数
	 * @param rank
	 *            ランク
	 * @param suit
	 *            マーク
	 * @param joker
	 *            ジョーカーがあるかどうか
	 * @param reverse
	 *            革命しているかどうか
	 * @return
	 */
	public boolean checkSequence(int counter, int size, int rank, int suit,
			boolean joker, boolean reverse) {
		boolean result = false;
		if (size <= counter) {// 階段が作れる時
			return true;
		}

		int JOKER = 0;
		if (joker) {
			JOKER = 1;
		}
		if (!reverse) {// 通常時

			if (rank + size - 14 - counter - JOKER > 0) { // 階段が作れない時の処理
				return false;
			}
			int num = rank + suit * 13;

			if (field[num] == 1) {// そのカードが存在する時
				counter++;
				rank++;
				result = checkSequence(counter, size, rank, suit, joker,
						reverse);
			} else if (joker) {// jokerが存在する時
				counter++;
				rank++;
				joker = false;
				result = checkSequence(counter, size, rank, suit, joker,
						reverse);
			}

		} else {// 革命時
			if (rank - size + counter + JOKER < 0) { // 階段が作れない時の処理
				return false;
			}
			int num = rank + suit * 13;

			if (field[num] == 1) {// そのカードが存在する時
				counter++;
				rank--;
				result = checkSequence(counter, size, rank, suit, joker,
						reverse);
			} else if (joker) {// jokerが存在する時
				counter++;
				rank--;
				joker = false;
				result = checkSequence(counter, size, rank, suit, joker,
						reverse);
			}
		}

		return result;
	}

	// getter
	public int[] getMyHand() {
		return myHand.clone();
	}

	public int[] getField() {
		return field.clone();
	}

	public int getSeat() {
		return seat;
	}

	public int getRank() {
		return rank;
	}

	public int getPlayerNumber() {
		return playerNumber;
	}

	public boolean isYomikiri() {
		return yomikiri;
	}

	public Meld getYomikiriMelds() {
		return yomikiriMelds.remove(0);
	}

}
