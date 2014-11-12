package com.starnet.snview.alarmmanager;

import java.util.List;

import com.starnet.snview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AlarmDeviceAdapter extends BaseAdapter {
	
	private List <AlarmDevice> alarmInfoList;
	private Context context;
	
	public AlarmDeviceAdapter(List <AlarmDevice> alarmInfoList,Context context){
		this.alarmInfoList = alarmInfoList;
		this.context = context;
	}

	@Override
	public int getCount() {
		int size = 0;
		if(alarmInfoList!= null){
			size = alarmInfoList.size();
		}
		return size;
	}

	@Override
	public Object getItem(int position) {
		return alarmInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.alarm_listview_item_layout, null);
		}
		
		TextView device_item_name = (TextView) convertView.findViewById(R.id.device_item_name);	//设备名称
		device_item_name.setText(alarmInfoList.get(position).getDeviceName());
		
		TextView alarm_time_txt = (TextView) convertView.findViewById(R.id.alarm_time);			//设备报警时间
		alarm_time_txt.setText(alarmInfoList.get(position).getAlarm_time());
		
		TextView alarm_type_txt = (TextView) convertView.findViewById(R.id.alarm_type);			//设备报警类型
		alarm_type_txt.setText(alarmInfoList.get(position).getAlarm_type());
		
		return convertView;
	}

}
