package object;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameRecordData {
	ArrayList<List<RecordData>> gameRecord;

	public GameRecordData() {

		gameRecord = new ArrayList<List<RecordData>>();
		for (int i = 0; i < 12; i++) {
			gameRecord.add(new ArrayList<RecordData>());
		}
		gameRecordRead();
	}

	public char[] getGameRecord(int number, int myHands, int sc, int allPLayerHands) {
		char[] result = null;
		result = gameRecord.get(myHands).get(number).getKey(myHands, allPLayerHands, sc);
		return result;
	}

	public void gameRecordRead() {
		File file;
		BufferedReader bf = null;
		String message = "";
		char[] c;
		for (int j = 1; j <= 11; j++) {
			if (j >= 10) {
				file = new File("./gameRecord/myHand_" + j + ".txt");
			} else {
				file = new File("./gameRecord/myHand_0" + j + ".txt");
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
					gameRecord.get(j).add(new RecordData(j, c));
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

	public int size(int myHands) {
		return gameRecord.get(myHands).size();
	}
}