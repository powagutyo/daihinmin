package object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import object.Pair.Status;
import utility.Changer;

import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.card.Meld.Single;
import jp.ac.uec.daihinmin.player.BotSkeleton;

//モンテカルロ法を実装するときはこのクラスを用いる
public class Hand implements Cloneable {
	/** 自分がjokerがを所持しているかどうか */
	private boolean joker;
	/** 一番強いカードのランクを選ぶ 0～13のカード　0→3 2→13 */
	private int strongCardRank;
	/**
	 * 自分の手札をint[]に置換えたもの 0がjoker そこから .スペード. ハート ,ダイヤ ,クラブの順
	 * 数字　0が持っていない　1が持っている 2は自分の持っているカードでかつ単発出し以外にも使えるもの 3は階段や複数出しで被っているカード
	 * */
	private int[] myHand;
	/** 持っている数字の枚数を記憶しているもの 0→3 12→2 */
	private int[] ranknumber;
	/**
	 * 評価値 最初の評価値を25に設定する。 PASSの基準として評価値が25未満の時にPASSにする
	 * また評価値が-の時は単体出しではabsで扱いそれ以外は/-10で扱う
	 */
	private int[] evaluation;
	/** 一番最初の評価値 */
	private final int firstEvaluation = 25;
	/* 今は使用していない */
	private ArrayList<Pair> melds;
	/** 単体出しのカード集合 */
	private ArrayList<Pair> singleMelds;
	/** 複数枚出しのカード集合 */
	private ArrayList<Pair> pairMelds;
	/** 階段出しのカード集合 */
	private ArrayList<Pair> stairMelds;
	/** プレイヤーの手札 */
	private ArrayList<Integer> playerHand;
	/** 場に出せるカードの集合 */
	private ArrayList<Pair> canPutHands;

	public Hand(Cards cards) {
		/*
		 * 変数の初期化
		 */
		melds = new ArrayList<Pair>();
		singleMelds = new ArrayList<Pair>();
		pairMelds = new ArrayList<Pair>();
		stairMelds = new ArrayList<Pair>();
		playerHand = new ArrayList<Integer>();
		canPutHands = new ArrayList<Pair>();

		myHand = new int[53];
		evaluation = new int[53];
		ranknumber = new int[13];
		init(cards);
	}

	public Hand() {
		melds = new ArrayList<Pair>();
		singleMelds = new ArrayList<Pair>();
		pairMelds = new ArrayList<Pair>();
		stairMelds = new ArrayList<Pair>();
		playerHand = new ArrayList<Integer>();
		canPutHands = new ArrayList<Pair>();

		myHand = new int[53];
		evaluation = new int[53];
		ranknumber = new int[13];

	}

	/***
	 * 初期化用のメソッド
	 */
	public void init() {
		melds.clear();
		singleMelds.clear();
		pairMelds.clear();
		stairMelds.clear();
		playerHand.clear();
		canPutHands.clear();
		/* 変数の初期化 */
		joker = false;
		for (int i = 0; i < 53; i++) {
			myHand[i] = 0;
			evaluation[i] = 0;
		}
		for (int i = 0; i < 13; i++) {
			ranknumber[i] = 0;
		}
	}

	/**
	 * 初期化のメソッド
	 * 
	 * @param cards
	 *            渡したカード
	 */
	public void init(Cards cards) {
		init();
		// 自分の持っているカードの集合をmyHandに分けている
		for (Card card : cards) {
			if (card == Card.JOKER) {// JOKERの時にtrue
				joker = true;
				myHand[0] = 3;
				continue;
			}
			myHand[Changer.changeIntMark(card) * 13 + card.rank().toInt() - 2] = 1;
			ranknumber[card.rank().toInt() - 3]++;
		}
		strongCardRank = 0;

		// searchMyHand();
		divideHand();
		searchPair();// ペアの探索
	}

	/**
	 * 初期化のメソッド
	 * 
	 * @param array
	 *            自分の持っているカード群
	 */
	public void init(ArrayList<Integer> array) {
		init();
		// 自分の持っているカードの集合をmyHandに分けている
		int size = array.size();
		int num = 0;
		for (int i = 0; i < size; i++) {
			num = array.get(i);
			if (num == 0) {
				joker = true;
				myHand[0] = 3;
				continue;
			}
			myHand[num] = 1;
			ranknumber[num % 13]++;

		}
		strongCardRank = 0;
		// searchMyHand();
		divideHand();
		searchPair();// ペアの探索
	}

