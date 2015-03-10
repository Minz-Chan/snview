package com.starnet.snview.syssetting;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.util.CommonUtils;
import com.starnet.snview.util.IPAndPortUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;
import com.starnet.snview.util.SynObject;

@SuppressLint({ "HandlerLeak", "SdCardPath" })
public class CloudAccountUpdatingActivity extends BaseActivity {

	private EditText domainExt;
	private EditText portExt;
	private EditText userExt;
	private EditText passwordExt;
	private RadioButton isenablYseRadioBtn;
	private RadioButton isenablNoRadioBtn;

	private Button identifyBtn; // 验证按钮
	private Button saveBtn; // 保存按钮
	private CloudAccount identifyCloudAccount; // 验证后的账户
	private CloudAccount clickCloudAccount = new CloudAccount(); // 要修改的原始账户
	private int clickPostion;
	private SynObject synObj = new SynObject();

	private final int DDNS_RESP_SUCC = 0x1100; // 获取设备信息成功
	private final int DDNS_RESP_FAILURE = 0x1101; // 获取设备信息失败
	private final int DDNS_REQ_TIMEOUT = 0x1102; // 设备列表请求超时
	private final int DDNS_SYS_FAILURE = 0x1103; // 非DDNS返回错误

	private Context context;

	private String server;
	private String port;
	private String username;
	private String password;
	private List<PreviewDeviceItem> previewDeviceItems; // 预览通道
	private List<PreviewDeviceItem> deletePDeviceItems = new ArrayList<PreviewDeviceItem>(); // 预览通道
	
