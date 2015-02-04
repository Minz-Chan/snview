package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.starnet.snview.protocol.message.LoginResponse;
import com.starnet.snview.protocol.message.OWSPDateTime;

import android.R.integer;
import android.annotation.SuppressLint;
import android.util.Log;

public class PlaybackControllTaskUtils {

	private static final String TAG = "PlaybackControllTaskUtils";

	public static boolean isCanPlay;

	private static DataProcessService service = new DataProcessServiceImpl("");;

	public static ArrayList<TLV_V_RecordInfo> parseResponsePacketFromSocket(
			InputStream receiver) {// , Handler mHandler
		ArrayList<TLV_V_RecordInfo> infoList = new ArrayList<TLV_V_RecordInfo>();
		TLV_V_SearchRecordResponse srr = new TLV_V_SearchRecordResponse();
		byte[] head = new byte[8];
		try {
			receiver.read(head);
			@SuppressWarnings("static-access")
			ByteBuffer headBuffer = ByteBuffer.allocate(8).wrap(head);
			headBuffer = headBuffer.order(ByteOrder.BIG_ENDIAN);
			int packetLen = headBuffer.getInt() - 4;// TLV的总长度
			Log.i("packetLen", "packetLen:" + packetLen);
			byte[] tBuf = new byte[7];
			receiver.read(tBuf);
			ByteBuffer tu = ByteBuffer.wrap(tBuf)
					.order(ByteOrder.LITTLE_ENDIAN);
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
			if (tempcount < 0) {
				result = PlaybackControllTaskUtils.getIntFromByte(tempresult);
			} else {
				result = tempresult;
			}

			if (result == 1) {// 表示成
				isCanPlay = true;
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						TLV_V_RecordInfo recordInfo = new TLV_V_RecordInfo();
						byte[] temp = new byte[26];
						receiver.read(temp);
						ByteBuffer tempBuffer = ByteBuffer.wrap(temp).order(
								ByteOrder.LITTLE_ENDIAN);
						tempBuffer.getShort();
						tempBuffer.getShort();
						int deviceID = tempBuffer.getInt();
						// 解析时间
						int startyear = tempBuffer.getShort();
						int startMonth = tempBuffer.get();
						int startDay = tempBuffer.get();
						int starthour = tempBuffer.get();
						int startminute = tempBuffer.get();
						int startsecond = tempBuffer.get();

						OWSPDateTime startTime = new OWSPDateTime();
						startTime.setYear(startyear + 2009);
						startTime.setMonth(startMonth);
						startTime.setDay(startDay);
						startTime.setHour(starthour);
						startTime.setMinute(startminute);
						startTime.setSecond(startsecond);

						int endyear = tempBuffer.getShort();
						int endMonth = tempBuffer.get();
						int endDay = tempBuffer.get();
						int endhour = tempBuffer.get();
						int endminute = tempBuffer.get();
						int endsecond = tempBuffer.get();

						OWSPDateTime endTime = new OWSPDateTime();
						endTime.setYear(endyear + 2009);
						endTime.setMonth(endMonth);
						endTime.setDay(endDay);
						endTime.setHour(endhour);
						endTime.setMinute(endminute);
						endTime.setSecond(endsecond);

						int channel = tempBuffer.get();
						int recordTypeMask = tempBuffer.get();
						int reserve1 = tempBuffer.get();
						int reserve2 = tempBuffer.get();
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

	/** 解析只有视频数据 **/
	public static void parseVideoOnlyRsp(InputStream receiver) {

	}

	/** 解析只有视频数据 **/
	public static void parseSearchRecordRsp(InputStream receiver) {

	}

	@SuppressWarnings("resource")
	public static void newParseVideoAndAudioRsp(InputStream receiver) {// 需要一直从Socket中接收数据，直到接收完毕
		try {
			SocketInputStream sockIn = new SocketInputStream(receiver);
			byte[] packetHeaderBuf = new byte[8];
			sockIn.read(packetHeaderBuf);
			TLV_V_PacketHeader owspPacketHeader = (TLV_V_PacketHeader) ByteArray2Object
					.convert2Object(TLV_V_PacketHeader.class, packetHeaderBuf,
							0, OWSP_LEN.OwspPacketHeader);
			if (!(owspPacketHeader.getPacket_length() >= 4 && owspPacketHeader
					.getPacket_seq() > 0)) {
				return;
			}
			byte[] tlvContent = new byte[65535]; // 1 * 1024 * 1024
			tlvContent = makesureBufferEnough(tlvContent, (int) owspPacketHeader.getPacket_length() - 4);
			int len00 = (int) owspPacketHeader.getPacket_length() - 4;
			sockIn.read(tlvContent, 0, len00);
			while (!tlvContent.equals("")) {
				service.process(tlvContent, (int) owspPacketHeader.getPacket_length());
				do {
					for (int i = 0; i < 8; i++) {/* 数据重置 */
						packetHeaderBuf[i] = 0;
					}
					sockIn.read(packetHeaderBuf, 0, 8);/* 读取公共包头 */
					int headerLen = OWSP_LEN.OwspPacketHeader;
					owspPacketHeader = (TLV_V_PacketHeader) ByteArray2Object
							.convert2Object(TLV_V_PacketHeader.class,
									packetHeaderBuf, 0, headerLen);
				} while (owspPacketHeader.getPacket_length() <= 0);
				tlvContent = makesureBufferEnough(tlvContent, (int) owspPacketHeader.getPacket_length() - 4);
				/* 重置数据数组 */
				resetArray(tlvContent);
				int iRead = sockIn.read(tlvContent, 0, (int) owspPacketHeader.getPacket_length() - 4);
				Log.i(TAG, "iRead:" + iRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 解析包含视频和音频数据 **/
	public static void parseVideoAndAudioRsp(InputStream receiver) {// 需要一直从Socket中接收数据，直到接收完毕
		try {

			byte[] headBuf = new byte[8];
			receiver.read(headBuf);
			ByteBuffer headBuffer = ByteBuffer.wrap(headBuf).order(
					ByteOrder.BIG_ENDIAN);
			int packetLen = headBuffer.getInt() - 4;// TLV的总长度
			Log.i(TAG, "packetLen:" + packetLen);
			byte[] rspBuf = new byte[6];
			receiver.read(rspBuf);
			ByteBuffer rspBuffer = ByteBuffer.wrap(rspBuf).order(
					ByteOrder.LITTLE_ENDIAN);
			int rspType = rspBuffer.getShort();
			int rspLen = rspBuffer.getShort();
			Log.i(TAG, "" + rspType + "--rspLen" + rspLen);
			int result = rspBuffer.get();
			int reserve = rspBuffer.get();
			Log.i(TAG, "reserve" + reserve);
			if (result == 1) {// 表示请求成功
				TLV_V_StreamDataFormat sdf = getStreamFormatInfo(receiver);// StreamDataFormat信息
				while (true) {// 如何确定收到的是eof
					// 获取公共包头
					int packetLength = parseCommonPacket(receiver);

					Log.i(TAG, "packetLength:" + packetLength);
					byte[] aRspByte = new byte[4];
					receiver.read(aRspByte);
					ByteBuffer aRspBuffer = ByteBuffer.wrap(aRspByte).order(
							ByteOrder.LITTLE_ENDIAN);
					int aRrpType = aRspBuffer.getShort();
					int aRrpLength = aRspBuffer.getShort();
					printTypeAndLength(aRrpType, aRrpLength);

					if (aRrpType != 354) {
						TLV_V_VideoFrameInfo vfiFrameInfo = getVideoFrameInfo(receiver);// 视频数据
						byte[] videoData = getVideoIFrameData(receiver);
						TLV_V_AudioInfo audioInfo = getAudioInfo(receiver);
						byte[] audioData = getAudioData(receiver);
						Log.i(TAG, sdf.toString());
						Log.i(TAG, vfiFrameInfo.toString());
						Log.i(TAG, audioInfo.toString());
						Log.i(TAG, "videoData len:" + videoData.length);
						Log.i(TAG, "audioData len:" + audioData.length);
					} else {
						break;
					}
				}
			} else {// 表示请求失败

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int parseCommonPacket(InputStream receiver)
			throws IOException {
		byte[] headerBuf = new byte[8];
		receiver.read(headerBuf);
		ByteBuffer headerBuffer = ByteBuffer.wrap(headerBuf).order(ByteOrder.BIG_ENDIAN);
		return headerBuffer.getInt() - 4;// TLV的总长度
	}

	private static byte[] getAudioData(InputStream receiver) throws IOException {
		byte[] aDBuf = new byte[4];// AudioDataFormatInfo
		receiver.read(aDBuf);
		ByteBuffer audioBufer = ByteBuffer.wrap(aDBuf).order(
				ByteOrder.LITTLE_ENDIAN);
		int audioType = audioBufer.getShort();
		int audioLength = audioBufer.getShort();// 视频数据的长度
		printTypeAndLength(audioType, audioLength);
		byte[] audioByte = new byte[audioLength];
		receiver.read(audioByte);
		ByteBuffer audBuf = ByteBuffer.wrap(audioByte).order(
				ByteOrder.LITTLE_ENDIAN);
		return audBuf.array();
	}

	private static TLV_V_AudioInfo getAudioInfo(InputStream receiver)
			throws IOException {
		TLV_V_AudioInfo afi = new TLV_V_AudioInfo();
		// 音频信息AudioInfo
		byte[] audioInfoBuf = new byte[12];
		receiver.read(audioInfoBuf);
		ByteBuffer audioBuf = ByteBuffer.wrap(audioInfoBuf).order(
				ByteOrder.LITTLE_ENDIAN);
		int type = audioBuf.getShort();
		int length = audioBuf.getShort();

		printTypeAndLength(type, length);

		int channlId = audioBuf.get();
		int audioReserve = audioBuf.get();
		int checkSum = audioBuf.getShort();
		int audioTime = audioBuf.getInt();

		afi.setChannelId(channlId);
		afi.setReserve(audioReserve);
		afi.setChecksum(checkSum);
		afi.setTime(audioTime);
		return afi;
	}

	private static void printTypeAndLength(int type, int length) {
		Log.i(TAG, "(type,length):" + "(" + type + "," + length + ")");
	}

	private static byte[] getVideoIFrameData(InputStream receiver)
			throws IOException {

		byte[] vFIBuf = new byte[4];// VideoData
		receiver.read(vFIBuf);
		ByteBuffer std = ByteBuffer.wrap(vFIBuf).order(ByteOrder.LITTLE_ENDIAN);
		int type = std.getShort();
		int length = std.getShort();// 视频数据的长度
		Log.i(TAG, "type" + type);
		// 视频数据
		byte[] vFDBuff = new byte[length];// VideoFrameInfo
		receiver.read(vFDBuff);
		ByteBuffer std4 = ByteBuffer.wrap(vFDBuff).order(
				ByteOrder.LITTLE_ENDIAN);
		byte[] videoArray = std4.array();// ？？？？？
		return videoArray;
	}

	private static TLV_V_VideoFrameInfo getVideoFrameInfo(InputStream receiver)
			throws IOException {

		TLV_V_VideoFrameInfo vfi = new TLV_V_VideoFrameInfo();
		byte[] vFIBuffer = new byte[12];// VideoFrameInfo
		receiver.read(vFIBuffer);
		ByteBuffer std = ByteBuffer.wrap(vFIBuffer).order(
				ByteOrder.LITTLE_ENDIAN);

		short channelId = std.get();
		short reserve = std.get();
		int checksum = std.getShort();
		int fIndex = std.getInt();
		int time = std.getInt();

		vfi.setChannelId(channelId);
		vfi.setChecksum(checksum);
		vfi.setFrameIndex(fIndex);
		vfi.setTime(time);
		vfi.setReserve(reserve);
		return vfi;
	}

	private static TLV_V_StreamDataFormat getStreamFormatInfo(
			InputStream receiver) throws IOException {
		byte[] buffer = new byte[44];
		receiver.read(buffer);
		TLV_V_StreamDataFormat sdf = new TLV_V_StreamDataFormat();

		ByteBuffer std = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
		int type = std.getShort();
		int length = std.getShort();
		printTypeAndLength(type, length);

		int videoChannel = std.get();
		int audioChannel = std.get();
		int dataType = std.get();
		int reser = std.get();

		// VideoDataFormat
		TLV_V_VideoDataFormat vdf = new TLV_V_VideoDataFormat();
		int codecId = std.getInt();
		int bitrate = std.getInt();
		int width = std.getShort();
		int height = std.getShort();
		short framerate = std.get();
		short colorDepth = std.get();
		int rserve = std.getShort();
		vdf.setCodecId(codecId);
		vdf.setBitrate(bitrate);
		vdf.setWidth(width);
		vdf.setHeight(height);
		vdf.setFramerate(framerate);
		vdf.setColorDepth(colorDepth);
		vdf.setReserve(rserve);
		// VideoDataFormat

		// AudioDataFormat?????
		TLV_V_AudioDataFormat adFormat = new TLV_V_AudioDataFormat();
		int samplesPerSecond = std.getInt();
		int audioBitrate = std.getInt();
		int waveFormat = std.getShort();
		int channelNumber = std.getShort();
		int blockAlign = std.getShort();
		int bitsPerSample = std.getShort();
		int frameInterval = std.getShort();
		int andioReserve = std.getShort();
		adFormat.setSamplesPerSecond(samplesPerSecond);
		adFormat.setBitrate(audioBitrate);
		adFormat.setWaveFormat(waveFormat);
		adFormat.setChannelNumber(channelNumber);
		adFormat.setBlockAlign(blockAlign);
		adFormat.setBitrate(bitsPerSample);
		adFormat.setFrameInterval(frameInterval);
		adFormat.setReserve(andioReserve);
		// AudioDataFormat?????

		// StreamDataFormat???????????
		return sdf;

	}

	/** 解析登陆返回数据 **/
	public static boolean parseLoginRsp(InputStream receiver) {
		boolean isSuccess = false;
		LoginResponse lr = new LoginResponse();
		byte[] head = new byte[8];
		try {
			receiver.read(head);
			ByteBuffer headBuffer = ByteBuffer.wrap(head).order(
					ByteOrder.BIG_ENDIAN);
			int packetLen = headBuffer.getInt() - 4;// TLV的总长度
			Log.i(TAG, "packetLen:" + packetLen);
			byte[] temp = new byte[8];
			receiver.read(temp);
			ByteBuffer tuBf = ByteBuffer.wrap(temp).order(
					ByteOrder.LITTLE_ENDIAN);

			int type1 = tuBf.getShort();
			int leng1 = tuBf.getShort();
			// tuBf.getShort();
			// tuBf.getShort();
			Log.i(TAG, "type1:" + type1 + "--leng1:" + leng1);

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
}
