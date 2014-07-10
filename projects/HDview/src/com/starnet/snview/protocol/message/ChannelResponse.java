package com.starnet.snview.protocol.message;

public class ChannelResponse {
	private int result;
	private int currentChannel;
	private int reserve;
	
	
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public int getCurrentChannel() {
		return currentChannel;
	}
	public void setCurrentChannel(int currentChannel) {
		this.currentChannel = currentChannel;
	}
	public int getReserve() {
		return reserve;
	}
	public void setReserve(int reserve) {
		this.reserve = reserve;
	}
}
