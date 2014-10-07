package jp.ac.uec.daihinmin.player;


import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import jp.ac.hosei.daihinmin.fujita.State;
import jp.ac.hosei.daihinmin.fujita.Utils;
import jp.ac.hosei.daihinmin.fujita.strategy.*;

public final class Sokosoko extends BotSkeleton {
	/**
	 * 場の状況を管理するオブジェクト
	 */
	public State state;

	/**
     * 交換カードを決定するメソッド．
     * @return 交換相手に渡すカード集合
     */
	@Override
    public Cards requestingGivingCards() {
		//System.out.println("Card Change");
    	Cards result = Cards.EMPTY_CARDS;
    	// 手札を昇順にソート．たとえば，D3, D4, ... S2, JOKER
    	Cards sortedHand = Cards.sort(this.hand());
    	sortedHand = sortedHand.remove(Card.JOKER);

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
    		
    		initGame();
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
    }

	Strategy[] strategyList = {
			new AllPassStrategy(),
			// melds を計算するだけ
			new MeldCalculationStrategy(),
			new RevolutionStrategy(),
			new RenewStrategy(),
			// 縛りの場合の計算をするだけ
			new LockedStrategy(),
			new FittingStrategy(),
			new NoCandidateStrategy(),
			new YomikiriStrategy(),
			new RemoveJokerMoreThanThreeStrategy(),
			new NoCandidateStrategy(),
			new StrongerMoreThanHalfStrategy(),
			new AverageStrategy(),
	};

    /**
     * 場に出すカードを決定するメソッド．
     * @return 場に出す役
     */
    @Override
    public Meld requestingPlay() {
    	try {
    		Meld strategyMeld = strategyLoop(strategyList);
    		if(strategyMeld != null) {
    			return strategyMeld;
    		}
    	} catch (Exception e) {
    	}

    	return PASS;
    }

    /**
     * 戦略選択と実行のためのループ
     * @return 選ばれた役
     */
	public Meld strategyLoop(Strategy[] strategyList) {
		String ret = "";
		for(Strategy strategy: strategyList) {
			ret = "Strategy: " + strategy.getClass().getName();
    		if(strategy.satisfyCondition(this, state)) {
    			Utils.debug(ret + ", true");
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
    			Utils.debug(ret + ", false");
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

		Melds groupMelds = Melds.parseGroupMelds(cards);
		Melds sequenceMelds = Melds.parseSequenceMelds(cards);

		for(Meld meld: groupMelds) {
			for(Card card: meld.asCards()) {
				cards = cards.remove(card);
			}
		}

		// エラー発見 2011/7/4
		// Melds sequenceMelds = Melds.parseSequenceMelds(cards);

		for(Meld meld: sequenceMelds) {
			for(Card card: meld.asCards()) {
				cards = cards.remove(card);
			}
		}

		return cards;
	}

	@Override
	public void played(java.lang.Integer number, Meld playedMeld) {
		super.played(number, playedMeld);

		// 場の状況のアップデート
		state.played(number, playedMeld);
	}

	@Override
	public void placeRenewed() {
		super.placeRenewed();

		// playerPassedの初期化
		state.placeRenewed();
	}

	@Override
	public void gameStarted() {
		super.gameStarted();

		// 場の状況オブジェクトの初期化
		state = new State();
		//System.out.println("GameStarted");
		// 平民の場合は状態の初期化
		Integer rank = this.rank();
		if(rank == null || rank == 3) {
			initGame();
		}
	}

	@Override
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);

		// 場の状況オブジェクトのアップデート
		state.playerWon(number);
	}

	@Override
	public void gaveCards(Integer from, Integer to, Cards cards) {
		super.gaveCards(from, to, cards);
		if(to == this.number() && this.rank() > 3) {
			initGame();
		}
	}

	/**
	 * ゲームの最初に自分の手札の処理や、階段役の解釈を行う
	 */
	public void initGame() {
		state.addHand(this.hand());
		Melds melds = Melds.parseSequenceMelds(this.hand());
		if(!melds.isEmpty()) {
			state.sequence = true;
		}
	}
}
