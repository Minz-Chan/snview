package com.starnet.snview.playback;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.starnet.snview.R;
import com.starnet.snview.component.wheelview.widget.NumericWheelAdapter;
import com.starnet.snview.component.wheelview.widget.OnWheelScrollListener;
import com.starnet.snview.component.wheelview.widget.WheelView;

public class TimeDialogActivity extends Activity {

	private WheelView year;
	private WheelView month;
	private WheelView day;
	private WheelView hour;
	private WheelView minute;

	private int dayNum;
	private int curyear;
	private int curMonth;
	private int curDays;
	private int poor;

	private Button okBtn;
	private Button cancelBtn;

	private String flag;
	private String selectTime;
	private final int REQUESTCODE = 0x0003;
	private NumericWheelAdapter dayAdapter;
	private NumericWheelAdapter yearAdapter;
	private NumericWheelAdapter hourAdapter;
	private NumericWheelAdapter monthAdapter;
	private NumericWheelAdapter minuteAdapter;
	private final String TAG = "TimeDialogActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.time_dialog);

		initViews();
		setListenersForWadgets();
	}

	private void setListenersForWadgets() {
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("ok", "cancel");
				data.putExtra("flag", flag);
				data.putExtra("selecttime", selectTime);
				setResult(REQUESTCODE, data);
				TimeDialogActivity.this.finish();
			}
		});
		okBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent data = new Intent();
				data.putExtra("ok", "ok");
				data.putExtra("flag", flag);
				data.putExtra("selecttime", selectTime);
				setResult(REQUESTCODE, data);
				TimeDialogActivity.this.finish();

			}
		});
	}

	private void initViews() {

		okBtn = (Button) findViewById(R.id.okBtn);
//		cancelBtn = (Button) findViewById(R.id.cancelBtn);

		year = (WheelView) findViewById(R.id.year);
		month = (WheelView) findViewById(R.id.month);
		day = (WheelView) findViewById(R.id.day);

		hour = (WheelView) findViewById(R.id.hour);
		minute = (WheelView) findViewById(R.id.minute);

		flag = getIntent().getStringExtra("flag");

		Calendar c1 = Calendar.getInstance();
		Calendar c = Calendar.getInstance();
		curyear = c.get(Calendar.YEAR);
		curMonth = c.get(Calendar.MONTH);
		curDays = c.get(Calendar.DAY_OF_MONTH);
		poor = curyear - 10;

		yearAdapter = new NumericWheelAdapter(2000, c1.get(Calendar.YEAR));
		year.setAdapter(yearAdapter);
		year.setLabel("年");
		year.setCyclic(true);
		year.setCurrentItem(curyear);
		curMonth += 1;

		monthAdapter = new NumericWheelAdapter(1, 12, "%02d");
		month.setAdapter(monthAdapter);
		month.setLabel("月");
		month.setCyclic(true);

		dayNum = setwheelDay(curyear, curMonth);
		dayAdapter = new NumericWheelAdapter(1, dayNum, "%02d");
		day.setAdapter(dayAdapter);
		day.setLabel("日");
		day.setCyclic(true);

		hourAdapter = new NumericWheelAdapter(0, 23, "%02d");
		hour.setAdapter(hourAdapter);
		hour.setLabel("时");
		hour.setCyclic(true);

		minuteAdapter = new NumericWheelAdapter(0, 59, "%02d");
		minute.setAdapter(minuteAdapter);
		minute.setLabel("分");
		minute.setCyclic(true);

		year.setCurrentItem(curyear + 10);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDays - 1);

		int hours = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		selectTime = curyear + "-" + curMonth + "-" + curDays + "  " + hours
				+ ":" + minutes;

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

				selectTime = yearContent + "-" + monthContent + "-"
						+ dayContent + "  " + hourContent + ":" + minuteContent;
				Log.v(TAG, selectTime);
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
}
