package jp.ac.uec.daihinmin.player;

import static jp.ac.uec.daihinmin.card.MeldFactory.*;

import java.util.ArrayList;

import jp.ac.uec.daihinmin.Rules;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import monteCalro.FieldData;
import monteCalro.MonteCalro_02;
import monteCalro.MyData;
import monteCalro.Utility;

public class UCT extends BotSkeleton {
	private final int players = 5;
	/** MyDataクラス **/
	private MyData myData = null;
	/** FiledDataクラス **/
	private FieldData fieldData = null;
	/** 場が流れすかどうか **/
	private boolean doRenew;
	/** 場が流れたかどうか **/
	private boolean renew;
	/** 場に出された自分のカード **/
	private Meld beforeMeld;
	/** プレイヤーの順位 **/
	private int[] grade = new int[5];
	/** 勝利した順に入れる **/
	private int winCounter = 1;
	/** カード交換を行った場合は true **/
	private boolean exchangeCard = false;

	/*** 自分が相手に渡したカード群 **/
	private Cards handOverCard;
	/*** カード交換したプレイヤー番号 ***/
	private int exchangedPlayer;
	/** 最初のゲームかどうか **/
	private boolean firstGame = true;
	/** 実験用の重み **/
	private int[] weight = new int[275];
	/** C言語の時のみ適応するMODE **/
	private final boolean modeC = false;

	private int turn = 0;
	/** 全員の手札群 **/
	ArrayList<ArrayList<Card>> playersCard = new ArrayList<ArrayList<Card>>(
			players);

	@Override
	/**
	 * カード交換する時に呼ばれるメソッド
	 *
	 * @return 渡すカード群
	 */
	public Cards requestingGivingCards() {

		// Mainの流用
		try {
			// 相手に渡す手札の集合
			Cards sendCard = Cards.EMPTY_CARDS;
			/** 手牌昇順ソート 同じ数の時は 1. スペード2. ハート3. ダイヤ4. クラブ */
			Cards sortHand = Cards.sort(this.hand());
			// 相手に渡すカードの枚数
			int sendCardNum = Rules.sizeGivenCards(this.rules(), this.rank());
			/** myRankは大富豪から2 1 0 -1 -2となる */
			int myRank = Rules.heiminRank(this.rules()) - this.rank();

			if (myRank > 0) {// 富豪か大富豪の時
				// TODO 富豪や大富豪の送るカードを考える
				sortHand = sortHand.remove(Card.JOKER);// JOKERを抜く

				Cards notPairHand = sortHand;// 自分の手札をコピーする

				Melds squenceMelds = Melds.parseSequenceMelds(notPairHand);// 階段の切り出し

				Melds groupMelds = Melds.parseGroupMelds(notPairHand);// ペアの切り出し
				/* 階段手の削除 */
				for (Meld meld : squenceMelds) {
					for (Card card : meld.asCards()) {
						notPairHand = notPairHand.remove(card);
					}
				}
				/* ペア手の削除 */
				for (Meld meld : groupMelds) {
					for (Card card : meld.asCards()) {
						notPairHand = notPairHand.remove(card);
					}
				}
				notPairHand = notPairHand.remove(Card.D3);// ダイヤの3を抜く

				notPairHand = notPairHand.extract(Cards.rankOver(Rank.EIGHT));// 7以下の数字かどうかを調べる

				if (notPairHand.size() >= sendCardNum) {// 複数枚存在する時
					for (int i = 0; i < sendCardNum; i++) {// 弱い方から返す
						sendCard = sendCard.add(notPairHand.get(i));
					}
				} else {
					/* 7以下の単体出しカードであげられる枚数分のカードよりも少ない場合 */
					notPairHand = sortHand;

					Melds notTwoGroupMelds = groupMelds.extract(Melds
							.sizeOver(2));// サイズが2以上の時

					/* 階段手の削除 */
					for (Meld meld : squenceMelds) {
						for (Card card : meld.asCards()) {
							notPairHand = notPairHand.remove(card);
						}
					}
					/* 3枚以上のペア手の削除 */
					for (Meld meld : notTwoGroupMelds) {
						for (Card card : meld.asCards()) {
							notPairHand = notPairHand.remove(card);
						}
					}
					notPairHand = notPairHand.remove(Card.D3);// ダイヤの3を抜く

					if (notPairHand.size() >= sendCardNum) {// 複数枚存在する時
						for (int i = 0; i < sendCardNum; i++) {// 弱い方から返す
							sendCard = sendCard.add(notPairHand.get(i));
						}
					} else {
						sendCard = sendCard.remove(Card.D3);

						for (int i = 0; i < sendCardNum; i++) {// 弱い方から返す
							sendCard = sendCard.add(sortHand.get(i));
						}
					}
				}
			}
			return sendCard;

		} catch (Exception e) {// エラー発生時に大富豪、富豪なら一番弱いカードを返すようにする

			// 相手に渡す手札の集合
			Cards sendCard = Cards.EMPTY_CARDS;
			/* 手牌昇順ソート 同じ数の時は 1. スペード2. ハート3. ダイヤ4. クラブ */
			Cards sortHand = Cards.sort(this.hand());
			// 相手に渡すカードの枚数
			int sendCardNum = Rules.sizeGivenCards(this.rules(), this.rank());

			int myRank = Rules.heiminRank(this.rules()) - this.rank();
			if (myRank > 0) {// 大富豪、富豪の時
				sortHand = sortHand.remove(Card.JOKER);// JOKERを抜く
				for (int i = 0; i < sendCardNum; i++) {// 弱い方から返す
					sendCard = sendCard.add(sortHand.get(i));
				}
			}
			return sendCard;
		}
	}

