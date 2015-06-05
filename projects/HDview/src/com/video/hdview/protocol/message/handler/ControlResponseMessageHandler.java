package com.video.hdview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import android.util.Log;

import com.video.hdview.protocol.message.ControlResponse;

public class ControlResponseMessageHandler implements
		MessageHandler<ControlResponse> {
	private static final String TAG = "ControlResponseMessageHandler";
		
	@Override
	public void handleMessage(IoSession session, ControlResponse message)
			throws Exception {
		Log.i(TAG, "ControlResponse arrived... result: " + message.getResult());		
	}

}
