package com.starnet.snview.syssetting;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.CommonUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class CloudAccountAddingActivity extends BaseActivity implements
		OnClickListener {
	public static final String STARUSERSFILEPATH = "/data/data/com.starnet.snview/star_cloudAccount.xml";
	private static final int IDENTIFY_DIALOG = 0x0010;
	private ProgressDialog progress;
	private Context ctx;
	private EditText domainExt;
	private EditText portExt;
	private EditText userExt;
	private EditText pswdExt;
	private RadioButton isUsableYs;
	private Button identifyBtn;
	private CloudAccount account;
	private IdentifyTask task = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_setting_activity_another);
		initViews();
		setListeners();
	}

	private void initViews() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_cloudaccount_setting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		ctx = CloudAccountAddingActivity.this;
		domainExt = (EditText) findViewById(R.id.cloudaccount_setting_server_edittext);
		portExt = (EditText) findViewById(R.id.cloudaccount_setting_port_edittext);
		userExt = (EditText) findViewById(R.id.cloudaccount_setting_username_edittext);
		pswdExt = (EditText) findViewById(R.id.cloudaccount_setting_password_edittext);
		isUsableYs = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_yes_radioBtn);
		identifyBtn = (Button) findViewById(R.id.identify_cloudaccount_right);
	}

	public void setListeners() {
		super.getLeftButton().setOnClickListener(this);
		super.getRightButton().setOnClickListener(this);
		identifyBtn.setOnClickListener(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.identify_cloudaccount_right:
			boolean isOpen = NetWorkUtils.checkNetConnection(ctx);
			if (isOpen) {
				showDialog(IDENTIFY_DIALOG);
				account = getCloudAccount();
				task = new IdentifyTask(account);
				task.execute();
			}else {
				showToast(getString(R.string.system_setting_alarm_pushset_netnotopen));
			}
			break;
		case R.id.base_navigationbar_left_btn:// 左按钮
			CloudAccountAddingActivity.this.finish();
			break;
		case R.id.base_navigationbar_right_btn:// 保存按钮
			saveStarAccountsToXML();
			break;
		default:
			break;
		}
	}

	private void saveStarAccountsToXML() {
		account = getCloudAccount();
		String domain = account.getDomain();
		String port = account.getPort();
		String userName = account.getUsername();
		String password = account.getPassword();
		if (!domain.trim().equals("") && !port.trim().equals("") && !userName.trim().equals("") && !password.trim().equals("")) {
			try {
				List<CloudAccount> cloudAcountList = ReadWriteXmlUtils.getCloudAccountList(STARUSERSFILEPATH);
				boolean result = judgeListContainCloudAccount(account,cloudAcountList);
				if (result) {// 如果包含，则不添加
					String content = getString(R.string.device_manager_setting_setedit_contain_no_need);
					showToast(content);
				} else {// 如果不包含，则添加
					new Thread() {
						@Override
						public void run() {
							ReadWriteXmlUtils.addNewCloudAccoutNodeToRootXML(STARUSERSFILEPATH, account);
						}
					}.start();
					CommonUtils.setTags(ctx, account);
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable("cloudAccount", account);
					intent.putExtras(bundle);
					setResult(3, intent);
					CloudAccountAddingActivity.this.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			showToast(getString(R.string.system_setting_cloudaccountsetting_null_content));
		}
	}

	private CloudAccount getCloudAccount() {
		CloudAccount account = new CloudAccount();
		String domain = domainExt.getText().toString();
		String port = portExt.getText().toString();
		String userName = userExt.getText().toString();
		String password = pswdExt.getText().toString();
		boolean isEnabled = isUsableYs.isChecked();
		account.setRotate(false);
		account.setExpanded(false);
		account.setEnabled(isEnabled);
		account.setPort(port);
		account.setDomain(domain);
		account.setPassword(password);
		account.setUsername(userName);
		return account;
	}

	private boolean judgeListContainCloudAccount(CloudAccount cloudAccount,
			List<CloudAccount> cloudAccountList2) {
		boolean result = false;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cA = cloudAccountList2.get(i);
			String cAUsername = cA.getUsername();
			if (cloudAccount.getUsername().equals(cAUsername)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void showToast(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case IDENTIFY_DIALOG:
			progress = ProgressDialog.show(this, "",getString(R.string.system_set_setting_identify_user_right),true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					progress.dismiss();
					if (!task.isCancelled()) {
						task.cancel(true);
					}
				}
			});
			return progress;
		default:
			return null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			dissmissIdentifyDialog();
			if (!task.isCancelled()) {
				task.cancel(true);
			}
		}
		return false;
	}

	public class IdentifyTask extends AsyncTask<Void, Void, String> {

		private CloudAccount account;
		public IdentifyTask(CloudAccount account) {
			this.account = account;
		}

		@Override
		protected String doInBackground(Void... params) {
			String domain = account.getDomain();
			String port = account.getPort();
			String username = account.getUsername();
			String password = account.getPassword();
			String deviceName = "conn";
			try {
				Document doc = ReadWriteXmlUtils.SendURLPost(domain, port,username, password, deviceName);
				String requestResult = ReadWriteXmlUtils.readXmlStatus(doc);
				if (requestResult == null) // 请求成功，返回null
				{
					return getString(R.string.system_setting_cloudaccount_adding_connect_succ);
				} else { // 请求失败，返回错误原因
					return requestResult;
				}
			} catch (SocketTimeoutException e) {
				return getString(R.string.system_setting_cloudaccount_adding_connect_timeout);
			} catch (IOException e) {
				return getString(R.string.system_setting_cloudaccount_adding_connect_fail);
			}catch (DocumentException e) {
				return getString(R.string.system_setting_cloudaccount_adding_connect_fail);
			} 
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dissmissIdentifyDialog();
			showToast(result);
		}
	}

	public void dissmissIdentifyDialog() {
		if ((progress != null) && (progress.isShowing())) {
			progress.dismiss();
		}
	}
}