	/**
	 * 各ゲーム開始時に実行されるメソッドです
	 */
	@Override
	public void gameStarted() {
		super.gameStarted();

		turn = 0;
		if (fieldData == null) { // 初回だけは生成する
			fieldData = new FieldData();

		} else {
			for (int i = 0; i < players; i++) {
				fieldData.setGrade(playersInformation().getSeatOfPlayer(i),
						grade[i]);
			}
		}
		for (int i = 0; i < players; i++) {
			grade[i] = 0;
		}
		renew = true;

		handOverCard = null; // 自分が渡したカード群を初期化

		fieldData.init();// フィールドデータを初期化する

		if (modeC) {
			// gradeの補正式
			boolean flag = false;
			for (int i = 0; i < players; i++) {
				if (grade[i] == 0) {
					flag = true;
					break;
				}
			}
			if (flag) {
				for (int i = 0; i < players; i++) {
					if (this.playersInformation().numCardsOfPlayers()[i] >= 1) {
						grade[i] = 5;
						break;
					}
				}
				for (int i = 0; i < players; i++) {
					if (grade[i] == 0) {
						grade[i] = 4;
					}
				}

			}
		}

		if (firstGame) {
			for (int i = 0; i < players; i++) {
				playersCard.add(new ArrayList<Card>());
			}
			for (int i = 0; i < weight.length; i++) {
				weight[i] = 0;
			}
		} else {
			for (int i = 0; i < players; i++) {
				playersCard.get(i).clear();
				;
			}
		}

	}

	/**
	 * ゲーム終了時に呼び出される
	 */
	@Override
	public void gameEnded() {
		super.gameEnded();
		try {
			myData = null;

			winCounter = 1;

		} catch (Exception e) {
			System.out.println("エラー発生　gemeEndedメソッド");
		}

		// examineWeight();

		firstGame = false;

	}

	/**
	 * 初期手札の重みを調べるためのメソッド
	 */
	private void examineWeight() {
		if (!firstGame) {// 最初のゲームではない時
			int size = 0;
			int num = 0;
			int plus = 0;
			Melds pMelds = Melds.EMPTY_MELDS;// pairの役
			Melds sMelds = Melds.EMPTY_MELDS;// 階段の役
			Cards cards = Cards.EMPTY_CARDS;
			for (int i = 0; i < players; i++) {
				for (Card card : playersCard.get(i)) {
					num = i * 55 + Utility.cardParseInt(card);
					weight[num]++;
				}
				for (int j = 0; j < playersCard.get(i).size(); j++) {
					cards = cards.add(playersCard.get(i).get(j));
				}
				cards = cards.remove(Card.JOKER);

				pMelds = Melds.parseGroupMelds(cards);
				sMelds = Melds.parseSequenceMelds(cards);
				num = (i + 1) * 55 - 2;
				plus = pMelds.size();
				weight[num] += plus;
				num = (i + 1) * 55 - 1;
				plus = sMelds.size();
				weight[num] += plus;
				cards = Cards.EMPTY_CARDS;
				pMelds = Melds.EMPTY_MELDS;// pairの役
				sMelds = Melds.EMPTY_MELDS;// 階段の役
			}
		}
		System.out.println("重み表");
		System.out.println();
		for (int i = 0; i < weight.length; i++) {
			System.out.print(weight[i]);
			if (weight.length - 1 != i) {
				System.out.print(",");
			}

		}
		System.out.println();

	}

