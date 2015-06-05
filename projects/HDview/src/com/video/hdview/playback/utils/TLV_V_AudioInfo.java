package com.video.hdview.playback.utils;

public class TLV_V_AudioInfo {

	// u_int8 channelId;
	// u_int8 reserve;
	// u_int16 checksum;
	// u_int32 time;

	private int channelId;
	private int reserve;
	private int checksum;
	private int time;

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public int getReserve() {
		return reserve;
	}

	public void setReserve(int reserve) {
		this.reserve = reserve;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
