package com.starnet.snview.util;

import java.io.File;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StatFs;

/**
 * @author : 桥下一粒砂 chenyoca@gmail.com
 * date    : 2012-9-13
 * SD卡工具类
 */
public class SDCardUtils {
	
	//????检测内存是否充足...???

	/** SDCard是否可用 **/
	public static boolean IS_MOUNTED = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	
	/** SDCard的根路径 **/
	private static String SDCARD_PATH;
	
	/**
	 * 取得SD卡路径，以/结尾
	 * @return SD卡路径
	 */
	public static String getSDCardPath(){
		if(!IS_MOUNTED) return null;
		if(null != SDCARD_PATH) return SDCARD_PATH;
		File path = Environment.getExternalStorageDirectory(); 
		String SDCardPath = path.getAbsolutePath();
		SDCardPath += SDCardPath.endsWith(File.separator) ? "" : File.separator;
		SDCARD_PATH = SDCardPath;
		return SDCardPath;
	}
	
	/**
	* 获取手机内部可用空间大小（MB）
	* @return
	*/ 
	@SuppressWarnings("deprecation")
	public static long getAvailableInternalMemorySize() { 
	    File path = Environment.getDataDirectory(); // 获取data根目录
	    StatFs stat = new StatFs(path.getPath()); 
	    
		long blockSize = stat.getBlockSize(); 
	    long availableBlocks = stat.getAvailableBlocks();
	    return (availableBlocks * blockSize)/1024/1024; 
	} 
	
	//返回的是SDCard区间大小（MB）
	@SuppressWarnings("deprecation")
	public static long getAvailableExternalMemorySize() {

		File path = Environment.getExternalStorageDirectory();// 获取SDCard根目录
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();//获取块区间大小
		long availableBlocks = stat.getAvailableBlocks();//获取可用块数
		return (availableBlocks * blockSize)/1024/1024;
		
	}	
}