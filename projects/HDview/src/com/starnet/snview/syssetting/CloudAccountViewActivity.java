package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;

@SuppressLint("SdCardPath")
public class CloudAccountViewActivity extends BaseActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "CloudAccountViewActivity";
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	private ListView mNetWorkSettingList;
	private CloudAccountAdapter caAdapter;

	private CloudAccountXML caXML;
	private List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
	private int pos;
	private String titleName;
	private CloudAccount deleteCA;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_activity);

		initView();
		setListeners();

		mNetWorkSettingList.setOnItemClickListener(new OnItemClickListener() {//进入用户编辑界面？？？？？更改原来的信息；

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				pos = position;
				CloudAccount cloudAccount = cloudAccountList.get(position);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("cloudAccount", cloudAccount);
				intent.putExtras(bundle);
				intent.setClass(CloudAccountViewActivity.this,CloudAccountSetEditActivity.class);
				startActivityForResult(intent,20);
			}
		});
		
		mNetWorkSettingList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
				//弹出删除对话框。。。
				deleteCA = (CloudAccount) parent.getItemAtPosition(position);
				titleName = deleteCA.getUsername();
				Builder builder = new Builder(CloudAccountViewActivity.this);
				builder.setTitle(getString(R.string.system_setting_delete_user)+titleName);
				builder.setPositiveButton(getString(R.string.channel_listview_ok),new DialogInterface.OnClickListener(){
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//列表中删除操作....
						cloudAccountList.remove(deleteCA);
						caAdapter.notifyDataSetChanged();
						//文件中删除操作....
						Thread thread = new Thread(){
							@Override
							public void run() {
								super.run();
								caXML = new CloudAccountXML();
								caXML.removeCloudAccoutFromXML(filePath, deleteCA);
							}
						};
						thread.start();
					}
				 });
				 builder.setNegativeButton(getString(R.string.channel_listview_cancel),null);
				builder.show();
				return false;
			}
		});
		
		
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_network_setting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);

		mNetWorkSettingList = (ListView) findViewById(R.id.cloudaccount_listview);

		try {
			caXML = new CloudAccountXML();
			cloudAccountList = caXML.getCloudAccountList(filePath);
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

		super.getRightButton().setOnClickListener(new OnClickListener() {//进入用户增加界面？？？？？增加一个账户信息；

			@Override
			public void onClick(View v) {
				gotoCloudAccountSetting();
			}
		});

	}

	protected void gotoCloudAccountSetting() {
		Intent intent = new Intent();
		intent.setClass(CloudAccountViewActivity.this,CloudAccountSettingActivity.class);
		startActivityForResult(intent, 10);
	}

	// 保存以后，回到该函数中，通知listView的变化；
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		caAdapter.notifyDataSetChanged();
		if(requestCode == 20){//从编辑界面返回
			if(data != null){
				Bundle bundle = data.getExtras();
				if(bundle != null){
					CloudAccount cloudAccount = (CloudAccount) bundle.getSerializable("ca");
					cloudAccountList.set(pos, cloudAccount);
					caAdapter.notifyDataSetChanged();
				}
			}
		}else if (requestCode == 10) {//从添加界面返回
			if(data != null){
				Bundle bundle = data.getExtras();
				if(bundle != null){
					CloudAccount cloudAccount = (CloudAccount) bundle.getSerializable("cloudAccount");
					boolean result = judgeListContainCloudAccount(cloudAccount,cloudAccountList);
					if (!result) {
						cloudAccountList.add(cloudAccount);
						caAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	}

	private boolean judgeListContainCloudAccount(CloudAccount cloudAccount,List<CloudAccount> cloudAccountList2) {
		boolean result = false;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cA = cloudAccountList2.get(i);
			String cADomain = cA.getDomain();
			String cAPort = cA.getPort();
			String cAUsername = cA.getUsername();
			String cAPassword = cA.getPassword();
			if (cloudAccount.getUsername().equals(cAUsername)&& cloudAccount.getDomain().equals(cADomain)
				&& cloudAccount.getPassword().equals(cAPassword)&& cloudAccount.getPort().equals(cAPort)) {
				result = true;
				break;
			}
		}
		return result;
	}
}