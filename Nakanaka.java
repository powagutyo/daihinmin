
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import java.util.ArrayList;
import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import jp.ac.hosei.daihinmin.fujita2.*;
import jp.ac.hosei.daihinmin.fujita2.strategy.*;

/**
 * UECコンピュータ大貧民大会用大貧民プレイヤ 2011
 * UECコンピュータ大貧民大会のルールに沿って、default package に作成しているが、本来であれば、
 * jp.ac.hosei.daihinmin.fujita2 のパッケージにおくべきクラス
 * 
 * @author 藤田 悟(法政大学)
  */
public final class Nakanaka extends BotSkeleton {
	/**
	 * 場の状況を管理するオブジェクト
	 */
	public State state;

	/**
	 * プレイヤごとの勝利得点を格納 
	 */
	private int[] points = new int[5];
	
	/**
	 * ポイント加算に使う変数(5～1に変化)
	 */
	private int point;

	/**
	 * 直前に自分が出した役
	 */
	private Meld last;

	/**
     * 交換カードを決定するメソッド．
     * @return 交換相手に渡すカード集合
     */
	@Override
    public Cards requestingGivingCards() {
		try {
			//Utils.debug("Card Change");
			Cards result = Cards.EMPTY_CARDS;
			// 手札を昇順にソート．たとえば，D3, D4, ... S2, JOKER
			Cards sortedHand = Cards.sort(this.hand());
			sortedHand = sortedHand.remove(Card.JOKER);
			// sortedHand = sortedHand.remove(Card.S3);
			
			// 単騎出しにしか使えないカード
			Cards singleCards = removeCombinationMelds(sortedHand);
			singleCards = Cards.sort(singleCards);
			
			// 平民以上の時には、ペアではないカードを渡す
			int givenSize = Rules.sizeGivenCards(this.rules(),this.rank());
			// Rank は、大富豪が1,平民が3, 大貧民が5
			// diffrank は、富豪の時1、大富豪の時に2
			int diffrank = Rules.heiminRank(this.rules()) - this.rank();
			if(diffrank > 0) {
				// ダイヤの3は渡さない
				singleCards.remove(Card.D3);
				sortedHand.remove(Card.D3);
				
				if(givenSize == 1) {
					// 交換する1枚のカードを求める
					if(singleCards.size() < 1) {
						// 単騎の不要カードが1枚以下の時には、それ以上計算しないで
						// 弱いカードを適当に1枚出してしまう。
						result = result.add(sortedHand.get(0));
					} else {
						Card first = singleCards.get(0);
						if(first.rank() != Rank.EIGHT
								&& first.rank().compareTo(Rank.JACK) < 0) {
							// 10以下で8でないカード
							result = result.add(first);
						} else {
							result = result.add(sortedHand.get(0));
						}
					}
				} else if(givenSize == 2) {
					// 交換する2枚のカードを求める
					if(singleCards.size() < 2) {
						// 単騎カードが0か1枚しかないなら、弱い2枚を適当にだす。
						result = result.add(sortedHand.get(0));
						result = result.add(sortedHand.get(1));
					} else {
						Card first = singleCards.get(0);
						Card second = singleCards.get(1);
						
						if(first.rank() != Rank.EIGHT
								&& first.rank().compareTo(Rank.JACK) < 0) {
							result = result.add(first);
							if(second.rank() != Rank.EIGHT
									&& second.rank().compareTo(Rank.JACK) < 0) {
								// 10以下かつ、8でない単騎カードが見つかれば選択
								result = result.add(second);
							} else {
								// 2枚目の選択が複雑な時には、弱いカードを1枚選んでしまう
								result = result.add(sortedHand.remove(first).get(0));
							}
						} else {
							// 複雑な状況の時にはあきらめて、弱い2枚を出してしまう
							result = result.add(sortedHand.get(0));
							result = result.add(sortedHand.get(1));
						}
					}
				}
				
				// initGame();
			} else {
				// 平民、貧民、大貧民の場合
				// 実際には、このコードは呼び出されない
				int size = sortedHand.size();
				
				for(int i = 0; i < givenSize; i++){
					result = result.add(sortedHand.get(size - i - 1));
				}
			}
			
			//たとえば，大貧民なら JOKER S2
			//たとえば，大富豪なら D3 D4
			return result;
		} catch (Exception e) {
			// どこかで Exception が出てしまった時には、強制的に弱いカードを交換する。
			Cards result = Cards.EMPTY_CARDS;
			
			Cards sortedHand = Cards.sort(this.hand());
			sortedHand = sortedHand.remove(Card.JOKER);
			int givenSize = Rules.sizeGivenCards(this.rules(),this.rank());

			result.add(sortedHand.get(0));
			if(givenSize == 2) {
				result.add(sortedHand.get(1));
			}
			
			return result;
		}
	}
		
