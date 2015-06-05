package com.video.hdview.protocol.message;

public class StreamDataFormat {
	private int videoChannel;
	private int audioChannel;
	private int dataType;
	private int reserve;
	OwspVideoDataFormat videoDataFormat;
	OwspAudioDataFormat audioDataFormat;
	
	
	public int getVideoChannel() {
		return videoChannel;
	}
	public void setVideoChannel(int videoChannel) {
		this.videoChannel = videoChannel;
	}
	public int getAudioChannel() {
		return audioChannel;
	}
	public void setAudioChannel(int audioChannel) {
		this.audioChannel = audioChannel;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public int getReserve() {
		return reserve;
	}
	public void setReserve(int reserve) {
		this.reserve = reserve;
	}
	public OwspVideoDataFormat getVideoDataFormat() {
		return videoDataFormat;
	}
	public void setVideoDataFormat(OwspVideoDataFormat videoDataFormat) {
		this.videoDataFormat = videoDataFormat;
	}
	public OwspAudioDataFormat getAudioDataFormat() {
		return audioDataFormat;
	}
	public void setAudioDataFormat(OwspAudioDataFormat audioDataFormat) {
		this.audioDataFormat = audioDataFormat;
	}
}