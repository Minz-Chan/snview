/*
 * FileName:H264DecodeUtil.java
 * 
 * Package:com.starsecurity.util
 * 
 * Date:2013-04-13
 * 
 * Copyright: Copyright (c) 2013 Minz.Chan
 */
package com.starnet.snview.component.h264;

import android.util.Log;



/**
 * @function     功能	  h264码流解码工具类
 *     本类对H264Decoder解码类进行封装，使得更易于解码时进行调用。
 * @author       创建人                陈明珍
 * @date        创建日期           2013-04-13
 * @author       修改人                陈明珍
 * @date        修改日期           2013-04-15
 * @description 修改说明	   对解码函数进行二次封装
 */
public class H264DecodeUtil {
	private static final String TAG = "H264DecodeUtil";
	H264Decoder decoder = new H264Decoder();   
	MP4Recorder mp4recorder = new MP4Recorder();
	byte [] NalBuf = new byte[65535]; // 64k
	int NalBufUsed = 0;
	boolean bFirst = true;
	boolean bFindPPS = true;
	int mTrans = 0x0F0F0F0F;
	
	private int mInstanceId;
	private boolean isCodecOpened = false;
	
	private boolean mStartRecord = false;
	private boolean mInRecording = false;  // 是否正在录像
	private int mSpsCount = -1;
	private int mPlayFPS = 6;
	private String mMp4RecordFileName;
	private long mMP4FileHanlde = 0;
	
	private int width;
	private int height;
	
	private OnResolutionChangeListener mOnResolutionChangeListener;
	
	public H264DecodeUtil(String connName) {
		mInstanceId = connName.hashCode();
	}
	
	public int init(int width, int height) {
		isCodecOpened = true;
		this.width = width;
		this.height = height;
		return decoder.init(mInstanceId, width, height);  
	}
	
	public int uninit() {
		isCodecOpened = false;
		return decoder.uninit(mInstanceId);
	}

	/**
	 * decode array buffer of h264 video stream data, the minimum decode unit should be a NAL unit
	 * @param packet an array of byte that contains a complete NAL unit(should be starting with 0x00000001)
	 * @param len the length of packet
	 * @param byteBitmap an array buffer used to store the decoded data
	 * @return return 1 if successful; otherwise, it returns 0
	 */
	public int decodePacket(byte[] packet, int len, byte[] byteBitmap)   
    {   
		NalBuf = makesureBufferEnough(NalBuf, NalBufUsed + len);
		
		int result = 0;
    	int iTemp = 0;
    	int nalLen;
    	int bytesRead = len;    	
    	int SockBufUsed = 0;
    	
    	byte[] param = new byte[7];
    	
    	if(bytesRead < 0) {
    		return result;
    	}
        
    	
    	byte [] SockBuf = packet;
    	
		while (bytesRead-SockBufUsed>0) {
			nalLen = MergeBuffer(NalBuf, NalBufUsed, SockBuf, SockBufUsed, bytesRead-SockBufUsed);
					
			NalBufUsed += nalLen;
			SockBufUsed += nalLen;
			
			/*
			if (SockBufUsed == bytesRead) { // reach the end of packet, the last NAL unit
				mTrans = 1;
			}*/
			
			/* decode process while NalBuf contains a complete NAL unit */
			while (mTrans == 1) {	
				mTrans = 0xFFFFFFFF;	

				if (bFirst == true && NalBufUsed == 4) {	// the first start flag, pass
					bFirst = false;
				} else {				// a complete NAL data, include 0x00000001 trail
					if (bFindPPS == true && bFirst == false) { // picture parameter set
						if ((NalBuf[4] & 0x1F) == 7) { // if sps
							bFindPPS = false;
							
							if (H264Decoder.probeSps(NalBuf, NalBufUsed - 4, param) == 1) { // 根据得到的参数判断分辨率信息是否发生变化
								Log.d(TAG, "->H264DecodeUtil->probe_sps");
//								VideoView v = ViewManager.getInstance().getVideoView();
								int realWidth = ((param[2] + 1) * 16);
								int realHeight = ((param[3] + 1) * 16);
								Log.d(TAG, "real width:" + realWidth + ", height:" + realHeight);
								Log.d(TAG, "curr width" + width +", height:" + height);
								if (width != realWidth || height != realHeight) {
									init(realWidth, realHeight);
									if (mOnResolutionChangeListener != null) {
										mOnResolutionChangeListener.onResolutionChanged(width, height, realWidth, realWidth);
									}
//									VideoView.changeScreenRevolution(realWidth, realHeight);
//									v.init();
								}
							} else {
								Log.d(TAG, "->H264DecodeUtil->probe_sps , can not return 1");
							}
							
//							System.arraycopy(NalBuf, 4, AVConfig.Video.sps, 0, (NalBufUsed - 4) - 4);
//							AVConfig.Video.spsLen = (NalBufUsed - 4) - 4;
							
						} else {				// if NAL unit sequence is not 'sps, pps, ...', reread from buffer
			   				NalBuf[0] = 0;
		    				NalBuf[1] = 0;
		    				NalBuf[2] = 0;
		    				NalBuf[3] = 1;
		    				
		    				NalBufUsed = 4;
		    				
							break;
						}
					} else if (bFirst == true) {
						bFirst = false;
					}
					
					long t1 = System.currentTimeMillis();
					// decode nal unit when there exists a complete NAL unit in NalBuf
					// the second parameter is the length of NAL unit
					iTemp = decoder.decode(mInstanceId, NalBuf, NalBufUsed - 4, byteBitmap);   
					
					Log.i("H264DecodeUtil", "$$$XFramedecode consume: " + (System.currentTimeMillis()-t1) + ", data size:" + (NalBufUsed - 4));
					
					// pack h264 stream data into mp4 file
//					if (mStartRecord && mSpsCount >= 0) {
//						if (NalBuf[4] == 0x67 && mSpsCount == 0) { // first sps
//							mSpsCount++;
//							mInRecording = true;
//							//mp4recorder.updateSPS(mInstanceId, NalBuf, NalBufUsed - 4, mPlayFPS);
//							mMP4FileHanlde = mp4recorder.createRecordFile(mMp4RecordFileName, NalBuf, NalBufUsed - 4, mPlayFPS);
//							mp4recorder.packVideo(mMP4FileHanlde, NalBuf, NalBufUsed - 4);
//						} else if (mSpsCount > 0) {
//							if (NalBuf[4] == 0x67) mSpsCount++;
//							mp4recorder.packVideo(mMP4FileHanlde, NalBuf, NalBufUsed - 4);
//						}
//					}
					
		            if(iTemp > 0) {
		            	result = 1;
		            	/*
		            	if (SockBufUsed == bytesRead) {
		            		NalBufUsed = 4;
		            	}*/
		            } else if (iTemp == -2) {
		            	return -2;
		            } else if (iTemp < -66) {
		            	return iTemp;
		            }
		            
				}

				NalBuf[0] = 0;
				NalBuf[1] = 0;
				NalBuf[2] = 0;
				NalBuf[3] = 1;
				
				NalBufUsed = 4;
			}		
		} 

        return result;
    }  
	

