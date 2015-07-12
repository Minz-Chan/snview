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
import com.starnet.snview.alarmmanager.AlarmSettingUtils;
import com.starnet.snview.alarmmanager.Utils;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.componet.switchbutton.CheckSwitchButton;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;
import com.starnet.snview.util.StringUtils;

@SuppressLint("HandlerLeak")
public class AnotherAlarmPushManagerActivity extends BaseActivity implements
		OnCheckedChangeListener, OnClickListener {
	protected static final String TAG = "AnotherAlarmPushManagerActivity";
	
	private Context ctx;
	public static final int REQUESTCODE = 0x0001;
	
	private boolean isGlobalAlarmOpen;
	private boolean isShakeOpen;
	private boolean isSoundOpen;
	private boolean isUserAlarmOpen;
	
	private CheckSwitchButton globalAlarmButton;
	private CheckSwitchButton shakeButton;
	private CheckSwitchButton soundButton;
	private CheckSwitchButton userAlarmButton;
	private Button clearAlarmsButton;
	
	private TextView txtGlobalAlarm;
	private TextView txtShake;
	private TextView txtSound;
	private TextView txtUserAlarm;
	private TextView txtAlarmUsers;	// 显示推送账户列表
	private ProgressDialog waittingDialog;
	private LinearLayout layout0;
	private RelativeLayout layout1;
	private RelativeLayout layout2;
	private RelativeLayout layout3;
	private RelativeLayout layout4;
	private RelativeLayout alarmUserContainer;
	
	private AlarmSettingUtils alarmSettingUtils;
	private List<String> tags = new ArrayList<String>();//保存从本地的报警账户标签
	private List<AlarmUser> userList = new ArrayList<AlarmUser>();//报警账户列表
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_layout_notify_user_activity);
		
		AlarmSettingUtils.getInstance().init(this);
		AlarmSettingUtils.getInstance().attachHandler(handler);
		
		intialViews();
		setListeners();
	}

	private void setListeners() {
		globalAlarmButton.setOnCheckedChangeListener(this);
		shakeButton.setOnCheckedChangeListener(this);
		soundButton.setOnCheckedChangeListener(this);
		userAlarmButton.setOnCheckedChangeListener(this);
	
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmReceiver.mActivity = null;
				finish();
			}
		});
		clearAlarmsButton.setOnClickListener(this);
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
					showMessage(getString(R.string.system_setting_pushset_clear_alarm_suc));
				} else {
					showMessage(getString(R.string.system_setting_pushset_clear_alarm_fai));
				}
			}
		});
		builder.show();
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
		
		globalAlarmButton = (CheckSwitchButton) findViewById(R.id.global_alarm);
		shakeButton = (CheckSwitchButton) findViewById(R.id.alarm_shake);
		soundButton = (CheckSwitchButton) findViewById(R.id.alarm_sound);
		userAlarmButton = (CheckSwitchButton) findViewById(R.id.alarm_user);

		txtGlobalAlarm = (TextView) findViewById(R.id.tv_push_accept);
		txtShake = (TextView) findViewById(R.id.tv_push_shake);
		txtSound = (TextView) findViewById(R.id.tv_push_sound);
		layout0 = (LinearLayout) findViewById(R.id.container_layout);
		txtUserAlarm = (TextView) findViewById(R.id.tv_alarmuser_accept);

		txtAlarmUsers = (TextView) findViewById(R.id.tv_alarmusers);
		clearAlarmsButton = (Button) findViewById(R.id.clearAlarmInfBtn);
		
		layout1 = (RelativeLayout) findViewById(R.id.container_csvs1);
		layout2 = (RelativeLayout) findViewById(R.id.container_csvs2);
		layout3 = (RelativeLayout) findViewById(R.id.container_csvs3);
		layout4 = (RelativeLayout) findViewById(R.id.container_csvs4);
		alarmUserContainer = (RelativeLayout) findViewById(R.id.alarmUserContainer);
		
		SharedPreferences sharedPref = ctx.getSharedPreferences(
				AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE);
		isGlobalAlarmOpen = sharedPref.getBoolean(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, true);
		isShakeOpen = sharedPref.getBoolean(AlarmSettingUtils.ALARM_CONFIG_SHAKE, true);
		isSoundOpen = sharedPref.getBoolean(AlarmSettingUtils.ALARM_CONFIG_SOUND, true);
		isUserAlarmOpen = sharedPref.getBoolean(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, true);

		if (isGlobalAlarmOpen) {
			globalAlarmButton.setChecked(true);
			txtGlobalAlarm.setText(getString(R.string.notify_accept_open));
		} else {
			txtGlobalAlarm.setText(getString(R.string.notify_accept_off));
			globalAlarmButton.setChecked(false);
			shakeButton.setEnabled(false);
			soundButton.setEnabled(false);
			userAlarmButton.setEnabled(false);
		}
		if (isShakeOpen) {
			shakeButton.setChecked(true);
			txtShake.setText(getString(R.string.remind_shake_open));
		} else {
			shakeButton.setChecked(false);
			txtShake.setText(getString(R.string.remind_shake_off));
		}
		if (isSoundOpen) {
			soundButton.setChecked(true);
			txtSound.setText(getString(R.string.remind_sound_open));
		} else {
			soundButton.setChecked(false);
			txtSound.setText(getString(R.string.remind_sound_off));
		}
		if (isUserAlarmOpen) {
			userAlarmButton.setChecked(true);
			txtUserAlarm.setText(getString(R.string.alarm_accept_open));
		} else {
			userAlarmButton.setChecked(false);
			txtUserAlarm.setText(getString(R.string.alarm_accept_off));
		}
		
		listener = new PhoneUIListener();
		layout0.getViewTreeObserver().addOnGlobalLayoutListener(listener);
		
		alarmSettingUtils = AlarmSettingUtils.getInstance();
