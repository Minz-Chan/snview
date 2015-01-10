package com.starnet.snview.playback;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.DeviceItem;

public class PlayBackInfoActivity extends BaseActivity {

	private DeviceItem device;
	private Spinner typeSpinner;
	private List<String> tList;
	private Spinner channelSpinner;
	private List<String> cList;
	private ArrayAdapter<String> tAdapter;
	private ArrayAdapter<String> cAdapter;

	private int group;
	private int child;
	private TextView devicenameTxt;
	private final int REQUESTCODE = 0x0005;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playback_requestinfo_setting);

		initViews();
		setListeners();
	}

	private void setListeners() {

		super.getLeftButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backAndLeftOperation();
			}
		});
	}

	private void backAndLeftOperation() {
		Intent data = new Intent();
		String type = typeSpinner.getSelectedItem().toString();
		String channel = channelSpinner.getSelectedItem().toString();
		data.putExtra("type", type);
		data.putExtra("channel", channel);
		data.putExtra("okBtn", true);
		data.putExtra("group", group);
		data.putExtra("child", child);
		setResult(REQUESTCODE, data);
		PlayBackInfoActivity.this.finish();
	}

	private void initViews() {

		super.setToolbarVisiable(false);
		super.getRightButton().setVisibility(View.GONE);
		super.getExtendButton().setVisibility(View.GONE);
		String title = getString(R.string.playback_infoact_setting);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.getTitleView().setText(title);
		typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
		devicenameTxt = (TextView) findViewById(R.id.devicename);
		channelSpinner = (Spinner) findViewById(R.id.channelSpinner);
		tList = new ArrayList<String>();
		cList = new ArrayList<String>();

		Intent intent = getIntent();
		group = intent.getIntExtra("group", 0);
		child = intent.getIntExtra("child", 0);
		device = (DeviceItem) intent.getSerializableExtra("device");
		loadData();

		if (device!=null) {
			devicenameTxt.setText(device.getDeviceName());
		}
		tAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, tList);
		cAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, cList);
		tAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(tAdapter);
		channelSpinner.setAdapter(cAdapter);

	}

	private void loadData() {
		String chName = getString(R.string.playback_channel);
		List<Channel> chList = device.getChannelList();
		if (chList!=null) {
			int size = chList.size();
			int j = 1;
			for (int i = 0; i < size; i++) {
				String data = chName +j;
				j++;
				cList.add(data);
			}
		}

		String allType = getString(R.string.playback_alarm_type);
		String typeShD = getString(R.string.playback_alarm_type1);
		String typeDsh = getString(R.string.playback_alarm_type2);
		String typeYDZC = getString(R.string.playback_alarm_type3);
		String typeKGLJG = getString(R.string.playback_alarm_type4);

		tList.add(typeKGLJG);
		tList.add(typeYDZC);
		tList.add(typeDsh);
		tList.add(typeShD);
		tList.add(allType);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			backAndLeftOperation();
		}
		return super.onKeyDown(keyCode, event);
	}
}