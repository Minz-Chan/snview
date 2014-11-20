package com.starnet.snview.realplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Document;

import android.content.Context;
import android.util.Log;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountInfoInXMLFile;
import com.starnet.snview.channelmanager.xml.CloudService;
import com.starnet.snview.channelmanager.xml.CloudServiceImpl;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.CloudAccountUtils;
import com.starnet.snview.util.CollectDeviceParams;

public abstract class RefreshDeviceConnectionTask {
	public static final int REFRESH_CLOUDACCOUT_PROCESS_DIALOG = 0x0008;
	private static final String TAG = "RefreshDeviceConnectionTask";
	private final int DEFAULT_TIMEOUT_IN_SECONDS = 7; 
	
	private boolean isTimeout;
	private boolean shouldTimeoutThreadOver;
	private boolean isCanceled;
	private boolean isExistCloudAccount;
	private boolean isDeviceConnectionInfoUpdated;
	private AtomicInteger subWorkFinishedCount;
	
	private Context context;
	private Thread workThread;
	private Thread timeoutThread;
	
	private Vector<CloudAccount> updatedAccounts;
	private ArrayList<PreviewDeviceItem> updatedDevices;
	
	public RefreshDeviceConnectionTask(Context context) {
		this.context = context;
		isExistCloudAccount = true;
		subWorkFinishedCount = new AtomicInteger();
		updatedAccounts = new Vector<CloudAccount>();
		updatedDevices = new ArrayList<PreviewDeviceItem>();
		
		workThread = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean success = true;
				try {
					startRefreshDeviceConnectionInfoWork();
				} catch (Exception e) {
					success = false;
					onWorkFailed();
					e.printStackTrace();
				} finally {
					if (!isCanceled && !isTimeout && success) {
						if (updatedAccounts.size() > 0) {
							onWorkFinished();
						} else {
							onWorkFailed();
						}
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
							if (!isCanceled) {
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
	
	/**
	 * 更新星云平台账户下相关设备连接信息。仅对通过星云平台账户选择预览的设备进行连接信息
	 * 更新，收藏夹中的设备不进行更新。
	 */
	private void startRefreshDeviceConnectionInfoWork() {
		List<CloudAccount> cloudAccountsToBeUpdated = getCloudAccountToBeUpdated();
		if (cloudAccountsToBeUpdated.size() == 0) {
			isExistCloudAccount = false;  // 仅存在收藏设备、无星云平台账户
			return;
		}
		
		// 星云账户下设备信息更新
		startUpdateCloundAccountInfoTasks(cloudAccountsToBeUpdated);
		waitForSubTaskFinished(cloudAccountsToBeUpdated.size());
	}
	
	/**
	 * 获取待更新星云平台账户列表（约定：平台间账户名不重复）
	 * @return 待更新星云账户列表
	 */
	private List<CloudAccount> getCloudAccountToBeUpdated() {
		List<CloudAccount> allCloudAccounts = new CloudAccountInfoInXMLFile().getCloudAccountInfoFromUI();
		List<CloudAccount> cloudAccountsToBeUpdated = new ArrayList<CloudAccount>();
		List<String> cloudAccountNamesToBeUpdated = new ArrayList<String>();
		for (PreviewDeviceItem item : updatedDevices) {
			if (!cloudAccountNamesToBeUpdated.contains(item
					.getPlatformUsername())
					&& item.getPlatformUsername() != null
					&& !item.getPlatformUsername().equals(CollectDeviceParams.DEFAULT_COLLECTDEVICENAME_PREVIEWITEM_UPDATE)) {
				cloudAccountNamesToBeUpdated.add(item.getPlatformUsername());
			}
		}
		for (String cloudAccountName : cloudAccountNamesToBeUpdated) {
			for (CloudAccount ca : allCloudAccounts) {
				if (cloudAccountName.equals(ca.getUsername())) {
					cloudAccountsToBeUpdated.add(ca);
				}	
			}
		}
		
		return cloudAccountsToBeUpdated;
	}
	
	private void startUpdateCloundAccountInfoTasks(List<CloudAccount> cloudAccountsToBeUpdated) {
		for (CloudAccount account : cloudAccountsToBeUpdated) {
			new UpdateCloundAccountInfoSubTask(account).start();
		}
	}
	
	private void waitForSubTaskFinished(int taskCount) {
		while (subWorkFinishedCount.get() != taskCount) {};
	}
	
	private void onWorkFailed() {
		if (isCanceled) {
			return;
		}
		
		shouldTimeoutThreadOver = true;
		if (isExistCloudAccount) {
			onUpdateWorkFailed();
		} else {
			onWorkFinished();
		}
	}
	
	private void onWorkFinished() {
		if (isCanceled) {
			return;
		}
		
		shouldTimeoutThreadOver = true;
		updatePreviewDevices();
		onUpdateWorkFinished(updatedDevices, isDeviceConnectionInfoUpdated);
	}
	
	private void updatePreviewDevices() {
		int successWorkCount = updatedAccounts.size();
		if (successWorkCount > 0) {
			ArrayList<PreviewDeviceItem> allUpdatedDevices = new ArrayList<PreviewDeviceItem>();
			for (CloudAccount account : updatedAccounts) {  // 获取所有已更新设备信息
				Log.i(TAG, "Account updated: " + account.getUsername());
				for (DeviceItem di : account.getDeviceList()) {
					PreviewDeviceItem pdi = new PreviewDeviceItem();
					pdi.setDeviceRecordName(di.getDeviceName()
							.replace(getString(R.string.device_manager_online_en), "")
							.replace(getString(R.string.device_manager_offline_en), ""));
					pdi.setSvrIp(di.getSvrIp());
					pdi.setSvrPort(di.getSvrPort());
					pdi.setLoginPass(di.getLoginPass());
					pdi.setLoginUser(di.getLoginUser());
					pdi.setPlatformUsername(account.getUsername());
					allUpdatedDevices.add(pdi);
				}
			}
			
			for (PreviewDeviceItem itemUpdated : updatedDevices) { // 更新已有设备列表连接信息
				for (PreviewDeviceItem newItem : allUpdatedDevices) {
					if (isSameDevice(itemUpdated, newItem)) {
						itemUpdated.setSvrIp(newItem.getSvrIp());
						itemUpdated.setSvrPort(newItem.getSvrPort());
						itemUpdated.setLoginPass(newItem.getLoginPass());
						itemUpdated.setLoginUser(newItem.getLoginUser());
						isDeviceConnectionInfoUpdated = true;
					}
				}
			}
		}
	}
	
	private boolean isSameDevice(PreviewDeviceItem d1, PreviewDeviceItem d2) {
		return d1.getPlatformUsername() != null && d2.getPlatformUsername() != null 
				&& d1.getPlatformUsername().equals(d2.getPlatformUsername())
				&& d1.getDeviceRecordName() != null && d2.getDeviceRecordName() != null
				&& d1.getDeviceRecordName().equals(d2.getDeviceRecordName());
	}
	
	private String getString(int resId) {
		return context.getString(resId);
	}
	
	private void onTimeout() {
		if (isCanceled) {
			return;
		}
		
		if (subWorkFinishedCount.get() == 0) { // 所有星云账户更新超时，可能网络不连通
			onUpdateWorkTimeout();
		} else { // 部分星云账户更新超时，更新未超时账户相关设备信息至预览列表
			onWorkFinished();
		}
	}
	
	public void start() {
		isTimeout = false;
		shouldTimeoutThreadOver = false;
		isCanceled = false;
		isDeviceConnectionInfoUpdated = false;
		subWorkFinishedCount.set(0);
		
		init();		
		workThread.start();
		timeoutThread.start();
	}
	
	public void init() {
		List<PreviewDeviceItem> oldDevices = ((RealplayActivity) context)
				.getPreviewDevices();
		for (PreviewDeviceItem item : oldDevices) {
			updatedDevices.add(item);
		}
	}
	
	public void cancel() {
		isCanceled = true;
		shouldTimeoutThreadOver = true;
	}
	
	protected abstract void onUpdateWorkFinished(List<PreviewDeviceItem> devices, boolean isDeviceConnectionInfoUpdated);
	protected abstract void onUpdateWorkTimeout();
	protected abstract void onUpdateWorkFailed();
	
	
	private CloudService cloudService = new CloudServiceImpl("");
	private CloudAccountUtils cloudAccoutnUtils = new CloudAccountUtils();
	class UpdateCloundAccountInfoSubTask extends Thread {
		private CloudAccount cloudAccount;

		public UpdateCloundAccountInfoSubTask(CloudAccount cloudAccount) {
			this.cloudAccount = cloudAccount;
		}
		
		@Override
		public void run() {
			String domain = cloudAccount.getDomain();
			String port = cloudAccount.getPort();
			String username = cloudAccount.getUsername();
			String password = cloudAccount.getPassword();
			String deviceName = "con";
			String responseErrorText;
			Document doc;
			
			try {
				doc = cloudService.SendURLPost(domain, port, username, password, deviceName);
				responseErrorText = cloudService.readXmlStatus(doc);
				if (responseErrorText == null) { // 无失败原因，即成功
					List<DVRDevice> dvrDeviceList = cloudService.readXmlDVRDevices(doc);
					//CloudAccount cloudAccount = cloudAccoutnUtils.getCloudAccountFromDVRDevice(context, dvrDeviceList);
					cloudAccount.setDeviceList(cloudAccoutnUtils
							.getCloudAccountFromDVRDevice(context,
									dvrDeviceList).getDeviceList());
					updatedAccounts.add(cloudAccount);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				subWorkFinishedCount.incrementAndGet();
			}

		}
		
	}

}
