package com.starnet.snview.images;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.R.integer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.images.myutils.FileManager;
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
				if (!mIsEdit) {	// 菜单按钮
					
					
				} else {	// 退出图像管理编辑状态
					switch2EditStatus(false);;
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
				} else {	// 删除所选图片
					Log.i(TAG, "处在编辑状态");					
//					Iterator<ImagesGroup> imgGroupIterator = mImagesThumbnailGroupList.iterator();
//					while (imgGroupIterator.hasNext()) {
//						ImagesGroup imgGroup = imgGroupIterator.next();
//						List<Image> imgList = imgGroup.getThumbnailList();
//						Iterator<Image> imgIterator = imgList.iterator();
//						while (imgIterator.hasNext()) {
//							Image img = imgIterator.next();//出现异常,因为删除的数目多于2个时，在进行第二次遍历的时候，会出现已经删除....
//							if(img.isSelected()){
//								mImagesManager.deleteImage(img);
//							}
//						}
//					}
					mImagesManager.deleteSelectedImages();
					
					
					for (int i = 0; i < mImagesThumbnailGroupList.size(); i++) {
						int size = mImagesThumbnailGroupList.get(i).getGroupSize();
						if (size == 0) {
							mImagesThumbnailGroupList.remove(i);//需要remove掉，整个组...
						}
					}
					mExpandableListAdapter.notifyDataSetChanged();//更新图像管理
//						FileManager mFileManager = FileManager.getInstance();
//						String sdPath = Environment.getExternalStorageDirectory().getPath();
//						Log.v(TAG, sdPath);
//						String filePath1 = sdPath+"/SNview/capture/"+datePath;
//						String filePath2 = sdPath+"/SNview/record/"+datePath;
//						String filePath3 = sdPath+"/SNview/thumbnails/"+datePath;
//						boolean isNull = mFileManager.checkFileIsNull(filePath3);
//						if(isNull){
//							mImagesThumbnailGroupList.remove(i);
//							mFileManager.deleteFile(filePath3);
//						}
					
					
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
	
}
