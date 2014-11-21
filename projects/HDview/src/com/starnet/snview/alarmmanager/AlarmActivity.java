package com.starnet.snview.alarmmanager;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
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

public class AlarmActivity extends BaseActivity {

	private Button navBackBtn;
	private ExpandableListView alarmInfoListView;
	private AlarmDeviceAdapter listviewAdapter;
	private List<AlarmShowItem> alarmInfoList;
	Context mContext;

	private TextView titleView;// 报警

	Resources resource;
	String pkgName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_manager_layout);

		initUi();

		setOnClickListersForWadget();

	}

	private void setOnClickListersForWadget() {

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
						String title = alarm_titl + " " + deviceName + " "
								+ alarm_info + " ?";
						builder.setTitle(title);

						builder.setNegativeButton(
								getString(R.string.alarm_dialog_cancel), null);
						builder.setPositiveButton(
								getString(R.string.alarm_dialog_OK),
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
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

	private void initUi() {
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		titleView = super.getTitleView();
		titleView.setText(getString(R.string.alarm_title));
		mContext = AlarmActivity.this;
		navBackBtn = (Button) findViewById(R.id.base_navigationbar_right_btn);
		alarmInfoListView = (ExpandableListView) findViewById(R.id.alarm_expandlistview);
		navBackBtn.setVisibility(View.GONE);
		// navBackBtn.setBackgroundResource(R.drawable.alarm_right_back);

		alarmInfoList = new ArrayList<AlarmShowItem>();// 模拟数据

		AlarmDevice alarmDevice1 = new AlarmDevice();
		AlarmDevice alarmDevice2 = new AlarmDevice();
		AlarmDevice alarmDevice3 = new AlarmDevice();
		AlarmDevice alarmDevice4 = new AlarmDevice();
		AlarmDevice alarmDevice5 = new AlarmDevice();
		AlarmDevice alarmDevice6 = new AlarmDevice();
		
		AlarmShowItem item1 = new AlarmShowItem();
		AlarmShowItem item2 = new AlarmShowItem();
		AlarmShowItem item3 = new AlarmShowItem();
		AlarmShowItem item4 = new AlarmShowItem();
		AlarmShowItem item5 = new AlarmShowItem();
		AlarmShowItem item6 = new AlarmShowItem();

		alarmDevice1.setAlarmTime("2014-11-03 19:00");
		alarmDevice1.setDeviceName("乌鲁木齐平台");
		alarmDevice1.setAlarmType("烟雾报警");
		item1.setAlarm(alarmDevice1);

		alarmDevice2.setAlarmTime("2014-10-04 19:00");
		alarmDevice2.setDeviceName("哈尔滨平台");
		alarmDevice2.setAlarmType("入侵报警");
		item2.setAlarm(alarmDevice2);

		alarmDevice3.setAlarmTime("2014-10-04 19:00");
		alarmDevice3.setDeviceName("沈阳平台");
		alarmDevice3.setAlarmType("入侵报警");
		item3.setAlarm(alarmDevice3);

		alarmDevice4.setAlarmTime("2014-10-04 19:00");
		alarmDevice4.setDeviceName("天津平台");
		alarmDevice4.setAlarmType("入侵报警");
		item4.setAlarm(alarmDevice4);

		alarmDevice5.setAlarmTime("2014-10-04 19:00");
		alarmDevice5.setDeviceName("重庆平台");
		alarmDevice5.setAlarmType("入侵报警");
		item5.setAlarm(alarmDevice5);

		alarmDevice6.setAlarmTime("2014-10-04 19:00");
		alarmDevice6.setDeviceName("郑州平台");
		alarmDevice6.setAlarmType("入侵报警");
		item6.setAlarm(alarmDevice6);

		alarmInfoList.add(item1);
		alarmInfoList.add(item2);
		alarmInfoList.add(item3);
		alarmInfoList.add(item4);
		alarmInfoList.add(item5);
		alarmInfoList.add(item6);

		listviewAdapter = new AlarmDeviceAdapter(alarmInfoList, this);
		alarmInfoListView.setAdapter(listviewAdapter);
		
		ReadWriteXmlUtils.writeAlarm(alarmDevice1);
		ReadWriteXmlUtils.writeAlarm(alarmDevice2);
	}

	private final int IMAGE_LOAD_DIALOG = 0x0013;
	private final int VIDEO_LOAD_DIALOG = 0x0014;
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case IMAGE_LOAD_DIALOG:
			final ProgressDialog img_progress = new ProgressDialog(this);
			img_progress.setMessage(getString(R.string.alarm_iamgeload_wait));
			img_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			img_progress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					img_progress.dismiss();
				}
			});
			return img_progress;
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

}