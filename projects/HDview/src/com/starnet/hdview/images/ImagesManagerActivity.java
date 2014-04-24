package com.starnet.hdview.images;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.starnet.hdview.R;
import com.starnet.hdview.component.BaseActivity;
import com.starnet.hdview.global.GlobalApplication;
import com.starnet.hdview.util.ActivityUtility;

public class ImagesManagerActivity extends BaseActivity {

	private FrameLayout mBaseContentView;
	private ExpandableListView mExpandableListView;
	private ImagesExpandableListAdapter mExpandableListAdapter;
	
	private final ArrayList<ImagesGroup> mImagesThumbnailGroupList = new ArrayList();
	
	private ImagesManager mImagesManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.images_manager_activity);
		
		initViews();
		
		GlobalApplication.getInstance().setScreenWidth(ActivityUtility.getScreenSize(this).x);
		
		mImagesManager = ImagesManager.getInstance();
	}

	private void initViews() {
		TextView title = super.getTitleView();
		title.setText(R.string.navigation_title_picture_management);
		
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		super.setExtendBarVisible(false);
		
		mBaseContentView = ((FrameLayout)findViewById(R.id.base_content));
	    mBaseContentView.setBackgroundResource(R.color.list_view_bg);
	    mBaseContentView.setPadding(0, 0, 0, 0);
	    
	    mExpandableListView = (ExpandableListView) findViewById(R.id.images_listview);
		
	}
	
	private void updateImageGroupList()
	  {
	    this.mImagesThumbnailGroupList.clear();
	    List dateList = this.mImagesManager.getDateList();
	    if ((dateList == null) || (dateList.size() == 0)) {
	    	return;
	    } else {
	    	for (int i = 0; i < dateList.size(); i++) {
	    		String str = (String)dateList.get(i);
		        List imageList = this.mImagesManager.getImageListForDate(str);

		        ImagesGroup imagesGroup = new ImagesGroup(str, imageList);
		        this.mImagesThumbnailGroupList.add(imagesGroup);
	    	}
	    }
	   
	  }


	private boolean loadThumbnailsInBackground() {
		mImagesManager.loadLocalImages();
		updateImageGroupList();
		return true;
	}
	
	
	
	
	@Override
	protected void onDestroy() {
		ImageLoader.getInstance().release();
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	    LoadThumbnailTask localLoadThumbnailTask = new LoadThumbnailTask();
	    Object[] arrayOfObject = new Object[3];
	    arrayOfObject[0] = null;
	    arrayOfObject[1] = null;
	    arrayOfObject[2] = null;
	    localLoadThumbnailTask.execute(arrayOfObject);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private class LoadThumbnailTask extends AsyncTask<Object, Object, Object> {

		public LoadThumbnailTask() {}
		
		@Override
		protected Object doInBackground(Object... params) {
			return Boolean.valueOf(loadThumbnailsInBackground());
		}

		@Override
		protected void onPostExecute(Object result) {
			if (((Boolean)result).booleanValue()) {
				
				mExpandableListAdapter = new ImagesExpandableListAdapter(ImagesManagerActivity.this, ImagesManagerActivity.this.mImagesThumbnailGroupList);
		        mExpandableListView.setAdapter(ImagesManagerActivity.this.mExpandableListAdapter);
		        
		        
		        if (ImagesManagerActivity.this.mExpandableListAdapter != null) {
					ImagesManagerActivity.this.mExpandableListAdapter.notifyDataSetChanged();
			        for (int i = 0; i < ImagesManagerActivity.this.mExpandableListAdapter.getGroupCount(); i++) {
			          ImagesManagerActivity.this.mExpandableListView.expandGroup(i);
			        }
			     }
		        
		        if (ImagesManagerActivity.this.mImagesThumbnailGroupList.size() > 0) {
		        	ImagesManagerActivity.this.mExpandableListView.expandGroup(0);
		        }
			}
	

		}
		
		
	}
	
}
