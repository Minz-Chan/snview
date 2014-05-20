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
	private static final String TAG = "DeviceListAdapter";
	
	private Context mContext;
	
	private List<DeviceItem> mDeviceList;
	private LayoutInflater mLayoutInflater;
	
	
	public DeviceListAdapter(Context context, List<DeviceItem> mDeviceList) {
		super();
		this.mContext = context;
		this.mDeviceList = mDeviceList;
		this.mLayoutInflater = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}

	@Override
	public int getCount() {
		return mDeviceList.size();
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
			convertView = this.mLayoutInflater.inflate(
					R.layout.device_listview_item_layout, null);
		}
		
		DeviceItem item  = mDeviceList.get(position);
		
		TextView deviceItemName = (TextView) convertView.findViewById(R.id.device_item_name);
		
		deviceItemName.setText(item.getDeviceName());
		
		ImageButton securityProtection = (ImageButton) convertView.findViewById(R.id.device_listview_item_securityprotection);
		if (item.isSecurityProtectionOpen()) {
			securityProtection.setBackgroundResource(R.drawable.device_listview_item_securityprotection_on);
		} else {
			securityProtection.setBackgroundResource(R.drawable.device_listview_item_securityprotection_off);
		}
		
		return convertView;
	}

}
