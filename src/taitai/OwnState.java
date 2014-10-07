package taitai;

import java.util.*;

import taitai.util.*;

import jp.ac.uec.daihinmin.Place;
import jp.ac.uec.daihinmin.card.*;


/**
 * 
 * @author tai
 * 現在持っているカードで作る役の集合
 * 
 * 役の集合を生成する際は
 * ・連続は4枚の連続までにする（保留）
 * １．可能な連続の組み合わせを確保する
 * ２．可能な複数枚出しの組み合わせを確保する
 */
public class OwnState {
	private Cards nowCards;
	private Melds nowMelds;
	private Map<Integer,List<Meld>> groups = new HashMap<Integer, List<Meld>>();
	private Map<Integer,List<Meld>> sequences = new HashMap<Integer, List<Meld>>();
	private LinkedList<Meld> winningStrategyMelds = new LinkedList<Meld>();
	private boolean hasJoker;
	private int highMeld, lowMeld;
	
	public OwnState(Cards cards) {
		reset(cards);
	}
	
	public void clear() {
		nowCards = Cards.EMPTY_CARDS;
		nowMelds = Melds.EMPTY_MELDS;
		groups.clear();
		sequences.clear();
		highMeld = lowMeld = 0;
		winningStrategyMelds.clear();
	}
	
	public void reset(Cards cards) {
//		System.out.println("Reset");
		clear();
		
		nowCards = cards;
		nowMelds = MeldsUtility.createSequenses(nowCards);
		Cards tmpCards = CardsUtility.remove(nowCards, nowMelds);
		Melds tmpMelds = MeldsUtility.createGroup(tmpCards);
		tmpCards = CardsUtility.remove(tmpCards, tmpMelds);
		nowMelds = nowMelds.add(tmpMelds);
		hasJoker = tmpCards.contains(Card.JOKER);
		
		nowMelds = MeldsUtility.sort(nowMelds);
		groups.clear();
		sequences.clear();
		for(Meld meld : nowMelds) {
			add(meld);
			if(MeldUtility.isHighMeld(meld))
				highMeld++;
			else if(MeldUtility.isLowMeld(meld))
				lowMeld++;
		}
//		if(!check()) {
//			System.out.println("meldsの異常");
//			System.out.println(nowCards);
//			System.out.println(nowMelds);
//			System.exit(0);
//		}
	}
	
//	/**
//	 * おかしなMeldsになっていないかチェック
//	 * @return
//	 */
//	private boolean check() {
//		// jokerの枚数がおかしくなっていないか
//		int jokerCount = 0;
//		for(Meld meld : nowMelds) {
//			if(meld.asCards().contains(Card.JOKER))
//				jokerCount++;
//		}
//		if(jokerCount > 1) return false;
//		if(jokerCount == 1 && !nowCards.contains(Card.JOKER))
//			return false;
//		
//		return true;
//	}
	
	
	private void add(Meld target) {
		int size = target.asCards().size();
		if(size < 3 || target.type() != Meld.Type.SEQUENCE) {
			if(!groups.containsKey(size)) {
				groups.put(size, new LinkedList<Meld>());
			}
			groups.get(size).add(target);
		} else {
			if(!sequences.containsKey(size)) {
				sequences.put(size, new LinkedList<Meld>());
			}
			sequences.get(size).add(target);
		}
	}
	
