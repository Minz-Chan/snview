package com.starnet.snview.playback;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaybackChannelListViewAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private Context context = null;

	private List<Channel> channelList = null;// 用于显示的信息

	public PlaybackChannelListViewAdapter(Context context, List<Channel> channelList) {
		super();
		this.context = context;// 从上下文环境中获取加载器
		this.channelList = channelList;
	}
	@Override
	public int getCount() {
		int size = 0;
		if (channelList !=null) {
			size =channelList.size();
		}
		return size;
	}

	@Override
	public Object getItem(int position) {
		return channelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	//绘制界面的时候，若是选择过某个通道的话，保存起来，用于绘制界面的显示；
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mInflater = LayoutInflater.from(context);// 实例化一个填充器
			convertView = mInflater.inflate(R.layout.channel_listview_device_item_layout,null);
		}	
		
		TextView tv_channel = (TextView) convertView.findViewById(R.id.channel_listview_device_item_name);
		String channelName = channelList.get(position).getChannelName();
		tv_channel.setText(channelName);
		ImageView img = (ImageView) convertView.findViewById(R.id.channel_listview_device_item_chkbox);
		Channel channel = (Channel) getItem(position);
		if (channel.isSelected()) {
			img.setImageResource(R.drawable.channel_listview_selected);
		}else {
			img.setImageResource(R.drawable.channel_listview_unselected);
		}
		return convertView;
	}
	
	public void setChannelList(List<Channel> channelList){
		this.channelList = channelList;
	}

}
