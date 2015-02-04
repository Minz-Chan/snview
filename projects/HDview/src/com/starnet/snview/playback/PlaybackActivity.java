package com.starnet.snview.playback;

import java.util.ArrayList;
import java.util.Calendar;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.component.Toolbar.ActionImageButton;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.playback.utils.PlaybackControllTask;
import com.starnet.snview.playback.utils.TLV_V_RecordInfo;
import com.starnet.snview.playback.utils.TLV_V_SearchRecordRequest;
import com.starnet.snview.protocol.message.OWSPDateTime;

public class PlaybackActivity extends BaseActivity {
	private static final String TAG = "PlaybackActivity";

	private Context ctx;
	private Toolbar mToolbar;

	private boolean isFirstIn = false;

	private TimeBar mTimebar;
	private TimeBar.TimePickedCallBack mTimeBarCallBack;

	private final int REQUESTCODE_DOG = 0x0005;
	private final int TIMESETTING = 0x0007;
	private final int NOTIFYREMOTEUIFRESH_SUC = 0x0008;
	private final int NOTIFYREMOTEUIFRESH_EXCEPTION = 0x0009;
	private final int NOTIFYREMOTEUIFRESH_TMOUT = 0x0006;

	private PlaybackControllTask pbcTask;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NOTIFYREMOTEUIFRESH_SUC:
				isFirstIn = false;
				dismissPrg();
				Bundle data = msg.getData();
				ArrayList<TLV_V_RecordInfo> list = data
						.getParcelableArrayList("srres");
				if (list == null) {
					String content = getString(R.string.playback_remote_record_null);
					showTostContent(content);
				}else {
					setNewTimeBar(list);
				}
				break;
			case NOTIFYREMOTEUIFRESH_EXCEPTION:
				isFirstIn = false;
				dismissPrg();
				String content = getString(R.string.playback_netvisit_fail);
				showTostContent(content);
				break;
			case NOTIFYREMOTEUIFRESH_TMOUT:
				isFirstIn = false;
				dismissPrg();
				showTostContent(getString(R.string.playback_netvisit_timeout));
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

