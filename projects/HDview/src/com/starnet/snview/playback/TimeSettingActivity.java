package com.starnet.snview.playback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.wheelview.widget.NumericWheelAdapter;
import com.starnet.snview.component.wheelview.widget.OnWheelScrollListener;
import com.starnet.snview.component.wheelview.widget.WheelView;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint({ "SdCardPath", "SimpleDateFormat" })
public class TimeSettingActivity extends BaseActivity {

	private int dayNum;
	private int curyear;
	private int curMonth;
	private int curDays;
	private int poor;

	private Context ctx;
	private WheelView day;
	private WheelView hour;
	private WheelView year;
	private WheelView month;
	private WheelView minute;
	private View endtimeView;
	private ImageButton imgBtn;
	private View starttimeView;
	private TextView endtimeTxt;
	private TextView startTimeTxt;
	private PopupWindow popupWindow;
	private NumericWheelAdapter dayAdapter;
	private NumericWheelAdapter yearAdapter;
	private NumericWheelAdapter hourAdapter;
	private NumericWheelAdapter monthAdapter;
	private NumericWheelAdapter minuteAdapter;

	private boolean endFlag = false;
	private boolean startFlag = false;
	private ExpandableListView cloudAccountView;
	private AccountsPlayBackExpanableAdapter actsAdapter;
	private final String filePath = "/data/data/com.starnet.snview/cloudAccount_list.xml";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playback_time_setting_activity);
		initViews();
		setExtPandableListview();
		setListenersForWadgets();
	}

	/** 为用户添加设备数据 **/
	private void setExtPandableListview() {
		List<CloudAccount> accounts = getCloudAccounts();
		List<CloudAccount> users = testData();
		actsAdapter = new AccountsPlayBackExpanableAdapter(ctx, users);
		cloudAccountView.setAdapter(actsAdapter);
	}

	private List<CloudAccount> getCloudAccounts() {
		try {
			return ReadWriteXmlUtils.getCloudAccountList(filePath);
		} catch (Exception e) {
			return null;
		}
	}

	/** 测试假数据 **/
	private List<CloudAccount> testData() {
		List<CloudAccount> users = new ArrayList<CloudAccount>();
		List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		CloudAccount caAccount = new CloudAccount();
		caAccount.setUsername("jtpt");
		DeviceItem deviceItem = new DeviceItem();
		deviceItem.setDeviceName("天津平台");
		DeviceItem deviceItem2 = new DeviceItem();
		deviceItem2.setDeviceName("福州平台");
		deviceList.add(deviceItem);
		deviceList.add(deviceItem2);
		caAccount.setDeviceList(deviceList);
		users.add(caAccount);
		return users;
	}

	private void setListenersForWadgets() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TimeSettingActivity.this.finish();
			}
		});

		cloudAccountView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				if (RealPlaybackStateUtils.exapandFlag) {
					RealPlaybackStateUtils.exapandFlag = false;
				} else {
					RealPlaybackStateUtils.exapandFlag = true;
				}
				return false;
			}
		});

		starttimeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFlag = true;
				endFlag = false;
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				} else {
					popupWindow.showAsDropDown(v);
					popupWindow.setFocusable(false);
					popupWindow.setOutsideTouchable(true);
					popupWindow.update();
				}
			}
		});

		endtimeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				endFlag = true;
				startFlag = false;
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				} else {
					popupWindow.showAsDropDown(v);
					popupWindow.setFocusable(false);
					popupWindow.setOutsideTouchable(true);
					popupWindow.update();
				}
			}
		});
		imgBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startPlayBack();
			}
		});
	}

	/** 开始进行回放操作 **/
	private void startPlayBack() {

	}

	private void initViews() {
		super.setToolbarVisiable(false);
		super.getRightButton().setVisibility(View.GONE);
		super.getExtendButton().setVisibility(View.GONE);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.navigation_title_remote_playback));
		ctx = TimeSettingActivity.this;
		endtimeTxt = (TextView) findViewById(R.id.endtime);
		imgBtn = (ImageButton) findViewById(R.id.startScan);
		startTimeTxt = (TextView) findViewById(R.id.starttime);
		endtimeView = findViewById(R.id.input_endtime_view);
		starttimeView = findViewById(R.id.input_starttime_view);
		cloudAccountView = (ExpandableListView) findViewById(R.id.cloudaccountExtListview);
		cloudAccountView.setGroupIndicator(null);

		setCurrentTimeForTxt();

		initPopupWindow();
	}

	private void setCurrentTimeForTxt() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateNowStr = sdf.format(d);
		endtimeTxt.setText(dateNowStr);
		startTimeTxt.setText(dateNowStr);
	}

	private void initPopupWindow() {
		LayoutInflater inflater = LayoutInflater.from(ctx);
		View view = inflater.inflate(R.layout.time_dialog, null);
		popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		View view2 = popupWindow.getContentView();

		year = (WheelView) view2.findViewById(R.id.year);
		month = (WheelView) view2.findViewById(R.id.month);
		day = (WheelView) view2.findViewById(R.id.day);
		hour = (WheelView) view2.findViewById(R.id.hour);
		minute = (WheelView) view2.findViewById(R.id.minute);
		popupWindow.setAnimationStyle(R.style.PopupAnimation);
		setWheelView();

	}

	private void setWheelView() {
		Calendar c1 = Calendar.getInstance();
		Calendar c = Calendar.getInstance();
		curyear = c.get(Calendar.YEAR);
		curMonth = c.get(Calendar.MONTH);
		curDays = c.get(Calendar.DAY_OF_MONTH);
		poor = curyear - 10;

		yearAdapter = new NumericWheelAdapter(2000, c1.get(Calendar.YEAR));
		year.setAdapter(yearAdapter);
		year.setLabel(null);
		year.setCyclic(true);
		year.setCurrentItem(curyear);
		curMonth += 1;

		monthAdapter = new NumericWheelAdapter(1, 12, "%02d");
		month.setAdapter(monthAdapter);
		month.setLabel(null);
		month.setCyclic(true);

		dayNum = setwheelDay(curyear, curMonth);
		dayAdapter = new NumericWheelAdapter(1, dayNum, "%02d");
		day.setAdapter(dayAdapter);
		day.setLabel(null);
		day.setCyclic(true);

		hourAdapter = new NumericWheelAdapter(0, 23, "%02d");
		hour.setAdapter(hourAdapter);
		hour.setLabel(null);
		hour.setCyclic(true);

		minuteAdapter = new NumericWheelAdapter(0, 59, "%02d");
		minute.setAdapter(minuteAdapter);
		// minute.setLabel("分");
		minute.setLabel(null);
		minute.setCyclic(true);

		year.setCurrentItem(curyear + 10);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDays - 1);

		OnWheelScrollListener scrollListener = new OnWheelScrollListener() {

			@Override
			public void onScrollingStarted(WheelView wheel) {

			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				if (wheel.getId() == R.id.year) {
					dayNum = setwheelDay(year.getCurrentItem() + poor,
							month.getCurrentItem() + 1);
					day.setAdapter(new NumericWheelAdapter(1, dayNum, "%02d"));
				} else if (wheel.getId() == R.id.month) {
					dayNum = setwheelDay(year.getCurrentItem() + poor,
							month.getCurrentItem() + 1);
					day.setAdapter(new NumericWheelAdapter(1, dayNum, "%02d"));
				} else if (wheel.getId() == R.id.day) {
					curDays = day.getCurrentItem();
				}

				int newday = day.getCurrentItem();
				int curYear = year.getCurrentItem();
				int newmon = month.getCurrentItem();
				int newhour = hour.getCurrentItem();
				int newmin = minute.getCurrentItem();

				String dayContent = dayAdapter.getItem(newday);
				String hourContent = hourAdapter.getItem(newhour);
				String yearContent = yearAdapter.getItem(curYear);
				String monthContent = monthAdapter.getItem(newmon);
				String minuteContent = minuteAdapter.getItem(newmin);

				String selectTime = yearContent + "-" + monthContent + "-"
						+ dayContent + "  " + hourContent + ":" + minuteContent;
				if (startFlag) {
					startTimeTxt.setText(selectTime);
				} else if (endFlag) {
					endtimeTxt.setText(selectTime);
				}
			}
		};

		year.addScrollingListener(scrollListener);
		month.addScrollingListener(scrollListener);
		day.addScrollingListener(scrollListener);
		hour.addScrollingListener(scrollListener);
		minute.addScrollingListener(scrollListener);
	}

	private int setwheelDay(int year, int month) {
		int day = 31;
		if (month == 2) {
			if ((year % 4 == 0) && ((year % 100 != 0) | (year % 400 == 0))) {
				day = 29;
			} else {
				day = 28;
			}
		}
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			day = 30;
		}
		return day;
	}

	/** 检测时间是否超过三天，如果超过，则返回false；否则，返回true **/
	private boolean examineTimeExt3() {
		boolean isExt = false;
		String endTime = (String) endtimeTxt.getText();
		String startTime = (String) startTimeTxt.getText();
		int[] endData = getValidateTime(endTime);
		int[] startData = getValidateTime(startTime);
		isExt = getBooleanBtwTime(startData, endData);
		return isExt;
	}

	/** 比较开始时间和结束时间 **/
	private boolean getBooleanBtwTime(int[] startData, int[] endData) {
		boolean isExt = false;

		int endDay = endData[0] * 365 + endData[1] * 30 + endData[2];
		int startDay = startData[0] * 365 + startData[1] * 30 + startData[2];

		int endSeconds = endData[3] * 3600 + endData[4] * 60;
		int startSeconds = startData[3] * 3600 + startData[4] * 60;

		return isExt;
	}

	private int[] getValidateTime(String endTime) {
		int timeData[] = new int[5];
		String[] data = endTime.split("-");
		int year = Integer.valueOf(data[0]);
		int month = Integer.valueOf(data[1]);
		int day = Integer.valueOf(data[2]);

		String[] hourMinutes = endTime.split("  ");
		String hourMinute = hourMinutes[1];

		String[] hm = hourMinute.split(":");

		int hour = Integer.valueOf(hm[0]);
		int mintute = Integer.valueOf(hm[1]);

		timeData[0] = year;
		timeData[1] = month;
		timeData[2] = day;
		timeData[3] = hour;
		timeData[4] = mintute;
		return timeData;
	}
}