package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.AlarmReceiver;
import com.starnet.snview.alarmmanager.Utils;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.componet.switchbutton.CheckSwitchButton;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("HandlerLeak")
public class AnotherAlarmPushManagerActivity extends BaseActivity implements OnCheckedChangeListener ,OnClickListener{

	private static Context ctx;
	public static final int REQUESTCODE = 0x0001;
	protected static final String TAG = "AnotherAlarmPushManagerActivity";

	private CheckSwitchButton csvPush;
	private TextView tvPush;

	private CheckSwitchButton csvShake;
	private TextView tvShake;

	private CheckSwitchButton csvSound;
	private TextView tvSound;

	private CheckSwitchButton csvAlarmUserAccept;
	private TextView tvAlarmUserAccept;

	private TextView tvAlarmUsers;// 显示推送账户
	private Button clearAlarmInfBtn;
	private RelativeLayout alarmUserContainer;
	
	private boolean isAcc;
	private boolean isShake;
	private boolean isSound;
	private boolean isAllAcc;
	private SharedPreferences showFlagSP;
	private ProgressDialog pushServicePrg;
	private LinearLayout container_layout;
	private RelativeLayout container_csvs1;
	private RelativeLayout container_csvs2;
	private RelativeLayout container_csvs3;
	private RelativeLayout container_csvs4;
	
	private List<String>tags = new ArrayList<String>();
		
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
				AlarmReceiver.mActivity = null;
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
		AlarmReceiver.mActivity = AnotherAlarmPushManagerActivity.this;
		
		csvPush = (CheckSwitchButton) findViewById(R.id.csv_push);
		csvShake = (CheckSwitchButton) findViewById(R.id.csv_push_shake);
		csvSound = (CheckSwitchButton) findViewById(R.id.csv_push_sound);
		csvAlarmUserAccept = (CheckSwitchButton) findViewById(R.id.csv_alarmuser_push);

		tvPush = (TextView) findViewById(R.id.tv_push_accept);
		tvShake = (TextView) findViewById(R.id.tv_push_shake);
		tvSound = (TextView) findViewById(R.id.tv_push_sound);
		container_layout = (LinearLayout) findViewById(R.id.container_layout);
		tvAlarmUserAccept = (TextView) findViewById(R.id.tv_alarmuser_accept);

		tvAlarmUsers = (TextView) findViewById(R.id.tv_alarmusers);
		clearAlarmInfBtn = (Button) findViewById(R.id.clearAlarmInfBtn);
		
		container_csvs1 = (RelativeLayout) findViewById(R.id.container_csvs1);
		container_csvs2 = (RelativeLayout) findViewById(R.id.container_csvs2);
		container_csvs3 = (RelativeLayout) findViewById(R.id.container_csvs3);
		container_csvs4 = (RelativeLayout) findViewById(R.id.container_csvs4);
		alarmUserContainer = (RelativeLayout) findViewById(R.id.alarmUserContainer);
		
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
		
		listener = new PhoneUIListener();
		container_layout.getViewTreeObserver().addOnGlobalLayoutListener(listener);
		
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
		
