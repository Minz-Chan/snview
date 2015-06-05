package com.video.hdview.playback.utils;

public class TLV_V_ChannelResponse {
	private int result; // result of request. _RESPONSECODE_SUCC - succeeded,
						// others - failed
	private short currentChannel; // 如果不支持的通道，则返回当前通道号
	private short reserve;

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public short getCurrentChannel() {
		return currentChannel;
	}

	public void setCurrentChannel(short currentChannel) {
		this.currentChannel = currentChannel;
	}

	public short getReserve() {
		return reserve;
	}

	public void setReserve(short reserve) {
		this.reserve = reserve;
	}
}
