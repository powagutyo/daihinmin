package jp.ac.hosei.daihinmin.fujita;

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
	public boolean[] playerIsWon;

	/**
	 * 最後にカードを出したプレイヤID
	 */
	public int lastPlayer;

	/**
	 * 何ターン目か
	 */
	public int turn;

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
	 * 場の状態オブジェクトのコンストラクタ
	 * 内部の配列の初期化を実施
	 */
	public State() {
		playedCards = new boolean[4][14];
		playerPassed = new boolean[5];
		playerIsWon = new boolean[5];
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
		playerIsWon[number] = true;
	}

	/**
	 * プレイヤが場にカードを出した時に呼び出される
	 * @param number プレイヤID
	 * @param playedMeld 場に出したカード。Empty の時はパスを表す
	 */
	public void played(Integer number, Meld playedMeld) {
		if (playedMeld.asCards().isEmpty()) {
			playerPassed[number] = true;
			return;
		}

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
		}
	}

	/**
	 * 自分以外の全員 のプレイヤがパスしたかどうかを確認
	 * @param me 自分のプレイヤID
	 * @return 全員パスなら true
	 */
	public boolean isOtherPlayerAllPassed(int me) {
		for (int i = 0; i < 5; i++) {
			if (me == i || playerIsWon[i])
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
		Melds maxMelds = melds.extract(maxSize);

		if(maxMelds.get(0).asCards().size() == cards.size()) {
			return maxMelds.get(0);
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

	public void addHand(Cards cards) {
		for(Card card: cards) {
			if(card == Card.JOKER) {
				playedCards[0][0] = true;
			} else {
				int suit = card.suit().ordinal();
				int rank = card.rank().toInt();
				if(rank > 13) rank -= 13;
				playedCards[suit][rank] = true;
			}
		}
	}
}