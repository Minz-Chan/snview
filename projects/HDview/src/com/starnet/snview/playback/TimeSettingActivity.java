package com.starnet.snview.playback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.PinyinComparator;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.wheelview.widget.NumericWheelAdapter;
import com.starnet.snview.component.wheelview.widget.OnWheelScrollListener;
import com.starnet.snview.component.wheelview.widget.WheelView;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.playback.utils.PlayBackTask;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.protocol.message.SearchRecordRequest;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint({ "SdCardPath", "SimpleDateFormat", "HandlerLeak" })
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
	private PopupWindow popWindow;
	private NumericWheelAdapter dayAdapter;
	private NumericWheelAdapter yearAdapter;
	private NumericWheelAdapter hourAdapter;
	private NumericWheelAdapter monthAdapter;
	private NumericWheelAdapter minuteAdapter;

	private Button startScanBtn;

	private List<CloudAccount> originCAs;
	private boolean endFlag = false;
	private boolean startFlag = false;
	private final int TIMEOUT = 0x0002;
	private final int LOADSUC = 0x0003;
	private final int LOADFAI = 0x0004;
	private DeviceItemRequestTask[] tasks;
	private final int REQUESTCODE = 0x0005;
	private ExpandableListView cloudAccountView;
	private AccountsPlayBackExpanableAdapter actsAdapter;
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	private Handler mHdler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case TIMEOUT:
				Bundle msgD = msg.getData();
				CloudAccount netCA1 = (CloudAccount) msgD
						.getSerializable("netCA");
				showToast(netCA1.getUsername() + "用户请求超时");

				int positi = msgD.getInt("position");

				netCA1.setRotate(true);
				originCAs.set(positi, netCA1);
				actsAdapter.notifyDataSetChanged();
				break;
			case LOADSUC:
				msgD = msg.getData();
				final int posi = msgD.getInt("position");
				String suc = msgD.getString("success");
				if (suc.equals("Yes")) {
					final int pos = Integer.valueOf(posi);
					final CloudAccount netCA = (CloudAccount) msgD
							.getSerializable("netCA");
					netCA.setRotate(true);
					originCAs.set(pos, netCA);
				}
				int size = originCAs.size();
				for (int i = 1; i < size; i++) {
					CloudAccount cloudAccount = originCAs.get(i);
					if (cloudAccount != null) {
						List<DeviceItem> dList = cloudAccount.getDeviceList();
						if ((dList != null) && (dList.size() > 0)) {
							Collections.sort(dList, new PinyinComparator());// 排序...
						}
					}
				}
				actsAdapter.notifyDataSetChanged();
				break;
			case LOADFAI:
				msgD = msg.getData();
				int posit = msgD.getInt("position");
				CloudAccount netCA2 = (CloudAccount) msgD
						.getSerializable("netCA");
				showToast(netCA2.getUsername() + "用户请求失败");
				netCA2.setRotate(true);
				originCAs.set(posit, netCA2);
				actsAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}

	};

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
		originCAs = downloadDatas();
		actsAdapter = new AccountsPlayBackExpanableAdapter(ctx, originCAs);
		cloudAccountView.setAdapter(actsAdapter);
	}

	/** 加载新的数据 **/
	private List<CloudAccount> downloadDatas() {
		List<CloudAccount> accounts = getCloudAccounts();
		if (accounts != null) {
			boolean isOpen = NetWorkUtils.checkNetConnection(ctx);
			int enableSize = getEnableCACount(accounts);
			tasks = new DeviceItemRequestTask[enableSize];
			if (isOpen) {
				int j = 0;
				for (int i = 0; i < accounts.size(); i++) {
					CloudAccount c = accounts.get(i);
					if (c.isEnabled()) {
						tasks[j] = new DeviceItemRequestTask(ctx, c, mHdler, i);
						tasks[j].start();
						j++;
					}
				}
			}
		}
		return accounts;
	}

	private int getEnableCACount(List<CloudAccount> accounts) {
		int count = 0;
		for (int i = 0; i < accounts.size(); i++) {// 启动线程进行网络访问，每个用户对应着一个线程
			CloudAccount cAccount = accounts.get(i);
			boolean isEnable = cAccount.isEnabled();
			if (isEnable) {
				count++;
			}
		}
		return count;
	}

	private List<CloudAccount> getCloudAccounts() {
		try {
			return ReadWriteXmlUtils.getCloudAccountList(filePath);
		} catch (Exception e) {
			return null;
		}
	}

	private void backAndLeftOperation() {
		dismissTimeDialog();
		if (tasks != null) {
			for (int i = 0; i < tasks.length; i++) {
				if (tasks[i] != null) {
					tasks[i].setCanceled(true);
				}
			}
		}
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
				CloudAccount cA = (CloudAccount) parent
						.getExpandableListAdapter().getGroup(groupPosition);// 获取用户账号信息
				if (cA.isExpanded()) {// 判断列表是否已经展开
					cA.setExpanded(false);
				} else {
					cA.setExpanded(true);
				}
				return false;
			}
		});

		starttimeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFlag = true;
				endFlag = false;
				if (popWindow.isShowing()) {
					popWindow.dismiss();
				} else {
					popWindow.showAsDropDown(v);
					popWindow.setFocusable(false);
					popWindow.setOutsideTouchable(true);
					popWindow.update();
				}
			}
		});

		endtimeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				endFlag = true;
				startFlag = false;
				if (popWindow.isShowing()) {
					popWindow.dismiss();
				} else {
					popWindow.showAsDropDown(v);
					popWindow.setFocusable(false);
					popWindow.setOutsideTouchable(true);
					popWindow.update();
				}
			}
		});
		startScanBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startPlayBack();
				TimeSettingActivity.this.finish();
			}
		});
	}

	private PlayBackTask pbTask;

	/** 开始进行回放操作 **/
	private void startPlayBack() {
		dismissTimeDialog();
		SearchRecordRequest srr = getRequestInfo();
		pbTask = new PlayBackTask(srr);
		pbTask.start();
	}

	private SearchRecordRequest getRequestInfo() {
		SearchRecordRequest srr = new SearchRecordRequest();
		String startTime = (String) startTimeTxt.getText();
		String endTime = (String) endtimeTxt.getText();
		DeviceItem dItem = (DeviceItem) actsAdapter.getChild(clickGroup,
				clickChild);
		int maxChannelCount = 1;
		OWSPDateTime sTime = getOWSPDateTime(startTime);
		OWSPDateTime eTime = getOWSPDateTime(endTime);
		srr.setStartTime(sTime);
		srr.setEndTime(eTime);
		
		
		return srr;
	}

	private OWSPDateTime getOWSPDateTime(String time) {
		OWSPDateTime owspTime = new OWSPDateTime();
		String []sumTime = time.split(" ");
		String ymdTemp = sumTime[0];
		String hmsTemp = sumTime[1];
		int []ymd = getIntYMDData(ymdTemp);
		int []hms = getIntHMSData(hmsTemp);
		owspTime.setYear(ymd[0]);
		owspTime.setMonth(ymd[1]);
		owspTime.setDay(ymd[2]);
		owspTime.setHour(hms[0]);
		owspTime.setMinute(hms[1]);
		owspTime.setSecond(hms[2]);
		return owspTime;
	}

	private int[] getIntHMSData(String ymdTemp) {
		int []data = new int[3];
		String []temp = ymdTemp.split(":");
		for (int i = 0; i < 2; i++) {
			data[i] = Integer.valueOf(temp[i]);
		}
		return data;
	}
	
	private int[] getIntYMDData(String ymdTemp) {
		int []data = new int[3];
		String []temp = ymdTemp.split("-");
		for (int i = 0; i < 3; i++) {
			data[i] = Integer.valueOf(temp[i]);
		}
		return data;
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
		popWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		View view2 = popWindow.getContentView();

		year = (WheelView) view2.findViewById(R.id.year);
		month = (WheelView) view2.findViewById(R.id.month);
		day = (WheelView) view2.findViewById(R.id.day);
		hour = (WheelView) view2.findViewById(R.id.hour);
		minute = (WheelView) view2.findViewById(R.id.minute);
		popWindow.setAnimationStyle(R.style.PopupAnimation);
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

		yearAdapter = new NumericWheelAdapter(2003, c1.get(Calendar.YEAR));
		year.setAdapter(yearAdapter);
		year.setLabel(null);
		year.setCyclic(true);
		year.setCurrentItem(curyear);

		monthAdapter = new NumericWheelAdapter(1, 12, "%02d");
		month.setAdapter(monthAdapter);
		month.setLabel(null);
		month.setCyclic(true);
		curMonth += 1;

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

				if (curYear >= 2) {
					curYear = curYear - 2;
				} else {
					curYear = curYear + 8;
				}

				if (newmon >= 2) {
					newmon = newmon - 2;
				} else {
					newmon = newmon + 10;
				}

				if (newday >= 2) {
					newday = newday - 2;
				}

				if (newhour >= 2) {
					newhour = newhour - 2;
				} else {
					newhour = newhour + 22;
				}

				if (newmin >= 2) {
					newmin = newmin - 2;
				} else {
					newmin = newmin + 58;
				}

				String dayContent = dayAdapter.getItem(newday);
				String hourContent = hourAdapter.getItem(newhour);
				String yearContent = yearAdapter.getItem(curYear);
				String monthContent = monthAdapter.getItem(newmon);
				String minuteContent = minuteAdapter.getItem(newmin);

				String selectTime = yearContent + "-" + monthContent + "-"
						+ dayContent + "  " + hourContent + ":" + minuteContent;
				startTimeTxt.setText(selectTime);
				//
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
		if (popWindow != null && popWindow.isShowing()) {
			popWindow.dismiss();
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
	private int channelNo;
	private String type;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUESTCODE) {
			if (data != null) {
				okFlag = data.getBooleanExtra("okBtn", false);
				if (okFlag) {
					String chName = getString(R.string.playback_channel);
					actsAdapter.setOkFlag(true);
					clickGroup = data.getIntExtra("group", 0);
					clickChild = data.getIntExtra("child", 0);
					type = data.getStringExtra("type");
					channelNo = Integer.valueOf(data.getStringExtra("channel").replace(chName, ""));
					actsAdapter.setChild(clickChild);
					actsAdapter.setGroup(clickGroup);
					actsAdapter.notifyDataSetChanged();
				}
			}
		}
	}
}