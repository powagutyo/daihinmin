
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import java.util.ArrayList;
import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import jp.ac.hosei.daihinmin.fujita2.*;
import jp.ac.hosei.daihinmin.fujita2.strategy.*;

//import javax.swing.JOptionPane;


public final class Nakanaka extends BotSkeleton {
	/**
	 * 場の状況を管理するオブジェクト
	 */
	public State state;
	
	private int[] points = new int[5];
	private int point;
	private int game = 0;
	private int[][] wins = new int[5][5000];
	private int[][][] wins2 = new int[5][5][5];
	
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
			int diffrank = Rules.heiminRank(this.rules()) - this.rank();
			if(diffrank > 0) {
				// ダイヤの3は渡さない
				singleCards.remove(Card.D3);
				sortedHand.remove(Card.D3);
				
				if(givenSize == 1) {
					if(singleCards.size() < 1) {
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
				int size = sortedHand.size();
				
				for(int i = 0; i < givenSize; i++){
					result = result.add(sortedHand.get(size - i - 1));
				}
			}
			
			//たとえば，大貧民なら JOKER S2
			//たとえば，大富豪なら D3 D4
			return result;
		} catch (Exception e) {
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
    		if(state.hand == null) {
    			initGame();
    		}
    		
    		state.hand = hand();
    		
    		Meld strategyMeld = strategyLoop(strategyList);
    		if(strategyMeld != null) {
    			// state の状況書き換え
    			state.place(strategyMeld);
    			
    			Cards cards = strategyMeld.asCards();
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

    	last = PASS;
    	return last;
    }

    /**
     * 戦略選択と実行のためのループ
     * @return 選ばれた役
     */
	public Meld strategyLoop(Strategy[] strategyList) {
		for(Strategy strategy: strategyList) {
			String mesg = "Strategy: " + strategy.getClass().getName();
    		if(strategy.satisfyCondition(this, state)) {
    			Utils.debug(mesg + ", true");
    			if(state.melds != null) {
    				Utils.debug("size of melds = " + state.melds.size());
    			}
    			Meld meld = strategy.execute(this, state);
    			if(meld != null) {
    				Utils.debug("Found: " + meld);
        			if(state.melds != null) {
        				Utils.debug("size of melds = " + state.melds.size());
        			}
    				return meld;
    			} else {
        			if(state.melds != null) {
        				Utils.debug("size of melds = " + state.melds.size());
        			}
    				Utils.debug("Not Found");
    			}
    		} else {
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
		
		Melds sequenceMelds = min.sequence;
		
		for(Meld meld: sequenceMelds) {
			cards = cards.remove(meld.asCards());
		}
		
		Melds groupMelds = Melds.parseGroupMelds(cards);
		

		for(Meld meld: groupMelds) {
			for(Card card: meld.asCards()) {
				cards = cards.remove(card);
			}
		}

		return cards;
	}

	@Override
	public void played(java.lang.Integer number, Meld playedMeld) {
		super.played(number, playedMeld);
		
		try {
			/*
			if(playedMeld.asCards().contains(Card.JOKER)) {
				System.out.println(playedMeld);
			}
			 */
		
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

	@Override
	public void placeRenewed() {
		super.placeRenewed();

		try {
		// playerPassedの初期化
			state.placeRenewed();
		} catch (Exception e) {
		}
	}

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
	
	int[][] colors = {
			{255, 0, 0}, 
			{0, 255, 0},
			{0, 0, 255},
			{255, 255, 0},
			{0, 255,255}
	};
	
	@Override
	public void gameEnded() {
		super.gameEnded();

		try {
			if(game == 0) {
				Canvas.show(600, 600);
				Canvas.setColor(0, 0, 0);
				Canvas.drawLine(50, 550, 550, 550);
				Canvas.drawLine(50, 50, 50, 550);
			} else {
				Canvas.disableAutoRepaint();
				Canvas.setColor(255, 255, 255);
				Canvas.fillRect(0, 0, 600, 600);
				Canvas.setColor(0, 0, 0);
				Canvas.drawLine(50, 550, 550, 550);
				Canvas.drawLine(50, 50, 50, 550);
				for(int player = 0; player < 5; player++) {
					for(int i = 0; i < 5; i++) {
						for(int j = 0; j < 5; j++) {
							Canvas.setColor(colors[j][0], colors[j][1], colors[j][2]);
							int height =  wins2[player][i][j] * 2000 / game;
							Canvas.fillRect(50 + 100 * player + i * 20 + j * 4, 550 - height, 4, height); 
						}
					}
				}
				Canvas.forceRepaint();
			}
			
			game++;
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
	
	
	@Override
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);

		try {
			// 場の状況オブジェクトのアップデート
			state.playerWon(number);
			points[number] += point;
			wins[number][game] = 6 - point;
			wins2[number][wins[number][game - 1]-1][5 - point]++;
			point--;
		} catch (Exception e) {
		}
	}

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
