package com.starnet.snview.util;

import java.io.File;

import com.starnet.snview.global.GlobalApplication;

import android.os.Environment;
import android.os.StatFs;

public class SDCardUtils {

	/** SDCard是否可用 **/
	public static boolean IS_MOUNTED = ExternalStorage.isAvailable();

	/** SDCard的根路径 **/
	private static String SDCARD_PATH;

	/**
	 * 取得SD卡路径，以/结尾。若存在外置SD存储卡，优先使用外置SD存储卡；反之，使用内置SD存储卡
	 * 
	 * @return SD卡路径
	 */
	public static String getSDCardPath() {
		if (!IS_MOUNTED) {
			return null;
		}

		if (SDCARD_PATH != null) {
			return SDCARD_PATH;
		}
		File path;
		boolean isAvailable = isAvailableForExternalSDCard();
		if (isAvailable) {
			path = ExternalStorage.getAllStorageLocations().get(
					ExternalStorage.EXTERNAL_SD_CARD); // external sdcard
		} else {
			path = ExternalStorage.getAllStorageLocations().get(ExternalStorage.SD_CARD); // internal sdcard
		}

		// File path =
		// ExternalStorage.getAllStorageLocations().get(ExternalStorage.EXTERNAL_SD_CARD);
		// // external sdcard
		// if (path == null) {
		// path = ExternalStorage.getAllStorageLocations().get(
		// ExternalStorage.SD_CARD); // internal sdcard
		// }
		String absPath = path.getAbsolutePath();
		absPath += absPath.endsWith(File.separator) ? "" : File.separator;
		SDCARD_PATH = absPath;

		return absPath;
	}

	/** 获取内置SDCard的路径 **/
	public static String getInternalSDCardPath() {
		File path = ExternalStorage.getAllStorageLocations().get(ExternalStorage.SD_CARD);
		String absPath = path.getAbsolutePath();
		absPath += absPath.endsWith(File.separator) ? "" : File.separator;
		return absPath;
	}

	/** 获取外置SDCard的路径 ；如果不可用，则返回为空 **/
	public static String getExternalSDCardPath() {
		File path = ExternalStorage.getAllStorageLocations().get(ExternalStorage.EXTERNAL_SD_CARD);
		String absPath = path.getAbsolutePath();
		absPath += absPath.endsWith(File.separator) ? "" : File.separator;
		return absPath;
	}

	/** 判断外置SDCard是否可用 **/
	public static boolean isAvailableForExternalSDCard() {
		boolean isAvailable = false;
		File path = ExternalStorage.getAllStorageLocations().get(ExternalStorage.EXTERNAL_SD_CARD);
		if (path == null) {
			return false;
		}
		String appName = GlobalApplication.getInstance().getAppName();
		String absPath = path.getAbsolutePath();
		String pat = absPath + File.separator + appName;
		File file = new File(pat);
		if (file.exists()) {
			isAvailable = true;
		}
		return isAvailable;
	}
	
	public static boolean existSDCard() {  
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取手机内部可用空间大小（MB）
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory(); // 获取data根目录
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();

		return (availableBlocks * blockSize) / 1024 / 1024;
	}

	/**
	 * 获取SDCard空间大小（MB）
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getAvailableExternalMemorySize() {
		File path = ExternalStorage.getAllStorageLocations().get(
				ExternalStorage.EXTERNAL_SD_CARD); // external sdcard
		if (path == null) {
			path = ExternalStorage.getAllStorageLocations().get(
					ExternalStorage.SD_CARD); // internal sdcard
		}
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();

		return (availableBlocks * blockSize) / 1024 / 1024;
	}

}