		getTags(accounts);
	}
	
	private void getTags(List<CloudAccount> accounts){
		try {
			if (accounts != null && accounts.size() > 0) {
				for (int i = 0; i < accounts.size() - 1; i++) {
					String userName = accounts.get(i).getUsername();
					String paswd = accounts.get(i).getPassword();
					paswd = MD5Utils.createMD5(paswd);
					String result = userName + paswd;
					tags.add(result);
				}
				String userName = accounts.get(accounts.size() - 1).getUsername();
				String paswd = accounts.get(accounts.size() - 1).getPassword();
				paswd = MD5Utils.createMD5(paswd);
				String result = userName + paswd;
				tags.add(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			int errorCode = data.getInt("errorCode", -1);
			if(errorCode == 0){//推送服务调用成功
				if (currentAction == ACTION.START_SERVICE) {
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					showToast(ctx.getString(R.string.pushservice_open_success));
				}
				if (currentAction == ACTION.STOP_SERVICE) {
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					showToast(ctx.getString(R.string.pushservice_close_success));
				}
				if (currentAction == ACTION.ADD_TAG) {
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					showToast(ctx.getString(R.string.pushservice_settags_success));
				}
				if (currentAction == ACTION.DEL_TAG) {
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					showToast(ctx.getString(R.string.pushservice_deltags_success));
				}
			}else{//置回原来的标识情况
				if (currentAction == ACTION.START_SERVICE) {
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					startSvrTask.cancel(false);
					showToast(ctx.getString(R.string.pushservice_open_failure));
				}
				if (currentAction == ACTION.STOP_SERVICE) {
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					stopSvrTask.cancel(false);
					showToast(ctx.getString(R.string.pushservice_close_failure));
				}
				if (currentAction == ACTION.ADD_TAG) {
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					setSvrTask.cancel(false);
					showToast(ctx.getString(R.string.pushservice_settags_failure));
				}
				if (currentAction == ACTION.DEL_TAG) {
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					delSvrTask.cancel(false);
					showToast(ctx.getString(R.string.pushservice_deltags_failure));
				}
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
		tags.clear();
		getTags(accounts);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			AlarmReceiver.mActivity = null;
			this.finish();
		}
		return true;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
			if (!isActionSuccess) {
				return;
			}
			if (isChecked) {
				startSvrTask = new StartPushServiceTask();
				startSvrTask.execute(new Object());
			} else {
				stopSvrTask = new StopPushServiceTask();
				stopSvrTask.execute(new Object());
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
			
			if (!isActionSuccess) {
				return;
			}
			if (!isChecked) {
				delSvrTask = new DelTagServiceTask();
				delSvrTask.execute(new Object());
			} else {
				setSvrTask = new SetTagServiceTask();
				setSvrTask.execute(new Object());
			}
			break;
		}
	}

	private void openProgressDialogForPushService(String title){
		pushServicePrg = ProgressDialog.show(ctx, "",title,  true, false);
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
	
	private enum ACTION {
		START_SERVICE,
		STOP_SERVICE,
		ADD_TAG,
		DEL_TAG
	}
	
	private ACTION currentAction;
	private boolean isActionSuccess = true;  // 操作是否成功 
	
	private enum PUSH_SERVICE_STATUS {
		INIT,	 // 初始
		WORKING, // 工作中
		STOP	 // 已停止
	}
	
	private PUSH_SERVICE_STATUS serviceStatus = PUSH_SERVICE_STATUS.INIT;
	private StartPushServiceTask startSvrTask;
	private StopPushServiceTask stopSvrTask;
	private SetTagServiceTask setSvrTask;
	private DelTagServiceTask delSvrTask;
	
	public class SetTagServiceTask extends AsyncTask<Object, Object, Boolean>{

		@Override
		protected void onPreExecute() {
			// 启动添加标签服务，同时显示加载框
			currentAction = ACTION.ADD_TAG;
			serviceStatus = PUSH_SERVICE_STATUS.INIT;
			PushManager.setTags(ctx, tags);
			openProgressDialogForPushService(ctx.getString(R.string.pushservice_alarmusr_settags));
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			// 等待服务启动完成
			while (serviceStatus != PUSH_SERVICE_STATUS.WORKING && !isCancelled());
			Boolean startSuccess = true;
			if (isCancelled()) {  // 被取消说明启动失败
				startSuccess = false; 
			}
			return startSuccess;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// 服务启动成功，onPostExecute正常调用 
			isActionSuccess = true;
			isAcc = true;
			saveSharedPreference();
			tvAlarmUserAccept.setText(getString(R.string.alarm_accept_open));
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}

		@Override
		protected void onCancelled(Boolean result) {// 服务启动失败，onPostExecute不被调用，onCancelled被调用 
			isActionSuccess = false;
			isAcc = false;
			saveSharedPreference();
			tvAlarmUserAccept.setText(getString(R.string.alarm_accept_off));
			// 操作失败的时候，相应的onCheckedChanged事件处理会被屏蔽，按钮回复原始状态
			csvAlarmUserAccept.setChecked(false); 
			isActionSuccess = true;
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}
	}
	
	public class DelTagServiceTask extends AsyncTask<Object, Object, Boolean>{

		@Override
		protected void onPreExecute() {
			// 启动添加标签服务，同时显示加载框
			currentAction = ACTION.DEL_TAG;
			serviceStatus = PUSH_SERVICE_STATUS.INIT;
			PushManager.delTags(ctx, tags);
			openProgressDialogForPushService(ctx.getString(R.string.pushservice_alarmusr_deltags));
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			// 等待服务启动完成
			while (serviceStatus != PUSH_SERVICE_STATUS.WORKING && !isCancelled());
			Boolean startSuccess = true;
			if (isCancelled()) {  // 被取消说明启动失败
				startSuccess = false; 
			}
			return startSuccess;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// 服务启动成功，onPostExecute正常调用 
			isActionSuccess = true;
			isAcc = false;
			saveSharedPreference();
			tvAlarmUserAccept.setText(getString(R.string.alarm_accept_off));
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}

		@Override
		protected void onCancelled(Boolean result) {
			// 服务启动失败，onPostExecute不被调用，onCancelled被调用 
			isAcc = true;
			saveSharedPreference();
			isActionSuccess = false;
			tvAlarmUserAccept.setText(getString(R.string.alarm_accept_open));
			// 操作失败的时候，相应的onCheckedChanged事件处理会被屏蔽，按钮回复原始状态
			csvAlarmUserAccept.setChecked(true); 
			isActionSuccess = true;
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}
	}
	
	public class StartPushServiceTask extends AsyncTask<Object, Object, Boolean> {
		
		@Override
		protected void onPreExecute() {
			// 启动推送服务，同时显示加载框
			currentAction = ACTION.START_SERVICE;
			serviceStatus = PUSH_SERVICE_STATUS.INIT;
			PushManager.startWork(ctx, PushConstants.LOGIN_TYPE_API_KEY,Utils.getMetaValue(ctx.getApplicationContext(), "api_key"));
			openProgressDialogForPushService(ctx.getString(R.string.system_setting_pushservice_openning));
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			// 等待服务启动完成
			while (serviceStatus != PUSH_SERVICE_STATUS.WORKING && !isCancelled());
			
			Boolean startSuccess = true;
			if (isCancelled()) {  // 被取消说明启动失败
				startSuccess = false; 
			}
			return startSuccess;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// 服务启动成功，onPostExecute正常调用 
			isAllAcc = true;
			saveSharedPreference();
			isActionSuccess = true;
			csvShake.setEnabled(true);
			csvSound.setEnabled(true);
			csvAlarmUserAccept.setEnabled(true);
			tvPush.setText(getString(R.string.notify_accept_open));
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}

		@Override
		protected void onCancelled(Boolean result) {
			// 服务启动失败，onPostExecute不被调用，onCancelled被调用 
			isAllAcc = false;
			saveSharedPreference();
			isActionSuccess = false;
			csvShake.setEnabled(false);
			csvSound.setEnabled(false);
			csvAlarmUserAccept.setEnabled(false);
			tvPush.setText(getString(R.string.notify_accept_off));
			// 操作失败的时候，相应的onCheckedChanged事件处理会被屏蔽，按钮回复原始状态
			csvPush.setChecked(false); 
			isActionSuccess = true;
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}
	}
	
	public class StopPushServiceTask extends AsyncTask<Object, Object, Boolean> {
		
		@Override
		protected void onPreExecute() {
			// 启动推送服务，同时显示加载框
			currentAction = ACTION.STOP_SERVICE;
			PushManager.stopWork(ctx);
			openProgressDialogForPushService(ctx.getString(R.string.system_setting_pushservice_closing));
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			// 等待服务关闭完成
			while (serviceStatus != PUSH_SERVICE_STATUS.STOP && !isCancelled());
			Boolean stopSuccess = true;
			if (isCancelled()) {  // 被取消说明关闭失败
				stopSuccess = false; 
			}
			return stopSuccess;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// 服务关闭成功，onPostExeceute正常调用
			isAllAcc = false;
			saveSharedPreference();
			isActionSuccess = true;
			csvShake.setEnabled(false);
			csvSound.setEnabled(false);
			csvAlarmUserAccept.setEnabled(false);
			tvPush.setText(getString(R.string.notify_accept_off));
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}

		@Override
		protected void onCancelled(Boolean result) {
			// 服务关闭失败，onPostExeceute不被调用，onCancelled被调用
			isAllAcc = true;
			saveSharedPreference();
			isActionSuccess = false;
			csvShake.setEnabled(true);
			csvSound.setEnabled(true);
			csvAlarmUserAccept.setEnabled(true);
			
			tvPush.setText(getString(R.string.notify_accept_open));
			// 操作失败的时候，相应的onCheckedChanged事件处理会被屏蔽，按钮回复原始状态
			csvPush.setChecked(true);
			isActionSuccess = true;
			
			if(pushServicePrg!=null && pushServicePrg.isShowing()){
				pushServicePrg.dismiss();
			}
		}
	}
	
	private PhoneUIListener listener;
	private boolean hasCalculateHeight = false;
	
	private final class PhoneUIListener implements OnGlobalLayoutListener{

		@Override
		public void onGlobalLayout() {
			if (!hasCalculateHeight) {
				hasCalculateHeight = true;
				int gtHeight = container_csvs1.getHeight();
				LayoutParams csvLayout1 = container_csvs1.getLayoutParams();
				csvLayout1.height = gtHeight;
				container_csvs1.setLayoutParams(csvLayout1);
				
				LayoutParams csvLayout2 = container_csvs1.getLayoutParams();
				csvLayout2.height = gtHeight;
				container_csvs2.setLayoutParams(csvLayout2);
				
				LayoutParams csvLayout3 = container_csvs3.getLayoutParams();
				csvLayout3.height = gtHeight;
				container_csvs3.setLayoutParams(csvLayout3);
				
				LayoutParams csvLayout4 = container_csvs4.getLayoutParams();
				csvLayout4.height = gtHeight;
				container_csvs4.setLayoutParams(csvLayout4);
				
				LayoutParams csvLayout5 = alarmUserContainer.getLayoutParams();
				csvLayout5.height = gtHeight;
				alarmUserContainer.setLayoutParams(csvLayout5);
				
				
				Log.i(TAG, "=====gtHeight====:"+gtHeight);
			}
		}
	}
}