package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.Utils;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.componet.switchbutton.CheckSwitchButton;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

public class AnotherAlarmPushManagerActivity extends BaseActivity implements OnCheckedChangeListener ,OnClickListener{

	private static Context ctx;
	public static final int REQUESTCODE = 0x0001;
	protected static final String TAG = "AnotherAlarmPushManagerActivity";
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	private static CheckSwitchButton csvPush;
	private static TextView tvPush;

	private static CheckSwitchButton csvShake;
	private static TextView tvShake;

	private static CheckSwitchButton csvSound;
	private static TextView tvSound;

	private static CheckSwitchButton csvAlarmUserAccept;
	private static TextView tvAlarmUserAccept;

	private static TextView tvAlarmUsers;// 显示推送账户
	private static RelativeLayout alarmUserContainer;
	
	private Button clearAlarmInfBtn;
	
	private static boolean isAcc;
	private static boolean isShake;
	private static boolean isSound;
	private static boolean isAllAcc;
	private static ProgressDialog pushServicePrg;
	private SharedPreferences showFlagSP;
	
	private static int openOperationFlag = -1;//启动服务的标识位，用于标识启动的哪一个服务（startWork,stopWork,setTags,delTags）
	private static final int STOPWORKFLAG = 0x0001;
	private static final int STARTWORKFLAG = 0x0002;
	private static final int DELTAGSFLAG = 0x0003;
	private static final int SETTAGSFLAG = 0x0004;
	
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
	
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		clearAlarmInfBtn.setOnClickListener(this);
		alarmUserContainer.setOnClickListener(this);
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
					showToast(getString(R.string.system_setting_pushset_clear_alarm_suc));
				} else {
					showToast(getString(R.string.system_setting_pushset_clear_alarm_fai));
				}
			}
		});
		builder.show();
	}

	private static void showToast(String content) {
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
		
		csvPush = (CheckSwitchButton) findViewById(R.id.csv_push);
		csvShake = (CheckSwitchButton) findViewById(R.id.csv_push_shake);
		csvSound = (CheckSwitchButton) findViewById(R.id.csv_push_sound);
		csvAlarmUserAccept = (CheckSwitchButton) findViewById(R.id.csv_alarmuser_push);

		tvPush = (TextView) findViewById(R.id.tv_push_accept);
		tvShake = (TextView) findViewById(R.id.tv_push_shake);
		tvSound = (TextView) findViewById(R.id.tv_push_sound);
		tvAlarmUserAccept = (TextView) findViewById(R.id.tv_alarmuser_accept);

		tvAlarmUsers = (TextView) findViewById(R.id.tv_alarmusers);
		clearAlarmInfBtn = (Button) findViewById(R.id.clearAlarmInfBtn);
		alarmUserContainer = (RelativeLayout) findViewById(R.id.alarmUserContainer);
		
		showFlagSP = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", 0);
		isAcc = showFlagSP.getBoolean("isAccept", true);
		isShake = showFlagSP.getBoolean("isShake", true);
		isSound = showFlagSP.getBoolean("isSound", true);
		isAllAcc = showFlagSP.getBoolean("isAllAccept", true);
		//test????????????????????????????????????????????????????????????
//		isAllAcc = true;
//		isAcc = true;
		//
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
		
		String content = "";
		List<CloudAccount> accounts = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		if (accounts != null && accounts.size() > 0) {
			for (int i = 0; i < accounts.size(); i++) {
				if (i != (accounts.size() - 1)) {
					String result = accounts.get(i).getUsername() + ",";
					content += result;
				} else {
					String result = accounts.get(i).getUsername();
					content += result;
				}
			}
		}
		if (content.length() >= 18) {
			content = content.substring(0, 18) + "...";
		}
		tvAlarmUsers.setText(content);
	}
	
	private void saveSharedPreference(){
		SharedPreferences sps = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", Context.MODE_PRIVATE);
		Editor editor = sps.edit();
		editor.putBoolean("isAllAccept", isAllAcc);
		editor.putBoolean("isShake", isShake);
		editor.putBoolean("isSound", isSound);
		editor.putBoolean("isAccept", isAcc);
		editor.commit();
	}

	public static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			int errorCode = data.getInt("errorCode", -1);
			if(errorCode == 0){//推送服务调用成功
				if(openOperationFlag == STOPWORKFLAG){
					showToast(ctx.getString(R.string.pushservice_close_success));
				}else if (openOperationFlag == STARTWORKFLAG) {
					showToast(ctx.getString(R.string.pushservice_open_success));
				}else if (openOperationFlag == SETTAGSFLAG) {
					showToast(ctx.getString(R.string.pushservice_settags_success));
				}else if (openOperationFlag == DELTAGSFLAG) {
					showToast(ctx.getString(R.string.pushservice_deltags_success));
				}
				Log.i(TAG, "======成功之后关闭*****"+openOperationFlag);
			}else{//置回原来的标识情况
				if(openOperationFlag == STOPWORKFLAG){
					isAllAcc = true;
					csvPush.setChecked(isAllAcc);
					showToast(ctx.getString(R.string.pushservice_close_failure));
				}else if (openOperationFlag == STARTWORKFLAG) {
					isAllAcc = false;
					csvPush.setChecked(isAllAcc);
					showToast(ctx.getString(R.string.pushservice_open_failure));
					csvShake.setEnabled(false);
					csvSound.setEnabled(false);
					csvAlarmUserAccept.setEnabled(false);
				}else if (openOperationFlag == SETTAGSFLAG) {
					isAcc = false;
					csvAlarmUserAccept.setChecked(isAcc);
					showToast(ctx.getString(R.string.pushservice_settags_failure));
				}else if (openOperationFlag == DELTAGSFLAG) {
					isAcc = true;
					csvAlarmUserAccept.setChecked(isAcc);
					showToast(ctx.getString(R.string.pushservice_deltags_failure));
				}
				Log.i(TAG, "======失败之后的*****"+openOperationFlag);
			}
			closeProgressDialog();
		}

		private void closeProgressDialog() {
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String content = "";
		List<CloudAccount> accounts = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		if (accounts != null && accounts.size() > 0) {
			for (int i = 0; i < accounts.size(); i++) {
				if (i != (accounts.size() - 1)) {
					String result = accounts.get(i).getUsername() + ",";
					content += result;
				} else {
					String result = accounts.get(i).getUsername();
					content += result;
				}
			}
		}
		if (content.length() >= 18) {
			content = content.substring(0, 18) + "...";
		}
		tvAlarmUsers.setText(content);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			this.finish();
		}
		return true;
	}
	
	private List<String>tags = new ArrayList<String>();
	private boolean isRequestStartWork = false;//用于标识开启推送服务还是关闭推送服务，false 表示关闭，true表示开启

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String apiKey = Utils.getMetaValue(ctx.getApplicationContext(), "api_key");
		switch (buttonView.getId()) {
		case R.id.csv_push:
			boolean isOpen = NetWorkUtils.checkNetConnection(ctx);
			if(!isOpen){
				isAllAcc = !isChecked;
				csvPush.setChecked(isAllAcc);
				showToast(getString(R.string.pushservice_network_notopen));
				if(!isAllAcc){
					csvShake.setEnabled(false);
					csvSound.setEnabled(false);
					csvAlarmUserAccept.setEnabled(false);
				}else{
					csvShake.setEnabled(true);
					csvSound.setEnabled(true);
					csvAlarmUserAccept.setEnabled(true);
				}
				return;
			}
			isAllAcc = isChecked;
			csvShake.setEnabled(isChecked);
			csvSound.setEnabled(isChecked);
			csvAlarmUserAccept.setEnabled(isChecked);
			if(isAllAcc){
				isRequestStartWork = true;
				openOperationFlag = STARTWORKFLAG;
				tvPush.setText(getString(R.string.notify_accept_open));
				PushManager.startWork(ctx,PushConstants.LOGIN_TYPE_API_KEY,apiKey);
				openProgressDialogForPushService(ctx.getString(R.string.system_setting_pushservice_openning));
			}else{
				isRequestStartWork = false;
				PushManager.stopWork(ctx);
				openOperationFlag = STOPWORKFLAG;
				tvPush.setText(getString(R.string.notify_accept_off));
				openProgressDialogForPushService(ctx.getString(R.string.system_setting_pushservice_closing));
			}
			break;
		case R.id.csv_push_shake:
			isShake = isChecked;
			if(isShake){
				tvShake.setText(getString(R.string.remind_shake_open));
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				long[] pattern = { 50, 200, 50, 200 };
				vibrator.vibrate(pattern, -1);
			}else{
				tvShake.setText(getString(R.string.remind_shake_off));
			}
			saveSharedPreference();
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
			saveSharedPreference();
			break;
		case R.id.csv_alarmuser_push:
			isOpen = NetWorkUtils.checkNetConnection(ctx);
			if(!isOpen){
				isAcc = !isChecked;
				csvAlarmUserAccept.setChecked(isAcc);
				showToast(getString(R.string.pushservice_network_notopen));
				return;
			}
			
			if((tags == null) || tags.equals("") || tags.size() == 0){
				isAcc = !isChecked;
				csvAlarmUserAccept.setChecked(isAcc);
				showToast(ctx.getString(R.string.pushservice_alarmusr_null_open));
				return;
			}
			
			isAcc = isChecked;
			if(isAcc){
				openOperationFlag = SETTAGSFLAG;
				PushManager.setTags(ctx, tags);
				tvAlarmUserAccept.setText(getString(R.string.alarm_accept_open));
				openProgressDialogForPushService(ctx.getString(R.string.pushservice_alarmusr_settags));
			}else{
				openOperationFlag = DELTAGSFLAG;
				tvAlarmUserAccept.setText(getString(R.string.alarm_accept_off));
				openProgressDialogForPushService(ctx.getString(R.string.pushservice_alarmusr_deltags));
				PushManager.delTags(ctx, tags);
			}
			break;
		}
		Log.i(TAG, "======开启的服务标识*****"+openOperationFlag);
	}

	private void openProgressDialogForPushService(String title){
		pushServicePrg = ProgressDialog.show(ctx, "",title,  true, false);
		pushServicePrg.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.alarmUserContainer:
			Intent intent = new Intent(ctx, AlarmAccountsPreviewActivity.class);
			startActivityForResult(intent, REQUESTCODE);
			break;
		case R.id.clearAlarmInfBtn:
			jumpClearDialog();
			break;
		}
	}
}