package simulationBransing;

import java.util.ArrayList;
import java.util.HashMap;

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
		for (int i = 0; i < size; i++) {
			points[i] = gameList.get(i).getUCB();
			sum += points[i];
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
		int orderSize = order.size();

		int orderCounter = 0;

		comeBackUpdate(order, orderCounter, orderSize, 1, winPoint);
	}

	public void comeBackUpdate(ArrayList<Integer> order, int orderCounter, int orderSize, int pos, int winPoint) {
		if (orderSize <= orderCounter)
			return;
		int size = childrenGameFeild.get(pos).size();
		int orderNum = order.get(orderCounter);
		visit[pos]++;
		for (int i = 0; i < size; i++) {
			if (orderNum == i) {
				childrenGameFeild.get(pos).get(i).upDateUCB(visit[pos], winPoint, true);
			} else {
				childrenGameFeild.get(pos).get(i).upDateUCB(visit[pos], winPoint, false);
			}
		}

		orderCounter++;

		comeBackUpdate(order, orderCounter, orderSize, childrenGameFeild.get(pos).get(orderNum).getHaveChildNumber(), winPoint);
	}

	/**
	 * すべてのGameFieldクラスをリリースする
	 */
	public void realseAllGameFeild() {
		int arraySize = 0;
		int size = childrenGameFeild.size();
		for (int i = 1; i <= size; i++) {
			visit[i] = 0;
			arraySize = childrenGameFeild.get(i).size();
			for (int j = 0; j < arraySize; j++) {
				childrenGameFeild.get(i).get(0).release();
				ObjectPool.releaseGameField(childrenGameFeild.get(i).remove(0));
			}
			ObjectPool.releaseGameFeilds(childrenGameFeild.remove(i));

		}
		parent.release();
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
			gf.initChild(); // 子供の時のみ行う初期化
			gf = growUpGameField(gf, arrayLong.get(i), true, wd);
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
		GameField GF = getChildGameFiled_clone(parent, order, 0, size);
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

		size = order.size();
		childrenGameFeild.put(childDepth, gamelist);
		getChildGameFiled(parent, order, 0, size, childDepth);
		//GF.setHaveChildNumber(childDepth); // どの子供を持っているかの番号を渡す
		if (putHand != null) {
			ObjectPool.releasePutHand(putHand);
		}
		childDepth++;
	}

	public GameField getChildGameFiled_clone(GameField gf, ArrayList<Integer> order, int orderCounter, int size) {
		if (size <= orderCounter) {
			return gf.clone();
		}
		orderCounter++;

		return getChildGameFiled_clone(childrenGameFeild.get(gf.getHaveChildNumber()).get(order.get(orderCounter - 1)), order, orderCounter, size);
	}
	public void getChildGameFiled(GameField gf, ArrayList<Integer> order, int orderCounter, int size, int haveChildNumber) {
		if (size <= orderCounter) {
			gf.setHaveChildNumber(haveChildNumber);
			return;
		}
		orderCounter++;

		getChildGameFiled(childrenGameFeild.get(gf.getHaveChildNumber()).get(order.get(orderCounter - 1)), order, orderCounter, size,haveChildNumber);
	}

	/**
	 * 親につける子供を探索するメソッド
	 *
	 * @param gf
	 *            子供
	 */
	public static GameField growUpGameField(GameField gf, long num, boolean first,
			WeightData wd) {
		gf.updatePlace(num);
		if (gf.checkGoalPlayer()) { // 上がった人の判定
			gf.setCanGrowUpTree(false);// 木を成長できないように変更する
			return gf;
		}
		gf.endTurn();// ターン等々の処理

		return isContinueUpdate(gf.getPutHand(), gf);
	}

	public static GameField isContinueUpdate(ArrayList<Long> putHand, GameField gf) {
		if (putHand.size() <= 1) {
			gf.updatePlace(putHand.get(0));
			if (gf.checkGoalPlayer()) { // 上がった人の判定
				gf.setCanGrowUpTree(false);// 木を成長できないように変更する
				ObjectPool.releasePutHand(putHand);
				return gf;
			}
			gf.endTurn();// ターン等々の処理
			ObjectPool.releasePutHand(putHand);
			return isContinueUpdate(gf.getPutHand(), gf);
		}
		ObjectPool.releasePutHand(putHand);
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
