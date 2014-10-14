package object;

import java.util.ArrayList;
import java.util.List;

import simulationBransing.GameField;
import simulationBransing.SimulationBalancing;

public class ObjectPool {

	public static SimulationBalancing sb = new SimulationBalancing();

	private static List<GameField> arrayGameField;
	private static List<ArrayList<GameField>> arrayGameFields;

	private static List<int[]> pair1;
	private static List<int[]> pair2;
	private static List<int[]> pair3;
	private static List<int[]> pair4;
	private static List<int[]> pair5;
	private static List<int[]> pair6;
	private static List<int[]> pair7;
	private static List<int[]> pair8;
	private static List<int[]> pair9;
	private static List<int[]> pair10;
	private static List<int[]> pair11;
	
	private static List<ArrayList<Integer>> arrayInt;
	private static List<double[]> arrayDouble;

	private static List<long[]> playersHands;
	private static List<long[]> conbine;

	private static ArrayList<ArrayList<Long>> putHand;

	public static ArrayList<GameField> getGameFields(){
		if(arrayGameFields == null)
			arrayGameFields = new ArrayList<ArrayList<GameField>>(64);
		int size = arrayGameFields.size();
		if(size == 0){
			return new ArrayList<GameField>(64);
		}else{
			return arrayGameFields.remove(size-1);
		}
	}
	public static void releaseGameFeilds(ArrayList<GameField> array){
		array.clear();
		arrayGameFields.add(array);
	}
	
	public static ArrayList<Integer> getArrayInt(){
		if(arrayInt == null){
			arrayInt = new ArrayList<ArrayList<Integer>>(32);
		}
		int size = arrayInt.size();
		if(size == 0){
			return new ArrayList<Integer>(64);
		}else{
			return arrayInt.get(size-1);
		}
	}
	public static void releaseArrayInt(ArrayList<Integer> array){
		array.clear();
		arrayInt.add(array);
	}

	public static double[] getArrayDouble(){
		if(arrayDouble == null){
			arrayDouble = new ArrayList<double[]>(32);
		}
		int size = arrayDouble.size();
		if(size == 0){
			return new double[64];
		}else{
			return arrayDouble.get(size-1);
		}
	}
	public static void releaseArrayDouble(double[] array){
		arrayDouble.add(array);
	}
	
	
	public static GameField getGameField(){
		if(arrayGameField == null)
			arrayGameField = new ArrayList<GameField>(256);
		int size = arrayGameField.size();
		if(size == 0 ){
			return new GameField();
		}else{
			return arrayGameField.remove(size -1);
		}
	}

	public static void releaseGameField(GameField gf){
		arrayGameField.add(gf);
	}




	public static int[] getArrayInt(int size){
		List<int[]> list = getArrayIntList(size);
		int arraySize = list.size();
		if(arraySize == 0){
			return new int[size];
		}else{
			return list.remove(arraySize-1);
		}

	}
	public static void releaseArrayInt(int[] array){
		int size = array.length;
		List<int[]> list = getArrayIntList(size);
		list.add(array);
	}
	public static long[] getConbine(){
		if(conbine == null){
			conbine = new ArrayList<long[]>(8);
		}
		int arraySize = conbine.size();
		if(arraySize == 0){
			return new long[5];
		}else{
			return conbine.remove(arraySize-1);
		}

	}
	public static void releaseConbine(long[] array){
		conbine.add(array);
	}


	public static long[] getPLayersHands(){
		if(playersHands == null){
			playersHands = new ArrayList<long[]>(128);
		}
		int arraySize = playersHands.size();
		if(arraySize == 0){
			return new long[5];
		}else{
			return playersHands.remove(arraySize-1);
		}

	}
	public static void releasePLayersHands(long[] array){
		playersHands.add(array);
	}
	public static ArrayList<Long> getPutHand(){
		if( putHand == null){
			putHand = new ArrayList<ArrayList<Long>>(8);
		}
		int arraySize = putHand.size();
		if(arraySize == 0){
			return new ArrayList<Long>(64);
		}else{
			return putHand.remove(arraySize-1);
		}

	}
	public static void releasePutHand(ArrayList<Long> array){
		array.clear();
		putHand.add(array);
	}


	/**
	 * 欲しい配列の大きさを受け取って、空の配列を渡す
	 *
	 * @param size
	 * @return
	 */
	private static List<int[]> getArrayIntList(int size) {
		List<int[]> list = null;
		switch (size) {
		case 1:
			if(pair1 == null)
				pair1 = new ArrayList<int[]>(64);
			list  = pair1;
			break;
		case 2:
			if(pair2 == null)
				pair2 = new ArrayList<int[]>(64);
			list  = pair2;
			break;
		case 3:
			if(pair3 == null)
				pair3 = new ArrayList<int[]>(64);
			list  = pair3;
			break;
		case 4:
			if(pair4 == null)
				pair4 = new ArrayList<int[]>(64);
			list  = pair4;
			break;
		case 5:
			if(pair5 == null)
				pair5 = new ArrayList<int[]>(64);
			list  = pair5;
			break;
		case 6:
			if(pair6 == null)
				pair6 = new ArrayList<int[]>(64);
			list  = pair6;
			break;
		case 7:
			if(pair7 == null)
				pair7 = new ArrayList<int[]>(64);
			list  = pair7;
			break;
		case 8:
			if(pair8 == null)
				pair8 = new ArrayList<int[]>(64);
			list  = pair8;
			break;
		case 9:
			if(pair9 == null)
				pair9 = new ArrayList<int[]>(64);
			list  = pair9;
			break;
		case 10:
			if(pair10 == null)
				pair10 = new ArrayList<int[]>(64);
			list  = pair10;
			break;
		case 11:
			if(pair11 == null)
				pair11 = new ArrayList<int[]>(64);
			list  = pair11;
			break;

		default:
			break;
		}
		return list;
	}

}
