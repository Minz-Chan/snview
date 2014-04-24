package com.starnet.hdview.images;

import java.util.List;
import com.starnet.hdview.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ImagesExpandableListAdapter extends BaseExpandableListAdapter {

	private static final String TAG = "ImagesExpandableListAdapter";
	private List<ImagesGroup> mGroupList;
	private ImagesManagerActivity mImagesActivity;
	private LayoutInflater mLayoutInflater;
	private int mThumbnailSelectedCount;

	public ImagesExpandableListAdapter(
			ImagesManagerActivity imagesManagerActivity,
			List<ImagesGroup> imageGroupList) {
		this.mImagesActivity = imagesManagerActivity;
		this.mGroupList = imageGroupList;
		this.mLayoutInflater = ((LayoutInflater) imagesManagerActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mGroupList.get(groupPosition).getChildList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(
					R.layout.images_listview_thumbnail_layout, null);
		}

		ImagesGridView imageGridView = (ImagesGridView) convertView
				.findViewById(R.id.images_listview_image_gridview);
		imageGridView.setAdapter(new ImagesGridViewAdapter(mImagesActivity,
				((ImagesGroup) mGroupList.get(groupPosition))
						.getThumbnailList()));

		imageGridView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

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
		return mGroupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroupList.size();
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
					R.layout.images_listview_date_info_layout, null);
		}
		((TextView) convertView.findViewById(R.id.images_date_textview))
				.setText(((ImagesGroup) mGroupList.get(groupPosition))
						.getDateInfo());
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

}