//		alarmSettingUtils.setContext(ctx);
		String content = "";
		//使用SharedPreference去维护报警账户信息
		String tagString = ctx.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE).getString("tags", "");
		if(tagString==null || tagString.equals("") || tagString.length()==0){
			content = getString(R.string.pushservice_alarmusr_null);
		}else{
			content = getTagsContent(content, tagString);
			if (content.length() >= 18) {
				content = content.substring(0, 18) + "...";
			}
		}
		txtAlarmUsers.setText(content);
		alarmSettingUtils.writeAlarmUserToXml(tags);// 同步至SharedPreference配置中
		
	}
	
	private String getTagsContent(String content, String tagString) {
		if (tagString == null || tagString.equals("") || tagString.length() == 0) { }else {
			String []tempTag = tagString.split(",");
			int tempTagLength = tempTag.length;
			for (int i = 0; i < tempTagLength; i++) {
				
				tags.add(tempTag[i]);
				
				String []tag = tempTag[i].split("\\|");
				
				int length = Integer.valueOf(tag[1]);
				AlarmUser user = new AlarmUser();
				String userName = tag[0].substring(0, length);
				String passWord = tag[0].substring(length);
				user.setUserName(userName);
				user.setPassword(passWord);
				userList.add(user);
				if (i == tempTag.length-1) {
					content += userName;
				}else{
					content += (userName+",");
				}
			}
		}
		return content;
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			AlarmReceiver.mActivity = null;
			this.finish();
		}
		return true;
	}
	
	// 提示滑动开关操作是否来自用户手动操作
	private boolean isUserManul = true;
	
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 取消加载框
			dismissWaittingDialog();
			
			switch (msg.what) {
			case AlarmReceiver.SERVICE_RSP_START:
				// 如果服务启动成功，使能其他按钮并提示成功；否则，复位按钮状态，并提示失败
				if (msg.arg1 == AlarmReceiver.SUCCESS) {
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, true);
					soundButton.setEnabled(true);
					shakeButton.setEnabled(true);
					userAlarmButton.setEnabled(true);
					showMessage("推送服务启动成功");
				} else {
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, false);
					restoreButtonState(globalAlarmButton);
					showMessage("推送服务启动失败");
				}
				break;
			case AlarmReceiver.SERVICE_RSP_STOP:
				// 如果服务关闭成功，禁用其他按钮并提示已关闭；否则，提示关闭失败
				if (msg.arg1 == AlarmReceiver.SUCCESS) {
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, false);
					soundButton.setEnabled(false);
					shakeButton.setEnabled(false);
					userAlarmButton.setEnabled(false);
					showMessage("推送服务已关闭");
				} else {
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, true);
					restoreButtonState(globalAlarmButton);
					showMessage("推送服务关闭失败");
				}
			case AlarmReceiver.SERVICE_RSP_SET_TAG:
				// 如果报警接收开启成功，不作提示；开启失败，复位滑动开关并提示
				if (msg.arg1 == AlarmReceiver.SUCCESS) {
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, true);
				} else if (msg.arg1 == AlarmReceiver.FAILURE) {
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, false);
					restoreButtonState(userAlarmButton);
					showMessage("报警账户接收开启失败");
				} else {
					// TODO 部分tag设置居功
				}
				break;
			case AlarmReceiver.SERVICE_RSP_DEL_TAG:
				// 如果报警接收关闭成功，不作提示；开启失败，复位滑动开关并提示
				if (msg.arg1 == AlarmReceiver.SUCCESS) {
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, false);
				} else if (msg.arg1 == AlarmReceiver.FAILURE) {
					// FIXME 报警账户接收开关时，若即有注册，又有删除，如何处理？？
					saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, true);
					restoreButtonState(userAlarmButton);	
					showMessage("报警账户接收关闭失败");
				} else {
					// TODO 部分tag删除居功
				}
				break;
			case AlarmReceiver.SERVICE_RSP_NULL_ALARM_TAGLIST:
				if (userAlarmButton.isChecked()) {
					isUserManul = false;
					userAlarmButton.setChecked(false);
					showMessage("报警账户列表为空，请先添加报警账户！");
				}
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.global_alarm:
			globalAlarmButtonClick(buttonView, isChecked);
			break;
		case R.id.alarm_shake:
			shakeButtonClick(buttonView, isChecked);
			break;
		case R.id.alarm_sound:
			soundButtonClick(buttonView, isChecked);
			break;
		case R.id.alarm_user:
			userAlarmButtonClick(buttonView, isChecked);
			break;
		}
		
		// 无特殊 设置，正常状态为true，表征操作是用户行为
		isUserManul = true; 
	}
	
	/*
	 * 全局报警推送开关点击
	 */
	private void globalAlarmButtonClick(CompoundButton buttonView, boolean isChecked) {
		// 如果网络连通，正常执行流程；否则，复位滑动开关状态
		if (isNetworkAvalible()) {
			if (isChecked) {
				PushManager.startWork(ctx, PushConstants.LOGIN_TYPE_API_KEY,
						Utils.getMetaValue(ctx.getApplicationContext(),
								"api_key"));
				showWaittingDialog("报警推送服务启动中...");
			} else {
				if (isUserManul) {
					// 关闭时若服务不连通，可能导致onUnbind()不被回调，引起加载框无法消失
					if (PushManager.isConnected(ctx)) {
						PushManager.stopWork(ctx);
						showWaittingDialog("报警推送服务关闭中...");
					} else {
						if (isUserManul) {
							restoreButtonState((CheckSwitchButton)buttonView);   
							showMessage("暂时无法关闭报警推送服务，请稍后再试！");
						} else {
							// 复位操作，不作处理
						}
					}
				} else {
					// 复位操作，不处理
				}
			}
			
		} else {
			if (isUserManul) {
				restoreButtonState((CheckSwitchButton)buttonView);  
				showMessage("当前网络不可用，请检查网络设置");
			} else {
				// 复位操作，不处理
			}
		}
	}
	
	/*
	 * 震动开关点击
	 */
	private void shakeButtonClick(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			txtShake.setText(getString(R.string.remind_shake_open));
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 50, 200, 50, 200 };
			vibrator.vibrate(pattern, -1);
		}else{
			txtShake.setText(getString(R.string.remind_shake_off));
		}
		saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_SHAKE, isChecked);
	}
	
	/*
	 * 声音开关点击
	 */
	private void soundButtonClick(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			txtSound.setText(getString(R.string.remind_sound_open));
			new Thread(new Runnable() {
				@Override
				public void run() {
					SnapshotSound s = new SnapshotSound(ctx);
					s.playPushSetSound();
				}
			}).start();
		}else{
			txtSound.setText(getString(R.string.remind_sound_off));
		}
		saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_SOUND, isChecked);
	}
	
	/*
	 * 报警账户开关点击
	 */
	private void userAlarmButtonClick(CompoundButton buttonView, boolean isChecked) {
		saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, isChecked);
		
		// 如果非用户手动操作，不作处理
		if (!isUserManul) {
			return;
		}
		
		// 如果网络连通，且全局推送开关开启，则触 发tag的注册或删除
		if (isNetworkAvalible()) {
			if (globalAlarmButton.isChecked()) {
				// 若此时服务不连通，可能导致onSetTags/onDelTags无法被间接回调，导致加载框无法消失，需作处理
				if (PushManager.isConnected(ctx)) {
					PushManager.listTags(ctx);  // onListTags()被回调
					showWaittingDialog(isChecked ? "报警账户接收开启中..." : "报警账户接收关闭中...");
				} else {
					if (isUserManul) {
						restoreButtonState((CheckSwitchButton)buttonView); 
						saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, !isChecked);
						showMessage("暂时无法启动报警账户推送接收，请稍后再试！");
					} else {
						// 复位操作调用，不作处理
					}
				}
			} else {
				// 推送服务全局开关关闭，不作处理
			}
		} else {
			if (isUserManul) {
				restoreButtonState((CheckSwitchButton)buttonView);
				saveAlarmConfig(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, !isChecked);
				showMessage("当前网络不可用，请检查网络设置");
			} else {
				// 复位操作调用，不作处理
			}
		}
	}

	/*
	 * 还原开关状态（若原先为开启状态，则置回关闭状态；若碑为关闭状态，则置为开启状态）
	 */
	private void restoreButtonState(CompoundButton cb) {
		isUserManul = false;
		if (cb != null) {
			cb.toggle();
		}
	}
	
	/*
	 * 网络是否可用
	 */
	private boolean isNetworkAvalible() {
		return NetWorkUtils.checkNetConnection(ctx);
	}

	private void showWaittingDialog(String msg){
		waittingDialog = ProgressDialog.show(ctx, "", msg,  true, false);
	}
	
	private void dismissWaittingDialog() {
		if (waittingDialog != null && waittingDialog.isShowing()) {
			waittingDialog.dismiss();
		}
	}
	
	private void showMessage(String s) {
		Toast.makeText(ctx, s, Toast.LENGTH_LONG).show();
	}
	
	private void saveAlarmConfig(String key, boolean value) {
		if (StringUtils.isEquals(key, AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM)) {
			txtGlobalAlarm.setText(value ? 
					getString(R.string.notify_accept_open) : getString(R.string.notify_accept_off));
		} if (StringUtils.isEquals(key, AlarmSettingUtils.ALARM_CONFIG_SHAKE)) {
			txtShake.setText(value ? 
					getString(R.string.remind_shake_open) : getString(R.string.remind_shake_off));
		} if (StringUtils.isEquals(key, AlarmSettingUtils.ALARM_CONFIG_SOUND)) {
			txtSound.setText(value ? 
					getString(R.string.remind_sound_open) : getString(R.string.remind_sound_off));
		} if (StringUtils.isEquals(key, AlarmSettingUtils.ALARM_CONFIG_USER_ALARM)) {
			txtUserAlarm.setText(value ? 
					getString(R.string.alarm_accept_open) : getString(R.string.alarm_accept_off));
		}
		
		SharedPreferences config = ctx.getSharedPreferences(
				AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE);
		Editor editor = config.edit();
		editor.putBoolean(key, value);
		editor.commit();
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String content = "";
		String tagString = ctx.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE).getString("tags", "");
		if(tagString == null || tagString.equals("") || tagString.length()==0){
			content = getString(R.string.pushservice_alarmusr_null);
		}else{
			content = getTagsContent(content, tagString);
			if (content.length() >= 18) {
				content = content.substring(0, 18) + "...";
			}
		}
		txtAlarmUsers.setText(content);
		
		AlarmSettingUtils alarmSetting = AlarmSettingUtils.getInstance();
		// 如果全局推送开关和报警账户推送开关都开启，则触发tag进行注册或删除
		if (alarmSetting.isPushOpen() && alarmSetting.isUserAlarmOpen()) {
			PushManager.listTags(ctx);
		}
	}

	
	private PhoneUIListener listener;
	private boolean hasCalculateHeight = false;
	
	private final class PhoneUIListener implements OnGlobalLayoutListener{

		@Override
		public void onGlobalLayout() {
			if (!hasCalculateHeight) {
				hasCalculateHeight = true;
				int gtHeight = layout1.getHeight();
				LayoutParams csvLayout1 = layout1.getLayoutParams();
				csvLayout1.height = gtHeight;
				layout1.setLayoutParams(csvLayout1);
				
				LayoutParams csvLayout2 = layout1.getLayoutParams();
				csvLayout2.height = gtHeight;
				layout2.setLayoutParams(csvLayout2);
				
				LayoutParams csvLayout3 = layout3.getLayoutParams();
				csvLayout3.height = gtHeight;
				layout3.setLayoutParams(csvLayout3);
				
				LayoutParams csvLayout4 = layout4.getLayoutParams();
				csvLayout4.height = gtHeight;
				layout4.setLayoutParams(csvLayout4);
				
				LayoutParams csvLayout5 = alarmUserContainer.getLayoutParams();
				csvLayout5.height = gtHeight;
				alarmUserContainer.setLayoutParams(csvLayout5);
				
				
				Log.i(TAG, "=====gtHeight====:"+gtHeight);
			}
		}
	}
}