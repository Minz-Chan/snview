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
	// private Button okBtn;
	private TextView devicenameTxt;
	private final int REQUESTCODE = 0x0001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playback_requestinfo_setting);

		initViews();
		setListeners();
	}

	private void setListeners() {
		// okBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// }
		// });

		super.getLeftButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backAndLeftOperation();
			}
		});
	}

	private void backAndLeftOperation() {
		Intent data = new Intent();
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
		// okBtn = (Button) findViewById(R.id.okBtn);
		typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
		devicenameTxt = (TextView) findViewById(R.id.devicename);
		channelSpinner = (Spinner) findViewById(R.id.channelSpinner);
		tList = new ArrayList<String>();
		cList = new ArrayList<String>();

		Intent intent = getIntent();
		group = intent.getIntExtra("group", 0);
		child = intent.getIntExtra("child", 0);
		device = intent.getParcelableExtra("device");
		testData();

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

	private void testData() {

		String data11 = "通道1";
		String data12 = "通道2";
		String data13 = "通道3";
		String data14 = "通道4";

		cList.add(data11);
		cList.add(data12);
		cList.add(data13);
		cList.add(data14);

		String data21 = "开关量告警录像";
		String data22 = "移动侦测录像";
		String data23 = "定时录像";
		String data24 = "手动录像";
		String data25 = "全部";

		tList.add(data21);
		tList.add(data22);
		tList.add(data23);
		tList.add(data24);
		tList.add(data25);

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