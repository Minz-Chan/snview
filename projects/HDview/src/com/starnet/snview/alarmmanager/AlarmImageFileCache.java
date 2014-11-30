package com.starnet.snview.alarmmanager;

import java.io.File;

import com.starnet.snview.util.SDCardUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AlarmImageFileCache {

	/** 从缓存中获取图片 **/
	public static Bitmap getImage(String url) {
		String[] urls = url.split("/");
		String path = SDCardUtils.getSDCardPath() + urls[urls.length - 1];
		File file = new File(path);
		if (file.exists()) {
			Bitmap bmp = BitmapFactory.decodeFile(path);
			return bmp;
		} else {
			return null;
		}
	}

	/***判断图像文件是否在SDCard上**/
	public static boolean isExistImageFile(String imgUrl){
		boolean isExist = false;
		String []urls = imgUrl.split("/");
		String path = SDCardUtils.getSDCardPath()+ urls[urls.length-1];
		File file = new File(path);
		if (file.exists()) {
			isExist = true;
		}
		return isExist;
	}
}