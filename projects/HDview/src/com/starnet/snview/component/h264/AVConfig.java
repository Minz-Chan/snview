package com.starnet.snview.component.h264;

public class AVConfig {
	public static class Video {
		private int width;
		private int height;
		private int framerate;
		private byte[] sps = new byte[1024];  // 不包含起始码
		private int spsLen;
		
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
		public byte[] getSps() {
			return sps;
		}
		public void setSps(byte[] sps) {
			this.sps = sps;
		}
		public int getSpsLen() {
			return spsLen;
		}
		public void setSpsLen(int spsLen) {
			this.spsLen = spsLen;
		}		
	}
	
	public static class Audio {
		
	}
}
