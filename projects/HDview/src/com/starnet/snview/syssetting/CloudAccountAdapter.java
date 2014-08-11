package com.starnet.snview.syssetting;

//import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CloudAccountAdapter extends BaseAdapter {

	private Context mContext;
	private List<CloudAccount> mCloudAccountList;
	private LayoutInflater mLayoutInflater;
//	private List<Integer> posList = new ArrayList<Integer>();//用于记录需要显示不同颜色的位置

	public CloudAccountAdapter(Context context,List<CloudAccount> mCloudAccountList) {
		super();
		this.mContext = context;
		this.mCloudAccountList = mCloudAccountList;
		this.mLayoutInflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
//		int size = mCloudAccountList.size();
//		for(int i = 0; i < size; i++){
//			CloudAccount cloudAccount = mCloudAccountList.get(i);
//			if(!cloudAccount.isEnabled()){//不需要网络加载的用户，记录其位置...
//				posList.add(i);
//			}
//		}
	}

	@Override
	public int getCount() {
		int size = 0;
		if (mCloudAccountList != null) {
			size = mCloudAccountList.size();
		}
		return size;
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
			mLayoutInflater = LayoutInflater.from(mContext);
			convertView = mLayoutInflater.inflate(R.layout.cloudaccount_listview_item_layout, null);
		}
		CloudAccount item = mCloudAccountList.get(position);
		TextView caTitleName = (TextView) convertView.findViewById(R.id.cloudaccount_item_name);
		caTitleName.setText(item.getUsername());
		ImageView itemIcon = (ImageView) convertView.findViewById(R.id.imageView_user_photo);
		
//		boolean isContain = containPositon(position,posList);
//		if (isContain) {
//			itemIcon.setBackgroundResource(R.drawable.user_photo_noused);
//		}else {
//			itemIcon.setBackgroundResource(R.drawable.user_photo_select);
//		}
		
		if (item.isEnabled()) {
			itemIcon.setBackgroundResource(R.drawable.user_photo_select);
		}else{
			itemIcon.setBackgroundResource(R.drawable.user_photo_noused);
		}
		
		return convertView;
	}
	
//	private boolean containPositon(int groupPosition,List<Integer> pList){
//		boolean result = false;
//		int size = pList.size();
//		for(int i =0 ;i<size;i++){
//			if(pList.get(i) == groupPosition){
//				result = true;
//				break;
//			}
//		}
//		return result;
//	}
}