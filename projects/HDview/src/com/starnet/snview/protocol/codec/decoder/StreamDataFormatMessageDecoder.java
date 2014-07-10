package com.starnet.snview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.OwspAudioDataFormat;
import com.starnet.snview.protocol.message.OwspVideoDataFormat;
import com.starnet.snview.protocol.message.StreamDataFormat;

public class StreamDataFormatMessageDecoder extends
		AbstractMessageDecoder<StreamDataFormat> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.STREAM_FORMAT_INFO;
	}

	@Override
	protected StreamDataFormat decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		StreamDataFormat s = new StreamDataFormat();
		
		s.setVideoChannel(body.get());
		s.setAudioChannel(body.get());
		s.setDataType(body.get());
		s.setReserve(body.get());
		
		OwspVideoDataFormat v = new OwspVideoDataFormat();
		v.setCodecId(body.getInt());
		v.setBitrate(body.getInt());
		v.setWidth(body.getShort());
		v.setHeight(body.getShort());
		v.setFramerate(body.get());
		v.setColorDepth(body.get());
		v.setReserve(body.getShort());
		s.setVideoDataFormat(v);
		
		OwspAudioDataFormat a = new OwspAudioDataFormat();
		a.setSamplesPerSecond(body.getInt());
		a.setBitrate(body.getInt());
		a.setWaveForamt(body.getShort());
		a.setChannelNumber(body.getShort());
		a.setBlockAlign(body.getShort());
		a.setBitsPerSample(body.getShort());
		a.setFrameInterval(body.getShort());
		a.setReserve(body.getShort());
		s.setAudioDataFormat(a);
		
		return s;
	}

}