	private Handler responseHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@SuppressLint("SdCardPath")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (synObj.getStatus() == SynObject.STATUS_RUN) {
				return;
			}
			dismissDialog(1);
			// 解除挂起， 程序往下执行
			// synObj.resume();
			String errMsg = "";
			context = CloudAccountUpdatingActivity.this;
			switch (msg.what) {
			case DDNS_RESP_SUCC:// 只验证，不保存
				showToast(getString(R.string.system_setting_cloudaccount_useable));
				// dismissDialog(1);
				break;
			case DDNS_RESP_FAILURE:
				errMsg = msg.getData().getString("ERR_MSG");
				showToast(errMsg);
				break;
			case DDNS_SYS_FAILURE:
				errMsg = getString(R.string.common_connection_wrong_check_port_domain);
				showToast(errMsg);
				break;
			case DDNS_REQ_TIMEOUT:
				errMsg = getString(R.string.common_request_outtime_check_port_server);
				showToast(errMsg);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_setting_activity_another);
		initView();
		loadViewFromData();
		setListenters();
	}

	private void setListenters() {

		identifyBtn.setOnClickListener(new OnClickListener() {
			// 验证用户的有效性；首先检查填写信息是否完整，如果不完整，则提示用户不完整信息；否则，进行网络验证，验证之后，保存验证用户的信息
			@Override
			public void onClick(View v) {
				boolean isConn = NetWorkUtils.checkNetConnection(context);// 检测网络是否连接，若网络并未连接则
				if (isConn) {
					server = domainExt.getText().toString().trim();
					port = portExt.getText().toString().trim();
					username = userExt.getText().toString().trim();
					password = passwordExt.getText().toString().trim();
					if (!server.equals("") && !port.equals("")
							&& !username.equals("") && !password.equals("")) {// 验证是否有为空的现象
						boolean isPort = IPAndPortUtils.isNetPort(port);// 检测是否是网络端口号
						if (isPort) {
							identifyCloudAccount = new CloudAccount();
							if (isenablYseRadioBtn.isChecked()) {
								identifyCloudAccount.setEnabled(true);
							} else {
								identifyCloudAccount.setEnabled(false);
							}
							identifyCloudAccount.setDomain(server);
							identifyCloudAccount.setPassword(password);
							identifyCloudAccount.setUsername(username);
							identifyCloudAccount.setPort(port);
							requset4DeviceList();
							synObj.suspend();// 挂起等待请求结果
						} else {
							showToast(getString(R.string.device_manager_editact_port_wrong));
						}
					} else {
						showToast(getString(R.string.system_setting_cloudaccountsetedit_null_content));
					}
				} else {
					showToast(getString(R.string.network_not_conn));
				}
			}
		});

		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountUpdatingActivity.this.finish();
			}
		});

		saveBtn.setOnClickListener(new OnClickListener() {// 单击保存时，需要验证用户的有效性
			@Override
			public void onClick(View v) {
				saveNewAccountToXML();
			}
		});
	}

	protected boolean changed(CloudAccount account) {
		boolean result = false;
		if (account == null) {
			result = true;
		} else {
			String domain = clickCloudAccount.getDomain();
			String paswod = clickCloudAccount.getPassword();
			String port = clickCloudAccount.getPort();
			String username = clickCloudAccount.getUsername();
			if (domain.equals(account.getDomain())
					|| paswod.equals(account.getPassword())
					|| port.equals(account.getPort())
					|| username.equals(account.getUsername())) {
				result = true;
			}
		}
		return result;
	}

	protected void saveNewAccountToXML() {//CloudAccount account
		final CloudAccount account = getCloudAccount();
		String server = account.getDomain().trim();
		String port = account.getPort().trim();
		String username = account.getUsername().trim();
		String password = account.getPassword().trim();
		if (!server.equals("") && !port.equals("") && !username.equals("")
				&& !password.equals("")) {
			account.setRotate(false);
			account.setExpanded(false);
			if (isenablYseRadioBtn.isChecked()) {
				account.setEnabled(true);
			} else {
				account.setEnabled(false);
			}
			boolean isSame = isEqualCloudAccounts(clickCloudAccount, account);
			if (isSame) {
				if (clickCloudAccount.isEnabled() == account.isEnabled()) {
					CloudAccountUpdatingActivity.this.finish();
				} else {
					try {
//						ReadWriteXmlUtils.replaceSpecifyCloudAccount(CloudAccountAddingActivity.STARUSERSFILEPATH,clickCloudAccount, account);
						ReadWriteXmlUtils.replaceSpecifyCloudAccount(CloudAccountAddingActivity.STARUSERSFILEPATH, clickPostion, account);
						notifyPreviewChange();
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putSerializable("edit_cloudAccount", account);
						intent.putExtras(bundle);
						setResult(3, intent);
						CloudAccountUpdatingActivity.this.finish();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					List<CloudAccount> cAList = ReadWriteXmlUtils.getCloudAccountList(CloudAccountAddingActivity.STARUSERSFILEPATH);
					boolean result = judgeListContainCloudAccount(account,cAList); // 检测是否已经存在账户
					if (result) {
						showToast(getString(R.string.device_manager_setting_setedit_contain_no_need));
					} else {
						new Thread(){

							@Override
							public void run() {
								try {
									ReadWriteXmlUtils.replaceSpecifyCloudAccount(CloudAccountAddingActivity.STARUSERSFILEPATH, clickPostion, account);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();
						
						notifyPreviewChange();
						if (isenablNoRadioBtn.isChecked()) {
							account.setEnabled(false);
						} else {
							account.setEnabled(true);
						}
						CommonUtils.delTags(context, clickCloudAccount);
						CommonUtils.setTags(context, account);
						showToast(getString(R.string.system_setting_cloudaccountupdate_edit_right));
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putSerializable("edit_cloudAccount", account);
						intent.putExtras(bundle);
						setResult(3, intent);
						CloudAccountUpdatingActivity.this.finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			showToast(getString(R.string.system_setting_cloudaccountsetedit_null_content));
		}
	}

	private void notifyPreviewChange() {
		if (previewDeviceItems.size()>0) {
			String userName = clickCloudAccount.getUsername();
			for (PreviewDeviceItem item : previewDeviceItems) {
				if (item.getPlatformUsername().equals(userName)) {
					deletePDeviceItems.add(item);
				}
			}
			
			for (int i = 0; i < deletePDeviceItems.size(); i++) {
				previewDeviceItems.remove(deletePDeviceItems.get(i));
			}
			
			if (deletePDeviceItems.size() > 0) {
				GlobalApplication.getInstance().getRealplayActivity().setPreviewDevices(previewDeviceItems);
				GlobalApplication.getInstance().getRealplayActivity().notifyPreviewDevicesContentChanged();
			}
		}
	}

	private void loadViewFromData() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			clickCloudAccount = (CloudAccount) bundle
					.getSerializable("cloudAccount");
			String server = clickCloudAccount.getDomain();
			String port = clickCloudAccount.getPort();
			String userName = clickCloudAccount.getUsername();
			String password = clickCloudAccount.getPassword();
			domainExt.setText(server);
			portExt.setText(port);
			userExt.setText(userName);
			passwordExt.setText(password);
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
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		saveBtn = super.getRightButton();
		context = CloudAccountUpdatingActivity.this;
		domainExt = (EditText) findViewById(R.id.cloudaccount_setting_server_edittext);
		portExt = (EditText) findViewById(R.id.cloudaccount_setting_port_edittext);
		userExt = (EditText) findViewById(R.id.cloudaccount_setting_username_edittext);
		passwordExt = (EditText) findViewById(R.id.cloudaccount_setting_password_edittext);
		isenablYseRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_yes_radioBtn);
		isenablNoRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_no_radioBtn);
		identifyBtn = (Button) findViewById(R.id.identify_cloudaccount_right);
		clickPostion = Integer.valueOf(getIntent().getExtras().getString(
				"clickPostion"));
		previewDeviceItems = GlobalApplication.getInstance().getRealplayActivity().getPreviewDevices();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			ProgressDialog progress = ProgressDialog.show(this, "",
					getString(R.string.system_set_setedit_identify_user_right),
					true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(1);
					synObj.resume();
				}
			});
			return progress;
		default:
			return null;
		}
	}

	private void showToast(String content) {
		Toast.makeText(context, content, Toast.LENGTH_LONG).show();
	}

	@SuppressWarnings("deprecation")
	private void requset4DeviceList() {
		showDialog(1);
		(new RequestDeviceInfoThread(responseHandler)).start();
	}

	class RequestDeviceInfoThread extends Thread {
		private Handler handler;

		public RequestDeviceInfoThread(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			Message msg = new Message();
			try {
				Document doc = ReadWriteXmlUtils.SendURLPost(server, port,
						username, password, "conn");
				String requestResult = ReadWriteXmlUtils.readXmlStatus(doc);
				if (requestResult == null) // 请求成功，返回null
				{
					msg.what = DDNS_RESP_SUCC;
				} else { // 请求失败，返回错误原因
					Bundle errMsg = new Bundle();
					msg.what = DDNS_RESP_FAILURE;
					errMsg.putString("ERR_MSG", requestResult);
					msg.setData(errMsg);
				}
			} catch (DocumentException e) {
				msg.what = DDNS_SYS_FAILURE;
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				msg.what = DDNS_REQ_TIMEOUT;
				e.printStackTrace();
			} catch (IOException e) {
				msg.what = DDNS_SYS_FAILURE;
				e.printStackTrace();
			}
			handler.sendMessage(msg);
		}
	}

	protected boolean isEqualCloudAccounts(CloudAccount save_CloudAccount2,
			CloudAccount identify_CloudAccount2) {
		boolean isEqual = false;
		if ((save_CloudAccount2 == null) || (identify_CloudAccount2 == null)) {
			return isEqual;
		} else {
			String sDman = save_CloudAccount2.getDomain();
			String sPort = save_CloudAccount2.getPort();
			String sPass = save_CloudAccount2.getPassword();
			String sName = save_CloudAccount2.getUsername();

			String iDman = identify_CloudAccount2.getDomain();
			String iPort = identify_CloudAccount2.getPort();
			String iPass = identify_CloudAccount2.getPassword();
			String iName = identify_CloudAccount2.getUsername();

			if (sDman.equals(iDman) && sPort.equals(iPort)&& sPass.equals(iPass) && sName.equals(iName)) {
				isEqual = true;
			} else {
				isEqual = false;
			}
			return isEqual;
		}
	}

	private boolean judgeListContainCloudAccount(CloudAccount cloudAccount,
			List<CloudAccount> cloudAccountList2) {
		boolean result = false;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			if (i != clickPostion) {
				CloudAccount cA = cloudAccountList2.get(i);
				String cADomain = cA.getDomain();
				String cAPort = cA.getPort();
				String cAUsername = cA.getUsername();
				if (cloudAccount.getUsername().equals(cAUsername)
						&& cloudAccount.getDomain().equals(cADomain)
						&& cloudAccount.getPort().equals(cAPort)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private CloudAccount getCloudAccount() {
		CloudAccount account = new CloudAccount();
		String domain = domainExt.getText().toString();
		String port = portExt.getText().toString();
		String userName = userExt.getText().toString();
		String password = passwordExt.getText().toString();
		boolean isEnabled = isenablYseRadioBtn.isChecked();
		account.setRotate(false);
		account.setExpanded(false);
		account.setEnabled(isEnabled);
		account.setPort(port);
		account.setDomain(domain);
		account.setPassword(password);
		account.setUsername(userName);
		return account;
	}
}