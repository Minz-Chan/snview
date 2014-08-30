package com.starnet.snview.channelmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountUtil;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.channelmanager.xml.CloudService;
import com.starnet.snview.channelmanager.xml.CloudServiceImpl;
import com.starnet.snview.channelmanager.xml.NetCloudAccountThread;
import com.starnet.snview.channelmanager.xml.PinyinComparator;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.NetWorkUtils;

/**
 * @author 陈名珍
 * @Date 2014/7/3
 * @ClassName ChannelListActivity.java
 * @Description 主要用于星云账号、账号中的平台内的信息显示;1、显示本地通道列表；2、加载网络的设备列表
 * @Modifier 赵康
 * @Modify date 2014/7/7
 * @Modify description 增加了字段：starUserNameList、starPlatformList
 */

@SuppressLint({ "SdCardPath", "HandlerLeak" })
public class ChannelListActivity extends BaseActivity {

	private static final String TAG = "ChannelListActivity";

	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";

	private ExpandableListView mExpandableListView;
	private CloudAccountXML caXML;

	private ImageButton startScanButton;// 开始预览按钮；// 用于通道列表选择的显示,(选择前和选择后)
	private List<CloudAccount> cloudAccounts = new ArrayList<CloudAccount>();// 用于网络访问时用户信息的显示(访问前与访问后)；
	private Context curContext;
	private ChannelExpandableListviewAdapter chExpandableListAdapter;

	private NetCloudAccountThread netThread;
	private CloudAccount collectCloudAccount;

	private List<PreviewDeviceItem> previewChannelList;// 当前预览通道

	// 有关开启线程的操作；
	// boolean stopThread=false;

