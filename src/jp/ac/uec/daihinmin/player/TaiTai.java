package jp.ac.uec.daihinmin.player;
import jp.ac.uec.daihinmin.*;
import jp.ac.uec.daihinmin.card.*;
import static jp.ac.uec.daihinmin.card.MeldFactory.*;
import jp.ac.uec.daihinmin.player.BotSkeleton;

import taitai.*;
import taitai.player.*;
import taitai.util.*;
/**
 * 
 * @author tai
 * 注意点：オーバーライドするときは，最初にsuperクラスのメソッドを呼び出しましょう！
 */
public final class TaiTai extends BotSkeleton {
	GameState gameState;
	PlayersInfo playersInfo;
	GameTimeCounter timeCounter = new GameTimeCounter();
	Meld ownLastMeld;
	
	/**
	 * コンストラクタの呼び出しが終る時点では，number()は呼び出すことができない．
	 * 
	 */
	public TaiTai() {
		super();
	}
	
	@Override
	/**
	 * 大貧民開始時に実行されるメソッドです.
	 */
	public void daihinminStarted(Rules rules) {
		super.daihinminStarted(rules);
		
		playersInfo = new PlayersInfo(number());
	}
	
	@Override
	/**
	 * 各ゲーム開始時に実行されるメソッドです.
	 * 
	 * hand()はこの時点では呼び出すことができません．
	 * 席順を設定
	 */
	public void gameStarted() {
		super.gameStarted();
		playersInfo.gameStarted(playersInformation());
		ownLastMeld = null;
	}
	
	@Override
	/**
	 * ゲーム開始直後, 自分の手札が配られたときに実行されるメソッドです.
	 * 
	 * 
	 */
	public void dealed(Cards hand) {
		super.dealed(hand);
		gameState = new GameState(this.hand());
	}
	
	@Override
	/**
	 * super.gaveCards(playerFrom, playerTo, cards);を呼ばないと，
	 * hand()の更新をしてくれない・・・
	 */
	public void gaveCards(java.lang.Integer playerFrom, 
			java.lang.Integer playerTo, Cards cards) {
		super.gaveCards(playerFrom, playerTo, cards);
		gameState.setUp(hand());
	}
	
