package com.starnet.snview.alarmmanager;

import java.io.File;

import com.starnet.snview.util.SDCardUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

@SuppressLint("SdCardPath")
public class AlarmImageFileCache {

	public static Context context;

	/** 从缓存中获取图片 **/
	public static Bitmap getImage(String url) {
		String appName = getApplicationName();
		String[] urls = url.split("/");
		String path = SDCardUtils.getSDCardPath() + appName
				+ urls[urls.length - 1];
		// String path = imagePath + "/"+ urls[urls.length - 1];
		File file = new File(path);
		if (file.exists()) {
			Bitmap bmp = BitmapFactory.decodeFile(path);
			return bmp;
		} else {
			return null;
		}
	}

	/*** 判断图像文件是否在SDCard上 **/
	public static boolean isExistImageFile(String imgUrl) {
		String appName = getApplicationName();
		boolean isExist = false;
		String[] urls = imgUrl.split("/");
		String path = SDCardUtils.getSDCardPath() + appName
				+ urls[urls.length - 1];
		// String path = imagePath + "/"+ urls[urls.length-1];
		File file = new File(path);
		if (file.exists()) {
			isExist = true;
		}
		return isExist;
	}

	private static String getApplicationName() {
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getApplicationContext().getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			applicationInfo = null;
		}
		String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
		return applicationName+"/alarmImg/";
	}

}