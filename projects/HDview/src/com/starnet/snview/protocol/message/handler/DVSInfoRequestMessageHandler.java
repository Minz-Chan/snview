package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.protocol.message.DVSInfoRequest;

public class DVSInfoRequestMessageHandler implements
		MessageHandler<DVSInfoRequest> {
	private final AttributeKey LIVEVIEW_ITEM = new AttributeKey(Connection.class, "liveview_item");
	private final AttributeKey CONNECTION_LISTENER = new AttributeKey(Connection.class, "connection_listener");
	
	@Override
	public void handleMessage(IoSession session, DVSInfoRequest message)
			throws Exception {
		System.out.println("DVSInfoRequest is arrived...");	
		
		LiveViewItemContainer liveViewItemConatainer = (LiveViewItemContainer)  session.getAttribute(LIVEVIEW_ITEM);
		StatusListener connectionStatusListener = (StatusListener) session.getAttribute(CONNECTION_LISTENER);
		if (liveViewItemConatainer != null && connectionStatusListener != null) {
			connectionStatusListener.OnConnectionBusy(liveViewItemConatainer);
		}
	}

}
