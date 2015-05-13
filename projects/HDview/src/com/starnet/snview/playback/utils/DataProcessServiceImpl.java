package com.starnet.snview.playback.utils;


import java.util.ArrayList;

import org.apache.mina.core.buffer.IoBuffer;

import com.starnet.snview.component.audio.AudioHandler;
import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.h264.H264DecodeUtil.OnResolutionChangeListener;
import com.starnet.snview.component.h264.H264Decoder;
import com.starnet.snview.component.h264.MP4RecorderUtil;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.component.video.VideoHandler;
import com.starnet.snview.playback.PlaybackActivity;
import com.starnet.snview.playback.PlaybackControlAction;
import com.starnet.snview.playback.RecordHandler;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DataProcessServiceImpl implements DataProcessService {

	private String TAG = "DataProcessServiceImpl";
	private Context context;
	
	private H264DecodeUtil h264;
	private boolean isIFrameFinished = false;
	private IoBuffer oneFrameBuffer;
	private int oneFrameDataSize;
	
	private Handler handler;

	private int oldSecond;
	
	private AudioHandler aHandler;
	private VideoHandler vHandler;
	private RecordHandler rHandler;
	
	public DataProcessServiceImpl(Context context, AudioHandler audioHandler, VideoHandler videoHandler, RecordHandler recordHandler) {
		super();
		this.context = context;
		this.aHandler = audioHandler;
		this.vHandler = videoHandler;
		this.rHandler = recordHandler;
		h264 = vHandler.getH264Decoder();
		h264.init(352, 288);
		h264.setOnResolutionChangeListener(new OnResolutionChangeListener() {
			@Override
			public void onResolutionChanged(int oldWidth, int oldHeight, int newWidth,
					int newHeight) {
				Log.d(TAG, "onResolutionChanged, [" + oldWidth + ", "
						+ oldHeight + ", " + newWidth + ", " + newHeight + "]");
				getPlaybackContainer().getVideoConfig().setWidth(newWidth);
				getPlaybackContainer().getVideoConfig().setHeight(newHeight);
				if (vHandler != null) {
					vHandler.onResolutionChanged(newWidth, newHeight);
				}				
			}
		});
		
		oneFrameBuffer = IoBuffer.allocate(65536);
		handler = ((PlaybackActivity) context).getHandler();
	}
	
//	private boolean isPlaying() {
//		return getPlaybackActivity().isPlaying();
//	}
	
//	private boolean isOnPlayControl() {
//		return getPlaybackActivity().isOnPlayControl();
//	}
//	
//	private boolean isInRandomPlay() {
//		return getPlaybackActivity().isInRandomPlay();
//	}
	
	private PlaybackControlAction getAction() {
		return getPlaybackActivity().getAction();
	}

//	private PlaybackLiveView getPlaybackLiveView() {
//		return getPlaybackContainer().getSurfaceView();
//	}

	private PlaybackLiveViewItemContainer getPlaybackContainer() {
		return getPlaybackActivity().getVideoContainer();
	}

//	private PlaybackTestActivity getPlaybackTestActivity() {
//		return (PlaybackTestActivity) context;
//	}

	private PlaybackActivity getPlaybackActivity() {
		return (PlaybackActivity) context;
	}

	/**
	 * TLV解析
	 * @param data TLV字节数组
	 * @param length TLV数据长度
	 * @return 
	 */
	@Override
	public int process(byte[] data, int length) {
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
				
				TLV_V_VideoFrameInfo tlv_V_VideoFrameInfo = (TLV_V_VideoFrameInfo) ByteArray2Object
						.convert2Object(TLV_V_VideoFrameInfo.class, data, flag,
								OWSP_LEN.TLV_V_VideoFrameInfo);
				
				long time = tlv_V_VideoFrameInfo.getTime();
				int day = (int) (time / (1000*60*60*24));
				int hour = (int) ((time / (1000*60*60)) % 24);
				int minute = (int) ((time / (1000*60)) % 60);
				int second = (int) ((time / (1000) % 60));
				int millisecond = (int) (time % 1000);
				
				currVideoFrameTimestamp = time;
				
				checkVideoRecorderStatus(time);
				
				// 更新时间轴
				if (oldSecond != second) { 
					Message msg = Message.obtain();
					msg.what = PlaybackActivity.UPDATE_MIDDLE_TIME;
					Bundle b = new Bundle();
					b.putLong("VIDEO_TIME", time);
					msg.setData(b);
					handler.sendMessage(msg);
				}
				oldSecond = second;
				
				Log.i(TAG, "video time: "  + time);
				Log.i(TAG, "video time: " + day + " " + hour + ":" + minute + ":" + second + "." + millisecond);
				oneFrameDataSize = -1;
				oneFrameBuffer.clear();
				if (oneFrameBuffer.remaining() < 0xFFFF) {
					oneFrameBuffer.expand(0xFFFF);
				}				
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_PFRAME_DATA) {
				// 若第1帧接到的不是I帧，则后续的P帧不处理
				if (!isIFrameFinished) {
					return 0;
				}

				Log.i(TAG, "######TLV TYPE: TLV_T_VIDEO_PFRAME_DATA");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_VideoData.class, data, flag,
						tlv_Header.getTlv_len());
				
				Log.i(TAG, "$$$Frame data P:" + tmp.length);
				processFrameData(tmp, false);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_IFRAME_DATA) {
				Log.i(TAG, "######TLV TYPE: TLV_T_VIDEO_IFRAME_DATA");
				byte[] tmp = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_VideoData.class, data, flag,
						tlv_Header.getTlv_len());
				
				Log.d(TAG, "(I)Video data size:" + tmp.length);
				
				if (lastType == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO_EX
						|| lastType == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO) {
					Log.d(TAG, "I Frame found...");
					h264.setbFirst(true);
					h264.setbFindPPS(true);
					
					byte[] _sps = H264Decoder.extractSps(tmp, tmp.length);
					byte[] sps = getPlaybackContainer().getVideoConfig().getSps();
					if (_sps != null && sps != null) {
						System.arraycopy(_sps, 4, sps, 0, _sps.length-4);
						getPlaybackContainer().getVideoConfig().setSpsLen(_sps.length-4);
					} else {
						Log.d(TAG, "_sps:" + _sps + ", sps:" + sps);
					}
					
					if (getPlaybackContainer().isInRecording()) {
						getPlaybackContainer().setCanStartRecord(true);
					}
				}

				processFrameData(tmp, true);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_AUDIO_INFO) {
				Log.i(TAG, "######TLV TYPE: TLV_T_AUDIO_INFO");
				TLV_V_AudioInfo audioInfo = (TLV_V_AudioInfo) ByteArray2Object
						.convert2Object(TLV_V_AudioInfo.class, data, flag,
								OWSP_LEN.TLV_V_AudioInfo);
				
				long time = audioInfo.getTime();
				int day = (int) (time / (1000*60*60*24));
				int hour = (int) ((time / (1000*60*60)) % 24);
				int minute = (int) ((time / (1000*60)) % 60);
				int second = (int) ((time / (1000) % 60));
				int millisecond = (int) (time % 1000);
				
				currAudioFrameTimestamp = time;
				
				Log.i(TAG, "audio time: "  + time);
				Log.i(TAG, "audio time: " + day + " " + hour + ":" + minute + ":" + second + "." + millisecond);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_AUDIO_DATA) {
				Log.i(TAG, "######TLV TYPE: TLV_T_AUDIO_DATA");
				byte[] alawData = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_AudioData.class, data, flag,
						tlv_Header.getTlv_len());
				
				if (getPlaybackContainer().isInRecording() && getPlaybackContainer().canStartRecord()) {
					requestAudioRecord(alawData.clone(), alawData.length,
							MP4RecorderUtil.calcAudioDuration(
									currAudioFrameTimestamp,
									preAudioFrameTimestamp, alawData.length));
					preAudioFrameTimestamp = currAudioFrameTimestamp;
				} else {
					currAudioFrameTimestamp = 0;
					preAudioFrameTimestamp = 0;
				}
				
				aHandler.getBufferQueue().write(alawData);
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
				
				Log.d(TAG, "sampleRate: " + sampleRate);
				Log.d(TAG, "framerate: " + framerate);

				if (tlv_V_StreamDataFormat != null) {
					if (width > 0 && height > 0) {
//						h264.init(width, height); // 初始化视频分辨率
//						playbackVideo.init(width, height);
						
						getPlaybackContainer().getVideoConfig().setFramerate(framerate);
						getPlaybackContainer().getVideoConfig().setWidth(width);
						getPlaybackContainer().getVideoConfig().setHeight(height);
						
						Message msg = Message.obtain();
						msg.what = VideoHandler.MSG_VIDEOPLAYER_INIT;
						msg.arg1 = width;
						msg.arg2 = height;
						vHandler.sendMessage(msg);
					}
					if (sampleRate > 0) {
						Message msg = Message.obtain();
						msg.what = AudioHandler.MSG_AUDIOPLAYER_INIT;
						msg.arg1 = sampleRate;
						aHandler.sendMessage(msg);
					}
					
					Message msg = Message.obtain();
					msg.what = PlaybackActivity.RECV_STREAM_DATA_FORMAT;
					handler.sendMessage(msg);
					
					lastVideoTimestamp = 0;
					
					preAudioFrameTimestamp = 0;
					currAudioFrameTimestamp = 0;
					preVideoFrameTimestamp = 0;
					currVideoFrameTimestamp = 0;
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_LOGIN_ANSWER) {//TLV_T_LOGIN_ANSWER
				Log.i(TAG, "######TLV TYPE: TLV_T_LOGIN_ANSWER");
				TLV_V_LoginResponse tlv_V_LoginResponse;
				tlv_V_LoginResponse = (TLV_V_LoginResponse) ByteArray2Object
						.convert2Object(TLV_V_LoginResponse.class, data, flag,
								OWSP_LEN.TLV_V_LoginResponse);
				int result = tlv_V_LoginResponse.getResult();
				if (result == 1) {
					returnValue = PlaybackControllTask.LOGIN_SUC;
//					PlaybackControllTaskUtils.isCanPlay = true;
				} else {
					returnValue = PlaybackControllTask.LOGIN_FAIL;
//					PlaybackControllTaskUtils.isCanPlay = false;
				}
				return returnValue;
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_VIDEO_FRAME_INFO_EX) {
				Log.i(TAG, "######TLV TYPE: TLV_T_VIDEO_FRAME_INFO_EX");
				TLV_V_VideoFrameInfoEx tlv_V_VideoFrameInfoEx = (TLV_V_VideoFrameInfoEx) ByteArray2Object
						.convert2Object(TLV_V_VideoFrameInfoEx.class, data,
								flag, OWSP_LEN.TLV_V_VideoFrameInfoEX);
				
				
				long time = tlv_V_VideoFrameInfoEx.getTime();
				int day = (int) (time / (1000*60*60*24));
				int hour = (int) ((time / (1000*60*60)) % 24);
				int minute = (int) ((time / (1000*60)) % 60);
				int second = (int) ((time / (1000) % 60));
				int millisecond = (int) (time % 1000);
				
				currVideoFrameTimestamp = time;
				
				checkVideoRecorderStatus(time);
				
				// 更新时间轴
				if (oldSecond != second) { 
					Message msg = Message.obtain();
					msg.what = PlaybackActivity.UPDATE_MIDDLE_TIME;
					Bundle b = new Bundle();
					b.putLong("VIDEO_TIME", time);
					msg.setData(b);
					handler.sendMessage(msg);
				}
				oldSecond = second;
				
				oneFrameDataSize = (int) tlv_V_VideoFrameInfoEx.getDataSize();
				oneFrameBuffer.clear();
				if (oneFrameBuffer.remaining() < oneFrameDataSize) {
					oneFrameBuffer.expand(oneFrameDataSize);
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_PLAY_RECORD_RSP) {
				Log.i(TAG, "######TLV TYPE: TLV_T_PLAY_RECORD_RSP");
				TLV_V_PlayRecordResponse prr = (TLV_V_PlayRecordResponse) ByteArray2Object.convert2Object(TLV_V_PlayRecordResponse.class, data,flag, OWSP_LEN.TLV_V_PlayRecordResponse);
				int result = prr.getResult();
				PlaybackControlAction action = getAction();
				if (result == 1) {//表示请求成功
					if (action == PlaybackControlAction.PLAY) {
						returnValue = PlaybackActivity.ACTION_PLAY_SUCC;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_PLAY_SUCC);
					} else if (action == PlaybackControlAction.PAUSE) {
						returnValue = PlaybackActivity.ACTION_PAUSE_SUCC;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_PAUSE_SUCC);
						break;
					} else if (action == PlaybackControlAction.RESUME) {
						returnValue = PlaybackActivity.ACTION_RESUME_SUCC;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_RESUME_SUCC);
					} else if (action == PlaybackControlAction.RANDOM_PLAY) {
						returnValue = PlaybackActivity.ACTION_RANDOM_SUCC;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_RANDOM_SUCC);
					} else if (action == PlaybackControlAction.STOP) {
						returnValue = PlaybackActivity.ACTION_STOP_SUCC;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_STOP_SUCC);
						
						preAudioFrameTimestamp = 0;
						currAudioFrameTimestamp = 0;
						preVideoFrameTimestamp = 0;
						currVideoFrameTimestamp = 0;
					}
				}else {//表示暂停失败
					if (action == PlaybackControlAction.PLAY) {
						returnValue = PlaybackActivity.ACTION_PLAY_FAIL;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_PLAY_FAIL);
						break;
					} else if (action == PlaybackControlAction.PAUSE) {
						returnValue = PlaybackActivity.ACTION_PAUSE_FAIL;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_PAUSE_FAIL);
					} else if (action == PlaybackControlAction.RESUME) {
						returnValue = PlaybackActivity.ACTION_RESUME_FAIL;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_RESUME_FAIL);
						break;
					} else if (action == PlaybackControlAction.RANDOM_PLAY) {
						returnValue = PlaybackActivity.ACTION_RANDOM_FAIL;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_RANDOM_FAIL);
					} else if (action == PlaybackControlAction.STOP) {
						returnValue = PlaybackActivity.ACTION_STOP_FAIL;
						sendMsgToPlayActivity(PlaybackActivity.ACTION_STOP_FAIL);
					}
				}
				
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_RECORD_EOF) {
				Log.i(TAG, "######TLV TYPE: TLV_T_RECORD_EOF");
				returnValue = PlaybackControllTask.RECORD_EOF;
				break;
			}  else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_V_SEARCHRECORD) {
				TLV_V_SearchRecordResponse srr = (TLV_V_SearchRecordResponse) ByteArray2Object.convert2Object(TLV_V_SearchRecordResponse.class, data,flag, OWSP_LEN.TLV_V_SearchFileResponse);
			    int result = srr.getResult();
			    int count = srr.getCount();
			    
			    if (result == 1 && count ==0 ) {
			    	return PlaybackControllTask.SEARCH_RECORD_FILE_NULL;
			    }
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_V_RECORDINFO) {
				TLV_V_RecordInfo info = (TLV_V_RecordInfo) ByteArray2Object.convert2Object(TLV_V_RecordInfo.class, data, flag,OWSP_LEN.TLV_V_RECORDINFO);
				recordInfoList.add(info);
				Log.i(TAG, "record info: " + info.getStartTime() + "~" + info.getEndTime());
				returnValue = PlaybackControllTask.RECORDINFORS;
			}
			lastType = tlv_Header.getTlv_type();
			nLeft -= tlv_Header.getTlv_len();
			flag += tlv_Header.getTlv_len();
		}
		return returnValue;
	}
	
	private ArrayList<TLV_V_RecordInfo> recordInfoList = new ArrayList<TLV_V_RecordInfo>();

	@Override
	public ArrayList<TLV_V_RecordInfo> getRecordInfos() {
		return recordInfoList;
	}
	
	private void sendMsgToPlayActivity(int flag){
		Message msg = new Message();
		msg.what = flag;
		handler.sendMessage(msg);
	}
	
	private long lastVideoTimestamp = 0;
	private void checkVideoRecorderStatus(long videoTimestamp) {
		long delta = (videoTimestamp - lastVideoTimestamp) / 1000;
		if (lastVideoTimestamp != 0 && delta >= 60) { // 若视频时间戳间隔超过1分钟，说明录像源已变更
			getPlaybackActivity().stopMP4RecordIfInRecording();
		}
		lastVideoTimestamp = videoTimestamp;		
	}
	
	private void processFrameData(byte[] tmp, boolean isIFrame) {
		oneFrameBuffer.put(tmp);
		if (oneFrameDataSize == -1 // The data size of I Frame is less than 65536
				|| oneFrameBuffer.position() >= oneFrameDataSize // The all I Frame data has been collected
		) {
			int frameLength = oneFrameBuffer.position();
			byte[] toBeWritten = oneFrameBuffer.flip().array();
			
			Log.d(TAG, "Video data size , frameLength:" + frameLength);
			
			if (getPlaybackContainer().isInRecording() && getPlaybackContainer().canStartRecord()) {
				int framerate = 0;
				if (getPlaybackContainer().getVideoConfig() != null) {
					framerate = getPlaybackContainer().getVideoConfig().getFramerate();
				} else {
					framerate = 25;
				}
				requestVideoRecord(toBeWritten.clone(), frameLength,
						MP4RecorderUtil.calcVideoDuration(
								currVideoFrameTimestamp,
								preVideoFrameTimestamp, framerate));
				preVideoFrameTimestamp = currVideoFrameTimestamp;
			} else {
				currVideoFrameTimestamp = 0;
				preVideoFrameTimestamp = 0;
			}
			
			int count = 0;					
			while (vHandler.isAlive() && 
					vHandler.getBufferQueue().write(toBeWritten.clone()) == 0) {
				Log.i(TAG, "video write queue full222...");
				try {
					if (count == 5) {
						count = 0;
						Message msg = Message.obtain();
						msg.what = VideoHandler.MSG_BUFFER_PROCESS;
						vHandler.sendMessage(msg);
					}
					
					Thread.sleep(10);
					count++;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				} 
			}
			
			if (isIFrame) {
				isIFrameFinished = true;
			}
		}
	}
	
	private long preAudioFrameTimestamp = 0;
	private long currAudioFrameTimestamp = 0;