	/**
	 * 戦略の適用順を決定する配列
	 * 順番によっては、戦略がうまく適用できなくなるので、注意が必要
	 */
	Strategy[] strategyList = {
			new UseSpade3Strategy(),
			// new JokerLock(),
			new AllPassStrategy(),
			// melds を計算するだけ
			//new KeepSpade3Strategy(),
			new KeepSequenceStrategy(),
			new MeldCalculationStrategy(),
			new RevolutionStrategy(),
			// new KeepWeakestStrategy(),
			new RenewStrategy(),
			new LockedStrategy(),
			// 縛りの場合の計算をするだけ
			new FittingStrategy(),
			new NoCandidateStrategy(),
			new LastStrategy(),
			new YomikiriStrategy(),
			new LastTwoStrategy(),
			new LockingStrategy(),
			new RemoveJokerMoreThanThreeStrategy(),
			new NoCandidateStrategy(),
			// new DefeatStrongestStrategy(),
			new LessThanHalfStrategy(),
			new LessThanThreeCardsStrategy(),
			new GiveUpStrategy(),
			new FugouStrategy(),
			new HeiminStrategy(),
			new HinminStrategy(),
			new DaihinminStrategy(),
			//new StrongerMoreThanHalfStrategy(),
			new AverageStrategy(),
	};

    /**
     * 場に出すカードを決定するメソッド．
     * @return 場に出す役
     */
    @Override
    public Meld requestingPlay() {
    	try {
    		// 最初の順番が回ってきたときにだけ初期化する
    		if(state.hand == null) {
    			initGame();
    		}
    		
    		state.hand = hand();
    		
    		// 戦略ループからの戻り値を場に出す役にする
    		
    		Meld strategyMeld = strategyLoop(strategyList);
    		if(strategyMeld != null) {
    			// state の状況書き換え
    			state.place(strategyMeld);
    			
    			Cards cards = strategyMeld.asCards();
    			
    			// ジョーカーの1枚出しの時には、最強手になるように注意して場に出す
    			if(cards.size() == 1 && cards.get(0) == Card.JOKER) {
    				if(place().order() == Order.NORMAL) {
    					strategyMeld = MeldFactory.createSingleMeldJoker(strategyMeld.suits().get(0), Rank.JOKER_HIGHEST);
    				} else {
    					strategyMeld = MeldFactory.createSingleMeldJoker(strategyMeld.suits().get(0), Rank.JOKER_LOWEST);
    				}
    			}
    			last = strategyMeld;
    			return strategyMeld;
    		}
    	} catch (Exception e) {
    		// e.printStackTrace();
    	}

    	// エラーが起きた時には、自動的に PASS にしてしまう
    	last = PASS;
    	return last;
    }

    /**
     * 戦略選択と実行のためのループ
     * このループが、大貧民プレイヤの役判定のメインループになる
     * 
     * @return 選ばれた役
     */
	public Meld strategyLoop(Strategy[] strategyList) {
		// 戦略を strategyList から取り出して、順に試していくためのループ
		for(Strategy strategy: strategyList) {
			String mesg = "Strategy: " + strategy.getClass().getName();
			// 最初に条件チェックを行う
    		if(strategy.satisfyCondition(this, state)) {
    			Utils.debug(mesg + ", true");
    			if(state.melds != null) {
    				Utils.debug("size of melds = " + state.melds.size());
    			}
    			
    			// 条件チェックが通った戦略について、詳細な戦略の検討を行う
    			Meld meld = strategy.execute(this, state);
    			if(meld != null) {
    				// 出す手が決まった時には、その役を出す。
    				Utils.debug("Found: " + meld);
        			if(state.melds != null) {
        				Utils.debug("size of melds = " + state.melds.size());
        			}
    				return meld;
    			} else {
    				// 出す手が決まらなかった時には、次の戦略に進む
        			if(state.melds != null) {
        				Utils.debug("size of melds = " + state.melds.size());
        			}
    				Utils.debug("Not Found");
    			}
    		} else {
    			// 条件が満たされなかった時には、次の戦略に進む
    			Utils.debug(mesg + ", false");
    			if(state.melds != null) {
    				Utils.debug("size of melds = " + state.melds.size());
    			}
    		}
    	}

		return null;
	}

	/**
	 * カード集合から、JOKER と組み合わせ役のカードを除く
	 * @param cards 対象とするカード集合
	 * @return 組み合わせ役が除かれたカード集合
	 */
	private Cards removeCombinationMelds(Cards cards) {
		// JOKER を除く
		cards = cards.remove(Card.JOKER);

		ArrayList<MeldSet> meldSets = Utils.parseMelds(cards);
		
		MeldSet min = meldSets.get(0);
		int size = min.size();
		
		for(int i = 1; i < meldSets.size(); i++) {
			int tmp = meldSets.get(i).size();
			if(size > tmp) {
				size = tmp;
				min = meldSets.get(i);
			}
		}
		
		// 階段役を抽出
		Melds sequenceMelds = min.sequence;
		
		// 階段役を取り除く
		for(Meld meld: sequenceMelds) {
			cards = cards.remove(meld.asCards());
		}
		
		// グループ役を抽出 
		Melds groupMelds = Melds.parseGroupMelds(cards);

		// グループ役を取り除く
		for(Meld meld: groupMelds) {
			for(Card card: meld.asCards()) {
				cards = cards.remove(card);
			}
		}

		return cards;
	}

