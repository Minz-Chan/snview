package com.starnet.snview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.VideoIFrameData;

public class VideoIFrameDataMessageDeocder extends
		AbstractMessageDecoder<VideoIFrameData> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.VIDEO_IFRAME_DATA;
	}

	@Override
	protected VideoIFrameData decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		return new VideoIFrameData(body.array());
	}

}