//	private int calcAudioDuration(long currFrameTimestamp, long preFrameTimestamp, int audioFrameSize) {
//		int duration = 0;
//		
//		if (preFrameTimestamp == 0) {  // 首帧 
//			duration = 8000 / (8000/audioFrameSize);  // 音频录制1秒相当于8000tick, 8000 / 帧率
//		} else {
//			duration = (int) ((currFrameTimestamp-preFrameTimestamp)*8000/1000);
//		}
//		return duration;
//	}
	private void requestAudioRecord(byte[] alawData, int len, int duration) {
		Bundle b = new Bundle();
		b.putByteArray("AUDIO_DATA", alawData);
		b.putInt("FRAME_LENGTH", len);
		b.putInt("DURATION", duration);
		Message msg = Message.obtain();
		msg.what = RecordHandler.MSG_AUDIO_RECORD;
		msg.setData(b);
		rHandler.sendMessage(msg);
	}
	
	private long preVideoFrameTimestamp = 0;
	private long currVideoFrameTimestamp = 0;
	private void requestVideoRecord(byte[] videoData, int len, int duration) {
		Bundle b = new Bundle();
		b.putByteArray("VIDEO_DATA", videoData);
		b.putInt("FRAME_LENGTH", len);
		b.putInt("DURATION", duration);
		Message msg = Message.obtain();
		msg.what = RecordHandler.MSG_VIDEO_RECORD;
		msg.setData(b);
		rHandler.sendMessage(msg);
	}	
}
