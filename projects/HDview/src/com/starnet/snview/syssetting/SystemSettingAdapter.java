package com.starnet.snview.syssetting;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.starnet.snview.R;

public class SystemSettingAdapter extends BaseAdapter {
	private LayoutInflater layoutInflater;
	private String inflater = Context.LAYOUT_INFLATER_SERVICE;
	private List<HashMap<String, Object>> mData;

	public SystemSettingAdapter(Context context, List<HashMap<String, Object>> mData) {
		this.mData = mData;
		layoutInflater = (LayoutInflater) context.getSystemService(inflater);
	}

	@Override
	public int getCount() {
		if (mData == null)
			return 0;
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if (mData == null)
			return null;
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RelativeLayout layout = (RelativeLayout) layoutInflater.inflate(
				R.layout.system_setting_list_adapter_item, null);

		ImageView iv2 = (ImageView) layout.findViewById(R.id.ivConfirm);
		iv2.setImageResource(R.drawable.arrow);
		
		TextView tv = (TextView) layout.findViewById(R.id.tvColorSet);
		String tvForwardDays = mData.get(position).get("text").toString();
		
		tv.setText(tvForwardDays);

		layout.setPadding(1, 0, 1, 0);
		return layout;
	}

}
