package com.starnet.snview.playback.utils;

public class TLV_V_VersionInfoRequest {
	private int versionMajor;		// major version
	private int versionMinor;		// minor version
	public int getVersionMajor() {
		return versionMajor;
	}
	public void setVersionMajor(int versionMajor) {
		this.versionMajor = versionMajor;
	}
	public int getVersionMinor() {
		return versionMinor;
	}
	public void setVersionMinor(int versionMinor) {
		this.versionMinor = versionMinor;
	}
}
