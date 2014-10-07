package jp.ac.hosei.daihinmin.fujita2;

public class TeruteruMain {
	public static void main(String[] args) {
		/*
		String[] options = {
				"-mode",
				"alone",
				"-players",
				//"jp.ac.hosei.daihinmin.minegishi.StrategyBot",
				"Sokosoko",
				"Teruteru",
				"Teruteru",
				"Sokosoko",		
				"Nakanaka",
		};
		jp.ac.uec.daihinmin.UECda07.main(options);
		 */
		String[] options = {
				"-player",
				"Teruteru",
				"-port",
				"42400"
		};
		jp.ac.uec.daihinmin.net.TableClient.main(options);
}
}
