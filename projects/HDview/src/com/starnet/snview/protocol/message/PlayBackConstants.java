package com.starnet.snview.protocol.message;

public class PlayBackConstants {
	
	public interface MSG_TYPE {
		static final int RECORD_REQUEST = 333;	
		static final int RECORD_ANSWER = 334;
		static final int PLAY_RECORD_REQUEST = 336;
		static final int PLAY_RECORD_ANSWER = 337;
	}

	public interface MSG_LEN {
		static final int RECORD_REQUEST = 24;
		static final int PLAY_RECORD_ANSWER = 14;
	}
}