	/**
	 * myHandからカード群(handCard)に格納する
	 */
	public void divideHand() {
		playerHand.clear();
		int num = 0;
		for (int i = 1; i < 14; i++) {// 普通の役の処理
			for (int j = 0; j < 4; j++) {
				num = i + j * 13;
				if (myHand[num] != 0) {
					strongCardRank = i - 1;// 一番強いカードを格納
					playerHand.add(num);
				}
			}
		}
		if (myHand[0] > 0)// JOKERの処理
			playerHand.add(0);
		Collections.sort(playerHand);
	}

	/***
	 * 自分の手札からペアとなるものを抜き出す またjokerを用いる時は階段のみ適応
	 */
	public void searchPair() {
		singleMelds.clear();
		pairMelds.clear();
		stairMelds.clear();

		searchSingleMelds(); // 単体出しの探索

		searchPairMelds();// 複数出しの探索

		searchStairMelds();// 階段出しの探索

	}

	/***
	 * 単体出しの役の探索
	 */
	public void searchSingleMelds() {
		int size = playerHand.size();
		int num = 0;
		for (int i = 0; i < size; i++) {
			num = playerHand.get(i);
			if (num == 0) {// jokerの時
				singleMelds.add(new Pair(0, 14, true));
			} else {// joker以外の時
				num--;
				singleMelds.add(new Pair(num / 13, num % 13 + 1, false));
			}
		}
	}

	/***
	 * 複数枚出しの役の探索
	 */
	public void searchPairMelds() {
		boolean[] mark = new boolean[4];
		for (int i = 0; i < 4; i++) {
			mark[i] = false;
		}
		// ペア出しの判定 JOKER無しの判定
		for (int i = 0; i < 13; i++) {// 数字の判定
			if (ranknumber[i] >= 2) {// 同じrankのカードが2枚以上存在する時
				for (int j = 0; j < 4; j++) {// マークでの判定
					if (myHand[j * 13 + i + 1] >= 1) {
						mark[j] = true;
						myHand[j * 13 + i + 1] = 2;
					}
				}
				pairMelds.add(new Pair(mark, ranknumber[i], i + 1, false));// 役を格納

				for (int j = 0; j < 4; j++) {// markの初期化
					mark[j] = false;
				}
			}
		}

		// ペア出しの判定 JOKER有りの判定
		// TODO jokerの判定部が重なっていない
		if (joker) {
			for (int i = 0; i < 13; i++) {// 数字の判定
				if (ranknumber[i] >= 1) {// 同じrankのカードが2枚以上存在する時
					for (int j = 0; j < 4; j++) {// マークでの判定
						if (myHand[j * 13 + i + 1] >= 1) {
							mark[j] = true;
							myHand[j * 13 + i + 1] = 2;
						}
					}
					pairMelds
							.add(new Pair(mark, ranknumber[i] + 1, i + 1, true));// 役を格納

					for (int j = 0; j < 4; j++) {// markの初期化
						mark[j] = false;
					}
				}
			}
		}

	}

	/**
	 * 階段出しの役の探索
	 */
	public void searchStairMelds() {
		int size = 0;
		ArrayList<Integer> stairs = new ArrayList<Integer>();

		// 階段の判定
		for (int i = 1; i < 53; i++) {
			if (myHand[i] > 0) {// 自分の手
				stairs.add(i);
				if (searchStairs(i + 1, stairs, (i - 1) / 13)) {// 階段の探索
					stairMelds
							.add(new Pair((i - 1) / 13, stairs.size(),
									new ArrayList<Integer>(stairs), stairs
											.contains(0)));// 役を格納
					size = stairs.size();
					for (int j = 0; j < size; j++) {
						myHand[stairs.get(j)] += 2;
					}
				}
				stairs.clear();
			}
		}

	}

