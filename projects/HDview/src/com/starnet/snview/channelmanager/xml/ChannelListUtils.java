package com.starnet.snview.channelmanager.xml;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

public class ChannelListUtils {

	/** 获取需要连接验证的设备列表 */
	public static List<DeviceItem> getDeviceItems(CloudAccount source) {
		List<DeviceItem> items = new ArrayList<DeviceItem>();
		if (source == null) {
			return items;
		}
		List<DeviceItem> temp = source.getDeviceList();
		if (temp == null) {
			return items;
		}
		for (DeviceItem item : temp) {
			if (item != null && !item.isConnPass() && item.isUsable()) {
				if (hasChannelSelect(item)) {
					items.add(item);
				}
			}
		}
		return items;
	}

	private static boolean hasChannelSelect(DeviceItem item) {
		boolean hasSelect = false;
		if (item == null) {
			return hasSelect;
		}
		List<Channel> channels = item.getChannelList();
		if (channels == null ) {
			return hasSelect;
		}
		
		for (Channel channel : channels) {
			if (channel.isSelected()) {
				hasSelect = true;
				break;
			}
		}
		
		return hasSelect;
	}

	/** 获取设备在收藏设备列表中的下标 */
	public static int getIndex(DeviceItem deviceItem, CloudAccount ca) {
		int index = 0;
		if (ca == null) {
			return index;
		}
		List<DeviceItem> items = ca.getDeviceList();
		if (items == null) {
			return index;
		}
		for (int i = 0; i < items.size(); i++) {
			if (deviceItem.getDeviceName().equals(items.get(i).getDeviceName())) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	public static void setChannelSelectedDeviceItem(DeviceItem item){
		if (item == null) {
			return;
		}
		List<Channel> channels = item.getChannelList();
		if (channels == null) {
			return ;
		}
		
		for (Channel channel : channels) {
			channel.setSelected(true);
		}
	}
}