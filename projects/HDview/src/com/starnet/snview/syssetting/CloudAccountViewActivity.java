package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class CloudAccountViewActivity extends BaseActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "CloudAccountViewActivity";
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	private ListView mNetWorkSettingList;
	private CloudAccountAdapter caAdapter;

	private List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
	private int pos;
	private String titleName;
	private CloudAccount deleteCA;
	private CloudAccount beforeEditCA;

	private Button user_save_btn;// 账号添加按钮

	private List<PreviewDeviceItem> previewDeviceItems;
	private List<PreviewDeviceItem> editPreviewDeviceItems;
	private List<PreviewDeviceItem> delPreviewDeviceItems = new LinkedList<PreviewDeviceItem>();
	private List<PreviewDeviceItem> delEditPreviewDeviceItems = new LinkedList<PreviewDeviceItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_activity);

		initView();
		setListeners();

		mNetWorkSettingList.setOnItemClickListener(new OnItemClickListener() {// 进入用户编辑界面？？？？？更改原来的信息；

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						pos = position;
						beforeEditCA = cloudAccountList.get(position);
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString("clickPostion", "" + pos);
						bundle.putSerializable("cloudAccount", beforeEditCA);
						intent.putExtras(bundle);
						intent.setClass(CloudAccountViewActivity.this,
								CloudAccountUpdateActivity.class);
						startActivityForResult(intent, 20);
					}
				});

		mNetWorkSettingList
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						// 弹出删除对话框。。。
						deleteCA = (CloudAccount) parent
								.getItemAtPosition(position);
						titleName = deleteCA.getUsername();
						Builder builder = new Builder(
								CloudAccountViewActivity.this);
						builder.setTitle(getString(R.string.system_setting_cloudaccountview_delete_user)
								+ " " + titleName + " ?");
						builder.setPositiveButton(
								getString(R.string.system_setting_cloudaccountview_ok),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										int previewDeviceSize = previewDeviceItems
												.size();
										for (int i = 0; i < previewDeviceSize; i++) {
											PreviewDeviceItem previewDeviceItem = previewDeviceItems
													.get(i);
											boolean isFrom = checkPreviewDeviceItemFromDelCA(
													previewDeviceItem, deleteCA);
											if (isFrom) {
												delPreviewDeviceItems
														.add(previewDeviceItem);
											}
										}

										int delSize = delPreviewDeviceItems
												.size();
										for (int i = 0; i < delSize; i++) {
											previewDeviceItems
													.remove(delPreviewDeviceItems
															.get(i));
										}
										if (delSize > 0) {
											GlobalApplication
													.getInstance()
													.getRealplayActivity()
													.notifyPreviewDevicesContentChanged();
										}
										ReadWriteXmlUtils
												.removeCloudAccoutFromXML(
														filePath, deleteCA);
										cloudAccountList.remove(deleteCA);
										caAdapter.notifyDataSetChanged();

										if (deleteCA.isEnabled()) {// 删除tag
											List<String> del_tags = new ArrayList<String>();
											del_tags.add(deleteCA.getUsername()
													+ "_"
													+ deleteCA.getPassword());
											PushManager
													.delTags(
															CloudAccountViewActivity.this,
															del_tags);
										}

									}
								});
						builder.setNegativeButton(
								getString(R.string.system_setting_cloudaccountview_cancel),
								null);
						builder.show();
						return true;
					}
				});
	}

	private boolean checkPreviewDeviceItemFromDelCA(
			PreviewDeviceItem previewDeviceItem, CloudAccount deleteCA) {
		boolean isFrom = false;
		String platformUsername = previewDeviceItem.getPlatformUsername();
		String userName = deleteCA.getUsername();
		if (platformUsername != null) {
			if (platformUsername.equals(userName)) {
				isFrom = true;
			}
		}
		return isFrom;
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.system_setting_newworksetting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);

		user_save_btn = super.getRightButton();
		mNetWorkSettingList = (ListView) findViewById(R.id.cloudaccount_listview);

		previewDeviceItems = GlobalApplication.getInstance()
				.getRealplayActivity().getPreviewDevices();

		try {
			cloudAccountList = ReadWriteXmlUtils.getCloudAccountList(filePath);
			caAdapter = new CloudAccountAdapter(this, cloudAccountList);
			mNetWorkSettingList.setAdapter(caAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountViewActivity.this.finish();
			}
		});

		user_save_btn.setOnClickListener(new OnClickListener() {// 进入用户增加界面？？？？？增加一个账户信息；

					@Override
					public void onClick(View v) {
						gotoCloudAccountAddding();
					}
				});

	}

	protected void gotoCloudAccountAddding() {
		Intent intent = new Intent();
		intent.setClass(CloudAccountViewActivity.this,
				CloudAccountAddingActivity.class);
		startActivityForResult(intent, 10);
	}

	// 保存以后，回到该函数中，通知listView的变化
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 20) {// 从编辑界面返回
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					CloudAccount afterEditCA = (CloudAccount) bundle.getSerializable("edit_cloudAccount");
					cloudAccountList.set(pos, afterEditCA);
					caAdapter.notifyDataSetChanged();
					try {
						Context ctx = CloudAccountViewActivity.this;
						boolean isChanged = checkCloudAccountChange(afterEditCA, beforeEditCA); // 检测用户信息是否改变
						if (!isChanged) {// 用户信息更改
							changeNoUseState();
							if (beforeEditCA.isEnabled()&& !afterEditCA.isEnabled()) {
								if (NetWorkUtils.checkNetConnection(ctx)) {
									delTags();
								}
							}else if (beforeEditCA.isEnabled() && afterEditCA.isEnabled()) {
								if (NetWorkUtils.checkNetConnection(ctx)) {
									registerTags(afterEditCA);
									delTags();
								}
							}
						} else { // 如果用户信息未改变的话
							if (beforeEditCA.isEnabled()&& !afterEditCA.isEnabled()) {//删除注册标签
								changeNoUseState();
								if (NetWorkUtils.checkNetConnection(ctx)) {
									delTags();
								}
							}else if (!beforeEditCA.isEnabled()&& afterEditCA.isEnabled()) {//注册账户标签
								if (NetWorkUtils.checkNetConnection(ctx)) {
									registerTags(afterEditCA);
								}
							}
						}
					} catch (Exception e) {// MD5加密异常的处理
						e.printStackTrace();
					}
				}
			}
		} else if (requestCode == 10) {
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					CloudAccount cloudAccount = (CloudAccount) bundle
							.getSerializable("cloudAccount");
					boolean result = judgeListContainCloudAccount(cloudAccount,
							cloudAccountList);
					if (!result) {
						cloudAccountList.add(cloudAccount);
						caAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	}
	
	/**注册新的账户标签***/
	private void registerTags(CloudAccount afterEditCA) throws Exception{
		List<String> reg_tags = new ArrayList<String>();
		reg_tags.add(afterEditCA.getUsername()+ MD5Utils.createMD5(afterEditCA.getPassword()));
		PushManager.setTags(CloudAccountViewActivity.this,reg_tags);
	}
	
	/**删除旧的账户标签***/
	private void delTags() throws Exception{
		List<String> del_tags = new ArrayList<String>();
		del_tags.add(beforeEditCA.getUsername()+ MD5Utils.createMD5(beforeEditCA.getPassword()));
		PushManager.delTags(CloudAccountViewActivity.this,del_tags);
	}

	/**通知预览通道禁用***/
	private void changeNoUseState() {
		editPreviewDeviceItems = GlobalApplication.getInstance()
				.getRealplayActivity().getPreviewDevices();
		int previewSize = editPreviewDeviceItems.size();
		for (int i = 0; i < previewSize; i++) {
			PreviewDeviceItem delItem = editPreviewDeviceItems.get(i);
			boolean isFrom = checkPreviewDeviceItemFromDelCA(delItem,
					beforeEditCA);
			if (isFrom) {
				delEditPreviewDeviceItems.add(delItem);
			}
		}

		int delEditSize = delEditPreviewDeviceItems.size();
		for (int i = 0; i < delEditSize; i++) {
			editPreviewDeviceItems.remove(delEditPreviewDeviceItems.get(i));
		}

		if (delEditSize > 0) {
			// if (editPreviewDeviceItems.size() > 0) {
			GlobalApplication.getInstance().getRealplayActivity()
					.notifyPreviewDevicesContentChanged();
			// }
		}
	}

	/** 用户信息更改时，返回的是false；否则返回的是true ***/
	private boolean checkCloudAccountChange(CloudAccount afterEditCA,
			CloudAccount beforeEditCA2) {
		boolean isChanged = false;
		String befDomain = beforeEditCA2.getDomain();
		String beforPort = beforeEditCA2.getPort();
		String befUsname = beforeEditCA2.getUsername();

		String aftDomain = afterEditCA.getDomain();
		String afterPort = afterEditCA.getPort();
		String aftUsname = afterEditCA.getUsername();

		if (befDomain.equals(aftDomain) && beforPort.equals(afterPort)
				&& befUsname.equals(aftUsname)) {
			isChanged = true;
		}

		return isChanged;
	}

	private boolean judgeListContainCloudAccount(CloudAccount cloudAccount,
			List<CloudAccount> cloudAccountList2) {
		boolean result = false;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cA = cloudAccountList2.get(i);
			String cADomain = cA.getDomain();
			String cAPort = cA.getPort();
			String cAUsername = cA.getUsername();
			String cAPassword = cA.getPassword();
			if (cloudAccount.getUsername().equals(cAUsername)
					&& cloudAccount.getDomain().equals(cADomain)
					&& cloudAccount.getPassword().equals(cAPassword)
					&& cloudAccount.getPort().equals(cAPort)) {
				result = true;
				break;
			}
		}
		return result;
	}
}