package com.starnet.snview.playback.utils;

import com.starnet.snview.protocol.message.OWSPDateTime;

public class PlayRecordRequest {

	private int deviceId;
	private OWSPDateTime startTime; // 暂停、继续、停止命令无效
	private int channel;
	private int command; // 回放控制命令
	private int reserve;

	public PlayRecordRequest() {

	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public OWSPDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(OWSPDateTime startTime) {
		this.startTime = startTime;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	public int getReserve() {
		return reserve;
	}

	public void setReserve(int reserve) {
		this.reserve = reserve;
	}

}
