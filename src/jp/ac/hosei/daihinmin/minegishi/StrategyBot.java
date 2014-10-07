package jp.ac.hosei.daihinmin.minegishi;

//import javax.swing.JOptionPane;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import jp.ac.uec.daihinmin.*;

public class StrategyBot extends BotSkeleton{
	/**
	 * パスしたプレイヤ
	 */
	private boolean[] playerPassed;
	/**
	 * 既に勝利したプレイヤ
	 */
	private boolean[] playerWon;
	/**
	 * 場に出たカード
	 */
	boolean[][] playedCards;

	@Override
    public Cards requestingGivingCards() {
    	Cards result = Cards.EMPTY_CARDS;
    	//手札を昇順にソート．たとえば，D3, D4, ... S2, JOKER
    	Cards sortedHand = Cards.sort(this.hand());
    	sortedHand = sortedHand.remove(Card.JOKER);

    	// 平民以上の時には、ペアではないカードを渡す
    	int givenSize = Rules.sizeGivenCards(this.rules(),this.rank());
    	// Rank は、大富豪が1,平民が3, 大貧民が5
    	int diffrank = Rules.heiminRank(this.rules()) - this.rank();
    	if(diffrank > 0) {
    		if(givenSize == 1) {
    			Card first = findLowestSingleCard(sortedHand, 0);
    			if(first == null) {
    				result = result.add(sortedHand.get(0));
    			} else {
    				result = result.add(first);
    			}
    			return result;
    		} else if(givenSize == 2) {
    			Card first = findLowestSingleCard(sortedHand, 0);
    			if(first != null) {
    				int len = sortedHand.size();
    				int num = 0;
    				for(int i = 0; i < len; i++) {
    					if(sortedHand.get(i) == first) {
    						num = i;
    						break;
    					}
    				}
    				Card second = findLowestSingleCard(sortedHand, num + 1);
    				if(second != null) {
    					result = result.add(first);
    					result = result.add(second);
    					return result;
    				}
    			}
				result = result.add(sortedHand.get(0));
				result = result.add(sortedHand.get(1));
				return result;
    		}
    	}

    	// 再度、空にする
    	result = Cards.EMPTY_CARDS;

    	//渡すカードの枚数だけ，resultにカードを追加
    	for(int i=0;i < Rules.sizeGivenCards(this.rules(),this.rank()); i++){
    		result = result.add(sortedHand.get(/*平民より上か？ 注:07年度のルールでは平民以上の時のみ選ぶことができる */
    				Rules.heiminRank(this.rules()) < this.rank()?
    						sortedHand.size()-1-i   /*平民より下*/
    						:i                      /*平民より上*/));
    	}
    	//たとえば，大貧民なら D3 D4
    		//たとえば，大富豪なら JOKER S2

    	return result;
    }

    private Card findLowestSingleCard(Cards cards, int num) {
    	Melds groupMelds = Melds.parseGroupMelds(cards);
    	Melds seqMelds = Melds.parseSequenceMelds(cards);
    	
		for(int i = num; i < cards.size(); i++) {
			Card first = cards.get(i);
		    boolean found = false;
			for(Meld meld:groupMelds){
				if(meld.asCards().contains(first)){
				   found = true;
				   break;
				}
			}

		    for(Meld meld:seqMelds){
		    	if(meld.asCards().contains(first)){
		    		found = true;
		    		break;
		    	}
		    }
			if(found){
				continue;
			}
			// 直前のカードと等しい時には、読み飛ばす
			

		      int rank = first.rank().toInt();
				if(rank != 8 && rank <=10){
					// 結果のカードが8より小さければ、それを渡す
					return first;
				} else {
					// 結果のカードが8より小さくなければ、null を返す
					return null;
				}
		
		}
		return null;
    }

	@Override
	public Meld requestingPlay() {
		try {
			Meld meld ;
			
			Strategy[] strategyList = {new RenewStrategy(),
					new SingleSizeStrategy(),
					new LockSuitsStrategy(),
					new AdjustRankStrategy(),
					
			};
			for(Strategy strategy : strategyList){
				meld = strategy.execute(this);
				if (meld!=null){
					return meld;
				}
			}
		} catch (Exception e) {
		}
		
        return MeldFactory.PASS;
	}
	/**
	 * 誰かが場に札を出す、あるいは、パスした時に呼び出されるメソッド
	 */
	@Override
	public void played(java.lang.Integer number, Meld playedMeld) {
		super.played(number, playedMeld);

		// System.out.println(number + "さんが" + playedMeld.toString() + "を出しました。");

		if (playedMeld.asCards().isEmpty()) {
			playerPassed[number] = true;
		}

		for (Card card : playedMeld.asCards()) {
			// suit = マーク
			if (card == Card.JOKER) {
				playedCards[0][0] = true;
			} else {
				Suit suit = card.suit();
				Rank rank = card.rank();
				int suitNum = suit.ordinal();
				int rankNum = rank.toInt();
				if (rankNum > 13) {
					rankNum -= 13;
				}
				playedCards[suitNum][rankNum] = true;

			}
		}
	}

	/**
	 * 場が流れた時に呼び出されるメソッド
	 */
	@Override
	public void placeRenewed() {
		super.placeRenewed();
		// playerPassedの初期化
		for (int i = 0; i < 5; i++) {
			playerPassed[i] = false;
		}
	}

	/**
	 * 新しいゲームがスタートする時に呼び出されるメソッド
	 */
	@Override
	public void gameStarted() {
		super.gameStarted();
		// jokerは[0][0]
		playedCards = new boolean[4][14];
		playerPassed = new boolean[5];
		playerWon = new boolean[5];

	}

	/**
	 * プレイヤが勝利したときに呼び出されるメソッド
	 */
	@Override
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);
		playerWon[number] = true;
	}
	public boolean isStrongestCard(Card card) {
		// 自分のカードより強いカードが出ているか判断する
		if (card == Card.JOKER) {
			return true;
		}

		if (playedCards[0][0] == false) {
			// ジョーカーが未だ出ていなければ、最強ではない
			return false;
		}

		int rankNum = card.rank().toInt();
		boolean order = this.place().order() == Order.NORMAL;

		for (int i = rankNum + (order? 1: -1); (order? i <= 15: i >= 3); i = i + (order? 1: -1)) {
			int num;

			if (i >= 14) {
				num = i - 13;
			} else {
				num = i;
			}

			// jはスートを調べる
			for (int j = 0; j < 4; j++) {
				if (!playedCards[j][num]) {
					return false;
				}
			}
		}

		return true;
	}
}
