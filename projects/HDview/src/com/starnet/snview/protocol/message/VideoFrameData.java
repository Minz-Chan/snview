package com.starnet.snview.protocol.message;

public class VideoFrameData {
	private byte[] data;

	public VideoFrameData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
