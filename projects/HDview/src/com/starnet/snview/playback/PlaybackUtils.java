package com.starnet.snview.playback;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;

import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class PlaybackUtils {

	public static boolean isClickOk = false;
	public static boolean stateFlag = false;
	public static boolean exapandFlag = false;
	private static final String ACCOUNT_FILEPATH_STRING = "/data/data/com.starnet.snview/star_cloudAccount.xml";
	
	public static int[] getValidateTime(String endTime) {
		int timeData[] = new int[5];
		String[] tempData = endTime.split(" ");
		String days = tempData[0];
		String[] data = days.split("-");
		int year = Integer.valueOf(data[0]);
		int month = Integer.valueOf(data[1]);
		int day = Integer.valueOf(data[2]);

		String hourMinute = tempData[1];

		String[] hm = hourMinute.split(":");

		int hour = Integer.valueOf(hm[0]);
		int mintute = Integer.valueOf(hm[1]);

		timeData[0] = year;
		timeData[1] = month;
		timeData[2] = day;
		timeData[3] = hour;
		timeData[4] = mintute;
		return timeData;
	}

	
	public static OWSPDateTime getOWSPDateTime(String time) {
		OWSPDateTime owspTime = new OWSPDateTime();
		String[] sumTime = time.split(" ");
		String ymdTemp = sumTime[0];
		String hmsTemp = sumTime[1];
		int[] ymd = getIntYMDData(ymdTemp);
		int[] hms = getIntHMSData(hmsTemp);
		owspTime.setYear(ymd[0]);
		owspTime.setMonth(ymd[1]);
		owspTime.setDay(ymd[2]);
		owspTime.setHour(hms[0]);
		owspTime.setMinute(hms[1]);
		owspTime.setSecond(hms[2]);
		return owspTime;
	}
	
	/**获取收藏设备并且返回给**/
	public static CloudAccount getCollectCloudAccount(String accountName){
		CloudAccount ca = new CloudAccount();
		try {
			List<DeviceItem> items = ReadWriteXmlUtils.getCollectDeviceListFromXML(ChannelListActivity.filePath);
			ca.setEnabled(true);
			ca.setRotate(true);
			ca.setExpanded(false);
			ca.setUsername(accountName);
			ca.setDeviceList(items);
		} catch (Exception e) {
			return ca;
		}
		return ca;
	}
	
	public static CloudAccount getFirstCollectCloudAccount(String accountName){
		CloudAccount cloudAccount = new CloudAccount();
		cloudAccount.setUsername(accountName);
		cloudAccount.setEnabled(true);
		cloudAccount.setPort("8080");
		cloudAccount.setDomain("bo.com");
		cloudAccount.setPassword("4a50");
		cloudAccount.setExpanded(false);
		cloudAccount.setRotate(true);
		cloudAccount.setDeviceList(null);
		return cloudAccount;
	}
	
	public static List<CloudAccount> getCloudAccounts() {
		try {
			List<CloudAccount> accounts = new ArrayList<CloudAccount>();
			List<CloudAccount> cas = ReadWriteXmlUtils.getCloudAccountList(ACCOUNT_FILEPATH_STRING);
			if ((cas != null) && (cas.size() > 0)) {
				for (int i = 0; i < cas.size(); i++) {
					if (cas.get(i).isEnabled()) {
						accounts.add(cas.get(i));
					}
				}
			}
			return accounts;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int getEnableCACount(List<CloudAccount> accounts) {
		int count = 0;
		for (int i = 0; i < accounts.size(); i++) {// 启动线程进行网络访问，每个用户对应着一个线程
			CloudAccount cAccount = accounts.get(i);
			boolean isEnable = cAccount.isEnabled();
			if (isEnable) {
				count++;
			}
		}
		return count;
	}

	private static int[] getIntHMSData(String ymdTemp) {
		int[] data = new int[3];
		String[] temp = ymdTemp.split(":");
		for (int i = 0; i < 2; i++) {
			data[i] = Integer.valueOf(temp[i]);
		}
		return data;
	}

	private static int[] getIntYMDData(String ymdTemp) {
		int[] data = new int[3];
		String[] temp = ymdTemp.split("-");
		for (int i = 0; i < 3; i++) {
			data[i] = Integer.valueOf(temp[i]);
		}
		return data;
	}
}
