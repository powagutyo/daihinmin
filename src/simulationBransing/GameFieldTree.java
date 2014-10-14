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
		visit = new int[256];
		childDepth = 1;
	}

	public int getUCBPos(int childNumber) {
		double sum = 0;
		ArrayList<GameField> gameList = childrenGameFeild.get(childNumber);
		int size = gameList.size();
		// TODO ObjectPoolを活用
		double[] points = new double[size];
		for (int i = 0; i < size; i++) {
			points[i] = gameList.get(i).getUCB();
			sum = points[i];
		}
		sum = sum * Math.random();
		int pos = 0;
		for (int i = 0; i < size; i++) {
			sum = sum - points[i];
			if (sum < 0) {
				pos = i;
			}
		}
		return pos;
	}
	public GameField returnGameFeild(int childNumber, int pos){
		return childrenGameFeild.get(childNumber).get(pos);
	}
	/**
	 * 木を更新するメソッド
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
		parentGF = parent;
		while (true) {
			if(orderSize <= orderCounter)
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
		int size = 0;
		ArrayList<GameField> ag;
		for (Integer key : childrenGameFeild.keySet()) {
			ag = childrenGameFeild.remove(key);
			size = ag.size();
			for (int i = 0; i < size; i++) {
				ObjectPool.releaseGameField(ag.remove(0));
			}
			ObjectPool.releaseGameFeilds(ag);
		}
		parent.release();
		ObjectPool.releaseGameField(parent);
		childDepth = 1;
	}

	public void init(GameField p){
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
	public void firstInitChildren(GameField parent, long[] arrayLong,
			WeightData wd) {

		ArrayList<GameField> gamelist = ObjectPool.getGameFields();
		int size = arrayLong.length;
		GameField gf;
		for (int i = 0; i < size; i++) {
			gf = ObjectPool.getGameField();
			gf = parent.clone();
			gf = growUpGameField(gf, arrayLong[i], true, wd);
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
	public void initChildren(GameField parent, WeightData wd) {
		ArrayList<GameField> gamelist = ObjectPool.getGameFields();
		ArrayList<Long> putHand = parent.getPutHand();
		int size = putHand.size();
		GameField gf;
		for (int i = 0; i < size; i++) {
			gf = ObjectPool.getGameField();
			gf = parent.clone();
			gf = growUpGameField(gf,putHand.get(i), false, wd);
			gf.initChild(); // 子供の時のみ行う初期化
			gamelist.add(gf);
		}
		childrenGameFeild.put(childDepth, gamelist);
		parent.setHaveChildNumber(childDepth); // どの子供を持っているかの番号を渡す
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
		boolean flag = false;
		while (true) {
			if (first) {
				if (num != 0)
					flag = true;
				gf.updatePlace(num);
			} else {
				gf.useSimulationBarancing(InitSetting.LEARNING, wd);
				if (gf.getYaku() != 0)
					flag = true;
			}
			if (gf.checkGoalPlayer()) { // 上がった人の判定
				gf.setCanGrowUpTree(false);// 木を成長できないように変更する
			}
			gf.endTurn();// ターン等々の処理
			if (flag)
				break;
		}
		return gf;
	}

	public int returnPutPos(){
		int childNumber = parent.getHaveChildNumber();
		ArrayList<GameField> arrayGF = childrenGameFeild.get(childNumber);
		int size = arrayGF.size();
		double point = 0;
		double max = -1024;
		int pos = -1;
		for(int i = 0;i<size;i++){
			point = arrayGF.get(i).getWinPoint();
			if(max < point){
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
