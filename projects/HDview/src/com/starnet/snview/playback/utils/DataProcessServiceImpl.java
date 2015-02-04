/*
 * FileName:DataProcessServiceImpl.java
 * 
 * Package:com.starsecurity.service.impl
 * 
 * Date:2013-04-15
 * 
 * Copyright: Copyright (c) 2013 Minz.Chan
 */
package com.starnet.snview.playback.utils;

import java.io.FileWriter;
import java.io.IOException;

import com.starnet.snview.component.h264.H264DecodeUtil;

import android.os.Message;
import android.util.Log;

public class DataProcessServiceImpl implements DataProcessService {

	private String TAG = "DataProcessServiceImpl";
	private String conn_name;
	// private H264DecodeUtil h264 = new H264DecodeUtil();

	private boolean isIFrameFinished = false;

	public DataProcessServiceImpl(String conn_name) {
		super();
		this.conn_name = conn_name;
		// h264.init(352, 288);
	}

	@Override
	public int process(byte[] data, int length) {
		// VideoView v = ViewManager.getInstance().getVideoView();
		int nLeft = length - 4; // 未处理的字节数
		int nLen_hdr = OWSP_LEN.TLV_HEADER;
		int flag = 0;

		// 循环处理所有的TLVl
		while (nLeft > nLen_hdr) {
			// 处理TLV头memcpy(&tlv_hdr,buf,nLen_hdr);
			TLV_HEADER tlv_Header = (TLV_HEADER) ByteArray2Object
					.convert2Object(TLV_HEADER.class, data, flag,
							OWSP_LEN.TLV_HEADER);
			nLeft -= nLen_hdr;
			flag += nLen_hdr;
			// 处理TLV的V部分
			if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO) {
				TLV_V_VideoFrameInfo tlv_V_VideoFrameInfo;
				tlv_V_VideoFrameInfo = (TLV_V_VideoFrameInfo) ByteArray2Object
						.convert2Object(TLV_V_VideoFrameInfo.class, data, flag,
								OWSP_LEN.TLV_V_VideoFrameInfo);
				Log.i(TAG, "time:" + tlv_V_VideoFrameInfo.getTime());
				// ConnectionManager.getConnection(conn_name).addResultItem(tlv_V_VideoFrameInfo);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_PFRAME_DATA) {
				// 若第1帧接到的不是I帧，则后续的P帧不处理
				if (!isIFrameFinished) {
					return 0;
				}

				Log.i(TAG, "*********** P Frame process start  *******");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_VideoData.class, data, flag,
						tlv_Header.getTlv_len());
				Log.i(TAG, "vediodata length:" + tmp.length);
				int result = 0;
				try {
					for (int i = 0; i < tmp.length; i++) {
						String cc = "" + tmp[i] + ",";
						testSaveFile(videoFileName, cc);
					}
					// result = h264.decodePacket(tmp, tmp.length,
					// v.getmPixel());
				} catch (Exception e) {
					e.printStackTrace();
					// 解码过程发生异常
					// ViewManager.getInstance().setHelpMsg(R.string.IDS_Unknown);
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_AUDIO_INFO) {

				TLV_V_AudioInfo audioInfo = (TLV_V_AudioInfo) ByteArray2Object
						.convert2Object(TLV_V_AudioInfo.class, data, flag,
								OWSP_LEN.TLV_V_AudioInfo);
				Log.i(TAG, "time:" + audioInfo.getTime());

			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_AUDIO_DATA) {
				Log.i(TAG, "==========audiodata============:");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_AudioData.class, data, flag,
						tlv_Header.getTlv_len());
				Log.i(TAG, "length:" + tmp.length);

				try {
					for (int i = 0; i < tmp.length; i++) {
						String c = "" + tmp[i] + ",";
						testSaveFile(audioFileName, c);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_IFRAME_DATA) {
				Log.i(TAG, "==========vediodata============:");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_VideoData.class, data, flag,
						tlv_Header.getTlv_len());
				Log.i(TAG, "length:" + tmp.length);
				int result = 0;
				try {
					for (int i = 0; i < tmp.length; i++) {
						String c = "" + tmp[i] + ",";
						testSaveFile(videoFileName, c);
					}
					// result = h264.decodePacket(tmp, tmp.length,
					// v.getmPixel());
				} catch (Exception e) {
					// 解码过程发生异常
					// ViewManager.getInstance().setHelpMsg(R.string.IDS_Unknown);
				}

				isIFrameFinished = true;
				if (result == 1) {

				}
				Log.i(TAG, "==========audiodata end============:");
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_STREAM_FORMAT_INFO) {
				TLV_V_StreamDataFormat tlv_V_StreamDataFormat;
				tlv_V_StreamDataFormat = (TLV_V_StreamDataFormat) ByteArray2Object
						.convert2Object(TLV_V_StreamDataFormat.class, data,
								flag, OWSP_LEN.TLV_V_StreamDataFormat);

				int framerate = tlv_V_StreamDataFormat.getVideoFormat()
						.getFramerate();
				int width = tlv_V_StreamDataFormat.getVideoFormat().getWidth();
				int height = tlv_V_StreamDataFormat.getVideoFormat()
						.getHeight();
				int bitrate = (int) (tlv_V_StreamDataFormat.getVideoFormat()
						.getBitrate() / 1024);

			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_LOGIN_ANSWER) {
				TLV_V_LoginResponse tlv_V_LoginResponse;
				tlv_V_LoginResponse = (TLV_V_LoginResponse) ByteArray2Object
						.convert2Object(TLV_V_LoginResponse.class, data, flag,
								OWSP_LEN.TLV_V_LoginResponse);

				int result = tlv_V_LoginResponse.getResult();
				Log.i(TAG, "result:" + result);

			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO_EX) {
				TLV_V_VideoFrameInfoEx infoEx;
				infoEx = (TLV_V_VideoFrameInfoEx) ByteArray2Object
						.convert2Object(TLV_V_VideoFrameInfoEx.class, data,
								flag, OWSP_LEN.TLV_V_VideoFrameInfoEX);
				Log.i(TAG, "data:" + infoEx.getTime());
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_PLAY_RECORD_RSP) {
				TLV_V_PlayRecordResponse prr = (TLV_V_PlayRecordResponse) ByteArray2Object
						.convert2Object(TLV_V_PlayRecordResponse.class, data,
								flag, OWSP_LEN.TLV_V_PlayRecordResponse);
				Log.i(TAG, "data:" + prr);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_RECORD_EOF) {

				Log.i(TAG, "EOF:" + tlv_Header.getTlv_type());
				break;

			}
			nLeft -= tlv_Header.getTlv_len();
			flag += tlv_Header.getTlv_len();
		}
		return 0;
	}

	/**
	 * 更新UI消息传递
	 * 
	 * @param msg
	 *            消息对象
	 */
	private void updateUIMessage(Message msg) {
		// Handler handler = ViewManager.getInstance().getHandler();
		// if (handler != null) {
		// handler.sendMessage(msg);
		// } else {
		// ViewManager.getInstance().setHelpMsg(R.string.IDS_Unknown);
		// }
	}

	private final String audioFileName = "/data/data/com.starnet.snview/audio.txt";
	private final String videoFileName = "/data/data/com.starnet.snview/video.txt";

	private static void testSaveFile(String fileName, String content) {
		FileWriter writer = null;
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			writer = new FileWriter(fileName, true);
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}