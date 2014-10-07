package object;

import java.util.ArrayList;

import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.MeldFactory;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suits;
import jp.ac.uec.daihinmin.player.BotSkeleton;
import utility.Changer;
import utility.MeldSort;

public class MyState {
	/** プレイヤーの人数 */
	private final int numberOfPlayers = 5;
	/** 自分のプレイヤー番号 */
	private int playerNumber;
	/** myRankは大富豪から2 1 0 -1 -2となる */
	private int myRank;
	/** 自分の手札 */
	private Hand myHand;
	/** 　全ての手札の枚数 */
	public int[] numberOfPLayersCards;
	/**
	 * 最初の配列はカードの種類を表す 0. スペード. 1 ハート 2. ダイヤ ,3 クラブ 最後の配列は数字を表す また0 0
	 * 1 0 2 0 3 0をJOKERとする また falseがまだ見ていないCard 1～14は3～2までを表している。
	 * */
	private boolean[][] lookCard;
	/** Start していたらtrue */
	private boolean start;
	/** 自分がjokerがを所持しているかどうか */
	private boolean joker;
	/** スペードの3を持っているかどうか **/
	private boolean have_Spade_3;
	/** スペードの3が場に存在しているかどうか　見えているときtrue */
	private boolean look_Spade_3;
	/** 革命が起きているかどうかの判定 革命が起きている時がtrue */
	private boolean reverse;
	/** 場が新しくなった時にtrue それ以外はfalse */
	private boolean renew;
	/** 革命を起こせるかどうか　革命を起こせる場合はtrue */
	private boolean canReverse;
	/** 革命を起こすかどうか */
	private boolean doReverse;
	/** 　確定で勝てる時かどうかの判定 */
	private boolean oneTurnKill;
	/** 自分の手札に8のカードが存在する時 */
	private boolean existEight;
	/** ペア出しと階段か重なっているかどうか */
	private boolean lapPair;
	/** existEightが存在する時のマーク */
	private boolean[] suitEight;
	/** まだ場に出されていないカードで一番強いとされるカード **/
	private Card strongestCard;
	/** 自分の最後に出した手を記憶させる */
	private Meld beforeMelt;
	/** 自分の手札と場に出ていない全てのカードの集合 */
	private Cards notLookCard;
	/** 自分の手札からJOKERを抜いた手札 */
	private Cards removeJokerMyhand;
	/** 終了の役の手順を格納する */
	public ArrayList<Meld> finshMelds;

	/** 単体出ししかできないカード群 */
	private Melds singleMelds;

	/** 8よりも弱い、単体出ししかできないカード群 **/
	private Melds weekSingleMelds;
	/** 8よりも強い、単体出ししかできないカード群 **/
	private Melds strongSingleMelds;
	/** 革命を起こせる役の集合 */
	private Melds reverseMelds;

	/** 自分の手札にある2とJokerの枚数 */
	private int twoAndOver;
	/** 自分の手札にある3とJokerの枚数 */
	private int threeAndOver;

	/** ターン数の計測用の変数 */
	private int turn;


	/** 自分の手札の平均値 (一番強いカードを除く平均値) */
	private double myHandAverage;
	/** 見えていないカードの平均値 */
	private double notLookAverage;

	private Situation situation;

	public Cards getNotLookCard() {
		return notLookCard;
	}

	/**
	 * コンストラクタ
	 *
	 * @param playerNumber
	 *            自分のプレイヤー番号
	 */
	public MyState(int playerNumber) {
		init(playerNumber);
	}

	/**
	 * 初期化用のメソッド
	 *
	 * @param playerNumber
	 *            自分のプレイヤー番号
	 */
	public void init(int playNumber) {
		// TODO startのboolean変更
		start = false;
		joker = false;
		have_Spade_3 = false;
		look_Spade_3 = false;
		canReverse = false;
		doReverse = false;
		reverse = false;
		oneTurnKill = false;
		existEight = false;
		lapPair = false;

		renew = true;
		lookCard = new boolean[4][14];
		suitEight = new boolean[4];
		for (int i = 0; i < 4; i++) {
			suitEight[i] = false;
		}
		myRank = 0;
		turn = 0;
		twoAndOver = 0;
		threeAndOver = 0;
		myHandAverage = 0;
		notLookAverage = 0;

		this.playerNumber = playNumber;
		strongestCard = Card.S2;// 最初の最強カードは2 JOKERにしない
		removeJokerMyhand = Cards.EMPTY_CARDS;
		initCard();
		finshMelds = new ArrayList<Meld>();
		situation = Situation.Setting;// ゲームの設定の状態にする。
		beforeMelt = null;

	}

