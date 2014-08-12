package com.starnet.snview.protocol.message;

public final class Constants {
	public interface MSG_TYPE {
		static final int VERSION_INFO_REQUEST = 40;
		static final int LOGIN_REQUEST = 41;
		static final int LOGIN_ANSWER = 42;
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
		static final int LOGIN_ANSWER = 4;
		static final int PHONE_INFO_REQUEST = 36;
		static final int DVS_INFO_REQUEST = 72;
		static final int CHANNEL_ANSWER = 4;
		static final int STREAM_FORMAT_INFO = 40;
		static final int VIDEO_FRAME_INFO = 12;
		static final int VIDEO_FRAME_INFO_EX = 16;
	}
	
	public interface RESPONSE_CODE {
		static final int _RESPONSECODE_SUCC	= 0x01;					//成功
		static final int _RESPONSECODE_USER_PWD_ERROR = 0x02;		//用户名或密码错
		static final int _RESPONSECODE_PDA_VERSION_ERROR = 0x04;	//版本不一致
		static final int _RESPONSECODE_MAX_USER_ERROR = 0x05;	
		static final int _RESPONSECODE_DEVICE_OFFLINE = 0x06;		//设备已经离线
		static final int _RESPONSECODE_DEVICE_HAS_EXIST = 0x07;		//设备已经存在
		static final int _RESPONSECODE_DEVICE_OVERLOAD = 0x08;		//设备性能超载(设备忙)
		static final int _RESPONSECODE_INVALID_CHANNLE = 0x09;		//设备不支持的通道	
		static final int _RESPONSECODE_PROTOCOL_ERROR = 0X0A;		//协议解析出错
		static final int _RESPONSECODE_NOT_START_ENCODE =0X0B;		//未启动编码
		static final int _RESPONSECODE_TASK_DISPOSE_ERROR = 0X0C;	//任务处理过程出错
		
	}
}
