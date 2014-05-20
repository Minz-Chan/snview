package com.starnet.snview.channelmanager;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.images.ImagesGroup;
import com.starnet.snview.images.ImagesManagerActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceExpandableListAdapter extends BaseExpandableListAdapter {
	
	private static final String TAG = "DeviceExpandableListAdapter";
	private ChannelListActivity mChannelListActivity;
	private LayoutInflater mLayoutInflater;
	
	private List<DeviceItem> mDeviceList;
	
	
	public DeviceExpandableListAdapter(
			ChannelListActivity channelListActivity,
			List<DeviceItem> deviceList) {
		this.mChannelListActivity = channelListActivity;
		this.mDeviceList = deviceList;
		this.mLayoutInflater = ((LayoutInflater) channelListActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mDeviceList.get(groupPosition).getChannelList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = this.mLayoutInflater.inflate(
					R.layout.channel_listview_channel_layout, null);
		}
		
		ListView channelListView = (ListView) convertView.findViewById(R.id.channel_sublistview);
		channelListView.setAdapter(new ChannelListAdapter(mChannelListActivity, 
				mDeviceList.get(groupPosition).getChannelList()));
		
		int channelCount = mDeviceList.get(groupPosition).getChannelList().size();
		float itemHeight = mChannelListActivity.getResources().getDimension(R.dimen.channel_listview_channel_item_height);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, (int)(itemHeight * channelCount));
		channelListView.setLayoutParams(params);
		
		
		channelListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				Channel c = (Channel) parent.getItemAtPosition(position);
				
				if (c.isSelected()) {
					c.setSelected(false);
				} else {
					c.setSelected(true);
				}
				
				((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();				
			}
		});
		
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mDeviceList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mDeviceList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(
					R.layout.channel_listview_device_item_layout, null);
		}
		
		TextView deviceItemName = (TextView) convertView.findViewById(R.id.channel_listview_device_item_name);
		deviceItemName.setText(mDeviceList.get(groupPosition).getDeviceName());
		
		ImageView itemIcon = (ImageView) convertView.findViewById(R.id.channel_listview_device_item_icon);
		ImageView arrow = (ImageView) convertView.findViewById(R.id.channel_listview_arrow);
		
		if (mDeviceList.get(groupPosition).isExpanded()) {
			convertView.setBackgroundColor(getColor(R.color.channel_listview_device_item_bg_expanded));
			itemIcon.setBackgroundResource(R.drawable.channel_listview_device);
			arrow.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
			((ExpandableListView) parent).expandGroup(groupPosition);
		} else {
			convertView.setBackgroundColor(getColor(R.color.channel_listview_device_item_bg_collapsed));
			itemIcon.setBackgroundResource(R.drawable.channel_listview_device);
			arrow.setBackgroundResource(R.drawable.channel_listview_right_arrow);
			((ExpandableListView) parent).collapseGroup(groupPosition);
		}
		
		
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private int getColor(int resid) {
		return mChannelListActivity.getResources().getColor(resid);
	}
	
}
