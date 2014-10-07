package jp.ac.uec.daihinmin.player;
import object.*;
import tactics.*;
import utility.Changer;
import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

public final class CopyOfMain extends BotSkeleton {
	// TODO ダイヤの3を持っている時の判定を行っていない

	/** 自分の状態を表す */
	private MyState myState;
	
	private boolean won[] = new boolean[5];
	/** 通常行う戦略 */
	private Tactics[] tactics = { new T_Sp3(), new T_Single(),
			new T_Single_Reverse(), new T_Group(), new T_Group_reverse(),
			new T_Sequence(), new T_Sequence_Reverse(), new T_Pass() };
	/** 新しく自分の番が来た時の戦略 */
	private Tactics[] tactics_renew = { new T_Renew(), new T_Renew_Reverse() };

	/**
	 * カード交換する時に呼ばれるメソッド
	 * 
	 * @return 渡すカード群
	 */
	@Override
	public Cards requestingGivingCards() {
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
			} else {
				/* ここは呼ばれないはず */
				int handSize = sortHand.size() - 1;// 自分の手にあるカードの枚数 -1(list版)
				for (int i = 0; i < sendCardNum; i++) {// 強いほうから返す
					sendCard = sendCard.add(sortHand.get(handSize - i));
				}
			}
			return sendCard;

		} catch (Exception e) {// エラー発生時に大富豪、富豪なら一番弱いカードを大貧民、貧民なら一番強いカードを返すようにする

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
			} else {// 平民、貧民、大貧民の場合
				int handSize = sortHand.size() - 1;// 自分の手にあるカードの枚数 -1(list版)
				for (int i = 0; i < sendCardNum; i++) {// 強いほうから返す
					sendCard = sendCard.add(sortHand.get(handSize - i));
				}
			}
			return sendCard;
		}
	}

	/**
	 * 自分が場に出したカードが, サーバーによりリジェクトされたとき実行されるメソッドです.
	 * 
	 * @param number
	 *            プレイヤー番号?
	 * @param playMeld
	 *            リジェクトされた役
	 */
	@Override
	public void playRejected(java.lang.Integer number, Meld playedMeld) {
		super.playRejected(number, playedMeld);
		// TODO 実際に完成した時にこのデバック用部分をコメントアウトする
		// デバック用
		try {
			if (this.number() == number) {// 自分の手がリジェクトされた時
				System.out.println("この手はリジェクトされました :" + playedMeld.asCards());
			}
		} catch (Exception e) {
			System.out.println("エラー発生：playRejectedメソッド");
		}
	}

	/**
	 * 各ゲーム開始時に実行されるメソッドです
	 */
	@Override
	public void gameStarted() {
		super.gameStarted();
		try {
		} catch (Exception e) {
			System.out.println("gameStartメソッドでエラー発生");
		}
	}

	/**
	 * ゲーム開始直後, 自分の手札が配られたときに実行されるメソッドです.
	 */
	@Override
	public void dealed(Cards hand) {
		super.dealed(hand);
		init();
		myState.removeMyHand(this.hand());

	}

	/**
	 * ゲーム終了時に呼び出される
	 */
	@Override
	public void gameEnded() {
		super.gameEnded();
		try {
			myState = null;
		} catch (Exception e) {
			System.out.println("エラー発生　gemeEndedメソッド");
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
		try {
			if (myState == null) {
				init();
			}
			myState.look_Card(number, playedMeld, this);// 捨てたカードを記憶させる
			myState.searchStrongCard(true);// 出ていないカードで一番強いカードを検索
			if (myState.isRenew()) {
				myState.setRenew(false);// 場が流れた状態を元に戻す
			}
			/* 自分の手だけで終わると思っていたのに失敗した時 */
			if (myState.isOneTurnKill()) {
				if (playedMeld != PASS && this.number() != number) {// もし違う人が手をだした時
					myState.setOneTurnKill(false);
				}
			}

			myState.plusTurn(this);// ターン数を増やす
		} catch (Exception e) {
			System.out.println("エラー発生 : playedメソッド");
		}
	}

	/**
	 * 自分が場に出したカードが, 受理されたとき実行されるメソッドです.
	 * 
	 * @parm number - このプレイヤーの番号
	 * @parm playedMeld - 受理された役
	 */
	@Override
	public void playAccepted(java.lang.Integer number, Meld playedMeld) {
		super.playAccepted(number, playedMeld);
		/*
		 * ここで前回出したカードの役集合meltを記憶させる
		 */
		try {
			if (this.number() == number) {// 自分のターンの時
				if (playedMeld == PASS) {
					myState.setBeforeMelt(null);
				} else {
					myState.setBeforeMelt(playedMeld);
				}
			}
			// TODO　8の判定を表示する
		} catch (Exception e) {
			System.out.println("エラー発生 : playAcceptedメソッド");
		}
	}

	/**
	 * 場が流れたとき実行されるメソッドです
	 */
	@Override
	public void placeRenewed() {
		super.placeRenewed();
		try {
			myState.setRenew(true);// 場が流れた時の更新
			myState.setBeforeMelt(null);// 前回出したカードを初期化する
		} catch (Exception e) {
			System.out.println("エラー発生　: placeRenewedメソッド");
		}
	}

	/**
	 * 場に出すカードを決めるメソッド
	 * 
	 * @return 場に出す役
	 */
	@Override
	public Meld requestingPlay() {
		if (myState == null) {
			init();
		}
		// TODO 以下のコメントアウト
		/*
		 * 下の部分にそのままAIを書いているが、このままだと絶対に複雑なメソッドになるので
		 * AI用のクラスを設けてそれにStateクラスと場に出されたMeld、placeなど(変更される恐れあり)
		 * を送って自分の打つ手を決めるようにさせる
		 */
		System.out.println("状態" + myState.getSituation() + "ターン数"
				+ myState.getTurn());

		myState.setRemoveJokerMyhand(this.hand());

		if (this.place().isReverse() != myState.isReverse()) {// 革命が起きている時
			myState.setReverse(this.place().isReverse());// stateに革命を更新させる
		}
		try{
			return useEvaluation();			
		}catch(Exception e){
			return PASS;
		}
	}

	/**
	 * いずれかのプレイヤが勝利した時に呼び出される
	 * 
	 * @param number
	 *            プレイヤ番号
	 */
	@Override
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);
		try {
			// 場の状況オブジェクトのアップデート
			won[number] = true;
		} catch (Exception e) {
			
		}
	}

	/**
	 * 戦術クラスを用いるメソッド
	 * 
	 * @return
	 */
	public Meld useTactics() {
		try{
			int size = 0;

			Meld lastMeld = place().lastMeld();// 最後にフィールドに出た役

			myState.calcWeekSingleMelds(this.hand());// 自分の手札から単体手を抜き出す

			Meld finalMeld = null;// 最終的に決まった役

			Melds melds = Melds.parseMelds(this.hand());// 自分の手札を全て分解する

			if (myState.check_myMelt(lastMeld)) {// 自分の出した役で全員がパスをした時

				finalMeld = PASS;

			} else {

				if (myState.isRenew()) {// 自分のターンから始まる時

					size = tactics_renew.length;

					for (int i = 0; i < size; i++) {
						if (tactics_renew[i].doAction(myState, this)) {
							finalMeld = tactics_renew[i].discardMeld(melds,
									myState, this);// 役の探索
						}
						if (finalMeld != null)// もし出す役が決定した場合
							break;
					}

				} else {// 相手ターンから自分のターンになった時
					if (!myState.isOneTurnKill()) {// 自分の手番だけで終わる時
						myState.checkGoal(this, lastMeld);
					}

					if (myState.isOneTurnKill()){// 自分の手版だけで終わるとき
						System.out.println("勝ち確");
						return myState.finshMelds.remove(0);
					}
					/*
					 * 全ての役を調べてそれに対応するものを打たせる
					 */
					Suits boundSuit = this.place().lockedSuits();// 縛りの絵柄を抽出

					if (boundSuit != Suits.EMPTY_SUITS) {// 縛りが存在している時
						melds = melds.extract(Melds.suitsContain(boundSuit));// 縛りの絵柄が含まれているものを抽出する
					}

					if (melds.size() == 0) {// 縛りの条件でカードが出せない場合
						finalMeld = PASS;
					} else {
						size = tactics.length;

						for (int i = 0; i < size; i++) {
							if (tactics[i].doAction(myState, this)) {
								finalMeld = tactics[i].discardMeld(melds, myState,
										this);
							}
							if (finalMeld != null)// もし出す役が決定した時
								break;
						}
					}
				}
			}

			// もしfinalMeldが何も入っていない場合
			if (finalMeld == null) {
				// ランダムで適当な役をサーバー側に送る（リジェクトされる可能性が大!!）
				finalMeld = PASS;
			}
			return finalMeld;	
		}catch(Exception e){
			System.out.println("エラー発生");
			return PASS;
		}
		
	}

	/**
	 * 評価値で自分の出す手を生成する
	 * 今は使っていない
	 * @return 自分の役を返す
	 */
	public Meld useEvaluation() {

		myState.initHand(this.hand());// 自分の手札を生成する

		int num = 0;

		int pos = 0;// 最終的に出す役

		int counter = 0;

		Meld lastMeld = place().lastMeld();// 最後にフィールドに出た役

		Meld finalMeld = null;

		int evaluation = -100;// 評価値、最初はありえない数

		Melds melds = Melds.parseMelds(this.hand());// 自分の手札を全て分解する

		if (myState.check_myMelt(lastMeld)) {// 自分の出した役で全員がパスをした時

			finalMeld = PASS;

		} else {

			if (myState.isRenew()) {// 自分のターンから始まる時
				// TODO renewの時の打ち方を考えたほうが良い

				for (Meld meld : melds) {
					num = myState.getMyHand().returnEvalution_renew(meld);
					if (evaluation <= num) {
						evaluation = num;
						pos = counter;
					}
					counter++;
				}
				finalMeld = melds.get(pos);

			} else {// 相手ターンから自分のターンになった時
				if (place().rank() == Rank.JOKER_HIGHEST
						|| place().rank() == Rank.JOKER_LOWEST) {// スペ3の処理
					if (hand().contains(Card.S3)) {
						melds = melds.extract(Melds.rankOf(Rank.THREE));
						melds = melds.extract(Melds.sizeOf(1));
						for (Meld meld : melds) {
							if (!meld.suits().contains(Suit.SPADES))
								continue;
							return meld;
						}
					}
				}
				/*
				 * 全ての役を調べてそれに対応するものを打たせる
				 */
				Suits boundSuit = this.place().lockedSuits();// 縛りの絵柄を抽出

				if (boundSuit != Suits.EMPTY_SUITS) {// 縛りが存在している時
					melds = melds.extract(Melds.suitsContain(boundSuit));// 縛りの絵柄が含まれているものを抽出する
					// TODO マークによって自分の出すカードの評価値を変更する
				}

				if (melds.size() == 0) {// 縛りの条件でカードが出せない場合
					finalMeld = PASS;
				} else {
					// 場の縛りを見て、縛れるカードの評価値を上げる
					for (Suit suit : place().suits()) {// マークの分解を行っている
						myState.getMyHand().calcEvalution_Mark(
								Changer.changeIntMark(suit));
					}
					// 場に出せる役にする
					melds = melds.extract(Melds.typeOf(place().type()));
					melds = melds.extract(Melds.rankOver(place().rank()));
					melds = melds.extract(Melds.sizeOf(place().size()));

					if (melds == Melds.EMPTY_MELDS) {
						finalMeld = PASS;
					} else {
						for (Meld meld : melds) {
							num = myState.getMyHand().returnEvalution(meld);
							if (evaluation < num) {
								evaluation = num;
								pos = counter;
							}
							counter++;
						}
						if (myState.getSituation() == Situation.Opening) {
							num = 25;
						} else if (myState.getSituation() == Situation.Middle) {
							num = 20;
						} else {
							num = 0;
						}
						if (evaluation >= num) {
							finalMeld = melds.get(pos);

						} else {
							finalMeld = PASS;
						}

					}
				}
			}
			System.out.println("評価値は" + evaluation);
		}

		// もしfinalMeldが何も入っていない場合
		if (finalMeld == null) {
			// ランダムで適当な役をサーバー側に送る（リジェクトされる可能性が大!!）
			finalMeld = PASS;
		}

		return finalMeld;
	}

	/**
	 * ゲームの状態の初期設定
	 */
	public void init() {
		for (int i = 0; i < 5; i++) {
			won[i] = false;
		}
		myState = new MyState(this.number());

		myState.setSituation(Situation.Opening);// ゲーム開始したため序盤にする。
		PlayersInformation info = playersInformation();
		int[] cards = info.numCardsOfPlayers();

		myState.numberOfPLayersCards = new int[5];
		for (int i = 0; i < 5; i++) {
			myState.numberOfPLayersCards[i] = cards[i];
		}

	}
}