	/**
	 * ターン数を足していく また、自分の状態遷移の管理
	 *
	 * @parm bs BotSkeleton
	 */
	public void plusTurn(BotSkeleton bs) {
		int num = 0;
		for (int i = 0; i < 5; i++) {// 全てのプレイヤーのカードの枚数を数える
			num += numberOfPLayersCards[i];
		}
		turn++;
		if (bs.hand().size() == 1) {// 自分の手札が1枚の時
			situation = Situation.Last;
		}

		if (situation == Situation.Opening) {// 序盤の時の処理
			if (turn >= 30) {// ターン数で計測
				situation = Situation.Middle;
			} else {
				if (num <= 30) {// 全体の手札枚数で計測
					situation = Situation.Middle;
				} else {
					if (numberOfPLayersCards[playerNumber] <= 6)// 自分の手札の枚数で計測
						situation = Situation.Middle;
				}
			}
		} else if (situation == Situation.Middle) {// 中盤の時の処理
			if (turn >= 50) {// ターン数で計測
				situation = Situation.Last;
			} else {
				if (num <= 12) {// 全体の手札枚数で計測
					situation = Situation.Last;
				} else {
					if (numberOfPLayersCards[playerNumber] <= 3)// 自分の手札の枚数で計測
						situation = Situation.Last;
				}
			}
		}
	}

	/**
	 * 自分の手札の中の8よりも弱い単体出しの集合を計算する
	 *
	 * @param cards
	 *            自分の手札
	 */
	public void calcWeekSingleMelds(Cards cards) {
		cards = cards.remove(Card.JOKER);

		Melds sequence = Melds.parseSequenceMelds(cards);

		int size = sequence.size();
		// 階段になり得る数の除去
		for (int i = 0; i < size; i++) {
			for (Card card : sequence.get(i).asCards()) {
				cards = cards.remove(card);
			}
		}
		Melds group = Melds.parseGroupMelds(cards);

		// グループになり得る数の除去
		size = group.size();
		for (int i = 0; i < size; i++) {
			for (Card card : group.get(i).asCards()) {
				cards = cards.remove(card);
			}
		}

		singleMelds = Melds.parseMelds(cards);// 単体出ししかできないカード群

		weekSingleMelds = singleMelds.extract(Melds.rankUnder(Rank.EIGHT));// 8以下のカードを格納

		strongSingleMelds = singleMelds.extract(Melds.rankOver(Rank.EIGHT));// 8以上のカードを格納
	}

	/**
	 * 階段とペアが重なっているかどうか調べる
	 *
	 * @param cards
	 *            自分の手札
	 */
	public void examineLapPair(Cards cards) {
		Melds sequence = Melds.parseSequenceMelds(cards);
		Melds group = Melds.parseGroupMelds(cards);
		ArrayList<Rank> groupRank = new ArrayList<Rank>();
		for (Meld g : group) {
			if (!groupRank.contains(g.rank())) {
				groupRank.add(g.rank());
			}
		}
		boolean result = false;
		for (Meld s : sequence) {
			for (Meld g : group) {
				if (s.rank() == g.rank()) {// ランクが等しい時
					result = true;
					for (int i = s.rank().toInt(); i < s.rank().toInt() + 3; i++) {
						if (!groupRank.contains(Rank.valueOf(i))) {
							result = false;
							break;
						}
					}
					if (result)
						break;
				}
			}
			if (result)
				break;
		}
		lapPair = result;

	}

	/**
	 * 全てのカードをnotLookCardに入れてソートする
	 */
	public void initCard() {
		notLookCard = Cards.EMPTY_CARDS;
		notLookCard = notLookCard.add(Card.JOKER);
		for (int i = 0; i < 4; i++) {// マーク
			for (int j = 1; j < 14; j++) {// 数字
				notLookCard = notLookCard.add(Changer.changwMarkInt(i, j));
			}
		}
	}

