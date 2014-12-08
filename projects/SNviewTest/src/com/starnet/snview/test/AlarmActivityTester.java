package com.starnet.snview.test;

import com.starnet.snview.alarmmanager.AlarmDevice;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.test.AndroidTestCase;

public class AlarmActivityTester extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		testAddAlarmInfo();
	}

	@Override
	protected void tearDown() throws Exception {
		
	}

	public void testAddAlarmInfo() throws Exception {
		String imageUrl = "http://photocdn.sohu.com/20111123/Img326603573.jpg";
		for (int j = 0; j < 4; j++) {
			if (j == 1) {
				imageUrl = "http://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Hukou_Waterfall.jpg/800px-Hukou_Waterfall.jpg";
			}
			AlarmDevice alarmDevice = new AlarmDevice();
			alarmDevice.setAlarmContent("AlarmContent");
			alarmDevice.setAlarmTime("2014-12-03");
			alarmDevice.setAlarmType("ÑÌÎí±¨¾¯");
			alarmDevice.setChannel(1);
			alarmDevice.setDeviceName("deviceName" + j);
			alarmDevice.setImageUrl(imageUrl);
			alarmDevice.setIp("114.123.212.45");
			alarmDevice.setPort(8080);
			alarmDevice.setUserName("user" + j);
			ReadWriteXmlUtils.writeAlarm(alarmDevice);
		}
	}
	
}
