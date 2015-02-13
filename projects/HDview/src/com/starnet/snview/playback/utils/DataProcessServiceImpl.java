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

import java.util.ArrayList;

import org.apache.mina.core.buffer.IoBuffer;

import com.starnet.snview.component.audio.AudioPlayer;
import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.PlaybackLiveView;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.playback.PlaybackActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.media.AudioFormat;
import android.os.Message;
import android.util.Log;

public class DataProcessServiceImpl implements DataProcessService {

	private String TAG = "DataProcessServiceImpl";
	private Context context;
	private String conn_name;
	private final int LOGIN_SUC = 41;
	private final int LOGIN_FAIL = 42;
	private static final int PAUSE_PLAYRECORDREQ_SUCC = 45;
	private static final int PAUSE_PLAYRECORDREQ_FAIL = 46;
	private static final int RESUME_PLAYRECORDREQ_SUCC = 43;
	private static final int RESUME_PLAYRECORDREQ_FAIL = 44;
	
	private H264DecodeUtil h264;
	private boolean isIFrameFinished = false;
	private IoBuffer oneIFrameBuffer;
	private int oneIFrameDataSize;
	
	private AudioPlayer audioPlayer;
	private static final int UPDATINGTIMEBAR = 0x0010;
	private Handler handler;

	public DataProcessServiceImpl(Context context, String conn_name) {
		super();
		this.context = context;
		this.conn_name = conn_name;
		h264 = getPlaybackContainer().getH264Decoder();
		h264.init(352, 288);

		oneIFrameBuffer = IoBuffer.allocate(65536);
		handler = ((PlaybackActivity) context).getHandler();
		// oneIFrameBuffer.setAutoExpand(true);
	}

	private PlaybackLiveView getPlaybackLiveView() {
		return getPlaybackContainer().getSurfaceView();
	}

	private PlaybackLiveViewItemContainer getPlaybackContainer() {
		return getPlaybackActivity().getVideoContainer();
	}

//	private PlaybackTestActivity getPlaybackTestActivity() {
//		return (PlaybackTestActivity) context;
//	}

	private PlaybackActivity getPlaybackActivity() {
		return (PlaybackActivity) context;
	}

