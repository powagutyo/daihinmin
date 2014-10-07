package jp.ac.uec.daihinmin.player;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.ac.hosei.daihinmin.HiroshiIwasaki3.TeruteruTradeCard;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import jp.ac.uec.daihinmin.*;

public class Teruteru extends BotSkeleton {

	int Kcount = 0;

	/**
	 * パスしたプレイヤ
	 */
	private boolean[] playerPassed;

	/**
	 * 既に勝利したプレイヤ
	 */
	private boolean[] playerWon;

	private int playerWonNum, myRank;

	private double average;

	/**
	 * 場に出たカード boolean[4][14] playedCards[0][0]がJOKER
	 * playedCards[suitNum][rankNum] suitNum=0 →　SPADES suitNum=1　→　HEARTS
	 * suitNum=2　→　CLUBS suitNum=3　→　DIAMONDS
	 */
	private boolean[][] playedCards;

	private Rank MaxRank, MinRank, MaxPairRank, MinPairRank, MaxSecondRank,
			MinSecondRank;

	// MaxSecondRankとMinSecondRankを使うプログラムを追加しようと思ったが力尽きた・・・
	// MaxSecondRankとMinSecondRankにrankを追加するプログラムは作成済み

	private Meld MyLastMeld;

	Extractor<Meld, Melds> SinglesExtractor = Melds.SINGLES;
	Extractor<Meld, Melds> sizeTwoExtractor = Melds.sizeOf(2);
	Extractor<Meld, Melds> sizeThreeExtractor = Melds.sizeOf(3);
	Extractor<Meld, Melds> sizeFourExtractor = Melds.sizeOf(4);
	Extractor<Meld, Melds> selectEightExtractor = Melds.rankOf(Rank.EIGHT);
	Extractor<Meld, Melds> selectTwoExtractor = Melds.rankOf(Rank.TWO);
	Extractor<Meld, Melds> selectAceExtractor = Melds.rankOf(Rank.ACE);
	Extractor<Meld, Melds> selectKingExtractor = Melds.rankOf(Rank.KING);
	Extractor<Meld, Melds> selectNotTwoExtractor = selectTwoExtractor.not();
	Extractor<Meld, Melds> selectNotAceExtractor = selectAceExtractor.not();
	Extractor<Meld, Melds> selectNotKingExtractor = selectKingExtractor.not();

