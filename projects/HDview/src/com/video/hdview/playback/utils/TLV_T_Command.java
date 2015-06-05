package com.video.hdview.playback.utils;

public class TLV_T_Command {
	public final static int TLV_T_VERSION_INFO_ANSWER = 39;
	public final static int TLV_T_VERSION_INFO_REQUEST = 40;
	public final static int TLV_T_LOGIN_REQUEST = 41;
	public final static int TLV_T_LOGIN_ANSWER = 42;
	public final static int TLV_T_TOTAL_CHANNEL = 43;		//NOT USED
	public final static int TLV_T_SENDDATA_REQUEST = 44;		//通道请求
	public final static int TLV_T_SENDDATA_ANSWER = 45;		//通道请求应答
	public final static int TLV_T_TOTAL_CHANEL_ANSWER = 46;	//Not used
	public final static int TLV_T_SUSPENDSENDDATA_REQUEST = 47;		//停止发送数据
	public final static int TLV_T_SUSPENDSENDDATA_ANSWER = 48;
	public final static int TLV_T_DEVICE_KEEP_ALIVE	= 49;		//心跳包
	public final static int TLV_T_DEVICE_FORCE_EXIT	= 50;		
	public final static int TLV_T_CONTROL_REQUEST = 51;		//云台等控制请求
	public final static int TLV_T_CONTROL_ANSWER = 52;		//云台等响应
	public final static int TLV_T_RECORD_REQUEST = 53;		//录像请求
	public final static int TLV_T_RECORD_ANSWER	= 54;
	public final static int TLV_T_DEVICE_SETTING_REQUEST = 55;		//设备参数设置请求
	public final static int TLV_T_DEVICE_SETTING_ANSWER	= 56;		//设备参数设置应答
	public final static int TLV_T_KEEP_ALIVE_ANSWER	= 57;		//心跳包响应
	public final static int TLV_T_DEVICE_RESET = 58;		//通知设备重启
	public final static int TLV_T_DEVICE_RESET_ANSWER = 59;	//设备接收到重启命令后的响应，通常不用发出
	public final static int TLV_T_ALERT_REQUEST = 60;   //报警请求，由设备发出
	public final static int TLV_T_ALERT_ANSWER = 61;    //报警请求回应，由服务器发出，通常可以不用发出
	public final static int TLV_T_ALERT_SEND_PHOTO = 62;   //报警后，设备采集当时的图片，发送到服务器
	public final static int TLV_T_ALERT_SEND_PHOTO_ANSWER = 63;   //设备发送MSG_CMD_ALERT_SEND_PHOTO后，服务器的回应
	public final static int TLV_T_CHANNLE_REQUEST = 64;   		///切换到另一通道
	public final static int TLV_T_CHANNLE_ANSWER = 65;   		//切换另一通道应答
	public final static int TLV_T_SUSPEND_CHANNLE_REQUEST = 66;   		//挂起某一通道
	public final static int TLV_T_SUSPEND_CHANNLE_ANSWER = 67;   		//应答
	public final static int TLV_T_VALIDATE_REQUEST = 68;   		//程序验证请求
	public final static int TLV_T_VALIDATE_ANSWER = 69;   		//应答
	public final static int TLV_T_DVS_INFO_REQUEST = 70;		//设备DVS通知连接方设备信息请求
	public final static int TLV_T_DVS_INFO_ANSWER = 71;			//
	public final static int TLV_T_PHONE_INFO_REQUEST = 72;			//手机通知连接方手机信息请求
	public final static int TLV_T_PHONE_INFO_ANSWER	= 73;			//

	//vod & live
	public final static int TLV_T_AUDIO_INFO	= 0x61;   //97		音频信息, 表示V为音频信息
	public final static int TLV_T_AUDIO_DATA	= 0x62;   //98		音频数据, 表示V为音频数据
	public final static int TLV_T_VIDEO_FRAME_INFO = 0x63;   //99    视频帧信息, 表示V的数据描述帧信息
	public final static int TLV_T_VIDEO_IFRAME_DATA = 0x64;  //100   视频关键帧数据，表示V的数据为关键帧
	public final static int TLV_T_VIDEO_PFRAME_DATA = 0x66;  //102   视频P帧(参考帧)数据, 表示V的数据为参考帧
	public final static int TLV_T_VIDEO_FRAME_INFO_EX = 0x65;  //101   扩展视频帧信息支持>=64KB的视频帧
	public final static int TLV_T_STREAM_FORMAT_INFO	= 200;    ///			流格式信息 ,描述视频属性,音频属性
	public final static int TLV_T_PLAY_RECORD_RSP 	= 337;    ///			流格式信息 ,描述视频属性,音频属性
	public final static int TLV_T_RECORD_EOF 	= 354;    ///			流格式信息 ,描述视频属性,音频属性
	public final static int TLV_V_SEARCHRECORD = 334;    ///			流格式信息 ,描述视频属性,音频属性
	public final static int TLV_V_RECORDINFO = 335;    ///			流格式信息 ,描述视频属性,音频属性

	//vod
	public final static int TLV_T_STREAM_FILE_INDEX = 213;
	public final static int TLV_T_STREAM_FILE_ATTRIBUTE = 214;
	public final static int TLV_T_STREAM_FILE_END = 0x0000FFFF;

}
