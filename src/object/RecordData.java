package object;

public class RecordData {
	private int myHands;
	private int mygrade;
	private int code;
	private int wonPlayer;
	private int se;
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
		this.se += num*10;
		num = Character.getNumericValue(c[6]);
		this.se += num;
		num = Character.getNumericValue(c[7]);
		this.wonPlayer = num;

		int counter = 5;
		for (int i = 0; i < 6; i++) {
			num = Character.getNumericValue(c[i]);
			code += num * Math.pow(10, counter);
			counter--;
		}

	}

	public char[] getKey(int h, int g, int w,int se ,int ph) {
		if (mygrade == g && myHands == h&& wonPlayer == w && this.se == se) {
			if(Math.abs(ph -allPLayerHands ) <= InitSetting.RANGE＿ALLHANDS){
				return records.clone();
			}
			return null;
		} else {
			return null;
		}
	}
}
