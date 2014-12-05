package com.starnet.snview.test;

import org.json.JSONException;
import org.json.JSONObject;

import junit.framework.Assert;

import com.starnet.snview.alarmmanager.AlarmDevice;
import com.starnet.snview.alarmmanager.AlarmReceiver;
import com.starnet.snview.util.Base64Util;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.Suppress;
import android.text.TextUtils;

public class AlarmReceiverTester extends AndroidTestCase {

	private AlarmReceiver ar;
	
	@Override
	protected void setUp() throws Exception {
		ar = new AlarmReceiver();
	}

	public void testMessageParse() throws Exception{
//		String message = "{'title':'','description':'alarmlog','open_type':2,'custom_content':" 
//				+ "{'alarm_content':'57qi5rKz6Zm255O36YCa6YGTM+WumuaXtuaKk+aLjQ==','image_path':"
//				+ "'d2h5OjFAaHR0cDovL3h5LnN0YXItbmV0c2VjdXJpdHkuY29tOjgwODAvcC9samc4ODgvMjAxNC0w" 
//				+ "NC0yMS8xMzk4MDcwNTc2Nzc3OTAzLmpwZw==','video_url':'d2h5OjFAb3dzcDovLzEyMy41NS"
//				+ "4yNi4xOTg6OTUwNS9jaG40','push_user':'d2h5OjFAeHkuc3Rhci1uZXRzZWN1cml0eS5jb20=" 
//				+ "','device_name':'U042MTA0LTA4RjM=','alarm_time':'MjAxNC0xMi0wMywxNzoyMDo0MQ==" 
//				+ "','alarm_type':'6KeG6aKR5Lii5aSx5oql6K2m'}}";
//		String message = "{'title':'','description':'alarmlog','open_type':2,'custom_content':" 
//				+ "{'alarm_content':'57qi5rKz6Zm255O36YCa6YGTM-WumuaXtuaKk-aLjQ..','image_path':"
//				+ "'d2h5OjFAaHR0cDovL3h5LnN0YXItbmV0c2VjdXJpdHkuY29tOjgwODAvcC9samc4ODgvMjAxNC0w" 
//				+ "NC0yMS8xMzk4MDcwNTc2Nzc3OTAzLmpwZw..','video_url':'d2h5OjFAb3dzcDovLzEyMy41NS"
//				+ "4yNi4xOTg6OTUwNS9jaG40','push_user':'d2h5OjFAeHkuc3Rhci1uZXRzZWN1cml0eS5jb20." 
//				+ "','device_name':'U042MTA0LTA4RjM.','alarm_time':'MjAxNC0xMi0wMywxNzoyMDo0MQ.." 
//				+ "','alarm_type':'6KeG6aKR5Lii5aSx5oql6K2m'}}";
		String message = "{'title':'','description':'alarmlog','custom_content':{'alarm_content':'57qi5rKz6Zm255O36YCa6YGTM-WumuaXtuaKk-aLjQ..','image_path':'d2h5OjFAaHR0cDovL3h5LnN0YXItbmV0c2VjdXJpdHkuY29tOjgwODAvcC9samc4ODgvMjAxNC0wNC0yMS8xMzk4MDcwNTc2Nzc3OTAzLmpwZw..','video_url':'d2h5OjFAb3dzcDovLzEyMy41NS4yNi4xOTg6OTUwNS9jaG40','push_user':'d2h5OjFAeHkuc3Rhci1uZXRzZWN1cml0eS5jb20.','device_name':'U042MTA0LTA4RjM.','alarm_time':'MjAxNC0xMi0wNCwyMDoyNTo0MQ..','alarm_type':'6KeG6aKR5Lii5aSx5oql6K2m'}}";
		ar.onMessage(getContext(), message, null);
	}
	
