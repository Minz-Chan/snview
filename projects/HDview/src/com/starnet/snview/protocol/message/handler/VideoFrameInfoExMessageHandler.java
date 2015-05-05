package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import android.util.Log;

import com.starnet.snview.protocol.message.VideoFrameInfoEx;

public class VideoFrameInfoExMessageHandler implements
		MessageHandler<VideoFrameInfoEx> {
	private static final String TAG = "VideoFrameInfoExMessageHandler";
	
	private AttributeKey ONE_FRAME_BUFFER = new AttributeKey(VideoFrameInfoExMessageHandler.class, "oneFrameBuffer");
	private AttributeKey ONE_FRAME_BUFFER_SIZE = new AttributeKey(VideoFrameInfoExMessageHandler.class, "oneFrameBufferSize");
	private AttributeKey DATA_EXCEED_64KB = new AttributeKey(VideoFrameInfoExMessageHandler.class, "dataExceed64Kb");

	@Override
	public void handleMessage(IoSession session, VideoFrameInfoEx message)
			throws Exception {
		Log.d(TAG, "VideoFrameInfoEx is arrived...");
		
		session.setAttribute(DATA_EXCEED_64KB, Boolean.valueOf(true));
		
		Integer oneIFrameDataSize = Integer.valueOf(message.getDataSize());
		session.setAttribute(ONE_FRAME_BUFFER_SIZE, oneIFrameDataSize);
		
		IoBuffer oneIFrameBuffer = (IoBuffer) session.getAttribute(ONE_FRAME_BUFFER);
		if (oneIFrameBuffer == null) {
			oneIFrameBuffer = IoBuffer.allocate(message.getDataSize());
		}
		oneIFrameBuffer.clear();
		if (oneIFrameBuffer.remaining() < oneIFrameDataSize) {
			oneIFrameBuffer.expand(oneIFrameDataSize);
		}
		session.setAttribute(ONE_FRAME_BUFFER, oneIFrameBuffer);
		
	}

}