	/**
	 * 階段が出来るかどうかの探索
	 * 
	 * @parm card 調べているカード配列の番号
	 * @parm stairs 階段となっているランクの集合体
	 * @return
	 */
	public boolean searchStairs(int card, ArrayList<Integer> stairs, int mark) {
		boolean result = false;
		if ((card - 1) / 13 != mark) {// 13で割り切れる時は違うマークになるため
			return false;
		}
		if (myHand[card] >= 1) {// 階段の次のカードを所持している時
			stairs.add(card);
			result = searchStairs(card + 1, stairs, mark);
		} else if (joker) {// jokerを持っている時
			joker = false;
			stairs.add(0);
			result = searchStairs(card + 1, stairs, mark);
			joker = true;
		}
		if (stairs.size() > 2)// 階段の大きさ
			result = true;
		return result;
	}

	/**
	 * 自分の手が場に置けるかどうかの判定
	 * 
	 * @param　renew　自分の手から始まるかどうか
	 * @parm bs BotSkeleton
	 */
	public void searchCanPut(boolean renew, BotSkeleton bs) {
		// TODO 全ての通りを考えるかどうかは速度を考えてから決める
		canPutHands.clear();
		if (renew) {// 自分の手から始まる時
			for (Pair p : singleMelds) {
				if (!p.joker)
					canPutHands.add(p);
			}
			for (Pair p : pairMelds) {
				if (!p.joker) {// jokerが含まれている時
					canPutHands.add(p);
				} else {// jokerが含まれていない時
					if (p.cardSize >= 3)// 3枚出し以上の時
						canPutHands.add(p);
				}
			}
			for (Pair p : stairMelds) {
				canPutHands.add(p);
			}
		} else {// それ以外
			int num = bs.place().rank().toInt() - 2;
			int cardSize = bs.place().size();
			boolean lockSuits = false;
			boolean[] mark = new boolean[4];

			if (!bs.place().lockedSuits().isEmpty()) {
				lockSuits = true;
				for (Suit s : bs.place().lockedSuits()) {
					mark[Changer.changeIntMark(s)] = true;
				}
			}
			List<Pair> pairs;
			if (bs.place().type() == Meld.Type.SINGLE) {// 1枚出しの時
				pairs = singleMelds;
			} else if (bs.place().type() == Meld.Type.GROUP) {// 2枚出し
				pairs = pairMelds;
			} else {
				pairs = stairMelds;
			}
			for (Pair p : pairs) {
				if (num < p.minCard) {// カードのランク比較
					if (cardSize == p.cardSize) {
						if (lockSuits && num != 13) {// 縛りがある且つ2のカードじゃない時
							if (!p.checkCardMark(mark))
								continue;
						}
						canPutHands.add(p);
					}
				}
			}
			canPutHands.add(new Pair());// PASSを入れる
		}
	}

	/**
	 * 自分の手札が単発のみの組み合わせが2枚以上存在する時
	 * 
	 * @return
	 */
	public boolean check_single_2(int strongestRank) {
		boolean result = true;
		int counter = 0;
		int maxresult = 2;
		if (joker)
			maxresult = 3;
		for (int i = 0; i < 4; i++) {
			for (int j = 1; j <= 13; j++) {
				if (j == 6 || j == strongestRank)// 8と最強カードの時
					continue;
				if (myHand[i * 13 + j] == 1)
					counter++;
				if (counter >= maxresult) {
					result = false;
					break;
				}
			}
			if (!result)
				break;
		}
		return result;
	}
	/***
	 * canPutHandsSizeの大きさを返すメソッド
	 */
	public int getCunPutHandsSize() {
		return canPutHands.size();
	}