	@Override
	public int process(byte[] data, int length) {
		// VideoView v = ViewManager.getInstance().getVideoView();
		PlaybackLiveView playbackVideo = getPlaybackLiveView();
		int returnValue = 0;
		int nLeft = length - 4; // 未处理的字节数
		int nLen_hdr = OWSP_LEN.TLV_HEADER;
		int flag = 0;
		int lastType = 0;

		// 循环处理所有的TLVl
		while (nLeft > nLen_hdr) {
			// 处理TLV头memcpy(&tlv_hdr,buf,nLen_hdr);
			TLV_HEADER tlv_Header = (TLV_HEADER) ByteArray2Object
					.convert2Object(TLV_HEADER.class, data, flag,
							OWSP_LEN.TLV_HEADER);
			Log.i(TAG, "TLV_HEADER, TYPE:" + tlv_Header.getTlv_type()
					+ ", LEN:" + tlv_Header.getTlv_len());
			nLeft -= nLen_hdr;
			flag += nLen_hdr;
			// 处理TLV的V部分
			if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO) {
				Log.i(TAG, "######TLV TYPE: TLV_T_VIDEO_FRAME_INFO");
				/*
				TLV_V_VideoFrameInfo tlv_V_VideoFrameInfo = (TLV_V_VideoFrameInfo) ByteArray2Object
						.convert2Object(TLV_V_VideoFrameInfo.class, data, flag,
								OWSP_LEN.TLV_V_VideoFrameInfo);*/
				oneIFrameDataSize = -1;
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_PFRAME_DATA) {
				// 若第1帧接到的不是I帧，则后续的P帧不处理
				if (!isIFrameFinished) {
					return 0;
				}

				Log.i(TAG, "######TLV TYPE: TLV_T_VIDEO_PFRAME_DATA");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(TLV_V_VideoData.class, data, flag,tlv_Header.getTlv_len());
				int result = 0;
				try {
					result = h264.decodePacket(tmp, tmp.length,playbackVideo.retrievetDisplayBuffer());
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (result == 1) {
					Log.i(TAG, "*********************** update video: P Frame  *************************");
					playbackVideo.onContentUpdated();					
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_IFRAME_DATA) {
				Log.i(TAG, "######TLV TYPE: TLV_T_VIDEO_IFRAME_DATA");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_VideoData.class, data, flag,
						tlv_Header.getTlv_len());

				if (lastType == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO_EX
						|| lastType == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO) {
					h264.setbFirst(true);
					h264.setbFindPPS(true);
				}

				oneIFrameBuffer.put(tmp);

				if (oneIFrameDataSize == -1 // The data size of I Frame is less than 65536
						|| !oneIFrameBuffer.hasRemaining() // The all I Frame data has been collected
				) {
					int result = 0;
					try {
						result = h264.decodePacket(oneIFrameBuffer.array(),oneIFrameBuffer.position(),playbackVideo.retrievetDisplayBuffer());
					} catch (Exception e) {
						e.printStackTrace();
					}

					isIFrameFinished = true;
					if (result == 1) {
						playbackVideo.onContentUpdated();
						Log.i(TAG,
								"*********************** update video: I Frame  *************************");
					}
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_AUDIO_INFO) {
				Log.i(TAG, "######TLV TYPE: TLV_T_AUDIO_INFO");
				/*
				TLV_V_AudioInfo audioInfo = (TLV_V_AudioInfo) ByteArray2Object
						.convert2Object(TLV_V_AudioInfo.class, data, flag,
								OWSP_LEN.TLV_V_AudioInfo);*/
				
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_AUDIO_DATA) {
				Log.i(TAG, "######TLV TYPE: TLV_T_AUDIO_DATA");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_AudioData.class, data, flag,
						tlv_Header.getTlv_len());
//				PlaybackControllTaskUtils.saveBytesToFile(tmp);//Test 
				
				audioPlayer.playAudioTrack(tmp, 0, tmp.length);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_STREAM_FORMAT_INFO) {
				Log.i(TAG, "######TLV TYPE: TLV_T_STREAM_FORMAT_INFO");
				TLV_V_StreamDataFormat tlv_V_StreamDataFormat = (TLV_V_StreamDataFormat) ByteArray2Object
						.convert2Object(TLV_V_StreamDataFormat.class, data,
								flag, OWSP_LEN.TLV_V_StreamDataFormat);
				int framerate = tlv_V_StreamDataFormat.getVideoFormat()
						.getFramerate();
				int width = tlv_V_StreamDataFormat.getVideoFormat().getWidth();
				int height = tlv_V_StreamDataFormat.getVideoFormat()
						.getHeight();
				int bitrate = (int) (tlv_V_StreamDataFormat.getVideoFormat()
						.getBitrate() / 1024);
				int sampleRate = (int) tlv_V_StreamDataFormat.getAudioFormat()
						.getSamplesPerSecond();

				if (tlv_V_StreamDataFormat != null) {
					if (width > 0 && height > 0) {
						h264.init(width, height); // 初始化视频分辨率
						playbackVideo.init(width, height);
					}
					if (sampleRate > 0) {
						if (audioPlayer != null) {
							audioPlayer.release();
						}

						audioPlayer = new AudioPlayer(sampleRate,
								AudioFormat.CHANNEL_CONFIGURATION_STEREO,
								AudioFormat.ENCODING_PCM_16BIT);
						audioPlayer.init();
					}
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_LOGIN_ANSWER) {//TLV_T_LOGIN_ANSWER
				Log.i(TAG, "######TLV TYPE: TLV_T_LOGIN_ANSWER");
				TLV_V_LoginResponse tlv_V_LoginResponse;
				tlv_V_LoginResponse = (TLV_V_LoginResponse) ByteArray2Object
						.convert2Object(TLV_V_LoginResponse.class, data, flag,
								OWSP_LEN.TLV_V_LoginResponse);
				int result = tlv_V_LoginResponse.getResult();
				if (result == 1) {
					returnValue = LOGIN_SUC;
					PlaybackControllTaskUtils.isCanPlay = true;
				} else {
					returnValue = LOGIN_FAIL;
					PlaybackControllTaskUtils.isCanPlay = false;
				}
				return returnValue;
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO_EX) {
				Log.i(TAG, "######TLV TYPE: TLV_T_VIDEO_FRAME_INFO_EX");
				TLV_V_VideoFrameInfoEx tlv_V_VideoFrameInfoEx = (TLV_V_VideoFrameInfoEx) ByteArray2Object
						.convert2Object(TLV_V_VideoFrameInfoEx.class, data,
								flag, OWSP_LEN.TLV_V_VideoFrameInfoEX);
				oneIFrameDataSize = (int) tlv_V_VideoFrameInfoEx.getDataSize();
				oneIFrameBuffer.clear();
				if (oneIFrameBuffer.remaining() < oneIFrameDataSize) {
					oneIFrameBuffer.expand(oneIFrameDataSize);
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_PLAY_RECORD_RSP) {
				Log.i(TAG, "######TLV TYPE: TLV_T_PLAY_RECORD_RSP");
				TLV_V_PlayRecordResponse prr = (TLV_V_PlayRecordResponse) ByteArray2Object.convert2Object(TLV_V_PlayRecordResponse.class, data,flag, OWSP_LEN.TLV_V_PlayRecordResponse);
				int result = prr.getResult();
				if (result == 1) {//表示请求成功
					if (isPauseFlag) {//通知PlaybackActivity的变化
						returnValue = PAUSE_PLAYRECORDREQ_SUCC;
						sendMsgToPlayActivity(PAUSE_PLAYRECORDREQ_SUCC);
						break;
					}
					if(isResumeFlag){
						returnValue = RESUME_PLAYRECORDREQ_SUCC;
						sendMsgToPlayActivity(RESUME_PLAYRECORDREQ_SUCC);
					}
				}else {//表示暂停失败
					if (isPauseFlag) {
						returnValue = PAUSE_PLAYRECORDREQ_FAIL;
						sendMsgToPlayActivity(PAUSE_PLAYRECORDREQ_FAIL);
						break;
					}
					if (isResumeFlag) {
						returnValue = RESUME_PLAYRECORDREQ_FAIL;
						sendMsgToPlayActivity(RESUME_PLAYRECORDREQ_FAIL);
						break;
					}
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_RECORD_EOF) {
				Log.i(TAG, "######TLV TYPE: TLV_T_RECORD_EOF");
				returnValue = -1;
				break;
			}  else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_V_SEARCHRECORD) {
				TLV_V_SearchRecordResponse srr = (TLV_V_SearchRecordResponse) ByteArray2Object.convert2Object(TLV_V_SearchRecordResponse.class, data,flag, OWSP_LEN.TLV_V_SearchFileResponse);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_V_RECORDINFO) {
				TLV_V_RecordInfo info = (TLV_V_RecordInfo) ByteArray2Object.convert2Object(TLV_V_RecordInfo.class, data, flag,OWSP_LEN.TLV_V_RECORDINFO);
				recordInfoList.add(info);
				returnValue = RECORDINFORS;
			}
			lastType = tlv_Header.getTlv_type();
			nLeft -= tlv_Header.getTlv_len();
			flag += tlv_Header.getTlv_len();
		}
		return returnValue;
	}

	private static final int RECORDINFORS = 32;
	private ArrayList<TLV_V_RecordInfo> recordInfoList = new ArrayList<TLV_V_RecordInfo>();

	@Override
	public ArrayList<TLV_V_RecordInfo> getRecordInfos() {
		return recordInfoList;
	}

	/** 更新播放时的进度时间轴 **/
	private void updatePlaybackUIMessage(Message msg) {
		Bundle data = new Bundle();
		msg.setData(data);
		msg.what = UPDATINGTIMEBAR;
		handler.sendMessage(msg);
	}
	
	private void sendMsgToPlayActivity(int flag){
		Message msg = new Message();
		msg.what = flag;
		handler.sendMessage(msg);
	}
	
	private boolean isPauseFlag;
	private boolean isResumeFlag;
	public void setPause(boolean isPause){
		this.isPauseFlag = isPause;
	}
	public void setResume(boolean isResume){
		this.isResumeFlag = isResume;
	}
	
	
}
