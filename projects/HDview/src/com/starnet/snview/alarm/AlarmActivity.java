package com.starnet.snview.alarm;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.pushutils.AlarmDeviceInfo;
import com.baidu.pushutils.AlarmInfoAdapter;
import com.baidu.pushutils.Utils;
import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class AlarmActivity extends BaseActivity {

	private Button navBackBtn;
	private ListView alarmInfoListView;
	private AlarmInfoAdapter listviewAdapter;
	private List<AlarmDeviceInfo> alarmInfoList;
	private Context mContext;

	private TextView titleView;// 报警
	
	Resources resource;
	String pkgName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_layout_act);

		initUi();

		setOnClickListersForWadget();

//		pushServiceOperation();// 百度的推送服务操作...
	}

	protected void pushServiceOperation() {

		Utils.logStringCache = Utils.getLogText(getApplicationContext());
		resource = this.getResources();
		pkgName = this.getPackageName();

		PushManager.startWork(getApplicationContext(),PushConstants.LOGIN_TYPE_API_KEY,Utils.getMetaValue(AlarmActivity.this, "api_key"));
		
		//通知栏的自定义样式
		int noti_custom = resource.getIdentifier("notification_custom_builder", "layout", pkgName);
		int icon = resource.getIdentifier("notification_icon", "id", pkgName);
		int title = resource.getIdentifier("notification_title", "id", pkgName);
		int text = resource.getIdentifier("notification_text", "id", pkgName);
		CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(getApplicationContext(),noti_custom,icon,title,text);
		cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
		cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
		cBuilder.setLayoutDrawable(resource.getIdentifier("simple_notification_icon", "drawable", pkgName));
		PushManager.setNotificationBuilder(this, 1, cBuilder);
		//通知栏的自定义样式
		
		// 设置标签
		List<String> tags = new ArrayList<String>();
		tags.add("hongxubaidu");// 模拟标签....
		PushManager.setTags(getApplicationContext(), tags);

	}

//    @Override
//    protected void onNewIntent(Intent intent) {
//        String action = intent.getAction();
//        if (Utils.ACTION_LOGIN.equals(action)) {
//            // Push: 百度账号初始化，用access token绑定
//            String accessToken = intent.getStringExtra(Utils.EXTRA_ACCESS_TOKEN);
//            PushManager.startWork(getApplicationContext(),PushConstants.LOGIN_TYPE_ACCESS_TOKEN, accessToken);
//        }
//    }
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Utils.setLogText(getApplicationContext(), Utils.logStringCache);
		super.onDestroy();
	}

	private void setOnClickListersForWadget() {
		// navBackBtn.setOnClickListener(this);

		alarmInfoListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				AlarmDeviceInfo selectDevice = alarmInfoList.get(position); // 单击的报警设备
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
		alarmInfoListView = (ListView) findViewById(R.id.alarm_info_listview);
		navBackBtn.setVisibility(View.GONE);
		// navBackBtn.setBackgroundResource(R.drawable.alarm_right_back);
		
		alarmInfoList = new ArrayList<AlarmDeviceInfo>();// 模拟数据

		AlarmDeviceInfo alarmDevice1 = new AlarmDeviceInfo();
		AlarmDeviceInfo alarmDevice2 = new AlarmDeviceInfo();
		AlarmDeviceInfo alarmDevice3 = new AlarmDeviceInfo();
		AlarmDeviceInfo alarmDevice4 = new AlarmDeviceInfo();
		AlarmDeviceInfo alarmDevice5 = new AlarmDeviceInfo();
		AlarmDeviceInfo alarmDevice6 = new AlarmDeviceInfo();

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
		
		listviewAdapter = new AlarmInfoAdapter(alarmInfoList, this);
		alarmInfoListView.setAdapter(listviewAdapter);

	}
}