	private Handler netHandler = new Handler() {// 处理线程的handler

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String position = data.getString("position");
			String success = data.getString("success");
			if (success.equals("Yes")) {// 通知ExpandableListView的第position个位置的progressBar不再转动;获取到访问的整个网络数据；
				int pos = Integer.valueOf(position);
				CloudAccount cloudAccount = (CloudAccount) data
						.getSerializable("netCloudAccount");// 取回网络访问数据；
				cloudAccount.setRotate(true);
				cloudAccounts.set(pos, cloudAccount);
			} else {
				int pos = Integer.valueOf(position);
				CloudAccount cloudAccount = (CloudAccount) data
						.getSerializable("netCloudAccount");// 取回网络访问数据；
				cloudAccount.setRotate(false);
				cloudAccounts.set(pos, cloudAccount);
			}

			int size = cloudAccounts.size();
			for (int i = 1; i < size; i++) {
				CloudAccount cloudAccount = cloudAccounts.get(i);
				if (cloudAccount != null) {
					List<DeviceItem> deviceList = cloudAccount.getDeviceList();
					if ((deviceList != null) && (deviceList.size() > 0)) {
						Collections.sort(deviceList, new PinyinComparator());// 排序...
					}
				}
			}
			chExpandableListAdapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_listview_activity);
		initView();
		setListenersForWadgets();
	}

	private void setListenersForWadgets() {
		mExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {

				CloudAccount cloudAccount = (CloudAccount) parent
						.getExpandableListAdapter().getGroup(groupPosition);// 获取用户账号信息
				if (cloudAccount.isExpanded()) {// 判断列表是否已经展开
					cloudAccount.setExpanded(false);
				} else {
					cloudAccount.setExpanded(true);
				}
				return false;
			}
		});
		startScanButton.setOnClickListener(new OnClickListener() {// 单击该按钮时，收集选择的通道列表，从cloudAccounts中就可以选择。。。

					@Override
					public void onClick(View v) {
						previewChannelList = new ArrayList<PreviewDeviceItem>();
						previewChannelList = getPreviewChannelList(cloudAccounts);

						if (previewChannelList.size() > 0) {
							PreviewDeviceItem p = previewChannelList.get(0);

							PreviewDeviceItem[] l = new PreviewDeviceItem[previewChannelList
									.size()];
							previewChannelList.toArray(l);

							Intent intent = ChannelListActivity.this
									.getIntent();
							intent.putExtra("DEVICE_ITEM_LIST", l);

							ChannelListActivity.this.setResult(8, intent);
							ChannelListActivity.this.finish();
						} else {
							String printSentence = getString(R.string.channel_manager_channellistview_loadfail);
							Toast toast = Toast.makeText(ChannelListActivity.this, printSentence,Toast.LENGTH_SHORT);
							toast.show();
						}

					}
				});

		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChannelListActivity.this.finish();
			}
		});
	}

	private void initView() {

		super.setTitleViewText(getString(R.string.navigation_title_channel_list));// 设置列表标题名
		super.setToolbarVisiable(false);
		super.hideRightButton();
		super.hideExtendButton();
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);

		curContext = ChannelListActivity.this;
		startScanButton = (ImageButton) findViewById(R.id.startScan);// 开始预览按钮
		mExpandableListView = (ExpandableListView) findViewById(R.id.channel_listview);
		caXML = new CloudAccountXML();
		// 当用户选择了1以后，便是每次打开软件后，都从从网络上读取设备信息；
		// 当用户选择了0以后，即用户从此便从上次保存的文档中获取用户信息；根据用户的选择而改变

		caXML = new CloudAccountXML();
		cloudAccounts = getCloudAccountInfoFromUI();// 获取收藏设备，以及用户信息
		int netSize = cloudAccounts.size();

		// 查看网络是否开启
		boolean isOpen = NetWorkUtils.checkNetConnection(ChannelListActivity.this);
		if (isOpen) {
			// while (!stopThread) {
			for (int i = 1; i < netSize; i++) {// 启动线程进行网络访问，每个用户对应着一个线程
				String conn_name = "conn1";
				CloudAccount cAccount = cloudAccounts.get(i);
				boolean isEnable = cAccount.isEnabled();
				if (isEnable) {
					cAccount.setRotate(false);
				} else {
					cAccount.setRotate(true);
				}
				if (isEnable) {// 如果启用该用户的话，则访问网络，否则，不访问；不访问网络时，其rotate=true;
					CloudService cloudService = new CloudServiceImpl(conn_name);
					netThread = new NetCloudAccountThread(cAccount,
							cloudService, netHandler, i);
					netThread.start();// 线程开启，进行网络访问
				}
			}

			File file = new File(CLOUD_ACCOUNT_PATH);
			if (file.exists()) {
				file.delete();
			}
			// }
		} else {
			String printSentence = getString(R.string.channel_manager_channellistview_netnotopen);
			Toast toast = Toast.makeText(ChannelListActivity.this,printSentence, Toast.LENGTH_LONG);
			toast.show();
		}

		curContext = ChannelListActivity.this;
		chExpandableListAdapter = new ChannelExpandableListviewAdapter(
				curContext, cloudAccounts);
		mExpandableListView.setAdapter(chExpandableListAdapter);

		// boolean enabled = chExpandableListAdapter.areAllItemsEnabled();
		// if( !enabled ){//如果所有的条目有不可用的，则不支持点击事件；
		// mExpandableListView.setEnabled(false);
		// }

		// for(int i = 1; i < netSize; i++){
		// CloudAccount cAccount = cloudAccounts.get(i);
		// boolean isEnable = cAccount.isEnabled();
		// if(isEnable){
		// mExpandableListView.setSelection(i);
		// mExpandableListView.
		// }
		// }
	}

	private List<PreviewDeviceItem> getPreviewChannelList(
			List<CloudAccount> cloudAccounts) {
		List<PreviewDeviceItem> previewList = new ArrayList<PreviewDeviceItem>();
		if ((cloudAccounts == null) || (cloudAccounts.size() < 1)) {
			// 打印一句话，用户尚未进行选择
			String printSentence = getString(R.string.channel_manager_channellistview_channelnotchoose);
			Toast toast = Toast.makeText(ChannelListActivity.this,
					printSentence, Toast.LENGTH_SHORT);
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
									int len = deviceName.length();
									String wordLen = getString(R.string.device_manager_off_on_line_length);
									int wordLength = Integer.valueOf(wordLen);
									if (len >= wordLength) {
										String showName = deviceName.substring(
												0, wordLength);
										// String word1 =
										// getString(R.string.device_manager_online_cn);
										// String word2 =
										// getString(R.string.device_manager_offline_cn);
										String word3 = getString(R.string.device_manager_online_en);
										String word4 = getString(R.string.device_manager_offline_en);
										if (showName.contains(word3)
												|| showName.contains(word4)) {
											/*
											 * ||showName.contains(word1)||showName
											 * .contains(word2)
											 */
											deviceName = deviceName
													.substring(wordLength);
										}
									}

									previewDeviceItem
											.setDeviceRecordName(deviceName);

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

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description TODO
	 * @return
	 */
	private List<CloudAccount> getCloudAccountInfoFromUI() {// 从设置界面中获取用户信息

		CloudAccountUtil caUtil = new CloudAccountUtil();
		List<CloudAccount> accoutInfo = new ArrayList<CloudAccount>();
		accoutInfo = caUtil.getCloudAccountInfoFromUI();
		return accoutInfo;

	}

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description 从本地获取设备的通道列表
	 * @return
	 */
	public List<CloudAccount> getGroupListFromLocal() {// 注意，目前只有一个用户的情况下；从收藏设备中读取账户
		List<CloudAccount> groupList = caXML
				.readCloudAccountFromXML(CLOUD_ACCOUNT_PATH);
		return groupList;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 根据得到的值确定状态框的显示情形,全选、半选或者空选,通知ExpandableListView中状态框的改变
		if ((resultCode == 31)) {
			Bundle bundle = data.getExtras();
			collectCloudAccount = (CloudAccount) bundle.getSerializable("wca");
			// 更新ExpandableListView指定的按钮
			int pos = bundle.getInt("parentPos");
			cloudAccounts.set(pos, collectCloudAccount);
			chExpandableListAdapter.notifyDataSetChanged();
			caXML = new CloudAccountXML();

			// 判断获取的cloudAccount3是否是属于第一个用户(即“收藏设备”)，若是，则需要保存到收藏设备中，便于程序下一次启动时，读取结果
			if (collectCloudAccount.getUsername().equals("收藏设备")
					&& (collectCloudAccount.getDomain().equals("com"))
					&& (collectCloudAccount.getPort().equals("808"))
					&& (collectCloudAccount.getPassword().equals("0208"))) {
				Thread thread = new Thread() {
					@Override
					public void run() {
						super.run();
						List<DeviceItem> deviceList = collectCloudAccount
								.getDeviceList();
						int size = deviceList.size();
						for (int i = 0; i < size; i++) {
							try {
								caXML.addNewDeviceItemToCollectEquipmentXML(
										deviceList.get(i), filePath);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				thread.start();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// boolean isFinished = isFinishing();
		// Log.i(TAG, ""+isFinished);
		// if ((keyCode == KeyEvent.KEYCODE_BACK)) {
		//
		// }
		ChannelListActivity.this.onDestroy();
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// stopThread = true;
		super.onDestroy();
	}
}
