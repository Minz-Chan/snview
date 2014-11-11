package com.baidu.pushutils;

public class AlarmDeviceInfo {

	private String deviceName; // 设备名称
	private String alarm_time; // 报警时间
	private String alarm_type; // 报警类型
	private String device_ip; // 报警IP
	private String device_port; // 报警端口号

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getAlarm_time() {
		return alarm_time;
	}

	public void setAlarm_time(String alarm_time) {
		this.alarm_time = alarm_time;
	}

	public String getAlarm_type() {
		return alarm_type;
	}

	public void setAlarm_type(String alarm_type) {
		this.alarm_type = alarm_type;
	}

	public String getDevice_ip() {
		return device_ip;
	}

	public void setDevice_ip(String device_ip) {
		this.device_ip = device_ip;
	}

	public String getDevice_port() {
		return device_port;
	}

	public void setDevice_port(String device_port) {
		this.device_port = device_port;
	}

}