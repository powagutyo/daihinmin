package simulationBransing;

import java.util.ArrayList;
import java.util.HashMap;

import object.InitSetting;
import object.ObjectPool;
import object.WeightData;

public class GameFieldTree {

	public HashMap<Integer, ArrayList<GameField>> childrenGameFeild;

	public int[] visit;

	/** 子供がどのくらい作られたかどうかの判定用 **/
	public int childDepth;
	public GameField parent;

	public GameFieldTree() {
		childrenGameFeild = new HashMap<Integer, ArrayList<GameField>>();
		visit = new int[2048];
		childDepth = 1;
	}

	/**
	 * UCBの値で手を決定する
	 *
	 * @param childNumber
	 * @param gf
	 *            GameField
	 * @param wd
	 *            WeightDataクラス
	 * @param learing
	 *            学習率
	 * @return
	 */
	public int getUCBPos(int childNumber, GameField gf, WeightData wd, double learning) {
		double sum = 0;
		ArrayList<GameField> gameList = childrenGameFeild.get(childNumber);
		int size = gameList.size();
		double[] points = ObjectPool.getArrayDouble();
		if (InitSetting.putHandMode == 2) {

			for (int i = 0; i < size; i++) {
				points[i] = gameList.get(i).getUCB();
				sum += points[i];
			}
		} else {
			for (int i = 0; i < size; i++) {
				points[i] = gameList.get(i).getUCB();
				sum += points[i];
			}
		}
		sum = sum * Math.random();
		int pos = 0;
		for (int i = 0; i < size; i++) {
			sum = sum - points[i];
			if (sum < 0) {
				pos = i;
				break;
			}
		}
		ObjectPool.releaseArrayDouble(points);
		return pos;
	}

	public GameField returnGameFeild(int childNumber, int pos) {
		return childrenGameFeild.get(childNumber).get(pos);
	}

	/**
	 * 木を更新するメソッド
	 *
	 * @param order
	 * @param winPoint
	 */
	public void upDateTree(ArrayList<Integer> order, int winPoint) {
		GameField parentGF = null;
		ArrayList<GameField> arrayGF = null;
		int size = 0;
		int orderSize = order.size();
		int orderNum = 0;
		int orderCounter = 0;
		int pos = 0;
		parentGF = this.parent.clone();
		while (true) {
			if (orderSize <= orderCounter)
				break;
			pos = parentGF.getHaveChildNumber();
			arrayGF = childrenGameFeild.get(pos);
			size = arrayGF.size();
			orderNum = order.get(orderCounter);
			visit[pos]++;
			for (int i = 0; i < size; i++) {
				if (orderNum == i) {
					arrayGF.get(i).upDateUCB(visit[pos], winPoint, true);
				} else {
					arrayGF.get(i).upDateUCB(visit[pos], winPoint, false);
				}
			}
			parentGF = arrayGF.get(orderNum);
			orderCounter++;
		}
	}

	/**
	 * すべてのGameFieldクラスをリリースする
	 */
	public void realseAllGameFeild() {
		int size = childrenGameFeild.size();
		int arraySize = 0;
		ArrayList<GameField> ag;
		for (int i = 1; i <= size; i++) {
			visit[i] = 0;
			ag = childrenGameFeild.get(i);
			arraySize = ag.size();
			for (int j = 0; j < arraySize; j++) {
				ag.get(0).release();
				ObjectPool.releaseGameField(ag.remove(0));
			}
			ObjectPool.releaseGameFeilds(ag);
		}
		parent.release();
		ObjectPool.releaseGameField(parent);
		parent = null;
		childDepth = 1;
	}

	public void init(GameField p) {
		childrenGameFeild.clear();
		parent = p;
		childDepth = 1;
	}

	/**
	 * 親を成長させる
	 *
	 * @param parent
	 * @param arrayLong
	 * @param wd
	 */
	public void firstInitChildren(GameField parent, ArrayList<Long> arrayLong,
			WeightData wd) {

		ArrayList<GameField> gamelist = ObjectPool.getGameFields();
		int size = arrayLong.size();
		GameField gf;
		for (int i = 0; i < size; i++) {
			gf = ObjectPool.getGameField();
			gf = parent.clone();
			gf = growUpGameField(gf, arrayLong.get(i), true, wd);
			gf.initChild(); // 子供の時のみ行う初期化
			gamelist.add(gf);
		}
		childrenGameFeild.put(childDepth, gamelist);
		parent.setHaveChildNumber(childDepth); // どの子供を持っているかの番号を渡す
		childDepth++;
	}

	/**
	 * 親を成長させる
	 *
	 * @param parent
	 * @param wd
	 */
	public void initChildren(ArrayList<Integer> order, WeightData wd) {
		int size = order.size();
		GameField GF = this.parent.clone();
		for (int i = 0; i < size; i++) {
			GF = childrenGameFeild.get(GF.getHaveChildNumber()).get(order.get(i));
		}
		ArrayList<GameField> gamelist = ObjectPool.getGameFields();
		ArrayList<Long> putHand = GF.getPutHand();
		size = putHand.size();
		GameField gf;
		for (int i = 0; i < size; i++) {
			gf = ObjectPool.getGameField();
			gf = GF.clone();
			gf = growUpGameField(gf, putHand.get(i), false, wd);
			gf.initChild(); // 子供の時のみ行う初期化
			gf.setFirstWonPlayer(parent.getFirstWonPlayer());
			gamelist.add(gf);
		}
		childrenGameFeild.put(childDepth, gamelist);
		GF.setHaveChildNumber(childDepth); // どの子供を持っているかの番号を渡す
		ObjectPool.releasePutHand(putHand);
		childDepth++;
	}

	/**
	 * 親につける子供を探索するメソッド
	 *
	 * @param gf
	 *            子供
	 */
	public static GameField growUpGameField(GameField gf, long num, boolean first,
			WeightData wd) {
		ArrayList<Long> putHand = null;
		while (true) {
			gf.updatePlace(num);
			if (gf.checkGoalPlayer()) { // 上がった人の判定
				gf.setCanGrowUpTree(false);// 木を成長できないように変更する
				break;
			}
			gf.endTurn();// ターン等々の処理
			putHand = gf.getPutHand();
			if (putHand.size() == 1) {
				num = putHand.get(0);
			} else {
				break;
			}
		}
		if (putHand != null) {
			ObjectPool.releasePutHand(putHand);
		}
		return gf;
	}

	/**
	 * 最終的に出す役の場所を返すメソッド
	 *
	 * @return
	 */
	public int returnPutPos() {
		int childNumber = parent.getHaveChildNumber();
		ArrayList<GameField> arrayGF = childrenGameFeild.get(childNumber);
		int size = arrayGF.size();
		double point = 0;
		double max = -1024;
		int pos = -1;
		for (int i = 0; i < size; i++) {
			point = arrayGF.get(i).getWinPoint();
			if (max < point) {
				max = point;
				pos = i;
			}
		}
		return pos;
	}

	public GameField getParent() {
		return parent;
	}
}
