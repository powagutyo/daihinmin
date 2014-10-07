package taitai.player;

public class PlayerScore {
	int Daihugou,sDaihugou;
	int Hugou,sHugou;
	int Heimin,sHeimin;
	int Hinmin,sHinmin;
	int Daihinmin,sDaihinmin;
	int score;
	int before = -1;
	int playerNum;
	
	public PlayerScore(int playerNum) {
		this.playerNum = playerNum;
	}
	
	public void playerWon(int score) {
		this.score += score;
		switch(score) {
		case 5 : Daihugou++; break;
		case 4 : Hugou++; break;
		case 3 : Heimin++; break;
		case 2 : Hinmin++; break;
		case 1 : Daihinmin++; break;
		}
		if(score == before) {
			switch(score) {
			case 5 : sDaihugou++; break;
			case 4 : sHugou++; break;
			case 3 : sHeimin++; break;
			case 2 : sHinmin++; break;
			case 1 : sDaihinmin++; break;
			}
		}
		before = score;
	}
	
	public String toString() {
		return String.format("%d : %4d/%4d %4d/%4d %4d/%4d %4d/%4d %4d/%4d : %5d", 
				playerNum, Daihugou, sDaihugou, Hugou, sHugou, 
				Heimin, sHeimin, Hinmin, sHinmin, Daihinmin, sDaihinmin, score);
	}
}
