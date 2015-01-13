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

public class AlarmAccountsEditingActivity extends BaseActivity {

	private int pos;
	private Context ctx;
	private EditText userEdt;
	private EditText pswdEdt;
	private CloudAccount originCA;
	private List<CloudAccount> mList;
	private final int REQUESTCODE_EDIT = 0x0004;

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
		originCA = (CloudAccount) intent.getSerializableExtra("cla");

		userEdt = (EditText) findViewById(R.id.user_edt);
		pswdEdt = (EditText) findViewById(R.id.password_edt);

		mList = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		showContent();

	}

	private void showContent() {
		userEdt.setText(originCA.getUsername());
		pswdEdt.setText(originCA.getPassword());
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
					CloudAccount aA = getCloudAccount();
					boolean isSame = checkISSame(originCA, aA);// 检测用户是否发生改变
					if (isSame) {
						AlarmAccountsEditingActivity.this.finish();
					} else {// 用户信息发生改变的时候，要检测是用户名发生改变还是用户密码发生了改变
						int inde = checkPswdOrUsernameChange(originCA, aA);// 1，代表用户名发生改变；2，代表密码发生改变
						if (inde==1) {// 1代表用户名发生改变
							if (isExistUser(aA)) {
								jumpCoverDialog(aA);
							} else {// 直接替换原来的用户
								ReadWriteXmlUtils.replaceAlarmPushUserToXML(aA, pos);
								Intent intent = new Intent();
								intent.putExtra("position", pos);
								intent.putExtra("claa", aA);
								setResult(REQUESTCODE_EDIT, intent);
								AlarmAccountsEditingActivity.this.finish();
							}
						}else if (inde==2) {
							ReadWriteXmlUtils.replaceAlarmPushUserToXML(aA, pos);
							Intent intent = new Intent();
							intent.putExtra("position", pos);
							intent.putExtra("claa", aA);
							setResult(REQUESTCODE_EDIT, intent);
							AlarmAccountsEditingActivity.this.finish();
						}
//						if (isExistUser(aA)) {
//							jumpCoverDialog(aA);
//						} else {// 直接替换原来的用户
//							ReadWriteXmlUtils.replaceAlarmPushUserToXML(aA, pos);
//							Intent intent = new Intent();
//							intent.putExtra("position", pos);
//							intent.putExtra("claa", aA);
//							setResult(REQUESTCODE_EDIT, intent);
//							AlarmAccountsEditingActivity.this.finish();
//						}
					}
				} else if (index == 1) {
					showToast(getString(R.string.alarm_usernamenull));
				} else if (index == 2) {
					showToast(getString(R.string.alarm_passwordnull));
				}
			}

			private int checkPswdOrUsernameChange(CloudAccount originCA,
					CloudAccount aA) {
				int index = -1;
				if (!originCA.getUsername().equals(aA.getUsername())) {
					index = 1;
				} else if (!originCA.getPassword().equals(aA.getPassword())) {
					index = 2;
				}
				return index;
			}

			private boolean checkISSame(CloudAccount orCA, CloudAccount aA) {
				if (orCA.getUsername().equals(aA.getUsername())
						&& (orCA.getPassword().equals(aA.getPassword()))) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	private boolean isExistUser(CloudAccount aA) {
		boolean isExist = false;
		int size = mList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount tA = mList.get(i);
			if (aA.getUsername().equals(tA.getUsername())) {
				isExist = true;
				break;
			}
		}
		return isExist;
	}

	private void jumpCoverDialog(CloudAccount ca) {
		Builder builder = new Builder(ctx);
		builder.setTitle(R.string.system_setting_alarm_pushset_exist_cover);
		String ok = getString(R.string.system_setting_alarm_pushset_builer_identify_add_ok);
		String cancel = getString(R.string.system_setting_alarm_pushset_builer_identify_add_cance);
		final CloudAccount cla = ca;
		builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent data = new Intent();
				data.putExtra("flag", "cover");
				data.putExtra("claa", cla);
				data.putExtra("cover", true);
				data.putExtra("position", pos);
				setResult(REQUESTCODE_EDIT, data);
				AlarmAccountsEditingActivity.this.finish();
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

	/** 获取增加标签的账户 **/
	private CloudAccount getCloudAccount() {
		CloudAccount account = new CloudAccount();
		String pswd = pswdEdt.getText().toString();
		String uNam = userEdt.getText().toString();
		account.setPassword(pswd);
		account.setUsername(uNam);
		return account;
	}
}
