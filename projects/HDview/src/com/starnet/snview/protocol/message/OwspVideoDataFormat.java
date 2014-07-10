package com.starnet.snview.protocol.message;

public class OwspVideoDataFormat {
	private int codecId;
	private int bitrate;
	private int width;
	private int height;
	private int framerate;
	private int colorDepth;
	private int reserve;
	
	
	public int getCodecId() {
		return codecId;
	}
	public void setCodecId(int codecId) {
		this.codecId = codecId;
	}
	public int getBitrate() {
		return bitrate;
	}
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getFramerate() {
		return framerate;
	}
	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}
	public int getColorDepth() {
		return colorDepth;
	}
	public void setColorDepth(int colorDepth) {
		this.colorDepth = colorDepth;
	}
	public int getReserve() {
		return reserve;
	}
	public void setReserve(int reserve) {
		this.reserve = reserve;
	}
}
