package object;

public class RecordData {
	private int myHands;
	private int code;
	private int se;
	private int allPLayerHands;
	private char[] records;

	public RecordData(int h, char[] c) {
		this.myHands = h;
		this.records = c.clone();
		int num = 0;
		num = Character.getNumericValue(c[3]);
		this.allPLayerHands += num * 10;
		num = Character.getNumericValue(c[4]);
		this.allPLayerHands += num;
		num = Character.getNumericValue(c[5]);
		this.se += num * 10;
		num = Character.getNumericValue(c[6]);
		this.se += num;

		int counter = 5;
		for (int i = 0; i < 6; i++) {
			num = Character.getNumericValue(c[i]);
			code += num * Math.pow(10, counter);
			counter--;
		}

	}

	public char[] getKey(int h, int ph,int se) {
		if (myHands == h && this.se == se) {
			if (Math.abs(h - myHands) <= InitSetting.RANGE＿ALLHANDS) {
				if (Math.abs(ph - allPLayerHands) <= InitSetting.RANGE＿ALLHANDS) {
					return records.clone();
				}
			}
			return null;
		} else {
			return null;
		}
	}
}