	/**
     * Merge SockBuf into NalBuf, return the offset when trail(start code, 0x00000001) is found
     * @param NalBuf buffer array for decoding
     * @param NalBufUsed length of NalBuf data read
     * @param SockBuf buffer array read from file
     * @param SockBufUsed length of SockBuf data read
     * @param SockRemain length of data unread(remain)
     * @return position of trail(0x00000001)
     */
    int MergeBuffer(byte[] NalBuf, int NalBufUsed, byte[] SockBuf, int SockBufUsed, int SockRemain) {
    	int  i = 0;
    	byte Temp;

    	for (i = 0; i < SockRemain; i++) {
    		Temp = SockBuf[i+SockBufUsed];
    		NalBuf[i+NalBufUsed] = Temp;

    		mTrans <<= 8;
    		mTrans  |= Temp;

    		if (mTrans == 1) { // 0x00000001(start code) is found
    			i++;
    			break;
    		}	
    	}

    	return i;
    }
    
	public boolean isbFindPPS() {
		return bFindPPS;
	}

	public void setbFindPPS(boolean bFindPPS) {
		this.bFindPPS = bFindPPS;
	}

	public boolean isbFirst() {
		return bFirst;
	}

	public void setbFirst(boolean bFirst) {
		this.bFirst = bFirst;
	}
    
	private byte[] makesureBufferEnough(byte[] buffer, int realSize) {
		byte[] result = buffer;
		int size = buffer.length;
		
		if (size < realSize) {
			byte[] new_buffer = new byte[(int) (realSize * 1.2)];
			
			System.arraycopy(buffer, 0, new_buffer, 0, size);
			buffer = null;
			result = new_buffer;
		}
		
		return result;
	}
	
	
	
	public int getPlayFPS() {
		return mPlayFPS;
	}

	public void setPlayFPS(int fps) {
		this.mPlayFPS = fps;
	}

	public String getMp4RecordFileName() {
		return mMp4RecordFileName;
	}

	public void setMp4RecordFileName(String mp4FileName) {
		this.mMp4RecordFileName = mp4FileName;
	}
	
	public boolean isInRecording() {
		return mInRecording;
	}

	public void startMP4Record(String filename) {
		setMp4RecordFileName(filename);
		mStartRecord = true;
		mSpsCount = 0;
	}
	
	public void stopMP4Record() {
		mStartRecord = false;
		mInRecording = false;
		mSpsCount = -1;
		mp4recorder.closeRecordFile(mMP4FileHanlde);
		mMP4FileHanlde = 0;
	}

	@Override
	protected void finalize() throws Throwable {
		if (decoder != null && isCodecOpened) {
			decoder.uninit(mInstanceId);
		}
		
		super.finalize();
	}
	
	public OnResolutionChangeListener getOnResolutionChangeListener() {
		return mOnResolutionChangeListener;
	}

	public void setOnResolutionChangeListener(
			OnResolutionChangeListener onResolutionChangeListener) {
		this.mOnResolutionChangeListener = onResolutionChangeListener;
	}



	public static interface OnResolutionChangeListener {
		public void onResolutionChanged(int oldWidth, int oldHeight, int newWidth, int newHeight);
	} 
	
	
}
