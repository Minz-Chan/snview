package com.starnet.snview.channelmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountInfoInXMLFile;
import com.starnet.snview.channelmanager.xml.CloudService;
import com.starnet.snview.channelmanager.xml.CloudServiceImpl;
import com.starnet.snview.channelmanager.xml.NetCloudAccountThread;
import com.starnet.snview.channelmanager.xml.PinyinComparator;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

/** 主要用于星云账号、账号中的平台内的信息显示;1、显示本地通道列表；2、加载网络的设备列表 */
@SuppressLint({ "SdCardPath", "HandlerLeak" })
public class ChannelListActivity extends BaseActivity {

	private static final String TAG = "ChannelListActivity";
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";

	private long end_time = 0;
	private int title_num = 0;
	private Context curContext;
	private TextView titleView;// 通道列表
	private Button rightButton;

	private long start_time = 0;
	private EditText search_edt;
	List<CloudAccount> searchList;
	private ImageButton startScanButton;
	private boolean isFirstSearch = false;
	private NetCloudAccountThread netThread;
	private CloudAccount collectCloudAccount;
	private final int CONNIDENTIFYDIALOG = 5;
	private final int STARCLOUNDDOWNLOAD = 10;

	private ExpandableListView mExpandableListView;
	private List<CloudAccount> cloudAccounts_enable;// 保存原来用户的Enable的值
	private List<PreviewDeviceItem> previewChannelList;// 当前预览通道
	private final int CONNECTIFYIDENTIFY_WRONG = 0x0012;
	private final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;
	private final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;
	private List<PreviewDeviceItem> originPreviewChannelList;
	private ChannelExpandableListviewAdapter chExpandableListAdapter;
	private List<CloudAccount> origin_cloudAccounts = new ArrayList<CloudAccount>();// 用于网络访问时用户信息的显示(访问前与访问后)；

	private Handler netHandler = new Handler() {// 处理线程的handler

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case STARCLOUNDDOWNLOAD:
				Bundle data = msg.getData();
				String position = data.getString("position");
				String success = data.getString("success");
				if (success.equals("Yes")) {
					int pos = Integer.valueOf(position);
					CloudAccount account = (CloudAccount) data
							.getSerializable("netCloudAccount");
					account.setRotate(true);
					origin_cloudAccounts.set(pos, account);
				} else {
					int pos = Integer.valueOf(position);
					CloudAccount acc = (CloudAccount) data
							.getSerializable("netCloudAccount");
					acc.setRotate(false);
					origin_cloudAccounts.set(pos, acc);
				}
				int size = origin_cloudAccounts.size();
				for (int i = 1; i < size; i++) {
					CloudAccount cloudAccount = origin_cloudAccounts.get(i);
					if (cloudAccount != null) {
						List<DeviceItem> dList = cloudAccount.getDeviceList();
						if ((dList != null) && (dList.size() > 0)) {
							Collections.sort(dList, new PinyinComparator());// 排序...
						}
					}
				}
				chExpandableListAdapter.notifyDataSetChanged();
				break;
			case CONNECTIFYIDENTIFY_SUCCESS:
				showToast("连接验证成功");
				if (prg.isShowing()) {
					dismissDialog(CONNIDENTIFYDIALOG);
					gotoChannelListViewActivity(msg);
					chExpandableListAdapter.notifyDataSetChanged();
				}
				break;
			case CONNECTIFYIDENTIFY_WRONG:
				if (prg.isShowing()) {
					dismissDialog(CONNIDENTIFYDIALOG);
					showToast("连接验证错误，请检查信息");
					gotoChannelListViewActivity(msg);
					chExpandableListAdapter.notifyDataSetChanged();
				}
				break;
			case CONNECTIFYIDENTIFY_TIMEOUT:
				if (prg.isShowing()) {
					dismissDialog(CONNIDENTIFYDIALOG);
					showToast("连接验证超时");
					gotoChannelListViewActivity(msg);
					chExpandableListAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}
	};