	/**
	 * 自分の手札にあるカードをlookCardを入れる。 ジョーカーやスペ3のを持っているかどうかの判定
	 *
	 * @param cards自分の手札
	 *
	 */
	public void removeMyHand(Cards cards) {
		myHand = new Hand(cards);// 自分の手札の生成
		myHand.calcEvaluation(Situation.Opening);

		int mark = 0;// markの判定用にしよう
		for (Card card : cards) {
			removeCard(card);// notLookCardの処理
			// JOKERの判定
			if (card.equals(Card.JOKER)) {// Cardsの中にJoker含まれている時
				joker = true;// jokerを保持している状態する
				twoAndOver++;
				threeAndOver++;
				lookCard[0][0] = true;
				continue;
			}
			// スペ3の判定
			if (card == Card.S3) {
				have_Spade_3 = true;
				look_Spade_3 = true;
			}
			// 8の判定
			if (card.rank() == Rank.EIGHT) {
				existEight = true;// 8のカードを持っている
				suitEight[Changer.changeIntMark(card)] = true;// 8のカードのマークを保持
			}
			// 2の判定
			if (card.rank() == Rank.TWO) {
				twoAndOver++;
			}
			// 3の判定
			if (card.rank() == Rank.THREE) {
				threeAndOver++;
			}
			// マークの判定
			mark = Changer.changeIntMark(card);

			// カードのマークとランクから得たデータをtrueに変化させる
			lookCard[mark][card.rank().toInt() - 2] = true;
		}
	}

	/**
	 * 自分の手札を生成する
	 *
	 * @param cards
	 */
	public void initHand(Cards cards) {
		myHand = new Hand(cards);// 自分の手札の生成
		myHand.calcEvaluation(situation);// 評価値を計算させる
	}

	/**
	 * 場に捨てられたカードをlookCardをtrueにする。 また自分のターンの時にスペ3やJOKERを使った時のフラグをfalseにする
	 *
	 * @param number
	 *            　プレイヤーの番号
	 * @param playedMeld
	 *            役の集合
	 * @param bs
	 *            BotSkleton
	 */
	public void look_Card(int number, Meld playedMeld, BotSkeleton bs) {
		// パスでは無い時
		if (playedMeld != MeldFactory.PASS) {// パスじゃない時
			if (bs.number() != number) {// 自分の番じゃない時
				oneTurnKill = false;
				int mark;
				for (Card card : playedMeld.asCards()) {
					if (card == Card.JOKER) {// JOKERの時
						lookCard[0][0] = true;
						continue;
					}
					if (card == Card.S3)
						look_Spade_3 = true;

					mark = Changer.changeIntMark(card);

					lookCard[mark][card.rank().toInt() - 2] = true;
					removeCard(card);
					numberOfPLayersCards[number]--;
				}

			} else {// 自分のターンの場合
				for (Card card : playedMeld.asCards()) {
					if (card == Card.JOKER) {// ジョーカーの時
						joker = false;
						twoAndOver--;
						threeAndOver--;
					} else if (card == Card.S3) {// スペ3の時
						have_Spade_3 = false;
					} else if (card.rank() == Rank.EIGHT) {// 8の数字を出した時
						suitEight[Changer.changeIntMark(card)] = false;
						existEight = false;
						;
						for (int i = 0; i < 4; i++) {
							if (suitEight[i]) {
								existEight = true;
								break;
							}
						}
					} else if (card.rank() == Rank.TWO) {// 2のカード枚数
						twoAndOver--;
					} else if (card.rank() == Rank.THREE) {
						threeAndOver--;
					}
					removeCard(card);
					numberOfPLayersCards[number]--;
				}
			}
		}
	}

	/**
	 * jokerを持っているかどうかをチェックするメソッド
	 *
	 * @param hand
	 *            自分の手札
	 */
	public void haveJoker(Cards hand) {
		if (joker) {
			joker = false;
			for (Card card : hand) {
				if (card == Card.JOKER) {
					joker = true;
					break;
				}
			}
		}
	}