	/**
	 * 自分の手札から評価値を考える 評価値を考える時に場の状態から推測
	 * 
	 * @param st
	 *            　自分の状態
	 */
	public void calcEvaluation(Situation st) {
		// TODO また縛りを狙うように立ち回る
		// TODO 縛りを取れて、必ず流せてる手を考える
		// TODO 状態の遷移を全てのカード数から考えるようにする。　ここら辺を変えることにより状態も変わるかも
		/*
		 * 大貧民の時などはダブるの手をくずさないようにする。 =<13以上の枚数が3枚以上の時のみダブるをくずして単体出しする
		 */

		// TODO この評価値テーブルを用いると2の評価値がダブるとかで用いてしまうと出してしまうので変更

		int num = 0;
		boolean flag = false;// for文の抜けたときの判定用のbool
		int numberOfStrongCard = ranknumber[10] + ranknumber[11]
				+ ranknumber[12];// この中にはJOKERやKやAや2のカードを格納
		if (joker)// Jokerを持っている時
			numberOfStrongCard++;
		for (int i = 0; i < 53; i++) {// 評価値の初期化
			if (myHand[i] > 0) {
				evaluation[i] = firstEvaluation;
			} else {
				evaluation[i] = 0;
			}
		}
		if (st == Situation.Opening) {// 序盤　
			/*
			 * 序盤は自分の手札を温存する戦略をとる 主に一番強いカードや重なっているカードや8のカードを温存する
			 * また、三枚だしよりも一枚出しを優先的に消費して上がりやすくする。
			 * また、自分の手札にAや2やJOKERの枚数が多いときは積極的に2などを出すようにして手札の枚数を減らしていく
			 * 場に出ているマークで評価値を少しずらすのもよさそう
			 */
			for (int i = 0; i < 53; i++) {
				// 序盤は絶対Jokerを出さないようにする
				if (i == 0 && joker)// Jokerの時の判定
					evaluation[0] = 0;
				if (myHand[i] > 0) {// 手札に1枚以上の数が存在する時
					num = i % 13;
					switch (num) {
					case 0:// 2の判定
							// TODO
						/*
						 * 　Aと2とJOKERの持っている数が4枚で数えてるが変更することによって違う結果が生まれるかも　
						 * また、平均のrank数とかから計算してもいいかも
						 */

						if (myHand[i] >= 3) {// 階段の時の判定 温存
							evaluation[i] = 0;
						} else {
							if (numberOfStrongCard >= 4) {
								if (ranknumber[12] >= 2) {// 2枚以上存在する時
									evaluation[i] = -25;
									// 評価値をそのまま
								} else {// 温存
									evaluation[i] = 0;
								}
								// TODO
							} else {// 温存
								evaluation[i] = 0;
							}
						}
						break;
					case 6:// 8の時の判定
						evaluation[i] = 0;
						break;
					case 12: // Aの判定
						if (strongCardRank <= 11) {// 一番強いカードがAより小さい時、温存
							evaluation[i] = 0;
						} else {// 2のカードが存在する時
							if (myHand[i] >= 2) {// pairとして使われている場合
								evaluation[i] = 0;
							} else {// pairとして使われていない時
								evaluation[i] += 2;
							}
						}
						break;
					default:
						if (myHand[i] >= 2) {// pairとして使われている場合
							if (myHand[i] >= 3) {// 階段の時
								num = melds.size();
								flag = false;
								for (int j = 0; j < num; j++) {
									if (melds.get(j).check_Card(i)) {
										if (!melds.get(j).joker) {
											evaluation[i] = 0;
											flag = true;
											break;
										}
									}
								}
								if (!flag)
									evaluation[i] -= (num + 1) * 2;
								flag = true;
							} else {
								evaluation[i] -= (num + 1) * 2;
							}
						} else {// pairとして使われていない時
							evaluation[i] += (13 - num) * 2;
						}
						break;
					}
				}
			}
			if (strongCardRank == 10 && strongCardRank != 11) {// Aと2以外が一番強い時そのカードを温存
				for (int i = 0; i < 4; i++) {
					evaluation[strongCardRank + i * 13] = 0;
				}
			}

		} else if (st == Situation.Middle) {
			/*
			 * 中盤は自分の手札の枚数を減らすことを考えて立ち回る 中盤までには単体出しの数をほぼなくすことを目的とする また
			 * pairしかない時の場合を考えてpairを分解する
			 */

			for (int i = 0; i < 53; i++) {

				if (i == 0 && joker)// Jokerの時の判定
					evaluation[0] = 0;
				if (myHand[i] > 0) {// 手札に1枚以上の数が存在する時
					num = i % 13;
					switch (num) {
					case 0:// 2の判定
						if (numberOfStrongCard >= 3) {
							if (myHand[i] >= 2) {
								evaluation[i] = -25;

							} else {
								evaluation[i] = 2;
							}
						} else {
							if (myHand[i] >= 2) {
								evaluation[i] = 2;
							} else {
								evaluation[i] = 25;

							}
						}
						break;
					case 6:// 8の時の判定
						/* Aの時と同じ評価値にしている */
						if (myHand[i] >= 2) {// pairとして使われている場合
							evaluation[i] = 3;
						} else {// pairとして使われていない時
							evaluation[i] += 2;

						}
						break;

					default:
						if (myHand[i] >= 2) {// pairとして使われている場合
							evaluation[i] -= (num + 1) * 2;

						} else {// pairとして使われていない時
							evaluation[i] += (12 - num) * 2;

						}
						break;
					}
				}

			}
			for (int i = 0; i < 4; i++) {// 一番強いカードを残す
				evaluation[strongCardRank + 13 * i] = 0;
			}

		} else {
			/*
			 * 終盤は自分の手札が全部ダブるの時は一番強いのを単体出しとして出していく。 また出せるカードをどんどん出していく
			 */
			for (int i = 0; i < 53; i++) {

				if (i == 0 && joker)// Jokerの時の判定
					evaluation[0] = 1;
				if (myHand[i] > 0) {// 手札に1枚以上の数が存在する時
					num = i % 13;
					if (myHand[i] >= 2) {// pairとして使われている場合
						evaluation[i] -= (num + 1);

					} else {// pairとして使われていない時
						evaluation[i] += (12 - num) * 2;

					}

				}

			}
		}
	}

