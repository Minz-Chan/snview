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

public class AlarmAccountsEditingActivity extends BaseActivity {

	//“why********|3”,“zhao********|4”,“hongxu********|6”
	
	private int pos;
	private Context ctx;
	private EditText userEdt;
	private EditText pswdEdt;
	private AlarmUser originCA;
	private final int REQUESTCODE_EDIT = 0x0004;
	
	private List<String>tagList;
	private SharedPreferences sps;
	private List<AlarmUser>alarmUserList;
	private AlarmSettingUtils alarmSettingUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_accounts_editing_activity);

		initViews();
		setListeners();
	}

	private void initViews() {
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		ctx = AlarmAccountsEditingActivity.this;
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.system_setting_preview_user_info));

		Intent intent = getIntent();
		pos = intent.getIntExtra("pos", 0);
		originCA = (AlarmUser) intent.getParcelableExtra("cla");

		userEdt = (EditText) findViewById(R.id.user_edt);
		pswdEdt = (EditText) findViewById(R.id.password_edt);
		userEdt.setText(originCA.getUserName());
		
		sps = ctx.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE);
		alarmSettingUtils = AlarmSettingUtils.getInstance();
//		alarmSettingUtils.setContext(ctx);
		tagList = alarmSettingUtils.getAlarmUserTagsWithLength();
		alarmUserList = alarmSettingUtils.getAlarmUsers();

	}


	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmAccountsEditingActivity.this.finish();
			}
		});

		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = checkIsNull();
				if (index == -1) {
					AlarmUser aA = getCloudAccount();
					boolean isSame = checkISSame(originCA, aA);// 检测用户是否发生改变
					if (isSame) {
						AlarmAccountsEditingActivity.this.finish();
					} else {// 用户信息发生改变的时候，要检测是用户名发生改变还是用户密码发生了改变
						int inde = checkPswdOrUsernameChange(originCA, aA);// 1，代表用户名发生改变；2，代表密码发生改变
						if (inde==1) {// 1代表用户名发生改变
							if (isExistUser(aA)) {
								jumpCoverDialog(aA);
							} else {// 不存在与该用户同名的用户，直接替换原来的用户
								putTagsToSharedPreference(aA);//先删除,后保存
								Intent intent = new Intent();
								intent.putExtra("position", pos);
								intent.putExtra("claa", aA);
								setResult(REQUESTCODE_EDIT, intent);
								AlarmAccountsEditingActivity.this.finish();
							}
						}else if (inde==2) {
							putTagsToSharedPreference(aA);
							Intent intent = new Intent();
							intent.putExtra("position", pos);
							intent.putExtra("claa", aA);
							setResult(REQUESTCODE_EDIT, intent);
							AlarmAccountsEditingActivity.this.finish();
						}
					}
				} else if (index == 1) {
					showToast(getString(R.string.alarm_usernamenull));
				} else if (index == 2) {
					showToast(getString(R.string.alarm_passwordnull));
				}
			}

			private int checkPswdOrUsernameChange(AlarmUser originCA, AlarmUser aA) {
				int index = -1;
				if (!originCA.getUserName().equals(aA.getUserName())) {
					index = 1;
				} else if (!originCA.getPassword().equals(aA.getPassword())) {
					index = 2;
				}
				return index;
			}

			private boolean checkISSame(AlarmUser orCA, AlarmUser aA) {
				if (orCA.getUserName().equals(aA.getUserName()) && (orCA.getPassword().equals(aA.getPassword()))) {
					return true;
				} else {
					return false;
				}
			}
			
		});
	}
	
	private void jumpCoverDialog(AlarmUser ca) {
		Builder builder = new Builder(ctx);
		builder.setTitle(R.string.system_setting_alarm_pushset_exist_cover);
		String ok = getString(R.string.system_setting_alarm_pushset_builer_identify_add_ok);
		String cancel = getString(R.string.system_setting_alarm_pushset_builer_identify_add_cance);
		final AlarmUser cla = ca;
		builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//先删除原来的用户
				try {
					replaceOriginTagWithNewTag(cla);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton(cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlarmAccountsEditingActivity.this.finish();
					}
				});
		builder.show();
	}

private void replaceOriginTagWithNewTag(AlarmUser alarmUser) throws Exception{
		
		String oldUserName = originCA.getUserName();
		String oldPassword = originCA.getPassword();
		String oldTag = oldUserName + oldPassword + "|" + oldUserName.length();
		tagList.remove(oldTag);//移除单击时的报警账户
		
		String newUserName = alarmUser.getUserName();
		String newPassword = alarmUser.getPassword();
		newPassword = MD5Utils.createMD5(newPassword);
		String newTag = newUserName + newPassword + "|"+ newUserName.length();
		
		int size = tagList.size();
		for (int i = 0; i < size; i++) {
			String tag = tagList.get(i);
			String tempTag[] = tag.split("\\|");			
			int userNameLen = Integer.valueOf(tempTag[1]);
			String userName = tempTag[0].substring(0, userNameLen);
			if (userName.equals(alarmUser.getUserName())) {
				tagList.set(i, newTag);
				break;
			}
		}
		
//		ctx.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE).edit().clear().commit();
		alarmSettingUtils.writeAlarmUserToXml(tagList);
		
		Intent data = new Intent();
		data.putExtra("flag", "cover");
		data.putExtra("claa", alarmUser);
		data.putExtra("cover", true);
		data.putExtra("position", pos);
		setResult(REQUESTCODE_EDIT, data);
		AlarmAccountsEditingActivity.this.finish();
	}
	
	/**替换原来的用户，先删除后保存**/
	private void putTagsToSharedPreference(AlarmUser aA) {
		try {
			//sps.edit().clear().commit();
			///旧的标签
			String oldUsername = originCA.getUserName();
			String oldePassword = originCA.getPassword();
			String oldTagPart = oldUsername + oldePassword;
			///新的标签
			String userName = aA.getUserName();
			String password = aA.getPassword();
			password = MD5Utils.createMD5(password);
			String newTag = userName + password + "|" + userName.length();//将要被注册
			for (int i = 0; i < tagList.size(); i++) {
				if (tagList.get(i).contains(oldTagPart)) {
					tagList.set(i, newTag);
					break;
				}
			}
			//重新组织tags
			alarmSettingUtils.writeAlarmUserToXml(tagList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	protected int checkIsNull() {
		int index = -1;
		String userName = userEdt.getText().toString().trim();
		if (userName == null || userName.equals("") || userName.equals(null)) {
			index = 1;
			return index;
		}
		String password = pswdEdt.getText().toString().trim();
		if (password == null || password.equals("") || password.equals(null)) {
			index = 2;
			return index;
		}
		return index;
	}

	private void showToast(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}
	
	private boolean isExistUser(AlarmUser aA) {
		boolean isExist = false;
		int size = alarmUserList.size();
		for (int i = 0; i < size; i++) {
			AlarmUser tA = alarmUserList.get(i);
			if (aA.getUserName().equals(tA.getUserName())) {
				isExist = true;
				break;
			}
		}
		return isExist;
	}

	/** 获取增加标签的账户 **/
	private AlarmUser getCloudAccount() {
		AlarmUser account = new AlarmUser();
		String pswd = pswdEdt.getText().toString();
		String uNam = userEdt.getText().toString();
		account.setPassword(pswd);
		account.setUserName(uNam);
		return account;
	}
}
