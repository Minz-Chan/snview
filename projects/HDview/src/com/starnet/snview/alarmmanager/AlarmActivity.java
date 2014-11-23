package com.starnet.snview.alarmmanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("HandlerLeak")
public class AlarmActivity extends BaseActivity {

	private static final String TAG = "AlarmActivity";
	public static final String START_FROM_NOTIFICATION = "start_from_notification";

	private Context mContext;
	private Button navBackBtn;
	private TextView titleView;
	private boolean cancel = false;

	public ProgressDialog imgprogress;
	private List<AlarmShowItem> alarmInfoList;
	private AlarmDeviceAdapter listviewAdapter;
	private ExpandableListView alarmInfoListView;

	private final int IMAGE_LOAD_DIALOG = 0x0013;
	private final int VIDEO_LOAD_DIALOG = 0x0014;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(!isStartFromNotificationBar());	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_manager_layout);
		initView();

		initListener();

	}
	
	private boolean isStartFromNotificationBar() {
		Boolean startFromNotification = null;
		if (getIntent().getExtras() == null) {
			startFromNotification = false;
		} else {
			startFromNotification = (Boolean) getIntent().getExtras().get(
					START_FROM_NOTIFICATION);
			if (startFromNotification == null) {
				startFromNotification = false;
			}
		}
		
		return startFromNotification;
	}
	

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		setNewAlarmDevices();		
		super.onNewIntent(intent);
	}
	
	private void initView() {
		if (isStartFromNotificationBar()) {
			super.hideLeftButton();
		}
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		titleView = super.getTitleView();
		titleView.setText(getString(R.string.alarm_title));
		mContext = AlarmActivity.this;
		navBackBtn = (Button) findViewById(R.id.base_navigationbar_right_btn);
		alarmInfoListView = (ExpandableListView) findViewById(R.id.alarm_expandlistview);
		navBackBtn.setVisibility(View.GONE);
		List<AlarmDevice> alarmDevices = ReadWriteXmlUtils.readAlarms();
		if (alarmDevices != null) {
			alarmInfoList = getShowAlarmItems(alarmDevices);
			setAdapterForListView();
		}
	}

	private void initListener() {
		alarmInfoListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {

						final int pos = position;
						Builder builder = new Builder(AlarmActivity.this);

						String alarm_info = getString(R.string.alarm_dialog_infor);
						String alarm_titl = getString(R.string.alarm_dialog_title);
						String deviceName = alarmInfoList.get(position).getAlarm().getDeviceName();
						String title = alarm_titl + " " + deviceName + " " + alarm_info + " ?";
						builder.setTitle(title);

						builder.setNegativeButton(getString(R.string.alarm_dialog_cancel), null);
						builder.setPositiveButton(getString(R.string.alarm_dialog_OK),
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,int which) {
										ReadWriteXmlUtils.removeAlarm(alarmInfoList.get(pos).getAlarm());
										alarmInfoList.remove(pos);
										listviewAdapter.notifyDataSetChanged();
									}
								});
						builder.show();
						return true;
					}
				});
		alarmInfoListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				boolean isExpanded = alarmInfoList.get(groupPosition).isExpanded();
				if (isExpanded) {
					alarmInfoList.get(groupPosition).setExpanded(false);
				} else {
					alarmInfoList.get(groupPosition).setExpanded(true);
				}
				return false;
			}
		});
	}

	private void setAdapterForListView() {
		listviewAdapter = new AlarmDeviceAdapter(alarmInfoList, mContext);
		alarmInfoListView.setAdapter(listviewAdapter);
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
					listviewAdapter.cancel(true);
					if (imgprogress != null) {
						imgprogress.dismiss();
					}
				}
			});
			return imgprogress;
		case VIDEO_LOAD_DIALOG:
			final ProgressDialog videoProgress = new ProgressDialog(this);
			videoProgress.setMessage(getString(R.string.alarm_videoload_wait));
			videoProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			videoProgress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {

					videoProgress.dismiss();
				}
			});
			return videoProgress;
		default:
			return null;
		}
	}

	private void setNewAlarmDevices() {
		alarmInfoList.clear();
		alarmInfoList = getShowAlarmItems(ReadWriteXmlUtils.readAlarms());
		listviewAdapter = new AlarmDeviceAdapter(alarmInfoList, mContext);
		alarmInfoListView.setAdapter(listviewAdapter);
	}

	// 获取新的报警显示（AlarmShowItem）信息
	private ArrayList<AlarmShowItem> getShowAlarmItems(List<AlarmDevice> alarmDevices) {
		ArrayList<AlarmShowItem> showItems = new ArrayList<AlarmShowItem>();
		for (int i = 0; i < alarmDevices.size(); i++) {
			AlarmShowItem showItem = new AlarmShowItem();
			showItem.setAlarm(alarmDevices.get(i));
			showItems.add(showItem);
		}
		return showItems;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		setNewAlarmDevices();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			boolean alarmCancel = data.getBooleanExtra("alarmCancel", false);
			if (imgprogress != null) {
				imgprogress.dismiss();
			}
			listviewAdapter.cancel(alarmCancel);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			cancel = true;
			if (listviewAdapter!=null) {
				listviewAdapter.cancel(cancel);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// private Handler mHandler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// super.handleMessage(msg);
	// Bundle data = msg.getData();
	// byte[] imgData = data.getByteArray("image");
	// if (imgData != null) {
	// Log.i(TAG, "mgData.length：" + imgData.length);
	// Intent intent = new Intent();
	// intent.setClass(AlarmActivity.this, AlarmImageActivity.class);
	// intent.putExtra("image", imgData);
	// intent.putExtra("cancel", cancel);
	// startActivityForResult(intent, REQUESTCODE);
	// }
	// }
	// };
}
