package com.starnet.snview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.VideoFrameInfo;

public class VideoFrameInfoMessageDecoder extends
		AbstractMessageDecoder<VideoFrameInfo> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.VIDEO_FRAME_INFO;
	}

	@Override
	protected VideoFrameInfo decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		VideoFrameInfo v = new VideoFrameInfo();
		v.setChannelId(body.get());
		v.setReserve(body.get());
		v.setCheckSum(body.getShort());
		v.setFrameIndex(body.getInt());
		v.setTime(body.getInt());
		
		return v;
	}

}
