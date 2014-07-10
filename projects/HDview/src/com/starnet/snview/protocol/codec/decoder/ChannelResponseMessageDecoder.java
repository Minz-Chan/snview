package com.starnet.snview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.ChannelResponse;
import com.starnet.snview.protocol.message.Constants;

public class ChannelResponseMessageDecoder extends
		AbstractMessageDecoder<ChannelResponse> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.CHANNEL_ANSWER;
	}

	@Override
	protected ChannelResponse decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		ChannelResponse cr = new ChannelResponse();
		
		cr.setResult(body.getShort());
		cr.setCurrentChannel(body.get());
		cr.setReserve(body.get());
		
		return cr;
	}

}
