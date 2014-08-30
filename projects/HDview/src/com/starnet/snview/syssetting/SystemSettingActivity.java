package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class SystemSettingActivity extends BaseActivity {
	private static final String TAG = "SystemSettingActivity";

	private CornerListView firstCornerListView = null; 
	private CornerListView secondCornerListView = null; 
	private Button logoutBtn = null; 
	
	private List<HashMap<String, Object>> settingList;
	private HashMap<String, Object> map;
	private SystemSettingAdapter settingListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.system_setting_activity);
		
		initView();
	}
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting));
		super.hideExtendButton();
		super.hideRightButton();
		super.setToolbarVisiable(false);
		
		settingList = new ArrayList<HashMap<String, Object>>();
        map = new HashMap<String, Object>();
        map.put("text", getResources().getString(R.string.system_setting_newworksetting));
        settingList.add(map);
        
        settingListAdapter = new SystemSettingAdapter(this, settingList);
        firstCornerListView = (CornerListView)findViewById(R.id.first_setting_list); 
        firstCornerListView.setAdapter(settingListAdapter);          
        firstCornerListView.setOnItemClickListener(new OnItemClickListener() {
    @Override
	public void onItemClick(AdapterView<?> parent, View view,int index, long id) {
      		switch (index) {
				case 0:
					gotoCloudAccount();
					break;
				default:
					break;
				}
			}
        });
        
        settingList = new ArrayList<HashMap<String, Object>>();
        map = new HashMap<String, Object>();
        map.put("text", getResources().getString(R.string.system_setting_update));
        settingList.add(map);
        map = new HashMap<String, Object>();
        map.put("text", getResources().getString(R.string.system_setting_help));
        settingList.add(map);
        map = new HashMap<String, Object>();
        map.put("text", getResources().getString(R.string.system_setting_about));
        settingList.add(map);
        settingListAdapter = new SystemSettingAdapter(this, settingList);
        secondCornerListView = (CornerListView)findViewById(R.id.second_setting_list); 
        secondCornerListView.setAdapter(settingListAdapter);          
        secondCornerListView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View view,int index, long id) {
      			switch (index) {
      			case 0:
      				gotoUpdate();
      				break;
      			case 1:
      				gotoHelp();
//      				gotoChannelListActivity_copy();
      				break;
				case 2:
					gotoAbout();
					break;
				default:
					break;
				}
			}
        });
        
        logoutBtn = (Button)findViewById(R.id.logout_setting_btn); 
        logoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(), "ButtonPressed", Toast.LENGTH_LONG).show();
				showTips();
			}
		});
	}

	private void showTips(){
		Builder builder = new Builder(SystemSettingActivity.this);
		builder.setTitle(getString(R.string.system_setting_logout_ok));
		builder.setPositiveButton(getString(R.string.system_setting_logout_ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ActivityManager actMgr= (ActivityManager) getSystemService(ACTIVITY_SERVICE );
				actMgr.killBackgroundProcesses(getPackageName());
				android.os.Process.killProcess(android.os.Process.myPid());
				SystemSettingActivity.this.finish();
				System.exit(0);
////				activityMgr.restartPackage();
//				activityMgr.killBackgroundProcesses(getPackageName());
				
			}
		});
		builder.setNegativeButton(getString(R.string.system_setting_logout_cancel), null);
		
		builder.show();
//		AlertDialog alertDialog = 
	}
	
	protected void gotoUpdate(){//zk。进入 更新界面。。。
		Intent intent = new Intent();
		//。。。。
//        intent.setClass(SystemSettingActivity.this, AboutActivity.class); 
//        startActivity(intent);
	}
	
	protected void gotoHelp() {//zk。进入 帮助界面。。。
		Intent intent = new Intent();
		//。。。。。
	}
	
	protected void gotoCloudAccount() {
		Intent intent = new Intent();
        intent.setClass(SystemSettingActivity.this, CloudAccountViewActivity.class); 
        startActivity(intent);
	}
	
	protected void gotoAbout() {
		Intent intent = new Intent();
        intent.setClass(SystemSettingActivity.this, AboutActivity.class); 
        startActivity(intent);
	}
}
