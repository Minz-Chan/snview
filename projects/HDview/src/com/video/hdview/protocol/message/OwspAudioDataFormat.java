package com.video.hdview.protocol.message;

public class OwspAudioDataFormat {
	private int samplesPerSecond;
	private int bitrate;
	private int waveForamt;
	private int channelNumber;
	private int blockAlign;
	private int bitsPerSample;
	private int frameInterval;
	private int reserve;
	
	
	public int getSamplesPerSecond() {
		return samplesPerSecond;
	}
	public void setSamplesPerSecond(int samplesPerSecond) {
		this.samplesPerSecond = samplesPerSecond;
	}
	public int getBitrate() {
		return bitrate;
	}
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}
	public int getWaveForamt() {
		return waveForamt;
	}
	public void setWaveForamt(int waveForamt) {
		this.waveForamt = waveForamt;
	}
	public int getChannelNumber() {
		return channelNumber;
	}
	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}
	public int getBlockAlign() {
		return blockAlign;
	}
	public void setBlockAlign(int blockAlign) {
		this.blockAlign = blockAlign;
	}
	public int getBitsPerSample() {
		return bitsPerSample;
	}
	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}
	public int getFrameInterval() {
		return frameInterval;
	}
	public void setFrameInterval(int frameInterval) {
		this.frameInterval = frameInterval;
	}
	public int getReserve() {
		return reserve;
	}
	public void setReserve(int reserve) {
		this.reserve = reserve;
	}
}
