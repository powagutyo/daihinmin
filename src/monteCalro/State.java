package monteCalro;

public enum State{// 役の種類
	/** 初期状態 **/
	EMPTY,
	/** 新しく場に回ってきた状態 **/
	RENEW,
	/** 場に1枚出しが出されている状態 **/
	SINGLE,
	/** 場に複数のペア出しが出されている状態 **/
	GROUP,
	/** 場に階段出しが出されている状態 **/
	SEQUENCE
}

