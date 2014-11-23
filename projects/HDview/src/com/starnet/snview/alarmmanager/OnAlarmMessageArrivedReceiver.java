package com.starnet.snview.alarmmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnAlarmMessageArrivedReceiver extends BroadcastReceiver {
	public static final String ACTION = "com.starnet.snview.action.ALARM_MESSAGE_ARRIVED";
	private static final String TAG = "OnAlarmMessageArrivedReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    Intent i = new Intent(context, AlarmActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.putExtra(AlarmActivity.START_FROM_NOTIFICATION, true);
		context.getApplicationContext().startActivity(i);  // 确保Activity被带至前台
	}

}