	@Override
	public Cards requestingGivingCards() {
		try {
			myRank = this.rank();
			return TeruteruTradeCard.tradeCard(this);
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

	@Override
	public Meld requestingPlay() {
		try {
			// 自分の出したmeldをMyLastMeldに保存するためrequesting()を作成した
			Meld meld = requesting();
			MyLastMeld = meld;
			return meld;
		}catch (Exception e) {
			return MeldFactory.PASS;
		}
	}

	public Meld requesting() {
		averageCalc();
		// JOptionPane.showMessageDialog(null,average);

		// JOptionPane.showMessageDialog(null,"playerWonNum:"+playerWonNum);

		// 場が流れた直後か
		if (this.place().isRenew()) {
			return createRenewMeld();
		}

		// 場のmeldがMyLastMeldだったとき、パス
		// 自分以外にカードを出せるplayerがいない時、続けてカードを出さない
		// !?問題点　縛りのとき続けて出したほうがいいときもあるかも
		if (this.place().lastMeld() == MyLastMeld) {
			return MeldFactory.PASS;
		}

		Melds melds;

		// 場にJokerが出ている　かつ　S3を持っているとき　s3を出す
		Meld meldS3;
		if (this.place().order() == Order.NORMAL) {
			// JOKER が出ている時は、勝てないのであきらめる
			if (this.place().rank() == Rank.JOKER_HIGHEST) {
				// スペードの3を出す
				if (this.hand().contains(Card.S3)) {
					meldS3 = MeldFactory.createSingleMeld(Card.S3);
					melds = Melds.EMPTY_MELDS;
					melds = melds.add(meldS3);
					// JOptionPane.showMessageDialog(null,"s3");
					return adjustSizeTypeRankCheakEmptyCheakJoker(melds);
				}
			}
		} else {
			if (this.place().rank() == Rank.JOKER_LOWEST) {
				if (this.hand().contains(Card.S3)) {
					// スペードの3を出す
					meldS3 = MeldFactory.createSingleMeld(Card.S3);
					melds = Melds.EMPTY_MELDS;
					melds = melds.add(meldS3);
					// JOptionPane.showMessageDialog(null,"s3");
					return adjustSizeTypeRankCheakEmptyCheakJoker(melds);
				}
			}
		}

		// melds セット
		melds = Melds.parseMelds(this.hand());

		// これはmeldsをリセットしたときは、再びやったほうがいい
		// 場が縛られていれば,そのスートで縛る
		melds = selectSuits(this.place(), melds);
		// 上の処理で、この時点ではすでに場の縛りスート以外は除外されている

		// 自分の残りカードが2枚のとき 　かつ　場が単騎のとき
		if (this.handSize() == 2) {
			Melds lastTwo = melds;
			// 2枚あるうちJOKERカードが入っていたら
			if (this.place().size() == 1) {
				if (containsJoker(lastTwo)
						&& (playedCards[0][3] == true || this.hand().contains(
								Card.S3))) {
					// JOptionPane.showMessageDialog(null, "joker");
					lastTwo = Melds.parseMelds(createJokerMeld().asCards());
					melds = lastTwo;
				}
				// 2枚あるうち８のカードが入っていたら、８を出す
				else if (containsEight(lastTwo)) {
					// JOptionPane.showMessageDialog(null, "eight");
					lastTwo = lastTwo.extract(selectEightExtractor);
					melds = lastTwo;
				}
				if (!containsJoker(lastTwo)) {
					if (this.place().order() == Order.NORMAL) {
						// 2枚あるうちのランクの高いほうがMaxRankかどうか
						if (Cards.sort(this.hand()).get(1).rank() == MaxRank) {
							// JOptionPane.showMessageDialog(null, "selectMax");
							lastTwo = lastTwo.extract(Melds.MAX_RANK);
							melds = lastTwo;
						}
					} else {
						// 2枚あるうちのランクの低いほうがMinRankかどうか
						if (Cards.sort(this.hand()).get(0).rank() == MinRank) {
							// JOptionPane.showMessageDialog(null, "selectMin");
							lastTwo = lastTwo.extract(Melds.MIN_RANK);
							melds = lastTwo;
						}
					}
				}
			}
			return adjustSizeTypeRankCheakEmptyCheakJoker(melds);
		}

		// 自分の残りカードが3枚のとき
		if (this.handSize() == 3) {
			Melds lastThree = melds;
			// 場がペアのとき
			if (this.place().size() == 2) {
				// ペアの役だけにする
				lastThree = lastThree.extract(sizeTwoExtractor);
				// 3枚あるうちMaxPairRankのペア、カードが入っていたら、MaxPairRankのペアを出す
				// 　また、８のペアがあれば、８のペアを出す
				if (containsEight(lastThree)) {
					melds = lastThree;
				} else if (this.place().order() == Order.NORMAL) {
					// ペアがMaxPairRankがあるかどうか
					for (Meld meld : lastThree) {// 1回だけ処理
						if (meld.asCards().get(0).rank() == MaxPairRank) {
							melds = lastThree;
						}
					}
				} else if (this.place().order() == Order.REVERSED) {
					// ペアがMinPairRankがあるかどうか
					for (Meld meld : lastThree) {// 1回だけ処理
						if (meld.asCards().get(0).rank() == MinPairRank) {
							melds = lastThree;
						}
					}
				}

				// 場が単騎のとき
				// ペア　かつ　MaxRank のカードを持っている場合
				// 　1番強いカードと2番目に強いカードを持っているとき
			} else if (this.place().size() == 1) {
				// ペアを持っている場合
				if (containsPair(lastThree)) {
					// 3枚あるうち８のカードが入っていたら、８を出す
					if (containsEight(lastThree)) {
						// JOptionPane.showMessageDialog(null, "eight");
						lastThree = lastThree.extract(selectEightExtractor);
						melds = lastThree;
					}
					// 3枚あるうちJOKERのカードが入っている　and スペードの3が場に出ていない
					// Jokerを出す

					if (containsJoker(lastThree)
							&& (playedCards[0][3] == true || this.hand()
									.contains(Card.S3))) {
						Cards lastThree2 = this.hand();
						lastThree2 = lastThree2.remove(Card.JOKER);
						Melds lastThree3 = Melds.parseMelds(lastThree2);
						if (containsPair(lastThree3)) {
							// JOptionPane.showMessageDialog(null, "lastThree");
							lastThree = Melds.parseMelds(createJokerMeld()
									.asCards());
							melds = lastThree;
						}
					}
					// 3枚あるうちJOKERが入っていなかったら
					if (!containsJoker(lastThree)) {
						if (this.place().order() == Order.NORMAL) {
							// 3枚あるうちMaxRankのカードがあるかどうか
							for (Card card : this.hand()) {// 1回しか処理しない
								if (card.rank() == MaxRank) {
									// JOptionPane.showMessageDialog(null,"selectMax");
									lastThree = lastThree
											.extract(Melds.MAX_RANK);
									melds = lastThree;
									break;
								}
							}
						} else if (this.place().order() == Order.REVERSED) {
							// 3枚あるうちMinRankのカードがあるかどうか
							for (Card card : this.hand()) {// 1回しか処理しない
								if (card.rank() == MaxRank) {
									// JOptionPane.showMessageDialog(null,"selectMin");
									lastThree = lastThree
											.extract(Melds.MAX_RANK);
									melds = lastThree;
									break;
								}
							}
						}
					}
				}
			}
			return adjustSizeTypeRankCheakEmptyCheakJoker(melds);
		}

		// 強くなったかどうかはわからない　（５０００回のうちあんまり実行されない）
		// 自分の手札が4枚のとき　 かつ　場が３枚組のとき
		if (this.handSize() == 4) {
			Melds lastFour = melds;
			if (this.place().size() == 3) {
				// 4枚のうち3枚組を持つとき
				if (containsThreeGroup(melds)) {
					lastFour = lastFour.extract(sizeThreeExtractor);
					// JOptionPane.showMessageDialog(null, "lastFour");
					melds = lastFour;
				}
			}

			if (HowManyMyMaxCards() == 3) {
				lastFour = lastFour.extract(Melds.MAX_RANK);
				melds = lastFour;
			}

			return adjustSizeTypeRankCheakEmptyCheakJoker(melds);
		}

		// melds リセット
		// サイズが1の時に、ペア(Group)や階段のカードを対象外にする
		// Jokerも除外する
		// ただし、Ace や 2 は、バラバラで出す
		// これってまだ改善の余地があるかも
		if (this.place().size() == 1) {

			melds = selectSingleCandidates();

			// これはmeldsをリセットしたときは、再びやったほうがいい
			// 場が縛られていれば,そのスートで縛る
			melds = selectSuits(this.place(), melds);

			if (myRank == 5 && playerWonNum <= 1) {
				if (average < 8 && getNumberOfAllCards() > 12) {
					melds.extract(selectNotTwoExtractor);
					melds.extract(selectNotAceExtractor);
					melds.extract(selectNotKingExtractor);
				}
			}

			// if(MaxRank!=Rank.ACE && getNumberOfAllCards() > 20){
			// melds.extract(selectNotAceExtractor);
			// }
		}

		// melds リセット
		// サイズが2の時に、ペアで2とJOKERは出さない
		if (this.place().size() == 2) {
			Cards cardsTwo = this.hand();
			cardsTwo = cardsTwo.remove(Card.JOKER);
			melds = Melds.parseMelds(cardsTwo);

			melds = melds.extract(selectNotTwoExtractor);

			if (MaxPairRank != Rank.ACE) {
				melds.extract(selectNotAceExtractor);
			}

			// これはmeldsをリセットしたときは、再びやったほうがいい
			// 場が縛られていれば,そのスートで縛る
			melds = selectSuits(this.place(), melds);
		}

		// melds リセット
		// サイズが3の時に、3枚組で2とJOKERは出さない
		if (this.place().size() == 3) {
			Cards cardsThree = this.hand();
			cardsThree = cardsThree.remove(Card.JOKER);
			melds = Melds.parseMelds(cardsThree);

			melds = melds.extract(selectNotTwoExtractor);

			// これはmeldsをリセットしたときは、再びやったほうがいい
			// 場が縛られていれば,そのスートで縛る
			melds = selectSuits(this.place(), melds);
		}

		// 出せる役の中から最小ランク、最大サイズの役にする
		return adjustSizeTypeRankCheakEmptyCheakJoker(melds);

	}

	private Meld adjustSizeTypeRankCheakEmptyCheakJoker(Melds melds) {
		// サイズとタイプの合った役集合を抽出
		melds = adjustSizeType(melds);

		// ランクの合った役集合を抽出
		melds = adjustRank(melds);

		Melds meldsSuits = melds.extract(Melds.suitsOf(this.place().suits()));

		if (meldsSuits.size() != 0) {
			melds = meldsSuits;
		}

		if (melds.isEmpty()) {
			return MeldFactory.PASS;
		} else {
			return checkJoker(melds.get(0));
		}
	}

	/**
	 * これはmeldsをリセットしたときは、再びやったほうがいい 場が縛られていれば,そのスートで縛る
	 * 
	 * @param place
	 * @param melds
	 * @return　Melds
	 */
	private Melds selectSuits(Place place, Melds melds) {
		// 
		if (!place.lockedSuits().equals(Suits.EMPTY_SUITS)) {
			// 場を縛っているスート集合に適合する役を抽出して,候補とする．
			melds = melds.extract(Melds.suitsOf(place.lockedSuits()));
		}
		return melds;
	}

	/**
	 * meldsの中に4枚組があるときtrueを返す
	 */
	public boolean containsFourGroup(Melds melds) {
		melds = melds.extract(sizeFourExtractor);
		if (melds.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * meldsの中に3枚組があるときtrueを返す
	 */
	public boolean containsThreeGroup(Melds melds) {
		melds = melds.extract(sizeThreeExtractor);
		if (melds.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * meldsの中にペアがあるときtrueを返す
	 */
	public boolean containsPair(Melds melds) {
		melds = melds.extract(sizeTwoExtractor);
		if (melds.size() == 0) {
			return false;
		}
		return true;
	}

	public boolean containsGroup(Cards cards) {
		Melds melds = Melds.EMPTY_MELDS;
		melds = Melds.parseGroupMelds(cards);
		if (melds.size() == 0) {
			return false;
		}
		return true;
	}

	public static boolean containsEight(Cards cards) {
		// Cards...カード集合を指定すると、８を含むか判断
		if (cards.contains(Card.C8) || cards.contains(Card.S8)
				|| cards.contains(Card.D8) || cards.contains(Card.H8)) {
			return true;
		} else
			return false;
	}

	public static boolean containsEight(Melds melds) {
		// Melds...役集合を指定すると、８を含むか判断
		boolean frag = false;
		for (Meld oneMeld : melds) {
			if (containsEight(oneMeld.asCards())) {
				frag = true;
				return frag;
			}
		}
		return frag;
	}

	/**
	 * Cards...カード集合を指定すると、JOKERを含むか判断
	 * 
	 * @param cards
	 * @return boolean
	 */
	public static boolean containsJoker(Cards cards) {
		if (cards.contains(Card.JOKER)) {
			return true;
		} else
			return false;
	}

	/**
	 * Melds...役集合を指定すると、JOKERを含むか判断
	 * 
	 * @param melds
	 * @return boolean
	 */
	public static boolean containsJoker(Melds melds) {

		boolean frag = false;
		for (Meld oneMeld : melds) {
			if (containsJoker(oneMeld.asCards())) {
				frag = true;
				return frag;
			}
		}
		return frag;
	}

	Melds selectSingleCandidates() {
		Cards cards = this.hand();
		Cards strongerCards = Cards.EMPTY_CARDS;
		// JOKERを除いて、ACEと2をstrongerCardsに格納
		for (Card card : cards) {
			if (card == Card.JOKER) {
				// JOKER は加えない
				// strongerCards = strongerCards.add(card);
			} else if (card.rank().toInt() == Rank.EIGHT.toInt()) {
				strongerCards = strongerCards.add(card);
			} else if (this.place().order() == Order.NORMAL
					&& card.rank().toInt() > 13) {
				strongerCards = strongerCards.add(card);
			} else if (this.place().order() == Order.REVERSED
					&& card.rank().toInt() < 5) {
				strongerCards = strongerCards.add(card);
			}
		}

		// JokerとGroupと階段のカードは除外する
		cards = singleCards(cards);
		for (Card card : strongerCards) {
			if (!cards.contains(card)) {
				cards = cards.add(card);
			}
		}

		return Melds.parseMelds(cards);
	}

	/**
	 * Group や Sequence を構成するカードを除外して、単騎のカードだけを抽出
	 * 
	 * @param cards
	 *            元になるカード集合
	 * @return 単騎でしか利用できないカード集合
	 */
	Cards singleCards(Cards cards) {
		// JOKER は対象外にする
		cards = cards.remove(Card.JOKER);

		// Group 型の対象カードを削除
		Melds groupMelds = Melds.parseGroupMelds(cards);
		for (Meld meld : groupMelds) {
			cards = cards.remove(meld.asCards());
		}

		// Sequence 型の対象カードを削除
		Melds sequenceMelds = Melds.parseSequenceMelds(cards);
		for (Meld meld : sequenceMelds) {
			cards = cards.remove(meld.asCards());
		}

		return cards;
	}

	/**
	 * 場に出せるタイプとサイズに合わせた役集合に絞り込む
	 * 
	 * @param melds
	 *            役集合の候補
	 * @return 絞り込んだ役集合
	 */
	Melds adjustSizeType(Melds melds) {
		Extractor<Meld, Melds> sizeExtractor = Melds
				.sizeOf(this.place().size());// 抽出ルール設定
		// place.size()最後に場に出された役の枚数（int）を返すメソッド. 場が新しい場合NULLを返す.
		// Melds.sizeOf(int size)指定した枚数sizeに対して,
		// 役集合から,枚数がsizeに「等しい」役のみを抽出するための抽出子を返します

		Extractor<Meld, Melds> typeExtractor = Melds
				.typeOf(this.place().type());// 抽出ルール設定
		// place.type()最後に場に出された役の種類(Meld.Type)を返す.
		// Melds.typeOf(Meld.Type type)指定した役のタイプtypeに対して,
		// 役集合から,役のタイプがtypeに等しい役のみを抽出するための抽出子を返す．
		// Meld.Typeの定数(GROUP,PASS,SEQUENCE,SINGLE)
		return melds.extract(sizeExtractor.and(typeExtractor));
	}

	/**
	 * 場に出せるランクに合わせた役集合に絞り込む 場にJokerが出ていたら、s3を出す
	 * 
	 * @param melds
	 *            役集合の候補
	 * @return 絞り込んだ役集合
	 */
	Melds adjustRank(Melds melds) {

		// 革命を考慮しながら、出せるランクのカードに絞り込む\
		Rank rank = this.place().rank(); // 現在の場のランク
		Extractor<Meld, Melds> rankExtractor;
		if (this.place().order() == Order.NORMAL) {
			// JOKER が出ている時は、勝てないのであきらめる
			if (rank != Rank.JOKER_HIGHEST) {
				Rank oneRankHigh = rank.higher();
				rankExtractor = Melds.rankOf(oneRankHigh).or(
						Melds.rankOver(oneRankHigh));
				melds = melds.extract(rankExtractor);
				melds = melds.extract(Melds.MIN_RANK);
			} else if (this.hand().contains(Card.S3)) {
				// スペードの3を出す
				Meld meld = MeldFactory.createSingleMeld(Card.S3);
				melds = Melds.EMPTY_MELDS;
				return melds.add(meld);
			} else {
				melds = Melds.EMPTY_MELDS;
			}
		} else {
			if (rank != Rank.JOKER_LOWEST) {
				Rank oneRankHigh = rank.lower();
				rankExtractor = Melds.rankOf(oneRankHigh).or(
						Melds.rankUnder(oneRankHigh));
				melds = melds.extract(rankExtractor);
				melds = melds.extract(Melds.MAX_RANK);
			} else if (this.hand().contains(Card.S3)) {
				// スペードの3を出す
				Meld meld = MeldFactory.createSingleMeld(Card.S3);
				melds = Melds.EMPTY_MELDS;
				return melds.add(meld);
			} else {
				melds = Melds.EMPTY_MELDS;
			}
		}
		return melds;
	}

	/**
	 * 場が流れた直後の場に出すカードを決定 　最小ランクの最小サイズの役を返す
	 * 
	 * @param melds
	 *            手札で可能な役集合
	 * @return 場に出すカード
	 */
	Meld createRenewMeld() {
		Cards cards = this.hand();
		Melds melds = Melds.parseMelds(cards);

		// 自分の残りカードが2枚のとき 　かつ　ペアがないとき
		Melds lastTwo = melds;
		// 自分の残りカードが2枚のとき
		if (this.handSize() == 2) {
			// ペアがないとき
			if (!containsPair(lastTwo)) {
				// Jokerが入っていない場合（念のために入れておく）
				if (!containsJoker(lastTwo)) {
					// 2枚あるうち８のカードが入っていたら、８を出す
					if (containsEight(lastTwo)) {
						// JOptionPane.showMessageDialog(null, "eight");
						lastTwo = lastTwo.extract(selectEightExtractor);
						melds = lastTwo;
						return judgeRenewReturnMeld(melds);
					}
					if (this.place().order() == Order.NORMAL) {
						// 2枚あるうちのランクの高いほうがMaxRankかどうか
						if (Cards.sort(this.hand()).get(1).rank() == MaxRank) {
							// JOptionPane.showMessageDialog(null, "selectMax");
							lastTwo = lastTwo.extract(Melds.MAX_RANK);
							melds = lastTwo;
							return judgeRenewReturnMeld(melds);
						}
					} else if (this.place().order() == Order.REVERSED) {
						// 2枚あるうちのランクの低いほうがMinRankかどうか
						if (Cards.sort(this.hand()).get(0).rank() == MinRank) {
							// JOptionPane.showMessageDialog(null, "selectMin");
							lastTwo = lastTwo.extract(Melds.MIN_RANK);
							melds = lastTwo;
							return judgeRenewReturnMeld(melds);
						}
					}
				}
			}
		}
		// 自分の残りカードが3枚のとき
		if (this.handSize() == 3) {
			// 3枚組がないとき
			if (!containsThreeGroup(melds)) {
				Melds lastThree = melds;

				// ペアを持っている場合
				if (containsPair(melds)) {
					// 3枚あるうち８のカードが入っていたら、８を出す
					if (containsEight(melds)) {
						// JOptionPane.showMessageDialog(null, "eight");
						lastThree = lastThree.extract(selectEightExtractor);
						melds = lastThree;
						return judgeRenewReturnMeld(melds);
					}
					// Jokerが入っていない場合
					if (!containsJoker(lastThree)) {
						// 3枚あるうちMaxPairRankのペア、カードが入っていたら、MaxPairRankのペアを出す
						if (this.place().order() == Order.NORMAL) {
							// ペアがMaxPairRankがあるかどうか
							for (Meld meld : lastThree) {// 1回だけ処理
								if (meld.asCards().get(0).rank() == MaxPairRank) {
									lastThree = lastThree
											.extract(sizeTwoExtractor);
									melds = lastThree;
									return judgeRenewReturnMeld(melds);
								}
							}
						} else if (this.place().order() == Order.REVERSED) {
							// ペアがMinPairRankがあるかどうか
							for (Meld meld : lastThree) {// 1回だけ処理
								if (meld.asCards().get(0).rank() == MinPairRank) {
									lastThree = lastThree
											.extract(sizeTwoExtractor);
									melds = lastThree;
									return judgeRenewReturnMeld(melds);
								}
							}
						}
					}
					// 3枚あるうちJOKERが入っていなかったら
					if (!containsJoker(lastThree)) {
						if (this.place().order() == Order.NORMAL) {
							// 3枚あるうちMaxRankのカードがあるかどうか
							for (Card card : this.hand()) {
								if (card.rank() == MaxRank) {
									// JOptionPane.showMessageDialog(null,"selectMax");
									lastThree = lastThree
											.extract(Melds.MAX_RANK);
									melds = lastThree;
									return judgeRenewReturnMeld(melds);
								}
							}
						} else if (this.place().order() == Order.REVERSED) {
							// 3枚あるうちMinRankのカードがあるかどうか
							for (Card card : this.hand()) {
								if (card.rank() == MaxRank) {
									// JOptionPane.showMessageDialog(null,"selectMin");
									lastThree = lastThree
											.extract(Melds.MAX_RANK);
									melds = lastThree;
									return judgeRenewReturnMeld(melds);
								}
							}
						}
					}
				}
			}
		}

		// 強くなったかどうかはわからない　
		// 自分の手札が4枚のとき　
		if (this.handSize() == 4) {
			Melds lastFour = melds;
			// 4枚組がないとき
			if (!containsFourGroup(melds)) {
				// 4枚のうち3枚組を持つとき
				if (containsThreeGroup(melds)) {
					lastFour = lastFour.extract(sizeThreeExtractor);
					// JOptionPane.showMessageDialog(null, "lastFour");
					melds = lastFour;
					return judgeRenewReturnMeld(melds);
				}
			}

		}

		if (cards.size() > 2) {
			cards = cards.remove(Card.JOKER);
		}
		melds = Melds.parseMelds(cards);

		if (myRank == 5 && playerWonNum <= 1) {
			if (average < 8 && getNumberOfAllCards() > 12) {
				melds.extract(selectNotTwoExtractor);
				melds.extract(selectNotAceExtractor);
				melds.extract(selectNotKingExtractor);
			}
		}

		return judgeRenewReturnMeld(melds);
	}

	private Meld judgeRenewReturnMeld(Melds melds) {
		if (this.place().order() == Order.NORMAL) {
			melds = melds.extract(Melds.MIN_RANK);
		} else {
			melds = melds.extract(Melds.MAX_RANK);
		}
		melds = melds.extract(Melds.MAX_SIZE);
		return checkJoker(melds.get(0));
	}

	/**
	 * 最終的にmeldsをこのメソッドの引数に必ずする 最終候補が JOKER の単騎だったときJOKERを最強カードにする
	 * 
	 * @param meld
	 *            最終候補
	 * @return JOKER の単騎の時には、最強のJOKERを返却
	 */
	Meld checkJoker(Meld meld) {
		if (meld.type() == Meld.Type.SINGLE
				&& meld.asCards().get(0) == Card.JOKER) {
			return createJokerMeld();
		} else {
			return meld;
		}
	}

	Meld createJokerMeld() {
		// 場のスートに合わせた,最大のランクを持つ役に変更して,それを出す．
		Suit suit;
		if (this.place().suits() == Suits.EMPTY_SUITS) {
			// 取り合えず、スペードにしておく
			suit = Suit.SPADES;
		} else {
			suit = this.place().suits().get(0);
		}

		Rank rank;
		if (this.place().order() == Order.NORMAL) {
			rank = Rank.JOKER_HIGHEST;
		} else {
			rank = Rank.JOKER_LOWEST;
		}
		return MeldFactory.createSingleMeldJoker(suit, rank);
	}

	/**
	 * 他のプレイヤが皆パスをしたかどうか
	 * 
	 * @return 皆パスをしていたら true
	 */
	public boolean isOtherPlayerAllPassed() {
		for (int i = 0; i < 5; i++) {
			if (number() == i || playerWon[i])
				continue;
			if (!playerPassed[i])
				return false;
		}

		return true;
	}

	/**
	 * 全員の残り手札の合計
	 * 
	 * @return 枚数
	 */
	public int getNumberOfAllCards() {
		// プレイヤー全員の手札合計。減っていく。
		int[] allCards = playersInformation().numCardsOfPlayers();
		int total = 0;

		for (int cards : allCards) {
			total += cards;
		}

		return total;
	}

	/**
	 * 貧民戦かどうかを判定
	 * 
	 * @return 貧民戦ならtrue、そうでなければ false
	 */
	public boolean isHinminsen() {
		int total = 0;
		for (int i = 0; i < 5; i++) {
			if (number() == i || playerWon[i]) {
				continue;
			} else {
				total++;
			}
		}
		if (total > 1) {
			return false;
		} else {
			return true;
		}
	}

	public int numberOfStrongerCards(double rate) {
		boolean order = this.place().order() == Order.NORMAL;
		int rank = rankOfStrongerCards(rate);
		System.out.printf("stronger rank = %d%n", rank);

		Cards cards = this.hand();
		int count = 0;
		for (Card card : cards) {
			if (card.equals(Card.JOKER)) {
				count++;
			} else if (order && card.rank().toInt() >= rank) {
				count++;
			} else if (!order && card.rank().toInt() <= rank) {
				count++;
			}
		}

		return count;
	}

	public int rankOfStrongerCards(double rate) {
		int total = 0;
		int stronger = 0;

		if (!playedCards[0][0]) {
			total++;
			stronger++;
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 1; j <= 13; j++) {
				if (!playedCards[i][j]) {
					total++;
				}
			}
		}

		int target = (int) ((1.0 - rate) * total);
		boolean order = this.place().order() == Order.NORMAL;

		for (int j = (order ? 15 : 3); (order ? j >= 3 : j <= 15); j = j
				+ (order ? -1 : 1)) {
			int num = (j <= 13 ? j : j - 13);
			for (int i = 0; i < 4; i++) {
				if (!playedCards[i][num]) {
					stronger++;
					if (stronger > target) {
						return j;
					}
				}
			}
		}

		return (order ? 3 : 15);
	}

	/**
	 * 自分のカードが最強カードか判断する ただし、JOKER に対するスペードの3は考慮しない
	 * 
	 * @param card
	 *            手持ちカード
	 * @return 最強カードなら true、そうでなけえれば false。
	 */
	public boolean isStrongestCard(Card card) {
		if (card == Card.JOKER) {
			return true;
		}

		if (playedCards[0][0] == false) {
			// ジョーカーが未だ出ていなければ、最強ではない
			return false;
		}

		int rankNum = card.rank().toInt();
		boolean order = this.place().order() == Order.NORMAL;

		for (int i = rankNum + (order ? 1 : -1); (order ? i <= 15 : i >= 3); i = i
				+ (order ? 1 : -1)) {
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
	 * MaxRank
	 * 
	 * @return
	 */
	Rank selectMaxRank() {

		if (playedCards[0][0] == true) {
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][2] == false) {
					return Rank.TWO;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][1] == false) {
					return Rank.ACE;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][13] == false) {
					return Rank.KING;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][12] == false) {
					return Rank.QUEEN;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][11] == false) {
					return Rank.JACK;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][10] == false) {
					return Rank.TEN;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][9] == false) {
					return Rank.NINE;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][8] == false) {
					return Rank.EIGHT;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][7] == false) {
					return Rank.SEVEN;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][6] == false) {
					return Rank.SIX;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][5] == false) {
					return Rank.FIVE;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][4] == false) {
					return Rank.FOUR;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][3] == false) {
					return Rank.THREE;
				}
			}
		} else {
			if (this.place().order() == Order.NORMAL) {
				return Rank.JOKER_HIGHEST;
			} else {
				return Rank.JOKER_LOWEST;
			}
		}
		return Rank.JOKER_HIGHEST;
	}

	/**
	 * MinRank 但し、JOKERがある場合、JOKERがMinRank
	 */
	Rank selectMinRank() {
		if (playedCards[0][0] == true) {

			for (int i = 0; i < 4; i++) {
				if (playedCards[i][3] == false) {
					return Rank.THREE;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][4] == false) {
					return Rank.FOUR;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][5] == false) {
					return Rank.FIVE;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][6] == false) {
					return Rank.SIX;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][7] == false) {
					return Rank.SEVEN;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][8] == false) {
					return Rank.EIGHT;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][9] == false) {
					return Rank.NINE;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][10] == false) {
					return Rank.TEN;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][11] == false) {
					return Rank.JACK;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][12] == false) {
					return Rank.QUEEN;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][13] == false) {
					return Rank.KING;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][1] == false) {
					return Rank.ACE;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (playedCards[i][2] == false) {
					return Rank.TWO;
				}
			}

		} else {
			if (this.place().order() == Order.NORMAL) {
				return Rank.JOKER_HIGHEST;
			} else {
				return Rank.JOKER_LOWEST;
			}
		}
		return Rank.JOKER_LOWEST;
	}

	/**
	 * 2番に高いランクを返す
	 * 
	 * @return
	 */
	Rank selectMaxSecondRank() {
		int count = 0;
		if (playedCards[0][0] == false) {
			count++;
			;
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][2] == false) {
				count++;
				if (count == 2) {
					return Rank.TWO;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][1] == false) {
				count++;
				if (count == 2) {
					return Rank.ACE;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][13] == false) {
				count++;
				if (count == 2) {
					return Rank.KING;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][12] == false) {
				count++;
				if (count == 2) {
					return Rank.QUEEN;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][11] == false) {
				count++;
				if (count == 2) {
					return Rank.JACK;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][10] == false) {
				count++;
				if (count == 2) {
					return Rank.TEN;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][9] == false) {
				count++;
				if (count == 2) {
					return Rank.NINE;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][8] == false) {
				count++;
				if (count == 2) {
					return Rank.EIGHT;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][7] == false) {
				count++;
				if (count == 2) {
					return Rank.SEVEN;
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][6] == false) {
				count++;
				if (count == 2) {
					return Rank.SIX;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][5] == false) {
				count++;
				if (count == 2) {
					return Rank.FIVE;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][4] == false) {
				count++;
				if (count == 2) {
					return Rank.FOUR;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][3] == false) {
				count++;
				if (count == 2) {
					return Rank.THREE;
				}
			}
		}
		return null;
	}

	/**
	 * 2番目に低いランクを返す
	 * 
	 * @return
	 */
	Rank selectMinSecondRank() {
		int count = 0;

		if (playedCards[0][0] == false) {
			count++;
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][3] == false) {
				count++;
				if (count == 2) {
					return Rank.THREE;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][4] == false) {
				count++;
				if (count == 2) {
					return Rank.FOUR;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][5] == false) {
				count++;
				if (count == 2) {
					return Rank.FIVE;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][6] == false) {
				count++;
				if (count == 2) {
					return Rank.SIX;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][7] == false) {
				count++;
				if (count == 2) {
					return Rank.SEVEN;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][8] == false) {
				count++;
				if (count == 2) {
					return Rank.EIGHT;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][9] == false) {
				count++;
				if (count == 2) {
					return Rank.NINE;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][10] == false) {
				count++;
				if (count == 2) {
					return Rank.TEN;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][11] == false) {
				return Rank.JACK;
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][12] == false) {
				count++;
				if (count == 2) {
					return Rank.QUEEN;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][13] == false) {
				count++;
				if (count == 2) {
					return Rank.KING;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][1] == false) {
				count++;
				if (count == 2) {
					return Rank.ACE;
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			if (playedCards[i][2] == false) {
				count++;
				if (count == 2) {
					return Rank.TWO;
				}
			}
		}
		return null;
	}

	/**
	 * ランクの一番高いペアがあるとすれば、そのランクを返す（あくまで可能性） ペアが作成できない時はnullを返
	 * 
	 * @return
	 */
	Rank selectMaxPairRank() {
		int count = 0;
		int joker = 0;
		if (playedCards[0][0] == false) {
			joker = 1;
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][2] == false) {
				count++;
				if (count == 2) {
					return Rank.TWO;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][1] == false) {
				count++;
				if (count == 2) {
					return Rank.ACE;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][13] == false) {
				count++;
				if (count == 2) {
					return Rank.KING;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][12] == false) {
				count++;
				if (count == 2) {
					return Rank.QUEEN;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][11] == false) {
				count++;
				if (count == 2) {
					return Rank.JACK;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][10] == false) {
				count++;
				if (count == 2) {
					return Rank.TEN;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][9] == false) {
				count++;
				if (count == 2) {
					return Rank.NINE;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][8] == false) {
				count++;
				if (count == 2) {
					return Rank.EIGHT;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][7] == false) {
				count++;
				if (count == 2) {
					return Rank.SEVEN;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][6] == false) {
				count++;
				if (count == 2) {
					return Rank.SIX;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][5] == false) {
				count++;
				if (count == 2) {
					return Rank.FIVE;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][4] == false) {
				count++;
				if (count == 2) {
					return Rank.FOUR;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][3] == false) {
				count++;
				if (count == 2) {
					return Rank.THREE;
				}
			}
		}
		return null;
	}

	/**
	 * ランクの一番低いペアがあるとすれば、そのランクを返す（あくまで可能性） ペアが作成できない時はnullを返す
	 * 
	 * @return
	 */
	Rank selectMinPairRank() {
		int count = 0;
		int joker = 0;
		if (playedCards[0][0] == false) {
			joker = 1;
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][3] == false) {
				count++;
				if (count == 2) {
					return Rank.THREE;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][4] == false) {
				count++;
				if (count == 2) {
					return Rank.FOUR;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][5] == false) {
				count++;
				if (count == 2) {
					return Rank.FIVE;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][6] == false) {
				count++;
				if (count == 2) {
					return Rank.SIX;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][7] == false) {
				count++;
				if (count == 2) {
					return Rank.SEVEN;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][8] == false) {
				count++;
				if (count == 2) {
					return Rank.EIGHT;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][9] == false) {
				count++;
				if (count == 2) {
					return Rank.NINE;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][10] == false) {
				count++;
				if (count == 2) {
					return Rank.TEN;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][11] == false) {
				return Rank.JACK;
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][12] == false) {
				count++;
				if (count == 2) {
					return Rank.QUEEN;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][13] == false) {
				count++;
				if (count == 2) {
					return Rank.KING;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][1] == false) {
				count++;
				if (count == 2) {
					return Rank.ACE;
				}
			}
		}
		count = joker;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][2] == false) {
				count++;
				if (count == 2) {
					return Rank.TWO;
				}
			}
		}
		return null;
	}

	/**
	 * あるrankのカードが何枚あるのか返す関数
	 * 
	 * @param rank
	 * @return
	 */
	int HowManyRank(Rank rank) {

		if (rank == Rank.JOKER_HIGHEST || rank == Rank.JOKER_LOWEST) {
			return 1;
		}

		int rankNum = rank.toInt();

		if (rankNum == 14) {
			rankNum = 1;
		} else if (rankNum == 15) {
			rankNum = 2;
		}

		int count = 0;
		for (int i = 0; i < 4; i++) {
			if (playedCards[i][rankNum] == false) {
				count++;
			}
		}
		return count;
	}

	int HowManyMyMaxCards() {
		int count = 0;
		if (MaxRank == MaxSecondRank) {
			for (Card card : this.hand()) {
				try {
					if (card.rank() == MaxRank) {
						count++;
					}

				} catch (Exception e) {
				}
			}
		} else {
			for (Card card : this.hand()) {
				try {
					if (card.rank() == MaxRank) {
						count++;
					}

				} catch (Exception e) {
				}
			}
			for (Card card : this.hand()) {
				try {
					if (card.rank() == MaxSecondRank) {
						count++;
					}

				} catch (Exception e) {
				}
			}
		}
		return count;
	}

	/**
	 * SPADEの中で一番強いrankを返す
	 * 
	 * @return
	 */
	Rank StrongestCardOfSPADE() {
		if (playedCards[0][2] == false) {
			return Rank.TWO;
		} else if (playedCards[0][1] == false) {
			return Rank.ACE;
		} else if (playedCards[0][13] == false) {
			return Rank.ACE;
		} else if (playedCards[0][12] == false) {
			return Rank.ACE;
		} else if (playedCards[0][11] == false) {
			return Rank.ACE;
		} else if (playedCards[0][10] == false) {
			return Rank.ACE;
		} else if (playedCards[0][9] == false) {
			return Rank.ACE;
		} else if (playedCards[0][8] == false) {
			return Rank.ACE;
		} else if (playedCards[0][7] == false) {
			return Rank.ACE;
		} else if (playedCards[0][6] == false) {
			return Rank.ACE;
		} else if (playedCards[0][5] == false) {
			return Rank.ACE;
		} else if (playedCards[0][4] == false) {
			return Rank.ACE;
		} else if (playedCards[0][3] == false) {
			return Rank.ACE;
		}
		return null;
	}

	/**
	 * HEARTの中で一番強いrankを返す
	 * 
	 * @return
	 */
	Rank StrongestCardOfHEART() {
		if (playedCards[1][2] == false) {
			return Rank.TWO;
		} else if (playedCards[1][1] == false) {
			return Rank.ACE;
		} else if (playedCards[1][13] == false) {
			return Rank.ACE;
		} else if (playedCards[1][12] == false) {
			return Rank.ACE;
		} else if (playedCards[1][11] == false) {
			return Rank.ACE;
		} else if (playedCards[1][10] == false) {
			return Rank.ACE;
		} else if (playedCards[1][9] == false) {
			return Rank.ACE;
		} else if (playedCards[1][8] == false) {
			return Rank.ACE;
		} else if (playedCards[1][7] == false) {
			return Rank.ACE;
		} else if (playedCards[1][6] == false) {
			return Rank.ACE;
		} else if (playedCards[1][5] == false) {
			return Rank.ACE;
		} else if (playedCards[1][4] == false) {
			return Rank.ACE;
		} else if (playedCards[1][3] == false) {
			return Rank.ACE;
		}
		return null;
	}

	/**
	 * CLUBの中で一番強いrankを返す
	 * 
	 * @return
	 */
	Rank StrongestCardOfCLUB() {
		if (playedCards[2][2] == false) {
			return Rank.TWO;
		} else if (playedCards[2][1] == false) {
			return Rank.ACE;
		} else if (playedCards[2][13] == false) {
			return Rank.ACE;
		} else if (playedCards[2][12] == false) {
			return Rank.ACE;
		} else if (playedCards[2][11] == false) {
			return Rank.ACE;
		} else if (playedCards[2][10] == false) {
			return Rank.ACE;
		} else if (playedCards[2][9] == false) {
			return Rank.ACE;
		} else if (playedCards[2][8] == false) {
			return Rank.ACE;
		} else if (playedCards[2][7] == false) {
			return Rank.ACE;
		} else if (playedCards[2][6] == false) {
			return Rank.ACE;
		} else if (playedCards[2][5] == false) {
			return Rank.ACE;
		} else if (playedCards[2][4] == false) {
			return Rank.ACE;
		} else if (playedCards[2][3] == false) {
			return Rank.ACE;
		}
		return null;
	}

	/**
	 * DIAMONDSの中で一番強いrankを返す
	 * 
	 * @return
	 */
	Rank StrongestCardOfDIAMONDS() {
		if (playedCards[3][2] == false) {
			return Rank.TWO;
		} else if (playedCards[3][1] == false) {
			return Rank.ACE;
		} else if (playedCards[3][13] == false) {
			return Rank.ACE;
		} else if (playedCards[3][12] == false) {
			return Rank.ACE;
		} else if (playedCards[3][11] == false) {
			return Rank.ACE;
		} else if (playedCards[3][10] == false) {
			return Rank.ACE;
		} else if (playedCards[3][9] == false) {
			return Rank.ACE;
		} else if (playedCards[3][8] == false) {
			return Rank.ACE;
		} else if (playedCards[3][7] == false) {
			return Rank.ACE;
		} else if (playedCards[3][6] == false) {
			return Rank.ACE;
		} else if (playedCards[3][5] == false) {
			return Rank.ACE;
		} else if (playedCards[3][4] == false) {
			return Rank.ACE;
		} else if (playedCards[3][3] == false) {
			return Rank.ACE;
		}
		return null;
	}

	// 保留

	// 現在、強いカードを何枚もっているのか
	// int HowManyMaxCard(Cards cards){
	// int count=0;
	// if(cards.contains(Card.JOKER) && playedCards[0][3] == false) {
	// count++;
	// }
	// for()
	// }

	void averageCalc() {

		int total = 0;
		for (Card card : this.hand()) {
			if (card.equals(Card.JOKER)) {
				// joker = true;
				if (this.place().order() == Order.NORMAL) {
					total += 16;
				} else {
					total += 2;
				}
			} else {
				total += card.rank().toInt();
			}
		}
		average = (double) total / this.hand().size();
	}

	/**
	 * 誰かが場に札を出す、あるいは、パスした時に呼び出されるメソッド (おそらくカードを出す前)
	 */
	@Override
	public void played(java.lang.Integer number, Meld playedMeld) {
		super.played(number, playedMeld);

		// System.out.println(number + "さんが" + playedMeld.toString() +
		// "を出しました。");

		// 誰がパスをしたのか記録
		if (playedMeld.asCards().isEmpty()) {
			playerPassed[number] = true;
		}

		// 出されたカードを記録
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
				// JOptionPane.showMessageDialog(null,card.suit()+"//"+suit.ordinal());
			}

		}

		// MaxRankを記録
		MaxRank = selectMaxRank();
		// selectMaxRank()の確認
		// JOptionPane.showMessageDialog(null,MaxRank);

		MinRank = selectMinRank();
		// selectMinRank()の確認
		// JOptionPane.showMessageDialog(null,MinRank);

		MaxPairRank = selectMaxPairRank();
		// JOptionPane.showMessageDialog(null,MaxPairRank);

		MinPairRank = selectMinPairRank();
		// JOptionPane.showMessageDialog(null, MinPairRank);

		MaxSecondRank = selectMaxSecondRank();
		// JOptionPane.showMessageDialog(null, MaxSecondRank);

		MinSecondRank = selectMinSecondRank();
		// JOptionPane.showMessageDialog(null, MinSecondRank);
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
		playerWonNum = 0;
	}

	/**
	 * プレイヤが勝利したときに呼び出されるメソッド
	 */
	@Override
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);
		playerWon[number] = true;
		playerWonNum++;
	}
}