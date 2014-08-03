package com.starnet.snview.syssetting;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.CloudService;
import com.starnet.snview.devicemanager.CloudServiceImpl;

public class CloudAccountSetEditActivity extends BaseActivity {

	private EditText serverEditText;
	private EditText portEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private RadioButton isenablYseRadioBtn;
	private RadioButton isenablNoRadioBtn;

	private CloudAccount clickCloudAccount = new CloudAccount();
	private String printMessage;

	private Thread thread;
	private Handler hanler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String serverSentence="";
			switch (msg.what) {
			case 0:
				serverSentence = "信息编辑正确，保存成功...";
				Toast.makeText(CloudAccountSetEditActivity.this,serverSentence, Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("ca",clickCloudAccount);
				intent.putExtras(bundle);
				setResult(20, intent);
				dismissDialog(1);
				//写入文档中；
				break;
			case 1:
				serverSentence = printMessage;
				Toast.makeText(CloudAccountSetEditActivity.this,serverSentence, Toast.LENGTH_LONG).show();
				dismissDialog(1);
				break;
			case 2:
				serverSentence = "端口号错误,请检查...";
				Toast.makeText(CloudAccountSetEditActivity.this,serverSentence, Toast.LENGTH_LONG).show();
				dismissDialog(1);
				break;
			case 3:
				serverSentence = "域名错误,请检查...";
				Toast.makeText(CloudAccountSetEditActivity.this,serverSentence, Toast.LENGTH_LONG).show();
				dismissDialog(1);
				break;
			case 4:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_set_activity);
		initView();
		loadViewFromData();
		setListenters();
	}

	private void setListenters() {
		// 直接返回...
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountSetEditActivity.this.finish();
			}// 如果信息无改变，则不刷新ListView
		});

		super.getRightButton().setOnClickListener(new OnClickListener() {// 单击保存时，需要验证用户的有效性...
					@Override
					public void onClick(View v) {
						showDialog(1);
						final String server = serverEditText.getText().toString();
						if (server.equals("")) {
							String serverSentence = "域名不能为空，请重设...";
							Toast.makeText(CloudAccountSetEditActivity.this,serverSentence, Toast.LENGTH_LONG).show();
							return;
						}
						final String port = portEditText.getText().toString();
						if (port.equals("")) {
							String portSentence = "端口号不能为空，请重设...";
							Toast.makeText(CloudAccountSetEditActivity.this,portSentence, Toast.LENGTH_LONG).show();
							return;
						}
						final String username = usernameEditText.getText().toString();
						if (username.equals("")) {
							String usernameSentence = "用户名不能为空，请重设...";
							Toast.makeText(CloudAccountSetEditActivity.this,usernameSentence, Toast.LENGTH_LONG).show();
							return;
						}
						final String psd = passwordEditText.getText().toString();
						if (psd.equals("")) {
							String psdSentence = "用户名不能为空，请重设...";
							Toast.makeText(CloudAccountSetEditActivity.this,psdSentence, Toast.LENGTH_LONG).show();
							return;
						}
						boolean isEnable = false;
						if (isenablYseRadioBtn.isChecked()) {
							isEnable = true;
						} else if (isenablNoRadioBtn.isChecked()) {
							isEnable = false;
						}

						if (!server.equals("") && !port.equals("")&& !psd.equals("") && !username.equals("")) {
							thread = new Thread() {// 用于验证用户信息的正确，如果正确则写入文档中，否则不写入文档中；
								Message msg = new Message();
								@Override
								public void run() {
									super.run();
									CloudService cloudService = new CloudServiceImpl("conn1");
									try {
										Document document = cloudService.SendURLPost(server, port,username, psd);
										String status = cloudService.readXmlStatus(document);
										if (status == null) {// 用户的信息正确											
											msg.what = 0;
											hanler.sendMessage(msg);
										} else {// 用户信息不正确
											printMessage = status;
											msg.what = 1;
											hanler.sendMessage(msg);
										}
									} catch (IOException e) {
										msg.what = 2;
										hanler.sendMessage(msg);
									} catch (DocumentException e) {
										msg.what = 3;
										hanler.sendMessage(msg);
									} 
								}
							};
							thread.start();
						}
					}
				});
	}

	private void loadViewFromData() {

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			clickCloudAccount = (CloudAccount) bundle.getSerializable("cloudAccount");
			String server = clickCloudAccount.getDomain();
			String port = clickCloudAccount.getPort();
			String userName = clickCloudAccount.getUsername();
			String password = clickCloudAccount.getPassword();
			serverEditText.setText(server);
			portEditText.setText(port);
			usernameEditText.setText(userName);
			passwordEditText.setText(password);
			boolean isEnabled = clickCloudAccount.isEnabled();
			if (isEnabled) {
				isenablYseRadioBtn.setChecked(true);
				isenablNoRadioBtn.setChecked(false);
			} else {
				isenablYseRadioBtn.setChecked(false);
				isenablNoRadioBtn.setChecked(true);
			}
		}
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_cloudaccount_setting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		serverEditText = (EditText) findViewById(R.id.cloudaccount_set_server_edittext);
		portEditText = (EditText) findViewById(R.id.cloudaccount_set_port_edittext);
		usernameEditText = (EditText) findViewById(R.id.cloudaccount_set_username_edittext);
		passwordEditText = (EditText) findViewById(R.id.cloudaccount_set_password_edittext);
		isenablYseRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_set_isenable_yes_radioBtn);
		isenablNoRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_set_isenable_no_radioBtn);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog progress = ProgressDialog.show(this, "","正在验证客户有效性，请等待...", true, true);
		progress.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dismissDialog(1);
				if(thread != null){
					Message msg = new Message();
					msg.what = 4;
					hanler.sendMessage(msg);
				}
			}
		});
		return progress;
	}
}