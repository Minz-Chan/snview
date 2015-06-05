package com.video.hdview.syssetting;

import java.util.ArrayList;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.baidu.android.pushservice.PushManager;
import com.video.hdview.R;
import com.video.hdview.component.BaseActivity;
import com.video.hdview.global.GlobalApplication;
import com.video.hdview.realplay.PreviewDeviceItem;
import com.video.hdview.util.CommonUtils;
import com.video.hdview.util.MD5Utils;
import com.video.hdview.util.NetWorkUtils;
import com.video.hdview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class CloudAccountViewActivity extends BaseActivity {
	private final String filePath = "/data/data/com.video.hdview/star_cloudAccount.xml";
	private ListView mListView;
	private CloudAccountAdapter caAdapter;

	private List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
	private Context context;
	private int pos;
	private String titleName;
	private CloudAccount beforeEditCA;

	private List<PreviewDeviceItem> previewDeviceItems; // 预览通道
	private List<PreviewDeviceItem> deletePDeviceItems = new ArrayList<PreviewDeviceItem>(); // 预览通道
	
	public static final int UPDATINGUI = 0x0020;
	public static final int ADDINGUI = 0x0030;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_activity);
		initView();
		setListeners();
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.system_setting_newworksetting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		
		mListView = (ListView) findViewById(R.id.cloudaccount_listview);
		previewDeviceItems = GlobalApplication.getInstance().getRealplayActivity().getPreviewDevices();

		context = CloudAccountViewActivity.this;
		try {
			cloudAccountList = ReadWriteXmlUtils.getCloudAccountList(filePath);
			caAdapter = new CloudAccountAdapter(this, cloudAccountList);
			mListView.setAdapter(caAdapter);
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

		super.getRightButton().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						gotoCloudAccountAddding();
					}
				});
		mListView.setOnItemClickListener(new OnItemClickListener() {// 进入用户编辑界面？？？？？更改原来的信息；

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				pos = position;
				beforeEditCA = cloudAccountList.get(position);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("clickPostion", "" + pos);
				bundle.putSerializable("cloudAccount", beforeEditCA);
				intent.putExtras(bundle);
//				intent.setClass(CloudAccountViewActivity.this,CloudAccountUpdatingActivity.class);
				intent.setClass(CloudAccountViewActivity.this,CloudAccountInfoActivity.class);
				startActivityForResult(intent, UPDATINGUI);
			}
		});
		
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
					View view, int position, long id) {
				final CloudAccount account = (CloudAccount) parent.getItemAtPosition(position);
				titleName = account.getUsername();
				Builder builder = new Builder(CloudAccountViewActivity.this);
				builder.setTitle(getString(R.string.system_setting_cloudaccountview_delete_user) + " " + titleName + " ?");
				final int pos = position;
				builder.setPositiveButton(
						getString(R.string.system_setting_cloudaccountview_ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								changeNoUseState(account);										
//								ReadWriteXmlUtils.removeCloudAccoutFromXML(filePath, account);
								ReadWriteXmlUtils.removeCloudAccoutFromXML(pos, filePath);
								cloudAccountList.remove(account);
								caAdapter.notifyDataSetChanged();
								if (account.isEnabled()) {// 删除tagring
									try {
										Context ctx = CloudAccountViewActivity.this;
										List<String> dTags = new ArrayList<String>();
										String uNm = account.getUsername();
										String pswd = MD5Utils.createMD5(account.getPassword());
										dTags.add(uNm + pswd);
										PushManager.delTags(ctx, dTags);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						});
				builder.setNegativeButton(getString(R.string.system_setting_cloudaccountview_cancel),null);
				builder.show();
				return true;
			}
		});
	}

	protected void gotoCloudAccountAddding() {
		Intent intent = new Intent();
		intent.setClass(CloudAccountViewActivity.this,CloudAccountAddingActivity.class);
		startActivityForResult(intent, ADDINGUI);
	}

	// 保存以后，回到该函数中，通知listView的变化
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UPDATINGUI) {// 从编辑界面返回
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					try {
						CloudAccount afterEditCA = (CloudAccount) bundle.getSerializable("edit_cloudAccount");
						cloudAccountList.set(pos, afterEditCA);
						caAdapter.notifyDataSetChanged();
						boolean isChanged = checkCloudAccountChange(afterEditCA, beforeEditCA); // 检测用户信息是否改变
						if (!isChanged) {// 用户信息更改
							if (beforeEditCA.isEnabled()&& !afterEditCA.isEnabled()) {
								if (NetWorkUtils.checkNetConnection(context)) {
									CommonUtils.delTags(context, beforeEditCA);
								}
							} else if (beforeEditCA.isEnabled() && afterEditCA.isEnabled()) {
								if (NetWorkUtils.checkNetConnection(context)) {
									CommonUtils.setTags(context, afterEditCA);
									CommonUtils.delTags(context, beforeEditCA);
								}
							}
						} else { // 如果用户信息未改变的话
							if (beforeEditCA.isEnabled() && !afterEditCA.isEnabled()) {// 删除注册标签
								if (NetWorkUtils.checkNetConnection(context)) {
									CommonUtils.delTags(context, beforeEditCA);
								}
							} else if (!beforeEditCA.isEnabled()
									&& afterEditCA.isEnabled()) {// 注册账户标签
								if (NetWorkUtils.checkNetConnection(context)) {
									CommonUtils.setTags(context, afterEditCA);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}else if(requestCode == ADDINGUI){
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					CloudAccount cloudAccount = (CloudAccount) bundle.getSerializable("cloudAccount");
					cloudAccountList.add(cloudAccount);
					caAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	/** 通知预览通道禁用 ***/
	private void changeNoUseState(CloudAccount account) {
		if ((previewDeviceItems!=null)&&previewDeviceItems.size() > 0) {
			for (PreviewDeviceItem item : previewDeviceItems) {
				if (item.getPlatformUsername().equals(account.getUsername())) {
					deletePDeviceItems.add(item);
				}
			}
			for (int i = 0; i < deletePDeviceItems.size(); i++) {
				previewDeviceItems.remove(deletePDeviceItems.get(i));
			}
			if (deletePDeviceItems.size() > 0) {
				GlobalApplication.getInstance().getRealplayActivity().setPreviewDevices(previewDeviceItems);
				GlobalApplication.getInstance().getRealplayActivity().notifyPreviewDevicesContentChanged();
			}
		}
	}

	/** 用户信息更改时，返回的是false；否则返回的是true ***/
	private boolean checkCloudAccountChange(CloudAccount afterEditCA,CloudAccount beforeEditCA2) {
		boolean isChanged = false;
		String befDomain = beforeEditCA2.getDomain();
		String beforPort = beforeEditCA2.getPort();
		String befUsname = beforeEditCA2.getUsername();

		String aftDomain = afterEditCA.getDomain();
		String afterPort = afterEditCA.getPort();
		String aftUsname = afterEditCA.getUsername();

		if (befDomain.equals(aftDomain) && beforPort.equals(afterPort) && befUsname.equals(aftUsname)) {
			isChanged = true;
		}

		return isChanged;
	}
}