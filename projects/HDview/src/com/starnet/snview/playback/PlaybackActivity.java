package com.starnet.snview.playback;

import java.util.ArrayList;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.playback.utils.PlayBackSearchRecordTask;
import com.starnet.snview.playback.utils.RecordInfo;
import com.starnet.snview.playback.utils.SearchRecordRequest;
import com.starnet.snview.protocol.message.OWSPDateTime;

public class PlaybackActivity extends BaseActivity {
	private static final String TAG = "PlaybackActivity";

	private Context ctx;
	private Toolbar mToolbar;

	private TimeBar mTimebar;
	private TimeBar.TimePickedCallBack mTimeBarCallBack;

	private final int TIMESETTING = 0x0007;
	private final int NOTIFYREMOTEUIFRESH_SUC = 0x0008;
	private final int NOTIFYREMOTEUIFRESH_FAIL = 0x0009;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NOTIFYREMOTEUIFRESH_SUC:
				Bundle data = msg.getData();
				ArrayList<RecordInfo> list = data
						.getParcelableArrayList("srres");
				setNewTimeBar(list);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.playback_activity);

		// setBackPressedExitEventValid(true);

		initView();

		setListenersForWadgets();

	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_remote_playback));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_search_btn_selector);

		ctx = PlaybackActivity.this;
		initToolbar();

		initTimebar();
	}

	public void setListenersForWadgets() {
		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ctx, TimeSettingActivity.class);
				startActivityForResult(intent, TIMESETTING);
			}
		});
	}

	/** 设置新的时间显示条 **/
	private void setNewTimeBar(ArrayList<RecordInfo> list) {
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				RecordInfo rcdInfo = list.get(i);
				OWSPDateTime starTime = rcdInfo.getStartTime();
				OWSPDateTime endTime = rcdInfo.getEndTime();
				showTimeBar(starTime, endTime);
			}
		}
	}

	/** 根据起始时间、结束时间进行时间显示条的渲染 **/
	private void showTimeBar(OWSPDateTime sTime, OWSPDateTime eTime) {

		Calendar startTime = Calendar.getInstance();
		startTime.set(sTime.getYear(), sTime.getMonth(), sTime.getDay(),
				sTime.getHour(), sTime.getMinute(), sTime.getSecond());

		Calendar endTime = Calendar.getInstance();
		endTime.set(eTime.getYear(), eTime.getMonth(), eTime.getDay(),
				eTime.getHour(), eTime.getMinute(), eTime.getSecond());

		mTimebar.addFileInfo(1, startTime, endTime);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initToolbar() {
		mToolbar = super.getBaseToolbar();

		ArrayList itemList = new ArrayList();
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PLAY_PAUSE,
				R.drawable.toolbar_play_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PICTURE,
				R.drawable.toolbar_take_picture_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.QUALITY,
				R.drawable.toolbar_quality_high_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PTZ,
				R.drawable.toolbar_ptz_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.MICROPHONE,
				R.drawable.toolbar_microphone_stop_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.SOUND,
				R.drawable.toolbar_sound_off_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.VIDEO_RECORD,
				R.drawable.toolbar_video_record_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.ALARM,
				R.drawable.toolbar_alarm_selector));

		mToolbar.createToolbar(itemList, GlobalApplication.getInstance()
				.getScreenWidth(),
				getResources().getDimensionPixelSize(R.dimen.toolbar_height));

		// this.mToolbar.setOnItemClickListener(mToolbarOnItemClickListener);
	}

	private void initTimebar() {
		mTimebar = (TimeBar) findViewById(R.id.timebar_control);

		mTimeBarCallBack = new TimeBar.TimePickedCallBack() {
			public void onTimePickedCallback(Calendar calendar) {

			}
		};

		Calendar c = Calendar.getInstance();

		Calendar c1 = Calendar.getInstance();
		c1.add(Calendar.MINUTE, 20);
		c1.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH),
				c1.get(Calendar.DAY_OF_MONTH), c1.get(Calendar.HOUR_OF_DAY),
				c1.get(Calendar.MINUTE));
		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.MINUTE, 50);
		c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH),
				c2.get(Calendar.DAY_OF_MONTH), c2.get(Calendar.HOUR_OF_DAY),
				c2.get(Calendar.MINUTE));
		mTimebar.addFileInfo(1, c1, c2);

		Calendar c3 = Calendar.getInstance();
		c3.add(Calendar.MINUTE, 70);
		c3.set(c3.get(Calendar.YEAR), c3.get(Calendar.MONTH),
				c3.get(Calendar.DAY_OF_MONTH), c3.get(Calendar.HOUR_OF_DAY),
				c3.get(Calendar.MINUTE));
		Calendar c4 = Calendar.getInstance();
		c4.add(Calendar.MINUTE, 110);
		c4.set(c4.get(Calendar.YEAR), c4.get(Calendar.MONTH),
				c4.get(Calendar.DAY_OF_MONTH), c4.get(Calendar.HOUR_OF_DAY),
				c4.get(Calendar.MINUTE));
		mTimebar.addFileInfo(1, c3, c4);

		Calendar c5 = Calendar.getInstance();
		c5.add(Calendar.MINUTE, 130);
		c5.set(c5.get(Calendar.YEAR), c5.get(Calendar.MONTH),
				c5.get(Calendar.DAY_OF_MONTH), c5.get(Calendar.HOUR_OF_DAY),
				c5.get(Calendar.MINUTE));
		Calendar c6 = Calendar.getInstance();
		c6.add(Calendar.MINUTE, 200);
		c6.set(c6.get(Calendar.YEAR), c6.get(Calendar.MONTH),
				c6.get(Calendar.DAY_OF_MONTH), c6.get(Calendar.HOUR_OF_DAY),
				c6.get(Calendar.MINUTE));
		mTimebar.addFileInfo(1, c5, c6);

		Calendar c7 = Calendar.getInstance();
		c7.add(Calendar.MINUTE, 220);
		c7.set(c7.get(Calendar.YEAR), c7.get(Calendar.MONTH),
				c7.get(Calendar.DAY_OF_MONTH), c7.get(Calendar.HOUR_OF_DAY),
				c7.get(Calendar.MINUTE));
		Calendar c8 = Calendar.getInstance();
		c8.add(Calendar.MINUTE, 260);
		c8.set(c8.get(Calendar.YEAR), c8.get(Calendar.MONTH),
				c8.get(Calendar.DAY_OF_MONTH), c8.get(Calendar.HOUR_OF_DAY),
				c8.get(Calendar.MINUTE));
		mTimebar.addFileInfo(1, c7, c8);
	}

	private PlayBackSearchRecordTask pbTask;
	private DeviceItem deviceItem;
	private SearchRecordRequest srr;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TIMESETTING) {
			if (data != null) {
				srr = (SearchRecordRequest) data.getSerializableExtra("srr");
				deviceItem = (DeviceItem) data.getSerializableExtra("visitDevItem");
				// LoginInfoRequest lr = new LoginInfoRequest();

				DeviceItem dItem = new DeviceItem();
				dItem.setSvrIp("192.168.87.10");
				dItem.setSvrPort("8080");
				dItem.setLoginUser("admin");
				dItem.setLoginPass("1");
				dItem.setDefaultChannel(1);
				dItem.setDeviceName("ewrte");

				OWSPDateTime startTime = new OWSPDateTime();
				startTime.setYear(2015 - 2009);
				startTime.setMonth(1);
				startTime.setDay(20);
				startTime.setHour(11);
				startTime.setMinute(10);
				startTime.setSecond(0);
				srr.setStartTime(startTime);

				OWSPDateTime endTime = new OWSPDateTime();
				endTime.setYear(2015 - 2009);
				endTime.setMonth(1);
				endTime.setHour(11);
				endTime.setMinute(10);
				endTime.setSecond(0);
				endTime.setDay(23);
				srr.setEndTime(endTime);
				srr.setChannel(0);

				pbTask = new PlayBackSearchRecordTask(mHandler, dItem, srr);
				// pbTask.setLoginReq(lr);
				pbTask.start();
				// pbTask.testParseResponsePacketFromSocket();
			}
		} else if (requestCode == NOTIFYREMOTEUIFRESH_SUC) {
			if (data != null) {
				ArrayList<RecordInfo> riList = data
						.getParcelableArrayListExtra("recordinfo");
			}
		}
	}
}
