package com.starnet.snview.alarmmanager;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class AlarmContentActivity extends BaseActivity {
	
	private int pos;

	private EditText alarm_content_ip;
	private EditText alarm_content_time;
	private EditText alarm_content_type;
	private EditText alarm_content_iport;
	private EditText alarm_content_device;
	private EditText alarm_content_channel;
	private EditText alarm_content_contents;
	private EditText alarm_content_pushdomain;
	private EditText alarm_content_pushusername;
	private final int ALARM_CONTEN_DIALOG = 0x0003;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_content_activity);

		initViews();
		setListeners();
	}

	private void initViews() {

		super.hideRightButton();
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.getTitleView().setText(getString(R.string.alarm_manageradapter_content));

		alarm_content_ip = (EditText) findViewById(R.id.alarm_content_ip);
		alarm_content_time = (EditText) findViewById(R.id.alarm_content_time);
		alarm_content_type = (EditText) findViewById(R.id.alarm_content_type);
		alarm_content_iport = (EditText) findViewById(R.id.alarm_content_iport);
		alarm_content_device = (EditText) findViewById(R.id.alarm_content_device);
		alarm_content_channel = (EditText) findViewById(R.id.alarm_content_channel);
		alarm_content_contents = (EditText) findViewById(R.id.alarm_content_contents);
		alarm_content_pushdomain = (EditText) findViewById(R.id.alarm_content_pushdomain);
		alarm_content_pushusername = (EditText) findViewById(R.id.alarm_content_pushusername);

		alarm_content_ip.setKeyListener(null);
		alarm_content_time.setKeyListener(null);
		alarm_content_type.setKeyListener(null);
		alarm_content_iport.setKeyListener(null);
		alarm_content_device.setKeyListener(null);
		alarm_content_channel.setKeyListener(null);
		alarm_content_contents.setKeyListener(null);
		alarm_content_pushdomain.setKeyListener(null);
		alarm_content_pushusername.setKeyListener(null);

		pos = getIntent().getIntExtra("position", 0);
		AlarmDevice device = getIntent().getParcelableExtra("alarmDevice");

		String time = device.getAlarmTime().replaceAll(",", " ").replaceAll("，", " ");
		
		alarm_content_time.setText(time);
		alarm_content_ip.setText(device.getIp());
		alarm_content_type.setText(device.getAlarmType());
		alarm_content_iport.setText("" + device.getPort());
		alarm_content_device.setText(device.getDeviceName());
		alarm_content_channel.setText("" + device.getChannel());
		alarm_content_contents.setText(device.getAlarmContent());
		alarm_content_pushdomain.setText(device.getPusherDomain());
		alarm_content_pushusername.setText(device.getPusherUserName());

	}

	protected void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backAndLeftCope();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			backAndLeftCope();
		}
		return true;
	}
	
	private void backAndLeftCope(){
		Intent data = new Intent();
		data.putExtra("position", pos);
		data.putExtra("isContentBack", true);
		setResult(ALARM_CONTEN_DIALOG, data);
		AlarmContentActivity.this.finish();
	}
}