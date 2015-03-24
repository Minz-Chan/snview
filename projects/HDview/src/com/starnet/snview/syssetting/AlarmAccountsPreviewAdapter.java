package com.starnet.snview.syssetting;

import java.util.List;

import com.starnet.snview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlarmAccountsPreviewAdapter extends BaseAdapter {

	private Context mContext;
	private List<CloudAccount> mUserList;
	private LayoutInflater mLayoutInflater;

	public AlarmAccountsPreviewAdapter(Context context, List<CloudAccount> mUserList) {
		this.mContext = context;
		this.mUserList = mUserList;
		this.mLayoutInflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}

	@Override
	public int getCount() {
		int size = 0;
		if (mUserList != null) {
			size = mUserList.size();
		}
		return size;
	}

	@Override
	public Object getItem(int position) {
		return mUserList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			mLayoutInflater = LayoutInflater.from(mContext);
			convertView = mLayoutInflater.inflate(
					R.layout.cloudaccount_listview_item_layout, null);
		}
		CloudAccount item = mUserList.get(position);
		TextView caTitleName = (TextView) convertView
				.findViewById(R.id.cloudaccount_item_name);
		caTitleName.setText(item.getUsername());
		ImageView itemIcon = (ImageView) convertView
				.findViewById(R.id.imageView_user_photo);
		itemIcon.setBackgroundResource(R.drawable.user_photo_select);
		if (item.isEnabled()) {
			itemIcon.setBackgroundResource(R.drawable.user_photo_select);
		} else {
			itemIcon.setBackgroundResource(R.drawable.user_photo_select);
			// itemIcon.setBackgroundResource(R.drawable.user_photo_noused);
		}
		return convertView;
	}

}
