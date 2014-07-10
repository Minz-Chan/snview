package com.starnet.snview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.VideoPFrameData;

public class VideoPFrameDataMessageDecoder extends
		AbstractMessageDecoder<VideoPFrameData> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.VIDEO_PFRAME_DATA;
	}

	@Override
	protected VideoPFrameData decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		return new VideoPFrameData(body.array());
	}

}
