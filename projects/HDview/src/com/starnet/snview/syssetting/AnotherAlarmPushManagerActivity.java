package com.starnet.snview.syssetting;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.componet.switchbutton.CheckSwitchButton;
import com.starnet.snview.util.ReadWriteXmlUtils;

public class AnotherAlarmPushManagerActivity extends BaseActivity implements
		OnCheckedChangeListener {

	private Context ctx;
	protected final String TAG = "AnotherAlarmPushManagerActivity";
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	private CheckSwitchButton csvPush;
	private TextView tvPush;

	private CheckSwitchButton csvShake;
	private TextView tvShake;

	private CheckSwitchButton csvSound;
	private TextView tvSound;

	private CheckSwitchButton csvAlarmUserAccept;
	private TextView tvAlarmUserAccept;

	private TextView tvAlarmUsers;// 显示推送账户
	
	private SharedPreferences showFlagSP;
	private boolean isAcc;
	private boolean isShake;
	private boolean isSound;
	private boolean isAllAcc;
	
	private Vibrator vibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_layout_notify_user_activity);

		intialViews();
		setListeners();
	}

	private void setListeners() {
		csvPush.setOnCheckedChangeListener(this);
		csvShake.setOnCheckedChangeListener(this);
		csvSound.setOnCheckedChangeListener(this);
		csvAlarmUserAccept.setOnCheckedChangeListener(this);
	}

	/** 清除所有的推送消息 **/
	protected void jumpClearDialog() {
		Builder builder = new Builder(ctx);
		builder.setTitle(R.string.system_setting_pushset_clear_allalarminfo_ok);
		String ok = getString(R.string.system_setting_alarm_pushset_builer_identify_add_ok);
		String cancel = getString(R.string.system_setting_alarm_pushset_builer_identify_add_cance);
		builder.setNegativeButton(cancel, null);
		builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				boolean isSuc = ReadWriteXmlUtils.deleteAlarmInfoFile();
				if (isSuc) {
					showTost(getString(R.string.system_setting_pushset_clear_alarm_suc));
				} else {
					showTost(getString(R.string.system_setting_pushset_clear_alarm_fai));
				}
			}
		});
		builder.show();
	}

	private void showTost(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}

	private void intialViews() {
		super.hideRightButton();
		super.hideExtendButton();
		super.getToolbarContainer().setVisibility(View.GONE);
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.system_setting_alarm_pushset));
		ctx = AnotherAlarmPushManagerActivity.this;
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		csvPush = (CheckSwitchButton) findViewById(R.id.csv_push);
		csvShake = (CheckSwitchButton) findViewById(R.id.csv_push_shake);
		csvSound = (CheckSwitchButton) findViewById(R.id.csv_push_sound);
		csvAlarmUserAccept = (CheckSwitchButton) findViewById(R.id.csv_alarmuser_push);

		tvPush = (TextView) findViewById(R.id.tv_push_accept);
		tvShake = (TextView) findViewById(R.id.tv_push_shake);
		tvSound = (TextView) findViewById(R.id.tv_push_sound);
		tvAlarmUserAccept = (TextView) findViewById(R.id.tv_alarmuser_accept);

		tvAlarmUsers = (TextView) findViewById(R.id.tv_alarmusers);
		
		showFlagSP = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", 0);
		isAcc = showFlagSP.getBoolean("isAccept", true);
		isShake = showFlagSP.getBoolean("isShake", true);
		isSound = showFlagSP.getBoolean("isSound", true);
		isAllAcc = showFlagSP.getBoolean("isAllAccept", true);
		
		if(isAllAcc){
			csvPush.setChecked(true);
			tvPush.setText(getString(R.string.notify_accept_open));
		}else{
			tvPush.setText(getString(R.string.notify_accept_off));
			csvPush.setChecked(false);
			csvShake.setEnabled(false);
			csvSound.setEnabled(false);
			csvAlarmUserAccept.setEnabled(false);
		}
		
		if(isShake){
			csvShake.setChecked(true);
			tvShake.setText(getString(R.string.remind_shake_open));
		}else{
			csvShake.setChecked(false);
			tvShake.setText(getString(R.string.remind_shake_off));
		}
		
		if(isSound){
			csvSound.setChecked(true);
			tvSound.setText(getString(R.string.remind_sound_open));
		}else{
			csvSound.setChecked(false);
			tvSound.setText(getString(R.string.remind_sound_off));
		}
		
		if(isAcc){
			csvAlarmUserAccept.setChecked(true);
			tvAlarmUserAccept.setText(getString(R.string.alarm_accept_open));
		}else{
			csvAlarmUserAccept.setChecked(false);
			tvAlarmUserAccept.setText(getString(R.string.alarm_accept_off));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			this.finish();
		}
		return true;
	}

	public static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

		}
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.csv_push:
			isAllAcc = isChecked;
			csvShake.setEnabled(isChecked);
			csvSound.setEnabled(isChecked);
			csvAlarmUserAccept.setEnabled(isChecked);
			if(isAllAcc){
				tvPush.setText(getString(R.string.notify_accept_open));
			}else{
				tvPush.setText(getString(R.string.notify_accept_off));
			}
			break;
		case R.id.csv_push_shake:
			isShake = isChecked;
			if(isShake){
				tvShake.setText(getString(R.string.remind_shake_open));
				long[] pattern = { 50, 200, 50, 200 };
				vibrator.vibrate(pattern, -1);
			}else{
				tvShake.setText(getString(R.string.remind_shake_off));
			}
			break;
		case R.id.csv_push_sound:
			isSound = isChecked;
			if(isSound){
				tvSound.setText(getString(R.string.remind_sound_open));
				new Thread(new Runnable() {
					@Override
					public void run() {
						SnapshotSound s = new SnapshotSound(ctx);
						s.playPushSetSound();
					}
				}).start();
			}else{
				tvSound.setText(getString(R.string.remind_sound_off));
			}
			break;
		case R.id.csv_alarmuser_push:
			isAcc = isChecked;
			if(isAcc){
				tvAlarmUserAccept.setText(getString(R.string.alarm_accept_open));
			}else{
				tvAlarmUserAccept.setText(getString(R.string.alarm_accept_off));
			}
			break;
		}
	}
}