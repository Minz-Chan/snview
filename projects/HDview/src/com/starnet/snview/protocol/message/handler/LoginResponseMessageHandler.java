package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.LoginResponse;

public class LoginResponseMessageHandler implements
		MessageHandler<LoginResponse> {
//	private final AttributeKey LIVEVIEW_ITEM = new AttributeKey(Connection.class, "liveview_item");
	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");
	
	private Connection connection;
	private LiveViewItemContainer lvContainer;
	@Override
	public void handleMessage(IoSession session, LoginResponse message)
			throws Exception {
		
		if (connection == null) {
			connection = (Connection) session.getAttribute(CONNECTION);
		}
		
		if (lvContainer == null) {
			lvContainer = connection.getLiveViewItemContainer();
		}
		
		int result = message.getResult();
		
		switch (result) {
		case Constants.RESPONSE_CODE._RESPONSECODE_SUCC:				// 登录服务器成功
			lvContainer.setWindowInfoContent("登录服务器成功");
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_USER_PWD_ERROR:		// 用户名或密码错
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_PDA_VERSION_ERROR:	// 版本不一致
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_MAX_USER_ERROR:	    // 已达最大用户数
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_OFFLINE:		// 设备已经离线
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_HAS_EXIST:	// 设备已经存在
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_OVERLOAD:		// 设备性能超载(设备忙)
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_INVALID_CHANNLE:		// 设备不支持的通道
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_PROTOCOL_ERROR:		// 协议解析出错
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_NOT_START_ENCODE:	// 未启动编码
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_TASK_DISPOSE_ERROR:	// 任务处理过程出错
			break;
		default: // 兼容旧版，登录服务器失败，原因即用户或密码错误
			lvContainer.setWindowInfoContent("用户或密码错误");
			break;
		}
		
	}

}
