package com.starnet.snview.playback.utils;

public class TLV_V_LoginResponse {
	private short result;				//result of login request. _RESPONSECODE_SUCC - succeeded, others - failed
	private short reserve;
	public short getResult() {
		return result;
	}
	public void setResult(short result) {
		this.result = result;
	}
	public short getReserve() {
		return reserve;
	}
	public void setReserve(short reserve) {
		this.reserve = reserve;
	}
}
