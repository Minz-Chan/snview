package com.video.hdview.alarmmanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.video.hdview.R;
import com.video.hdview.component.BaseActivity;
import com.video.hdview.util.ReadWriteXmlUtils;

@SuppressLint("HandlerLeak")
public class AlarmActivity extends BaseActivity {

	private int groupPos;
	private Context mContext;
	private Button navBackBtn;
	private TextView titleView;
	private boolean cancel = false;
	public ProgressDialog imgprogress;
	private boolean isContentBack = false;
	private ExpandableListView alarmListView;
	private List<AlarmShowItem> alarmInfoList;
	private AlarmDeviceAdapter listviewAdapter;

	private final int IMAGE_LOAD_DIALOG = 0x0013;
	private final int ALARM_CONTEN_DIALOG = 0x0003;
	protected static final String TAG = "AlarmActivity";
	public static final String ALARM_DEVICE_DETAIL = "alarm_device_detail";
	public static final String START_FROM_NOTIFICATION = "start_from_notification";

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
		if (intent != null) {
			int pos = intent.getIntExtra("position", 0);
			Log.v(TAG, "pos:" + pos);
		}
		setIntent(intent);
		setNewAlarmDevices();
		super.onNewIntent(intent);
	}

	private void initView() {
		if (isStartFromNotificationBar()) {
			super.hideLeftButton();
		}
		super.hideExtendButton();
		mContext = AlarmActivity.this;
		super.setToolbarVisiable(false);
		titleView = super.getTitleView();
		titleView.setText(getString(R.string.alarm_title));
		navBackBtn = (Button) findViewById(R.id.base_navigationbar_right_btn);
		alarmListView = (ExpandableListView) findViewById(R.id.alarm_expandlistview);
		navBackBtn.setVisibility(View.GONE);
		List<AlarmDevice> alarmDevices = ReadWriteXmlUtils.readAlarms();
		if (alarmDevices != null) {
			alarmInfoList = getShowAlarmItems(sortAlarmDevice(alarmDevices));
			setAdapterForListView();
		}
	}
	
	/**
	 * 按时间排序，时间越新排在越靠前（报警信息写入时按时间先后顺序写入）
	 * @param devices 从XML文件中读取出来的报警设备列表
	 * @return 排序后的报警设备列表
	 */
	private List<AlarmDevice> sortAlarmDevice(List<AlarmDevice> devices) {
		if (devices == null || devices.size() == 0) {
			return devices;
		}
		
		List<AlarmDevice> newDevices = new ArrayList<AlarmDevice>();
		int size = devices.size();
		
		for (int i = size-1; i >= 0; i--) {
			newDevices.add(devices.get(i));
		}
		
		return newDevices;
	}

	private void initListener() {
		alarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Builder builder = new Builder(AlarmActivity.this);
				groupPos = (Integer) view.getTag(R.id.arrowimg);
				String alarm_titl = getString(R.string.alarm_dialog_title);
				String alarm_delete_info = getString(R.string.alarm_dialog_infor);
				String deviceName = alarmInfoList.get(groupPos).getAlarm()
						.getDeviceName();
				String title = alarm_titl + " " + deviceName + " "
						+ alarm_delete_info + " ?";
				builder.setTitle(title);
				builder.setPositiveButton(getString(R.string.alarm_dialog_OK),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								listviewAdapter
										.setExpandIndex(getListviewExpand(
												alarmListView, groupPos));
								alarmInfoList.remove(groupPos);
								listviewAdapter.notifyDataSetChanged();
								restoreExpandedStatus();
								ReadWriteXmlUtils.removeSpecifyAlarm(getRealPosition(groupPos));
							}
						});
				builder.setNegativeButton(
						getString(R.string.alarm_dialog_cancel), null);
				builder.show();
				return true;
			}
		});
		
		alarmListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				Intent intent = new Intent();
				intent.setClass(mContext, AlarmContentActivity.class);
				intent.putExtra("position", groupPosition);
				AlarmDevice aD = alarmInfoList.get(groupPosition).getAlarm();
				intent.putExtra("alarmDevice", aD);
				startActivityForResult(intent, ALARM_CONTEN_DIALOG);
				return true;
			}
		});
	}
	
	/**
	 * 取得所选列表项在实际XML文件中对应的项索引
	 * @param pos 列表项索引
	 * @return 实际索引
	 */
	private int getRealPosition(int pos) {
		return alarmInfoList.size()-1-pos;
	}

	/*** 获取展开列表的下标;postion：元素的个数；delPos：删除的位置 ***/
	protected List<Integer> getListviewExpand(
			ExpandableListView alarmListView2, int delPos) {
		List<Integer> indexs = new ArrayList<Integer>();
		int count = alarmListView.getExpandableListAdapter().getGroupCount();
		for (int i = 0; i < count; i++) {
			boolean isExpand = alarmListView2.isGroupExpanded(i);
			if (isExpand) {
				if (i < delPos) {// 删除位置在下方时
					indexs.add(i);
				} else if (i > delPos) {// 删除位置在上方时
					indexs.add(i - 1);
				}
			}
		}
		return indexs;
	}

	/** 获取expandableListview中展开的项的数目 ***/
	protected int getExpandNum(ExpandableListView alarmListView2, long position) {
		int groupPostion = 0;
		for (int i = 0; i < position; i++) {
			boolean isExpand = alarmListView2.isGroupExpanded(i);
			if (isExpand) {
				groupPostion++;
			}
		}
		return groupPostion;
	}

	private void restoreExpandedStatus() {
		for (int j = 0; j < alarmListView.getCount(); j++) {
			alarmListView.collapseGroup(j);
		}

		List<Integer> expandedIndexes = listviewAdapter.getExpandedIndexes();
		if (expandedIndexes != null && expandedIndexes.size() > 0) {
			for (int i = 0; i < expandedIndexes.size(); i++) {
				alarmListView.expandGroup(expandedIndexes.get(i));
			}
		}
	}

	private void setAdapterForListView() {
		listviewAdapter = new AlarmDeviceAdapter(alarmInfoList, mContext);
		alarmListView.setAdapter(listviewAdapter);
		/*
		alarmListView.post(new Runnable() {
			@Override
			public void run() {
				// alarmListView.smoothScrollToPositionFromTop(alarmListView.getCount()*2,
				// 0, 5000);
				alarmListView.smoothScrollToPosition(alarmListView.getCount());
			}
		});*/
	}

