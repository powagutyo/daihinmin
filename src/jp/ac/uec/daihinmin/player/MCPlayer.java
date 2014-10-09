package jp.ac.uec.daihinmin.player;

import static jp.ac.uec.daihinmin.card.MeldFactory.*;
import jp.ac.uec.daihinmin.Rules;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import monteCalro.FieldData;
import monteCalro.MonteCalro_01;
import monteCalro.MyData;
import object.InitSetting;

public class MCPlayer extends BotSkeleton {
	private final int players = 5;
	/** MyDataクラス **/
	private MyData myData = null;
	/** FiledDataクラス **/
	private FieldData fieldData = null;
	/** 場が流れたかどうか **/
	private boolean renew;
	/** 場に出された自分のカード **/
	private Meld beforeMeld;
	/** プレイヤーの順位 **/
	private int[] grade = new int[5];
	/** 勝利した順に入れる **/
	private int winCounter = 1;

	private final boolean modeC = InitSetting.MODE_C;

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
		fieldData.init();// フィールドデータを初期化する

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
		System.out.println();
	}

	/**
	 * 場が流れた時に実行されるメソッドです
	 */
	@Override
	public void placeRenewed() {
		super.placeRenewed();

		fieldData.initPlayerInformation();// PASSしたプレイヤーの初期化
		fieldData.setPutLastPlayer(-1);// 最後に出した人の座席番号を初期化
	}

	@Override
	/**
	 * あるプレイヤーが勝ち抜けたとき実行されるメソッドです.
	 */
	public void playerWon(java.lang.Integer number) {
		/**
		 * ちゃんと5回通っている
		 */
		super.playerWon(number);

		grade[number] = winCounter;// プレイヤーにランクを挿入

		winCounter++;// ランク1つ下げる

		fieldData.setWonPlayer(playersInformation().getSeatOfPlayer(number));// 勝ったプレイヤーを記憶させてあげる
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
		if (myData == null) {
			init();
		}
		if (this.number() == number) {// 自分の手番の時
			if (playedMeld != null && playedMeld != PASS) {
				// 役と自分の手番が1回過ぎたことを記憶させる
				beforeMeld = playedMeld;
				renew = true;
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
				;
			}
		}
		System.out.println();
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
			if(!modeC){
				fieldData.compensateHandSize();// 手札の枚数を補正してあげる
			}

		}
	}

	/**
	 * 場に出すカードを決めるメソッド
	 *
	 * @return 場に出す役
	 */
	@Override
	public Meld requestingPlay() {
		try {
			if (myData == null) {
				init();
			}
			/* 前回自分の出した手の場合の処理 */
			if (renew) {// renewの状態
				if (beforeMeld == this.place().lastMeld()) {
					renew = false;// renewの初期化
					beforeMeld = null;// beforeMeldの初期化
					return PASS;
				}
			}

			/* 前回自分の出した手の場合の処理まで */

			// TODO 原始モンテカルロの実装

			renew = false;// renewの初期化
			beforeMeld = null;// beforeMeldの初期化
			/*
			 * Melds melds = Melds.parseSingleMelds(this.hand()); melds =
			 * melds.extract(Melds.MAX_RANK);
			 *
			 * return melds.get(0);
			 */

			return MonteCalro_01.MonteCalroPlay(this, myData, fieldData);
		} catch (Exception e) {
			return PASS;

		}
	}

	/**
	 * 初期化用のメソッド
	 */
	public void init() {
		myData = new MyData(this);// MyDataの初期化
		renew = false;
		beforeMeld = null;
		for (int i = 0; i < players; i++) {
			grade[i] = 0;
		}
	}

}
