package com.video.hdview.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @description:网络帮助类，主要提供，用于网络检测的方法
 *
 */
public class NetWorkUtils {
	//
	/**
	 * 检测网络是否连接，若网络连接，则返回true；否则，返回FALSE；
	 * 
	 * @param context
	 *            :上下文环境
	 * @return ：若网络连接，则返回true；否则，返回FALSE；
	 */
	public static boolean checkNetConnection(Context context) {
		boolean isConnected = false;
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);

		NetworkInfo wifiInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean wifiConnected = wifiInfo.isConnectedOrConnecting();

		NetworkInfo mobileInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean mobileConnected = mobileInfo.isConnectedOrConnecting();

		if (mobileConnected || wifiConnected) {
			isConnected = true;
		}

		return isConnected;
	}
}