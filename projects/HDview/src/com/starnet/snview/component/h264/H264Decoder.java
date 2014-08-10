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
    public int probe_sps(byte[] in, int insize, byte[] param) {
    	return ProbeSPS(in, insize, param);
    }
}
