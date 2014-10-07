import com.sun.corba.se.impl.protocol.NotLocalLocalCRDImpl;

import object.*;
import tactics.T_Group;
import tactics.T_Group_first;
import tactics.T_Group_reverse;
import tactics.T_Renew;
import tactics.T_Renew_Reverse;
import tactics.T_Renew_first;
import tactics.T_Sequence;
import tactics.T_Sequence_Reverse;
import tactics.T_Sequence_first;
import tactics.T_Single;
import tactics.T_Single_Reverse;
import tactics.Tactics;
import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;

public final class Main extends BotSkeleton {
	// TODO ダイヤの3を持っている時の判定を行っていない

	/** 自分の状態を表す */
	private MyState myState;
	/** 通常行う戦略 */
	private Tactics[] tactics = { new T_Single(), new T_Single_Reverse(),
			new T_Group_first() ,new T_Group(), new T_Group_reverse(),new T_Sequence_first() ,new T_Sequence(),
			new T_Sequence_Reverse() };
	/** 新しく自分の番が来た時の戦略 */
	private Tactics[] tactics_renew = {new T_Renew_first(), new T_Renew(), new T_Renew_Reverse() };

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
				for (int i = 0; i < sendCardNum; i++) {// 弱い方から返す
					sendCard = sendCard.add(sortHand.get(i));
				}
			} else {
				// TODO 　貧民や大貧民の送るカードを考える
				/*
				 * ここは呼ばれないはず
				 */
				int handSize = sortHand.size() - 1;// 自分の手にあるカードの枚数 -1(list版)
				for (int i = 0; i < sendCardNum; i++) {// 強いほうから返す
					sendCard = sendCard.add(sortHand.get(handSize - i));
				}
			}
			return sendCard;

		} catch (Exception e) {// エラー発生時に大富豪、富豪なら一番弱いカードを大貧民、貧民なら一番強いカードを返すようにする

			// 相手に渡す手札の集合
			Cards sendCard = Cards.EMPTY_CARDS;
			/** 手牌昇順ソート 同じ数の時は 1. スペード2. ハート3. ダイヤ4. クラブ */
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
			init();
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

		myState.removeMyHand(this.hand());
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
			myState.searchStrongCard();// 出ていないカードで一番強いカードを検索
			if (myState.isRenew()) {
				myState.setRenew(false);// 場が流れた状態を元に戻す
			}
			myState.plusTurn();//ターン数を増やす
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
		int size = 0;
		if (myState == null) {
			init();
		}
		try {
			// TODO 以下のコメントアウト
			/*
			 * 下の部分にそのままAIを書いているが、このままだと絶対に複雑なメソッドになるので
			 * AI用のクラスを設けてそれにStateクラスと場に出されたMeld、placeなど(変更される恐れあり)
			 * を送って自分の打つ手を決めるようにさせる
			 */
			
			myState.setRemoveJokerMyhand(this.hand());

			if (this.place().isReverse() != myState.isReverse()) {// 革命が起きている時
				myState.setReverse(this.place().isReverse());// stateに革命を更新させる
			}

			Meld lastMeld = place().lastMeld();// 最後にフィールドに出た役

			myState.oneTurnKill(this, lastMeld);
			
			myState.check_Single();

			Meld finalMeld = null;// 最終的に決まった役

			Melds melds = Melds.parseMelds(this.hand());// 自分の手札を全て分解する

			// TODO クラスで設計を終えたら適当に組んだ戦略が多いのでもう少し良い戦略に変える

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
					/*
					 * 全ての役を調べてそれに対応するものを打たせる
					 */
					Suits boundSuit = this.place().lockedSuits();// 縛りの絵柄を抽出

					if (boundSuit != Suits.EMPTY_SUITS) {// 縛りが存在している時
						melds = melds.extract(Melds.suitsContain(boundSuit));// 縛りの絵柄が含まれているものを抽出する
					}

					if (melds == Melds.EMPTY_MELDS) {// 縛りの条件でカードが出せない場合
						finalMeld = PASS;
					} else {
						size = tactics.length;

						for (int i = 0; i < size; i++) {
							if (tactics[i].doAction(myState, this)) {
								finalMeld = tactics[i].discardMeld(melds,
										myState, this);
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
				size = melds.size();
				finalMeld = melds.get((int) (Math.random() * size));
			}
			return finalMeld;

		} catch (Exception e) {
			Meld meld = PASS;
			return meld;
		}

	}

	/**
	 * ゲームの状態の初期設定
	 */
	public void init() {
		myState = new MyState(this.hand());

	}

}