	/**
	 * 自分の手札の平均と見ていないカードの平均を計算する
	 *
	 * @param cards
	 *            自分の手札
	 */
	public void calcAverage(Cards cards) {
		// 自分の手札の平均値を出す
		myHandAverage = cardsCalcAverage(cards);
		// 場に出ていないカードの平均値
		int size = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = 1; j < 14; j++) {
				if (!lookCard[i][j]) {// もしそのカードを見ていない時
					size++;
					notLookAverage += j + 2;
				}
			}
		}
		notLookAverage = notLookAverage / size;
	}

	/**
	 * カードの集合から平均のランクを求める
	 *
	 * @param cards
	 *            平均を取りたいカードの集合
	 * @return ランクの平均
	 */
	public double cardsCalcAverage(Cards cards) {
		double result = 0;
		int size = cards.size();
		for (Card card : cards) {
			if (card == Card.JOKER) {
				size--;
				continue;
			}
			if (!reverse) {// 革命じゃない時
				if (card.rank() == Rank.TWO) {
					size--;
					continue;
				}
			} else {
				if (card.rank() == Rank.THREE) {
					size--;
					continue;
				}
			}
			result += card.rank().toInt();
		}
		result = result / size;
		return result;
	}

	/**
	 * 自分の手札表示 (デバック用)
	 *
	 * @param cards自分の手札
	 */
	public void indicateHand(Cards cards) {
		int counter = 1;
		for (Card card : cards) {
			System.out
					.println(counter + "番目" + card.suit() + "," + card.rank());
			counter++;
		}
	}

	/**
	 * 場に出ているカードと自分の手札から一番強いと思われるカードをstrongestCardに格納する Jokerの時はスペ3の有無は考えていない
	 */
	public Card searchStrongCard(boolean rev) {
		// TODO 一番強いカードの探索　革命に注意
		Card strong = Card.C2;
		boolean finishFlag = false;
		if (!rev) {// 革命が起きていない時
			for (int i = 13; i > 0; i--) {// 大きい方の数字から小さい方の数字へ
				for (int j = 0; j < 4; j++) {// マークを順番に見ていく
					if (!lookCard[j][i]) {
						strong = Changer.changwMarkInt(j, i);
						finishFlag = true;
						break;
					}
				}
				if (finishFlag)
					break;
			}
		} else {
			for (int i = 1; i < 14; i++) {
				for (int j = 0; j < 4; j++) {
					if (!lookCard[j][i]) {
						strong = Changer.changwMarkInt(j, i);
						finishFlag = true;
						break;
					}
				}
				if (finishFlag)
					break;
			}
		}
		strongestCard = strong;
		return strong;
	}

	/**
	 * 場と自分の手札から考えうる一番強い2枚出しのペアのランクを返す
	 *
	 * @parm rev 革命の有無　trueが革命
	 * @return rank 最強と思われる2枚出し
	 */
	public Rank searchRank_double(boolean rev) {
		// TODO 一番強いカードの探索　革命に注意
		Rank rank;
		boolean finishFlag = false;
		int counter = 0;
		if (!rev) {// 革命が起きていない時
			rank = Rank.THREE;
			for (int i = 13; i > 0; i--) {// 大きい方の数字から小さい方の数字へ
				for (int j = 0; j < 4; j++) {// マークを順番に見ていく
					if (!lookCard[j][i]) {
						counter++;
					}
					if (counter >= 2) {
						finishFlag = true;
						break;
					}
				}
				if (finishFlag) {
					rank = Rank.valueOf(i + 2);
					break;
				}
			}
		} else {
			rank = Rank.TWO;
			for (int i = 1; i < 14; i++) {
				for (int j = 0; j < 4; j++) {
					if (!lookCard[j][i]) {
						counter++;
					}
					if (counter >= 2) {
						finishFlag = true;
						break;
					}
				}
				if (finishFlag) {
					rank = Rank.valueOf(i + 2);
					break;
				}
			}
		}
		return rank;
	}

	/**
	 * 自分の手札が革命できるかどうかの判定
	 *
	 * @parm cards 革命できるかどうかの判定する自分の手札
	 */
	public void canReverse(Cards cards) {
		boolean result = false;

		reverseMelds = Melds.EMPTY_MELDS;
		Melds melds = Melds.parseMelds(cards);

		melds = melds.extract(Melds.sizeOver(4));// 革命できそうな手札に変換
		// 革命が起こせるかどうかの判定
		for (Meld meld : melds) {
			if (meld.type() == Meld.Type.GROUP) {
				result = true;
				reverseMelds = reverseMelds.add(meld);
			} else {
				if (meld.asCards().size() >= 5) {// 5枚以上の時
					result = true;
					reverseMelds = reverseMelds.add(meld);
				}
			}
		}

		if (result) {// 革命が出来る時
			canReverse = true;
		} else {// 革命が出来ない時
			canReverse = false;
		}

	}

	/**
	 * 革命を起こすかどうかの判定 canReverseの後に読む必要あり
	 *
	 */
	public void doOrNotReverse() {
		boolean result = false;
		if (canReverse) {// 革命を起こせるかどうか
			if (!reverse) {// 普通時
				if (myRank <= 0) {// 自分のランクが平民以下の時
					result = true;
				} else {
					if (notLookAverage - 1 > myHandAverage) {
						result = true;
					}
				}
			} else {// 革命時
				if (myRank >= 0) {// 自分のランクが平民以上の時
					result = true;
				} else {
					if (notLookAverage > myHandAverage - 1) {
						result = true;
					}
				}
			}
		}
		if (result) {
			doReverse = true;
		} else {
			doReverse = false;
		}
	}

	/**
	 * 前回のターンに自分が出したカードと同じかどうか
	 *
	 * @parm 最後に出されたカードの集合melt
	 * @return　同じならtrue 違うなら false
	 */
	public boolean check_myMelt(Meld lastMeld) {
		if (beforeMelt != null && lastMeld.equals(beforeMelt)) {// 前回の手が存在する時かつ自分の出したカードと同じ時
			return true;
		}
		return false;
	}

	/**
	 * 自分のターンでワンキルができる時の判定
	 *
	 * @param bs
	 *            自身のbotSkelton
	 * @param meld
	 *            場に出された役
	 */
	public void checkGoal(BotSkeleton bs, Meld meld) {

		if (meld == null)
			return;

		finshMelds.clear();

		this.strongestCard = searchStrongCard(reverse); // 一番強いカードを入手する

		Cards hand = Cards.sort(bs.hand());// 自分の手札

		Suits boundSuit = bs.place().lockedSuits();// 縛りの色

		Melds melds = Melds.parseMelds(hand);// 自分の手札を全て分解する

		if (check_Single(hand)) {// 2枚出しや階段を抜いた時に単体出しが2枚以上存在する時
			return;
		}
		// 革命時に対応
		if (!reverse) {
			melds = melds.extract(Melds.rankOver(meld.rank()));
		} else {
			melds = melds.extract(Melds.rankUnder(meld.rank()));
		}

		melds = melds.extract(Melds.typeOf(meld.type()));

		melds = melds.extract(Melds.sizeOf(meld.asCards().size()));// 枚数によって変化させる

		if (melds.size() == 0) {// 出せる役があるかどうかの探索

			return;
		}
		if (boundSuit != Suits.EMPTY_SUITS) {// 縛りが存在している時

			melds = melds.extract(Melds.suitsContain(boundSuit));// 縛りの絵柄が含まれているものを抽出する

			if (melds == null) {// もし縛りに対応している手札がない時
				return;
			}
		}

		if (meld.type() == Meld.Type.SINGLE) {// 単体出しの時

			Meld myMeld = null;// 自分の取り出した役を一時的に保存

			Melds resultMelds = Melds.parseMelds(hand);// 判定の時に使う自分の手札

			int size = melds.size();

			boolean flag = false;

			for (int i = 0; i < size; i++) {

				myMeld = melds.get(i);

				if (myMeld.rank() == Rank.JOKER_HIGHEST
						|| myMeld.rank() == Rank.JOKER_LOWEST) {// JOKERの時の処理
					flag = true;
				}
				if (!reverse) {// 普通の時
					if (flag
							|| myMeld.rank() == Rank.EIGHT
							|| myMeld.rank().toInt() >= strongestCard.rank()
									.toInt()) {// 8切りまたは最強カードが存在する時

						hand = hand.remove(myMeld.asCards());

						if (oneTurnKill(resultMelds, hand, this.reverse)) {
							oneTurnKill = true;
							finshMelds.add(0, myMeld);
							break;
						}
						hand = hand.add(myMeld.asCards());
					}
				} else {// 革命時の時
					if (flag
							|| myMeld.rank() == Rank.EIGHT
							|| myMeld.rank().toInt() <= strongestCard.rank()
									.toInt()) {// 8切りまたは最強カードが存在する時

						hand = hand.remove(myMeld.asCards());

						if (oneTurnKill(resultMelds, hand, this.reverse)) {
							oneTurnKill = true;
							finshMelds.add(0, myMeld);
							break;
						}

						hand = hand.add(myMeld.asCards());
					}
				}

			}
		} else if (meld.type() == Meld.Type.GROUP) {// ペア出しの時

			Meld myMeld = null;// 自分の取り出した役を一時的に保存

			Melds resultMelds = Melds.parseMelds(hand);// 判定の時に使う自分の手札

			int size = melds.size();

			for (int i = 0; i < size; i++) {

				myMeld = melds.get(i);

				if (myMeld.rank() == Rank.EIGHT || check_Group(myMeld)) {// 8切りまたは最強カードが存在する時

					hand = hand.remove(myMeld.asCards());

					if (oneTurnKill(resultMelds, hand, this.reverse)) {
						oneTurnKill = true;
						finshMelds.add(0, myMeld);
						break;
					}

					hand = hand.add(myMeld.asCards());
				}
			}
		} else {// 階段

			Meld myMeld = null;// 自分の取り出した役を一時的に保存

			Melds resultMelds = Melds.parseMelds(hand);// 判定の時に使う自分の手札

			int size = melds.size();

			for (int i = 0; i < size; i++) {

				myMeld = melds.get(i);

				if (myMeld.rank() == Rank.EIGHT || check_Sequence(myMeld)) {// 8切りまたは最強カードが存在する時

					hand = hand.remove(myMeld.asCards());

					if (oneTurnKill(resultMelds, hand, this.reverse)) {
						oneTurnKill = true;
						finshMelds.add(0, myMeld);
						break;
					}

					hand = hand.add(myMeld.asCards());
				}
			}
		}
	}

	/**
	 * 自分の手札を分解して、一枚出しが2枚以上存在するかどうか調べる。 この時に8と一番強い数は無効とする。
	 *
	 * @param cards
	 *            自分の手札
	 * @return 2枚以上あるときtrue
	 */
	public boolean check_Single(Cards cards) {
		// TODO ここの除去する順番とかがおかしい気がする
		boolean result = false;

		cards = cards.remove(Card.JOKER);

		Melds group = Melds.parseGroupMelds(cards);
		Melds sequence = Melds.parseSequenceMelds(cards);

		// グループになり得る数の除去
		int size = group.size();
		for (int i = 0; i < size; i++) {
			for (Card card : group.get(i).asCards()) {
				cards = cards.remove(card);
			}
		}
		size = sequence.size();
		// 階段になり得る数の除去
		for (int i = 0; i < size; i++) {
			for (Card card : sequence.get(i).asCards()) {
				cards = cards.remove(card);
			}
		}
		int counter = 0;
		for (Card card : cards) {
			if (!reverse) {// 革命時ではない時
				if (card.rank().toInt() >= strongestCard.rank().toInt()
						&& card.rank() != Rank.EIGHT) {// 最強のカードより高い時と8じゃない場合
					counter++;
				}
			} else {// 革命時の時
				if (card.rank().toInt() <= strongestCard.rank().toInt()
						&& card.rank() != Rank.EIGHT) {// 最強のカードより高い時と8じゃない場合
					counter++;
				}
			}
			if (joker) {// ジョーカーが存在する時
				if (counter >= 3) {
					result = true;
					break;
				}
			} else {
				if (counter >= 2) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * ペア出しの時にそのペアを出した時に場が流れるかどうか
	 *
	 * @parm meld
	 * @return
	 */
	public boolean check_Group(Meld meld) {
		// TODO jokerの時は考慮しない　
		boolean result = true;
		int size = meld.asCards().size();
		int rank = meld.rank().toInt() - 2;
		int counter = 0;
		if (!reverse) {// 普通の時
			for (int i = 13; i > 1; i--) {
				counter = 0;
				if (i <= rank) {
					break;
				}
				for (int j = 0; j < 4; j++) {
					if (!lookCard[j][i]) {
						counter++;
					}
					if (counter >= size) {
						result = false;
						break;
					}
				}
				if (!result)
					break;
			}
			return result;

		} else {// 革命が起きている時
			for (int i = 1; i < 14; i++) {
				counter = 0;
				if (i >= rank) {
					break;
				}
				for (int j = 0; j < 4; j++) {
					if (!lookCard[j][i]) {
						counter++;
					}
					if (counter >= size) {
						result = false;
						break;
					}
				}
				if (!result)
					break;
			}
			return result;
		}
	}

	/**
	 * 階段出しの時にそのペアを出した時に場が流れるかどうか
	 *
	 * @parm meld
	 * @return
	 */
	public boolean check_Sequence(Meld meld) {
		// TODO jokerの時は考慮しない　
		boolean result = false;
		boolean notLookJoker = !lookCard[0][0];
		int size = meld.asCards().size();
		int rank = meld.rank().toInt() - 2;
		int mark = Changer.changeIntMark(meld.asCards().get(0));
		int counter = 0;
		if (!reverse) {
			for (int i = 14 - size; i > 0; i--) {
				if (i <= rank)
					break;
				counter = 0;
				if (!lookCard[0][0]) {// notLookJokerの初期化
					notLookJoker = true;
				}
				for (int j = 0; j < size; j++) {
					if (lookCard[mark][i + j]) {
						counter++;
					}
					if (notLookJoker) {// Jokerの時の処理
						counter++;
						notLookJoker = false;
					}
				}

				if (counter >= size) {
					result = true;
					break;
				}
			}
		} else {
			for (int i = 1; i < 14 - size; i++) {
				if (i >= rank)
					break;
				counter = 0;
				if (!lookCard[0][0]) {// notLookJokerの初期化
					notLookJoker = true;
				}
				for (int j = 0; j < size; j++) {
					if (lookCard[mark][i + j]) {
						counter++;
					}
					if (notLookJoker) {// Jokerの時の処理
						counter++;
						notLookJoker = false;
					}
				}

				if (counter >= size) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * 1ターンで勝てる時の判定 ある程度条件を緩和しており、3枚出しは通ること前提で動いている。
	 *
	 * @parm myMelds 役を生成する時に使う
	 * @parm myHand 自分の手札
	 * @parm rev 革命している時がtrue
	 * @return 1ターンで勝てる状況ならtrue
	 */
	public boolean oneTurnKill(Melds myMelds, Cards myHand, boolean rev) {

		if (myHand.isEmpty()) {// 全ての手札を使い切ったなら
			return true;
		}

		Card strongCard = searchStrongCard(rev);

		Cards copyHand = myHand;

		boolean result = false;

		int size = 0;
		int num = 0;

		Rank rank;
		Card card;
		Cards cards;

		myMelds = Melds.parseMelds(myHand);// 自分の役を格納

		if (myHand.contains(Card.JOKER)) {// jokerを含んでいたら

			Melds melds = Melds.EMPTY_MELDS;
			Melds divideMelds = Melds.EMPTY_MELDS;

			melds = melds.add(myMelds.extract(Melds.typeOf(Meld.Type.GROUP)));
			melds = melds
					.add(myMelds.extract(Melds.typeOf(Meld.Type.SEQUENCE)));
			if (!reverse) {
				myMelds = myMelds.extract(Melds.rankOf(Rank.JOKER_HIGHEST));
			} else {
				myMelds = myMelds.extract(Melds.rankOf(Rank.JOKER_LOWEST));
			}
			myMelds = myMelds.add(melds);

			melds = Melds.EMPTY_MELDS;
			// 単体出しのjokerの分け方
			divideMelds = myMelds.extract(Melds.typeOf(Meld.Type.SINGLE));
			melds = melds.add(divideMelds.get(0));
			// 2枚出しのjokerの分け方
			divideMelds = myMelds.extract(Melds.typeOf(Meld.Type.GROUP));
			size = divideMelds.size();
			for (int i = 0; i < size; i += 4) {
				melds = melds.add(divideMelds.get(i));
			}
			// 階段出しの抽出
			divideMelds = myMelds.extract(Melds.typeOf(Meld.Type.SEQUENCE));
			melds = melds.add(divideMelds);

			myMelds = melds;
		}
		myMelds = MeldSort.meldsSizeSort(myMelds);

		for (Meld meld : myMelds) {
			cards = meld.asCards();

			copyHand = myHand.remove(cards);// 自分のカードからそのカードを抜いた時のカード群

			if (copyHand.size() == 0) {// もしカードの数が使った時に0になる場合

				finshMelds.add(0, meld);

				return true;
			}

			size = cards.size();
			// 革命の起こる条件の判定

			if (size >= 5) {
				rev = !rev;
			} else if (size >= 4 && meld.type() == Meld.Type.GROUP) {
				rev = !rev;
			}
			// ↓ここから読みきりの判定

			if (size >= 3) {// 3枚出し以上なら
				// TODO ここの精度を上げる
				myHand = myHand.remove(cards);

				result = oneTurnKill(myMelds, myHand, rev);

				myHand = myHand.add(cards);

				if (result) {
					finshMelds.add(0, meld);
					break;
				}

			} else {
				rank = searchRank_double(rev);
				if (size == 2) {// 2枚出しの判定
					num = cards.get(0).rank().toInt();
					if (!rev) {// 普通の時
						if (num == 8 || num >= rank.toInt()) {// 8の数字または最強カードの判定
							myHand = myHand.remove(cards);

							result = oneTurnKill(myMelds, myHand, rev);

							myHand = myHand.add(cards);
						}
					} else {// 革命時の処理
						if (num == 8 || num <= rank.toInt()) {// 8の数字または最強カードの判定
							myHand = myHand.remove(cards);

							result = oneTurnKill(myMelds, myHand, rev);

							myHand = myHand.add(cards);
						}
					}
				} else {// 1枚出しの判定
					card = cards.get(0);
					if (card == Card.JOKER) {// jokerの時
						if (look_Spade_3) {
							myHand = myHand.remove(cards);

							result = oneTurnKill(myMelds, myHand, rev);

							myHand = myHand.add(cards);
						}
					} else {// それ以外の時
						num = card.rank().toInt();
						if (!rev) {// 普通の時
							if (card == Card.JOKER || num == 8
									|| num >= strongCard.rank().toInt()) {// 8の数字または最強カードの判定またはJOKERの時

								myHand = myHand.remove(cards);

								result = oneTurnKill(myMelds, myHand, rev);

								myHand = myHand.add(cards);
							}
						} else {// 革命の時
							if (card == Card.JOKER || num == 8
									|| num <= strongCard.rank().toInt()) {// 8の数字または最強カードの判定またはJOKERの時
								myHand = myHand.remove(cards);

								result = oneTurnKill(myMelds, myHand, rev);

								myHand = myHand.add(cards);
							}
						}
					}
				}
				if (result) {
					finshMelds.add(0, meld);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * notLookCardから同じカードを除去するメソッド
	 *
	 * @param card
	 *            　除去するカード
	 */
	public void removeCard(Card card) {
		if (card == null)
			return;
		if (card.equals(Card.JOKER)) {// JOKERの処理
			notLookCard.remove(notLookCard.get(0));// JOKERの処理
			return;
		}
		int size = notLookCard.size();
		for (int i = 0; i < size; i++) {
			if (notLookCard.get(i).equals(Card.JOKER))
				continue;
			if (notLookCard.get(i).rank().toInt() == card.rank().toInt()) {// ランクの比較
				if (notLookCard.get(i).suit().toString()
						.equals(card.suit().toString())) {// 役が等しい時
					notLookCard = notLookCard.remove(notLookCard.get(i));// 自身のi番目のカードを抜く
					break;
				}
			}
		}
	}

	/**
	 * 2枚出しをする時に必ず通るかどうかの判定を行う
	 *
	 * @param rank
	 *            　そのカードのランク
	 * @return trueの時必ず通る
	 */
	public boolean check_double_path(int rank) {
		boolean result = true;
		int counter = 0;
		int max = 2;
		if (!lookCard[0][0])// jokerの時
			max = 1;
		if (!reverse) {// 普通の時
			for (int i = rank; i < 14; i++) {
				counter = 0;
				for (int j = 0; j < 4; j++) {
					if (lookCard[j][i]) {
						counter++;
					}
					if (counter >= max) {
						result = false;
						break;
					}
				}
				if (!result)
					break;
			}
		} else {// 革命時の時
			for (int i = 13; i > rank; i--) {
				counter = 0;
				for (int j = 0; j < 4; j++) {
					if (lookCard[j][i]) {
						counter++;
					}
					if (counter >= max) {
						result = false;
						break;
					}
				}
				if (!result)
					break;
			}
		}
		return result;
	}

	/*
	 * 以下getter　or setter
	 */
	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public boolean isRenew() {
		return renew;
	}

	public void setRenew(boolean renew) {
		this.renew = renew;
	}

	public void setBeforeMelt(Meld beforeMelt) {
		this.beforeMelt = beforeMelt;
	}

	public Cards getRemoveJokerMyhand() {
		return removeJokerMyhand;
	}

	public void setRemoveJokerMyhand(Cards myHands) {
		this.removeJokerMyhand = myHands.remove(Card.JOKER);
	}

	public int getTurn() {
		return turn;
	}

	public boolean isOneTurnKill() {
		return oneTurnKill;
	}

	public void setOneTurnKill(boolean oneTurnKill) {
		this.oneTurnKill = oneTurnKill;
	}

	public Situation getSituation() {
		return situation;
	}

	public void setSituation(Situation situation) {
		this.situation = situation;
	}

	public Hand getMyHand() {
		return myHand;
	}

	public boolean isHave_Spade_3() {
		return have_Spade_3;
	}

	public Melds getSingleMelds() {
		return singleMelds;
	}

	public Melds getWeekSingleMelds() {
		return weekSingleMelds;
	}

	public Melds getStrongSingleMelds() {
		return strongSingleMelds;
	}

	public boolean isExistEight() {
		return existEight;
	}

	public int getTwoAndOver() {
		return twoAndOver;
	}

	public int getThreeAndOver() {
		return threeAndOver;
	}

	public void setTwoAndOver(int twoAndOver) {
		this.twoAndOver = twoAndOver;
	}

	public boolean isJoker() {
		return joker;
	}

	public double getMyHandAverage() {
		return myHandAverage;
	}

	public double getNotLookAverage() {
		return notLookAverage;
	}

	public boolean isCanReverse() {
		return canReverse;
	}

	public int getMyRank() {
		return myRank;
	}

	public void setMyRank(int myRank) {
		this.myRank = myRank;
	}

	public boolean isDoReverse() {
		return doReverse;
	}

	public Card getStrongestCard() {
		return strongestCard;
	}

	public Melds getReverseMelds() {
		return reverseMelds;
	}

	public boolean isLapPair() {
		return lapPair;
	}
	public void setTurn(int turn) {
		this.turn = turn;
	}
	public boolean[][] getLookCard() {
		return lookCard.clone();
	}

}
