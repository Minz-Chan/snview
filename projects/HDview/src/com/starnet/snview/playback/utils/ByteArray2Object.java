package com.starnet.snview.playback.utils;

public class ByteArray2Object {
	/**
	 * 
	 * @param clazz 需要转换成的Class
	 * @param byteArray byte数组
	 * @param start 数组转换起始位置
	 * @param arrayLength 数组转换长度
	 * @return
	 */
	public static Object convert2Object(Class clazz, byte byteArray[],int start,int arrayLength) {
		byte[] tempByteArray = new byte[arrayLength];
		for(int index=0;index<arrayLength;index++){
			tempByteArray[index] = byteArray[start+index];
		}
		if( clazz == TLV_V_PacketHeader.class ) {
			if(tempByteArray.length != OWSP_LEN.OwspPacketHeader)
				return null;
			TLV_V_PacketHeader owspPacketHeader = new TLV_V_PacketHeader();
			owspPacketHeader.setPacket_length(BByteConvert.bytesToUint(tempByteArray,0));
			owspPacketHeader.setPacket_seq(LByteConvert.bytesToUint(tempByteArray,4));
			return owspPacketHeader;
		}else if( clazz == TLV_HEADER.class){
			if(tempByteArray.length != OWSP_LEN.TLV_HEADER)
				return null;
			int i = 0;
			
//			byte[] tlv_typeArray = new byte[2];
//			byte[] tlv_lenArray = new byte[2];
			
			byte[] tlv_typeArray = new byte[4];
			byte[] tlv_lenArray = new byte[4];
			for(i=0;i<2;i++){
				tlv_typeArray[i] = tempByteArray[i];
				tlv_lenArray[i] = tempByteArray[i+2];
			}
			int tlv_typeInt = LByteConvert.bytesToInt(tlv_typeArray);
			int tlv_lenInt = LByteConvert.bytesToInt(tlv_lenArray);
			
//			int tlv_typeInt = LByteConvert.newBytesToInt(tlv_typeArray);
//			int tlv_lenInt = LByteConvert.newBytesToInt(tlv_lenArray);
			
			TLV_HEADER tlv_Header = new TLV_HEADER();
			tlv_Header.setTlv_type(tlv_typeInt);
			tlv_Header.setTlv_len(tlv_lenInt);
			return tlv_Header;
		}else if( clazz == TLV_V_VersionInfoRequest.class ){
			if(tempByteArray.length != OWSP_LEN.TLV_V_VersionInfoRequest)
				return null;
			TLV_V_VersionInfoRequest tlv_V_VersionInfoRequest = new TLV_V_VersionInfoRequest();
			tlv_V_VersionInfoRequest.setVersionMajor(LByteConvert.bytesToUshort(tempByteArray,0));
			tlv_V_VersionInfoRequest.setVersionMinor(LByteConvert.bytesToUshort(tempByteArray,2));
			return tlv_V_VersionInfoRequest;
		} else if( clazz == TLV_V_ChannelResponse.class ){
			if(tempByteArray.length != OWSP_LEN.TLV_V_ChannelResponse) {
				return null;
			}

			TLV_V_ChannelResponse tlv_V_ChannelResponse = new TLV_V_ChannelResponse();
			tlv_V_ChannelResponse.setResult(LByteConvert.bytesToUshort(tempByteArray, 0));
			tlv_V_ChannelResponse.setCurrentChannel((short)LByteConvert.bytesToUbyte(tempByteArray, 2));
			tlv_V_ChannelResponse.setReserve((short)LByteConvert.bytesToUbyte(tempByteArray, 3));
			
			return tlv_V_ChannelResponse;
		} else if( clazz == TLV_V_StreamDataFormat.class ){
			if(tempByteArray.length != OWSP_LEN.TLV_V_StreamDataFormat) {
				return null;
			}
			
			TLV_V_StreamDataFormat tlv_V_StreamDataFormat = new TLV_V_StreamDataFormat();
			
			tlv_V_StreamDataFormat.setVideoChannel((short)LByteConvert.bytesToUbyte(tempByteArray, 0));		// 视频通道号
			tlv_V_StreamDataFormat.setAudioChannel((short)LByteConvert.bytesToUbyte(tempByteArray, 1));		// 音频通道号
			tlv_V_StreamDataFormat.setDataType((short)LByteConvert.bytesToUbyte(tempByteArray, 2));			// 流数据类型, 取值见StreamDataType
			tlv_V_StreamDataFormat.setReserve((short)LByteConvert.bytesToUbyte(tempByteArray, 3));			// 保留
			
			// 视频格式
			TLV_V_VideoDataFormat videoFormat = new TLV_V_VideoDataFormat();									
			videoFormat.setCodecId(LByteConvert.bytesToUint(tempByteArray, 4));								// FOUR CC code，’H264’
			videoFormat.setBitrate(LByteConvert.bytesToUint(tempByteArray, 4 + 4));							// bps
			videoFormat.setWidth(LByteConvert.bytesToUshort(tempByteArray, 4 + 8));							// image widht
			videoFormat.setHeight(LByteConvert.bytesToUshort(tempByteArray, 4 + 10));						// image height
			videoFormat.setFramerate((short)LByteConvert.bytesToUbyte(tempByteArray, 4 + 12));				// fps
			videoFormat.setColorDepth((short)LByteConvert.bytesToUbyte(tempByteArray, 4 + 13));				// should be 24 bits
			videoFormat.setReserve(LByteConvert.bytesToUshort(tempByteArray, 4 + 14));						// reserve
			
			// 音频格式
			TLV_V_AudioDataFormat audioFormat = new TLV_V_AudioDataFormat();  								
			audioFormat.setSamplesPerSecond(LByteConvert.bytesToUint(tempByteArray, 20));					// samples per second
//			audioFormat.setSamplesPerSecond(LByteConvert.newBytesToUint(tempByteArray, 20));
			audioFormat.setBitrate(LByteConvert.bytesToUint(tempByteArray, 20 + 4));						// bps
			audioFormat.setWaveFormat(LByteConvert.bytesToUshort(tempByteArray, 20 + 8));					// wave format, such as WAVE_FORMAT_PCM,WAVE_FORMAT_MPEGLAYER3
			audioFormat.setChannelNumber(LByteConvert.bytesToUshort(tempByteArray, 20 + 10));				// audio channel number
			audioFormat.setBlockAlign(LByteConvert.bytesToUshort(tempByteArray, 20 + 12));					// block alignment defined by channelSize * (bitsSample/8)
			audioFormat.setBitsPerSample(LByteConvert.bytesToUshort(tempByteArray, 20 + 14));				// bits per sample
			audioFormat.setFrameInterval(LByteConvert.bytesToUshort(tempByteArray, 20 + 16));				// interval between frames, in milliseconds
			audioFormat.setReserve(LByteConvert.bytesToUshort(tempByteArray, 20 + 18));						// reserve
			
			tlv_V_StreamDataFormat.setVideoFormat(videoFormat);
			tlv_V_StreamDataFormat.setAudioFormat(audioFormat);
			
			return tlv_V_StreamDataFormat;
		} else if( clazz == TLV_V_VideoFrameInfo.class ){
			if(tempByteArray.length != OWSP_LEN.TLV_V_VideoFrameInfo)
				return null;
			TLV_V_VideoFrameInfo tlv_V_VideoFrameInfo = new TLV_V_VideoFrameInfo();
			tlv_V_VideoFrameInfo.setChannelId((short)LByteConvert.bytesToUbyte(tempByteArray, 0));
			tlv_V_VideoFrameInfo.setReserve((short)LByteConvert.bytesToUbyte(tempByteArray, 1));
			tlv_V_VideoFrameInfo.setChecksum(LByteConvert.bytesToUshort(tempByteArray, 2));
			tlv_V_VideoFrameInfo.setFrameIndex(LByteConvert.bytesToUint(tempByteArray, 4));
			tlv_V_VideoFrameInfo.setTime(LByteConvert.bytesToUint(tempByteArray, 8));
			return tlv_V_VideoFrameInfo;
		} else if ( clazz == TLV_V_VideoData.class) {
			
			return tempByteArray;
		} else if ( clazz == TLV_V_LoginResponse.class) {
			if(tempByteArray.length != OWSP_LEN.TLV_V_LoginResponse) {
				return null;
			}
			
			TLV_V_LoginResponse tlv_V_LoginResponse = new TLV_V_LoginResponse();
			tlv_V_LoginResponse.setResult(LByteConvert.bytesToUshort(tempByteArray, 0));
			tlv_V_LoginResponse.setReserve(LByteConvert.bytesToUshort(tempByteArray, 2));
			return tlv_V_LoginResponse;
		} else if ( clazz == TLV_V_AudioInfo.class) {
			if(tempByteArray.length != OWSP_LEN.TLV_V_AudioInfo)
				return null;
			
			TLV_V_AudioInfo tlv_V_AudioInfo = new TLV_V_AudioInfo();
			tlv_V_AudioInfo.setChannelId((int)LByteConvert.bytesToUbyte(tempByteArray, 0));
			tlv_V_AudioInfo.setReserve((int)LByteConvert.bytesToUbyte(tempByteArray, 1));
			tlv_V_AudioInfo.setChecksum(LByteConvert.bytesToUshort(tempByteArray, 2));
			tlv_V_AudioInfo.setTime((int)LByteConvert.bytesToUint(tempByteArray, 4));	
			return tlv_V_AudioInfo;
		}else if ( clazz == TLV_V_VideoFrameInfoEx.class) {
			if(tempByteArray.length != OWSP_LEN.TLV_V_VideoFrameInfoEX)
				return null;
			TLV_V_VideoFrameInfoEx info = new TLV_V_VideoFrameInfoEx();
			info.setChannelId((short)LByteConvert.bytesToUbyte(tempByteArray, 0));
			info.setReserve((short)LByteConvert.bytesToUbyte(tempByteArray, 1));
			info.setChecksum(LByteConvert.bytesToUshort(tempByteArray, 2));
			info.setFrameIndex((int)LByteConvert.bytesToUint(tempByteArray, 4));
			info.setTime((int)LByteConvert.bytesToUint(tempByteArray, 8));
			info.setDataSize((int)LByteConvert.bytesToUint(tempByteArray, 12));
			return info;
		} else if(clazz == TLV_V_AudioData.class){
			return tempByteArray;
		} else if ( clazz == TLV_V_PlayRecordResponse.class) {
			if(tempByteArray.length != OWSP_LEN.TLV_V_PlayRecordResponse)
				return null;
			
			TLV_V_PlayRecordResponse prr = new TLV_V_PlayRecordResponse();
			prr.setResult(LByteConvert.bytesToUbyte(tempByteArray, 0));
			prr.setReserve(LByteConvert.bytesToUbyte(tempByteArray, 1));
			
			return prr;
		}
		
		return null;
	}
}
