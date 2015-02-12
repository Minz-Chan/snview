package com.starnet.snview.playback.utils;

import java.io.InputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.util.Log;

public class PlaybackControllTaskUtils {

	private static final String TAG = "DataProcessServiceImpl";
	private static final int LOGIN_SUC = 41;
	private static final int LOGIN_FAIL = 42;
	private static final int RECORDINFORS = 32;
	private static final int PAUSE_PLAYRECORDREQ_SUCC = 45;
	private static final int PAUSE_PLAYRECORDREQ_FAIL = 46;
	private static final int RESUME_PLAYRECORDREQ_SUCC = 43;
	private static final int RESUME_PLAYRECORDREQ_FAIL = 44;

	public static boolean isCanPlay;
	private static boolean pause = false;
	private static DataProcessService service;
	private static boolean isPauseSuc = false;
	private static boolean isReusmeSuc = false;

	@SuppressWarnings("resource")
	public static void newParseVideoAndAudioRsp(InputStream receiver) {// 需要一直从Socket中接收数据，直到接收完毕
		try {
			SocketInputStream sockIn = new SocketInputStream(receiver);
			byte[] packetHeaderBuf = new byte[8];
			sockIn.read(packetHeaderBuf);
			TLV_V_PacketHeader owspPacketHeader = (TLV_V_PacketHeader) ByteArray2Object.convert2Object(TLV_V_PacketHeader.class, packetHeaderBuf,0, OWSP_LEN.OwspPacketHeader);
			if (!(owspPacketHeader.getPacket_length() >= 4 && owspPacketHeader.getPacket_seq() > 0)) {
				return;
			}
			Log.i(TAG, "Packet seq:" + owspPacketHeader.getPacket_seq() + ", len:" + (owspPacketHeader.getPacket_length() - 4));
			byte[] tlvContent = new byte[655350]; // 1 * 1024 * 1024
			tlvContent = makesureBufferEnough(tlvContent,(int) owspPacketHeader.getPacket_length() - 4);
			sockIn.read(tlvContent, 0,(int) owspPacketHeader.getPacket_length() - 4);
			while (!tlvContent.equals("")) {// && !stop
				int result = service.process(tlvContent,(int) owspPacketHeader.getPacket_length());
				if (result == -1) {/* 表示读到了TLV_T_RECORD_EOF包,则需要退出 */
					break;
				}else if(result == LOGIN_SUC){//表示登陆成功
					isCanPlay = true;
					break;
				}else if(result == LOGIN_FAIL){//表示登陆失败
					isCanPlay = false;
					break;
				} else if (result == RECORDINFORS){
					break;
				}else if(result == RESUME_PLAYRECORDREQ_SUCC){//成功时，则通知线程停止，否则，继续发送
					
				}else if(result == RESUME_PLAYRECORDREQ_FAIL){
					
					break;
				}else if(result == PAUSE_PLAYRECORDREQ_FAIL){
					
					break;
				}
				
				do {
					for (int i = 0; i < 8; i++) {/* 数据重置 */
						packetHeaderBuf[i] = 0;
					}
					sockIn.read(packetHeaderBuf, 0, 8);/* 读取公共包头 */
					owspPacketHeader = (TLV_V_PacketHeader) ByteArray2Object.convert2Object(TLV_V_PacketHeader.class,packetHeaderBuf, 0,OWSP_LEN.OwspPacketHeader);
				} while (owspPacketHeader.getPacket_length() <= 0);
				tlvContent = makesureBufferEnough(tlvContent,(int) owspPacketHeader.getPacket_length() - 4);
				resetArray(tlvContent);/* 重置数据数组 */
				sockIn.read(tlvContent, 0,(int) owspPacketHeader.getPacket_length() - 4);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<TLV_V_RecordInfo> getRecordInfos(){
		return service.getRecordInfos();
	}

	/** 字节数组转化为字符串 **/
	@SuppressLint("DefaultLocale")
	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			String temp = hex.toUpperCase();
			ret += temp;
		}
		return ret;
	}

	public static int getIntFromByte(byte b) {
		String result = byte2HexString(b);
		return HexString2Ten(result);
	}

	/** 单个字节转化为字符串 **/
	@SuppressLint("DefaultLocale")
	public static String byte2HexString(byte b) {
		String ret = "";
		String hex = Integer.toHexString(b & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		ret += hex.toUpperCase();
		return ret;
	}

	public static int HexString2Ten(String result) {
		int re = Integer.parseInt(result, 16);
		return re;
	}

	public static void resetArray(byte[] b) {
		int i = 0;
		for (i = 0; i < b.length; i++) {
			b[i] = 0;
		}
	}

	private static byte[] makesureBufferEnough(byte[] buffer, int realSize) {
		byte[] result = buffer;
		int size = buffer.length;
		if (size < realSize) {
			buffer = null;
			buffer = new byte[(int) (realSize * 1.2)];
			result = buffer;
		}
		return result;
	}

	public static void setService(DataProcessService serv) {
		service = serv;
	}

	public static String getIP(String[] ips) {
		String ip = "";
		int len = ips.length;
		for (int i = 0; i < len - 1; i++) {
			String temp = ips[i] + ".";
			ip += temp;
		}
		ip = ip + ips[len-1];
		return ip;
	}
	
	public static void setPause(boolean isPause ){
		pause = isPause;
	}
}