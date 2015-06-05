package com.video.hdview.playback;

import java.util.ArrayList;
import java.util.List;

import com.video.hdview.R;
import com.video.hdview.channelmanager.Channel;
import com.video.hdview.devicemanager.DeviceItem;
//import com.video.hdview.playback.utils.ConstantUtils;

import com.video.hdview.global.GlobalApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlayBackChannelListViewActivity extends Activity {
	
	private PlaybackChannelListViewAdapter adapter = null;// ListView的适配器
	private ListView myListView = null; // 显示列表listview
	private Context context = null;

	private DeviceItem clickDeviceItem;
	private List<Channel> channelList;
	private final int REQ = 0x0005;
	private Button button_cancel;
	private Button button_ok;
	private boolean isClickOk = false;

	private Intent intent;
	private int parentPos;
	private int childPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		channelList = new ArrayList<Channel>();
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		intent = getIntent();
		childPos = intent.getIntExtra("child", 0);
		parentPos = intent.getIntExtra("group", 0);

		// 判断点击的设备通道列表图标所对应的文档的用户
		int channelSize = 0;
		clickDeviceItem = (DeviceItem) intent.getSerializableExtra("device");
		channelList = clickDeviceItem.getChannelList();
		channelSize = channelList.size();
		// 判断通道数量的多少，如果数量比较多的话，则显示一个固定的界面；如果比较少的话，则根据通道数量的多少来显示界面的大小
		if (channelSize < 11) {
			setContentView(R.layout.channel_listview_channel_layout_other);
		} else {
			setContentView(R.layout.channel_listview_device_layout);
		}

		String titleName = clickDeviceItem.getDeviceName();
		String str1 = getString(R.string.device_manager_online_en);
		String str2 = getString(R.string.device_manager_offline_en);
		if (titleName.contains(str1) || titleName.contains(str2)) {
			titleName = titleName.substring(4);
		}

		PlayBackChannelListViewActivity.this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.about_titlebar_activity);

		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(titleName);
		((View) titleView.getParent().getParent()).setBackgroundColor(Color.BLACK);
		((View) titleView.getParent().getParent()).setPadding(0, 5, 0, 0);

		PlayBackChannelListViewActivity.this.setTitle(titleName);// 设置标题栏
		initWadgetsAndAddListeners();
	}

	private void initWadgetsAndAddListeners() {
		context = PlayBackChannelListViewActivity.this;
		myListView = (ListView) findViewById(R.id.channel_sublistview);
		button_cancel = (Button) findViewById(R.id.channel_listview_cancel);
		button_ok = (Button) findViewById(R.id.channel_listview_ok);
		
		adapter = new PlaybackChannelListViewAdapter(context, clickDeviceItem,channelList);
		myListView.setAdapter(adapter);

		button_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				GlobalApplication.getInstance().setStepOver(true);
				
//				SharedPreferences sp = getSharedPreferences("step_over_xml", Context.MODE_PRIVATE);
//				Editor editor = sp.edit();
//				editor.putBoolean("step_over", true);
//				editor.commit();
				
				PlaybackUtils.isClickOk = true;
				isClickOk = true;
				int channl = getChannelIndex();
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("child", childPos);
				bundle.putInt("group", parentPos);
				bundle.putInt("chnl", channl);
				bundle.putBoolean("okBtn", isClickOk);
				intent.putExtras(bundle);
				setResult(REQ, intent);
				PlayBackChannelListViewActivity.this.finish();

			}

			private int getChannelIndex() {
				int index = 0;
				int chSize = channelList.size();
				for (int i = 0; i < chSize; i++) {
					Channel channel = channelList.get(i);
					if (channel.isSelected()) {
						index = i;
						break;
					}
				}
				return index;
			}
		});

		myListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 方框显示按钮
				Channel channel = channelList.get(position);
				channel.setSelected(true);
				int channelSize = channelList.size();
				for (int i = 0; i < channelSize; i++) {
					if (i != position) {
						channelList.get(i).setSelected(false);
					}
				}
				adapter.notifyDataSetChanged();
			}
		});

		button_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayBackChannelListViewActivity.this.finish();
			}
		});
	}
}