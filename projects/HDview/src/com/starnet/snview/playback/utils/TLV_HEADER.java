package com.starnet.snview.playback.utils;

public class TLV_HEADER {
	private int tlv_type;
	private int tlv_len;

	public int getTlv_type() {
		return tlv_type;
	}

	public void setTlv_type(int tlv_type) {
		this.tlv_type = tlv_type;
	}

	public int getTlv_len() {
		return tlv_len;
	}

	public void setTlv_len(int tlv_len) {
		this.tlv_len = tlv_len;
	}
}
