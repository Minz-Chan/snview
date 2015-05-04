package com.starnet.snview.component.h264;

import java.util.HashMap;

import android.util.Log;

public class MP4Recorder {
	private static final String TAG = "MP4Recorder";
	private static HashMap<Long, Boolean> status = new HashMap<Long, Boolean>();
	private static HashMap<String, Long> locks = new HashMap<String, Long>();
	
	static {
		System.loadLibrary("H264SpsParser");
        System.loadLibrary("MP4Recorder");
    }

	private static native long MP4CreateRecordFile(String filename, int width,
			int height, int framerate, int AVCProfileIndication,
			int profile_compat, int AVCLevelIndication);
    private static native int MP4PackVideo(long fileHandle, byte[] nal, int nalLen);
    private static native int MP4PackAudio(long fileHandle, byte[] data, int len);
    private static native int MP4CloseRecordFile(long fileHandle);
    
    
    /**
     * 创建MP4文件
     * @param instance_id 实例ID
     * @param filename MP4文件名
     * @return 非0，文件句柄；0，失败
     */
    public static long createRecordFile(String filename, int width,
			int height, int framerate, int AVCProfileIndication,
			int profile_compat, int AVCLevelIndication) {
    	Log.d(TAG, "createRecordFile, width:" +  width + ", height:" + height);
    	long fileHandle = MP4CreateRecordFile(filename, width, height, framerate,
    			AVCProfileIndication, profile_compat, AVCLevelIndication);
    	status.put(Long.valueOf(fileHandle), Boolean.valueOf(true));
    	locks.put(String.valueOf(fileHandle), Long.valueOf(fileHandle));
    	return fileHandle;
    }
    
    /**
     * 打包视频帧
     * @param fileHandle 文件句柄
     * @param data 一帧视频数据（可为0x67,0x68,0x06,0x65,...等类型）
     * @param size 数据帧长度
     * @return 1，成功；0，失败
     */
    public static int packVideo(long fileHandle, byte[] data, int size) {
    	int result = 0;
    	synchronized (locks.get(String.valueOf(fileHandle))) {
    		result = MP4PackVideo(fileHandle, data, size);
    	}
    	return result;
    }
    
    public static int packAudio(long fileHandle, byte[] data, int size) {
    	int result = 0;
    	synchronized (locks.get(String.valueOf(fileHandle))) {
    		result = MP4PackAudio(fileHandle, data, size);
    	}
    	return result;
    }
    
    /**
     * 关闭MP4文件
     * @param fileHandle 文件句柄
     * @return 1，成功；0，失败
     */
    public static int closeRecordFile(long fileHandle) {
    	status.remove(Long.valueOf(fileHandle));
    	locks.remove(String.valueOf(fileHandle));
    	return MP4CloseRecordFile(fileHandle);
    }
    
    public static boolean isInRecording(long fileHandle) {
    	Boolean v = status.get(Long.valueOf(fileHandle));
    	return v != null && v.booleanValue();
    }
}
