package com.starnet.snview.images;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.images.Image.ImageType;
import com.starnet.snview.images.utils.PhotoView;
import com.starnet.snview.images.utils.PhotoViewAttacher.OnViewTapListener;
import com.starnet.snview.images.utils.SelfDefViewPager;
import com.starnet.snview.images.utils.Util;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImagePreviewViewPagerActivity extends BaseActivity {
	final String TAG = "ImagePreviewViewPagerActivity";
	private Context context;
	private int showSum; // 导航栏中总数
	private int showNum; // 导航栏中第几幅画面
	private TextView imgNumInfoTitle; // 显示设备的数量，以及显示画面的序号
	// private Button imagepreview_leftBtn; // 返回按钮

	private SelfDefViewPager mPager; // 自定义的ViewPager可以判断是左滑，还是右滑
	private SelfPagerAdapter mAdapter;

	private FrameLayout nToolbar;
	private RelativeLayout mNavigationBar; // 导航栏
	private int click_time = 0;
	private Button delBtn;
	// private PhotoView photoView;
	private int video_click_time = 0;

	private ImagesManager mImgManager;
	private List<Image> imgList = new LinkedList<Image>();

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

		nToolbar = super.getToolbarContainer();
		mNavigationBar = super.getNavbarContainer();

		mImgManager = ImagesManager.getInstance();

		// imagepreview_leftBtn = super.getLeftButton();
		imgNumInfoTitle = super.getTitleView();

		Intent intent = getIntent();
		if (intent != null) {
			showSum = Integer.valueOf(intent.getStringExtra("sumMap"));
			showNum = Integer.valueOf(intent.getStringExtra("imgPosInMap"));
			imgNumInfoTitle.setText("(" + showNum + "/" + showSum + ")");// 测试使用...

			imgList = intent.getParcelableArrayListExtra("imageList");
			int size = imgList.size();
			Log.v(TAG, "imageList size " + size);
		}

		int cur_pos = showNum - 1;
		mPager = new SelfDefViewPager(context, imgNumInfoTitle, showSum);
		mAdapter = new SelfPagerAdapter();
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(cur_pos);
		setContentView(mPager);

		RelativeLayout subLayout = new RelativeLayout(this);// 创建一个承装delete_button的相对布局

		RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		subLayout.setBackgroundColor(getResources().getColor(
				R.color.image_manager_delete_red));

		delBtn = new Button(context);// 自定义删除按钮
		delBtn.setBackgroundResource(R.drawable.imagepreview_deletebtn_selector);
		delBtn.setHeight(LayoutParams.WRAP_CONTENT);
		delBtn.setWidth(LayoutParams.WRAP_CONTENT);

		subLayout.addView(delBtn, rParams); // 将delete_button承装在相对布局中
		nToolbar.addView(subLayout); // 将相对布局承装在nToolbar中
		ImageView mRightArrow = (ImageView) nToolbar
				.findViewById(R.id.base_toolbar_container_arrowright);
		mRightArrow.setVisibility(View.GONE); // 隐藏小按钮

		setListenersForWidgets();
	}

	private void setListenersForWidgets() {

		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImagePreviewViewPagerActivity.this.finish();
			}
		});

		delBtn.setOnClickListener(new OnClickListener() {// 删除按钮...
			@Override
			public void onClick(View v) {
				Builder builder = new Builder(context);
				String title = getString(R.string.image_manager_imagepreview_delete_photo);
				builder.setTitle(title);
				String cancel = getString(R.string.image_manager_imagepreview_delete_cancel);
				builder.setNegativeButton(cancel, null);
				String ok = getString(R.string.image_manager_imagepreview_delete_ok);
				builder.setPositiveButton(ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								int ori_size = imgList.size(); // 事实改变的...
								int cps = mPager.getCurrentItem(); // 获取当前的视图...
								String imgPath = imgList.get(cps).getImagePath();
								if (ori_size >= 2) {								// 不止一张照片 删除最后一张照片，总数、序数同时变
									if ((ori_size - 1 == cps)) {
									int m_ori_size = ori_size - 1;
									imgNumInfoTitle.setText("("+ cps + "/"+ m_ori_size + ")");// 改变显示...
									imgList.remove(cps);					// 移除掉...
									int m_cur_pos = cps - 1;

									mAdapter.notifyDataSetChanged();
									showSum = m_ori_size;
									mPager.setShowSum(m_ori_size);
									mAdapter = new SelfPagerAdapter();
									mPager.setAdapter(mAdapter);
									mPager.setCurrentItem(m_cur_pos);
									
									// 进行删除操作...
									List<String> dateList = mImgManager.getDateList();
									if ((dateList == null)|| (dateList.size() == 0)) {
										return;
									} else {
										// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
										int flag = 0;
										for (int i = 0; i < dateList.size(); i++) {
											String str = dateList.get(i);
											List<Image> imageList = mImgManager.getImageListForDate(str);
											int imgSize = imageList.size();
											for (int j = 0; j < imgSize; j++) {
												Image image = imageList.get(j);
												if (image.getImagePath().equals(imgPath)) {
													mImgManager.deleteImage(image);// 根据路径删除文件....
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
									if (cps != 0) {// 不是第一张照片
										Log.v(TAG,"delete meidia。。。。。");
										int m_ori_size = ori_size - 1;
										imgNumInfoTitle.setText("("+ cps+ "/"+ m_ori_size+ ")");// 改变显示...
										imgList.remove(cps);// 移除掉...

										mAdapter.notifyDataSetChanged();
										mPager.setDelete_flag(true);
										mPager.setShowSum(m_ori_size);
										mAdapter = new SelfPagerAdapter();
										mPager.setAdapter(mAdapter);
										mPager.setCurrentItem(cps);
										showSum--;

										List<String> dateList = mImgManager.getDateList();
										if ((dateList == null)|| (dateList.size() == 0)) {
											return;
										} else {
											// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
											int flag = 0;
											for (int i = 0; i < dateList.size(); i++) {
												String str = dateList.get(i);
												List<Image> imageList = mImgManager.getImageListForDate(str);
												int imgSize = imageList.size();
												for (int j = 0; j < imgSize; j++) {
													Image image = imageList.get(j);
													if (image.getImagePath().equals(imgPath)) {
														mImgManager.deleteImage(image);// 根据路径删除文件....
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
										int m_ori_size = ori_size - 1;
										imgNumInfoTitle.setText("(1/"+ m_ori_size+ ")");// 改变显示...
										imgList.remove(cps);// 移除掉...

										mAdapter.notifyDataSetChanged();
										mPager.setShowSum(m_ori_size);
										mAdapter = new SelfPagerAdapter();
										mPager.setAdapter(mAdapter);
										mPager.setCurrentItem(cps);
										showSum--;

										List<String> dateList = mImgManager.getDateList();
										if ((dateList == null)|| (dateList.size() == 0)) {
											return;
										} else {
											// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
											int flag = 0;
											for (int i = 0; i < dateList.size(); i++) {
												String str = dateList.get(i);
												List<Image> imageList = mImgManager.getImageListForDate(str);
												int imgSize = imageList.size();
												for (int j = 0; j < imgSize; j++) {
													Image image = imageList.get(j);
													if (image.getImagePath().equals(imgPath)) {
														mImgManager.deleteImage(image);// 根据路径删除文件....
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
								List<String> dateList = mImgManager.getDateList();
								if ((dateList == null)|| (dateList.size() == 0)) {
									return;
								} else {// 根据路径在图像管理中列表删除图像:显示的路径是imagepath，如何根据该imagepath找到对应的image
									int flag = 0;
									for (int i = 0; i < dateList.size(); i++) {
										String str = dateList.get(i);
										List<Image> imageList = mImgManager.getImageListForDate(str);
										int imgSize = imageList.size();
										for (int j = 0; j < imgSize; j++) {
											Image image = imageList.get(j);
											if (image.getImagePath().equals(imgPath)) {
												mImgManager.deleteImage(image);// 根据路径删除文件....
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

	private void show(int cps, int size) {
		int mSize = size - 1;
		if (cps == 0) {
			cps = 1;
		}
		imgNumInfoTitle.setText("(" + cps + "/" + mSize + ")");// 改变显示...
		imgList.remove(cps); // 移除掉...
		mAdapter.notifyDataSetChanged();
		mPager.setShowSum(mSize);
		mAdapter = new SelfPagerAdapter();
		mPager.setAdapter(mAdapter);
	}

	private void deletImg(List<String> dList, String imgPath) {

		if (dList == null || dList.size() == 0) {
			return;
		}

		int flag = 0;
		for (int i = 0; i < dList.size(); i++) {
			String str = dList.get(i);
			List<Image> imageList = mImgManager.getImageListForDate(str);
			int imgSize = imageList.size();
			for (int j = 0; j < imgSize; j++) {
				Image image = imageList.get(j);
				if (image.getImagePath().equals(imgPath)) {
					mImgManager.deleteImage(image);// 根据路径删除文件....
					flag = 1;
					break;
				}
			}
			if (flag == 1) {
				break;
			}
		}
	}

	private String drawablePath;

	class SelfPagerAdapter extends PagerAdapter {

		public SelfPagerAdapter() {
			super();
		}

		@Override
		public int getCount() {
			int size = imgList.size();
			return size;
		}

		@SuppressLint("NewApi")
		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			int mPostion = position;
			// int m_pos = mSelfDefViewPager.getCurrentItem();
			final Image image = imgList.get(mPostion);
			drawablePath = image.getImagePath();
			if (image.getType() == ImageType.PICTURE) {
				Bitmap mBitmap = BitmapFactory.decodeFile(drawablePath);
				photoView.setImageBitmap(mBitmap);
				photoView.setZoomable(true);
				container.addView(photoView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				photoView.setOnViewTapListener(onViewTapListener);
				return photoView;
			} else {
				String path = imgList.get(mPostion).getImagePath();
				String jpgPath = swith2CapPath(path);

				View video = ((LayoutInflater) (context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE)))
						.inflate(R.layout.images_video_item, null, false);
				RelativeLayout imageVideoContent = (RelativeLayout) video
						.findViewById(R.id.images_video_content);
				ImageButton playBtn = (ImageButton) video
						.findViewById(R.id.images_video_play);
				
				Drawable bg = Drawable.createFromPath(jpgPath);// 设置的缩略图背景...
				if (bg == null) {
					bg = getResources().getDrawable(R.color.black);
				}

				int w = GlobalApplication.getInstance().getScreenWidth();
				int h = w * bg.getIntrinsicHeight() / bg.getIntrinsicWidth();

				imageVideoContent.setBackground(bg);
				imageVideoContent.setLayoutParams(new RelativeLayout.LayoutParams(w, h));
				container.addView(video, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				
				video.setOnClickListener(new OnClickListener() {
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

				playBtn.setOnClickListener(new OnClickListener() {// 添加播放事件
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						int pos = mPager.getCurrentItem();
						int cur_postion = mPager.getMPostion();
						String path = imgList.get(pos).getImagePath();
						String mp4Path = transformCapPath2RecordPath(path);
						int number = imgList.size();
						bundle.putInt("cur_postion", cur_postion);
						bundle.putInt("showSum", number);
						bundle.putString("video_path", mp4Path);
						intent.putExtras(bundle);
						intent.setClass(context,ImageManagerVideoPlayActivity.class);
						startActivityForResult(intent, 10);
					}
				});
				return video;
			}
		}

		protected String transformCapPath2RecordPath(String drawablePath) {
			String recordPath = "";
			recordPath = drawablePath.replace("capture", "record");
			if (recordPath.contains(".jpg")) {
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
			return super.getItemPosition(object);
		}
	}

	OnViewTapListener onViewTapListener = new OnViewTapListener() {
		@Override
		public void onViewTap(View view, float x, float y) {
			click_time++;
			if (click_time % 2 != 0) {
				mNavigationBar.setVisibility(View.GONE);
				nToolbar.setVisibility(View.GONE);
			} else {
				mNavigationBar.setVisibility(View.VISIBLE);
				nToolbar.setVisibility(View.VISIBLE);
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			ImagesManager imagesManager = ImagesManager.getInstance();
			List<String> dateList = imagesManager.getDateList();
			int date_size = dateList.size();
			int cur_pos = data.getIntExtra("cur_postion", 1);
			int sum = 0;
			cur_pos--;
			for (int i = 0; i < date_size; i++) {
				sum += imagesManager.getImageListForDate(dateList.get(i))
						.size();
			}
			imgNumInfoTitle.setText("(" + cur_pos + "/" + sum + ")");
			ArrayList<Image> imgNewAdded = null;
			try {
				imgNewAdded = (ArrayList<Image>) data.getExtras().get(
						"CAPTURE_NEW_ADDED");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if (imgNewAdded.size() == 0) {
				return;
			}
			for (int i = 0; i < imgNewAdded.size(); i++) {
				imgList.add(0, imgNewAdded.get(i));
			}
			mAdapter.notifyDataSetChanged();
			mPager.setShowSum(sum);
			mAdapter = new SelfPagerAdapter();
			mPager.setAdapter(mAdapter);
			mPager.setCurrentItem(cur_pos);
		}
	}

	public String swith2CapPath(String path) {
		String jpgPath = "";
		jpgPath = path.replace("record", "capture").replace(".mp4", ".jpg");
		return jpgPath;
	}
	
	class AsyncImageLoaderByPath {  
	    //SoftReference是软引用，是为了更好的为了系统回收变量  
	    private HashMap<String, SoftReference<Bitmap>> imageCache;  
	    private Context context;  
	      
	    public AsyncImageLoaderByPath(Context context) {  
	        this.imageCache = new HashMap<String, SoftReference<Bitmap>>();  
	        this.context = context;  
	    }  
	    public Bitmap loadBitmapByPath(final String imagePath, final ImageView imageView, final ImageCallback imageCallback){  
	        if (imageCache.containsKey(imagePath)) {  
	            //从缓存中获取  
	            SoftReference<Bitmap> softReference = imageCache.get(imagePath);  
	            Bitmap bitmap = softReference.get();  
	            if (bitmap != null) {  
	                return bitmap;  
	            }  
	        }  
	        final Handler handler = new Handler() {  
	            public void handleMessage(Message message) {  
	                imageCallback.imageLoaded((Bitmap) message.obj, imageView, imagePath);  
	            }  
	        };  
	        //建立新一个获取SD卡的图片  
	        new Thread() {  
	            @Override  
	            public void run() {  
	                Bitmap bitmap = BitmapFactory.decodeByteArray(Util.decodeBitmap(imagePath), 0, Util.decodeBitmap(imagePath).length);  
	                imageCache.put(imagePath, new SoftReference<Bitmap>(bitmap));  
	                Message message = handler.obtainMessage(0, bitmap);  
	                handler.sendMessage(message);  
	            }  
	        }.start();  
	        return null;  
	    }
	}
	 //回调接口  
    interface ImageCallback {  
        public void imageLoaded(Bitmap imageBitmap,ImageView imageView, String imagePath);  
    }
}
