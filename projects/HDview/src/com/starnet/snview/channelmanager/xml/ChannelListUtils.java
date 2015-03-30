package com.starnet.snview.channelmanager.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;

import android.R.integer;

import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.ReadWriteXmlUtils;

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
		if (channels == null) {
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

	public static void setChannelSelectedDeviceItem(DeviceItem item) {
		if (item == null) {
			return;
		}
		List<Channel> channels = item.getChannelList();
		if (channels == null) {
			return;
		}

		for (Channel channel : channels) {
			channel.setSelected(true);
		}
	}

	/** 检测星云用户是否已经将数据加载完毕 ,如果加载完毕返回true；否则，返回false **/
	public static boolean checkCloudAccountsLoad(List<CloudAccount> accounts) {
		boolean allLoad = true;
		if (accounts != null) {
			for (int i = 0; i < accounts.size(); i++) {
				if (!accounts.get(i).isRotate()) {// 未加载完成
					allLoad = false;
					break;
				}
			}
		}
		return allLoad;
	}

	//获取上一次该账户下的通道的选择列表
	public static List<PreviewDeviceItem> getLastSelectPreviewItems(String username, List<PreviewDeviceItem> oriPreviewChnls) {
		List<PreviewDeviceItem> ps = new ArrayList<PreviewDeviceItem>();
		
		if (oriPreviewChnls == null) {
			return ps;
		}
		
		for (PreviewDeviceItem pItem : oriPreviewChnls) {
			if (pItem.getPlatformUsername().equals(username)) {
				ps.add(pItem);
			}
		}
		return ps;
	}

	/**在上一次的选择通道中，获取需要删除的预览通道列表**/
	public static List<PreviewDeviceItem> getDeletePreviewItems(List<PreviewDeviceItem> currPreviewChanls, List<PreviewDeviceItem> lastSelectPs) {
		List<PreviewDeviceItem> deletePs = new ArrayList<PreviewDeviceItem>();
		for (PreviewDeviceItem pi : lastSelectPs) {
			if (!isExist(pi,currPreviewChanls)) {
				PreviewDeviceItem temp = new PreviewDeviceItem();
				temp.setChannel(pi.getChannel());
				temp.setDeviceRecordName(pi.getDeviceRecordName());
				temp.setLoginPass(pi.getLoginPass());
				temp.setLoginUser(pi.getLoginUser());
				temp.setPlatformUsername(pi.getPlatformUsername());
				temp.setSvrIp(pi.getSvrIp());
				temp.setSvrPort(pi.getSvrPort());
				deletePs.add(temp);
			}
		}
		return deletePs;
	}

	public static void deletePreviewItemInXML(List<PreviewDeviceItem> delPs,
			List<PreviewDeviceItem> preItemsInXML) throws IOException,
			DocumentException {
		for (int i = 0; i < delPs.size(); i++) {
			ReadWriteXmlUtils.removePreviewItemInXML(delPs.get(i));
		}
	}

	public static List<PreviewDeviceItem> getAddPreviewItems(
			List<PreviewDeviceItem> previewChanls,
			List<PreviewDeviceItem> lastSelectPs) {
		List<PreviewDeviceItem> ps = new ArrayList<PreviewDeviceItem>();
		if (previewChanls == null) {
			return ps;
		}
		for (PreviewDeviceItem pi : previewChanls) {
			if (!isExist(pi, lastSelectPs)) {
				PreviewDeviceItem temp = new PreviewDeviceItem();
				temp.setChannel(pi.getChannel());
				temp.setDeviceRecordName(pi.getDeviceRecordName());
				temp.setLoginPass(pi.getLoginPass());
				temp.setLoginUser(pi.getLoginUser());
				temp.setPlatformUsername(pi.getPlatformUsername());
				temp.setSvrIp(pi.getSvrIp());
				temp.setSvrPort(pi.getSvrPort());
				ps.add(temp);
			}
		}
		return ps;
	}

	private static boolean isExist(PreviewDeviceItem pi,
			List<PreviewDeviceItem> lastSelectPs) {
		for (PreviewDeviceItem tp : lastSelectPs) {
			if (pi.getDeviceRecordName().equals(tp.getDeviceRecordName())
					&& pi.getPlatformUsername().equals(tp.getPlatformUsername())
					&& (tp.getChannel() == pi.getChannel())) {
				return true;
			}
		}
		return false;
	}

	public static void addNewPreviewItemsToXML(List<PreviewDeviceItem> addPs) throws IOException, DocumentException {
		ReadWriteXmlUtils.addNewPreviewItemsToXML(addPs);
	}

	// private static int getIndexOf(PreviewDeviceItem
	// pi,List<PreviewDeviceItem> preItemsInXML) {
	// int index = -1;
	// if (preItemsInXML==null) {
	// return -1;
	// }
	// for (int i = 0; i < preItemsInXML.size(); i++) {
	// if
	// (pi.getDeviceRecordName().equals(preItemsInXML.get(i).getDeviceRecordName())&&pi.getChannel()
	// == preItemsInXML.get(i).getChannel()) {
	// index = i;
	// break;
	// }
	// }
	// return index;
	// }
}