	private void gotoChannelListViewActivity(Message msg) {
		Intent intent = new Intent();
		Bundle data2 = msg.getData();
		intent.setClass(curContext, ChannelListViewActivity.class);
		data2.putString("deviceName", data2.getString("deviceName"));
		data2.putString("childPosition",
				String.valueOf(data2.getInt("childPos")));
		data2.putString("groupPosition",
				String.valueOf(data2.getInt("parentPos")));
		DeviceItem dItem = (DeviceItem) data2
				.getSerializable("identifyDeviceItem");
		data2.putSerializable("clickCloudAccount",
				data2.getSerializable("clickCloudAccount"));
		data2.putSerializable("dItem", dItem);
		intent.putExtras(data2);
		startActivityForResult(intent, 31);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_listview_activity_copy);
		initView();
		setListenersForWadgets();
	}

	private void setListenersForWadgets() {

		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				title_num++;
				if (title_num % 2 == 0) {
					search_edt.setVisibility(View.GONE);
				} else {
					search_edt.setVisibility(View.VISIBLE);
				}
			}
		});

		search_edt.addTextChangedListener(new TextWatcher() {// 搜索查询事件

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {

						Editable able = search_edt.getText();

						if (able != null) {
							isFirstSearch = true;
							String input_content = search_edt.getText()
									.toString().trim();
							if (searchList != null && searchList.size() > 0) {
								searchList.clear();
							}
							if (search_edt.getText().toString().trim().length() > 0) {
								searchList = getSearchListFromCloudAccounts(input_content);
								chExpandableListAdapter = new ChannelExpandableListviewAdapter(
										curContext, searchList, titleView);
								mExpandableListView
										.setAdapter(chExpandableListAdapter);

							} else {
								setOriginCloudAccountsEnable();
								chExpandableListAdapter = new ChannelExpandableListviewAdapter(
										curContext, origin_cloudAccounts,
										titleView);
								mExpandableListView
										.setAdapter(chExpandableListAdapter);
							}
						} else {
							Log.v(TAG, "search_edt length:"
									+ search_edt.getText().toString().trim()
											.length());
							setOriginCloudAccountsEnable();
							chExpandableListAdapter = new ChannelExpandableListviewAdapter(
									curContext, origin_cloudAccounts, titleView);
							mExpandableListView
									.setAdapter(chExpandableListAdapter);
						}
					}

					@Override
					public void afterTextChanged(Editable s) {
					}

				});

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
						previewChannelList = getPreviewChannelList(origin_cloudAccounts);

						if (!isFirstSearch) {

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
								showToast(getString(R.string.channel_manager_channellistview_channelnotchoose));
							}
						} else {
							backAndLeftButtonOperation();
						}
					}
				});

		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {// 添加返回事件
				backAndLeftButtonOperation();
			}
		});
	}

	private void setOriginCloudAccountsEnable() {
		for (int i = 0; i < origin_cloudAccounts.size(); i++) {
			origin_cloudAccounts.get(i).setEnabled(
					cloudAccounts_enable.get(i).isEnabled());
		}
	}

	protected List<PreviewDeviceItem> mergeChannelAndSearchList(
			List<PreviewDeviceItem> previewChannelList2,
			List<PreviewDeviceItem> previewSearchList) {
		if (previewSearchList == null || previewSearchList.size() == 0) {
			return previewChannelList2;
		}
		int preview_size = previewSearchList.size();
		for (int i = 0; i < preview_size; i++) {
			previewChannelList2.add(previewSearchList.get(i));
		}
		return previewChannelList2;
	}

	protected List<PreviewDeviceItem> getPreviewChannelFromSearchList(
			List<CloudAccount> searchList) {
		List<PreviewDeviceItem> previewList = new ArrayList<PreviewDeviceItem>();
		if ((searchList == null) || (searchList.size() < 1)) {
			return null;
		} else {
			int size = searchList.size();
			for (int i = 0; i < size; i++) {
				CloudAccount cloudAccount = searchList.get(i);
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
									previewDeviceItem
											.setPlatformUsername(deviceItem
													.getPlatformUsername());
									int len = deviceName.length();
									String wordLen = getString(R.string.device_manager_off_on_line_length);
									int wordLength = Integer.valueOf(wordLen);
									if (len >= wordLength) {
										String showName = deviceName.substring(
												0, wordLength);
										String w3 = getString(R.string.device_manager_online_en);
										String w4 = getString(R.string.device_manager_offline_en);
										if (showName.contains(w3)
												|| showName.contains(w4)) {
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

	private void backAndLeftButtonOperation() {

		end_time = System.currentTimeMillis();

		long time = end_time - start_time;

		if (time / 1000 >= 3) {// 3秒钟限制;超过3秒时，检测用户是否加载完毕，，如果加载完毕，则读取用户数据；否则，使用原来的数据
			Log.v(TAG, "" + (time / 1000));
			boolean allLoad = checkCloudAccountsLoad();
			if (!allLoad) {
				ChannelListActivity.this.finish();
				return;
			}
		}

		if (!checkCloudAccountsLoad()) {//
			ChannelListActivity.this.finish();
			return;
		}

		List<PreviewDeviceItem> previewChannelList = new ArrayList<PreviewDeviceItem>();
		previewChannelList = getPreviewChannelList(origin_cloudAccounts);
		if (previewChannelList.size() > 0) {
			PreviewDeviceItem p = previewChannelList.get(0);

			PreviewDeviceItem[] l = new PreviewDeviceItem[previewChannelList
					.size()];
			previewChannelList.toArray(l);

			Intent intent = ChannelListActivity.this.getIntent();
			intent.putExtra("DEVICE_ITEM_LIST", l);

			ChannelListActivity.this.setResult(8, intent);
			ChannelListActivity.this.finish();
		} else {// 选择的通道为空时，不进行播放
			List<PreviewDeviceItem> previewDeviceItems = GlobalApplication
					.getInstance().getRealplayActivity().getPreviewDevices();
			previewDeviceItems.clear();
			GlobalApplication.getInstance().getRealplayActivity()
					.notifyPreviewDevicesContentChanged();
			ChannelListActivity.this.finish();
		}
	}

	private boolean checkCloudAccountsLoad() {
		boolean allLoad = true;
		for (int i = 0; i < origin_cloudAccounts.size(); i++) {
			if (!origin_cloudAccounts.get(i).isRotate()) {
				allLoad = false;
				break;
			}
		}
		return allLoad;
	}

	protected List<PreviewDeviceItem> removeContain(
			List<PreviewDeviceItem> previewChannelList2,
			List<PreviewDeviceItem> sList) {

		if (sList == null || sList.size() == 0) {
			return previewChannelList2;
		}

		if (previewChannelList2 == null || previewChannelList2.size() == 0) {
			return previewChannelList2;
		}

		int sListSize = sList.size();

		int tempSize = previewChannelList2.size();
		for (int i = 0; i < sListSize; i++) {
			int j = 0;
			while (j < tempSize) {
				boolean isLike = isLikePreviewItem(previewChannelList2.get(j),
						sList.get(i));
				if (isLike) {
					int k = j;
					previewChannelList2.remove(j);
					j = k;
					tempSize = previewChannelList2.size();
				} else {
					j++;
				}
			}
		}

		return previewChannelList2;
	}

	private boolean isLikePreviewItem(PreviewDeviceItem preview1,
			PreviewDeviceItem preview2) {
		boolean isLike = false;
		if (preview1.getPlatformUsername().equals(
				preview2.getPlatformUsername())
				&& preview1.getDeviceRecordName().equals(
						preview2.getDeviceRecordName())) {
			isLike = true;
		}
		return isLike;
	}

	private void initView() {

		titleView = super.getTitleView();
		List<PreviewDeviceItem> previews = GlobalApplication.getInstance()
				.getRealplayActivity().getPreviewDevices();
		if ((previews == null) || (previews != null && previews.size() == 0)) {
			titleView
					.setText(getString(R.string.navigation_title_channel_list));// 设置列表标题名
		} else {
			titleView.setText(getString(R.string.navigation_title_channel_list)
					+ "(" + previews.size() + ")");// 设置列表标题名
		}

		super.setToolbarVisiable(false);

		super.hideExtendButton();
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);

		rightButton = super.getRightButton();
		super.setRightButtonBg(R.drawable.navigation_bar_search_btn_selector);

		originPreviewChannelList = GlobalApplication.getInstance()
				.getRealplayActivity().getPreviewDevices();// 获取源预览通道列表
		cloudAccounts_enable = new ArrayList<CloudAccount>();

		curContext = ChannelListActivity.this;
		startScanButton = (ImageButton) findViewById(R.id.startScan);// 开始预览按钮
		mExpandableListView = (ExpandableListView) findViewById(R.id.channel_listview);

		search_edt = (EditText) findViewById(R.id.search_et);
		search_edt.setVisibility(View.GONE);

		// caXML = new CloudAccountXML();
		origin_cloudAccounts = getCloudAccountInfoFromUI();// 获取收藏设备，以及用户信息
		int netSize = origin_cloudAccounts.size();
		copyCloudAccountEnable();

		boolean isOpen = NetWorkUtils
				.checkNetConnection(ChannelListActivity.this);// 查看网络是否开启
		if (isOpen) {
			for (int i = 1; i < netSize; i++) {// 启动线程进行网络访问，每个用户对应着一个线程
				String conn_name = "conn1";
				CloudAccount cAccount = origin_cloudAccounts.get(i);
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

			start_time = System.currentTimeMillis();

			File file = new File(CLOUD_ACCOUNT_PATH);
			if (file.exists()) {
				file.delete();
			}
		} else {
			showToast(getString(R.string.channel_manager_channellistview_netnotopen));
		}

		curContext = ChannelListActivity.this;
		chExpandableListAdapter = new ChannelExpandableListviewAdapter(
				curContext, origin_cloudAccounts, titleView);
		chExpandableListAdapter.setHandler(netHandler);
		mExpandableListView.setAdapter(chExpandableListAdapter);
	}

	private void copyCloudAccountEnable() {
		for (int i = 0; i < origin_cloudAccounts.size(); i++) {
			CloudAccount cloudAccount = new CloudAccount();
			cloudAccount.setUsername(origin_cloudAccounts.get(i).getUsername());
			cloudAccount.setPassword(origin_cloudAccounts.get(i).getPassword());
			cloudAccount.setEnabled(origin_cloudAccounts.get(i).isEnabled());
			cloudAccounts_enable.add(cloudAccount);
		}
	}

	private void showToast(String content) {
		Toast.makeText(ChannelListActivity.this, content, Toast.LENGTH_SHORT)
				.show();
	}

	private List<PreviewDeviceItem> getPreviewChannelList(
			List<CloudAccount> cloudAccounts) {
		List<PreviewDeviceItem> previewList = new ArrayList<PreviewDeviceItem>();
		if ((cloudAccounts == null) || (cloudAccounts.size() < 1)) {
			showToast(getString(R.string.channel_manager_channellistview_loadfail));
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
									previewDeviceItem
											.setPlatformUsername(deviceItem
													.getPlatformUsername());
									int len = deviceName.length();
									String wordLen = getString(R.string.device_manager_off_on_line_length);
									int wordLength = Integer.valueOf(wordLen);
									if (len >= wordLength) {
										String showName = deviceName.substring(
												0, wordLength);
										String word3 = getString(R.string.device_manager_online_en);
										String word4 = getString(R.string.device_manager_offline_en);
										if (showName.contains(word3)
												|| showName.contains(word4)) {
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

	/** 从设置界面中获取用户信息 */
	private List<CloudAccount> getCloudAccountInfoFromUI() {

		CloudAccountInfoInXMLFile caUtil = new CloudAccountInfoInXMLFile();
		List<CloudAccount> accoutInfo = new ArrayList<CloudAccount>();
		accoutInfo = caUtil.getCloudAccountInfoFromUI();
		return accoutInfo;

	}

	/** 从本地获取设备的通道列表 */
	public List<CloudAccount> getGroupListFromLocal() {// 注意，目前只有一个用户的情况下；从收藏设备中读取账户
		List<CloudAccount> groupList = ReadWriteXmlUtils
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
			if (!isFirstSearch) {
				origin_cloudAccounts.set(pos, collectCloudAccount);
				chExpandableListAdapter.notify_number = 2;
				chExpandableListAdapter.notifyDataSetChanged();
			} else {
				String userName = collectCloudAccount.getUsername();
				String password = collectCloudAccount.getPassword();

				for (int i = 0; i < searchList.size(); i++) {
					if (searchList.get(i).getUsername().equals(userName)
							&& searchList.get(i).getPassword().equals(password)) {
						searchList.set(i, collectCloudAccount);
					}
				}
				// 查看searchList值是否有变化，考虑searchList.set(i, collectCloudAccount);
				chExpandableListAdapter.notify_number = 22;
				chExpandableListAdapter.notifyDataSetChanged();
				List<DeviceItem> colDevices = collectCloudAccount
						.getDeviceList();
				if (colDevices != null && colDevices.size() > 0) {
					for (int i = 0; i < origin_cloudAccounts.size(); i++) {
						CloudAccount origin_cloudAccount = origin_cloudAccounts
								.get(i);
						if (origin_cloudAccount.getUsername().equals(userName)
								&& origin_cloudAccount.getPassword().equals(
										password)) {// 找到用户
							List<DeviceItem> originDevices = origin_cloudAccount
									.getDeviceList();
							for (int k = 0; k < colDevices.size(); k++) {
								DeviceItem colDeviceItem = colDevices.get(k);
								for (int j = 0; j < originDevices.size(); j++) {
									DeviceItem originDeviceItem = originDevices
											.get(j);
									if (originDeviceItem.getDeviceName()
											.equals(colDeviceItem
													.getDeviceName())) {
										List<Channel> channels = colDeviceItem
												.getChannelList();
										originDeviceItem
												.setChannelList(channels);
										break;
									}
								}
							}
						}
					}
				}
			}

			List<PreviewDeviceItem> newPreviewList = getPreviewChannelList(origin_cloudAccounts);
			GlobalApplication.getInstance().getRealplayActivity()
					.setPreviewDevices_copy(newPreviewList);

			// 判断获取的cloudAccount3是否是属于第一个用户(即“收藏设备”)，若是，则需要保存到收藏设备中，便于程序下一次启动时，读取结果
			if (collectCloudAccount.getUsername().equals(
					getString(R.string.device_manager_collect_device))
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
								// ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(deviceList.get(i),
								// filePath);
								ReadWriteXmlUtils.replaceSpecifyDeviceItem(
										filePath, i, deviceList.get(i));
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

	private List<CloudAccount> getSearchListFromCloudAccounts(
			String input_content2) {
		List<CloudAccount> result = new ArrayList<CloudAccount>();

		for (int i = 0; i < origin_cloudAccounts.size(); i++) {
			CloudAccount cloudAccount = origin_cloudAccounts.get(i);
			CloudAccount resultCloudAccount = new CloudAccount();
			boolean add_flag = false;

			if (cloudAccount != null) {
				List<DeviceItem> result_deviceItem = new ArrayList<DeviceItem>();
				resultCloudAccount.setDomain(cloudAccount.getDomain());
				resultCloudAccount.setPassword(cloudAccount.getPassword());
				resultCloudAccount.setPort(cloudAccount.getPort());
				resultCloudAccount.setUsername(cloudAccount.getUsername());
				resultCloudAccount.setRotate(true);
				resultCloudAccount.setEnabled(true);
				resultCloudAccount.setExpanded(false);

				List<DeviceItem> deviceItems = cloudAccount.getDeviceList();
				if (deviceItems != null) {
					for (int j = 0; j < deviceItems.size(); j++) {
						DeviceItem deviceItem = deviceItems.get(j);
						if (deviceItem.getDeviceName().contains(input_content2)) {
							result_deviceItem.add(deviceItem);
							add_flag = true;
						}
					}
				}

				if (add_flag) {
					resultCloudAccount.setDeviceList(result_deviceItem);
					result.add(resultCloudAccount);
				}
			}
		}
		return result;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			backAndLeftButtonOperation();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private boolean isCanceled = false;
	private boolean isClickCancel = false;

	ProgressDialog prg;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONNIDENTIFYDIALOG:
			prg = ProgressDialog.show(this, "",
					getString(R.string.device_manager_conn_iden), true, true);
			prg.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(CONNIDENTIFYDIALOG);
					isCanceled = true;
					isClickCancel = true;
					chExpandableListAdapter.setCancel(true);// 不进行验证
				}
			});
			return prg;
		default:
			return null;
		}
	}
}
