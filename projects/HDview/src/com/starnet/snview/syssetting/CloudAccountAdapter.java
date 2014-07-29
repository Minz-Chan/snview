package com.starnet.snview.syssetting;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.devicemanager.DeviceItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class CloudAccountAdapter extends BaseAdapter {
	
	private static final String TAG = "CloudAccountAdapter";
	
	private Context mContext;
	
	private List<CloudAccount> mCloudAccountList;
	private LayoutInflater mLayoutInflater;

	public CloudAccountAdapter(Context context, List<CloudAccount> mCloudAccountList) {
		super();
		this.mContext = context;
		this.mCloudAccountList = mCloudAccountList;
		this.mLayoutInflater = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}
	
	@Override
	public int getCount() {
		return mCloudAccountList.size();
	}

	@Override
	public Object getItem(int position) {
		return mCloudAccountList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = this.mLayoutInflater.inflate(
					R.layout.cloudaccount_listview_item_layout, null);
		}
		
		CloudAccount item  = mCloudAccountList.get(position);
		
		TextView cloudaccountItemName = (TextView) convertView.findViewById(R.id.cloudaccount_item_name);
		
		cloudaccountItemName.setText(item.getUsername());
		
		ImageButton isenable = (ImageButton) convertView.findViewById(R.id.account_listview_item_enable);
		if (item.isEnabled()) {
			isenable.setBackgroundResource(R.drawable.device_listview_item_securityprotection_on);
		} else {
			isenable.setBackgroundResource(R.drawable.device_listview_item_securityprotection_off);
		}
		
		return convertView;
	}

}
