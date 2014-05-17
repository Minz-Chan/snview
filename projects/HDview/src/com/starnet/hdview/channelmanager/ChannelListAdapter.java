package com.starnet.hdview.channelmanager;

import java.util.List;

import com.starnet.hdview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelListAdapter extends BaseAdapter {

	private static final String TAG = "ChannelListAdapter";
	private Context mContext;
	private List<Channel> mChannelList;
	private LayoutInflater mLayoutInflater;
	
	ChannelListAdapter(Context context, List<Channel> channelList) {
		this.mContext = context;
		this.mChannelList = channelList;
		this.mLayoutInflater = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}
	
	@Override
	public int getCount() {
		return mChannelList.size();
	}

	@Override
	public Object getItem(int position) {
		return mChannelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = this.mLayoutInflater.inflate(
					R.layout.channel_listview_channel_item_layout, null);
		}
		
		final Channel c = mChannelList.get(position);
		
		TextView channelItemName = (TextView) convertView.findViewById(R.id.channel_listview_channel_item_name);
		TextView channelItemTextPre = (TextView) convertView.findViewById(R.id.channel_listview_channel_item_text_prefix);
		ImageView chkboxIsSelected = (ImageView) convertView.findViewById(R.id.channel_listview_channel_item_chkbox);
		
		channelItemName.setText(c.getChannelName());
		
		if (c.isSelected()) {
			chkboxIsSelected.setBackgroundResource(R.drawable.channel_listview_selected);
			channelItemName.setTextColor(getColor(R.color.channel_listview_text_selected));
			channelItemTextPre.setTextColor(getColor(R.color.channel_listview_text_selected));
		} else {
			chkboxIsSelected.setBackgroundResource(R.drawable.channel_listview_unselected);
			channelItemName.setTextColor(getColor(R.color.channel_listview_text_unselected));
			channelItemTextPre.setTextColor(getColor(R.color.channel_listview_text_unselected));
		} 
		
		
//		chkboxIsSelected.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (c.isSelected()) {
//					c.setSelected(false);
//				} else {
//					c.setSelected(true);
//				}
//				
//				ChannelListAdapter.this.notifyDataSetChanged();
//			}
//		});
		
		return convertView;
	}
	
	private int getColor(int resid) {
		return mContext.getResources().getColor(resid);
	}

}
