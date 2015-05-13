package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.R;
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
			lvContainer.setWindowInfoContent(R.string.connection_response_login_success);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_USER_PWD_ERROR:		// 用户名或密码错
			lvContainer.setWindowInfoContent(R.string.connection_response_user_pwd_error);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_PDA_VERSION_ERROR:	// 版本不一致
			lvContainer.setWindowInfoContent(R.string.connection_response_pda_version_error);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_MAX_USER_ERROR:	    // 已达最大用户数
			lvContainer.setWindowInfoContent(R.string.connection_response_max_user_error);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_OFFLINE:		// 设备已经离线
			lvContainer.setWindowInfoContent(R.string.connection_response_device_offline);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_HAS_EXIST:	// 设备已经存在
			lvContainer.setWindowInfoContent(R.string.connection_response_device_has_exist);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_OVERLOAD:		// 设备性能超载(设备忙)
			lvContainer.setWindowInfoContent(R.string.connection_response_device_overload);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_INVALID_CHANNLE:		// 设备不支持的通道
			lvContainer.setWindowInfoContent(R.string.connection_response_invalid_channel);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_PROTOCOL_ERROR:		// 协议解析出错
			lvContainer.setWindowInfoContent(R.string.connection_response_protocol_error);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_NOT_START_ENCODE:	// 未启动编码
			lvContainer.setWindowInfoContent(R.string.connection_response_not_start_encode);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_TASK_DISPOSE_ERROR:	// 任务处理过程出错
			lvContainer.setWindowInfoContent(R.string.connection_response_task_dispose_error);
			lvContainer.setIsResponseError(true);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_NO_PERMISSION:  		// 无权限
			lvContainer.setWindowInfoContent(R.string.connection_response_no_permission);
			lvContainer.setIsResponseError(true);
			break;
		default: // 兼容旧版，登录服务器失败，原因即用户或密码错误
			lvContainer.setWindowInfoContent(R.string.connection_response_user_pwd_error);
			lvContainer.setIsResponseError(true);
			break;
		}
		
	}

}