	/**
	 * 交換相手に渡すとしたら，どんなカードが良いか
	 * @param size : 渡すカードの枚数
	 * @return 相手に渡すカード
	 * 
	 * 戦略
	 * ・Joker,2,1,8,K,Q,Jは渡さない!
	 * ・Jokerとスペードの3を持っているなら
	 * 		Jokerを一枚出しとして出す戦略をとるときは，スペードの3を必ず残す
	 * 		Jokerを間違いなく複数枚出しに使うときには，スペードの3を渡す候補に入れる
	 * ・一枚組で8を除く10以下のカードが
	 */
	public Cards requestingGivingCards(int size) {
//		Melds melds = Melds.sort(nowMelds, new Comparator<Meld>(){
//			public int compare(Meld o1, Meld o2) {
//				if(o1.type() != o2.type()) {
//					if(o1.type() == Meld.Type.SINGLE) return -1;
//					else if(o2.type() == Meld.Type.SEQUENCE) return -1;
//					else if(o2.type() == Meld.Type.SINGLE) return 1;
//					else return 1;
//				}
//				if(o1.asCards().size() != o2.asCards().size())
//					return o1.asCards().size() - o2.asCards().size();
//				if(o1.asCards().size() == 1 && o1.asCards().contains(Card.JOKER))
//					return 1;
//				if(o2.asCards().size() == 1 && o2.asCards().contains(Card.JOKER))
//					return -1;
//				return o1.rank().compareTo(o2.rank());
//			}});
		Cards single = Cards.EMPTY_CARDS;
		Cards group = Cards.EMPTY_CARDS;
		Cards result = Cards.EMPTY_CARDS;
		for(Meld meld : nowMelds) {
			if(meld.type() == Meld.Type.SINGLE && 
					!meld.asCards().contains(Card.JOKER) && 
					RankUtility.useLowRank(meld.asCards().get(0).rank()))
				if(hasJoker && meld.asCards().contains(Card.S3)) continue;
				single = single.add(meld.asCards());
			if(meld.type() == Meld.Type.GROUP &&
					RankUtility.useLowRank(meld.asCards().get(0).rank())) {
				if(meld.asCards().contains(Card.D3)) continue;
				group = group.add(meld.asCards());
			}
		}
		single = Cards.sort(single);
		group = Cards.sort(group);
		if(single.size() >= size) {
			for(int i = 0; i < size; i++) {
				result = result.add(single.get(i));
			}
			return result;
		} else if(single.size() + group.size() >= size){
			for(int i = 0; i < single.size(); i++) {
				result = result.add(single.get(i));
			}
			for(int i = 0; i < size - single.size(); i++) {
				result = result.add(group.get(i));
			}
			return result;
		} else {
//			System.out.println(nowCards);
//			System.exit(0);
		}
		return result;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(nowCards+"\n");
		for(List<Meld> melds : sequences.values()) {
			for(Meld meld : melds) sb.append(meld+"\n");
		}
		for(List<Meld> melds : groups.values()) {
			for(Meld meld : melds) sb.append(meld+"\n");
		}
		return sb.toString();
	}
	
	public void playAccepted(Meld playMeld) {
		reset(nowCards.remove(playMeld.asCards()));
	}
	
	public int size() {
		return nowMelds.size();
	}
	
