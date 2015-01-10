package com.starnet.snview.protocol.message;

public class SearchRecordRequest {
	
	private int deviceId;//设备编号
	private int channel;//通道号
	private int recordType;//记录类型
	private int count;//记录总数
	private OWSPDateTime startTime;
	private OWSPDateTime endTime;
	private int []reserve = new int[3];
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int getRecordType() {
		return recordType;
	}
	public void setRecordType(int recordType) {
		this.recordType = recordType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public OWSPDateTime getStartTime() {
		return startTime;
	}
	public void setStartTime(OWSPDateTime startTime) {
		this.startTime = startTime;
	}
	public OWSPDateTime getEndTime() {
		return endTime;
	}
	public void setEndTime(OWSPDateTime endTime) {
		this.endTime = endTime;
	}
	public int[] getReserve() {
		return reserve;
	}
	public void setReserve(int[] reserve) {
		this.reserve = reserve;
	}
	
	
}
