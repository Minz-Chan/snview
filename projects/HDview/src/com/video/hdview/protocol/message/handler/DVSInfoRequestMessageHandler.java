package com.video.hdview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.video.hdview.component.liveview.LiveViewItemContainer;
import com.video.hdview.protocol.Connection;

import com.video.hdview.protocol.message.DVSInfoRequest;

public class DVSInfoRequestMessageHandler implements
		MessageHandler<DVSInfoRequest> {
//	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");

	private Connection connection;
	
	@Override
	public void handleMessage(IoSession session, DVSInfoRequest message)
			throws Exception {
		System.out.println("DVSInfoRequest is arrived...");	
		
//		if (connection == null) {
//			connection = (Connection) session.getAttribute(CONNECTION);
//		}
//		
//		LiveViewItemContainer liveViewItemConatainer = connection.getLiveViewItemContainer();
//		StatusListener connectionStatusListener = connection.getConnectionListener();
//		if (liveViewItemConatainer != null && connectionStatusListener != null) {
//			connectionStatusListener.OnConnectionBusy(liveViewItemConatainer);
//		}
	}

}
