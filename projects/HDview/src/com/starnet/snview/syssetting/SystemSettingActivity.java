package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;

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
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {
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
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {
      			switch (index) {
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
				Toast.makeText(getApplicationContext(), "ButtonPressed", Toast.LENGTH_LONG).show();
			}
		});
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
