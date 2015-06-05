package com.video.hdview.protocol.codec.factory;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import com.video.hdview.protocol.codec.decoder.ChannelResponseMessageDecoder;
import com.video.hdview.protocol.codec.decoder.ControlResponseMessageDecoder;
import com.video.hdview.protocol.codec.decoder.DVSInfoRequestMessageDecoder;
import com.video.hdview.protocol.codec.decoder.LoginResponseMessageDecoder;
import com.video.hdview.protocol.codec.decoder.StreamDataFormatMessageDecoder;
import com.video.hdview.protocol.codec.decoder.VersionInfoRequestMessageDecoder;
import com.video.hdview.protocol.codec.decoder.VideoFrameInfoExMessageDecoder;
import com.video.hdview.protocol.codec.decoder.VideoFrameInfoMessageDecoder;
import com.video.hdview.protocol.codec.decoder.VideoIFrameDataMessageDeocder;
import com.video.hdview.protocol.codec.decoder.VideoPFrameDataMessageDecoder;
import com.video.hdview.protocol.codec.encoder.ControlRequestMessageEncoder;
import com.video.hdview.protocol.codec.encoder.LoginRequestMessageEncoder;
import com.video.hdview.protocol.codec.encoder.OwspBeginMessageEncoder;
import com.video.hdview.protocol.codec.encoder.OwspEndMessageEncoder;
import com.video.hdview.protocol.codec.encoder.PhoneInfoRequestMessageEncoder;
import com.video.hdview.protocol.codec.encoder.VersionInfoRequestMessageEncoder;
import com.video.hdview.protocol.message.ControlRequest;
import com.video.hdview.protocol.message.LoginRequest;
import com.video.hdview.protocol.message.OwspBegin;
import com.video.hdview.protocol.message.OwspEnd;
import com.video.hdview.protocol.message.PhoneInfoRequest;
import com.video.hdview.protocol.message.VersionInfoRequest;


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
		addMessageDecoder(new LoginResponseMessageDecoder());
		addMessageDecoder(new ControlResponseMessageDecoder());
		
		// Register encoder
		//addMessageEncoder(ByteBuffer.class, new TlvMessageEncoder());
		addMessageEncoder(OwspBegin.class, new OwspBeginMessageEncoder());
		addMessageEncoder(OwspEnd.class, new OwspEndMessageEncoder());
		addMessageEncoder(VersionInfoRequest.class, new VersionInfoRequestMessageEncoder());
		addMessageEncoder(PhoneInfoRequest.class, new PhoneInfoRequestMessageEncoder());
		addMessageEncoder(LoginRequest.class, new LoginRequestMessageEncoder());
		addMessageEncoder(ControlRequest.class, new ControlRequestMessageEncoder());
		
		
	}
}
