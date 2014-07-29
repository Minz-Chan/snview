package com.starnet.snview.protocol.message;

public final class Constants {
	public interface MSG_TYPE {
		static final int VERSION_INFO_REQUEST = 40;
		static final int LOGIN_REQUEST = 41;
		static final int PHONE_INFO_REQUEST = 72;
		static final int DVS_INFO_REQUEST = 70;
		static final int CHANNEL_ANSWER = 65;
		static final int STREAM_FORMAT_INFO = 200;
		static final int VIDEO_FRAME_INFO = 99;
		static final int VIDEO_FRAME_INFO_EX = 101;
		static final int VIDEO_IFRAME_DATA = 100;
		static final int VIDEO_PFRAME_DATA = 102;		
	}
	
	public interface MSG_LEN {
		static final int VERSION_INFO_REQUEST = 4;
		static final int LOGIN_REQUEST = 56;
		static final int PHONE_INFO_REQUEST = 36;
		static final int DVS_INFO_REQUEST = 72;
		static final int CHANNEL_ANSWER = 4;
		static final int STREAM_FORMAT_INFO = 40;
		static final int VIDEO_FRAME_INFO = 12;
		static final int VIDEO_FRAME_INFO_EX = 16;
	}
}
