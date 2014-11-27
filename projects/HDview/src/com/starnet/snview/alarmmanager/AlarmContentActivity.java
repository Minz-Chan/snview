package com.starnet.snview.alarmmanager;

import com.starnet.snview.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;

public class AlarmContentActivity extends Activity {

	private EditText alarm_content_device;
	private EditText alarm_content_time;
	private EditText alarm_content_type;
	private EditText alarm_content_contents;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.alarm_content_act);
		
		initViews();
	}

	private void initViews() {
		
		alarm_content_time = (EditText) findViewById(R.id.alarm_content_time);
		alarm_content_type = (EditText) findViewById(R.id.alarm_content_type);
		alarm_content_device = (EditText) findViewById(R.id.alarm_content_device);
		alarm_content_contents = (EditText) findViewById(R.id.alarm_content_contents);
		
		alarm_content_time.setKeyListener(null);
		alarm_content_type.setKeyListener(null);
		alarm_content_device.setKeyListener(null);
		alarm_content_contents.setKeyListener(null);
		
		AlarmDevice device = getIntent().getParcelableExtra("alarmDevice");
		alarm_content_time.setText(device.getAlarmTime());
		alarm_content_type.setText(device.getAlarmType());
		alarm_content_device.setText(device.getDeviceName());
		alarm_content_contents.setText(device.getAlarmContent());
	}
	
	

}