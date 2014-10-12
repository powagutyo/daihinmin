package utility;

public class Test {
	private static final long ONE =1;
	public static void main(String[] args) {
		long a = Long.MAX_VALUE;
		long b = (31<< 5);
		System.out.println(Long.bitCount(b));
		System.out.println(Long.toBinaryString(b));
	}
}
