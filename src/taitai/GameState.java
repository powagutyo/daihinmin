package taitai;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import taitai.util.*;
import jp.ac.uec.daihinmin.Place;
import jp.ac.uec.daihinmin.card.*;

public class GameState {
	private Cards alreayPutedCards;
	private Cards hasOtherPlayersCards;
	private OwnState ownCardsState;
	private boolean findedWinStrategy;
	private Meld lastPostMeld;
	private int notWonPlayerCount = 0;
	
	public GameState(Cards ownCards) {
		setUp(ownCards);
	}
	
	public void setUp(Cards ownCards) {
		hasOtherPlayersCards = CardsUtility.getAllCards().remove(ownCards);
		alreayPutedCards = ownCards;
		ownCardsState = new OwnState(ownCards);
//		System.out.println("持ち札");
//		System.out.println(ownCardsState);
	}
	
	public boolean isOtherPlayerHasJoker() {
		return hasOtherPlayersCards.contains(Card.JOKER);
	}
	
	public void otehrPlayerPlay(Cards cards) {
		alreayPutedCards = alreayPutedCards.add(cards);
		hasOtherPlayersCards = hasOtherPlayersCards.remove(cards);
	}
	
//	/**
//	 * 他のプレイヤーが皆passをして，場が流れるかを判定
//	 * @param meld
//	 * @param isReverse
//	 * @param isSuitLock
//	 * @return
//	 */
//	public boolean willotherPlayerPass(Meld meld, boolean isReverse, boolean isSuitLock) {
//		
//		return false;
//	}
	
	public String toString() {
		return hasOtherPlayersCards.toString()+"\n"+ownCardsState;
	}
	
	/**
	 * placeであるときの，戦略
	 * ・できる限り縛る
	 * 
	 * 目標
	 * ・できる限り，弱いカードと強いカードのバランスがとれた出し方をする．
	 * @param place
	 */
	public Meld requestingPlay(Place place) {
		
		// 必ずあがれる戦略があるとき，
		if(findedWinStrategy) {
			if(!place.isRenew()) {
				if(place.type() == Meld.Type.SINGLE && place.lastMeld().asCards().contains(Card.JOKER)) {
//					System.out.println("OK");
//					System.exit(0);
					return ownCardsState.getWinningStrategeLastMeld();
				}
//				System.out.println("error3");
//				System.out.println(place);
//				System.out.println(ownCardsState.getNowCards());
//				for(int i = 0; i < ownCardsState.size(); i++) {
//					System.out.println("#"+ownCardsState.get(i));
//				}
//				System.exit(0);
			}
			Meld meld = playWinningStrategy(place);
//			if(meld == null) {
//				System.out.println("error");
//				System.exit(0);
//			}
			return meld;
		}
		
		if(place.isRenew()) {
			Meld meld = playRenew(place);
			if(meld != null) return meld;
			if(meld == null) {
				if(ownCardsState.contains(Card.JOKER))
					return MeldUtility.createSingleJoker(place);
//				System.out.println("error2");
//				System.out.println(ownCardsState);
//				System.exit(0);
			}
		}
		List<Meld> melds = ownCardsState.requestingPlay(place);
		
		if(melds.isEmpty()) return null;
		
		// 必ずあがれる戦略があるか探索
		Meld winStrategy = findWiningStrategy(melds, place);
		if(winStrategy != null) {
			System.out.println("あがり確定！");
			return winStrategy;
		}
		
		MeldsUtility.sort(melds);
		melds = MeldsUtility.leastLowerMeld(melds, place);
		
		for(Meld meld : melds) {
			if(Utility.isLockedSuits(place, meld)) {
				return meld;
			}
		}
		
		return melds.get(0);
	}
	
	private Meld playWinningStrategy(Place place) {
		if(ownCardsState.getWinningStrategeMeldsSize() > 1) {
			return ownCardsState.getWinningStrategyMeld();
		} else {
			Meld meld = ownCardsState.getWinningStrategyMeld();
			if(ownCardsState.contains(Card.JOKER) && 
					!meld.asCards().contains(Card.JOKER)) {
				return MeldUtility.createAddJokerMeld(meld);
			}
			return meld;
		}
	}
	
