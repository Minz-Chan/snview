package com.starnet.snview.realplay;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountUtil;
import com.starnet.snview.channelmanager.xml.CloudService;
import com.starnet.snview.channelmanager.xml.CloudServiceImpl;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.SplashActivity;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.CloudAccountUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.PreviewItemXMLUtils;

public class RefreshDeviceConnectionInfo {
	public static final int REFRESH_CLOUDACCOUT_PROCESS_DIALOG = 0x0011;
	private final int DEFAULT_TIMEOUT_IN_SECONDS = 7; 
	
	private boolean isTimeout;
	private boolean isWorkFinished;
	private boolean shouldTimeoutThreadOver;
	private boolean hasDataToBeUpdated = false;
	
	private Context context;
	private Thread workThread;
	private Thread timeoutThread;
	
	private ArrayList<PreviewDeviceItem> devices;
	private Handler handler;
	private Message msg;
	private final int mHandler_close = 11;
	
	public RefreshDeviceConnectionInfo(Context context,Handler handler) {
		this.context = context;
		this.handler = handler;
		msg = new Message();
		workThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					startRefreshDeviceConnectionInfoWork();
				} catch (Exception e) {
					onWorkFailed();
					e.printStackTrace();
				} finally {
					if (!isTimeout) {
						isWorkFinished = true;
						onWorkFinished();
					} 
				}
			}
		});
		timeoutThread = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean canRun = true;
				int timeCount = 0;
				while (canRun && !shouldTimeoutThreadOver) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == DEFAULT_TIMEOUT_IN_SECONDS) {
							isTimeout = true;
							canRun = false;
							if (!isWorkFinished) {
								onTimeout();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void startRefreshDeviceConnectionInfoWork() throws Exception {//预览通道信息的更新操作
		devices = (ArrayList<PreviewDeviceItem>) PreviewItemXMLUtils.getPreviewItemListInfoFromXML(context
				.getString(R.string.common_last_devicelist_path));
		if (devices == null || devices.size() == 0) {
			return;
//			throw new IllegalStateException("device count should to be zero");
		}		
		CloudService service = new CloudServiceImpl("");
		CloudAccountUtil caUtil = new CloudAccountUtil();
		List<CloudAccount> cloudAccountList = caUtil.getCloudAccountInfoFromUI();
		if (cloudAccountList != null && cloudAccountList.size() <= 1) {
			return;
		}		
		//考虑没有星云平台用户、只有手动添加的收藏设备的情况下，应该如何处理...
		boolean isOpen = NetWorkUtils.checkNetConnection(context);
		if (!isOpen) {
			return;
		}
		if(isAnyCloudAccountEnabled(cloudAccountList)){
			CloudAccountUtils caUtils= new CloudAccountUtils();
			List<CloudAccount>cAList = new ArrayList<CloudAccount>();
			for(int i = 1;i<cloudAccountList.size();i++){
				String domain = cloudAccountList.get(i).getDomain();
				String port = cloudAccountList.get(i).getPort();
				String username = cloudAccountList.get(i).getUsername();
				String password = cloudAccountList.get(i).getPassword();
				String deviceName = "con";
				Document doc = service.SendURLPost(domain, port, username, password, deviceName);
				String visitSuc = service.readXmlStatus(doc);
				if(visitSuc == null){//如果访问网络成功，则进行更新数据；
					CloudAccount cloudAccount = new CloudAccount();
					List<DVRDevice> dvrDeviceList = service.readXmlDVRDevices(doc);
					cloudAccount = caUtils.getCloudAccountFromDVRDevice(context,dvrDeviceList);
					cAList.add(cloudAccount);
					hasDataToBeUpdated = true;
				}else{
					hasDataToBeUpdated = false;
					break;
				}
			}
			if (hasDataToBeUpdated) {
				updatePreviewItems(devices,cAList);
			}
		}
	}
	
	private void updatePreviewItems(List<PreviewDeviceItem> pDevices,List<CloudAccount> cAList) {
		int pSize = pDevices.size();
		for (int i = 0; i < pSize; i++) {
			PreviewDeviceItem previewDeviceItem = pDevices.get(i);
			int caSize = cAList.size();
			for(int j = 1;j<caSize;j++){
				CloudAccount jAccount = cAList.get(j);
				List<DeviceItem> deviceItems = jAccount.getDeviceList();
				if (deviceItems!=null && deviceItems.size() > 0 ) {
					String preName = previewDeviceItem.getDeviceRecordName();
					for (int k = 0; k < deviceItems.size(); k++) {
						if (preName.equals(deviceItems.get(k).getDeviceName())) {
							updatePreviewItem(previewDeviceItem,deviceItems.get(k));
						}
					}
				}
			}
		}
	}

	private void updatePreviewItem(PreviewDeviceItem previewDeviceItem,DeviceItem deviceItem) {
		previewDeviceItem.setSvrIp(deviceItem.getSvrIp());
		previewDeviceItem.setSvrPort(deviceItem.getSvrPort());
		previewDeviceItem.setLoginPass(deviceItem.getLoginPass());
		previewDeviceItem.setLoginUser(deviceItem.getLoginUser());
	}

	private boolean isAnyCloudAccountEnabled(List<CloudAccount> cloudAccountList) {
		boolean isExistEnable = false;
		for(int i = 1;i<cloudAccountList.size();i++){
			if(cloudAccountList.get(i).isEnabled()){
				isExistEnable = true;
				break;
			}
		}
		return isExistEnable;
	}
	
	private void onWorkFailed() {
		dismissUpdatingDialog();
		previewWithLastPreservedData();
	}

	private void onWorkFinished() {
		shouldTimeoutThreadOver = true;
		if (hasDataToBeUpdated) {
			getSplashActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					getSplashActivity().previewWithUpdatedData(devices);
				}
			});
		}
		dismissUpdatingDialog();
		devices = (ArrayList<PreviewDeviceItem>) PreviewItemXMLUtils.getPreviewItemListInfoFromXML(context
				.getString(R.string.common_last_devicelist_path));
		Bundle data = new Bundle();
		data.putParcelableArrayList("previewItems", devices);
		msg.setData(data);
		msg.what = mHandler_close;
		handler.handleMessage(msg);
	}
	
	private void onTimeout() {
		dismissUpdatingDialog();
		previewWithLastPreservedData();
	}
	
	private void previewWithLastPreservedData() {
		getSplashActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				getSplashActivity().previewWithLastPreservedData();
			}
		});
		devices = (ArrayList<PreviewDeviceItem>) PreviewItemXMLUtils
				.getPreviewItemListInfoFromXML(context
						.getString(R.string.common_last_devicelist_path));
		Bundle data = new Bundle();
		data.putParcelableArrayList("previewItems", devices);
		msg.setData(data);
		msg.what = mHandler_close;
		handler.handleMessage(msg);
	}
	
	public void start() {
		isTimeout = false;
		isWorkFinished = false;
		shouldTimeoutThreadOver = false;
		workThread.start();
		timeoutThread.start();
		
		showUpdatingDialog();
	}
	
	private void showUpdatingDialog() {
		getSplashActivity().runOnUiThread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				getSplashActivity().showDialog(REFRESH_CLOUDACCOUT_PROCESS_DIALOG);
			}
		});
	}
	
	private void dismissUpdatingDialog() {
		getSplashActivity().runOnUiThread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				getSplashActivity().dismissDialog(REFRESH_CLOUDACCOUT_PROCESS_DIALOG);
			}
		});
	}
	
	private SplashActivity getSplashActivity() {
		return (SplashActivity) context;
	}
}