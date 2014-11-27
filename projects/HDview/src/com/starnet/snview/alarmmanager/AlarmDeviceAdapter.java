package com.starnet.snview.alarmmanager;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.NetWorkUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


@SuppressWarnings("deprecation")
public class AlarmDeviceAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private String deviceName;
	private List<AlarmShowItem> alarmInfoList;
	private AlarmImageDownLoadTask imgLoadTask;
	private final int IMAGE_LOAD_DIALOG = 0x0013;

	// private final int VIDEO_LOAD_DIALOG = 0x0014;

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList,
			Context context, Handler handler) {
		this.alarmInfoList = alarmInfoList;
		this.context = context;
	}

	public AlarmDeviceAdapter(List<AlarmShowItem> alarmInfoList, Context context) {
		this.alarmInfoList = alarmInfoList;
		this.context = context;
	}

	@Override
	public int getGroupCount() {
		int size = 0;
		if (alarmInfoList != null) {
			size = alarmInfoList.size();
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return alarmInfoList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.alarm_listview_item_layout, null);
		}
		TextView deviceTxt = (TextView) convertView
				.findViewById(R.id.device_item_name);
		deviceTxt.setText(alarmInfoList.get(groupPosition).getAlarm()
				.getDeviceName());
		ImageView arrowImg = (ImageView) convertView
				.findViewById(R.id.alarm_arrow_img);
		if (alarmInfoList.get(groupPosition).isExpanded()) {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
		} else {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.alarm_listview_subitem_layout, null);
		}


		Button imgLoadBtn = (Button) convertView.findViewById(R.id.image_load_btn);
		Button vdoLoadBtn = (Button) convertView.findViewById(R.id.video_load_btn);		
		Button contentBtn = (Button) convertView.findViewById(R.id.alarm_content_btn);
		final int pos = groupPosition;
		
		imgLoadBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (NetWorkUtils.checkNetConnection(context)) {
					getAlarmActivity().showDialog(IMAGE_LOAD_DIALOG);
					String imgUrl = alarmInfoList.get(pos).getAlarm().getImageUrl();
					if (imgUrl == null) {
						imgUrl = "http://img.my.csdn.net/uploads/201402/24/1393242467_3999.jpg";// 网络测试地址
					}
					deviceName = alarmInfoList.get(pos).getAlarm().getDeviceName();
					imgLoadTask = new AlarmImageDownLoadTask(imgUrl, context);
					imgLoadTask.setTitle(deviceName);
					imgLoadTask.start();
				} else {
					String netNotOpenContent = context
							.getString(R.string.alarm_net_notopen);
					Toast.makeText(context, netNotOpenContent,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		vdoLoadBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetWorkUtils.checkNetConnection(context)) {
					AlarmDevice device = alarmInfoList.get(pos).getAlarm();
					Intent intent = new Intent(context, RealplayActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(AlarmActivity.ALARM_DEVICE_DETAIL, device);
					context.startActivity(intent);
					((AlarmActivity)context).finish();
				} else {
					String netNotOpenContent = context
							.getString(R.string.alarm_net_notopen);
					Toast.makeText(context, netNotOpenContent,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		contentBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmDevice device = alarmInfoList.get(pos).getAlarm();
				Intent intent = new Intent(context,AlarmContentActivity.class);
				intent.putExtra("alarmDevice", device);
				context.startActivity(intent);
			}
		});

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private AlarmActivity getAlarmActivity() {
		return (AlarmActivity) context;
	}

	public void cancel(boolean isCanceld) {
		if (imgLoadTask!=null) {
			imgLoadTask.setCancel(isCanceld);
		}
	}
}
