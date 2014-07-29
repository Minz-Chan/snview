package com.starnet.snview.protocol.codec.factory;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import com.starnet.snview.protocol.codec.decoder.ChannelResponseMessageDecoder;
import com.starnet.snview.protocol.codec.decoder.DVSInfoRequestMessageDecoder;
import com.starnet.snview.protocol.codec.decoder.StreamDataFormatMessageDecoder;
import com.starnet.snview.protocol.codec.decoder.VersionInfoRequestMessageDecoder;
import com.starnet.snview.protocol.codec.decoder.VideoFrameInfoExMessageDecoder;
import com.starnet.snview.protocol.codec.decoder.VideoFrameInfoMessageDecoder;
import com.starnet.snview.protocol.codec.decoder.VideoIFrameDataMessageDeocder;
import com.starnet.snview.protocol.codec.decoder.VideoPFrameDataMessageDecoder;
import com.starnet.snview.protocol.codec.encoder.LoginRequestMessageEncoder;
import com.starnet.snview.protocol.codec.encoder.OwspBeginMessageEncoder;
import com.starnet.snview.protocol.codec.encoder.OwspEndMessageEncoder;
import com.starnet.snview.protocol.codec.encoder.PhoneInfoRequestMessageEncoder;
import com.starnet.snview.protocol.codec.encoder.VersionInfoRequestMessageEncoder;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.VersionInfoRequest;


public class TlvMessageFactory extends DemuxingProtocolCodecFactory {

	public TlvMessageFactory() {
		// Register decoder
		addMessageDecoder(new VersionInfoRequestMessageDecoder());
		addMessageDecoder(new DVSInfoRequestMessageDecoder());
		addMessageDecoder(new ChannelResponseMessageDecoder());
		addMessageDecoder(new StreamDataFormatMessageDecoder());
		addMessageDecoder(new VideoFrameInfoMessageDecoder());
		addMessageDecoder(new VideoFrameInfoExMessageDecoder());
		addMessageDecoder(new VideoIFrameDataMessageDeocder());
		addMessageDecoder(new VideoPFrameDataMessageDecoder());
		
		// Register encoder
		//addMessageEncoder(ByteBuffer.class, new TlvMessageEncoder());
		addMessageEncoder(OwspBegin.class, new OwspBeginMessageEncoder());
		addMessageEncoder(OwspEnd.class, new OwspEndMessageEncoder());
		addMessageEncoder(VersionInfoRequest.class, new VersionInfoRequestMessageEncoder());
		addMessageEncoder(PhoneInfoRequest.class, new PhoneInfoRequestMessageEncoder());
		addMessageEncoder(LoginRequest.class, new LoginRequestMessageEncoder());
		
		
	}
}
