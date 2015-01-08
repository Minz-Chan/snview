package com.starnet.snview.playback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
	private View starttimeView;
	private TextView endtimeTxt;
	private TextView startTimeTxt;
	private PopupWindow popupWindow;
	private NumericWheelAdapter dayAdapter;
	private NumericWheelAdapter yearAdapter;
	private NumericWheelAdapter hourAdapter;
	private NumericWheelAdapter monthAdapter;
	private NumericWheelAdapter minuteAdapter;

	private Button startScanBtn;

	private boolean endFlag = false;
	private boolean startFlag = false;
	private ExpandableListView cloudAccountView;
	private AccountsPlayBackExpanableAdapter actsAdapter;
	private final int REQUESTCODE = 0x0001;
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
		DeviceItem deviceItem1 = new DeviceItem();
		deviceItem1.setDeviceName("天津平台");
		DeviceItem deviceItem2 = new DeviceItem();
		deviceItem2.setDeviceName("福州平台");
		DeviceItem deviceItem3 = new DeviceItem();
		deviceItem3.setDeviceName("南京平台");
		deviceList.add(deviceItem1);
		deviceList.add(deviceItem2);
		deviceList.add(deviceItem3);
		caAccount.setDeviceList(deviceList);
		users.add(caAccount);
		return users;
	}

	private void backAndLeftOperation() {
		dismissTimeDialog();
		PlaybackUtils.exapandFlag = false;
		TimeSettingActivity.this.finish();
	}

	private void setListenersForWadgets() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backAndLeftOperation();
			}
		});

		cloudAccountView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				if (PlaybackUtils.exapandFlag) {
					PlaybackUtils.exapandFlag = false;
				} else {
					PlaybackUtils.exapandFlag = true;
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
		startScanBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startPlayBack();
				PlaybackUtils.exapandFlag = false;
				TimeSettingActivity.this.finish();
			}
		});

		cloudAccountView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				showToast("单击了" + childPosition + "项.");

				Intent intent = new Intent();

				intent.setClass(ctx, PlayBackInfoActivity.class);

				startActivityForResult(intent, REQUESTCODE);
				return true;
			}

		});
	}

	/** 开始进行回放操作 **/
	private void startPlayBack() {
		dismissTimeDialog();
	}

	private void showToast(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}

	private void initViews() {
		super.setToolbarVisiable(false);
		super.getRightButton().setVisibility(View.GONE);
		super.getExtendButton().setVisibility(View.GONE);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.navigation_title_remote_playback));
		ctx = TimeSettingActivity.this;
		endtimeTxt = (TextView) findViewById(R.id.endtime);
		endtimeView = findViewById(R.id.input_endtime_view);
		startScanBtn = (Button) findViewById(R.id.startScan);
		startTimeTxt = (TextView) findViewById(R.id.starttime);
		starttimeView = findViewById(R.id.input_starttime_view);
		cloudAccountView = (ExpandableListView) findViewById(R.id.cloudaccountExtListview);
		cloudAccountView.setGroupIndicator(null);

		setCurrentTimeForTxt();

		initPopupWindow();
	}

	/** 对开始、结束时间设置为当前时间 **/
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
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curMint = c.get(Calendar.MINUTE);

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

		year.setCurrentItem(curyear);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDays - 1);
		hour.setCurrentItem(curHour);
		minute.setCurrentItem(curMint);

		OnWheelScrollListener scrollListener = new OnWheelScrollListener() {

			@Override
			public void onScrollingStarted(WheelView wheel) {

			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				// 需要自动确定日期
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
				// 设置时间显示
				boolean isLeaapYear = PlaybackUtils.isLeapYear(Integer
						.valueOf(yearContent));
				if (startFlag) {
					startTimeTxt.setText(selectTime);
					if (isLeaapYear) {
						int monDay = Integer.valueOf(monthContent);
						if (monDay == 2) {
							int scrollDay = Integer.valueOf(dayContent);
							String t;
							if (scrollDay < 27) {
								t = yearContent + "-" + monthContent + "-"
										+ dayAdapter.getItem(newday + 2) + "  "
										+ hourContent + ":" + minuteContent;
								endtimeTxt.setText(t);
							} else {
								t = yearContent + "-" + monthContent + "-" + 29
										+ "  " + hourContent + ":"
										+ minuteContent;
								endtimeTxt.setText(t);
							}
						} else if ((monDay == 4) || (monDay == 6)
								|| (monDay == 9) || (monDay == 11)) {
							String t;
							int scrollDay = Integer.valueOf(dayContent);
							if (scrollDay < 28) {
								t = yearContent + "-" + monthContent + "-"
										+ dayAdapter.getItem(newday + 2) + "  "
										+ hourContent + ":" + minuteContent;
								endtimeTxt.setText(t);
							} else {
								t = yearContent + "-" + monthContent + "-" + 30
										+ "  " + hourContent + ":"
										+ minuteContent;
								endtimeTxt.setText(t);
							}
						} else {
							String t;
							int scrollDay = Integer.valueOf(dayContent);
							if (scrollDay < 29) {
								t = yearContent + "-" + monthContent + "-"
										+ dayAdapter.getItem(newday + 2) + "  "
										+ hourContent + ":" + minuteContent;
								endtimeTxt.setText(t);
							} else {
								t = yearContent + "-" + monthContent + "-" + 31
										+ "  " + hourContent + ":"
										+ minuteContent;
								endtimeTxt.setText(t);
							}
						}
					}
				} else if (endFlag) {

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
		if (month == 2) {// 闰年
			if ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))) {
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

	private void dismissTimeDialog() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			backAndLeftOperation();
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean okFlag = false;
	private int clickGroup;
	private int clickChild;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUESTCODE) {
			if (data != null) {
				okFlag = data.getBooleanExtra("okBtn", false);
				if (okFlag) {
					actsAdapter.setOkFlag(true);
					clickGroup = data.getIntExtra("group", 0);
					clickChild = data.getIntExtra("child", 0);
					actsAdapter.setChild(clickChild);
					actsAdapter.setGroup(clickGroup);
					actsAdapter.notifyDataSetChanged();
				}
			}
		}
	}
}