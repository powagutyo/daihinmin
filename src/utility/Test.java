package utility;

public class Test {
	private static final long ONE =1;
	public static void main(String[] args) {


		long a = (long) 1;
		for(int i = 0;i<3;i++){
			a = a | (a << (i * 13));
		}
		a = a << 1;
		System.out.println(Long.toBinaryString(a));
		System.out.println(a);
	}
}