//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//		case IMAGE_LOAD_DIALOG:
//			imgprogress = new ProgressDialog(this);
//			imgprogress.setMessage(getString(R.string.alarm_iamgeload_wait));
//			imgprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			imgprogress.setOnCancelListener(new OnCancelListener() {
//				@Override
//				public void onCancel(DialogInterface dialog) {
//					listviewAdapter.cancel(true);
//					if (imgprogress != null) {
//						imgprogress.dismiss();
//					}
//				}
//			});
//			return imgprogress;
//		default:
//			return null;
//		}
//	}

	private void setNewAlarmDevices() {
		if ((alarmInfoList != null && alarmInfoList.size() > 0)) {
			alarmInfoList.clear();
		}
		alarmInfoList = getShowAlarmItems(sortAlarmDevice(ReadWriteXmlUtils.readAlarms()));
		listviewAdapter = new AlarmDeviceAdapter(alarmInfoList, mContext);
		alarmListView.setAdapter(listviewAdapter);
	}

	// 获取新的报警显示（AlarmShowItem）信息
	private ArrayList<AlarmShowItem> getShowAlarmItems(
			List<AlarmDevice> alarmDevices) {
		if (alarmDevices == null) {
			return null;
		}
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
		if (!isContentBack) {
			setNewAlarmDevices();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		isContentBack = true;
		if (requestCode == IMAGE_LOAD_DIALOG) {
			if (data != null) {
				boolean alarmCancel = data
						.getBooleanExtra("alarmCancel", false);
				if (imgprogress.isShowing()) {
					imgprogress.dismiss();
				}
				listviewAdapter.cancel(alarmCancel);
			}
		} else if (requestCode == ALARM_CONTEN_DIALOG) {
			if (data != null) {
				isContentBack = true;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			isContentBack = true;
			cancel = true;
			if (listviewAdapter != null) {
				listviewAdapter.cancel(cancel);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
