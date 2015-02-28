/*
 * FileName:H264Decoder.java
 * 
 * Package:com.starsecurity.h264
 * 
 * Date:2013-04-12
 * 
 * Copyright: Copyright (c) 2013 Minz.Chan
 */
package com.starnet.snview.component.h264;

/**
 * @function     功能	  	解码h264码流
 *     本类用于对接收到的h264码流进行解码，解码前须调用init函数对解码器进
 * 行初始化操作；结束解码操作后，须使用uninit函数回收解码器资源。对于本解码
 * 库，其最小解码单元为一个NAL单元。
 * @author       创建人              陈明珍
 * @date        创建日期           2013-04-12
 * @author       修改人              陈明珍
 * @date        修改日期           2013-04-12
 * @description 修改说明	            首次增加
 */
public class H264Decoder {
	
	static {
		System.loadLibrary("ffmpeg");
        System.loadLibrary("H264Decoder");
    }

	private static synchronized native int InitDecoder(int instance_id, int width, int height);
    private static synchronized native int UninitDecoder(int instance_id); 
    private static native int DecoderNal(int instance_id, byte[] in, int insize, byte[] out);
    private static native int ProbeSPS(byte[] in, int insize, byte[] para);
    
    /**
     * 解码器初始化
     * @param width	视频宽度
     * @param height 视频高度
     * @return 返回1说明初始化成功，否则表示失败
     */
    public int init(int instance_id, int width, int height) {
    	return InitDecoder(instance_id, width, height);
    }
    
    /**
     * 解码器资源回收
     * @return 返回1说明资源回收成功，否则表示失败
     */
    public int uninit(int instance_id) {
    	return UninitDecoder(instance_id);
    }
    
    /**
     * h264解码，以一个NAL单元为单位
     * @param in 包含一个NAL单元的字节数组(以0x00000001开头)
     * @param insize 字节数组长度
     * @param out 接收解码完后数据的缓冲区
     * @return 解码的字节数
     */
    public int decode(int instance_id, byte[] in, int insize, byte[] out) {
    	return DecoderNal(instance_id, in, insize, out);
    }
    
    /**
     * 探测SPS相关参数
     * @param in 包含一个NAL单元的字节数组(以0x00000001开头)
     * @param insize 字节数组长度
     * @param param 待填充参数数组
     *        param[0] profile_idc
     *        param[1] level_idc
     *        param[2] pic_width_in_mbs_minus_1
     *        param[3] pic_height_in_map_units_minus_1
     *        param[4] seq_parameter_set_id
     *        param[5] num_ref_frames       
     * @return 成功返回1，失败返回0
     */
    public static int probeSps(byte[] in, int insize, byte[] param) {
    	return ProbeSPS(in, insize, param);
    }
    
    /**
     * 提取SPS（包含起始码 00 00 00 01）
     * @param IFrameData 完整的I帧
     * @param size 帧长度
     * @return 包含起始码的SPS数据
     */
    public static byte[] extractSps(byte[] IFrameData, int size) {
		if (size < 5) {
			return null;
		}
		
		byte[] p = IFrameData;
		int pos1, pos2;
		
		// find first sps start position
		pos1 = 0;
		while (!(p[pos1 + 0] == 0x00 && p[pos1 + 1] == 0x00
				&& p[pos1 + 2] == 0x00 && p[pos1 + 3] == 0x01
				&& p[pos1 + 4] == 0x67)) {
			pos1++;
		}
		
		// find next nal unit start position
		pos2 = pos1+1;
		while (!(p[pos2 + 0] == 0x00 && p[pos2 + 1] == 0x00
				&& p[pos2 + 2] == 0x00 && p[pos2 + 3] == 0x01)) {
			pos2++;
		}
		
		if (pos2 < size) {
			byte[] sps = new byte[pos2-pos1];
			System.arraycopy(IFrameData, pos1, sps, 0, pos2-pos1);
			return sps;
		}
		
		return null;
	}
}
