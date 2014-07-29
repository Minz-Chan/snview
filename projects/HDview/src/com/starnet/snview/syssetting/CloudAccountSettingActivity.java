package com.starnet.snview.syssetting;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.CloudService;
import com.starnet.snview.devicemanager.CloudServiceImpl;

public class CloudAccountSettingActivity extends BaseActivity {
	private static final String TAG = "CloudAccountSettingActivity";
	
	private EditText serverEditText;
	private EditText portEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private RadioGroup isenableRadioGroup;
	private RadioButton isenablYseRadioBtn;
	private RadioButton isenablNoRadioBtn;

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
				if(isenablYseRadioBtn.isChecked()){
					String server = serverEditText.getText().toString();
					String port = portEditText.getText().toString();
					String username = usernameEditText.getText().toString();
					String password = passwordEditText.getText().toString();
					CloudService cloudService = new CloudServiceImpl("conn1");
					try {
						Document doc = cloudService.SendURLPost(server,port,username,password);
						if(cloudService.readXmlStatus(doc)!=null){
							Toast.makeText(getApplicationContext(), cloudService.readXmlStatus(doc), Toast.LENGTH_LONG).show();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DocumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
}
