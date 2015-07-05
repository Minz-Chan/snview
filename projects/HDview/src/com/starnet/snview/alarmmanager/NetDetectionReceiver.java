package com.starnet.snview.alarmmanager;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
//需要考虑网络异常的情况
public class NetDetectionReceiver extends BroadcastReceiver {
	
	private final long TIME_SPACE = 5 * 60 * 1000;//5*60*1000分钟
	private long lasttime = System.currentTimeMillis();

	@Override
	public void onReceive(Context context, Intent intent) {
		while(true){
			if(context!=null && PushManager.isPushEnabled(context)){
				break;
			}
			long currentTime = System.currentTimeMillis();
			while (currentTime - lasttime >= TIME_SPACE) {
				if(context!=null && PushManager.isPushEnabled(context)){
					break;
				}
				if(context!=null){
					ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					if(manager==null){
						break;
					}
					NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					NetworkInfo activeInfo = manager.getActiveNetworkInfo();
					boolean isMobile = mobileInfo.isConnected();
					boolean isWifiConnect = wifiInfo.isConnected();
					if(isMobile || isWifiConnect){
						PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY,Utils.getMetaValue(context.getApplicationContext(), "api_key"));
					}else{
						String text = context.getString(R.string.channel_manager_channellistview_netnotopens);
						Toast.makeText(context,text,Toast.LENGTH_LONG).show();
					}
				}
				lasttime = currentTime;
			}
		}
	}
}