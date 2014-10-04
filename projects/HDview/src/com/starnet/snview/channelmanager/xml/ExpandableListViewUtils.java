package com.starnet.snview.channelmanager.xml;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

public class ExpandableListViewUtils {
	
	public static Context context;

	public static String getStateForCloudAccount(CloudAccount cloudAccount) {
		String state = "empty";
		if(cloudAccount == null){
			return state;
		}
		int sum_number = 0 ;
		int select_number = 0 ;
		List<DeviceItem> deviceList = cloudAccount.getDeviceList();
		if(deviceList == null ||(deviceList!= null && deviceList.size() == 0)){
			return state;
		}
		
		for(int i = 0 ;i<deviceList.size();i++){
			DeviceItem deviceItem = deviceList.get(i);
			List<Channel> channelList = deviceItem.getChannelList();
			int channelSize = channelList.size();
			for(int j =0 ;j<channelSize ;j++){
				Channel channel = channelList.get(j);
				sum_number++;
				if(channel.isSelected()){
					select_number++;
				}
			}
		}
		
		if(select_number==sum_number && sum_number!=0){
			state = "all";
		}else if(select_number < sum_number && sum_number!=0 && select_number!=0){
			state = "half";
		}
		return state;
	}

	public static void setStateForCloudAccount(String state,CloudAccount cloudAccount) {
		if(cloudAccount == null){
			return ;
		}
		List<DeviceItem> deviceList = cloudAccount.getDeviceList();
		if(cloudAccount != null && deviceList!= null && deviceList.size() ==0){
			return;
		}
		
		if(state.equals("empty")){
			for(int i = 0 ;i<deviceList.size();i++){
				DeviceItem deviceItem = deviceList.get(i);
				List<Channel> channelList = deviceItem.getChannelList();
				int channelSize = channelList.size();
				for(int j =0 ;j<channelSize ;j++){
					Channel channel = channelList.get(j);
					channel.setSelected(false);
				}
			}
		}
		
		if(state.equals("all")){
			for(int i = 0 ;i<deviceList.size();i++){
				DeviceItem deviceItem = deviceList.get(i);
				List<Channel> channelList = deviceItem.getChannelList();
				int channelSize = channelList.size();
				for(int j =0 ;j<channelSize ;j++){
					Channel channel = channelList.get(j);
					channel.setSelected(true);
				}
			}
		}
	}
	
	public static int getPreviewListFromCloudAccounts(List<CloudAccount> cloudAccountList2) {
		if((cloudAccountList2 == null)||(cloudAccountList2 !=null && cloudAccountList2.size() == 0)){
			return 0 ;
		}
		int number = 0 ;
		int size = cloudAccountList2.size();
		for(int i =0 ;i<size ;i++){
			CloudAccount cloudAccount = cloudAccountList2.get(i);
			List<DeviceItem> deviceItemList = cloudAccount.getDeviceList();
			if(deviceItemList != null){
				int deviceSize = deviceItemList.size();
				for(int j =0 ;j<deviceSize ;j++){
					DeviceItem deviceItem = deviceItemList.get(j);
					if(deviceItem != null){
						List<Channel> channelList = deviceItem.getChannelList();
						int channelSize = channelList.size();
						for(int k =0 ;k<channelSize;k++){
							Channel channel = channelList.get(k);
							if((channel!=null)&&channel.isSelected()){
								number++;
							}
						}
					}
				}
			}
		}
		return number;
	}
	
	public static List<PreviewDeviceItem> getPreviewChannelList(
			List<CloudAccount> cloudAccounts) {
		List<PreviewDeviceItem> previewList = new ArrayList<PreviewDeviceItem>();
		if ((cloudAccounts == null) || (cloudAccounts.size() < 1)) {
			String printSentence = context.getString(R.string.channel_manager_channellistview_loadfail);
			Toast toast = Toast.makeText(context,printSentence, Toast.LENGTH_SHORT);
			toast.show();
		} else {
			int size = cloudAccounts.size();
			for (int i = 0; i < size; i++) {
				CloudAccount cloudAccount = cloudAccounts.get(i);
				List<DeviceItem> deviceItems = cloudAccount.getDeviceList();
				if (deviceItems != null) {
					int deviceSize = deviceItems.size();
					for (int j = 0; j < deviceSize; j++) {
						DeviceItem deviceItem = deviceItems.get(j);
						List<Channel> channelList = deviceItem.getChannelList();
						if (channelList != null) {
							int channelSize = channelList.size();
							for (int k = 0; k < channelSize; k++) {
								Channel channel = channelList.get(k);
								if (channel.isSelected()) {// 判断通道列表是否选择
									PreviewDeviceItem previewDeviceItem = new PreviewDeviceItem();
									previewDeviceItem.setChannel(channel
											.getChannelNo());
									previewDeviceItem.setLoginPass(deviceItem
											.getLoginPass());
									previewDeviceItem.setLoginUser(deviceItem
											.getLoginUser());
									previewDeviceItem.setSvrIp(deviceItem
											.getSvrIp());
									previewDeviceItem.setSvrPort(deviceItem
											.getSvrPort());
									String deviceName = deviceItem
											.getDeviceName();
									previewDeviceItem.setPlatformUsername(deviceItem.getPlatformUsername());
									int len = deviceName.length();
									String wordLen = context.getString(R.string.device_manager_off_on_line_length);
									int wordLength = Integer.valueOf(wordLen);
									if (len >= wordLength) {
										String showName = deviceName.substring(
												0, wordLength);
										String word3 = context.getString(R.string.device_manager_online_en);
										String word4 = context.getString(R.string.device_manager_offline_en);
										if (showName.contains(word3)
												|| showName.contains(word4)) {
											deviceName = deviceName
													.substring(wordLength);
										}
									}

									previewDeviceItem.setDeviceRecordName(deviceName);

									previewList.add(previewDeviceItem);
								}
							}
						}
					}
				}
			}

		}
		return previewList;
	}
}