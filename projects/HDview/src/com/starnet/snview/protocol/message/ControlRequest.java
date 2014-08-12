package com.starnet.snview.protocol.message;

public class ControlRequest {
	private int deviceId;			// device id generating by the remote device
	private int channel;			// channel id 
	private int cmdCode;			// 控制命令字，参见_PTZCode
	private int size;				// 控制参数数据长度,如果size==0 表示无控制参数
	
	
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
	public int getCmdCode() {
		return cmdCode;
	}
	public void setCmdCode(int cmdCode) {
		this.cmdCode = cmdCode;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
}
