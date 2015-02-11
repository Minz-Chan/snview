package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.starnet.snview.protocol.message.LoginResponse;
import com.starnet.snview.protocol.message.OWSPDateTime;

import android.annotation.SuppressLint;
import android.util.Log;

public class PlaybackControllTaskUtils {

	private static final String TAG = "DataProcessServiceImpl";
	private static final int LOGIN_SUC = 41;
	private static final int LOGIN_FAIL = 42;
	private static final int RECORDINFORS = 32;
	private static final int PLAYRECORDREQ_SUCC = 43;
	private static final int PLAYRECORDREQ_FAIL = 44;

	public static boolean isCanPlay;
	private static boolean stop = false; 
	private static boolean isPauseSuc = false;
	private static boolean isReusmeSuc = false;

	private static DataProcessService service;

	@SuppressWarnings("static-access")
	public static ArrayList<TLV_V_RecordInfo> parseResponsePacketFromSocket(InputStream receiver) {// , Handler mHandler
		ArrayList<TLV_V_RecordInfo> infoList = new ArrayList<TLV_V_RecordInfo>();
		TLV_V_SearchRecordResponse srr = new TLV_V_SearchRecordResponse();
		byte[] head = new byte[8];
		try {
			receiver.read(head);
			ByteBuffer headBuffer = ByteBuffer.allocate(8).wrap(head);
			headBuffer = headBuffer.order(ByteOrder.BIG_ENDIAN);
			int packetLen = headBuffer.getInt() - 4;// TLV的总长度
			Log.i("packetLen", "packetLen:" + packetLen);
			byte[] tBuf = new byte[7];
			receiver.read(tBuf);
			ByteBuffer tu = ByteBuffer.wrap(tBuf).order(ByteOrder.LITTLE_ENDIAN);
			int type = tu.getShort();
			int length = tu.getShort();
			byte tempresult = tu.get(4);
			byte tempcount = tu.get(5);
			byte tempreserve = tu.get(6);

			Log.i(TAG, "type:" + type + "length:" + length);
			int reserve;
			if (tempreserve < 0) {
				reserve = PlaybackControllTaskUtils.getIntFromByte(tempreserve);
			} else {
				reserve = tempreserve;
			}

			int count;
			if (tempcount < 0) {
				count = PlaybackControllTaskUtils.getIntFromByte(tempcount);
			} else {
				count = tempcount;
			}

			int result;
			if (tempresult < 0) {
				result = PlaybackControllTaskUtils.getIntFromByte(tempresult);
			} else {
				result = tempresult;
			}
			if (result == 1) {// 表示成
				isCanPlay = true;
				byte[] recordInfoDataBuffer = new byte[26*count];
				receiver.read(recordInfoDataBuffer);
				ByteBuffer riBuffers = ByteBuffer.wrap(recordInfoDataBuffer).order(ByteOrder.LITTLE_ENDIAN);
				
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						TLV_V_RecordInfo recordInfo = new TLV_V_RecordInfo();
						int t = riBuffers.getShort();
						int l = riBuffers.getShort();
						
						Log.i(TAG, "ri type:" + t + ", length:" + l);
						riBuffers.getShort();
						riBuffers.getShort();
						int deviceID = riBuffers.getInt();
						// 解析时间
						int startyear = riBuffers.getShort();
						int startMonth = riBuffers.get();
						int startDay = riBuffers.get();
						int starthour = riBuffers.get();
						int startminute = riBuffers.get();
						int startsecond = riBuffers.get();

						OWSPDateTime startTime = new OWSPDateTime();
						startTime.setYear(startyear + 2009);
						startTime.setMonth(startMonth);
						startTime.setDay(startDay);
						startTime.setHour(starthour);
						startTime.setMinute(startminute);
						startTime.setSecond(startsecond);

						int endyear = riBuffers.getShort();
						int endMonth = riBuffers.get();
						int endDay = riBuffers.get();
						int endhour = riBuffers.get();
						int endminute = riBuffers.get();
						int endsecond = riBuffers.get();

						OWSPDateTime endTime = new OWSPDateTime();
						endTime.setYear(endyear + 2009);
						endTime.setMonth(endMonth);
						endTime.setDay(endDay);
						endTime.setHour(endhour);
						endTime.setMinute(endminute);
						endTime.setSecond(endsecond);

						int channel = riBuffers.get();
						int recordTypeMask = riBuffers.get();
						int reserve1 = riBuffers.get();
						int reserve2 = riBuffers.get();
						int reserv[] = { reserve1, reserve2 };

						recordInfo.setChannel(channel);
						recordInfo.setDeviceid(deviceID);
						recordInfo.setStartTime(startTime);
						recordInfo.setEndTime(endTime);
						recordInfo.setRecordTypeMask(recordTypeMask);
						recordInfo.setReserve(reserv);

						infoList.add(recordInfo);
					}
				} else {// 没有数据
					isCanPlay = false;
					return null;
				}
			} else {
				isCanPlay = false;
				return null;
			}
			srr.setReserve(reserve);
			srr.setCount(count);
			srr.setResult(result);

			return infoList;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

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
			while (!tlvContent.equals("") && !stop) {
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
				}else if(result == PLAYRECORDREQ_SUCC){//成功时，则通知线程停止，否则，继续发送
					
					break;
				}else if(result == PLAYRECORDREQ_FAIL){
					
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
	
	private static void printTypeAndLength(int type, int length) {
		Log.i(TAG, "(type,length):" + "(" + type + "," + length + ")");
	}

	/** 解析登陆返回数据 **/
	public static boolean parseLoginRsp(InputStream receiver) {
		boolean isSuccess = false;
		LoginResponse lr = new LoginResponse();
		byte[] head = new byte[8];
		try {
			receiver.read(head);
			ByteBuffer headBuffer = ByteBuffer.wrap(head).order(ByteOrder.BIG_ENDIAN);
			int packetLen = headBuffer.getInt() - 4;// TLV的总长度
			Log.i(TAG, "packetLen:" + packetLen);
			byte[] temp = new byte[8];
			receiver.read(temp);
			ByteBuffer tuBf = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN);

			int type1 = tuBf.getShort();
			int leng1 = tuBf.getShort();
			printTypeAndLength(type1,leng1);

			short result = tuBf.getShort();
			short reserve = tuBf.getShort();

			if (result == 1) {// 表示成功????????????
				isCanPlay = true;
				isSuccess = true;
			} else {// 表示成功
				isCanPlay = false;
				isSuccess = false;
			}

			lr.setReserve(reserve);
			lr.setResult(result);
			return isSuccess;
		} catch (IOException e) {
			return false;
		}
		// return lr;
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
	
	public static void setStop(boolean isStop ){
		stop = isStop;
	}
}
