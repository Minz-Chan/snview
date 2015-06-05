package com.video.hdview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.video.hdview.protocol.message.Constants;
import com.video.hdview.protocol.message.VideoFrameInfoEx;

public class VideoFrameInfoExMessageDecoder extends
		AbstractMessageDecoder<VideoFrameInfoEx> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.VIDEO_FRAME_INFO_EX;
	}

	@Override
	protected VideoFrameInfoEx decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		VideoFrameInfoEx v = new VideoFrameInfoEx();
		v.setChannelId(body.get());
		v.setReserve(body.get());
		v.setCheckSum(body.getShort());
		v.setFrameIndex(body.getInt());
		v.setTime(body.getInt());
		v.setDataSize(body.getInt());
		
		return v;
	}

}
