package jp.ac.hosei.daihinmin.fujita2;

import java.util.ArrayList;
import static jp.ac.uec.daihinmin.card.MeldFactory.PASS;
import jp.ac.uec.daihinmin.Order;
import jp.ac.uec.daihinmin.card.Card;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Extractor;
import jp.ac.uec.daihinmin.card.Meld;
import jp.ac.uec.daihinmin.card.Melds;
import jp.ac.uec.daihinmin.card.Rank;
import jp.ac.uec.daihinmin.card.Suit;
import jp.ac.uec.daihinmin.player.BotSkeleton;

public class State {
	/**
	 * MeldSet の計算を行うか否か
	 */
	public boolean useMeldSet = false;
	
	/**
	 * 場に出たカードをチェック
	 * playedCards[0][0] は JOKER
	 */
	public boolean[][] playedCards;

	/**
	 * プレイヤが現在の場で既にパスしているかどうか
	 */
	public boolean[] playerPassed;

	/**
	 * 既にあがったプレイヤを確認する配列
	 */
	public boolean[] playerWon;
	
	/**
	 * これまでの累積の成績
	 */
	public int[] points;

	/**
	 * 最後にカードを出したプレイヤID
	 */
	public int lastPlayer = -1;

	/**
	 * 何ターン目か
	 */
	public int turn;

	/**
	 * 現在の手札
	 */
	public Cards hand;

	/**
	 * JOKERを除いた手札
	 */
	public Cards handWithoutJoker;

	/**
	 * Sequence を除いた手札集合
	 */
	public Cards handWithoutSequence;
	
	/**
	 * 現在組める役集合
	 */
	public Melds melds;
	
	/**
	 * 現在が通常か革命中か。trueは通常。false は革命中。
	 */
	public boolean order;

	/**
	 * JOKERなしでシーケンスを持つ可能性があるか
	 */
	public boolean sequence;
	
	/**
	 * 最弱の　Meld を保存するための変数
	 */
	public Meld weakestMeld = null;
	
	/**
	 * JOKERを持っているか否か
	 */
	public boolean joker = false;
	
	/**
	 * JOKER を除いた役集合の
	 */
	public ArrayList<MeldSet> noJokerMeldSets;
	
	/**
	 * JOKER を加えた役集合
	 */
	public ArrayList<MeldSet> jokerMeldSets;
	
	/**
	 * 各プレイヤの残り枚数
	 */
	public int[] numberOfPlayerCards;
	
	/**
	 * Bot のランク
	 * ランクが未定の時は0, 大富豪は1, 大貧民は5
	 */
	public int botRank;
	
	/**
	 * 各ゲームのスタート時点に true にセットされる。
	 */
	public boolean start;
	
	public int[] minimumRank;
	
	public Melds seqMelds = null;
	
	public boolean placeThreeCards = false;

	/**
	 * 場の状態オブジェクトのコンストラクタ
	 * 内部の配列の初期化を実施
	 */
	public State() {
		playedCards = new boolean[4][14];
		playerPassed = new boolean[5];
		playerWon = new boolean[5];
		minimumRank = new int[5];
		turn = 0;
		sequence = false;
	}

	/**
	 * 場が流れた時に呼び出される
	 * すべてのプレイやをパスしていない状態に戻す
	 */
	public void placeRenewed() {
		for (int i = 0; i < 5; i++) {
			playerPassed[i] = false;
		}
		lastPlayer = -1;
	}

	/**
	 * あるプレイヤが勝利した時に呼び出される
	 * @param number 勝利したプレイヤID
	 */
	public void playerWon(Integer number) {
		playerWon[number] = true;
	}

