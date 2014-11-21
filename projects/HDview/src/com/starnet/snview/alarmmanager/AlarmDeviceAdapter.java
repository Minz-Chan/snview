package com.starnet.snview.alarmmanager;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.util.NetWorkUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmDeviceAdapter extends BaseExpandableListAdapter implements
		OnClickListener {
	
	private final int IAMGE_LOAD_DIALOG = 0x0013;
	private final int VIDEO_LOAD_DIALOG = 0x0014;

	private List<AlarmShowItem> alarmInfoList;
	private Context context;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.alarm_listview_item_layout, null);
		}
		TextView deviceTxt = (TextView) convertView.findViewById(R.id.device_item_name);
		deviceTxt.setText(alarmInfoList.get(groupPosition).getAlarm().getDeviceName());
		ImageView arrowImg = (ImageView) convertView
				.findViewById(R.id.alarm_arrow_img);
		if (alarmInfoList.get(groupPosition).isExpanded()) {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
		} else {
			arrowImg.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
		}
		// arrowImg.setImageResource(resId);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.alarm_listview_subitem_layout, null);
		}

		Button image_load_btn = (Button) convertView.findViewById(R.id.image_load_btn);
		Button video_load_btn = (Button) convertView.findViewById(R.id.video_load_btn);
		image_load_btn.setOnClickListener(this);
		video_load_btn.setOnClickListener(this);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.image_load_btn:
			if (NetWorkUtils.checkNetConnection(context)) {
				showImgLoadDialog(IAMGE_LOAD_DIALOG);
				String videoUrl = "";
				startImageLoadTask(videoUrl);//开启图片下载线程
			}else{
				Toast.makeText(context, context.getString(R.string.alarm_net_notopen), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.video_load_btn:
			if (NetWorkUtils.checkNetConnection(context)) {
				showImgLoadDialog(VIDEO_LOAD_DIALOG);
				String imageUrl = "";
				startVideoLoadTask(imageUrl);//开启视频下载线程
			}else {
				Toast.makeText(context, context.getString(R.string.alarm_net_notopen), Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private void showImgLoadDialog(int id) {
		getAlarmActivity().showDialog(id);
	}

	private void startVideoLoadTask(String videoUrl) {
		// TODO Auto-generated method stub
		
	}

	private void startImageLoadTask(String imageUrl) {
		// TODO Auto-generated method stub
		
	}
	
	private AlarmActivity getAlarmActivity(){
		return (AlarmActivity)context;
	}
}
