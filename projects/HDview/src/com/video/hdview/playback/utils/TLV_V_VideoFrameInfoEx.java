package com.video.hdview.playback.utils;

public class TLV_V_VideoFrameInfoEx {
	// u_int8 channelId;
	// u_int8 reserve;
	// u_int16 checksum;
	// u_int32 frameIndex;
	// u_int32 time;
	// u_int32 dataSize;
	private short channelId;
	private short reserve;
	private int checksum;
	private long frameIndex;
	private long time;
	private long dataSize;

	public short getChannelId() {
		return channelId;
	}

	public void setChannelId(short channelId) {
		this.channelId = channelId;
	}

	public short getReserve() {
		return reserve;
	}

	public void setReserve(short reserve) {
		this.reserve = reserve;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	public long getFrameIndex() {
		return frameIndex;
	}

	public void setFrameIndex(long frameIndex) {
		this.frameIndex = frameIndex;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

}
