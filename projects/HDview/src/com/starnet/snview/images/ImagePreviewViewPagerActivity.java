package com.starnet.snview.images;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.images.myutils.DragImageView;
import com.starnet.snview.images.utils.HackyViewPager;
import com.starnet.snview.images.utils.ImagePreviewOnPageChanger;
import com.starnet.snview.images.utils.PhotoView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ImagePreviewViewPagerActivity extends BaseActivity {
	
	private final String TAG = "ImagePreviewViewPagerActivity";

	private ViewPager mViewPager;
	private ImageView showImageView;//显示画面的图像视图...
	
	private PhotoView photoView;
	
	private DragImageView dragImageView;
	ViewTreeObserver viewTreeObserver;
	private int window_width, window_height;// 控件宽度
	private int state_height;// 状态栏的高度
	
	private int showSum ;//导航栏中总数
	private int showNum ;//导航栏中第几幅画面
	private ArrayList<String> pathList;//画面的路径...
	private TextView imagepreview_title_image_num;// 显示设备的数量，以及显示画面的序号...
	private Button imagepreview_leftBtn;// 返回按钮
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_manager_imagepreview_dragimageview_activity);
		super.setLeftButtonBg(R.drawable.image_manager_imagepreview_back_selector);
		super.getRightButton().setVisibility(View.GONE);
		super.setToolbarVisiable(false);
		super.hideRightButton();
		super.hideExtendButton();
		Context context = ImagePreviewViewPagerActivity.this;
//		photoView = (PhotoView) findViewById(R.id.imagepreview_imageView);
		dragImageView = (DragImageView) findViewById(R.id.imagepreview_imageView);

		imagepreview_leftBtn = super.getLeftButton();
		imagepreview_title_image_num = super.getTitleView();
		Intent intent = getIntent();
		if (intent != null) {
			String imgPosInMap = intent.getStringExtra("imgPosInMap");
			String sumMap = intent.getStringExtra("sumMap");
			String imagePath = intent.getStringExtra("imagePath");
			
			pathList = intent.getStringArrayListExtra("pathList");
			showSum = Integer.valueOf(sumMap);
			showNum = Integer.valueOf(imgPosInMap);
			imagepreview_title_image_num.setText("("+showNum+"/"+showSum+")");// 测试使用...
			
			Bitmap mBitmap = BitmapFactory.decodeFile(imagePath);
			dragImageView.setImageBitmap(mBitmap);
			dragImageView.setmActivity(ImagePreviewViewPagerActivity.this);
			WindowManager manager = getWindowManager();
			window_width = manager.getDefaultDisplay().getWidth();
			window_height = manager.getDefaultDisplay().getHeight();
			
			viewTreeObserver = dragImageView.getViewTreeObserver();
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					if (state_height == 0) {
						// 获取状况栏高度
						Rect frame = new Rect();
						getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
						state_height = frame.top;
						dragImageView.setScreen_H(window_height-state_height);
						dragImageView.setScreen_W(window_width);
					}

				}
			});
//			photoView = new PhotoView(context);
//			photoView.setImageBitmap(mBitmap);
			
		}
		
		
//		mViewPager = new HackyViewPager(this);
////		mViewPager.setCurrentItem(item);//设置当前的选项...
//		mViewPager.setCurrentItem(showNum);
//		setContentView(mViewPager);
//		ImagePreviewOnPageChanger ipopChanger = new ImagePreviewOnPageChanger(imagepreview_title_image_num);
//		mViewPager.setOnPageChangeListener(ipopChanger);
//		mViewPager.setAdapter(new SamplePagerAdapter(pathList));
		
		setListenersForWadgets();
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

		private List<Drawable> drawableList ;
		private List<String> drawPathList;
		private List<Bitmap> bitmapList ;
		
		
		public SamplePagerAdapter(List<String> drawablePathList) {
			super();
			this.drawPathList = drawablePathList;
			drawableList = new ArrayList<Drawable>();
			bitmapList = new ArrayList<Bitmap>();
			int size = drawablePathList.size();
			for (int i = 0; i < size; i++) {
				Drawable drawable = BitmapDrawable.createFromPath(drawPathList.get(i));
				Bitmap mBitmap = BitmapFactory.decodeFile(drawPathList.get(i));
				bitmapList.add(mBitmap);
				drawableList.add(drawable);
			}
		}

//		private static int[] sDrawables = { R.drawable.zz_test_image,
//				R.drawable.demo_bg,R.drawable.ic_launcher,R.drawable.images_failure_bg};

		@Override
		public int getCount() {
//			int size = bitmapList.size();
//			return size;
			return drawableList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
//			photoView.setImageResource(sDrawables[position]);
//			int localNum = showNum - 1 ;
			int cur_pos = mViewPager.getCurrentItem();
			Log.v(TAG, "cur_pos:" + cur_pos);
			photoView.setImageBitmap(bitmapList.get(position));
//			photoView.setImageDrawable(drawableList.get(position));
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.i(TAG, "postion--->" + position);
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}
}
