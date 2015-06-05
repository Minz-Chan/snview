package com.video.hdview.realplay;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;

import android.content.Context;

import com.video.hdview.R;
import com.video.hdview.channelmanager.Channel;
import com.video.hdview.devicemanager.DeviceItem;
import com.video.hdview.syssetting.CloudAccount;
import com.video.hdview.util.ReadWriteXmlUtils;

public class RealplayActivityUtils {
	
	private static List<PreviewDeviceItem> mPreviewItem;

	// 负责启动时预览通道信息的修改
	public static List<PreviewDeviceItem> updatePreviewItemInfo(Context context,List<CloudAccount>cloudAccounts) throws DocumentException {
		List<PreviewDeviceItem> oldDevices = ReadWriteXmlUtils.getPreviewItemListInfoFromXML(context.getString(R.string.common_last_devicelist_path));
		if(oldDevices==null){
			return oldDevices;
		}
		if(cloudAccounts==null) return null;
		
		for(int i =0 ;i<oldDevices.size();i++){
			PreviewDeviceItem oldPreviewDeviceItem = oldDevices.get(i);
			//对预览通道进行信息更新;如果没有发现相同的设备，则删除该预览通道...
			String platformUsername = oldPreviewDeviceItem.getPlatformUsername();
			for(int j =0 ;j<cloudAccounts.size();j++){
				if(platformUsername.equals(cloudAccounts.get(j).getUsername())){
					List<DeviceItem> deviceItems = cloudAccounts.get(j).getDeviceList();
					if(deviceItems==null) break;
					for(int k = 0;k<deviceItems.size();k++){
						if(oldPreviewDeviceItem.getDeviceRecordName().equals(deviceItems.get(k).getDeviceName())){
							oldPreviewDeviceItem.setLoginPass(deviceItems.get(k).getLoginPass());
							oldPreviewDeviceItem.setLoginUser(deviceItems.get(k).getLoginUser());
							oldPreviewDeviceItem.setSvrIp(deviceItems.get(k).getSvrIp());
							oldPreviewDeviceItem.setSvrPort(deviceItems.get(k).getSvrPort());
						}
					}
				}
			}
		}
		
		return oldDevices;
	}

	//在groupList中设置通道的选择情形
	public static List<PreviewDeviceItem> setSelectedAccDevices(List<PreviewDeviceItem> devices,List<CloudAccount> igroupList) {
		if (devices == null || igroupList == null) {
			return devices ;
		}
		if (devices != null && devices.size() == 0) {
			return devices ;
		}
		int tempSize = devices.size();
		List<Integer> delIndex = new ArrayList<Integer>();
		//删除不存在的预览通道列表
		for (int i = 0; i < tempSize; i++) {
			boolean isExist = isExistPreviewItem(devices.get(i),igroupList);
			if(!isExist){
				delIndex.add(i);
			}
		}
		if(delIndex.size() > 0 ){
			for(int i = delIndex.size()-1 ;i >= 0;i--){
				PreviewDeviceItem delPD = devices.get(delIndex.get(i));
				devices.remove(delPD);
			}
		}
		//删除不存在的预览通道列表
		
		int previewSize = devices.size();
		int groupListSize = igroupList.size();
		for (int i = 0; i < previewSize; i++) {
			PreviewDeviceItem iPreviewDeviceItem = devices.get(i);
			for (int j = 0; j < groupListSize; j++) {
				CloudAccount ica = igroupList.get(j);
				if (iPreviewDeviceItem.getPlatformUsername().equals(ica.getUsername())) {//||iPreviewDeviceItem.getPlatformUsername().equals("收藏设备")
					List<DeviceItem> deviceItems = igroupList.get(j).getDeviceList();
					if(deviceItems != null){
						int deviceSize = deviceItems.size();
						for(int k = 0 ;k<deviceSize;k++){
							DeviceItem idi = deviceItems.get(k);
							List<Channel> channels = idi.getChannelList();
							for(int m =0;m<channels.size();m++){
								if(idi.getDeviceName().contains(iPreviewDeviceItem.getDeviceRecordName())&&channels.get(m).getChannelNo() == iPreviewDeviceItem.getChannel()){
									iPreviewDeviceItem.setLoginUser(idi.getLoginUser());
									iPreviewDeviceItem.setLoginPass(idi.getLoginPass());
									iPreviewDeviceItem.setSvrIp(idi.getSvrIp());
									iPreviewDeviceItem.setSvrPort(idi.getSvrPort());
								}
							}
						}
					}
				}
			}
		}
		mPreviewItem = devices;
		return devices ;
	}
	
	private static boolean isExistPreviewItem(PreviewDeviceItem previewDeviceItem, List<CloudAccount> groupList) {
		boolean isExist = false;
		int groupSize = groupList.size();
		for(int i =0 ;i<groupSize;i++){
			CloudAccount ica = groupList.get(i);
			String platFormUsername = previewDeviceItem.getPlatformUsername();
			if(platFormUsername!=null&&platFormUsername.equals(ica.getUsername())){
				List<DeviceItem> devices = ica.getDeviceList();
				if(devices!=null && devices.size()>0){
					int deviceSize = devices.size();
					for(int k =0 ;k<deviceSize;k++){
						if(devices.get(k).getDeviceName().contains(previewDeviceItem.getDeviceRecordName())){
							isExist = true;
							return isExist;
						}
					}
				}
			}
		}
		return isExist;
	}

	public static List<PreviewDeviceItem> getPreviceItems(){
		return mPreviewItem;
	}
}