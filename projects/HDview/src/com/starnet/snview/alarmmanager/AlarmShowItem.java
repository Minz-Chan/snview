package com.starnet.snview.alarmmanager;

public class AlarmShowItem {
	private AlarmDevice alarm;
	private boolean isExpanded;

	public AlarmDevice getAlarm() {
		return alarm;
	}

	public void setAlarm(AlarmDevice alarm) {
		this.alarm = alarm;
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

}