	/**
	 * 場が流れた時に実行されるメソッドです
	 */
	@Override
	public void placeRenewed() {
		super.placeRenewed();
		renew = true;
		fieldData.initPlayerInformation();// PASSしたプレイヤーの初期化
		fieldData.setPutLastPlayer(-1);// 最後に出した人の座席番号を初期化
	}

	@Override
	/**
	 * あるプレイヤーが勝ち抜けたとき実行されるメソッドです.
	 */
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);

		grade[number] = winCounter;// プレイヤーにランクを挿入

		winCounter++;// ランク1つ下げる

		fieldData.setWonPlayer(playersInformation().getSeatOfPlayer(number));// 勝ったプレイヤーを記憶させてあげる
	}

	@Override
	/**
	 * ゲーム開始直後, あるプレイヤーの手札が配られたときに実行されるメソッドです.
	 */
	public void dealed(java.lang.Integer player, java.lang.Integer handSize) {
		super.dealed(player, handSize);

		fieldData.setSeatsHandSIze(
				playersInformation().getSeatOfPlayer(player), handSize); // 座席番号に手札を入れる

		if (player == 4) {// 最後のプレイヤーの時
			/*
			 * なぜかカード交換した時に大富豪と富豪の手札が引かれていないので補正してあげる。
			 */
			if (!modeC)
				fieldData.compensateHandSize();// 手札の枚数を補正してあげる

		}
	}

	@Override
	/**
	 *  自分が場に出したカードが, サーバーによりリジェクトされたとき実行されるメソッドです.
	 */
	public void playRejected(java.lang.Integer number, Meld playedMeld) {
		super.playRejected(number, playedMeld);

		if (number == this.number() && playedMeld != PASS) {
			System.out.println("Rejectされたよ");
		}

	}

	/**
	 * 各プレイヤーがそれぞれの手を出した時に呼び出される
	 *
	 * @parm number プレイヤー番号
	 * @param playedMeld
	 *            プレイヤーが出した番号
	 */
	@Override
	public void played(java.lang.Integer number, Meld playedMeld) {
		super.played(number, playedMeld);

		turn++;
		firstUpdate();// 最初の更新を行う

		if (this.number() == number) {// 自分の手番の時
			if (playedMeld != null && playedMeld != PASS) {
				// 役と自分の手番が1回過ぎたことを記憶させる
				beforeMeld = playedMeld;
				doRenew = true;
			}
		}
		if (playedMeld != null) {
			if (playedMeld != PASS) {
				myData.removeCards(number, playedMeld.asCards()); // カードデータの更新

				fieldData.takeOutHandCards(playersInformation()
						.getSeatOfPlayer(number), playedMeld.asCards().size());// 座席の枚数を減らしてあげる

				fieldData.setPutLastPlayer(playersInformation()
						.getSeatOfPlayer(number));// 最後に役を出したプレイヤーを格納

			} else {
				// 座席番号のプレイヤーの初期化
				fieldData.setPassPlayer(playersInformation().getSeatOfPlayer(
						number));
			}
		}
		renew = false;
	}

	/**
	 * 場に出すカードを決めるメソッド
	 *
	 * @return 場に出す役
	 */
	@Override
	public Meld requestingPlay() {
		try{
		firstUpdate();// 最初の更新を行う

		if (turn > 1000) {
			Melds melds = Melds.parseMelds(this.hand());
			return melds.get((int) (Math.random() * melds.size()));
		}

		/* 前回自分の出した手の場合の処理 */
		if (doRenew) {// renewの状態
			if (beforeMeld == this.place().lastMeld()) {
				doRenew = false;// renewの初期化
				beforeMeld = null;// beforeMeldの初期化
				return PASS;
			}
		}

		/* 前回自分の出した手の場合の処理まで */

		doRenew = false;// renewの初期化
		beforeMeld = null;// beforeMeldの初期化

		Meld meld = MonteCalro_02.MonteCalroPlay(this, myData, fieldData);

		return meld;
		}catch(Exception e){
			Melds melds = Melds.parseMelds(this.hand());
			return melds.get((int) (Math.random() * melds.size()));
		}
	}

	/**
	 * 初期化用のメソッド
	 */
	public void init() {
		myData = new MyData(this);// MyDataの初期化

		doRenew = false;
		beforeMeld = null;
		for (int i = 0; i < players; i++) {
			grade[i] = 0;
		}
	}

	/**
	 * 最初のみ更新するメソッド
	 */
	public void firstUpdate() {

		if (myData == null) {
			init();
		}

	}

}
