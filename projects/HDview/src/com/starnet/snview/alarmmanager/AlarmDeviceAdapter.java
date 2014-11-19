package com.starnet.snview.alarmmanager;

import java.util.List;

import com.starnet.snview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class AlarmDeviceAdapter extends BaseExpandableListAdapter  {
	
	private List <AlarmDevice> alarmInfoList;
	private Context context;
	
	public AlarmDeviceAdapter(List <AlarmDevice> alarmInfoList,Context context){
		this.alarmInfoList = alarmInfoList;
		this.context = context;
	}

	@Override
	public int getGroupCount() {
		int size = 0 ;
		if (alarmInfoList!=null) {
			size = alarmInfoList.size() ;
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
		if (convertView==null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.alarm_listview_item_layout, null);
		}
		TextView deviceTxt = (TextView) convertView.findViewById(R.id.device_item_name);
		deviceTxt.setText(alarmInfoList.get(groupPosition).getDeviceName());
//		ImageView arrowImg = (ImageView) convertView.findViewById(R.id.alarm_arrow_img);
//		arrowImg.setImageResource(resId);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView==null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.alarm_listview_subitem_layout, null);
		}
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

}
