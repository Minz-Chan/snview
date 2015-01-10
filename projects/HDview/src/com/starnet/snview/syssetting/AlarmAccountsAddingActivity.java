package com.starnet.snview.syssetting;

import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.ReadWriteXmlUtils;

public class AlarmAccountsAddingActivity extends BaseActivity {

	private Context ctx;
	private EditText userExt;
	private EditText pswdExt;
	private final int ADDINGTCODE = 0x0006;

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
				int index = checkIsNull();
				if (index == -1) {
					CloudAccount user = new CloudAccount();
					String userName = userExt.getText().toString();
					String password = pswdExt.getText().toString();
					user.setUsername(userName);
					user.setPassword(password);
					boolean isExist = checkIsExist(user);
					if (isExist) {
						jumpDialog(user);
					}else {
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
			}
		});
	}
	
	private void jumpDialog(CloudAccount ca){
		Builder builder = new Builder(ctx);
		builder.setTitle(R.string.system_setting_alarm_pushset_exist_cover);
		String ok = getString(R.string.system_setting_alarm_pushset_builer_identify_add_ok);
		String cancel = getString(R.string.system_setting_alarm_pushset_builer_identify_add_cance);
		builder.setNegativeButton(cancel, null);
		final CloudAccount cla = ca;
		builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent data = new Intent();
				data.putExtra("alarmUser", cla);
				data.putExtra("cover", true);
				setResult(ADDINGTCODE, data);
				AlarmAccountsAddingActivity.this.finish();
			}
		});
		
		builder.show();
	}
	
	private boolean checkIsExist(CloudAccount user){
		boolean isExist = false;
		List<CloudAccount>userList = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		if (userList!=null) {
			for (int i = 0; i < userList.size(); i++) {
				String uName = userList.get(i).getUsername();
				if (uName.equals(user.getUsername())) {
					isExist = true;
					break;
				}
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
	}

}
