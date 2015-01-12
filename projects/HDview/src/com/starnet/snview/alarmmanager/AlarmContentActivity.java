package com.starnet.snview.alarmmanager;

import java.io.File;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.SDCardUtils;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class AlarmContentActivity extends BaseActivity implements
		OnClickListener {

	private Context ctx;
	private Button imgBtn;
	private Button vdoBtn;
	private String dName;
	private AlarmDevice device;

	public ProgressDialog imgprogress;
	private EditText alarm_content_ip;
	private EditText alarm_content_time;
	private EditText alarm_content_type;
	private EditText alarm_content_iport;
	private EditText alarm_content_device;
	private EditText alarm_content_channel;
	private final int REQUESTCODE = 0x0023;
	private EditText alarm_content_contents;
	private EditText alarm_content_pushdomain;
	private AlarmImageDownLoadTask imgLoadTask;
	private EditText alarm_content_pushusername;
	private final int IMAGE_LOAD_DIALOG = 0x0013;

	private final int TIMOUTCODE = 0x0002;// 超时发送标志
	private final int LOADSUCCESS = 0x0004;
	private final int DOWNLOADFAILED = 0x0003;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case TIMOUTCODE:
				if (imgprogress != null && imgprogress.isShowing()) {
					imgprogress.dismiss();
				}
				showToast(ctx.getString(R.string.alarm_img_load_timeout));
				break;
			case LOADSUCCESS:
				if (imgprogress != null && imgprogress.isShowing()) {
					imgprogress.dismiss();
				}
				Bundle data = msg.getData();
				byte[] imgData = data.getByteArray("image");

				Intent in = new Intent();
				in.putExtra("isExist", true);
				in.putExtra("title", device.getDeviceName());
				in.putExtra("imageUrl", device.getImageUrl());
				in.putExtra("image", imgData);
				in.setClass(AlarmContentActivity.this, AlarmImageActivity.class);
				startActivityForResult(in, REQUESTCODE);
				break;
			case DOWNLOADFAILED:
				if (imgprogress != null && imgprogress.isShowing()) {
					imgprogress.dismiss();
				}
				showToast("");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_content_activity);
		initViews();
		setListenersForWadget();
	}

	private void setListenersForWadget() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmContentActivity.this.finish();
			}
		});
	}

	private void initViews() {
		super.hideRightButton();
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		String title = getString(R.string.alarm_manageradapter_content);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.getTitleView().setText(title);

		ctx = AlarmContentActivity.this;
		finViewsById();
		setInputNull();
		setImgAndVdoBtnVisible();
		setContentForEdt();
	}

	private void setContentForEdt() {
		String dTime = device.getAlarmTime();
		String time = dTime.replaceAll(",", " ").replaceAll("，", " ");
		alarm_content_time.setText(time);
		alarm_content_ip.setText(device.getIp());
		alarm_content_type.setText(device.getAlarmType());
		alarm_content_iport.setText("" + device.getPort());
		alarm_content_device.setText(device.getDeviceName());
		alarm_content_channel.setText("" + device.getChannel());
		alarm_content_contents.setText(device.getAlarmContent());
		alarm_content_pushdomain.setText(device.getPusherDomain());
		alarm_content_pushusername.setText(device.getPusherUserName());
	}

	private void setImgAndVdoBtnVisible() {
		device = getIntent().getParcelableExtra("alarmDevice");
		String imgUrl = device.getImageUrl();
		if (imgUrl == null || imgUrl.trim().length() == 0) {
			imgBtn.setVisibility(View.GONE);
		} else {
			imgBtn.setVisibility(View.VISIBLE);
			imgBtn.setOnClickListener(this);
		}

		boolean isNull = isExistNull(device);
		if (isNull) {
			vdoBtn.setVisibility(View.GONE);
		} else {
			vdoBtn.setVisibility(View.VISIBLE);
			vdoBtn.setOnClickListener(this);
		}
	}

	private void setInputNull() {
		alarm_content_ip.setKeyListener(null);
		alarm_content_time.setKeyListener(null);
		alarm_content_type.setKeyListener(null);
		alarm_content_iport.setKeyListener(null);
		alarm_content_device.setKeyListener(null);
		alarm_content_channel.setKeyListener(null);
		alarm_content_contents.setKeyListener(null);
		alarm_content_pushdomain.setKeyListener(null);
		alarm_content_pushusername.setKeyListener(null);
	}

	private void finViewsById() {
		imgBtn = (Button) findViewById(R.id.imgBtn);
		vdoBtn = (Button) findViewById(R.id.vdoBtn);
		alarm_content_ip = (EditText) findViewById(R.id.alarm_content_ip);
		alarm_content_time = (EditText) findViewById(R.id.alarm_content_time);
		alarm_content_type = (EditText) findViewById(R.id.alarm_content_type);
		alarm_content_iport = (EditText) findViewById(R.id.alarm_content_iport);
		alarm_content_device = (EditText) findViewById(R.id.alarm_content_device);
		alarm_content_channel = (EditText) findViewById(R.id.alarm_content_channel);
		alarm_content_contents = (EditText) findViewById(R.id.alarm_content_contents);
		alarm_content_pushdomain = (EditText) findViewById(R.id.alarm_content_pushdomain);
		alarm_content_pushusername = (EditText) findViewById(R.id.alarm_content_pushusername);
	}

	/** 判断收藏设备的IP、用户名、端口号是否为空，如果有一个为空，则返回true；否则返回false； **/
	private boolean isExistNull(AlarmDevice device) {
		boolean isExist = false;
		if (device.getIp() == null || device.getIp().trim().length() == 0) {
			return true;
		}
		if (device.getUserName() == null
				|| device.getUserName().trim().length() == 0) {
			return true;
		}
		if (device.getPort() == 0) {
			return true;
		}
		return isExist;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (imgprogress != null && imgprogress.isShowing()) {
				imgprogress.dismiss();
			}
			if (imgLoadTask != null) {
				imgLoadTask.setCancel(true);
			}
		}
		this.finish();
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgBtn:
			// 首先需要检测外置SDCard是否可用，如果可用，则到外置SDCard去查找，若是找到，则使用；否则，去内置SDCard查找
			showDialog(IMAGE_LOAD_DIALOG);
			boolean isExist = false;
			boolean isAvailable = false;
			AlarmImageFileCache.context = ctx;
			String imgUrl = device.getImageUrl();
			String exPath = SDCardUtils.getExternalSDCardPath();
			String appName = AlarmImageFileCache.getApplicationName2();
			String tempPath = exPath + appName;
			File file = new File(tempPath);
			if (file.exists()) {
				isAvailable = true;
			}
			if (isAvailable) {
				isExist = AlarmImageFileCache
						.isExistImageFileInExternal(imgUrl);
			}
			if (isExist) {
				if (imgprogress != null && imgprogress.isShowing()) {
					imgprogress.dismiss();
				}
				getImageFromUrlFromLocal(imgUrl);
			} else {
				boolean isEt = AlarmImageFileCache
						.isExistImgFileInternal(imgUrl);
				if (isEt) {
					if (imgprogress != null && imgprogress.isShowing()) {
						imgprogress.dismiss();
					}
					getImageFromUrlFromInteral(imgUrl);
				} else {
					if (NetWorkUtils.checkNetConnection(ctx)) {
						dName = device.getDeviceName();
						getImageFromUrlFromNet(imgUrl);
					} else {
						showToast(ctx.getString(R.string.alarm_net_notopen));
					}
				}
			}
			break;
		case R.id.vdoBtn:
			if (NetWorkUtils.checkNetConnection(ctx)) {
				Intent intent = new Intent(ctx, RealplayActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(AlarmActivity.ALARM_DEVICE_DETAIL, device);
				ctx.startActivity(intent);
				this.finish();
			} else {
				showToast(ctx.getString(R.string.alarm_net_notopen));
			}
			break;
		}
	}

	private void showToast(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_LONG).show();
	}

	/*** 获得一张图片,从是从文件中获取 ***/
	protected void getImageFromUrlFromLocal(String imgUrl) {
		if (imgprogress != null && imgprogress.isShowing()) {
			imgprogress.dismiss();
		}
		Intent intent = new Intent();
		intent.putExtra("isExistOutSD", true);
		intent.putExtra("imageUrl", imgUrl);
		intent.putExtra("title", dName);
		intent.setClass(ctx, AlarmImageActivity.class);
		startActivityForResult(intent, REQUESTCODE);

	}

	/*** 获得一张图片,从是从文件中获取 ***/
	protected void getImageFromUrlFromInteral(String imgUrl) {
		if (imgprogress != null && imgprogress.isShowing()) {
			imgprogress.dismiss();
		}
		Intent intent = new Intent();
		intent.putExtra("isInExist", true);
		intent.putExtra("imageUrl", imgUrl);
		intent.putExtra("title", dName);
		intent.setClass(ctx, AlarmImageActivity.class);
		startActivityForResult(intent, REQUESTCODE);

	}

	/*** 获得一张图片,从网络获取 ***/
	protected void getImageFromUrlFromNet(String imgUrl) {

		imgLoadTask = new AlarmImageDownLoadTask(imgUrl, ctx, mHandler);
		imgLoadTask.setTitle(dName);
		imgLoadTask.start();

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case IMAGE_LOAD_DIALOG:
			imgprogress = new ProgressDialog(this);
			imgprogress.setMessage(getString(R.string.alarm_iamgeload_wait));
			imgprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			imgprogress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (imgprogress.isShowing()) {
						imgprogress.dismiss();
						imgLoadTask.setCancel(true);
					}
				}
			});
			return imgprogress;
		default:
			return null;
		}
	}
}