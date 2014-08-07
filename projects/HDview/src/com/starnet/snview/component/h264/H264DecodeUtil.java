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
	H264Decoder decoder = new H264Decoder();       
	byte [] NalBuf = new byte[65535]; // 64k
	int NalBufUsed = 0;
	boolean bFirst = true;
	boolean bFindPPS = true;
	int mTrans = 0x0F0F0F0F;
	
	private int mInstanceId;
	
	public H264DecodeUtil(String connName) {
		mInstanceId = connName.hashCode();
	}
	
	public int init(int width, int height) {
		return decoder.init(mInstanceId, width, height);  
	}
	
	public int uninit() {
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
							
							if (decoder.probe_sps(NalBuf, NalBufUsed - 4, param) == 1) { 
								System.out.println("->H264DecodeUtil->probe_sps");
//								VideoView v = ViewManager.getInstance().getVideoView();
//								int realWidth = ((param[2] + 1) * 16);
//								int realHeight = ((param[3] + 1) * 16);
//								System.out.printf("real widthxheight: %dx%d\n", realWidth, realHeight);
//								System.out.printf("curr widthxheight: %dx%d\n", v.getWidth1(), v.getHeight1());
//								if (v.getWidth1() != realWidth || v.getHeight1()
//										!= realHeight) {
//									System.out.println("test");
//									init(realWidth, realHeight);
//									VideoView.changeScreenRevolution(realWidth, realHeight);
//									v.init();
//								}
							} else {
								System.out.println("->H264DecodeUtil->probe_sps , can not return 1");
							}
							
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
					// decode nal unit when there exists a complete NAL unit in NalBuf
					// the second parameter is the length of NAL unit
					iTemp = decoder.decode(mInstanceId, NalBuf, NalBufUsed - 4, byteBitmap);   
				
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

//	@Override
//	protected void finalize() throws Throwable {
//		if (decoder != null) {
//			decoder.uninit(mInstanceId);
//		}
//		
//		super.finalize();
//	}
	
	
}
