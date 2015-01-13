package com.starnet.snview.syssetting;

import java.util.HashMap;
import java.util.List;

import com.starnet.snview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class AlarmUserAdapter extends BaseAdapter {

	private Context ctx;
	private boolean isAccept;
	private boolean isClickFlag;
	private LayoutInflater flater;
	private List<HashMap<String, Object>> mData;

	public AlarmUserAdapter(Context ctx, List<HashMap<String, Object>> mData,
			boolean isAccept) {
		this.ctx = ctx;
		this.mData = mData;
		this.isAccept = isAccept;
		this.isClickFlag = isAccept;
		flater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		int size = 0;
		if (mData != null) {
			size = mData.size();
		}
		return size;
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			convertView = flater
					.inflate(R.layout.alarmnotifyadapter_item, null);
			TextView txt = (TextView) convertView.findViewById(R.id.alarm_txt);
			HashMap<String, Object> map = mData.get(position);
			String content = map.get("text").toString();
			txt.setText("" + content);
			final ImageButton imgBtn = (ImageButton) convertView
					.findViewById(R.id.imgBtn);

			final TextView cnt = (TextView) convertView
					.findViewById(R.id.alarm_cnt);
			if (isAccept) {
				cnt.setText(ctx.getString(R.string.alarm_accept_open));
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
			} else {
				cnt.setText(ctx.getString(R.string.alarm_accept_off));
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			}
			imgBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isClickFlag) {
						isClickFlag = false;
						cnt.setText(ctx.getString(R.string.alarm_accept_off));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
					} else {
						isClickFlag = true;
						cnt.setText(ctx.getString(R.string.alarm_accept_open));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
					}
				}
			});
		} else if (position == 1) {
			convertView = flater.inflate(R.layout.alarmuseradapter_item, null);
			TextView cnt = (TextView) convertView.findViewById(R.id.pset_cnt);
			HashMap<String, Object> map = mData.get(position);
			String content = map.get("text").toString();
			if (content.length() >= 18) {
				content = content.substring(0, 18) + "...";
			}
			cnt.setText("" + content);
		}
		return convertView;
	}

	public boolean isClickFlag() {
		return isClickFlag;
	}

	public void setClickFlag(boolean isClickFlag) {
		this.isClickFlag = isClickFlag;
	}
}