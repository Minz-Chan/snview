package com.starnet.snview.global;

public interface Constants {
	public enum PREVIEW_MODE {
		SINGLE_CHANNEL,
		MUL_CHANNEL
	}
	
	public static final int TAKE_PICTURE = 0x88888801;  // 拍照
	
	public interface ARROW {
		public static final int LEFT = 0;
		public static final int LEFT_DOWN = 1;
		public static final int DOWN = 2;
		public static final int RIGHT_DOWN = 3;
		public static final int RIGHT = 4;
		public static final int RIGHT_UP = 5;
		public static final int UP = 6;
		public static final int LEFT_UP = 7;
		
	}
}
