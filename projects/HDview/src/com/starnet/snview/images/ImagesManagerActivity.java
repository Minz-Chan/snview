package com.starnet.snview.images;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.util.ActivityUtility;

public class ImagesManagerActivity extends BaseActivity {
	
	private final String TAG = "ImagesManagerActivity";

	private FrameLayout mBaseContentView;//框布局...
	private ExpandableListView mExpandableListView;//表示加载图片的扩展列表...
	private ImagesExpandableListAdapter mExpandableListAdapter;//ExpandableListView的适配器...
	
	private final ArrayList<ImagesGroup> mImagesThumbnailGroupList = new ArrayList<ImagesGroup>();
	
	private ImagesManager mImagesManager;
	
	
	private boolean mIsEdit = false;	// 是否处于编辑状态
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.images_manager_activity);
		
		//setBackPressedExitEventValid(true);
		
		initViews();
		
		setListeners();
		
		GlobalApplication.getInstance().setScreenWidth(ActivityUtility.getScreenSize(this).x);
		
		mImagesManager = ImagesManager.getInstance();
	}

	private void initViews() {
		TextView title = super.getTitleView();
		title.setText(R.string.navigation_title_picture_management);
		
		
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_edit_btn_selector);
		super.setToolbarVisiable(false);
		//super.setExtendBarVisible(false);
		
		mBaseContentView = ((FrameLayout)findViewById(R.id.base_content));
	    mBaseContentView.setBackgroundResource(R.color.list_view_bg);
	    mBaseContentView.setPadding(0, 0, 0, 0);
	    
	    mExpandableListView = (ExpandableListView) findViewById(R.id.images_listview);
		
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mIsEdit) {	// 菜单按钮，处在非编辑状态，应该可以直接拉出抽屉菜单
					
					
				} else {	// 退出图像管理编辑状态
					switch2EditStatus(false);
					mIsEdit = false;
				}
			}
		});
		
		super.getRightButton().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (!mIsEdit) {	// 进入图像管理编辑状态
					switch2EditStatus(true);					
					mIsEdit = true;
				} else {	// 删除所选图片,进入非编辑状态...
					Log.i(TAG, "处在编辑状态");
					mImagesManager.deleteSelectedImages();
					
					for (int i = 0; i < mImagesThumbnailGroupList.size(); i++) {
						int size = mImagesThumbnailGroupList.get(i).getGroupSize();
						if (size == 0) {
							mImagesThumbnailGroupList.remove(i);//需要remove掉，整个组...
						}
					}
					mExpandableListAdapter.notifyDataSetChanged();//更新图像管理
					//更新导航栏....
					mIsEdit = false;
					switch2EditStatus(false);//转化为非编辑状态...????/
				}
			}
		});
	}
	
	private void updateImageGroupList()
	  {
	    this.mImagesThumbnailGroupList.clear();
	    List<String> dateList = this.mImagesManager.getDateList();
	    if ((dateList == null) || (dateList.size() == 0)) {
	    	return;
	    } else {
	    	for (int i = 0; i < dateList.size(); i++) {
	    		String str = (String)dateList.get(i);
		        List<Image> imageList = this.mImagesManager.getImageListForDate(str);

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
	
	public boolean getEditStatus() {
		return mIsEdit;
	}
	
	private void switch2EditStatus(boolean isOrnot) {
		if (isOrnot) {
			ImagesManagerActivity.this
					.setNavbarBgFromColor(ImagesManagerActivity.this
							.getResources().getColor(
									R.color.navigation_bar_red_bg));
			ImagesManagerActivity.this
					.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
			ImagesManagerActivity.this
					.setRightButtonBg(R.drawable.navigation_bar_del_btn_selector);
		} else {
			ImagesManagerActivity.this
					.setNavbarBgFromColor(ImagesManagerActivity.this
							.getResources().getColor(
									R.color.navigation_bar_blue_bg));
			ImagesManagerActivity.this
					.setLeftButtonBg(R.drawable.navigation_bar_menu_btn_selector);
			ImagesManagerActivity.this
					.setRightButtonBg(R.drawable.navigation_bar_edit_btn_selector);
			
			setTitleText(0);
			mExpandableListAdapter.setThumbnailSelectedCount(0);
			
			for (ImagesGroup ig : mExpandableListAdapter.getImageGroupList()) {
				for (Image img : ig.getThumbnailList()) {
					img.setSelected(false);
				}
			}
			
			mExpandableListView.invalidateViews();						
		}
	}
	
	public void setTitleText(int count) {
		String imageTitle = this.getResources().getString(R.string.navigation_title_picture_management);
		if (count > 0) {
			String oldTitle = imageTitle;
			StringBuilder newTitle = new StringBuilder(oldTitle);
			newTitle.append("(");
			newTitle.append(count);
			newTitle.append(")");
			super.setTitleViewText(newTitle.toString());
		} else {
			super.setTitleViewText(imageTitle);
		}
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.v(TAG, "data"+data);
		
	}
	
	
}