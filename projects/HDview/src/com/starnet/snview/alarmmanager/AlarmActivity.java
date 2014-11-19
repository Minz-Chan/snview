package com.starnet.snview.alarmmanager;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class AlarmActivity extends BaseActivity {

	private Button navBackBtn;
	private ExpandableListView alarmInfoListView;
	private AlarmDeviceAdapter listviewAdapter;
	private List<AlarmDevice> alarmInfoList;
	private Context mContext;

	private TextView titleView;// 报警
	
	Resources resource;
	String pkgName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_manager_layout);

		initUi();

		setOnClickListersForWadget();
		
	}

	private void setOnClickListersForWadget() {
		
		alarmInfoListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				AlarmDevice selectDevice = alarmInfoList.get(position); // 单击的报警设备
				Toast.makeText(mContext, "单击了：" + selectDevice.getDeviceName(),Toast.LENGTH_SHORT).show();
			}
		});
		
		alarmInfoListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
				
				final int pos = position;
				Builder builder = new Builder(AlarmActivity.this);
				
				String alarm_info = getString(R.string.alarm_dialog_infor);
				String alarm_titl = getString(R.string.alarm_dialog_title);
				String deviceName = alarmInfoList.get(position).getDeviceName();
				String title = alarm_titl+" "+deviceName+" "+alarm_info+" ?";
				builder.setTitle(title);
				
				builder.setNegativeButton(getString(R.string.alarm_dialog_cancel), null);
				builder.setPositiveButton(getString(R.string.alarm_dialog_OK), new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						alarmInfoList.remove(pos);
						listviewAdapter.notifyDataSetChanged();
					}
				});
				
				builder.show();
				return true;
			}
			
		});
	}

	private void initUi() {
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		titleView = super.getTitleView();
		titleView.setText(getString(R.string.alarm_title));
		mContext = AlarmActivity.this;
		navBackBtn = (Button) findViewById(R.id.base_navigationbar_right_btn);
		alarmInfoListView = (ExpandableListView) findViewById(R.id.alarm_expandlistview);
		navBackBtn.setVisibility(View.GONE);
		// navBackBtn.setBackgroundResource(R.drawable.alarm_right_back);
		
		alarmInfoList = new ArrayList<AlarmDevice>();// 模拟数据

		AlarmDevice alarmDevice1 = new AlarmDevice();
		AlarmDevice alarmDevice2 = new AlarmDevice();
		AlarmDevice alarmDevice3 = new AlarmDevice();
		AlarmDevice alarmDevice4 = new AlarmDevice();
		AlarmDevice alarmDevice5 = new AlarmDevice();
		AlarmDevice alarmDevice6 = new AlarmDevice();

		alarmDevice1.setAlarm_time("2014-11-03 19:00");
		alarmDevice1.setDeviceName("乌鲁木齐平台");
		alarmDevice1.setAlarm_type("烟雾报警");

		alarmDevice2.setAlarm_time("2014-10-04 19:00");
		alarmDevice2.setDeviceName("哈尔滨平台");
		alarmDevice2.setAlarm_type("入侵报警");

		alarmDevice3.setAlarm_time("2014-10-04 19:00");
		alarmDevice3.setDeviceName("沈阳平台");
		alarmDevice3.setAlarm_type("入侵报警");

		alarmDevice4.setAlarm_time("2014-10-04 19:00");
		alarmDevice4.setDeviceName("天津平台");
		alarmDevice4.setAlarm_type("入侵报警");

		alarmDevice5.setAlarm_time("2014-10-04 19:00");
		alarmDevice5.setDeviceName("重庆平台");
		alarmDevice5.setAlarm_type("入侵报警");

		alarmDevice6.setAlarm_time("2014-10-04 19:00");
		alarmDevice6.setDeviceName("郑州平台");
		alarmDevice6.setAlarm_type("入侵报警");

		alarmInfoList.add(alarmDevice1);
		alarmInfoList.add(alarmDevice2);
		alarmInfoList.add(alarmDevice3);
		alarmInfoList.add(alarmDevice4);
		alarmInfoList.add(alarmDevice5);
		alarmInfoList.add(alarmDevice6);
		
		listviewAdapter = new AlarmDeviceAdapter(alarmInfoList, this);
		alarmInfoListView.setAdapter(listviewAdapter);

	}
}