	private Meld playRenew(Place place) {
		if(!place.isReverse()) {
			if(ownCardsState.size() == 1) {
				if(ownCardsState.contains(Card.JOKER) && 
						!ownCardsState.get(0).asCards().contains(Card.JOKER)) {
					Meld meld = ownCardsState.get(0);
					if(meld.type() == Meld.Type.SEQUENCE) {
						return MeldUtility.createSequence(meld.asCards(), meld.rank());
					} else {
						return MeldUtility.createGroupWithJoker(meld.asCards());
					}
				}
				return ownCardsState.get(0);
//			} else if(ownCardsState.size() > 5) {
//				return ownCardsState.get(1);
			} else if(ownCardsState.size() > 1) {
				// 自身がjokerを持っていて，まだ出してないならS3は単品では出さない
				if(ownCardsState.hasSingleUseJoker() && 
						ownCardsState.get(0).type() == Meld.Type.SINGLE && 
						ownCardsState.get(0).asCards().contains(Card.S3))
					return ownCardsState.get(1);
//				// jokerが他のプレイヤーが持っていてまだ場に出ていないのなら，S3は出さない
//				Meld meld = ownCardsState.get(0);
//				if(MeldUtility.contains(meld, Card.S3) && 
//						hasOtherPlayersCards.contains(Card.JOKER))
//					return ownCardsState.get(1);
				return ownCardsState.get(0);
			} else if(ownCardsState.contains(Card.JOKER))
				return MeldUtility.getSingleMeldWithOnlyJoker(place);
			else return null;
		} else {
			if(ownCardsState.size() == 1)
				return ownCardsState.get(0);
			else return ownCardsState.get(ownCardsState.size() - 1);
		}
	}
	
	private Meld findWiningStrategy(List<Meld> melds, Place place) {
		// ある出し方をすると，誰も一手も出せずにあがれるか判定
		
		for(Meld meld : melds) {
			// そのMeldをだすことによって，場が流れるか判定，流れないならcontinue
			boolean isSuitLock = false;
			if(!place.isRenew() && place.lastMeld().suits().equals(meld.suits())) {
				isSuitLock = true;
			}
			
			if(isOtherPlayerExistingAvailableMelds(meld, isSuitLock, place.isReverse()) &&
					!(meld.type() == Meld.Type.SINGLE && 
							hasOtherPlayersCards.contains(Card.JOKER) && 
							!meld.asCards().contains(Card.S3) &&
							ownCardsState.contains(Card.S3)))
				continue;
			
			
			// このmeldを出した後に残ったMelds
			List<Meld> newMelds;
			int count;
			Meld lastPostMeldtmp = null;
			if(meld.type() == Meld.Type.SINGLE && 
					hasOtherPlayersCards.contains(Card.JOKER) && 
					ownCardsState.contains(Card.S3)) {
				newMelds = 
					ownCardsState.removeCardsMeldsAndAddLeastCardsToNewMelds(
							meld.asCards().add(Card.S3));
				count = 1;
				lastPostMeldtmp = MeldUtility.S3;
			} else {
				newMelds = 
					ownCardsState.removeCardsMeldsAndAddLeastCardsToNewMelds(
							meld.asCards());
				count = 0;
			}
			
			if(newMelds.size() == 0) {
				return meld;
			}
			
			// 一役を除いて，残りの役が誰の邪魔もされずに出せるなら出す．
			Collections.sort(newMelds, new Comparator<Meld>(){
				public int compare(Meld o1, Meld o2) {
					return o1.asCards().size() - o2.asCards().size();
				}});
			for(Meld m : newMelds) {
				if(isOtherPlayerExistingAvailableMelds(m, false, place.isReverse())) {
					count++;
					lastPostMeldtmp = m;
				}
			}
			if(count <= 1) {
				findedWinStrategy = true;
				if(newMelds.size() > 0) {
					if(count == 0) {
						lastPostMeld = newMelds.get(newMelds.size() - 1);
					} else {
						lastPostMeld = lastPostMeldtmp;
					}
				}
				ownCardsState.setWinningStrategyMelds(newMelds, meld, lastPostMeld);
				
//				System.out.println("place : "+place);
//				System.out.println("相手手札:"+hasOtherPlayersCards);
//				System.out.println("手札："+ownCardsState.getNowCards());
//				for(Meld m : newMelds) {
//					System.out.println("%"+m);
//				}
//				System.out.println("lastPostMeld : "+lastPostMeld);
				return meld;
			}
		}
		return null;
	}
	
