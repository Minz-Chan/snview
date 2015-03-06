package com.starnet.snview.devicemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class DeviceViewActivity extends BaseActivity {

	private static final int EDIT = 20;
	private static final int ADD = 10;

	private Context context;
	private ProgressDialog loadDataPrg;
	private LoadCollectDeviceItemsTask task;
	private final int LOAD_COLLECT_DEVICEITEM = 0x0010;
	public static final int SEMI_AUTO_ADD = 0x0011;//手动界面值手动输入添加返回码

	private ListView mListView;
	private int clickPosition = 0;
	private int deletPosition = 0;
	private DeviceItem clickDeviceItem;
	private DeviceListAdapter dLAdapter;
	private DeviceItem deleteDeviceItem;
	private Button navigation_bar_add_btn;
	private List<DeviceItem> deviceItemList;

	private List<PreviewDeviceItem> previewDeviceItems; // 预览通道
	private List<PreviewDeviceItem> deletePDeviceItems = new ArrayList<PreviewDeviceItem>(); // 预览通道

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_activity);
		initView();
		setListeners();
	}

	private void setListeners() {
		mListView.setOnItemClickListener(new OnItemClickListener() { // 进入该设备的信息查看界面
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						
						clickPosition = position;
						clickDeviceItem = deviceItemList.get(position);
						Intent intent = new Intent();
						intent.setClass(context,DeviceScanActivity.class);
						Bundle bundle = new Bundle();
						bundle.putInt("position", clickPosition);
						bundle.putSerializable("clickDeviceItem",clickDeviceItem);
						intent.putExtras(bundle);
						startActivityForResult(intent, 20);

					}
				});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Builder builder = new Builder(DeviceViewActivity.this);
				deleteDeviceItem = deviceItemList.get(position);
				String titleName = deleteDeviceItem.getDeviceName();
				deletPosition = position;

				String word1 = getString(R.string.device_manager_offline_en);
				String word2 = getString(R.string.device_manager_offline_cn);
				String word3 = getString(R.string.device_manager_online_cn);
				String word4 = getString(R.string.device_manager_online_en);
				String wordLen = getString(R.string.device_manager_off_on_line_length);
				int len = Integer.valueOf(wordLen);

				if ((titleName.length() > (len - 1))
						&& ((titleName.contains(word1) || titleName
								.contains(word2)) || (titleName.contains(word3) || titleName
								.contains(word4)))) {
					titleName = titleName.substring(4);
				}
				builder.setTitle(getString(R.string.device_manager_deviceview_delete_device)
						+ " " + titleName + " ?");
				builder.setPositiveButton(
						getString(R.string.device_manager_deviceview_ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									int previewSize = previewDeviceItems.size();
									for (int i = 0; i < previewSize; i++) {
										PreviewDeviceItem previewDeviceItem = previewDeviceItems
												.get(i);
										boolean isContained = checkPreviewDeviceIsInDevicesCollect(
												previewDeviceItem,
												deleteDeviceItem);
										if (isContained) {
											deletePDeviceItems
													.add(previewDeviceItem);// 获取需要删除的预览通道
										}
									}
									int delSize = deletePDeviceItems.size();
									for (int i = 0; i < delSize; i++) {
										PreviewDeviceItem delPreDeviceItem = deletePDeviceItems
												.get(i);
										previewDeviceItems
												.remove(delPreDeviceItem);
									}
									if (delSize > 0) {
										GlobalApplication
												.getInstance()
												.getRealplayActivity()
												.notifyPreviewDevicesContentChanged();
									}
									ReadWriteXmlUtils
											.removeDeviceItemToCollectEquipmentXML(
													deleteDeviceItem,
													ChannelListActivity.filePath);
								} catch (DocumentException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
								deviceItemList.remove(deletPosition);
								dLAdapter.notifyDataSetChanged(); // 列表的更新操作
							}
						});
				builder.setNegativeButton(
						getString(R.string.device_manager_deviceview_cancel),
						null);
				builder.show();
				return true;
			}
		});

		navigation_bar_add_btn.setOnClickListener(new OnClickListener() { // 手动与选择增加设备

					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(DeviceViewActivity.this,DeviceCollectActivity.class);
						startActivityForResult(intent, 10);
					}
				});
	}

	private boolean checkPreviewDeviceIsInDevicesCollect(
			PreviewDeviceItem previewDeviceItem, DeviceItem delDeviceItem) {
		boolean isContain = false;
		String prePlatFormUserName = previewDeviceItem.getPlatformUsername();
		String deviceName = previewDeviceItem.getDeviceRecordName();
		String devPlatFormUserName = delDeviceItem.getPlatformUsername();
		String ddeviceName = delDeviceItem.getDeviceName();

		if (ddeviceName.equals(deviceName)
				&& (prePlatFormUserName.equals(devPlatFormUserName))) {
			isContain = true;
		}
		return isContain;
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_device_management));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);

		previewDeviceItems = GlobalApplication.getInstance().getRealplayActivity().getPreviewDevices();

		mListView = (ListView) findViewById(R.id.device_listview);
		navigation_bar_add_btn = (Button) findViewById(R.id.base_navigationbar_right_btn);

		context = DeviceViewActivity.this;
		loadDeviceDatas();
		showDialog(LOAD_COLLECT_DEVICEITEM);
	}

	private void loadDeviceDatas() {
		task = new LoadCollectDeviceItemsTask();
		task.execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADD) {
			if (resultCode == SEMI_AUTO_ADD) { // 从手动添加设备界面返回
				if (data != null) {
					Bundle bundle = data.getExtras();
					if (bundle != null) {
						DeviceItem svDevItem = (DeviceItem) bundle.getSerializable("saveDeviceItem");
						String usernmae = getString(R.string.device_manager_collect_device);
						svDevItem.setPlatformUsername(usernmae);
						boolean result = checkContainDeviceItem(svDevItem,deviceItemList); // 检测列表中是否包含该DeviceItem
						if (!result) {
							deviceItemList.add(svDevItem);
						} else {
							int index = bundle.getInt("index");
							deviceItemList.set(index, svDevItem);
						}
						dLAdapter.notifyDataSetChanged();
					}
				}
			} else {
				// 进行文档更新，从文档中读取元素
				try {
					deviceItemList = ReadWriteXmlUtils.getCollectDeviceListFromXML(ChannelListActivity.filePath);
					dLAdapter = new DeviceListAdapter(this, deviceItemList);
					mListView.setAdapter(dLAdapter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (requestCode == EDIT) {// 从查看/编辑设备界面返回后...
			if (data != null) {
				SharedPreferences spf = getSharedPreferences("user",Context.MODE_PRIVATE);
				String dName = spf.getString("dName", "defaultValue");
				String lUser = spf.getString("lUser", "defaultValue");
				String lPass = spf.getString("lPass", "defaultValue");
				String chSum = spf.getString("chSum", "defaultValue");

				String dfChl = spf.getString("dfChl", "defaultValue");
				int defaultChannel = Integer.valueOf(dfChl);
				String svrIp = spf.getString("svrIp", "defaultValue");
				String svrPt = spf.getString("svrPt", "defaultValue");

				clickDeviceItem.setDeviceName(dName);
				clickDeviceItem.setLoginUser(lUser);
				clickDeviceItem.setLoginPass(lPass);
				clickDeviceItem.setChannelSum(chSum);

				clickDeviceItem.setDefaultChannel(defaultChannel);
				clickDeviceItem.setSvrIp(svrIp);
				clickDeviceItem.setSvrPort(svrPt);

				deviceItemList.set(clickPosition, clickDeviceItem);
				dLAdapter.notifyDataSetChanged();
			}
		}
	}

	protected boolean checkDefValueOfSpf(String dName, String chSum,
			String dChnl, String svrIp, String lgUsr, String lgPas, String svrPt) {// 检测是否存在defValue
		boolean isDefValue = false;
		if (dName.equals("defValue") || chSum.equals("defValue")
				|| dChnl.equals("defValue") || svrIp.equals("defValue")
				|| lgUsr.equals("defValue") || lgPas.equals("defValue")
				|| svrPt.equals("defValue")) {
			isDefValue = true;
		}
		return isDefValue;
	}

	private boolean checkContainDeviceItem(DeviceItem saveDeviceItem,
			List<DeviceItem> deviceItemList) {// 检测是否已经包含saveDeviceItem
		boolean result = false;
		int size = deviceItemList.size();
		for (int i = 0; i < size; i++) {
			DeviceItem deviceItem = deviceItemList.get(i);

			String dName = deviceItem.getDeviceName();
			String svrIp = deviceItem.getSvrIp();
			String sPort = deviceItem.getSvrPort();
			String lUser = deviceItem.getLoginUser();
			String lPass = deviceItem.getLoginPass();

			String saveLPass = saveDeviceItem.getLoginPass();
			String saveLUser = saveDeviceItem.getLoginUser();
			String saveSvrIp = saveDeviceItem.getSvrIp();
			String saveSPort = saveDeviceItem.getSvrPort();
			String saveDName = saveDeviceItem.getDeviceName();

			if ((dName.equals(saveDName) || (dName == saveDName))
					&& (sPort.equals(saveSPort) || (sPort == saveSPort))
					&& (svrIp.equals(saveSvrIp) || (svrIp == saveSvrIp))
					&& (lUser.equals(saveLUser) || (lUser == saveLUser))
					&& (lPass.equals(saveLPass) || (lPass == saveLPass))) {
				result = true;
				break;
			}
		}
		return result;
	}

	public class LoadCollectDeviceItemsTask extends
			AsyncTask<Void, Void, List<DeviceItem>> {

		public LoadCollectDeviceItemsTask() {

		}

		@Override
		protected List<DeviceItem> doInBackground(Void... params) {
			deviceItemList = new ArrayList<DeviceItem>();
			try {
				deviceItemList = ReadWriteXmlUtils
						.getCollectDeviceListFromXML(ChannelListActivity.filePath);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return deviceItemList;
		}

		@Override
		protected void onPostExecute(List<DeviceItem> result) {
			super.onPostExecute(result);
			dismissLoadDataDialog();
			dLAdapter = new DeviceListAdapter(DeviceViewActivity.this,
					deviceItemList);
			mListView.setAdapter(dLAdapter);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOAD_COLLECT_DEVICEITEM:
			String text = getString(R.string.system_setting_loading_collect_device_data);
			loadDataPrg = ProgressDialog.show(this, "", text, true, true);
			loadDataPrg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// loadDataPrg.dismiss();
					// if (!task.isCancelled()) {
					// task.cancel(true);
					// }
				}
			});
			return loadDataPrg;
		default:
			return null;
		}
	}

	private void dismissLoadDataDialog() {
		if (loadDataPrg != null && loadDataPrg.isShowing()) {
			loadDataPrg.dismiss();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		dismissLoadDataDialog();
		if (!task.isCancelled()) {
			task.cancel(true);
		}
	}
}