	public Meld get(int index) {
		try{
			return nowMelds.get(index);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public Cards getNowCards() {
		return nowCards;
	}
	
	public void remove(Meld meld) {
		nowCards = nowCards.remove(meld.asCards());
		reset(nowCards);
//		remove1(meld);
	}
	
//	private void remove1(Meld meld) {
//		Melds tmp = nowMelds.remove(meld);
//		if(tmp.size() != nowMelds.size()) {
//			// 分割して出した
//			reset(nowCards);
//			return;
//		}
//		
//		nowMelds = tmp;
//		if(meld.type() == Meld.Type.SEQUENCE) {
//			List<Meld> sequence = sequences.get(meld.asCards().size());
//			if(sequence != null) {
//				for(int i = 0; i < sequence.size(); i++) {
//					if(sequence.get(i).equals(meld)) {
//						sequence.remove(i);
//						break;
//					}
//				}
//			}else {
//				reset(nowCards);
//			}
//		} else {
//			List<Meld> group = groups.get(meld.asCards().size());
//			if(group != null) {
//				for(int i = 0; i < group.size(); i++) {
//					if(group.get(i).equals(meld)) {
//						group.remove(i);
//						break;
//					}
//				}
//			} else {
//				reset(nowCards);
//			}
//		}
////		System.out.println(toString());
//	}
	
	public List<Meld> requestingPlay(Place place) {
		Meld lastMeld = place.lastMeld();
		List<Meld> melds;
		if(lastMeld.type() == Meld.Type.SEQUENCE) {
			melds = Utility.getAcceptMelds(sequences.get(lastMeld.asCards().size()), place);
		} else {
			melds = Utility.getAcceptMelds(groups.get(lastMeld.asCards().size()), place);
		}
		if(melds.size() > 0) return melds;
		
		for(Meld meld : nowMelds) {
			if(lastMeld.asCards().size() < meld.asCards().size())
				melds.addAll(MeldUtility.extracteMeldWithoutJoker(meld, place));
		}
		
//		System.out.println("$"+melds.size());
		List<Meld> tmpMeld = MeldsUtility.removeJokerUseMeld(melds);
		// lowは分割には使わない
		tmpMeld = MeldsUtility.removeLowCardsUseMeld(tmpMeld, place);
//		System.out.println("%"+tmpMeld.size());
		if(tmpMeld.size() > 0) return tmpMeld;
		return melds;
	}
	
	public boolean contains(Card card) {
		return nowCards.contains(card);
	}

	public List<Meld> removeCardsMeldsAndAddLeastCardsToNewMelds(Cards cards) {
		List<Meld> result = new LinkedList<Meld>();
		Cards leastCards = Cards.EMPTY_CARDS;
		for(Meld meld : nowMelds) {
			if(MeldUtility.contain(meld, cards)) {
				leastCards = leastCards.add(meld.asCards().remove(cards));
			} else {
				result.add(meld);
			}
		}
//		System.out.println("崩れたカード:"+leastCards);
		
		if(hasJoker) {
			leastCards = leastCards.add(Card.JOKER);
			hasJoker = false;
		}
		Melds melds = MeldsUtility.createSequenses(leastCards);
		leastCards = CardsUtility.remove(leastCards, melds);
		melds = melds.add(MeldsUtility.createGroup(leastCards.remove(Card.JOKER)));
		if(leastCards.contains(Card.JOKER)) {
			hasJoker = true;
		}
//		System.out.println("$"+melds.size() + " : "+result.size());
		for(Meld m : melds) {
			result.add(m);
		}
		
//		System.out.println("結果のsize" + result.size());
		return result;
	}
	
	public Meld getRverseMeld() {
		for(Meld meld : nowMelds) {
			if(meld.type() == Meld.Type.GROUP) {
				if(meld.asCards().size() >= 4)
					return meld;
				else if(meld.asCards().size() == 3 && 
						nowCards.contains(Card.JOKER) && 
						!meld.asCards().contains(Card.JOKER))
					return MeldUtility.createGroupWithJoker(meld.asCards());
			} else if(meld.type() == Meld.Type.SEQUENCE) {
				if(meld.asCards().size() >= 5)
					return meld;
				if(meld.asCards().size() == 4 && 
						nowCards.contains(Card.JOKER) && 
						!meld.asCards().contains(Card.JOKER))
					return MeldUtility.createSequence(
							meld.asCards().add(Card.JOKER), meld.rank());
			}
		}
		return null;
	}
	
	public boolean isBalancing() {
		return highMeld >= lowMeld;
	}
	
	
	/**
	 * あがり確定戦略があったときのMeld取り出し口
	 * @return
	 */
	public Meld getWinningStrategyMeld() {
//		System.out.println("Meldの取り出し"+winningStrategyMelds.peek());
		return winningStrategyMelds.peek();
	}
	
	public void removeWinningStrategeMeldTop(Meld postMeld) {
		if(winningStrategyMelds.peek().equals(postMeld)) {
			winningStrategyMelds.poll();
//			System.out.println("remove : "+meld);
		} else {
			winningStrategyMelds.removeLast();
		}
		nowCards = nowCards.remove(postMeld.asCards());
	}
	
	public int getWinningStrategeMeldsSize() {
		return winningStrategyMelds.size();
	}
	
	public void setWinningStrategyMelds(List<Meld> melds, Meld fastPutMeld, Meld lastPutMeld) {
		winningStrategyMelds.clear();
		winningStrategyMelds.add(fastPutMeld);
		for(Meld meld : melds) {
			if(lastPutMeld.equals(meld)) continue;
			winningStrategyMelds.add(meld);
		}
		winningStrategyMelds.add(lastPutMeld);
//		for(Meld meld : winningStrategyMelds) {
//			System.out.println("$$"+meld);
//		}
	}
	
	public Meld getWinningStrategeLastMeld() {
		return winningStrategyMelds.getLast();
	}
	
	public boolean hasSingleUseJoker() {
		return hasJoker;
	}
}