	/**
	 * ownMeldを出したとして，他のプレイヤーがこの上に出す可能性があるか否かを返す．
	 * @param ownMeld
	 * @param isSuitlock
	 * @param isReverse
	 * @return
	 */
	public boolean isOtherPlayerExistingAvailableMelds(
			Meld ownMeld, boolean isSuitlock, boolean isReverse) {
		Melds list = Melds.EMPTY_MELDS;
		// ownMeldに8が含まれているか判定
		if(MeldUtility.containEight(ownMeld))
			return false;
		
		if(ownMeld.type() == Meld.Type.SINGLE && 
				hasOtherPlayersCards.contains(Card.JOKER))
			return true;
		
		if(ownMeld.type() == Meld.Type.SEQUENCE) {
			list = MeldsUtility.createSequenceAllMelds(
					hasOtherPlayersCards, ownMeld.asCards().size());
		} else if(ownMeld.type() == Meld.Type.GROUP) {
			list = MeldsUtility.createGroupAllMelds(
					hasOtherPlayersCards, ownMeld.asCards().size());
		} else if(ownMeld.type() == Meld.Type.SINGLE) {
			list = MeldsUtility.createSingleMeldsNotWithJoker(hasOtherPlayersCards);
		}
		for(Meld meld : list) {
			if(Utility.isAcceptMeld(meld, ownMeld, isReverse, 
					isSuitlock ? ownMeld.suits() : Suits.EMPTY_SUITS)) {
				return true;
			}
		}
		
//		if(ownMeld.type() == Meld.Type.SINGLE) {
//			Cards cards = ownMeld.asCards();
//			if(!cards.contains(Card.JOKER) 
//					&& hasOtherPlayersCards.contains(Card.JOKER) 
//					&& ownCardsState.contains(Card.S3) 
//					&& !cards.contains(Card.S3)) {
//				return false;
//			}
//			return hasOtherPlayersCards.contains(Card.JOKER);
//		}
		return false;
	}
	
	public void ownAcceptedPutMeld(Meld meld) {
		if(meld.type() == Meld.Type.PASS) return;
		if(findedWinStrategy) {
			ownCardsState.removeWinningStrategeMeldTop(meld);
		} else {
			ownCardsState.remove(meld);
		}
	}
	
	public boolean containsOwnCard(Card card) {
		return ownCardsState.contains(card);
	}
	
	public Cards requestingGivingCards(int size) {
		return ownCardsState.requestingGivingCards(size);
	}
	
	public void playerWon(java.lang.Integer number) {
		notWonPlayerCount++;
	}
	
	public int getWonPlayerCount() {
		return notWonPlayerCount;
	}
	
	public Cards getLeastCards() {
		return hasOtherPlayersCards;
	}
	
	public Cards getOwnCards() {
		return ownCardsState.getNowCards();
	}
	
	public boolean check(Cards handCards) {
		if(handCards.equals(ownCardsState.getNowCards()))
			return true;
		else {
//			System.out.println("実際　："+handCards);
//			System.out.println("間違い："+ownCardsState.getNowCards());
			return false;
		}
	}
	
	public void ownCardsReset(Cards hand) {
		ownCardsState.reset(hand);
	}
	
	public Meld getReverseMeld() {
		return ownCardsState.getRverseMeld();
	}
	
//	public boolean isHighRankCard(Meld meld) {
//		
//	}
	
	public boolean isBalancing() {
		return ownCardsState.isBalancing();
	}
	
	public boolean isFindedWinningStrategy() {
		return findedWinStrategy;
	}
}
