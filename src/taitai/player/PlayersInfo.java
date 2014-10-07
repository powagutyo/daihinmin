package taitai.player;

import java.util.Arrays;
import java.util.Comparator;

import taitai.GameTimeCounter;

import jp.ac.uec.daihinmin.Place;
import jp.ac.uec.daihinmin.PlayersInformation;
import jp.ac.uec.daihinmin.card.Cards;
import jp.ac.uec.daihinmin.card.Meld;

public class PlayersInfo {
	
	PlayerInformationImp[] players = new PlayerInformationImp[5];
	PlayerInformationImp[] playersSeatSort = new PlayerInformationImp[5];
	public static final int PLAYER_SIZE = 5;
	private int wonPlayerCount;
	private int ownNumber;
	private static Comparator<PlayerInformationImp> playersSeatSortComparator = 
		new Comparator<PlayerInformationImp>(){
			public int compare(PlayerInformationImp o1, PlayerInformationImp o2) {
				return o1.getSeat() - o2.getSeat();
			}
		};
	
	public PlayersInfo(int ownNumber) {
		this.ownNumber = ownNumber;
		for(int i = 0; i < PLAYER_SIZE; i++) {
			players[i] = new PlayerInformationImp(i);
			playersSeatSort[i] = players[i];
		}
	}
	
	public void gameStarted(PlayersInformation playersInformation) {
		wonPlayerCount = 0;
		for(int i = 0; i < PLAYER_SIZE; i++) {
			players[i].gameStarted();
			players[i].setSeat(playersInformation.getSeatOfPlayer(i));
		}
		Arrays.sort(playersSeatSort, playersSeatSortComparator);
	}
	
	public int getBeforeSeatPlayerNum() {
		return getBeforeSeatPlayerNum(ownNumber);
	}
	
	public int getNextSeatPlayerNum() {
		return getNextSeatPlayerNum(ownNumber);
	}
	
	public int getBeforeSeatPlayerNumWithoutWonPlayer() {
		return getBeforeSeatPlayerNumWithoutWonPlayer(ownNumber);
	}
	
	public int getNextSeatPlayerNumWithoutWonPlayer() {
		return getNextSeatPlayerNumWithoutWonPlayer(ownNumber);
	}
	
	public int getBeforeSeatPlayerNum(int playerNum) {
		return playersSeatSort[(players[playerNum].getSeat() 
				+ PLAYER_SIZE - 1) % PLAYER_SIZE].getNumber();
	}
	
	public int getNextSeatPlayerNum(int playerNum) {
		return playersSeatSort[(players[playerNum].getSeat() 
				+ 1) % PLAYER_SIZE].getNumber();
	}
	
	public int getBeforeSeatPlayerNumWithoutWonPlayer(int playerNum) {
		int now = playerNum;
		for(int i = 0; i < PLAYER_SIZE; i++) {
			int before = getBeforeSeatPlayerNum(now);
			if(!players[before].isWin()) return before;
			now = before;
		}
		return now;
	}
	
	public int getNextSeatPlayerNumWithoutWonPlayer(int playerNum) {
		int now = playerNum;
		for(int i = 0; i < PLAYER_SIZE; i++) {
			int next = getNextSeatPlayerNum(now);
			if(!players[next].isWin()) return next;
			now = next;
		}
		return now;
	}
	
	/**
	 * Taiクラスから，numberのプレイヤーがあがったら，呼び出される．
	 * @param number
	 */
	public void playerWon(java.lang.Integer number) {
		players[number].playerWon(PLAYER_SIZE - wonPlayerCount);
		wonPlayerCount++;
	}
	
	public void played(java.lang.Integer number, Meld playedMeld, 
			Place place, GameTimeCounter timeCounter) {
//		if(number != ownNumber) {
			if(playedMeld.type() != Meld.Type.PASS) {
			}
			PlayerTurnState[] playersState = 
				new PlayerTurnState[PlayersInfo.PLAYER_SIZE];
			for(int i = 0; i < PlayersInfo.PLAYER_SIZE; i++)
				playersState[i] = players[i].isWin() ? 
						PlayerTurnState.Won : players[i].isPassed() ? 
								PlayerTurnState.PASSED : PlayerTurnState.NORMAL;
			
			players[number].play(place, playedMeld, timeCounter, playersState);
//		} else {
//			// 自分自身のとき
//		}
	}
	
	public void gameEnded(Cards leastCards) {
		for(int i = 0; i < PLAYER_SIZE; i++) {
			players[i].gameEnd(leastCards);
		}
	}
	
	/**
	 * 場が流れたとき実行されるメソッドです.
	 */
	public void placeRenewed() {
		for(int i = 0; i < PLAYER_SIZE; i++) {
			if(ownNumber == i) continue;
			players[i].placeRenewed();
		}
	}
	
	/**
	 * 他のプレイヤーが全員パスをしている状態であるかを判定する
	 * @return 他のプレイヤーが全員パスしているか否か
	 */
	public boolean isOtherPlayerAllPassed() {
		for(int i = 0; i < PLAYER_SIZE; i++) {
			if(ownNumber == i || players[i].isWin()) continue;
			if(!players[i].isPassed()) return false;
		}
		return true;
	}
	
	/**
	 * 自分よりも一つ手前の番に出す席のプレイヤー以外がpassしているか否かを返す
	 * @return
	 */
	public boolean isOtehrPlayerAllPassedWithoutBeforePlayer() {
		int ownSeat = players[ownNumber].getSeat();
		int beforePlayerSeat = (ownSeat + PLAYER_SIZE - 1) % PLAYER_SIZE;
		int beforePlayreNum = playersSeatSort[beforePlayerSeat].getNumber();
		for(int i = 0; i < PLAYER_SIZE; i++) {
			if(!playersSeatSort[beforePlayerSeat].isWin()) break;
			beforePlayerSeat = (beforePlayerSeat + PLAYER_SIZE - 1) % PLAYER_SIZE;
			beforePlayreNum = playersSeatSort[beforePlayerSeat].getNumber();
		}
		
		for(int i = 0; i < PLAYER_SIZE; i++) {
			if(ownNumber == i || players[i].isWin()) continue;
			if(beforePlayreNum == i) {
				if(players[i].isPassed()) return false;
			} else {
				if(!players[i].isPassed()) return false;
			}
		}
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("試合結果\n");
		for(PlayerInformationImp player : players) {
			sb.append(player+"\n");
		}
		return sb.toString();
	}
	
	public int getPlayerRenewTurnCout(int number) {
		return players[number].getRenewTurnCout();
	}
}
