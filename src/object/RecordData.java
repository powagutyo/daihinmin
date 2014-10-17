package object;

public class RecordData {
	private int myHands;
	private int mygrade;
	private int code;
	private int wonPlayer;
	private int allPLayerHands;
	private char[] records;

	public RecordData(int h, int g, char[] c) {
		this.myHands = h;
		this.mygrade = g;
		this.records = c.clone();
		int num = 0;
		num = Character.getNumericValue(c[3]);
		this.allPLayerHands += num * 10;
		num = Character.getNumericValue(c[4]);
		this.allPLayerHands += num;
		num = Character.getNumericValue(c[5]);
		this.wonPlayer = num;

		int counter = 5;
		for (int i = 0; i < 6; i++) {
			num = Character.getNumericValue(c[i]);
			code += num * Math.pow(10, counter);
			counter--;
		}

	}

	public char[] getKey(int h, int g, int w, int ph) {
		if (mygrade == g && myHands == h&& wonPlayer == w) {
			if(Math.abs(ph -allPLayerHands ) <= 10){
				return records.clone();
			}
			return null;
		} else {
			return null;
		}
	}
}
