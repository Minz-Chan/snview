package com.starnet.snview.protocol.message;

public final class Constants {
	public interface MSG_TYPE {
		static final int VERSION_INFO_REQUEST = 40;
		static final int LOGIN_REQUEST = 41;
		static final int LOGIN_ANSWER = 42;
		static final int PHONE_INFO_REQUEST = 72;
		static final int DVS_INFO_REQUEST = 70;
		static final int CHANNEL_ANSWER = 65;
		static final int CONTROL_REQUEST = 51;
		static final int CONTROL_ANSWER = 52;
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
		static final int CONTROL_ANSWER = 4;
		static final int CONTROL_REQUEST = 8;
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
	
	public interface OWSP_ACTION_CODE {
		static final int OWSP_ACTION_MD_STOP = 0;            // 停止运动
		static final int OWSP_ACTION_FOCAL_LENGTH_DEC=5;        // 缩小
		static final int OWSP_ACTION_FOCAL_LENGTH_INC=6;  // 放大
		static final int OWSP_ACTION_FOCUS_INC=7;   //焦距
		static final int OWSP_ACTION_FOCUS_DEC=8;
		static final int OWSP_ACTION_MD_UP=9;                // 向上
		static final int OWSP_ACTION_MD_DOWN=10;             // 向下
		static final int OWSP_ACTION_MD_LEFT=11;             // 向左
		static final int OWSP_ACTION_MD_RIGHT=12;            // 向右
		static final int OWSP_ACTION_APERTURE_INC = 13;  		//光圈
		static final int OWSP_ACTION_APERTURE_DEC = 14;     //
		static final int OWSP_ACTION_AUTO_CRUISE = 15;		//自动巡航
		static final int OWSP_ACTION_GOTO_PRESET_POSITION = 16; 	//跳转预置位
		static final int OWSP_ACTION_SET_PRESET_POSITION = 17;	//设置预置位点
		static final int OWSP_ACTION_CLEAR_PRESET_POSITION = 18; //清除预置位点
		static final int OWSP_ACTION_ACTION_RESET = 20;
		static final int OWSP_ACTION_TV_SWITCH = 128;	//切换视频源,消息参数为INT*,1--TV, 2--SV,3--CV1, 4--CV2 
		static final int OWSP_ACTION_TV_TUNER = 129;		//切换频道, 消息参数为INT*, 为频道号
		static final int OWSP_ACTION_TV_SET_QUALITY  = 130;		//画质设置, 亮度,色度,饱和度,对比度结构体
	}
}
