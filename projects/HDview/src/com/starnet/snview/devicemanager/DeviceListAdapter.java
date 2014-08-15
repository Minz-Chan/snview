package com.starnet.snview.devicemanager;

import java.util.List;

import com.starnet.snview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class DeviceListAdapter extends BaseAdapter {
//	private static final String TAG = "DeviceListAdapter";
	
	private Context mContext;
	
	private List<DeviceItem> mDeviceList;
	private LayoutInflater mLayoutInflater;
	
	
	public DeviceListAdapter(Context context, List<DeviceItem> mDeviceList) {
		super();
		this.mContext = context;
		this.mDeviceList = mDeviceList;
		this.mLayoutInflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}

	@Override
	public int getCount() {
		int size = 0;
		if (mDeviceList!=null) {
			size = mDeviceList.size();
		}
		return size;
	}

	@Override
	public Object getItem(int position) {
		return mDeviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = this.mLayoutInflater.inflate(R.layout.device_listview_item_layout_other, null);
		}
		
		DeviceItem item  = mDeviceList.get(position);
		
		TextView deviceItemName = (TextView) convertView.findViewById(R.id.device_item_name);
		/*TextView deviceIp  = (TextView) convertView.findViewById(R.id.device_listview_item_ip);*/
		
		/*deviceIp.setText("IP:"+item.getSvrIp()+":"+item.getSvrPort()+"  "+item.getChannelSum());*/
		String deviceName = item.getDeviceName();
		if (deviceName.length() >= 4) {
			String word1 = mContext.getString(R.string.device_manage_offline_en);
			String word2 = mContext.getString(R.string.device_manage_offline_cn);
			String word3 = mContext.getString(R.string.device_manage_online_cn);
			String word4 = mContext.getString(R.string.device_manage_online_en);
			
			String wordLen = mContext.getString(R.string.device_manage_off_on_line_length);
			int len = Integer.valueOf(wordLen);
			String dName = deviceName.substring(0, len);
			
			if(dName.contains(word1)||dName.contains(word2)||dName.contains(word3)||dName.contains(word4)){
				deviceName = deviceName.substring(len);
			}
		}
		
		deviceItemName.setText(deviceName);
		
		ImageButton securityProtection = (ImageButton) convertView.findViewById(R.id.device_listview_item_securityprotection);
		if (item.isSecurityProtectionOpen()) {
			securityProtection.setBackgroundResource(R.drawable.device_listview_item_securityprotection_on);
		} else {
			securityProtection.setBackgroundResource(R.drawable.device_listview_item_securityprotection_off);
		}
		
		return convertView;
	}

}
