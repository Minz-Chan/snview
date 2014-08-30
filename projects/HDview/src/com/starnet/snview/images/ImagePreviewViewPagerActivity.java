package com.starnet.snview.images;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.images.utils.PhotoView;
import com.starnet.snview.images.utils.PhotoViewAttacher;
import com.starnet.snview.images.utils.PhotoViewAttacher.OnViewTapListener;
import com.starnet.snview.images.utils.SelfDefViewPager;
import com.starnet.snview.images.utils.PhotoViewAttacher.OnPhotoTapListener;

import android.R.layout;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ImagePreviewViewPagerActivity extends BaseActivity {// implements
																	// OnGestureListener

	final String TAG = "ImagePreviewViewPagerActivity";

	private Context context;
	// private ViewPager mViewPager;//原始的测试数据...
	private List<Bitmap> bitmapList;
	private int showSum;// 导航栏中总数
	private int showNum;// 导航栏中第几幅画面
	private ArrayList<String> pathList;// 画面的路径...
	private TextView imagepreview_title;// 显示设备的数量，以及显示画面的序号...
	private Button imagepreview_leftBtn;// 返回按钮

	private SelfDefViewPager mSelfDefViewPager;// 自定义的ViewPager可以判断是左滑，还是右滑
	
	private RelativeLayout mNavigationBar;
	private int click_time = 0 ;
	
	private Context photoContext;
	
	private PhotoViewAttacher mPhotoViewAttacher;//用于注册监听器
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setLeftButtonBg(R.drawable.image_manager_imagepreview_back_selector);
		super.getRightButton().setVisibility(View.GONE);
		super.setToolbarVisiable(false);
		super.hideRightButton();
		super.hideExtendButton();
		context = ImagePreviewViewPagerActivity.this;
		
		mNavigationBar = super.getNavbarContainer();
//		mNavigationBar.setVisibility(View.GONE);

		imagepreview_leftBtn = super.getLeftButton();
		imagepreview_title = super.getTitleView();

		Intent intent = getIntent();
		if (intent != null) {
			String imgPosInMap = intent.getStringExtra("imgPosInMap");
			String sumMap = intent.getStringExtra("sumMap");

			bitmapList = new ArrayList<Bitmap>();
			pathList = intent.getStringArrayListExtra("pathList");
			showSum = Integer.valueOf(sumMap);
			showNum = Integer.valueOf(imgPosInMap);
			imagepreview_title.setText("(" + showNum + "/" + showSum + ")");// 测试使用...

			int size = pathList.size();
			for (int i = 0; i < size; i++) {
				Bitmap mBitmap = BitmapFactory.decodeFile(pathList.get(i));
				bitmapList.add(mBitmap);
			}
		}
		setListenersForWadgets();
		int cur_pos = showNum - 1;
		mSelfDefViewPager = new SelfDefViewPager(context, imagepreview_title,showSum);
		mSelfDefViewPager.setAdapter(new SamplePagerAdapter(pathList));
		mSelfDefViewPager.setCurrentItem(cur_pos);
		setContentView(mSelfDefViewPager);
		// mViewPager = new HackyViewPager(this);//ViewPager测试数据
		// mViewPager.setAdapter(new
		// SamplePagerAdapter(pathList));//ViewPager测试数据
		// mViewPager.setCurrentItem(showNum);//ViewPager测试数据
		// setContentView(mViewPager);//ViewPager测试数据
	}

	private void setListenersForWadgets() {

		imagepreview_leftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImagePreviewViewPagerActivity.this.finish();
			}
		});
	}
	
	class SamplePagerAdapter extends PagerAdapter {

		private List<Drawable> drawableList;
		private List<String> drawPathList;
		private List<Bitmap> bitmapList;

		public SamplePagerAdapter(List<String> drawablePathList) {
			super();
			this.drawPathList = drawablePathList;
			drawableList = new ArrayList<Drawable>();
			bitmapList = new ArrayList<Bitmap>();
			int size = drawablePathList.size();
			for (int i = 0; i < size; i++) {
				Drawable drawable = BitmapDrawable.createFromPath(drawPathList
						.get(i));
				Bitmap mBitmap = BitmapFactory.decodeFile(drawPathList.get(i));
				bitmapList.add(mBitmap);
				drawableList.add(drawable);
			}
		}

		@Override
		public int getCount() {
			return drawableList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());

			photoView.setImageBitmap(bitmapList.get(position));
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			
			//根据路径获取图片，判断图片的类型（若是以jpg结尾的则是图片，支持一切的图片操作；若是以mp4结尾的则是视频，只支持视频的点击操作，进入视频播放页面）
			int mPostion = position ;
			String drawablePath = drawPathList.get(mPostion);
			String []names = drawablePath.split("\\.");
			
//			if (names[1].equals("jpg")) {
//				photoContext = container.getContext();
//				
//				photoView.setOnPhotoTapListener(onPhotoTapListener);
//				Log.i(TAG, "drawablePath:"+drawablePath);
//				Log.i(TAG, "names.length:"+names.length);
//			}else {
//				photoView.setOnPhotoTapListener(onPhotoTapListener);
//			}
			
			photoView.setOnViewTapListener(onViewTapListener);
//			mPhotoViewAttacher.setOnPhotoTapListener(onPhotoTapListener);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
	
	OnPhotoTapListener onPhotoTapListener = new OnPhotoTapListener() {
		
		@Override
		public void onPhotoTap(View view, float x, float y) {	
			int cur_postion = mSelfDefViewPager.getMPostion();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putInt("cur_postion", cur_postion);
			bundle.putInt("showSum", showSum);
			intent.putExtras(bundle);
			intent.setClass(photoContext, ImageManagerVideoPlayActivity.class);
			startActivity(intent);
		}
	};
	
	OnViewTapListener onViewTapListener = new OnViewTapListener() {
		
		@Override
		public void onViewTap(View view, float x, float y) {
			
			click_time++;
			if (click_time %2 != 0) {
				mNavigationBar.setVisibility(View.GONE);
			}else {
				mNavigationBar.setVisibility(View.VISIBLE);
			}
		}
	};
	
}