	@Suppress
	public void testOnMessage() throws Exception{
//		String message = "{'title':'','description':'alarmlog','open_type':2,'custom_content':{'alarm_content':'uuy608zVtMnNqLXAM7aoyrHXpcXE','image_path':'why:1@http://xy.star-netsecurity.com:8080/p/ljg888/2014-04-21/1398070576777903.jpg','video_url':'why:1@owsp://123.55.26.198:9505/chn4','push_user':'why:1@xy.star-netsecurity.com','device_name':'U042MTA0LTA4RjM=','alarm_time':'2014-12-03,17:20:41','alarm_type':'ytPGtbaqyqexqL6v'}}";
		String message = "{'title':'','description':'alarmlog','open_type':2,'custom_content':" 
				+ "{'alarm_content':'57qi5rKz6Zm255O36YCa6YGTM+WumuaXtuaKk+aLjQ==','image_path':"
				+ "'d2h5OjFAaHR0cDovL3h5LnN0YXItbmV0c2VjdXJpdHkuY29tOjgwODAvcC9samc4ODgvMjAxNC0w" 
				+ "NC0yMS8xMzk4MDcwNTc2Nzc3OTAzLmpwZw==','video_url':'d2h5OjFAb3dzcDovLzEyMy41NS"
				+ "4yNi4xOTg6OTUwNS9jaG40','push_user':'d2h5OjFAeHkuc3Rhci1uZXRzZWN1cml0eS5jb20=" 
				+ "','device_name':'U042MTA0LTA4RjM=','alarm_time':'MjAxNC0xMi0wMywxNzoyMDo0MQ==" 
				+ "','alarm_type':'6KeG6aKR5Lii5aSx5oql6K2m'}}";
		if (!TextUtils.isEmpty(message)) {
            try {
            	String deviceName = null;
            	String alarmTime = null;
            	String alarmType = null;
                String alarmContent = null;
                String imageUrl = null;
                String videoUrl = null;
                String pushUserUrl = null;
                JSONObject messageJsonObj = new JSONObject(message);
                JSONObject customContentJsonObj = new JSONObject(messageJsonObj.getString("custom_content"));
                
                Assert.assertEquals(true, messageJsonObj.getString("custom_content").indexOf("alarm_content") > 0);
                
                Assert.assertEquals(true, !customContentJsonObj.isNull("device_name"));
                Assert.assertEquals(true, !customContentJsonObj.isNull("alarm_time"));
                Assert.assertEquals(true, !customContentJsonObj.isNull("alarm_type"));
                Assert.assertEquals(true, !customContentJsonObj.isNull("alarm_content"));
                Assert.assertEquals(true, !customContentJsonObj.isNull("push_user"));
                Assert.assertEquals(true, !customContentJsonObj.isNull("image_path"));
                Assert.assertEquals(true, !customContentJsonObj.isNull("video_url"));
                
                if (!customContentJsonObj.isNull("device_name")) {
                	deviceName = Base64Util.decode(customContentJsonObj
							.getString("device_name")); // 需先BASE64解密
				}
                if (!customContentJsonObj.isNull("alarm_time")) {
                	alarmTime = Base64Util.decode(customContentJsonObj
							.getString("AlarmTime"));
//                	alarmTime = customContentJsonObj.getString("alarm_time");
				}
                if (!customContentJsonObj.isNull("alarm_type")) {
                	alarmType = Base64Util.decode(customContentJsonObj
							.getString("alarm_type"));
				}
				if (!customContentJsonObj.isNull("alarm_content")) {
					alarmContent = Base64Util.decode(customContentJsonObj
							.getString("alarm_content")); 
				}
				if (!customContentJsonObj.isNull("push_user")) {
					pushUserUrl = Base64Util.decode(customContentJsonObj
							.getString("PushUser"));
//					pushUserUrl = customContentJsonObj.getString("push_user");
				}
				if (!customContentJsonObj.isNull("image_path")) {
					imageUrl = Base64Util.decode(customContentJsonObj
							.getString("ImageUrl"));
//					imageUrl = customContentJsonObj.getString("image_path");
				}
				if (!customContentJsonObj.isNull("video_url")) {
					videoUrl = Base64Util.decode(customContentJsonObj
							.getString("VideoUrl"));
//					videoUrl = customContentJsonObj.getString("video_url");
				}

                AlarmDevice ad = new AlarmDevice();
                ad.setDeviceName(deviceName);
                ad.setAlarmTime(alarmTime);
                ad.setAlarmType(alarmType);
                ad.setAlarmContent(alarmContent);
//                ar.parseImageUrl(ad, imageUrl);
//                ar.parseVideoUrl(ad, videoUrl);
//                ar.parsePushUser(ad, pushUserUrl);
                
                Assert.assertEquals("SN6104-08F3", ad.getDeviceName());
                Assert.assertEquals("2014-12-03,17:20:41", ad.getAlarmTime());
                Assert.assertEquals("视频丢失报警", ad.getAlarmType());
                Assert.assertEquals("红河陶瓷通道3定时抓拍", ad.getAlarmContent());
				Assert.assertEquals(
						"http://xy.star-netsecurity.com:8080/p/ljg888/2014-04-21/1398070576777903.jpg",
						ad.getImageUrl());
                Assert.assertEquals("123.55.26.198", ad.getIp());
                Assert.assertEquals(9505, ad.getPort());
                Assert.assertEquals(4, ad.getChannel());
                Assert.assertEquals("why", ad.getUserName());
                Assert.assertEquals("1", ad.getPassword());
                Assert.assertEquals("why", ad.getPusherUserName());
                Assert.assertEquals("1", ad.getPusherPassword());
                Assert.assertEquals("xy.star-netsecurity.com", ad.getPusherDomain());
                
                ReadWriteXmlUtils.writeAlarm(ad); // 持久化报警信息到文件中
                
//                ar.showNotification(getContext(), ad.getAlarmContent(), deviceName,
//						ad.getIp() + ":" + ad.getPort());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            	// error processing
            	e.printStackTrace();
            }
        }
	}
	
	@Override
	protected void tearDown() throws Exception {
		ar = null;
	}	
}
