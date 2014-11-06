package object;

import java.util.ArrayList;
import java.util.List;

import simulationBransing.GameField;
import simulationBransing.SimulationBalancing;

public class ObjectPool {

	public static SimulationBalancing sb = new SimulationBalancing();

	private static List<GameField> arrayGameField;
	private static List<ArrayList<GameField>> arrayGameFields;

	private static List<List<int[]>> pair;

	private static List<ArrayList<Integer>> arrayInt;
	private static List<ArrayList<Long>> arrayLong;
	private static List<int[]> weight;
	private static List<double[]> arrayDouble;

	private static List<long[]> playersHands;
	private static List<long[]> conbine;

	private static ArrayList<ArrayList<Long>> putHand;

	public static int[] getWeight() {
		if (weight == null) {
			weight = new ArrayList<int[]>(32);
		}
		int size = weight.size();
		if (size == 0) {
			return new int[InitSetting.WEIGHTNUMBER];
		} else {
			return weight.remove(size - 1);
		}
	}

	public static void releaseWeight(int[] array) {
		weight.add(array);
	}

	public static ArrayList<GameField> getGameFields() {
		if (arrayGameFields == null)
			arrayGameFields = new ArrayList<ArrayList<GameField>>(64);
		int size = arrayGameFields.size();
		if (size == 0) {
			return new ArrayList<GameField>(64);
		} else {
			return arrayGameFields.remove(size - 1);
		}
	}

	public static void releaseGameFeilds(ArrayList<GameField> array) {
		array.clear();
		arrayGameFields.add(array);
	}

	public static ArrayList<Integer> getArrayInt() {
		if (arrayInt == null) {
			arrayInt = new ArrayList<ArrayList<Integer>>(32);
		}
		int size = arrayInt.size();
		if (size == 0) {
			return new ArrayList<Integer>(64);
		} else {
			return arrayInt.remove(size - 1);
		}
	}

	public static void releaseArrayInt(ArrayList<Integer> array) {
		array.clear();
		arrayInt.add(array);
	}

	public static ArrayList<Long> getArrayLong() {
		if (arrayLong == null) {
			arrayLong = new ArrayList<ArrayList<Long>>(32);
		}
		int size = arrayLong.size();
		if (size == 0) {
			return new ArrayList<Long>(128);
		} else {
			return arrayLong.remove(size - 1);
		}
	}

	public static void releaseArrayLong(ArrayList<Long> array) {
		array.clear();
		arrayLong.add(array);
	}

	public static double[] getArrayDouble() {
		if (arrayDouble == null) {
			arrayDouble = new ArrayList<double[]>(64);
		}
		int size = arrayDouble.size();
		if (size == 0) {
			return new double[256];
		} else {
			return arrayDouble.remove(size - 1);
		}
	}

	public static void releaseArrayDouble(double[] array) {
		arrayDouble.add(array);
	}

	public static GameField getGameField() {
		if (arrayGameField == null)
			arrayGameField = new ArrayList<GameField>(256);
		int size = arrayGameField.size();
		if (size == 0) {
			return new GameField();
		} else {
			return arrayGameField.remove(size - 1);
		}
	}

	public static void releaseGameField(GameField gf) {
		arrayGameField.add(gf);
	}

	public static int[] getArrayInt(int size) {
		if (pair == null) {
			pair = new ArrayList<List<int[]>>();
			for (int i = 0; i < 12; i++) {
				pair.add(new ArrayList<int[]>(64));
			}
		}
		int arraySize = pair.get(size).size();
		if (arraySize == 0) {
			return new int[size];
		} else {
			return pair.get(size).remove(arraySize - 1);
		}
	}

	public static void releaseArrayInt(int[] array) {
		int size = array.length;
		pair.get(size).add(array);
	}

	public static long[] getConbine() {
		if (conbine == null) {
			conbine = new ArrayList<long[]>(8);
		}
		int arraySize = conbine.size();
		if (arraySize == 0) {
			return new long[5];
		} else {
			return conbine.remove(arraySize - 1);
		}

	}

	public static void releaseConbine(long[] array) {
		conbine.add(array);
	}

	public static long[] getPLayersHands() {
		if (playersHands == null) {
			playersHands = new ArrayList<long[]>(128);
		}
		int arraySize = playersHands.size();
		if (arraySize == 0) {
			return new long[5];
		} else {
			return playersHands.remove(arraySize - 1);
		}

	}

	public static void releasePLayersHands(long[] array) {
		playersHands.add(array);
	}

	public static ArrayList<Long> getPutHand() {
		if (putHand == null) {
			putHand = new ArrayList<ArrayList<Long>>();
		}
		int arraySize = putHand.size();
		if (arraySize == 0) {
			return new ArrayList<Long>();
		} else {
			return putHand.remove(arraySize - 1);
		}

	}

	public static void releasePutHand(ArrayList<Long> array) {
		array.clear();
		putHand.add(array);
	}

}
