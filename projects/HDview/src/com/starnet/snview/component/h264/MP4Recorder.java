package com.starnet.snview.component.h264;

public class MP4Recorder {
	static {
		System.loadLibrary("H264SpsParser");
        System.loadLibrary("MP4Recorder");
    }

//	private static native int UpdateSPS(int instance_id, byte[] sps, int spsLen, int framerate);
    private static native long MP4CreateRecordFile(String filename, byte[] sps, int spsLen, int framerate); 
    private static native int MP4PackVideo(long fileHandle, byte[] nal, int nalLen);
    private static native int MP4CloseRecordFile(long fileHandle);
    
    /**
     * 更新SPS信息
     * @param instance_id 实例ID
     * @param sps SPS字节数组（以0x0000000167）开头
     * @param spsLen SPS数组长度
     * @param framerate 当包含的SPS信息无法计算framerate时，framerate默认取值
     * @return 1，成功；0，失败
     */
//    public int updateSPS(int instance_id, byte[] sps, int spsLen, int framerate) {
//    	return UpdateSPS(instance_id, sps, spsLen, framerate);
//    }
    
    /**
     * 创建MP4文件
     * @param instance_id 实例ID
     * @param filename MP4文件名
     * @return 非0，文件句柄；0，失败
     */
    public long createRecordFile(String filename, byte[] sps, int spsLen, int framerate) {
    	return MP4CreateRecordFile(filename, sps, spsLen, framerate);
    }
    
    /**
     * 打包视频帧
     * @param fileHandle 文件句柄
     * @param nal 一帧视频数据（可为0x67,0x68,0x06,0x65,...等类型）
     * @param nalLen 数据帧长度
     * @return 1，成功；0，失败
     */
    public int packVideo(long fileHandle, byte[] nal, int nalLen) {
    	return MP4PackVideo(fileHandle, nal, nalLen);
    }
    
    /**
     * 关闭MP4文件
     * @param fileHandle 文件句柄
     * @return 1，成功；0，失败
     */
    public int closeRecordFile(long fileHandle) {
    	return MP4CloseRecordFile(fileHandle);
    }
}
