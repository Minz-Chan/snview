package com.starnet.snview.channelmanager;

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

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.images.ImagesManagerActivity;

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
		Channel c11 = new Channel();
		c11.setChannelName("通道1");
		c11.setSelected(true);
		
		Channel c21 = new Channel();
		c21.setChannelName("通道2");
		c21.setSelected(false);
		
		l1.add(c11);
		l1.add(c21);
		
		DeviceItem d1 = new DeviceItem();
		d1.setDeviceName("上海");
		d1.setChannelList(l1);
		d1.setExpanded(false);
		
		
		
		ArrayList<Channel> l2 = new ArrayList<Channel>();
		Channel c12 = new Channel();
		c12.setChannelName("通道1");
		c12.setSelected(false);
		
		Channel c22 = new Channel();
		c22.setChannelName("通道2");
		c22.setSelected(true);
		
		Channel c32 = new Channel();
		c32.setChannelName("通道3");
		c32.setSelected(true);
		
		l2.add(c12);
		l2.add(c22);
		l2.add(c32);

		DeviceItem d2 = new DeviceItem();
		d2.setDeviceName("福州");
		d2.setChannelList(l2);
		d2.setExpanded(true);
		
		
		
		ArrayList<Channel> l3 = new ArrayList<Channel>();
		Channel c13 = new Channel();
		c13.setChannelName("通道1");
		c13.setSelected(true);
		
		Channel c23 = new Channel();
		c23.setChannelName("通道2");
		c23.setSelected(false);
		
		Channel c33 = new Channel();
		c33.setChannelName("通道3");
		c33.setSelected(false);
		
		Channel c43 = new Channel();
		c43.setChannelName("通道4");
		c43.setSelected(true);
		
		l3.add(c13);
		l3.add(c23);
		l3.add(c33);
		l3.add(c43);

		DeviceItem d3 = new DeviceItem();
		d3.setDeviceName("深圳");
		d3.setChannelList(l3);
		d3.setExpanded(false);
		
		
		
		ArrayList<Channel> l4 = new ArrayList<Channel>();
		Channel c14 = new Channel();
		c14.setChannelName("通道1");
		c14.setSelected(false);
		
		Channel c24 = new Channel();
		c24.setChannelName("通道2");
		c24.setSelected(true);
		
		l4.add(c14);
		l4.add(c24);
		
		DeviceItem d4 = new DeviceItem();
		d4.setDeviceName("北京");
		d4.setChannelList(l4);
		d4.setExpanded(false);
		
		
		
		ArrayList<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		deviceList.add(d1);
		deviceList.add(d2);
		deviceList.add(d3);
		deviceList.add(d4);
		
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
