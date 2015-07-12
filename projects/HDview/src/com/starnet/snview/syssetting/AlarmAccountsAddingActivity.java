package com.starnet.snview.syssetting;

import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.AlarmSettingUtils;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.MD5Utils;

public class AlarmAccountsAddingActivity extends BaseActivity {

	private Context ctx;
	private EditText userExt;
	private EditText pswdExt;
	private final int ADDINGTCODE = 0x0006;

	private int indexSame;
	private String tags;
	private List<String>tagList;//用于保存所有的列表
	private SharedPreferences sps;
	private List<AlarmUser>userList;
	private AlarmSettingUtils alarmSettingUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_accounts_adding_activity);
		initViews();
		setListeners();
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmAccountsAddingActivity.this.finish();
			}
		});

		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					int index = checkIsNull();
					if (index == -1) {
						AlarmUser user = new AlarmUser();
						String userName = userExt.getText().toString();
						String password = pswdExt.getText().toString();
						user.setUserName(userName);
						user.setPassword(password);
						boolean isExist = checkIsExist(user);
						if (isExist) {
							jumpDialog(user);//替代用户
						}else {
							//sps.edit().clear().commit();
							password = MD5Utils.createMD5(password);
							String tag = userName + password + "|"+userName.length();
							if (tags == null || tags.equals("")||tags.length()==0) {
								tags = tag;
							}else{
								tags = tags + "," + tag;
							}
							sps.edit().putString("tags", tags).commit();
							
							Intent intent = new Intent();
							intent.putExtra("cover", false);
							intent.putExtra("alarmUser", user);
							setResult(ADDINGTCODE, intent);
							AlarmAccountsAddingActivity.this.finish();
						}						
					} else if (index == 1) {
						showToast(getString(R.string.alarm_usernamenull));
					} else if (index == 2) {
						showToast(getString(R.string.alarm_passwordnull));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void jumpDialog(AlarmUser ca){
		Builder builder = new Builder(ctx);
		builder.setTitle(R.string.system_setting_alarm_pushset_exist_cover);
		String ok = getString(R.string.system_setting_alarm_pushset_builer_identify_add_ok);
		String cancel = getString(R.string.system_setting_alarm_pushset_builer_identify_add_cance);
		builder.setNegativeButton(cancel, null);
		final AlarmUser cla = ca;
		builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					String userName = cla.getUserName();
					String pswd = cla.getPassword();
					//sps.edit().clear().commit();
					pswd = MD5Utils.createMD5(pswd);//新的tag
					String temp = userName + pswd + "|" + userName.length();
					
					tagList.set(indexSame, temp);//替代旧的tag
					int sizes = tagList.size();
					String tempTags = "";
					for (int i = 0; i < sizes - 1; i++) {
						tempTags = tagList.get(i) + "," + tempTags;
					}
					tempTags = tempTags + tagList.get(sizes-1);
					sps.edit().putString("tags", tempTags).commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Intent data = new Intent();
				data.putExtra("alarmUser", cla);
				data.putExtra("cover", true);
				setResult(ADDINGTCODE, data);
				AlarmAccountsAddingActivity.this.finish();
			}
		});
		builder.show();
	}
	
	private CloudAccount existAccount;
	
	private boolean checkIsExist(AlarmUser user){
		boolean isExist = false;
		if (tags == null || tags.equals("")) {
			return false;
		}
		int userSize = userList.size();
		for (int i = 0; i < userSize; i++) {
			AlarmUser tempUser = userList.get(i);
			if (user.getUserName().equals(tempUser.getUserName())) {
				isExist = true;
				indexSame = i;
				break;
			}
		}
		return isExist;
	}

	private void showToast(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}

	protected int checkIsNull() {
		int index = -1;
		String userName = userExt.getText().toString().trim();
		if (userName == null || userName.equals("") || userName.equals(null)) {
			index = 1;
			return index;
		}
		String password = pswdExt.getText().toString().trim();
		if (password == null || password.equals("") || password.equals(null)) {
			index = 2;
			return index;
		}
		return index;
	}

	private void initViews() {
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		ctx = AlarmAccountsAddingActivity.this;
		userExt = (EditText) findViewById(R.id.username_edittext);
		pswdExt = (EditText) findViewById(R.id.password_edittext);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setTitleViewText(getString(R.string.system_setting_pushset_alarmuser));
		
		sps = ctx.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE);
		alarmSettingUtils = AlarmSettingUtils.getInstance();
//		alarmSettingUtils.setContext(ctx);
		
		tags = sps.getString("tags", "");
		userList = alarmSettingUtils.getAlarmUsers();
		tagList = alarmSettingUtils.getAlarmUserTagsWithLength();
	}
}