	/**
	 * 単体出しの縛りのマークの配列 主に評価値を10あげる
	 * 
	 * @param mark
	 *            マークをint型で表したもの
	 */
	public void calcEvalution_Mark(int mark) {
		for (int i = 1; i < 13; i++) {// 2以外の数
			if (evaluation[mark * 13 + i] == 0)
				continue;
			evaluation[mark * 13 + i] += 10;
		}
	}

	/**
	 * 受け取ったMeldから評価値を返す
	 * 
	 * @return　int評価値を返す
	 */
	public int returnEvalution(Meld meld) {
		int result = 1;
		int rank = 0;
		int mark = 0;
		int num = 0;
		if (meld.type() == Meld.Type.SEQUENCE) {// 階段の時だけ重み重くする
			result = 2;
		}
		for (Card card : meld.asCards()) {// カードごとに評価値を出す
			if (card == Card.JOKER) {
				result *= evaluation[0];
				continue;
			}
			rank = card.rank().toInt() - 2;
			mark = Changer.changeIntMark(card);

			if (meld.type() == Meld.Type.SINGLE) {// 階段の時だけ重み重くする
				result = result * Math.abs(evaluation[rank + mark * 13]);// 評価値の計算
			} else {
				num = evaluation[rank + mark * 13];
				if (num < 0) {// ペア崩し以外のとき
					num = 2;
				}
				result = result * num;
			}
		}
		return result;
	}

	/**
	 * 受け取ったMeldから評価値を返す
	 * 
	 * @return　int評価値を返す
	 */
	public int returnEvalution_renew(Meld meld) {
		int result = 1;
		int rank = 0;
		int mark = 0;
		int num = 0;
		int correction = 0;// ペア出しの補正値
		int counter = 0;
		if (meld.type() == Meld.Type.SEQUENCE) {// 階段の時だけ重み重くする
			result = 2;
		}
		for (Card card : meld.asCards()) {// カードごとに評価値を出す
			if (card == Card.JOKER) {
				result *= evaluation[0];
				counter++;
				continue;
			}
			rank = card.rank().toInt() - 2;
			mark = Changer.changeIntMark(card);

			if (meld.type() == Meld.Type.SINGLE) {// 階段の時だけ重み重くする
				result = Math.abs(evaluation[rank + mark * 13]);// 評価値の計算
			} else {
				num = evaluation[rank + mark * 13];
				if (num < 0) {// ペア崩し以外のとき
					num = 2;
				}
				correction++;

				result = result * num;
			}
			counter++;
		}
		return (result / counter) + correction;
	}

	@Override
	/**
	 * cloneメソッド
	 */
	public Hand clone() {
		try {
			return (Hand) super.clone();

		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}
