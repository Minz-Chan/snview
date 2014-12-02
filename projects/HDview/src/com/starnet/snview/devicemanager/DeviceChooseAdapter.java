package com.starnet.snview.devicemanager;

import java.util.List;

import com.starnet.snview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceChooseAdapter extends BaseAdapter {
	
	private Context context;//显示环境
	private List<DeviceItem> deviceList;//显示数据

	@Override
	public int getCount() {
		int size = 0;
		if(deviceList!= null){
			size = deviceList.size();
		}
		return size;
	}

	@Override
	public Object getItem(int position) {
		return deviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.device_manage_choose_item_baseactivity, null, true);
		}
		DeviceItem deviceItem = deviceList.get(position);
		
		TextView device_name = (TextView) convertView.findViewById(R.id.device_choose_text);
//		ImageView selectImag = (ImageView) convertView.findViewById(R.id.device_choose_image);
		
//		if(deviceItem.isExpanded()){
//			selectImag.setImageResource(R.drawable.channel_listview_selected);
//		}else {
//			selectImag.setImageResource(R.drawable.channel_listview_unselected);
//		}
		
		String deviceItemName = deviceItem.getDeviceName();
		device_name.setText(deviceItemName);
		return convertView;
	}

	public DeviceChooseAdapter(Context context,List<DeviceItem> deviceList) {
		super();
		this.context = context;
		this.deviceList = deviceList;
	}
}