	/**
	 * プレイヤが場にカードを出した時に呼び出される
	 * @param number プレイヤID
	 * @param playedMeld 場に出したカード。Empty の時はパスを表す
	 */
	public void played(Integer number, Meld playedMeld, BotSkeleton bot) {
		boolean newOrder = (bot.place().order() == Order.NORMAL);
		// reverse が起きた時に、minimumRankの内容を破棄 
		if(order != newOrder) {
			order = newOrder;
			for(int i = 0; i < minimumRank.length; i++) {
				minimumRank[i] = 0;
			}
		}
		
		if(playedMeld != PASS) {
			// renew の時に、miminumRank を設定
			if(lastPlayer < 0) {
				Cards sorted = Cards.sort(playedMeld.asCards());
				
				int rank;
				if(order) {
					if(sorted.get(0) == Card.JOKER) {
						rank = 16;
					} else {
						rank = sorted.get(0).rank().toInt();
					}
				} else {
					if(sorted.get(sorted.size() - 1) == Card.JOKER) {
						rank = 2;
					} else {
						rank = sorted.get(0).rank().toInt();
					}
				}
				minimumRank[number] = rank;
			}
		}
		
		if(playedMeld.type() == Meld.Type.GROUP && playedMeld.asCards().size() == 3) {
			placeThreeCards = true;
		}
		
		Utils.debug("プレイヤ" + number +"が" + playedMeld + "を場に出す");
		if (playedMeld.asCards().isEmpty()) {
			playerPassed[number] = true;
			return;
		}

		start = false;
		lastPlayer = number;

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
			numberOfPlayerCards[number]--;
		}
	}

	/**
	 * 自分以外の全員 のプレイヤがパスしたかどうかを確認
	 * @param me 自分のプレイヤID
	 * @return 全員パスなら true
	 */
	public boolean isOtherPlayerAllPassed(int me) {
		for (int i = 0; i < 5; i++) {
			if (me == i || playerWon[i])
				continue;
			if (!playerPassed[i])
				return false;
		}

		return true;
	}

	/**
	 * カードが現在の最強カードかどうかを判定
	 * @param card 対象のカード
	 * @param order 革命が起こっているかいないならば true
	 * @return 最強の時に true
	 */
	public boolean isStrongestCard(Card card, boolean order) {
		// 自分のカードより強いカードが出ているか判断する
		if (card == Card.JOKER) {
			if(playedCards[Suit.SPADES.ordinal()][3] == true) {
				// スペードの3が既に場に出ている
				return true;
			} else {
				return false;
			}
		} else if(card.rank() == Rank.EIGHT) {
			return true;
		}

		if (playedCards[0][0] == false) {
			// ジョーカーが未だ出ていなければ、最強ではない
			return false;
		}

		int rankNum = card.rank().toInt();

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

	/**
	 * 役が現在の最強かどうかを判定
	 * 現在は縛りは考慮していない
	 * @param meld 対象の役
	 * @param order 革命が起こっているかいないならば true
	 * @return 最強の時に true
	 */
	public boolean isStrongestMeld(Meld meld, boolean order) {
		if(meld.type() == Meld.Type.SINGLE) {
			return isStrongestCard(meld.asCards().get(0), order);
		} else if(meld.type() == Meld.Type.GROUP) {
			if(meld.rank() == Rank.EIGHT) {
				return true;
			}
			int size = meld.asCards().size();

			// JOKER が場に出ていない場合は、1枚少ない枚数でもGroupを組めるので、sizeを-1する
			if(playedCards[0][0] == false) {
				size--;
			}

			int rank = meld.rank().toInt();

			for(int i = (order? rank + 1: rank - 1); (order? i <= 15: i >= 3); i = (order? i + 1: i - 1)) {
				int rk = i;
				if(rk > 13) rk -= 13;
				int count = 0;
				for(int j = 0; j < 4; j++) {
					if(playedCards[j][rk] == false) {
						count++;
					}
				}
				if(count >= size) {
					return false;
				}
			}
			return true;
		} else if(meld.type() == Meld.Type.SEQUENCE) {
			int length = meld.asCards().size();
			int rank = meld.rank().toInt();
			int count = 0;

			if(rank <= 8 && 8 < rank + length ) {
				return true;
			}

			for(int i = (order? rank + length: rank - length); (order? i <= 15 - length - 1: i >= 3 + length - 1); i = (order? i + 1: i - 1)) {
				for(int j = 0; j < 4; j++) {
					if(playedCards[0][0] == false) {
						// Joker を相手が持っている場合は、1枚欠けていてもシーケンスが完成できる
						count = 1;
					}
					boolean found = true;
					for(int k = 0; k < length; k++) {
						int rk = (order? i + k: i - k);
						if(rk > 13) rk -= 13;
						if(playedCards[j][rk] == true) {
							if(count == 0) {
								found = false;
								break;
							} else {
								count--;
							}
						}
					}
					if(found) {
						return false;
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 残りの全カード中の rate 以上に強いカードが cards 中に何枚あるかをカウント
	 * @param cards 調査したい手札
	 * @param rate 全体の中の強さの割合
	 * @param order 通常か革命か
	 * @return 強いカードの枚数
	 */
	public int numberOfStrongerCards(Cards cards, double rate, boolean order) {
		int rank = rankOfStrongerCards(rate, order);
		// System.out.printf("stronger rank = %d%n", rank);

		int count = 0;
		for(Card card: cards) {
			if(card.equals(Card.JOKER)) {
				count++;
			} else if(order && card.rank().toInt() >= rank) {
				count++;
			} else if(!order && card.rank().toInt() <= rank) {
				count++;
			}
		}

		return count;
	}

	/**
	 * 残りの全カード中の rate で示す割合の強さを持つカードのランク
	 * @param rate 残りカードの中での強さの割合
	 * @param order 通常か革命か
	 * @return 対象のカードのランク
	 */
	public int rankOfStrongerCards(double rate, boolean order) {
		int total = 0;
		int stronger = 0;

		if (!playedCards[0][0]) {
			total++;
			stronger++;
		}
		for(int i = 0; i < 4; i++) {
			for(int j = 1; j <=13; j++) {
				if(!playedCards[i][j]) {
					total++;
				}
			}
		}

		int target = (int)((1.0 - rate) * total);

		for(int j = (order? 15: 3); (order? j >= 3: j <= 15); j = j + (order? -1: 1)) {
			int num = (j <= 13? j : j - 13);
			for(int i = 0; i < 4; i++) {
				if(!playedCards[i][num]) {
					stronger++;
					if(stronger > target) {
						return j;
					}
				}
			}
		}

		return (order? 3: 15);
	}

	/**
	 * プレイヤー全員の手札合計を計算
	 * @param bot Botオブジェクト
	 * @return 残り枚数
	 */
	public int getNumberOfAllCards(BotSkeleton bot) {
		int[] allCards = bot.playersInformation().numCardsOfPlayers();
		int total = 0;

		for (int cards : allCards) {
			total += cards;
		}

		return total;
	}

	public Meld yomikiri(Cards cards, Melds melds) {
		Extractor<Meld,Melds> maxSize = Melds.MAX_SIZE;
		
		if(melds.isEmpty()) {
			melds = Melds.parseMelds(cards);
		}

		Melds maxMelds = melds.extract(maxSize);
		
		if(maxMelds.get(0).asCards().size() == cards.size()) {
			return maxMelds.get(0);
		}
		
		// 革命が起きないための措置
		if(maxMelds.get(0).asCards().size() >= 4) {
			maxMelds = melds.extract(Melds.sizeOf(4));
		}	
		
		if(cards.size() > 8) {
			// 探索を打ち切り
			return null;
		} else if(cards.size() > 6) {
			// 残り枚数が多い時には、探索を maxMeldsだけに絞る
			Extractor<Meld,Melds> maxRank;
			if(order)
				maxRank = Melds.MAX_RANK;
			else
				maxRank = Melds.MIN_RANK;
			melds = maxMelds.extract(maxRank);
		}

		for(Meld meld: melds) {
			if(isStrongestMeld(meld, order)) {
				Cards remain = cards.remove(meld.asCards());
				Melds remainMelds = Melds.parseMelds(remain);

				if(yomikiri(remain, remainMelds) != null) {
					return meld;
				}
			}
		}

		return null;
	}

	/**
	 * 自分の手札は、場に出ている手札と同じように残り札から削除する
	 * @param cards 自分の手札
	 */
	public void addHand(Cards cards) {
		for(Card card: cards) {
			if(card == Card.JOKER) {
				// JOKER の時は、[0][0]を持っていることにする
				playedCards[0][0] = true;
			} else {
				int suit = card.suit().ordinal();
				int rank = card.rank().toInt();
				if(rank > 13) rank -= 13;
				playedCards[suit][rank] = true;
			}
		}
		
		// state の初期化
		hand = Cards.sort(cards);
		
		//if(useMeldSet) {
			if(hand.contains(Card.JOKER)) {
				joker = true;
				handWithoutJoker = hand.remove(Card.JOKER);
				// jokerMeldSets = Utils.parseMelds(hand);
			} else {
				handWithoutJoker = hand;
			}
			noJokerMeldSets = Utils.parseMelds(handWithoutJoker);
				
			MeldSet min = noJokerMeldSets.get(0);
			int size = min.size();
			
			for(int i = 1; i < noJokerMeldSets.size(); i++) {
				int tmp = noJokerMeldSets.get(i).size();
				if(size > tmp) {
					size = tmp;
					min = noJokerMeldSets.get(i);
				}
			}
			
			seqMelds = min.sequence;
		//}
	}
	
	/**
	 * 累積ポイントの設定
	 * @param points　累積ポイント
	 */
	public void setPoints(int[] points) {
		this.points = points;
	}
	
	/**
	 * 自分以外の最高得点者を返却
	 * @param me 自分のID
	 * @return 最高得点者
	 */
	public int strongestPartner(int me) {
		int max = -1;
		int id = -1;
		
		for(int i = 0; i < 5; i++) {
			if(i == me) {
				continue;
			}
			
			if(max < points[i]) {
				max = points[i];
				id = i;
			}
		}
		
		return id;
	}
	
	/**
	 * 対象のカードより強いカードが何枚残っているか
	 * @param card
	 * @return
	 */
	public int numberOfStrongerCards(Card card) {
		// 自分のカードより強いカードが出ているか判断する
		if (card == Card.JOKER) {
			if(playedCards[Suit.SPADES.ordinal()][3] == true) {
				// スペードの3が既に場に出ている
				return 0;
			} else {
				return 1;
			}
		} else if(card.rank() == Rank.EIGHT) {
			return 0;
		}
		
		int number = 0;

		if (playedCards[0][0] == false) {
			// ジョーカーが未だ出ていなければ、最強ではない
			number++;
		}

		int rankNum = card.rank().toInt();

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
					number++;
				}
			}
		}

		return number;
	}
	
	/**
	 * 対象のカードと等しいか強いカードを何枚持っているか
	 * @param card
	 * @return
	 */
	public int numberOfMyStrongerCards(Card card) {
		if(card == Card.JOKER) {
			return 0;
		}
		
		// 自分を除くため、-1 にする
		int number = -1;
		int rank = card.rank().toInt();
		
		for(Card other: hand) {
			if(other == Card.JOKER) {
				number++;
			} else if(order) {
				if(other.rank().toInt() >= rank) {
					number++;
				}
			} else {
				if(other.rank().toInt() <= rank) {
					number++;
				}
			}
		}
		
		return number;
	}
	
	/**
	 * 場に Meld を出す時に、state の状態を変更
	 * @param meld 場に出す Meld
	 */
	public void place(Meld meld) {
		hand.remove(meld.asCards());
		
		if(useMeldSet) {
			if(joker) {
				if(meld.asCards().contains(Card.JOKER)) {
					joker = false;
					jokerMeldSets = null;
				} else {
					ArrayList<MeldSet> removed = new ArrayList<MeldSet>();
					for(MeldSet set: jokerMeldSets) {
						if(set.remove(meld)) {
							removed.add(set);
						}
					}
					
					for(MeldSet set: removed) {
						jokerMeldSets.remove(set);
					}
				}
			}
			
			// JOKER なしの MeldSet をチェック
			ArrayList<MeldSet> removed = new ArrayList<MeldSet>();
			for(MeldSet set: noJokerMeldSets) {
				if(set.remove(meld)) {
					removed.add(set);
				}
			}
			
			for(MeldSet set: removed) {
				noJokerMeldSets.remove(set);
			}
		}
	}
	
	/**
	 * プレイ中のプレイヤ数
	 * @return
	 */
	public int numberOfPlayers() {
		int count = 0;
		for(int i = 0; i < 5; i++) {
			if(!playerWon[i]) {
				count++;
			}
		}
		return count;
	}
		
	public void test() {
		if(useMeldSet) {
			if(jokerMeldSets != null) {
				Utils.debug("jokerMeldSet = " + jokerMeldSets.size());
				for(MeldSet set: jokerMeldSets) {
					Utils.debug("MeldSet Size = " + set.size());
				}
			}
			Utils.debug("noJokerMeldSet = " + noJokerMeldSets.size());
			for(MeldSet set: noJokerMeldSets) {
				Utils.debug("MeldSet Size = " + set.size());
			}
		}
	}
	
	Score highestMinimumRank() {
		int player = -1;
		int high = order? 1: 17;
		for(int i = 0; i < minimumRank.length; i++) {
			if(!playerWon[i]) {
				if(order) {
					if(high < minimumRank[i]) {
						high = minimumRank[i];
						player = i;
					}
				} else {
					if(high > minimumRank[i]) {
						high = minimumRank[i];
						player = i;
					}
				}
			}
		}
		
		return new Score(player, high);
	}
	
	class Score {
		int player;
		int rank;
		
		Score(int player, int high) {
			this.player = player;
			this.rank = high;
		}
	}
	
	/**
	 * 高いレベルの戦いになっているので諦める判定を行う
	 * @return
	 */
	public boolean shoudGiveUp(BotSkeleton bot) {
		Score highest = highestMinimumRank();
		
		if(highest.player == -1) {
			return false;
		}
		
		//int strong = 0;
		int weak = 0;
		//int total = 0;
		
		for(Card other: hand) {
			//total++;
			if(other == Card.JOKER) {
				//strong++;
			} else if(order) {
				int rank = other.rank().toInt();
				if(rank > highest.rank) {
					//strong++;
				} else if(rank < highest.rank) {
					weak++;
				}
			} else {
				int rank = other.rank().toInt();
				if(rank < highest.rank) {
					//strong++;
				} else if(rank > highest.rank) {
					weak++;
				}
			}
		}
		
		if(weak > numberOfPlayerCards[highest.player] + 7) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 自分より強い3カードが出せる可能性の数
	 * @param rank 自分が出したい3カードのランク
	 * @return 可能性数
	 */
	public int possibilityThreeCards(int rank) {
		int number = 0;
		if(order) {
			for(int i = rank + 1; i < 16; i++) {
				int count = 0;
				int r = i;
				if(r > 13) r -= 13;
				for(int j = 0; j < 4; j++) {
					if(!playedCards[j][r]) {
						count++;
					}
				}
				if(count > 2) {
					number++;
				}
			}
		} else {
			for(int i = rank - 1; i > 2; i--) {
				int count = 0;
				int r = i;
				if(r > 13) r -= 13;
				for(int j = 0; j < 4; j++) {
					if(!playedCards[j][r]) {
						count++;
					}
				}
				if(count > 2) {
					number++;
				}
			}
		}
		
		return number;
	}
}