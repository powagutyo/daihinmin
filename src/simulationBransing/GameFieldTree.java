package simulationBransing;

import java.util.ArrayList;
import java.util.HashMap;

import object.InitSetting;
import object.ObjectPool;
import object.WeightData;

public class GameFieldTree {

	public static HashMap<Integer,ArrayList<GameField>> childrenGameFeild;

	public static int[] visit;

	/**子供がどのくらい作られたかどうかの判定用**/
	public static int childDepth;
	public static GameField parent;


	public static GameField getUCBGameField(int num){
		double sum = 0;

		ArrayList<GameField> gameList = childrenGameFeild.get(num);



		return null;
	}
	/**
	 * すべてのGameFieldクラスをリリースする
	 */
	public static void realseAllGameFeild() {
		int size = 0;
		ArrayList<GameField> ag;
		for(Integer key : childrenGameFeild.keySet()){
			ag = childrenGameFeild.remove(key);
			size = ag.size();
			for(int i = 0;i<size ;i++){
				ObjectPool.releaseGameField(ag.remove(i));
			}
			ObjectPool.releaseGameFeilds(ag);
		}
		parent.release();
		ObjectPool.releaseGameField(parent);
		childDepth = 1;
	}

	public static void setParent(GameField p) {
		parent = p;
	}

	public static void initChildren(GameField parent, long[] arrayLong, WeightData wd) {
		if(childrenGameFeild == null){
			childrenGameFeild = new HashMap<Integer, ArrayList<GameField>>();
			visit = new int[256];
			childDepth = 1;
		}
		ArrayList<GameField> gamelist = ObjectPool.getGameFields();
		int size = arrayLong.length;
		GameField gf ;
		for (int i = 0; i < size; i++) {
			gf = ObjectPool.getGameField();
			gf = parent.clone();
			gf.initChild(); //子供の時のみ行う初期化
			gamelist.add(gf);
		}
		childrenGameFeild.put(childDepth, gamelist);
		parent.setHaveChildNumber(childDepth); //どの子供を持っているかの番号を渡す
		childDepth++;
	}
	/**
	 * 全ての子供を政調させる
	 * @param gf
	 */
	public GameField growUpGameField(GameField gf, long num, boolean first, WeightData wd){
		boolean flag = false;
		while(true){
			if(first){
				if(num != 0)
					flag = true;
				gf.updatePlace(num);
			}else{
				gf.useSimulationBarancing(InitSetting.LEARNING, wd);
				if(gf.getYaku() != 0)
					flag = true;
			}
			if (gf.checkGoalPlayer()) { // 上がった人の判定
				gf.setCanGrowUpTree(false);//木を成長できないように変更する
			}
			gf.endTurn();// ターン等々の処理
			if(flag)
				break;
		}
		return gf;
	}

}
