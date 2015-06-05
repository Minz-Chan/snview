package com.video.hdview.alarmmanager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnAlarmMessageArrivedReceiver extends BroadcastReceiver {
	public static final String ACTION = "com.video.hdview.action.ALARM_MESSAGE_ARRIVED";
	@SuppressWarnings("unused")
	private static final String TAG = "OnAlarmMessageArrivedReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(AlarmReceiver.NOTIFICATION_ID);
		
	    Intent i = new Intent(context, AlarmActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.putExtra(AlarmActivity.START_FROM_NOTIFICATION, true);
		context.getApplicationContext().startActivity(i);  // 确保Activity被带至前台
	}

}
