package com.starnet.hdview.channelmanager;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;

import com.starnet.hdview.R;
import com.starnet.hdview.component.BaseActivity;
import com.starnet.hdview.devicemanager.DeviceItem;
import com.starnet.hdview.images.ImagesManagerActivity;

public class ChannelListActivity extends BaseActivity {

	private ExpandableListView mExpandableListView;
	private DeviceExpandableListAdapter mExpandableListAdapter;
	private LayoutInflater mLayoutInflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.channel_listview_activity);
		
		initView();
		
	}
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_channel_list));
		super.setToolbarVisiable(false);
		
		mLayoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mExpandableListView = (ExpandableListView) findViewById(R.id.channel_listview);
		
		
		
		ArrayList<Channel> l1 = new ArrayList<Channel>();
		Channel c1 = new Channel();
		c1.setChannelName("通道1");
		c1.setSelected(true);
		
		Channel c2 = new Channel();
		c2.setChannelName("通道2");
		c2.setSelected(false);
		
		l1.add(c1);
		l1.add(c2);
		
		
		DeviceItem d1 = new DeviceItem();
		d1.setDeviceName("上海");
		d1.setChannelList(l1);
		d1.setExpanded(true);
		
		DeviceItem d2 = new DeviceItem();
		d2.setDeviceName("福州");
		d2.setChannelList(l1);
		d2.setExpanded(false);
		
		
		ArrayList<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		deviceList.add(d1);
		deviceList.add(d2);
		
		mExpandableListAdapter = new DeviceExpandableListAdapter(this, deviceList);
		
		mExpandableListView.setAdapter(mExpandableListAdapter);
		
//		for (int i = 0; i < mExpandableListAdapter.getGroupCount(); i++) {
//	          mExpandableListView.expandGroup(i);
//	    }
		
		mExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				
				DeviceItem device = (DeviceItem) parent.getExpandableListAdapter().getGroup(groupPosition);

				if (device.isExpanded()) {
					device.setExpanded(false);	
				} else {
					device.setExpanded(true);	
				}
				
				//v.invalidate();
				
				return false;
			}
			
		});
		

		
	}

	
	
}
