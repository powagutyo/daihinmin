package object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameRecordData {

	List<RecordData> gameRecord_1;
	List<RecordData> gameRecord_2;
	List<RecordData> gameRecord_3;
	List<RecordData> gameRecord_4;
	List<RecordData> gameRecord_5;

	public GameRecordData() {
		init();
		gameRecordRead();
	}

	public char[] getGameRecord(int number, int myGrade, int myHands, int wonPLayer, int allPLayerHands) {
		char[] result = null;
		switch (myGrade) {
		case 1:
			result = gameRecord_1.get(number).getKey(myHands, myGrade, wonPLayer,allPLayerHands);
			if (result != null)
				return result;
			break;
		case 2:
			result = gameRecord_2.get(number).getKey(myHands, myGrade, wonPLayer,allPLayerHands);
			if (result != null)
				return result;
			break;
		case 3:
			result = gameRecord_3.get(number).getKey(myHands, myGrade, wonPLayer,allPLayerHands);
			if (result != null)
				return result;
			break;
		case 4:
			result = gameRecord_4.get(number).getKey(myHands, myGrade, wonPLayer,allPLayerHands);
			if (result != null)
				return result;
			break;
		case 5:
			result = gameRecord_5.get(number).getKey(myHands, myGrade, wonPLayer,allPLayerHands);
			if (result != null)
				return result;
			break;
		default:

		}
		return result;
	}

	public void init() {
		gameRecord_1 = new ArrayList<RecordData>();
		gameRecord_2 = new ArrayList<RecordData>();
		gameRecord_3 = new ArrayList<RecordData>();
		gameRecord_4 = new ArrayList<RecordData>();
		gameRecord_5 = new ArrayList<RecordData>();
	}

	public void setGameRecord(int myGrade, RecordData rd) {
		switch (myGrade) {
		case 1:
			gameRecord_1.add(rd);
			break;
		case 2:
			gameRecord_2.add(rd);
			break;
		case 3:
			gameRecord_3.add(rd);
			break;
		case 4:
			gameRecord_4.add(rd);
			break;
		case 5:
			gameRecord_5.add(rd);
			break;

		default:
			break;
		}
	}

	public int size(int myGrade) {
		switch (myGrade) {
		case 1:
			return gameRecord_1.size();
		case 2:
			return gameRecord_2.size();
		case 3:
			return gameRecord_3.size();
		case 4:
			return gameRecord_4.size();
		case 5:
			return gameRecord_5.size();
		default:
			break;
		}
		return 0;
	}

	public void gameRecordRead() {
		File file;
		BufferedReader bf = null;
		String message = "";
		char[] c;
		for (int i = 1; i <= 5; i++) {// gradeの分だけ
			for (int j = 1; j <= 11; j++) {
				if(j >= 10){
					file = new File("./gameRecord/myHand_" + j + "_" + i + ".txt");
				}else{
					file = new File("./gameRecord/myHand_0" + j + "_" + i + ".txt");
				}
				try {
					bf = new BufferedReader(new FileReader(file));
					while (true) {
						message = "";
						message = bf.readLine();
						if (message == null) {
							break;
						}
						c = message.toCharArray();
						setGameRecord(i, new RecordData(j, i, c));
					}
				} catch (Exception e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				} finally {
					try {
						bf.close();
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
			}
		}
	}

}