package com.starnet.snview.playback.utils;


import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.mina.core.buffer.IoBuffer;

import com.starnet.snview.component.audio.AudioHandler;
import com.starnet.snview.component.h264.AVConfig;
import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.h264.H264DecodeUtil.OnResolutionChangeListener;
import com.starnet.snview.component.h264.H264Decoder;
import com.starnet.snview.component.h264.MP4Recorder;
import com.starnet.snview.component.liveview.PlaybackLiveView;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.component.video.VideoHandler;
import com.starnet.snview.playback.PlaybackActivity;
import com.starnet.snview.playback.PlaybackControlAction;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DataProcessServiceImpl implements DataProcessService {

	private String TAG = "DataProcessServiceImpl";
	private Context context;
	private String conn_name;
	
	private H264DecodeUtil h264;
	private boolean isIFrameFinished = false;
	private IoBuffer oneIFrameBuffer;
	private int oneIFrameDataSize;
	
	private Handler handler;

	private int oldSecond;
	
	private AudioHandler aHandler;
	private VideoHandler vHandler;
	
	public DataProcessServiceImpl(Context context, AudioHandler audioHandler, VideoHandler videoHandler) {
		super();
		this.context = context;
		this.aHandler = audioHandler;
		this.vHandler = videoHandler;
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
		
		oneIFrameBuffer = IoBuffer.allocate(65536);
		handler = ((PlaybackActivity) context).getHandler();
	}
	
	private boolean isPlaying() {
		return getPlaybackActivity().isPlaying();
	}
	
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

	/**
	 * TLV解析
	 * @param data TLV字节数组
	 * @param length TLV数据长度
	 * @return 
	 */
	@Override
	public int process(byte[] data, int length) {
//		PlaybackLiveView playbackVideo = getPlaybackLiveView();
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
				oneIFrameDataSize = -1;
				oneIFrameBuffer.clear();
				if (oneIFrameBuffer.remaining() < 0xFFFF) {
					oneIFrameBuffer.expand(0xFFFF);
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

//				if (getPlaybackContainer().isInRecording() && getPlaybackContainer().canStartRecord()) {
//					MP4Recorder.packVideo(getPlaybackContainer().getRecordFileHandler(), tmp, tmp.length);
//				} 
				
				int count = 0;
				while (vHandler.isAlive() && 
						vHandler.getBufferQueue().write(tmp) == 0) {
					Log.i(TAG, "video write queue full111...");
					try {
						if (count == 5) {
							count = 0;
							Message msg = Message.obtain();
							msg.what =VideoHandler.MSG_BUFFER_PROCESS;
							vHandler.sendMessage(msg);
						}
						
						Thread.sleep(10);
						count++;
					} catch (Exception e) {
						e.printStackTrace();
						break;
					} 
				}
				
//				int result = 0;
//				try {
//					long t1 = System.currentTimeMillis();
//					
//					result = h264.decodePacket(tmp, tmp.length,
//							playbackVideo.retrievetDisplayBuffer());
//					
//					Log.i(TAG, "$$$PFramedecode consume: " + (System.currentTimeMillis()-t1));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//				if (result == 1) {
//					Log.i(TAG, "*********************** update video: P Frame  *************************");
//					playbackVideo.onContentUpdated();					
//				}
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
					System.arraycopy(_sps, 4, sps, 0, _sps.length-4);
					getPlaybackContainer().getVideoConfig().setSpsLen(_sps.length-4);
					
					if (getPlaybackContainer().isInRecording()) {
						getPlaybackContainer().setCanStartRecord(true);
					}
				}

				oneIFrameBuffer.put(tmp);

				Log.i(TAG, "$$$oneIFrameDataSize:" + oneIFrameDataSize + ", remaining:" + oneIFrameBuffer.remaining() + ", pos:" + oneIFrameBuffer.position());
				if (oneIFrameDataSize == -1 // The data size of I Frame is less than 65536
						|| oneIFrameBuffer.position() >= oneIFrameDataSize // The all I Frame data has been collected
				) {
					Log.i(TAG, "$$$IFrame decode start");
					int dataSize = oneIFrameBuffer.position();
					ByteBuffer buf = ByteBuffer.allocate(dataSize);
					buf.put(oneIFrameBuffer.flip().array(), 0, dataSize);
					byte[] toBeWritten = buf.array();
					
					Log.d(TAG, "Video data size , toBeWritten.length:" + toBeWritten.length);
					
//					if (getPlaybackContainer().isInRecording() && getPlaybackContainer().canStartRecord()) {
//						MP4Recorder.packVideo(getPlaybackContainer().getRecordFileHandler(), toBeWritten, toBeWritten.length);
//					} 
					
					int count = 0;					
					while (vHandler.isAlive() && 
							vHandler.getBufferQueue().write(toBeWritten) == 0) {
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
					
					isIFrameFinished = true;
//					int result = 0;
//					try {
//						long t1 = System.currentTimeMillis();
//						result = h264.decodePacket(oneIFrameBuffer.array(),
//								oneIFrameBuffer.position(),
//								playbackVideo.retrievetDisplayBuffer());
//						Log.i(TAG, "$$$IFramedecode consume: " + (System.currentTimeMillis()-t1));
//
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//					isIFrameFinished = true;
//					if (result == 1) {
//						playbackVideo.onContentUpdated();
//						Log.i(TAG,
//								"*********************** update video: I Frame  *************************");
//					}
				}
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
				
				Log.i(TAG, "audio time: "  + time);
				Log.i(TAG, "audio time: " + day + " " + hour + ":" + minute + ":" + second + "." + millisecond);
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_AUDIO_DATA) {
				Log.i(TAG, "######TLV TYPE: TLV_T_AUDIO_DATA");
				byte[] alawData = (byte[]) ByteArray2Object.convert2Object(
						TLV_V_AudioData.class, data, flag,
						tlv_Header.getTlv_len());
				
				if (getPlaybackContainer().isInRecording() && getPlaybackContainer().canStartRecord()) {
					MP4Recorder.packAudio(getPlaybackContainer().getRecordFileHandler(), alawData, alawData.length);
				} 
				
				aHandler.getBufferQueue().write(alawData);
//				getPlaybackContainer().getAudioBufferQueue().write(alawData);
//				byte[] pcmData = new byte[alawData.length*2];
//
//				audioCodec.g711aDecode(alawData, alawData.length, pcmData, pcmData.length);
//				getAudioPlayer().playAudioTrack(pcmData, 0, pcmData.length);
				
				/*
				if (count < 800) {
					try {
						audioWriter.write(pcmData);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						audioWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Log.i(TAG, "$$$audioWriter close...");
				}
				
				count++;*/

				
//				PlaybackControllTaskUtils.saveBytesToFile(tmp);//Test 
				
//				audioPlayer.playAudioTrack(tmp, 0, tmp.length);
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
				
				oneIFrameDataSize = (int) tlv_V_VideoFrameInfoEx.getDataSize();
				oneIFrameBuffer.clear();
				if (oneIFrameBuffer.remaining() < oneIFrameDataSize) {
					oneIFrameBuffer.expand(oneIFrameDataSize);
				}
			} else if (tlv_Header.getTlv_type() == TLV_T_Command.TLV_T_PLAY_RECORD_RSP) {
				Log.i(TAG, "######TLV TYPE: TLV_T_PLAY_RECORD_RSP");
				TLV_V_PlayRecordResponse prr = (TLV_V_PlayRecordResponse) ByteArray2Object.convert2Object(TLV_V_PlayRecordResponse.class, data,flag, OWSP_LEN.TLV_V_PlayRecordResponse);
				int result = prr.getResult();
				PlaybackControlAction action = getAction();
//				if (isOnPlayControl()) {
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
						}
//						if (isInRandomPlay()) {
//							returnValue = PlaybackActivity.RANDOM_PLAYRECORDREQ_SUCC;
//							sendMsgToPlayActivity(PlaybackActivity.RANDOM_PLAYRECORDREQ_SUCC);
//							break;
//						}
						
//						if (isPlaying()) {//通知PlaybackActivity的变化
//							returnValue = PlaybackActivity.PAUSE_PLAYRECORDREQ_SUCC;
//							sendMsgToPlayActivity(PlaybackActivity.PAUSE_PLAYRECORDREQ_SUCC);
//							break;
//						} else {
//							returnValue = PlaybackActivity.RESUME_PLAYRECORDREQ_SUCC;
//							sendMsgToPlayActivity(PlaybackActivity.RESUME_PLAYRECORDREQ_SUCC);
//						}
					}else {//表示暂停失败
						if (isPlaying()) {
							returnValue = PlaybackActivity.ACTION_PAUSE_FAIL;
							sendMsgToPlayActivity(PlaybackActivity.ACTION_PAUSE_FAIL);
							break;
						} else {
							returnValue = PlaybackActivity.ACTION_RESUME_FAIL;
							sendMsgToPlayActivity(PlaybackActivity.ACTION_RESUME_FAIL);
							break;
						}
					}
//				}
				
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

	private void updateTimebar(int second) {
		// 更新时间轴
		
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
	
//	private boolean isPauseFlag;
//	private boolean isResumeFlag;
//	public void setPause(boolean isPause){
//		this.isPauseFlag = isPause;
//	}
//	public void setResume(boolean isResume){
//		this.isResumeFlag = isResume;
//	}
	
	
}
