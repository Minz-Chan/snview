package com.video.hdview.syssetting;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import com.video.hdview.R;
import com.video.hdview.component.BaseActivity;

public class CloudAccountInfoActivity extends BaseActivity implements
		OnClickListener {

	private EditText port_ext;
	private EditText server_ext;
	private EditText username_ext;
	private EditText password_ext;

	private RadioButton noRBtn;
	private RadioButton yesRBtn;

	private int clickPostion;
	private CloudAccount editAccount;
	private CloudAccount clickAccount;

	private final int RESULTCODE = 0x0023;
	private final int REQUESTCODE = 0x0023;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloudaccount_info_activity);

		initialUIViews();
		findViews();
		setListenersForBtn();
	}

	private void initialUIViews() {
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		super.setRightButtonBg(R.drawable.device_scan_edit_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_cloudaccount_setting));
	}

	private void findViews() {

		port_ext = (EditText) findViewById(R.id.cloudaccount_info_port_ext);
		server_ext = (EditText) findViewById(R.id.cloudaccount_info_server_ext);
		password_ext = (EditText) findViewById(R.id.cloudaccount_info_pswd_ext);
		username_ext = (EditText) findViewById(R.id.cloudaccount_info_username_ext);
		noRBtn = (RadioButton) findViewById(R.id.cloudaccount_info_no_radioBtn);
		yesRBtn = (RadioButton) findViewById(R.id.cloudaccount_info_yes_radioBtn);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		clickAccount = (CloudAccount) bundle.getSerializable("cloudAccount");

		port_ext.setText(clickAccount.getPort());
		server_ext.setText(clickAccount.getDomain());
		password_ext.setText(clickAccount.getPassword());
		username_ext.setText(clickAccount.getUsername());
		if (clickAccount.isEnabled()) {
			yesRBtn.setChecked(true);
			noRBtn.setChecked(false);
		}else {
			noRBtn.setChecked(true);
			yesRBtn.setChecked(false);
		}

		clickPostion = Integer.valueOf(getIntent().getExtras().getString(
				"clickPostion"));

		port_ext.setKeyListener(null);
		server_ext.setKeyListener(null);
		password_ext.setKeyListener(null);
		username_ext.setKeyListener(null);
	}

	private void setListenersForBtn() {
		super.getLeftButton().setOnClickListener(this);
		super.getRightButton().setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.base_navigationbar_left_btn:
			backAction();
			break;
		case R.id.base_navigationbar_right_btn:
			goToUpdatingActivity();
			break;
		}
	}
	
	private boolean hasEdited = false;

	private void goToUpdatingActivity() {
		Intent intent = new Intent();
		intent.setClass(this, CloudAccountUpdatingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("clickPostion", String.valueOf(clickPostion));
		if (hasEdited) {
			bundle.putSerializable("cloudAccount", editAccount);
		}else {
			bundle.putSerializable("cloudAccount", clickAccount);
		}
		intent.putExtras(bundle);
		startActivityForResult(intent, REQUESTCODE);
	}

	private void backAction() {
		CloudAccount account = getCloudAccountFromUI();
		setBundle(account);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			backAction();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setBundle(CloudAccount account) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable("edit_cloudAccount", account);
		intent.putExtras(bundle);
		setResult(RESULTCODE, intent);
	}

	private CloudAccount getCloudAccountFromUI() {
		CloudAccount account = new CloudAccount();
		account.setRotate(false);
		account.setExpanded(false);
		account.setDeviceList(null);
		account.setEnabled(yesRBtn.isChecked());
		account.setPort(port_ext.getText().toString());
		account.setDomain(server_ext.getText().toString());
		account.setUsername(username_ext.getText().toString());
		account.setPassword(password_ext.getText().toString());
		return account;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			hasEdited = true;
			Bundle bundle = data.getExtras();
			editAccount = (CloudAccount) bundle.getSerializable("edit_cloudAccount");
			refreshUI(editAccount);
		}
	}

	private void refreshUI(CloudAccount account) {
		port_ext.setText(account.getPort());
		server_ext.setText(account.getDomain());
		password_ext.setText(account.getPassword());
		username_ext.setText(account.getUsername());
		if (account.isEnabled()) {
			yesRBtn.setChecked(true);
			noRBtn.setChecked(false);
		}else {
			noRBtn.setChecked(true);
			yesRBtn.setChecked(false);
		}
	}
}