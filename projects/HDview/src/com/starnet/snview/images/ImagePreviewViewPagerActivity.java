package com.starnet.snview.images;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.images.Image.ImageType;
import com.starnet.snview.images.utils.PhotoView;
import com.starnet.snview.images.utils.PhotoViewAttacher.OnViewTapListener;
import com.starnet.snview.images.utils.SelfDefViewPager;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImagePreviewViewPagerActivity extends BaseActivity {
	final String TAG = "ImagePreviewViewPagerActivity";
	private Context context;
	// private ViewPager mViewPager;//原始的测试数据...
	private int showSum;// 导航栏中总数
	private int showNum;// 导航栏中第几幅画面
	private ArrayList<String> pathList;// 画面的路径...
	private TextView imagepreview_title;// 显示设备的数量，以及显示画面的序号...
	private Button imagepreview_leftBtn;// 返回按钮

	private SelfDefViewPager mSelfDefViewPager;// 自定义的ViewPager可以判断是左滑，还是右滑
	private SelfPagerAdapter selfPagerAdapter;

	private FrameLayout nToolbar;
	private RelativeLayout mNavigationBar;// 导航栏...
	private int click_time = 0;
	private Context photoContext;
	private Button delete_button;
	private ViewTreeObserver viewTreeObserver;
	private PhotoView photoView;
	private int video_click_time = 0 ;
	
	private int screen_height ;//手机屏幕的高度

	private ImagesManager mImagesManager;
	List<Image> imageList = new LinkedList<Image>();

	// private PhotoViewAttacher mPhotoViewAttacher;//用于注册监听器

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setLeftButtonBg(R.drawable.image_manager_imagepreview_back_selector);
		super.getRightButton().setVisibility(View.GONE);
		super.setToolbarVisiable(true);
		super.hideRightButton();
		super.hideExtendButton();
		super.getToolbarContainer();
		context = ImagePreviewViewPagerActivity.this;
				
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screen_height = dm.heightPixels;

		nToolbar = super.getToolbarContainer();
		mNavigationBar = super.getNavbarContainer();
		// nToolbar.setBackgroundResource(R.drawable.imagepreview_backgroud_toolbar_blue_bg);

		mImagesManager = ImagesManager.getInstance();

		imagepreview_leftBtn = super.getLeftButton();
		imagepreview_title = super.getTitleView();

		Intent intent = getIntent();
		if (intent != null) {
			String imgPosInMap = intent.getStringExtra("imgPosInMap");
			String sumMap = intent.getStringExtra("sumMap");
			pathList = intent.getStringArrayListExtra("pathList");
			showSum = Integer.valueOf(sumMap);
			showNum = Integer.valueOf(imgPosInMap);
			imagepreview_title.setText("(" + showNum + "/" + showSum + ")");// 测试使用...

			imageList = intent.getParcelableArrayListExtra("imageList");
			int size = imageList.size();
			Log.v(TAG, "imageList size " + size);
		}

		int cur_pos = showNum - 1;
		mSelfDefViewPager = new SelfDefViewPager(context,imagepreview_title,showSum);
		selfPagerAdapter = new SelfPagerAdapter();
		mSelfDefViewPager.setAdapter(selfPagerAdapter);
		mSelfDefViewPager.setCurrentItem(cur_pos);
		setContentView(mSelfDefViewPager);

		RelativeLayout subLayout = new RelativeLayout(this);// 创建一个承装delete_button的相对布局

		RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		subLayout.setBackgroundColor(getResources().getColor(
				R.color.image_manager_delete_red));

		delete_button = new Button(context);//自定义删除按钮
		viewTreeObserver = delete_button.getViewTreeObserver();
		delete_button.setBackgroundResource(R.drawable.navigation_bar_del_btn_selector);
		delete_button.setHeight(LayoutParams.WRAP_CONTENT);
		delete_button.setWidth(LayoutParams.WRAP_CONTENT);

		subLayout.addView(delete_button, rParams);// 将delete_button承装在相对布局中
		nToolbar.addView(subLayout);// 将相对布局承装在nToolbar中
		ImageView mRightArrow = (ImageView) nToolbar.findViewById(R.id.base_toolbar_container_arrowright);
		mRightArrow.setVisibility(View.GONE);//隐藏小按钮
		viewTreeObserver.addOnGlobalLayoutListener(listener);

		setListenersForWadgets();
	}

	private void setListenersForWadgets() {

		imagepreview_leftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImagePreviewViewPagerActivity.this.finish();
			}
		});

		delete_button.setOnClickListener(new OnClickListener() {// 删除按钮...
					@Override
					public void onClick(View v) {

						Builder builder = new Builder(context);

						String title = getString(R.string.image_manager_imagepreview_delete_photo);
						builder.setTitle(title);
						String cancel = getString(R.string.image_manager_imagepreview_delete_cancel);
						builder.setNegativeButton(cancel, null);

						String ok = getString(R.string.image_manager_imagepreview_delete_ok);
						builder.setPositiveButton(ok,new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 考虑最后一个的删除问题...
										int ori_size = imageList.size();// 事实改变的...
										int cur_pos = mSelfDefViewPager.getCurrentItem();// 获取当前的视图...
										String imagePath = imageList.get(cur_pos).getImagePath();
										if (ori_size >= 2) {// 不止一张照片
											// 删除最后一张照片，总数、序数同时变
											if ((ori_size - 1 == cur_pos)) {
												Log.v(TAG, "delete last。。。。。");
												int m_ori_size = ori_size - 1;
												imagepreview_title.setText("("+ cur_pos + "/"+ m_ori_size + ")");// 改变显示...
												imageList.remove(cur_pos);// 移除掉...
												int m_cur_pos = cur_pos - 1;

												selfPagerAdapter.notifyDataSetChanged();
												showSum = m_ori_size;
												mSelfDefViewPager.setShowSum(m_ori_size);
												selfPagerAdapter = new SelfPagerAdapter();
												mSelfDefViewPager.setAdapter(selfPagerAdapter);
												mSelfDefViewPager.setCurrentItem(m_cur_pos);
												
												// 进行删除操作...
												List<String> dateList = mImagesManager.getDateList();
												if ((dateList == null)|| (dateList.size() == 0)) {
													return;
												} else {
													// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
													int flag = 0;
													for (int i = 0; i < dateList.size(); i++) {
														String str = dateList.get(i);
														List<Image> imageList = mImagesManager.getImageListForDate(str);
														int imgSize = imageList.size();
														for (int j = 0; j < imgSize; j++) {
															Image image = imageList.get(j);
															if (image.getImagePath().equals(imagePath)) {
																mImagesManager.deleteImage(image);// 根据路径删除文件....
																flag = 1;
																break;
															}
														}
														if (flag == 1) {
															break;
														}
													}
												}
											} else {// 删除中间照片时,总数在变，序数不变....
												if (cur_pos != 0) {// 不是第一张照片
													Log.v(TAG,"delete meidia。。。。。");
													int m_ori_size = ori_size - 1;
													imagepreview_title.setText("("+ cur_pos+ "/"+ m_ori_size+ ")");// 改变显示...
													imageList.remove(cur_pos);// 移除掉...

													selfPagerAdapter.notifyDataSetChanged();
													mSelfDefViewPager.setDelete_flag(true);
													mSelfDefViewPager.setShowSum(m_ori_size);
													selfPagerAdapter = new SelfPagerAdapter();
													mSelfDefViewPager.setAdapter(selfPagerAdapter);
													mSelfDefViewPager.setCurrentItem(cur_pos);
													showSum--;

													List<String> dateList = mImagesManager.getDateList();
													if ((dateList == null)|| (dateList.size() == 0)) {
														return;
													} else {
														// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
														int flag = 0;
														for (int i = 0; i < dateList.size(); i++) {
															String str = dateList.get(i);
															List<Image> imageList = mImagesManager.getImageListForDate(str);
															int imgSize = imageList.size();
															for (int j = 0; j < imgSize; j++) {
																Image image = imageList.get(j);
																if (image.getImagePath().equals(imagePath)) {
																	mImagesManager.deleteImage(image);// 根据路径删除文件....
																	flag = 1;
																	break;
																}
															}
															if (flag == 1) {
																break;
															}
														}
													}
												} else {// 是第一张照片时...
													Log.v(TAG,"delete first。。。。。");
													int m_ori_size = ori_size - 1;
													imagepreview_title.setText("(1/"+ m_ori_size+ ")");// 改变显示...
													imageList.remove(cur_pos);// 移除掉...

													selfPagerAdapter.notifyDataSetChanged();
													mSelfDefViewPager.setShowSum(m_ori_size);
													selfPagerAdapter = new SelfPagerAdapter();
													mSelfDefViewPager.setAdapter(selfPagerAdapter);
													mSelfDefViewPager.setCurrentItem(cur_pos);
													showSum--;

													List<String> dateList = mImagesManager.getDateList();
													if ((dateList == null)|| (dateList.size() == 0)) {
														return;
													} else {
														// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
														int flag = 0;
														for (int i = 0; i < dateList.size(); i++) {
															String str = dateList.get(i);
															List<Image> imageList = mImagesManager.getImageListForDate(str);
															int imgSize = imageList.size();
															for (int j = 0; j < imgSize; j++) {
																Image image = imageList.get(j);
																if (image.getImagePath().equals(imagePath)) {
																	mImagesManager.deleteImage(image);// 根据路径删除文件....
																	flag = 1;
																	break;
																}
															}
															if (flag == 1) {
																break;
															}
														}
													}
												}
											}
										} else {// 只有一张照片
											List<String> dateList = mImagesManager.getDateList();
											if ((dateList == null)|| (dateList.size() == 0)) {
												return;
											} else {// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
												int flag = 0;
												for (int i = 0; i < dateList.size(); i++) {
													String str = dateList.get(i);
													List<Image> imageList = mImagesManager.getImageListForDate(str);
													int imgSize = imageList.size();
													for (int j = 0; j < imgSize; j++) {
														Image image = imageList.get(j);
														if (image.getImagePath().equals(imagePath)) {
															mImagesManager.deleteImage(image);// 根据路径删除文件....
															flag = 1;
															break;
														}
													}
													if (flag == 1) {
														break;
													}
												}
											}
											ImagePreviewViewPagerActivity.this.finish();
										}
									}
								});
						builder.show();
					}
				});
	}

	OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {// 布局改变监听器...
			Log.v(TAG, "onGlobalLayout changed...");
		}
	};
	private String drawablePath;

	class SelfPagerAdapter extends PagerAdapter {

		public SelfPagerAdapter() {
			super();
		}

		@Override
		public int getCount() {
			int size = imageList.size();
			return size;
			// return pathList.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			photoView = new PhotoView(container.getContext());
			int mPostion = position;
			
			int m_pos = mSelfDefViewPager.getCurrentItem();
			Log.v(TAG, "m_pos:"+m_pos);
			final Image m_image = imageList.get(m_pos);
			if (m_image.getType() == ImageType.PICTURE) {
				
			}						
			final Image image = imageList.get(mPostion);
			drawablePath = image.getImagePath();
			if (image.getType() == ImageType.PICTURE) {
				Bitmap mBitmap = BitmapFactory.decodeFile(drawablePath);
				photoView.setImageBitmap(mBitmap);
				photoView.setZoomable(true);
				container.addView(photoView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				photoView.setOnViewTapListener(onViewTapListener);
				return photoView;
			} else  {//if (image.getType() == ImageType.VIDEO)
//				int pos = mSelfDefViewPager.getCurrentItem();
//				String path = imageList.get(pos).getImagePath();
				String path = imageList.get(mPostion).getImagePath();
				String jpgPath = swith2CapPath(path);
					
				View imageVideo = ((LayoutInflater) (context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE)))
						.inflate(R.layout.images_video_item, null, false);
				RelativeLayout imageVideoContent = (RelativeLayout) imageVideo
						.findViewById(R.id.images_video_content);
				ImageButton playBtn = (ImageButton) imageVideo
						.findViewById(R.id.images_video_play);
				Drawable bg = Drawable.createFromPath(jpgPath);

				int w = GlobalApplication.getInstance().getScreenWidth();
				int h = w * bg.getIntrinsicHeight() / bg.getIntrinsicWidth();
				
				imageVideoContent.setBackgroundDrawable(bg);
				imageVideoContent.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
				
				Log.i(TAG,
						"w:" + w + ", h:" + h + ", InstrinsicW:"
								+ bg.getIntrinsicWidth() + ", InstrinsicH:"
								+ bg.getIntrinsicHeight());
				
				container.addView(imageVideo, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				

				
				imageVideo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						video_click_time++;
						if (video_click_time % 2 != 0) {
							mNavigationBar.setVisibility(View.GONE);
							nToolbar.setVisibility(View.GONE);
						} else {
							mNavigationBar.setVisibility(View.VISIBLE);
							nToolbar.setVisibility(View.VISIBLE);
						}
					}
				});
				
				
				playBtn.setOnClickListener(new OnClickListener() {//添加播放事件
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						Log.v(TAG, "playBtn onclick drawablePath:"+drawablePath);
						int pos = mSelfDefViewPager.getCurrentItem();
						int cur_postion = mSelfDefViewPager.getMPostion();
						Log.v(TAG, " pos : "+pos+",cur_postion-1"+(cur_postion-1));
						//将路径转化为mp4路径
						
						String path = imageList.get(pos).getImagePath();
						String mp4Path = transformCapPath2RecordPath(path);
						Log.v(TAG, " mp4Path : "+mp4Path);
						
						int number = imageList.size();
						bundle.putInt("cur_postion", cur_postion);
						bundle.putInt("showSum", number);
						bundle.putString("video_path", mp4Path);
						intent.putExtras(bundle);
						Log.i(TAG, "curr pos:" + cur_postion + ", showSum:" + showSum + ", video_path:" + mp4Path);

						intent.setClass(ImagePreviewViewPagerActivity.this,ImageManagerVideoPlayActivity.class);
						startActivityForResult(intent, 10);
					}
				});
				return imageVideo;
			}
		}

		protected String transformCapPath2RecordPath(String drawablePath) {
			String recordPath = "";
			recordPath = drawablePath.replace("capture", "record");
			if(recordPath.contains(".jpg")){
				recordPath = recordPath.replace(".jpg", ".mp4");
			}	
			return recordPath;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public int getItemPosition(Object object) {
//			return super.getItemPosition(object);
			return POSITION_NONE;
		}
	}
	
	OnViewTapListener onViewTapListener = new OnViewTapListener() { // 视图监听器...
		@Override
		public void onViewTap(View view, float x, float y) {
			click_time++;
			if (click_time % 2 != 0) {
				mNavigationBar.setVisibility(View.GONE);
				nToolbar.setVisibility(View.GONE);
				// photoView.setImageMatrix(matrix);//不能够让图片变形...
			} else {
				mNavigationBar.setVisibility(View.VISIBLE);
				nToolbar.setVisibility(View.VISIBLE);
			}
		}
	};

	// 需要更新ViewPager列表....???????
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			ImagesManager imagesManager = ImagesManager.getInstance();
			List<String> dateList = imagesManager.getDateList();
			int date_size = dateList.size();
			int cur_pos = data.getIntExtra("cur_postion", 1);
			int sum = 0;
			cur_pos--;
			for (int i = 0; i < date_size; i++) {
				sum += imagesManager.getImageListForDate(dateList.get(i)).size();
			}
			
			
			imagepreview_title.setText("(" + cur_pos + "/" + sum + ")");			
			
			ArrayList<Image> imgNewAdded = null;
			try {
				imgNewAdded = (ArrayList<Image>) data.getExtras().get("CAPTURE_NEW_ADDED");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
	
			if (imgNewAdded.size() == 0) {
				return;
			}
		
			for (int i = 0; i < imgNewAdded.size(); i++) {
				imageList.add(0, imgNewAdded.get(i));
			}
			
			selfPagerAdapter.notifyDataSetChanged();
			mSelfDefViewPager.setShowSum(sum);
			selfPagerAdapter = new SelfPagerAdapter();
			mSelfDefViewPager.setAdapter(selfPagerAdapter);
			mSelfDefViewPager.setCurrentItem(cur_pos);
			
			Log.i(TAG, "imageList:" + imageList.size());				
			Log.i(TAG, "count:" + selfPagerAdapter.getCount());
			
//			selfPagerAdapter.startUpdate(mSelfDefViewPager);
		}
	}

	public String swith2CapPath(String path) {
		String jpgPath = "";
		jpgPath = path.replace("record", "capture").replace(".mp4", ".jpg");
		return jpgPath;
	}
}