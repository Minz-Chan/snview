package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.Utils;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class AlarmPushManagerActivity extends BaseActivity {

	private Context ctx;

	boolean isAcc;
	boolean isShake;
	boolean isSound;
	boolean isAllAcc;
	private Button clearAlarmInfBtn;
	private HashMap<String, Object> map;
	private final int REQUESTCODE = 0x0001;
	private CornerListView alarmUserListView;// 报警账户的listView
	private AlarmUserAdapter alarmUserAdapter;
	private List<HashMap<String, Object>> list;
	private CornerListView alarmNotifyListView;// 报警通知（接收、声音和震动的设置）
	private AalarmNotifyAdapter alarmNotifyAdapter;
	private List<HashMap<String, Object>> settingList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_push_manager_activity);

		intialViews();
		setListeners();

	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettingsToSharedPreference();
				saveStarnetAndAlarmPushAccounts();
				AlarmPushManagerActivity.this.finish();
			}
		});

		clearAlarmInfBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				jumpClearDialog();
			}
		});

		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettingsToSharedPreference();
				saveStarnetAndAlarmPushAccounts();
				AlarmPushManagerActivity.this.finish();
			}
		});

		alarmUserListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 1) {
					Intent intent = new Intent();
					intent.setClass(ctx, AlarmAccountsPreviewActivity.class);
					startActivityForResult(intent, REQUESTCODE);
				}
			}
		});
	}

	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";
	private List<CloudAccount> ps;
	private List<CloudAccount> ca;

	/** 保存用户的设置选项 **/
	private void saveSettingsToSharedPreference() {
		isAcc = alarmUserAdapter.isClickFlag();
		isShake = alarmNotifyAdapter.isClickFlagSha();
		isSound = alarmNotifyAdapter.isClickFlagSou();
		isAllAcc = alarmNotifyAdapter.isClickFlagAcc();

		Editor edt = ctx.getSharedPreferences("ALARM_PUSHSET_FILE",
				Context.MODE_PRIVATE).edit();
		edt.putBoolean("isAccept", isAcc);
		edt.putBoolean("isShake", isShake);
		edt.putBoolean("isSound", isSound);
		edt.putBoolean("isAllAccept", isAllAcc);
		edt.commit();

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
				// boolean isSuc = ReadWriteXmlUtils.clearAllAlarmInfo();
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
		super.hideExtendButton();
		super.getToolbarContainer().setVisibility(View.GONE);
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.system_setting_alarm_pushset));

		ctx = AlarmPushManagerActivity.this;
		clearAlarmInfBtn = (Button) findViewById(R.id.clearAlarmInfBtn);
		alarmUserListView = (CornerListView) findViewById(R.id.alarmUserListView);
		alarmNotifyListView = (CornerListView) findViewById(R.id.alarmNotifyListView);

		sp = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", 0);
		isAcc = sp.getBoolean("isAccept", true);
		isShake = sp.getBoolean("isShake", true);
		isSound = sp.getBoolean("isSound", true);
		isAllAcc = sp.getBoolean("isAllAccept", true);

		setAalarmNotifyAdapter();
		setAlarmUserAdapter(isAcc);

	}

	SharedPreferences sp;
	private String api;

	/** 保存设置选项 **/
	private void saveStarnetAndAlarmPushAccounts() {
		if (!isAllAcc) {
			PushManager.stopWork(ctx.getApplicationContext());// 关闭百度推送服务
		} else {
			api = Utils.getMetaValue(ctx.getApplicationContext(), "api_key");
			int login = PushConstants.LOGIN_TYPE_API_KEY;
			PushManager.startWork(getApplicationContext(), login, api);
			if (isAcc) {
				try {
					saveAllAccounts();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					delPushUserAndSaveStarnetUser();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void saveAllAccounts() throws Exception {
		List<String> rTags = new ArrayList<String>();

		ps = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		ca = ReadWriteXmlUtils.getCloudAccountList(filePath);// 获取星云平台账户

		if (ps != null) {
			for (int i = 0; i < ps.size(); i++) {
				CloudAccount pA = ps.get(i);
				String uName = pA.getUsername();
				String pswd = MD5Utils.createMD5(pA.getPassword());
				String tag = uName + "" + pswd;
				rTags.add(tag);
			}
		}

		if (ca != null) {
			for (int i = 0; i < ca.size(); i++) {
				CloudAccount cA = ca.get(i);
				String uName = cA.getUsername();
				String pswd = MD5Utils.createMD5(cA.getPassword());
				String tag = uName + "" + pswd;
				rTags.add(tag);
			}
			PushManager.setTags(ctx.getApplicationContext(), rTags);// 注册星云平台账户
		}
	}

	/** 删除推送账户，注册星云平台账户 **/
	private void delPushUserAndSaveStarnetUser() throws Exception {
		List<String> dTags = new ArrayList<String>();
		List<String> rTags = new ArrayList<String>();

		ps = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		ca = ReadWriteXmlUtils.getCloudAccountList(filePath);// 获取星云平台账户

		if (ps != null) {
			for (int i = 0; i < ps.size(); i++) {
				CloudAccount pA = ps.get(i);
				String uName = pA.getUsername();
				String pswd = MD5Utils.createMD5(pA.getPassword());
				String tag = uName + "" + pswd;
				dTags.add(tag);
			}
			PushManager.delTags(ctx.getApplicationContext(), dTags);// 删除推送账户
		}

		if (ca != null) {
			for (int i = 0; i < ca.size(); i++) {
				CloudAccount cA = ca.get(i);
				String uName = cA.getUsername();
				String pswd = MD5Utils.createMD5(cA.getPassword());
				String tag = uName + "" + pswd;
				rTags.add(tag);
			}
			PushManager.setTags(ctx.getApplicationContext(), rTags);// 注册星云平台账户
		}
	}

	private void setAlarmUserAdapter(boolean isAccept) {
		list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map1 = new HashMap<String, Object>();
		map1.put("text", getString(R.string.system_setting_alarmuser_accept));
		list.add(map1);

		HashMap<String, Object> map2 = new HashMap<String, Object>();
		List<CloudAccount> accounts = new ArrayList<CloudAccount>();
		accounts = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		String content = "";
		if (accounts == null || (accounts != null && accounts.size() == 0)) {
			content = getString(R.string.system_setting_alarmuser_null);
		} else if (accounts != null && accounts.size() > 0) {
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
		map2.put("text", content);
		list.add(map2);
		alarmUserAdapter = new AlarmUserAdapter(this, list, isAccept);
		alarmUserListView.setAdapter(alarmUserAdapter);
	}

	private void setAalarmNotifyAdapter() {
		settingList = new ArrayList<HashMap<String, Object>>();
		map = new HashMap<String, Object>();
		map.put("text", getString(R.string.system_setting_alarminfo_accept));
		settingList.add(map);
		map = new HashMap<String, Object>();
		map.put("text",
				getString(R.string.system_setting_alarminfo_remind_shake));
		settingList.add(map);
		map = new HashMap<String, Object>();
		map.put("text",
				getString(R.string.system_setting_alarminfo_remind_sound));
		settingList.add(map);
		alarmNotifyAdapter = new AalarmNotifyAdapter(this, settingList,
				isAllAcc, isShake, isSound);
		alarmNotifyListView.setAdapter(alarmNotifyAdapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		HashMap<String, Object> map2 = new HashMap<String, Object>();
		List<CloudAccount> accounts = ReadWriteXmlUtils
				.getAlarmPushUsersFromXML();
		String content = "";
		if (accounts == null || (accounts != null && accounts.size() == 0)) {
			content = getString(R.string.system_setting_alarmuser_null);
		} else if (accounts != null && accounts.size() > 0) {
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
		map2.put("text", content);
		list.set(1, map2);
		alarmUserAdapter = new AlarmUserAdapter(this, list, isAcc);
		alarmUserListView.setAdapter(alarmUserAdapter);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			saveSettingsToSharedPreference();
			saveStarnetAndAlarmPushAccounts();
			this.finish();
		}
		return true;
	}

}