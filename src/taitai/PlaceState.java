package taitai;

import jp.ac.uec.daihinmin.card.*;

/*
 * PlayerTurnInformaiton で使われる　場の状態を管理するためのクラス
 */

public class PlaceState {
	private Meld place;
	private boolean isReverse;
	private boolean isSuitLock;
	
	public PlaceState(Meld lastMeld, boolean isReverse, boolean isSuitLock) {
		this.place = lastMeld;
		this.isReverse = isReverse;
		this.isSuitLock = isSuitLock;
	}
	
	public Suits lockedSuits() {
		if(isSuitLock)
			return place.suits();
		else 
			return Suits.EMPTY_SUITS;
	}
	
	/*
	 * 場が新しいか否かを返します.
	 */
	public boolean isRenew() {
		return place == null;
	}
	
	/*
	 * 革命が発生しているか否かを返します.
	 */
	public boolean isReverse() {
		return isReverse;
	}
}
