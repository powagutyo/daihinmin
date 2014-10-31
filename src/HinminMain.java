public class HinminMain {
	public static void main(String[] args) {
		String[] options = { "-mode", "alone", "-games", "1000", "-players",

				"jp.ac.uec.daihinmin.player.MCPlayer",
				//"jp.ac.uec.daihinmin.player.UCTPlayer_H",
				"jp.ac.uec.daihinmin.player.Nakanaka",
				"jp.ac.uec.daihinmin.player.Nakanaka",
				// "jp.ac.uec.daihinmin.player.UCTPlayer_H",
				 //"jp.ac.uec.daihinmin.player.UCTPlayer_H",
				//."jp.ac.uec.daihinmin.player.UCTPlayer_H",

				// "jp.ac.uec.daihinmin.player.Teruteru",
				"jp.ac.uec.daihinmin.player.UCT",
				"jp.ac.uec.daihinmin.player.UCTPlayer_H",
				// "jp.ac.hosei.daihinmin.ManualPlayer",
		};
		jp.ac.uec.daihinmin.UECda07.main(options);
	}
}