	protected void dismissPrg() {
		if (prg != null && prg.isShowing()) {
			prg.dismiss();
		}
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_remote_playback));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_search_btn_selector);
		isFirstIn = true;
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
	private void setNewTimeBar(ArrayList<TLV_V_RecordInfo> list) {
		// mTimebar.reset();
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				TLV_V_RecordInfo rcdInfo = list.get(i);
				OWSPDateTime starTime = rcdInfo.getStartTime();
				OWSPDateTime endTime = rcdInfo.getEndTime();
				showTimeBar(starTime, endTime);
			}
		}
	}

	/** 根据起始时间、结束时间进行时间显示条的渲染 **/
	private void showTimeBar(OWSPDateTime sTime, OWSPDateTime eTime) {

		Calendar startTime = Calendar.getInstance();
		startTime.set(sTime.getYear(), sTime.getMonth() - 1, sTime.getDay(),
				sTime.getHour(), sTime.getMinute());// , sTime.getSecond()

		Calendar endTime = Calendar.getInstance();
		endTime.set(eTime.getYear(), eTime.getMonth() - 1, eTime.getDay(),
				eTime.getHour(), eTime.getMinute());// , eTime.getSecond()

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

		this.mToolbar.setOnItemClickListener(mToolbarOnItemClickListener);
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

		mTimebar.reset();
	}

	private Toolbar.OnItemClickListener mToolbarOnItemClickListener = new Toolbar.OnItemClickListener() {
		@Override
		public void onItemClick(ActionImageButton imgBtn) {
			switch (imgBtn.getItemData().getActionID()) {
			case PLAY_PAUSE:
				if (isFirstIn) {
					showTostContent("没有远程回放信息，请选择选择回放信息后，再进行播放与暂停");
				} else {
					String curTime = mTimebar.getCurrentTime();
					OWSPDateTime startTime = getOWSPDateTime(curTime);
					Log.i(TAG, "curTime:" + curTime);
					// ？？？需要判断是否是继续播放
					if (isPlaying) {// 如果正在进行播放,单击按钮进行暂停
						isPlaying = false;
						pause(startTime);
					} else {
						isPlaying = true;
						resume(startTime);
					}
				}
				break;
			case PICTURE:
				showTostContent("单击了拍照按钮");
				break;
			default:
				showTostContent("单击了其他按钮");
				break;
			}
		}
	};

	private static boolean isPlayed = false;// 是否已经播放过
	private static boolean isPlaying = false;// 是否是正在进行播放

	private void showTostContent(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}

	protected void resume(OWSPDateTime startTime) {
		srr = new TLV_V_SearchRecordRequest();
		OWSPDateTime stTime = new OWSPDateTime();
		stTime.setYear(2015 - 2009);
		stTime.setMonth(2);
		stTime.setDay(1);
		stTime.setHour(15);
		stTime.setMinute(44);
		stTime.setSecond(0);
		srr.setStartTime(stTime);

		OWSPDateTime endTime = new OWSPDateTime();
		endTime.setYear(2015 - 2009);
		endTime.setMonth(2);
		endTime.setDay(3);
		endTime.setHour(14);
		endTime.setMinute(2);
		endTime.setSecond(0);
		srr.setEndTime(endTime);

		srr.setCount(255);
		srr.setRecordType(8);
		srr.setDeviceId(0);
		srr.setChannel(1);
		srr.setStartTime(startTime);// 需要重新获取时间
		pbcTask.setSearchRecord(srr);
		pbcTask.resume();
	}

	protected void pause(OWSPDateTime startTime) {
		srr = new TLV_V_SearchRecordRequest();
		OWSPDateTime stTime = new OWSPDateTime();
		stTime.setYear(2015 - 2009);
		stTime.setMonth(1);
		stTime.setDay(28);
		stTime.setHour(15);
		stTime.setMinute(44);
		stTime.setSecond(0);
		srr.setStartTime(stTime);
		OWSPDateTime endTime = new OWSPDateTime();
		endTime.setYear(2015 - 2009);
		endTime.setMonth(1);
		endTime.setDay(29);
		endTime.setHour(15);
		endTime.setMinute(45);
		endTime.setSecond(0);
		srr.setEndTime(endTime);

		srr.setCount(255);
		srr.setRecordType(8);
		srr.setDeviceId(0);
		srr.setChannel(1);
		srr.setStartTime(startTime);// ？？？需要重新获取时间
		pbcTask.setSearchRecord(srr);
		pbcTask.pause();

	}

	private OWSPDateTime getOWSPDateTime(String time) {
		OWSPDateTime owspTime = new OWSPDateTime();
		String[] sumTime = time.split(" ");
		String ymdTemp = sumTime[0];
		String hmsTemp = sumTime[1];
		int[] ymd = getIntYMDData(ymdTemp);
		int[] hms = getIntHMSData(hmsTemp);
		owspTime.setYear(ymd[0] - 2009);
		owspTime.setMonth(ymd[1]);
		owspTime.setDay(ymd[2]);
		owspTime.setHour(hms[0]);
		owspTime.setMinute(hms[1]);
		owspTime.setSecond(hms[2]);
		return owspTime;
	}

	private int[] getIntYMDData(String ymdTemp) {
		int[] data = new int[3];
		String[] temp = ymdTemp.split("-");
		for (int i = 0; i < 3; i++) {
			data[i] = Integer.valueOf(temp[i]);
		}
		return data;
	}

	private int[] getIntHMSData(String ymdTemp) {
		int[] data = new int[3];
		String[] temp = ymdTemp.split(":");
		for (int i = 0; i < 3; i++) {
			data[i] = Integer.valueOf(temp[i]);
		}
		return data;
	}

	private TLV_V_SearchRecordRequest srr;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TIMESETTING) {
			// try {
			// startPlayTask(srr, null);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			if (data != null) {
				Bundle bundle = data.getExtras();
				
				String svrPort = bundle.getString("svrPort");
				String svrPass = bundle.getString("svrPass");
				String svrUser = bundle.getString("svrUser");
				String []svrIps = bundle.getStringArray("svrIps");
				String svrIp = getSvrIp(svrIps);
//				String svrIp = data.getStringExtra("svrIp");

				DeviceItem dItem = new DeviceItem();
				dItem.setSvrIp(svrIp);
				dItem.setSvrPort(svrPort);
				dItem.setLoginUser(svrUser);
				dItem.setLoginPass(svrPass);

				TLV_V_SearchRecordRequest srr = (TLV_V_SearchRecordRequest) bundle
						.getParcelable("srr");
				isPlayed = false;
				isFirstIn = false;
				startPlayTask(srr, dItem);
			}
		}
	}

	private String getSvrIp(String[] svrIps) {
		String svrIp = "";
		for (int i = 0; i < svrIps.length-1; i++) {
			svrIp += svrIps[i]+".";
		}
		svrIp = svrIp + svrIps[svrIps.length-1];
		return svrIp;
	}

	@SuppressWarnings("deprecation")
	private void startPlayTask(TLV_V_SearchRecordRequest srr, DeviceItem dItem) {
		// DeviceItem dItem = new DeviceItem();
		// dItem.setSvrIp("61.131.16.27");
		// // dItem.setSvrIp("192.168.87.10");
		// // dItem.setSvrPort("8080");
		// dItem.setSvrPort("9509");
		// dItem.setLoginUser("admin");
		// dItem.setLoginPass("");
		// dItem.setDefaultChannel(1);
		// dItem.setDeviceName("ewrte");
		//
		// srr = new TLV_V_SearchRecordRequest();
		// OWSPDateTime stTime = new OWSPDateTime();
		// stTime.setYear(2015 - 2009);
		// stTime.setMonth(2);
		// stTime.setDay(3);
		// stTime.setHour(14);
		// stTime.setMinute(4);
		// stTime.setSecond(1);
		// srr.setStartTime(stTime);
		//
		// OWSPDateTime endTime = new OWSPDateTime();
		// endTime.setYear(2015 - 2009);
		// endTime.setMonth(2);
		// endTime.setDay(3);
		// endTime.setHour(14);
		// endTime.setMinute(25);
		// endTime.setSecond(2);
		// srr.setEndTime(endTime);
		// //
		// srr.setCount(255);
		// srr.setRecordType(4);
		// srr.setDeviceId(0);
		// srr.setChannel(0);
		showDialog(REQUESTCODE_DOG);
		pbcTask = PlaybackControllTask.getInstance(ctx, mHandler, srr, dItem);
		pbcTask.start();
	}

	private ProgressDialog prg;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case REQUESTCODE_DOG:
			prg = new ProgressDialog(this);
			prg.setMessage(getString(R.string.playback_timesetting_reqinfo));
			prg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			prg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (prg != null && prg.isShowing()) {
						prg.dismiss();
						pbcTask.setCancel(true);
					}
				}
			});
			return prg;
		default:
			return null;
		}
	}
}