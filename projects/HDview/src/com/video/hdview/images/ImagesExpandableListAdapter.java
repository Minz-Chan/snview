package com.video.hdview.images;

import java.util.ArrayList;
import java.util.List;

import com.video.hdview.R;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImagesExpandableListAdapter extends BaseExpandableListAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = "ImagesExpandableListAdapter";
	private List<ImagesGroup> mGroupList;
	private ImagesManagerActivity mImagesActivity;
	private LayoutInflater mLayoutInflater;
	private int mThumbnailSelectedCount = 0;
	
	private ArrayList<String> pathList = new ArrayList<String>();

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
			boolean isLastChild, View convertView, ViewGroup parent) {//子元素是由自定义的GridView构成...
		final int gPos = groupPosition;
		
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.images_listview_thumbnail_layout, null);
		}

		ImagesGridView imageGridView = (ImagesGridView) convertView.findViewById(R.id.images_listview_image_gridview);
		ImagesGridViewAdapter adapter = new ImagesGridViewAdapter(mImagesActivity,((ImagesGroup) mGroupList.get(groupPosition)).getThumbnailList());
		imageGridView.setAdapter(adapter);
		
		imageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//图片单击事件...
					@Override
					public void onItemClick(AdapterView<?> view, View parent,int position, long arg3) {
						
						ImagesManagerActivity activity = ImagesExpandableListAdapter.this.mImagesActivity;
						
						if (activity.getEditStatus()) {
							ImageView selectedImage = (ImageView) parent.findViewById(R.id.images_thumbnail_item_selected_bg_imageview);
							Image image = ImagesExpandableListAdapter.this.mGroupList.get(gPos).getThumbnailList().get(position);
							
							if (!image.isSelected()) {
								image.setSelected(true);
								selectedImage.setVisibility(View.VISIBLE);
								ImagesExpandableListAdapter.this.mThumbnailSelectedCount += 1;
							} else {
								image.setSelected(false);
								selectedImage.setVisibility(View.GONE);
								ImagesExpandableListAdapter.this.mThumbnailSelectedCount -= 1;
							}
							// 显示已选择图片数量
							activity.setTitleText(ImagesExpandableListAdapter.this.mThumbnailSelectedCount);
						} else {	// 响应按钮事件
							int imgPosInMap = 0;
							for (int i = 0; i < gPos; i++) {
								imgPosInMap += ImagesExpandableListAdapter.this.mGroupList.get(i).getGroupSize();
							}
							int clickposition = position + 1;
							imgPosInMap += clickposition;
							int sumMap = 0 ;
							int groupSize = mGroupList.size();
							ArrayList<Image> my_imageList = new ArrayList<Image>();
							for (int i = 0; i < groupSize; i++) {
								sumMap += mGroupList.get(i).getGroupSize();
								List<Image> imageList = mGroupList.get(i).getThumbnailList();
								int imageSize = imageList.size();
								for (int j = 0; j < imageSize; j++) {
									pathList.add(imageList.get(j).getImagePath());
								}
								List<Image> temp_ImageList = mGroupList.get(i).getThumbnailList();
								int t_size = temp_ImageList.size();
								for (int j = 0; j < t_size; j++) {
									my_imageList.add(temp_ImageList.get(j));
								}
							}
							//点击进入到图片查看界面
							Intent intent = new Intent();
							Image image = ImagesExpandableListAdapter.this.mGroupList.get(gPos).getThumbnailList().get(position);
							String thumbnailsPath = image.getThumbnailsPath();
							String imagePath = image.getImagePath();
							intent.putExtra("imgPosInMap",String.valueOf(imgPosInMap));
							intent.putExtra("sumMap", String.valueOf(sumMap));
							intent.putExtra("imagePath", imagePath);
							intent.putStringArrayListExtra("pathList", pathList);
							intent.putExtra("thumbnailsPath", thumbnailsPath);

							intent.putParcelableArrayListExtra("imageList",my_imageList);
							intent.setClass(mImagesActivity,ImagePreviewViewPagerActivity.class);
							mImagesActivity.startActivityForResult(intent,20);
						}
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
		int size = mGroupList.size();
		return size;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {//组元素只显示日期...
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
	
	public List<ImagesGroup> getImageGroupList() {
		return mGroupList;
	}

	public void setThumbnailSelectedCount(int mThumbnailSelectedCount) {
		this.mThumbnailSelectedCount = mThumbnailSelectedCount;
	}

	
}