	/**
	 * 各プレイヤがそれぞれの手を出した時に呼び出される
	 * @param number プレイヤ番号
	 * @param number プレイヤが出した役
	 */
	@Override
	public void played(java.lang.Integer number, Meld playedMeld) {
		super.played(number, playedMeld);
		
		try {
			/*
			if(playedMeld.asCards().contains(Card.JOKER)) {
				System.out.println(playedMeld);
			}
			 */
		
			// 各ゲームの最初だけに呼び出される
			if(state.hand == null) {
				initGame();
				// 2重にカード枚数を引いてしまわないように補正
				state.numberOfPlayerCards[number] += playedMeld.asCards().size();
			}
			
			
			// 場の状況のアップデート
			state.played(number, playedMeld, this);
		} catch (Exception e) {
		}
	}

	/**
	 * 場が流れた時に呼び出される
	 */
	@Override
	public void placeRenewed() {
		super.placeRenewed();

		try {
		// playerPassedの初期化
			state.placeRenewed();
		} catch (Exception e) {
		}
	}

	/**
	 * ゲーム開始時に呼び出される
	 * カード交換の前であることに注意
	 */
	@Override
	public void gameStarted() {
		super.gameStarted();

		try {
			// 場の状況オブジェクトの初期化
			state = new State();
			state.setPoints(points);
			//Utils.debug("GameStarted");
			// 平民の場合は状態の初期化
			/*
			Integer rank = this.rank();
			if(rank == null || rank == 3) {
				initGame();
			}
			 */
			String mesg = "Points: ";
			for(int i = 0; i < 5; i++) {
				mesg += points[i] + ", ";
			}
			Utils.debug(mesg);
			point = 5;
		} catch (Exception e) {
		}
	}
	
	/**
	 * ゲーム終了時に呼び出される
	 */
	@Override
	public void gameEnded() {
		super.gameEnded();

		try {
		/*
		PlayersInformation info = playersInformation();
		//Integer[] ranks = info.rankOfPlayers();
		String mesg = "Point: ";
		for(int i = 0; i < 5; i++) {
			try {
				int rank = info.rankOfPlayers(i);
				points[i] += 6 - rank;
				mesg += points[i] + ", ";
			} catch (Exception e) {
				
			}
		}
		Utils.debug(mesg);
		*/
		} catch (Exception e) {
		}
	}

	/**
	 * 自分の出した手がサーバからリジェクトされたときに呼び出される
	 * デバッグ時には、JOptionPane のコメントを削除して、自分の手の不正を確認する
	 * @param number プレイヤ番号
	 * @param playedMeld プレイヤの出した役
	 */
	@Override
	public void playRejected(java.lang.Integer number, Meld playedMeld) {
		super.playRejected(number, playedMeld);
	
		try {
			if(this.number().equals(number) && playedMeld != last && playedMeld != PASS) {
				// JOptionPane.showMessageDialog(null, "おかしいぞ" + playedMeld);
			}
			
			if(this.number().equals(number) && playedMeld != PASS) {
				// JOptionPane.showMessageDialog(null, "へんだぞ" + playedMeld);
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * いずれかのプレイヤが勝利した時に呼び出される
	 * @param number プレイヤ番号
	 */
	@Override
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);

		try {
			// 場の状況オブジェクトのアップデート
			state.playerWon(number);
			points[number] += point;
			point--;
		} catch (Exception e) {
		}
	}

	/**
	 * カード交換? 
	 */
	@Override
	public void gaveCards(Integer from, Integer to, Cards cards) {
		super.gaveCards(from, to, cards);
		/*
		if(to == this.number() && this.rank() > 3) {
			initGame();
		}
		*/
	}

	/**
	 * ゲームの最初に自分の手札の処理や、階段役の解釈を行う
	 * このメソッドは、手札交換を行った後に呼ばれるべきである。
	 * 平民や貧民の時には、呼び出されるタイミングが難しい(呼び出している場所を参照)
	 */
	public void initGame() {
		state.addHand(this.hand());
		state.start = true;
		Melds melds = Melds.parseSequenceMelds(this.hand());
		if(!melds.isEmpty()) {
			state.sequence = true;
		}
		
		PlayersInformation info = playersInformation();
		state.numberOfPlayerCards = new int[5];
		int [] tmp = info.numCardsOfPlayers();
		for(int i = 0; i < 5; i++) {
			state.numberOfPlayerCards[i] = tmp[i];
		}
		int rank = 0;
		
		try {
			rank = rank();
		} catch(Exception e) {
		}
		
		// 順位を登録
		state.botRank = rank;
	}
}
