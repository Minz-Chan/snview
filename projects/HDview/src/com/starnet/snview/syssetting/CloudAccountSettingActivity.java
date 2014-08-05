package com.starnet.snview.syssetting;

import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
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
import android.widget.RadioGroup;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.CloudService;
import com.starnet.snview.devicemanager.CloudServiceImpl;

@SuppressLint("SdCardPath")
public class CloudAccountSettingActivity extends BaseActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "CloudAccountSettingActivity";
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	private EditText serverEditText;
	private EditText portEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private RadioButton isenablYseRadioBtn;
	private RadioButton isenablNoRadioBtn;
	private RadioGroup isenableRadioGroup;

	private CloudAccount cloudAccount;
	private Thread thread;
	private CloudAccountXML caXML;
	private String showStatus;
//	private CloudAccount clickCloudAccount;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Toast toast0 = Toast.makeText(CloudAccountSettingActivity.this,showStatus, Toast.LENGTH_LONG);
				toast0.show();
				dismissDialog(1);
				break;
			case 1:
				String sentence1 = "添加成功...";
				Toast toast1 = Toast.makeText(CloudAccountSettingActivity.this,sentence1, Toast.LENGTH_LONG);
				toast1.show();
				dismissDialog(1);
				break;
			case 2:
				String sentence2 = "端口号错误，请检查端口号...";
				Toast toast2 = Toast.makeText(CloudAccountSettingActivity.this,sentence2, Toast.LENGTH_SHORT);
				toast2.show();
				dismissDialog(1);
				break;
			case 3:
				String sentence3 = "您还有包含未填写的内容，请填写每一项内容...";
				Toast toast3 = Toast.makeText(CloudAccountSettingActivity.this,sentence3, Toast.LENGTH_LONG);
				toast3.show();
				dismissDialog(1);
				break;
			case 4:	
				String text = "已经包含该用户，不需要再次添加...";
				Toast toast = Toast.makeText(CloudAccountSettingActivity.this, text,Toast.LENGTH_SHORT);
				toast.show();
				dismissDialog(1);
				break;
			case 5:	
				String text5 = "域名错误，请检查域名...";
				Toast toast5 = Toast.makeText(CloudAccountSettingActivity.this, text5,Toast.LENGTH_SHORT);
				toast5.show();
				dismissDialog(1);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_setting_activity);

		initView();
		setListeners();
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_cloudaccount_setting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);

		serverEditText = (EditText) findViewById(R.id.cloudaccount_setting_server_edittext);
		portEditText = (EditText) findViewById(R.id.cloudaccount_setting_port_edittext);
		usernameEditText = (EditText) findViewById(R.id.cloudaccount_setting_username_edittext);
		passwordEditText = (EditText) findViewById(R.id.cloudaccount_setting_password_edittext);
		isenableRadioGroup = (RadioGroup) findViewById(R.id.cloudaccount_setting_isenable_radioGroup);
		isenablYseRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_yes_radioBtn);
		isenablNoRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_no_radioBtn);
		
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountSettingActivity.this.finish();
			}
		});

		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cloudAccount = new CloudAccount();
				if (isenablYseRadioBtn.isChecked()) {
					cloudAccount.setEnabled(true);
				} else if (isenablNoRadioBtn.isChecked()) {
					cloudAccount.setEnabled(false);
				}
				showDialog(1);
				thread = new Thread() {
					private Message message = new Message();

					@Override
					public void run() {
						super.run();
						// 验证是否有为空的现象
						String server = serverEditText.getText().toString();
						String port = portEditText.getText().toString();
						String username = usernameEditText.getText().toString();
						String password = passwordEditText.getText().toString();

						if (!server.equals("") && !port.equals("")&& !username.equals("") && !password.equals("")) {
							cloudAccount.setDomain(server);
							cloudAccount.setPassword(password);
							cloudAccount.setUsername(username);
							cloudAccount.setPort(port);
							caXML = new CloudAccountXML();
							try {
								List<CloudAccount> cloudAcountList = caXML.getCloudAccountList(filePath);
								boolean result = judgeListContainCloudAccount(cloudAccount, cloudAcountList);
								if (result) {
									message.what = 4;// 代表已经包含...
									handler.sendMessage(message);
								} else {
									CloudService cloudService = new CloudServiceImpl("conn1");
									try {
										Document doc = cloudService.SendURLPost(server, port,username, password);
										String status = cloudService.readXmlStatus(doc);
										if (status != null) {
											showStatus = status;
											message.what = 0;// 代表失败
											handler.sendMessage(message);
										} else {
											message.what = 1;// 代表成功
											handler.sendMessage(message);
											Thread thread1 = new Thread() {
												@Override
												public void run() {
													super.run();
													caXML = new CloudAccountXML();
													caXML.addNewCloudAccoutNodeToRootXML(filePath,cloudAccount);// 保存到XML文档中。。。
												}
											};
											thread1.start();
											Intent intent = new Intent();
											Bundle bundle = new Bundle();
											bundle.putSerializable("cloudAccount",cloudAccount);
											intent.putExtras(bundle);
											setResult(3, intent);
										}
									} catch (IOException e) {
										e.printStackTrace();
										message.what = 2;// 代表“端口号错误”
										handler.sendMessage(message);
									}catch (DocumentException e) {
										message.what = 5;// 代表“域名错误”
										handler.sendMessage(message);
									} 
								}
							} catch (Exception e1) {
								System.out.println(e1.toString());
							}
						} else {
							message.what = 3;// 代表“包含”未填写的内容
							handler.sendMessage(message);
						}
					}
				};
				thread.start();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog progress = ProgressDialog.show(this, "","正在验证客户有效性，请等待...", true, true);
		progress.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dismissDialog(1);
				if (thread != null) {
					thread.suspend();
					return;
				}
			}
		});
		return progress;
	}

	private boolean judgeListContainCloudAccount(CloudAccount cloudAccount,List<CloudAccount> cloudAccountList2) {
		boolean result = false;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cA = cloudAccountList2.get(i);
			String cADomain = cA.getDomain();
			String cAPort = cA.getPort();
			String cAUsername = cA.getUsername();
			String cAPassword = cA.getPassword();
			/*boolean isEnabled = cA.isEnabled();*/
			if (cloudAccount.getUsername().equals(cAUsername)&& cloudAccount.getDomain().equals(cADomain)
				&& cloudAccount.getPassword().equals(cAPassword)&& cloudAccount.getPort().equals(cAPort)
				) {/*&&(isEnabled == cloudAccount.isEnabled())*/
				result = true;
				break;
			}
		}
		return result;
	}
}