	@Override
	/**
	 * 
	 */
	public void played(java.lang.Integer number, Meld playedMeld) {
		super.played(number, playedMeld);
		
		try {
		playersInfo.played(number, playedMeld, place(),timeCounter);
		timeCounter.played();
		
		if(number != number()) {
			if(playedMeld.type() != Meld.Type.PASS) {
				gameState.otehrPlayerPlay(playedMeld.asCards());
			}
		} else {
			// 自分自身のとき
			gameState.ownAcceptedPutMeld(playedMeld);
		}
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * カード交換の時に用いられるメソッド． 
	 * ただし，07 年度のルールでは自分の順位が平民以上の時のみ選ぶことができる． 
	 * 貧民以下の時は, このメソッドは呼ばれず, 自動的に徴収されます． 
	 * このメソッド内で用いられる主なメソッドおよびフィールド 自分の手札this.hand()
	 * 渡すカードの枚数Rules.sizeGivenCards(this.rules(),this.rank()); 
	 * 自分の順位this.rank()
	 * 平民の順位Rules.heiminRank(this.rules())
	 * 
	 * @return 交換相手に渡すカード集合
	 */
	public Cards requestingGivingCards() {
		int size = Rules.sizeGivenCards(this.rules(), this.rank());
		Cards cards = gameState.requestingGivingCards(size);
		if(cards.size() == size) return cards;
		
		
		Cards result = Cards.EMPTY_CARDS;
		// 手札を昇順にソート．たとえば，JOKER S2 HA ... D4 D3
		Cards sortedHand = Cards.sort(this.hand());
		// 渡すカードの枚数だけ，result にカードを追加
		for (int i = 0; i < Rules.sizeGivenCards(this.rules(), this.rank()); i++) {
			result = result
					.add(sortedHand.get(Rules.heiminRank(this.rules()) < this
							.rank() ? sortedHand.size() - 1 - i : i));
		}
		// たとえば，大貧民ならD3 D4
		// たとえば，大富豪ならJOKER S2
		
		return result;
	}

	/**
	 * カードを出す時に用いられるメソッド． 
	 * このメソッド内で用いられる主なメソッドおよびフィールド 自分の手札this.hand()
	 * 自分のプレイヤー番号this.number() 場this.place() 
	 * 場が新しいかthis.place().isRenew() 
	 * ただし, 場にカードがなければtrue 場に出されている役の枚数this.place().size()
	 * 場に出されている役のタイプthis.place().type() ただし，型はMeld.Type
	 * 革命中か否かthis.place().order() ただし，型はOrder パスの役PassMeld.PASS()
	 * 
	 * よく使われうるイディオム 場に出されている役のランクの, 
	 * 直ぐ上のランク Rank next_rank = this.place().type() ==
	 * 		Meld.Type.SEQUENCE? this.rules().nextRankSequence(this.place().rank(),
	 * 		this.place().size(),this.place().order()) :this.place().order() ==
	 * 		Order.NORMAL?this.place().rank().higher() :this.place().rank().lower();
	 * ただし，this.rules().nextRankSequence は次のランクがないとき, 
	 * IllegalArgumentException をスローします．
	 * 
	 * 場に出されている役の, タイプ, 枚数, ランク, 革命中か否か, 
	 * に合わせて, 「出すことができる」候補に絞る． melds =
	 * melds.extract(Melds.typeOf(this.place().type()).and(
	 * 	Melds.sizeOf(this.place().size()).and( Melds.rankOf(next_rank).or(
	 * 	this.place().order() == Order.NORMAL?Melds.rankOver(next_rank): 
	 * 	Melds.rankUnder(next_rank)))));
	 * 
	 * @return 自分が出したい役
	 * 
	 * 戦略
	 * ０．必ず勝てるパターンが存在するなら，そのパターンを出す
	 * １．強いカードと弱いカードのバランスを保つ
	 * ２．縛る
	 * ３．もっとも弱いカードは最後まで取っておく（あがる際に出す）
	 */
	public Meld requestingPlay() {
//		if(!gameState.check(hand())) {
//			System.exit(0);
//		}
		
		try {
		
		if(place().isRenew() || place().lastMeld().type() == Meld.Type.SINGLE) {
			if(gameState.getOwnCards().size() == 1 && gameState.getOwnCards().contains(Card.JOKER)) {
				return MeldUtility.createSingleJoker(place());
			}
		}
		
		// 自分以外の全員が既にパスをしているとき
		if(playersInfo.isOtherPlayerAllPassed()) {
			// あがり確定の状態で，直前のターンに場にカードを出して
			// 他のプレイヤーが皆パスした状態
			// のはず・・・
			if(gameState.isFindedWinningStrategy())
				return PASS;
			
			Cards lastCards = place().lastMeld().asCards();
			if(lastCards.size() == 1 && lastCards.contains(Card.JOKER)) {
				// 場に出ているカードがjokerの一枚出しのとき，
				// S3を持っていたら出す．それ以外なら出せないね・・・
				if(gameState.containsOwnCard(Card.S3))
					return MeldUtility.S3;
				else return PASS;
			}
		}
		
		
		if(gameState.isFindedWinningStrategy()) {
			if(place().isRenew())
				return gameState.requestingPlay(place());
			else {
				
			}
		}
		
		if(place().isRenew() && place().isReverse()) {
			// 革命がおこっていたら，できる限り，元に戻そう！
			Melds melds = MeldsUtility.getReverseMelds(hand());
			if(melds.size() > 0)
				return melds.get(0);
			
//			Meld meld = gameState.getReverseMeld();
//			if(meld != null && CardsUtility.containsAll(hand(), meld.asCards())) 
//				return meld;
		} else if(!place().isRenew() && place().isReverse()) {
			// 革命が直前に起こされた状態
			// 返すことができたら，返そう！
			Meld lastMeld = place().lastMeld();
			if((lastMeld.type() == Meld.Type.SEQUENCE && lastMeld.asCards().size() >= 5) || 
					(lastMeld.type() == Meld.Type.GROUP && lastMeld.asCards().size() >= 4)) {
				Melds melds = MeldsUtility.getReverseMelds(hand());
				for(Meld meld : melds) {
					if(Utility.isAcceptMeld(meld, place()))
						return meld;
				}
			}
		}
		
		if(!place().isRenew() && 
				MeldUtility.isOnlyJoker(place().lastMeld()) && 
				gameState.containsOwnCard(Card.S3)) {
			return MeldUtility.S3;
		}
		
		Meld meld = gameState.requestingPlay(place());
		if(meld != null) {
			return requestingPlay(meld);
		}
		
		return requestingPlay(requestingPlay(hand()));
		} catch (Exception e) {
			return PASS;
		}
	}
	
	/**
	 * 最終的に，そのmeldを出すかどうか判定
	 * 
	 * 皆がpassしているときに，自分が出すかどうか．
	 * lowカードが入っているなら出す．
	 * @param meld
	 * @return
	 */
	public Meld requestingPlay(Meld meld) {
		if(meld.equals(PASS)) {
			if(!playersInfo.isOtherPlayerAllPassed() &&
				!place().isRenew() && 
					place().lastMeld().type() == Meld.Type.SINGLE && 
					gameState.getOwnCards().contains(Card.JOKER)) {
				return MeldFactory.createSingleMeldJoker(
						place().lastMeld().asCards().get(0).suit(), 
						place().isReverse()? Rank.JOKER_LOWEST : Rank.JOKER_HIGHEST);
			}
			return PASS;
		}
		
		if(!gameState.isFindedWinningStrategy()) {
			if(rank() != null && rank() > 2 && !place().isReverse() && place().isRenew() && 
					gameState.isOtherPlayerExistingAvailableMelds(
							meld, !place().lockedSuits().equals(Suits.EMPTY_SUITS), 
							place().isReverse())) {
				if(!gameState.isBalancing() && MeldUtility.isMiddleMeld(meld))
					return PASS;
			}
			
			// 自分以外のプレイヤーがPASSをしていたとき
			if(playersInfo.isOtherPlayerAllPassed()) {
				if(meld.type() == Meld.Type.SINGLE && meld.asCards().contains(Card.S3))
					return meld;
//				if(MeldUtility.containsLowCard(meld, place()))
//					return meld;
				return PASS;
			}
			
			// 隣のプレイヤーだけがPASSをしていないとき
			// 自分もPASSして，隣のプレイヤーの番にして良いときの処理
			if(rank() != null && rank() > 2 && 
					gameState.getWonPlayerCount() <= 1 && 
					playersInfo.isOtehrPlayerAllPassedWithoutBeforePlayer() &&
					playersInfo.getPlayerRenewTurnCout(playersInfo.getBeforeSeatPlayerNum()) < 2 &&
					playersInformation().getSeatOfPlayer(playersInfo.getBeforeSeatPlayerNum()) > 5) {
				
//			if(rank() != null && rank() > 2 && gameState.getWonPlayerCount() == 0 && 
//					playersInfo.isOtehrPlayerAllPassedWithoutBeforePlayer() &&
//					playersInfo.getPlayerRenewTurnCout(playersInfo.getBeforeSeatPlayerNum()) < 2) {
				if(!MeldUtility.containsLowCard(meld, place()))
					return PASS;
			}
		}
		return meld;
	}
	
	public Meld requestingPlay(Cards hand) {
		// Jokerを抜きで判定し，出せたら，Jokerを使わない手を出す．
		if(hand.contains(Card.JOKER)) {
			Meld meld = requestingPlay(hand.remove(Card.JOKER));
			if(meld.type() != Meld.Type.PASS) return meld;
		}
		
		// melds : 出す役の候補の役集合
		// melds := 自分の手札から構成できる全ての役の集合
		Melds melds = Melds.parseMelds(hand);
		
		// 場に何のカードも出されていなければ,
		if (this.place().isRenew()) {
			// 候補の中で最小の数で最大の枚数を持つ役を抽出して, 候補とする．
			melds = melds.extract(Melds.MIN_RANK);
			melds = melds.extract(Melds.MAX_SIZE);
		} else {
			// 場が縛られていれば
			if (!this.place().lockedSuits().equals(Suits.EMPTY_SUITS)) {
				// 場を縛っているスート集合に適合する役を抽出して, 候補とする．
				melds = melds
						.extract(Melds.suitsOf(this.place().lockedSuits()));
			}
			try {
				melds = Utility.removeNotAcceptMelds(place(), melds, rules());
			} catch (IllegalArgumentException e) {
				return PASS;
			}
		}
		
		Meld result = 縛り(melds);
		
		// 残った候補の中からさらに絞る．たとえば，場のオーダが通常のとき最も弱い役を候補に残す．
		melds = melds
				.extract(this.place().order() == Order.NORMAL ? Melds.MIN_RANK
						: Melds.MAX_RANK);
		// 候補が残っているか？
		if (melds.size() == 0) {
			// 候補が残ってないときはパス．
			return PASS;
		}
		
		// 候補が残っているとき，
		// 候補のうち１つを最終候補とする．ここでは，melds.get(0)
		// 最終候補が一枚のJOKER だったとき,
		if (melds.get(0).type() == Meld.Type.SINGLE
				&& melds.get(0).asCards().get(0) == Card.JOKER) {
			// 場のスートに合わせた, 最大のランクを持つ役に変更して, それを出す．
			// この処理が必要な理由は, たとえば，最終候補が「一枚のJOKER を
			// H6 として出す」だったとき，
			// 場がD5 なら，「一枚のJOKER をD+として出す」が最も強力なため．
			if(place().isRenew()) {
				Meld meld =  createSingleMeldJoker(Suit.SPADES, this
					.place().order() == Order.NORMAL ? Rank.JOKER_HIGHEST
					: Rank.JOKER_LOWEST);
				return meld;
			}
			return createSingleMeldJoker(place().isRenew()? Suit.SPADES:
					this.place().suits().get(0), 
					(this.place().order() == Order.NORMAL ? Rank.JOKER_HIGHEST
					: Rank.JOKER_LOWEST));
		}

		if(result == null) result = melds.get(0);
		
//		if(playersInfo.isOtherPlayerAllPassed() && 
//				Utility.isHighCardUse(result.asCards())) {
//			return PASS;
//		}
		
//		if(result.type() == Meld.Type.SINGLE && 
//				MeldUtility.containsLowCard(result, place())) {
//			return PASS;
//		}
		
		// 最終候補を出す．
		return result;
	}
	
	
	private Meld 縛り(Melds melds) {
		if(place().isRenew()) return null;
		if(!place().lockedSuits().equals(Suits.EMPTY_SUITS))
			return null;
		
		// 縛れるか判定し，縛れる最小のものを出す．
		for(Meld meld : melds) {
			if(place().lastMeld().suits().contains(meld.suits())) {
				if(meld.asCards().contains(Card.JOKER) 
						|| Utility.isHighCardUse(meld.asCards()))
					continue;
				return meld;
			}
		}
		return null;
	}
	
	public void playRejected(java.lang.Integer number,
            Meld playedMeld) {
		super.playRejected(number, playedMeld);
//		System.out.println(hand()+" : "+place().lastMeld()+" : "+place());
//		System.out.println(gameState);
//		System.out.println();
//		System.out.println(gameState.getOwnCards());
		if(number == number()) {
//			System.out.println("OwnRejected");
			gameState.ownCardsReset(hand());
//			System.exit(0);
		}
	}
	
	@Override
	/**
	 * 各ゲーム終了時に実行されるメソッドです.
	 */
	public void gameEnded() {
		super.gameEnded();
		playersInfo.gameEnded(gameState.getLeastCards());
	}
	
	@Override
	/**
	 * 場が流れたとき実行されるメソッドです.
	 */
	public void placeRenewed() {
		super.placeRenewed();
		timeCounter.renewed();
		playersInfo.placeRenewed();
	}
	
	
	public void playerWon(java.lang.Integer number) {
		super.playerWon(number);
		playersInfo.playerWon(number);
	}
	
	public void daihinminEnded() {
		super.daihinminEnded();
//		System.out.println(playersInfo);
		timeCounter.reset();
	}
}

