package com.starnet.snview.realplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.LandscapeToolbar.LandControlbarClickListener;
import com.starnet.snview.component.LandscapeToolbar.QUALITY_LEVEL;
import com.starnet.snview.component.LandscapeToolbar.QualityClickListener;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.component.ToastTextView;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.component.VideoPager;
import com.starnet.snview.component.Toolbar.ActionImageButton;
import com.starnet.snview.component.VideoPager.ACTION;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.realplay.VideoPager.VideoPagerChangedCallback;
import com.starnet.snview.util.ActivityUtility;
import com.starnet.snview.util.PreviewItemXMLUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class RealplayActivity extends BaseActivity {

	private static final String TAG = "RealplayActivity";
	
	private Toolbar mToolbar;
	private FrameLayout mControlbar;
	private LinearLayout mQualityControlbarMenu;
	private com.starnet.snview.realplay.VideoPager mVideoPager; 	
	private com.starnet.snview.component.VideoPager mPager;
	
	private LiveViewManager liveViewManager;
	private LiveControl liveControl;
	private PTZControl ptzControl;
	
	private Animation mShotPictureAnim;
	private List<PreviewDeviceItem> previewDevices;  // 当前正在预览的设备列表
	private int mOldOrientation;
	private boolean mIsStartedCompleted = false;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle inState) {
		Log.i(TAG, "onCreate()");

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContainerMenuDrawer(true);
		super.onCreate(inState);
		setContentView(R.layout.realplay_activity);
		
		setBackPressedExitEventValid(true);
		GlobalApplication.getInstance().init(this);		
		GlobalApplication.getInstance().setHandler(mHandler);
		initView();
		initListener();
		restoreLastPreviewStatus();
	}
	
	private void restoreLastPreviewStatus() {
		restoreLastPreviewVideo();
	}
	
	private void restoreLastPreviewVideo() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int mode = sharedPreferences.getInt("PREVIEW_MODE", -1);
		final int page = sharedPreferences.getInt("PAGE", -1);
		final int pageCount = sharedPreferences.getInt("PAGE_COUNT", -1);
		
		List<PreviewDeviceItem> devices = getPreservedDevices();
		if (devices.size() > 0) {
			setPreviewDevices(devices);
			initVideoPagerAdapter(mode, devices);
			mVideoPager.post(new Runnable() { // run会在mVideoPager重新初始化完毕后执行
				@Override
				public void run() {
					restoreVideoRegionLayout(mode, page, pageCount);
					new DelayOrientationSetting().execute(new Object());
					playAndPause();
				}
			});
			
		}
	}
	
	private List<PreviewDeviceItem> getPreservedDevices() {
		return PreviewItemXMLUtils.getPreviewItemListInfoFromXML(this
				.getString(R.string.common_last_devicelist_path));
	}
	
	private void initVideoPagerAdapter(int mode, List<PreviewDeviceItem> devices) {
		VideoPagerAdapter vpAdapter = null;
		if (mode == 1) { // 单通道
			vpAdapter = new VideoPagerAdapter(this, PageMode.SINGLE, devices);
		} else { // 多通道
			vpAdapter = new VideoPagerAdapter(this, PageMode.MULTIPLE, devices);
		}
		mVideoPager.setAdapter(vpAdapter);
		vpAdapter.notifyDataSetChanged();
	}
	
	private void restoreVideoRegionLayout(int mode, int page, int pageCount) {
		if (previewDevices.size() != 0) {
			Log.i(TAG, "mode: " + mode + ", page: " + page);
			if (mode != -1 && page != -1) {				
				liveViewManager.setMultiMode(mode == 4 ? true : false);
				mVideoPager.setCurrentItem(page - 1);
				
				// 重新装载LiveViews
				liveViewManager.clearLiveView();
				if (mode == 4) {
					List<LiveViewItemContainer> l = getVR().getSurfaceMultiLayout().getLiveviews();
					for (int i = 0; i < l.size(); i++) {
						liveViewManager.addLiveView(l.get(i));
					}
				} else {
					liveViewManager.addLiveView(getVR().getSurfaceSingleLayout().getLiveview());
				}
				
				if (mode == 1) {
					ptzControl.setIsEnterPTZInSingleMode(true);
				}
				
				int newCurrPos = (page - 1) * liveViewManager.getPageCapacity() + 1;
				liveViewManager.setCurrenSelectedLiveViewtIndex(newCurrPos); 
				liveViewManager.selectLiveView(newCurrPos);
				liveViewManager.resetLiveView(pageCount);				
				mPager.setAmount(previewDevices.size());
				mPager.setNum(newCurrPos);
			}
			
			mOldOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		} else { // 首次进入
			liveViewManager.setDeviceList(null);
			liveViewManager.setMultiMode(null);
			getVR().showSingleOrMultiMode(null);
			mPager.setAmount(0);
			mPager.setNum(0);
			
			bIsPlaying = false;
			updatePlayStatus(bIsPlaying);
			
			mOldOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}
	}
	

	@Override
	protected void onRestart() {
		Log.i(TAG, "onRestart");
		
		if (mPager.getAmount() > 0) {
			int currPageStart = (liveViewManager.getCurrentPageNumber() - 1)
					* liveViewManager.getPageCapacity() + 1;
			int currPageEnd = (liveViewManager.getCurrentPageNumber() - 1)
					* liveViewManager.getPageCapacity()
					+ liveViewManager.getCurrentPageCount();
			liveViewManager.preview(currPageStart, currPageEnd - currPageStart
					+ 1);
			
			mPager.setNum(currPageStart);
			mPager.setAmount(liveViewManager.getPager().getTotalCount());
			
			liveViewManager.selectLiveView(currPageStart);
			
			updateUIElementsStatus();
		}
		
		super.onRestart();
	}
	

	@Override
	protected void onStart() {
		super.reattachActiveView();
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onPostCreate");
//		new RefreshDeviceConnectionInfo(this).start();
		super.onPostCreate(savedInstanceState);
	}
	
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(TAG, "ConfigurationChanged");

		GlobalApplication.getInstance().setScreenWidth(
				ActivityUtility.getScreenSize(this).x);
		GlobalApplication.getInstance().setScreenHeight(
				ActivityUtility.getScreenSize(this).y);

		// 根据新的宽度和高度重新计算mVideoRegion及其中的LiveView
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

			Log.i(TAG, "ConfigurationChanged ->LANDSCAPE, width:"
					+ ActivityUtility.getScreenSize(this).x + ", height:"
					+ ActivityUtility.getScreenSize(this).y);

			super.setMenuEnabled(false);
			super.getNavbarContainer().setVisibility(View.GONE);
			super.getToolbarContainer().setVisibility(View.GONE);
			mControlbar.setVisibility(View.GONE);

			GlobalApplication.getInstance().setFullscreenMode(true);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
					ActivityUtility.getScreenSize(this).x,
					ActivityUtility.getScreenSize(this).y);
			// mVideoRegion.setLayoutParams(param);
			mVideoPager.setLayoutParams(param);

			liveControl.showLandscapeToolbarFrame();

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

			Log.i(TAG, "ConfigurationChanged ->PORTRAIT, width:"
					+ ActivityUtility.getScreenSize(this).x + ", height:"
					+ ActivityUtility.getScreenSize(this).y);

			super.setMenuEnabled(true);
			super.getNavbarContainer().setVisibility(View.VISIBLE);
			super.getToolbarContainer().setVisibility(View.VISIBLE);
			mControlbar.setVisibility(View.VISIBLE);

			GlobalApplication.getInstance().setFullscreenMode(false);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
					ActivityUtility.getScreenSize(this).x,
					ActivityUtility.getScreenSize(this).x);
			// mVideoRegion.setLayoutParams(param);
			mVideoPager.setLayoutParams(param);

			liveControl.hideLandscapeToolbarFrame();
		}

		ptzControl.syncPTZStatus();  // PTZ工具条状态同步
		updateUIElementsStatus();

		super.onConfigurationChanged(newConfig);
	}



	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "msg code: " + msg.what);
			switch (msg.what) {
			case Constants.TAKE_PICTURE:
				String imgPath = (String) msg.getData().get("PICTURE_FULL_PATH");
				
				// 播放声音
				new Thread(new Runnable() {
					@Override
					public void run() {
						SnapshotSound s = new SnapshotSound(RealplayActivity.this);
						s.playSound();
					}	
				}).start();
				
				liveViewManager.getSelectedLiveView().startAnimation(mShotPictureAnim);
				
				// 显示提示
				// ToastUtils.show(RealplayActivity.this, "Image Path: " + imgPath, Toast.LENGTH_LONG);
				
				Toast t = Toast.makeText(RealplayActivity.this, "", Toast.LENGTH_LONG);
				
				ToastTextView txt = new ToastTextView(RealplayActivity.this);
				txt.setText(RealplayActivity.this.getString(R.string.realplay_toast_take_pic) + imgPath);
				
				t.setView(txt);
				t.show();
				
				break;
			default:
				break;
			}
			
			
			super.handleMessage(msg);
		}
		
		
	};
	
	
	private void initListener() {
		Log.i(TAG, "In function test()");


		// 初始化视频区域布局大小
		final int screenWidth = GlobalApplication.getInstance().getScreenWidth();
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, screenWidth);
		
		mVideoPager = (com.starnet.snview.realplay.VideoPager) findViewById(R.id.video_pager);
		mVideoPager.setLayoutParams(param);
		mVideoPager.setVideoPagerChangedCallback(mVideoPagerChangedCallback);
		
		
		// 单通道/多通道模式切换 事件
		LiveViewManager.OnVideoModeChangedListener onVideoModeChangedListener
			= new LiveViewManager.OnVideoModeChangedListener() {

				@Override
				public void OnVideoModeChanged(boolean isMultiMode) {
					//mVideoRegion.removeAllViews();
					liveViewManager.clearLiveView();
					
					//Log.i(TAG, "VideoRegion, width: " + mVideoRegion.getWidth() + ", height: " + mVideoRegion.getHeight());
					
					List<PreviewDeviceItem> devices = liveViewManager.getDeviceList();
					if (devices == null || devices.size() == 0) {
						return;
					}
					
					Log.i(TAG, "OnVideoModeChanged, current item:" + (liveViewManager.getCurrentPageNumber() - 1));
					if (isMultiMode) { // 多通道模式
						((VideoPagerAdapter) mVideoPager.getAdapter()).setPageMode(PageMode.MULTIPLE);
						mVideoPager.getAdapter().notifyDataSetChanged();
						
						//getVR().showSingleOrMultiMode(false);
						for (int i = 0; i < mVideoPager.getChildCount(); i++) {
							((VideoRegion) mVideoPager.getChildAt(i)).showSingleOrMultiMode(false);
						}
						
						
						int oldPage = liveViewManager.getCurrentPageNumber();
						int newPage = oldPage % 4 == 0 ? oldPage / 4 : (oldPage / 4 + 1);
						
						mVideoPager.setCurrentItem(newPage - 1);
						
						List<LiveViewItemContainer> l = getVR().getSurfaceMultiLayout().getLiveviews();
						for (int i = 0; i < l.size(); i++) {
							liveViewManager.addLiveView(l.get(i));
						}
					} else { // 单通道模式
						((VideoPagerAdapter) mVideoPager.getAdapter()).setPageMode(PageMode.SINGLE);
						mVideoPager.getAdapter().notifyDataSetChanged();
						
						//getVR().showSingleOrMultiMode(true);
						for (int i = 0; i < mVideoPager.getChildCount(); i++) {
							((VideoRegion) mVideoPager.getChildAt(i)).showSingleOrMultiMode(true);
						}
						
						mVideoPager.setCurrentItem(liveViewManager.getSelectedLiveViewIndex() - 1);
						
						liveViewManager.addLiveView(getVR().getSurfaceSingleLayout().getLiveview());
					}
					
					onContentChanged();
					
				}
			
		};
		
		liveViewManager.setOnVideoModeChangedListener(onVideoModeChangedListener);

		final StatusListener connectionStatusListener = new StatusListener() {

			@Override
			public void OnConnectionTrying(View v) {
				final LiveViewItemContainer c = (LiveViewItemContainer) v;
				
				c.setWindowInfoContent(getString(R.string.connection_status_connecting));
				
				//updateProgressbarStatus(c.getProgressBar(), true);
				mHandler.post( new Runnable() {
					@Override
					public void run() {
						if (c != null) {
							c.getRefreshImageView().setVisibility(View.GONE);
							c.getProgressBar().setVisibility(View.VISIBLE);
							c.getRefreshImageView().setVisibility(View.INVISIBLE); // 若使用View.GONE会导致部分情况下ProgressBar消失
							
							Log.i(TAG, "ProgressBar@" + c.getProgressBar() + ", visible");
						}
					}
				});
				
				bIsPlaying = true;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						updatePlayStatus(true);
					}
				});
			}

			@Override
			public void OnConnectionFailed(View v) {
				Log.i(TAG, "OnConnectionFailed");
				final LiveViewItemContainer c = (LiveViewItemContainer) v;
				
				c.setWindowInfoContent(getString(R.string.connection_status_failed));
				
				mHandler.post( new Runnable() {
					@Override
					public void run() {
						if (c != null) {
							c.getProgressBar().setVisibility(View.INVISIBLE);
							c.getRefreshImageView().setVisibility(View.VISIBLE);
						}
					}
				});
				
				boolean isAllVideoClosed = true;
				List<LiveViewItemContainer> liveviews = liveViewManager.getListviews();
				for (int i = 0; i < liveviews.size(); i++)	{
					LiveViewItemContainer lv = liveviews.get(i);
					
					if (lv.getCurrentConnection() != null && (lv.getCurrentConnection().isConnected() 
							|| lv.getCurrentConnection().isConnecting())) {
						isAllVideoClosed = false;
					}
				}
				
				if (isAllVideoClosed) {
					bIsPlaying = false;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							updatePlayStatus(false);
						}
					});
					
				}
				
			}

			@Override
			public void OnConnectionEstablished(View v) {
				final LiveViewItemContainer c = (LiveViewItemContainer) v;
				
				c.setWindowInfoContent(getString(R.string.connection_status_established));				
			}
			
			@Override
			public void OnConnectionBusy(View v) {
				
				final LiveViewItemContainer c = (LiveViewItemContainer) v;
				
				if (c.isManualStop()) {
					return;
				}
				
				//updateProgressbarStatus(c.getProgressBar(), false);
				mHandler.post( new Runnable() {
					@Override
					public void run() {
						if (c != null) {
							c.getProgressBar().setVisibility(View.INVISIBLE);	
							c.getRefreshImageView().setVisibility(View.GONE);
							Log.i(TAG, "ProgressBar@" + c.getProgressBar() + ", invisible");
						}
					}
				});
			}

			@Override
			public void OnConnectionClosed(View v) {
				final LiveViewItemContainer c = (LiveViewItemContainer) v;
				int currPageCount = liveViewManager.getCurrentPageCount();
				int index = liveViewManager.getIndexOfLiveView(c);
				
				if (index > currPageCount || c.isManualStop()) {
					return;
				}
				
				c.setWindowInfoContent(getString(R.string.connection_status_closed));
				
				mHandler.post( new Runnable() {
					@Override
					public void run() {
						if (c != null) {
							c.getProgressBar().setVisibility(View.INVISIBLE);
							c.getRefreshImageView().setVisibility(View.VISIBLE);
							
						}
					}
				});
				
			}

		};
		
		liveViewManager.setConnectionStatusListener(connectionStatusListener);
		
		
	}
	

	private void initView() {
		super.getExtendButton().setVisibility(View.GONE);
		
		Button btnDeviceList = super.getRightButton();
		btnDeviceList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(RealplayActivity.this,
						ChannelListActivity.class);
				RealplayActivity.this.startActivityForResult(intent, 0);
				
				bIsPlaying = false;
				updatePlayStatus(bIsPlaying);
				
				if (liveViewManager != null) {
					liveViewManager.stopPreview();
				}
			}
		});

		mShotPictureAnim = AnimationUtils.loadAnimation(RealplayActivity.this, R.anim.shot_picture);
		liveViewManager = new LiveViewManager(this);
		previewDevices = new ArrayList<PreviewDeviceItem>();
		
		initToolbar();
		initLandScapeToolbar();
		
		ptzControl = new PTZControl(this);
		initToolbarExtendMenu();
	}
	
	
	public VideoPager getPager() {
		return mPager;
	}
	
	public com.starnet.snview.realplay.VideoPager getVideoPager() {
		return mVideoPager;
	}
	
	public PTZControl getPtzControl() {
		return ptzControl;
	}
	
	public LiveControl getLiveControl() {
		return liveControl;
	}
	
	public LiveViewManager getLiveViewManager() {
		return liveViewManager;
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	public boolean isPlaying() {
		return bIsPlaying;
	}
	
	public void setIsPlaying(boolean b) {
		bIsPlaying = b;
	}
	
	public VideoRegion getVR() {
		Log.i(TAG, "###current item: " + mVideoPager.getCurrentItem());
		return ((VideoRegion) mVideoPager.findViewWithTag(mVideoPager.getCurrentItem()));
	}

	private VideoPager.OnActionClickListener mPagerOnActionClickListener = new VideoPager.OnActionClickListener() {
		@Override
		public void OnActionClick(View v, ACTION action) {
			if (liveViewManager.getPager() == null) {
				return;
			}

			int item = mVideoPager.getCurrentItem();
			int count = mVideoPager.getAdapter().getCount();
			
			switch (action) {
			case PREVIOUS:
				if (item > 0) {
					mVideoPager.setCurrentItem(item - 1, true);
				} else {
					return;
				}
				break;
			case NEXT:
				if (item < count - 1) {
					mVideoPager.setCurrentItem(item + 1, true);
				} else {
					return;
				}
				break;
			default:
				break;
			}
			
			refillLiveviews();
			
			if (action == ACTION.PREVIOUS) {
				liveViewManager.previousPage();
			} else if (action == ACTION.NEXT) {
				liveViewManager.nextPage();
			}

			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
		}
	};

	private boolean bIsPlaying = false;
	private boolean bQualityPressed = false;
	//private boolean bPTZPressed = false;
	private boolean bIsMicrophoneOpen = false;
	private boolean bIsSoundOpen = false;
	private boolean bVideoRecordPressed = false;
	
	/** 竖屏工具栏功能项点击监听器 */
	private Toolbar.OnItemClickListener mToolbarOnItemClickListener = new Toolbar.OnItemClickListener() {

		@Override
		public void onItemClick(ActionImageButton imgBtn) {
			switch (imgBtn.getItemData().getActionID()) {
			case PLAY_PAUSE:
				playAndPause();
				break;
			case PICTURE:
				Log.i(TAG, "Function, take picture");
				takePicture();				
				break;
			case QUALITY:
				bQualityPressed = !bQualityPressed;

				ptzControl.showPTZBar(false);

				if (bQualityPressed) {
					showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.MENU_QUALITY);
				} else {
					showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.PAGER);
				}

				break;
			case PTZ:
				Log.i(TAG, "PTZ Test");
				
				ptzControl.ptzButtonAction();
				
				break;
			case MICROPHONE:
				if (!bIsMicrophoneOpen) { // 开启麦克风
					bIsMicrophoneOpen = true;
					mToolbar.setActionImageButtonBg(
							Toolbar.ACTION_ENUM.MICROPHONE,
							R.drawable.toolbar_microphone_selector);
				} else { // 关闭麦克风
					bIsMicrophoneOpen = false;
					mToolbar.setActionImageButtonBg(
							Toolbar.ACTION_ENUM.MICROPHONE,
							R.drawable.toolbar_microphone_stop_selector);
				}

				break;
			case SOUND:
				if (!bIsSoundOpen) { // 开启扬声器
					bIsSoundOpen = true;
					mToolbar.setActionImageButtonBg(Toolbar.ACTION_ENUM.SOUND,
							R.drawable.toolbar_sound_selector);
				} else { // 关闭扬声器
					bIsSoundOpen = false;
					mToolbar.setActionImageButtonBg(Toolbar.ACTION_ENUM.SOUND,
							R.drawable.toolbar_sound_off_selector);
				}
				break;
			case VIDEO_RECORD:
				processVideoRecord();
				break;
			case ALARM:
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 判断正在预览的设备列表是否为空
	 * @return true，设备列表为空；false，设备列表不为空
	 */
	public boolean checkIfPreviewDeviceListEmpty() {
		boolean result = false;
		
		if (previewDevices == null
				|| (previewDevices != null && previewDevices.size() == 0)
				|| liveViewManager.getPager() == null
				|| (liveViewManager.getPager() != null && liveViewManager
						.getPager().getTotalCount() == 0)) {
			result = true;
		}
		
		return result;
	}
	
	private void playAndPause() {
		if (checkIfPreviewDeviceListEmpty()) {
			return;
		}
		
		if (!bIsPlaying) { // 播放
			Log.i(TAG, "play video");
			
			int count = liveViewManager.getCurrentPageCount();

			for (int i = 0; i < count; i++) {
				liveViewManager.getListviews().get(i).getRefreshImageView().performClick();
			}
		} else { // 暂停
			Log.i(TAG, "stop video");
			liveViewManager.stopPreview();
			
			if (ptzControl.isPTZModeOn()) {
				ptzControl.setIsEnterPTZInSingleMode(true);
				ptzControl.closePTZ();
			}
			
			bIsPlaying = false;
			updatePlayStatus(bIsPlaying);
		}
	}
	
	private void takePicture() {
		if (checkIfPreviewDeviceListEmpty()) {
			return;
		}
		
		LiveViewItemContainer c = liveViewManager.getSelectedLiveView();
		
		if (c.getCurrentConnection() != null && c.getCurrentConnection().isConnected()) {
			liveViewManager.getSelectedLiveView().getSurfaceView().setTakePicture(true);
		}
	}
	
	public void updatePlayStatus(boolean isPlaying) {
		if (isPlaying) {
			mToolbar.setActionImageButtonBg(
					Toolbar.ACTION_ENUM.PLAY_PAUSE,
					R.drawable.toolbar_pause_selector);	
			liveControl.getLandscapeToolbar().switchStopResumeButtonStatus(true);
			
		} else {
			mToolbar.setActionImageButtonBg(
					Toolbar.ACTION_ENUM.PLAY_PAUSE,
					R.drawable.toolbar_play_selector);
			liveControl.getLandscapeToolbar().switchStopResumeButtonStatus(false);
		}
	}
	
	private void processVideoRecord() {
		Log.i(TAG, "processVideoRecord");
		if (!checkIsPTZDeviceConnected()) {
			Log.i(TAG, "processVideoRecord, unconnected...");
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, false);
			return; // 未连接状态下，不作处理
		}
		
		bVideoRecordPressed = !bVideoRecordPressed;
		
		if (bVideoRecordPressed) { // 开启录像
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, true);
			liveViewManager.getSelectedLiveView().startMP4Record();
		} else { // 关闭录像
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, false);
			liveViewManager.getSelectedLiveView().stopMP4Record();
		}
	}
	
	public void updateUIElementsStatus() {
		if (mIsStartedCompleted) {
			updateUIElementsStatus(true);
		}
	}
	
	
	public void updateUIElementsStatus(boolean autoUpdate) {		
		// 更新录像按钮状态
		if (autoUpdate) {
			bVideoRecordPressed = liveViewManager.getSelectedLiveView().getSurfaceView().isStartRecord();
		}
		
		if (!GlobalApplication.getInstance().isIsFullMode()) {
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, bVideoRecordPressed);
		} else {
			liveControl.getLandscapeToolbar().getRecoredButton().setSelected(bVideoRecordPressed);
		}
	}
	
	/** 横屏工具栏功能项点击监听器 */
	private LandControlbarClickListener mLandscapeControlbarClickListener = new LandControlbarClickListener() {
		@Override
		public void landControlbarClick(View v) {
			switch (v.getId()) {
			case R.id.landscape_liveview_capture_button:
				takePicture();
				break;
			case R.id.landscape_liveview_ptz_button:
				ptzControl.ptzButtonAction();
				break;
			case R.id.landscape_liveview_quality_button:
				liveControl.getLandscapeToolbar().showQualityControlBar();
				break;
			case R.id.landscape_liveview_delete_button:
				playAndPause();
				break;
			case R.id.landscape_liveview_record_button:
				processVideoRecord();
				break;
			}

		}
	};
	
	
	private QualityClickListener mLandscapeQualityBarClickListener = new QualityClickListener() {

		@Override
		public void qualityClick(View v) {
			Log.i(TAG, "qualityBarClick");
			
			switch (v.getId()) {
			case R.id.landscape_liveview_quality_clear_button:
				liveControl.getLandscapeToolbar().setQualityLevel(QUALITY_LEVEL.CLEAR);
				break;
			case R.id.landscape_liveview_quality_balance_button:
				liveControl.getLandscapeToolbar().setQualityLevel(QUALITY_LEVEL.BLANCE);
				break;
			case R.id.landscape_liveview_quality_fluent_button:
				liveControl.getLandscapeToolbar().setQualityLevel(QUALITY_LEVEL.FLUENT);
				break;
			case R.id.landscape_liveview_quality_custom_button:
				liveControl.getLandscapeToolbar().setQualityLevel(QUALITY_LEVEL.CUSTOM);
				break;
			case R.id.landscape_liveview_quality_back_button:
				liveControl.getLandscapeToolbar().hideQualitybar();
				break;
			}
			
		}
		
	};

	/**
	 * 检查当前所选通道是否已连接
	 * @return true, 已连接；false, 未连接
	 */
	public boolean checkIsPTZDeviceConnected() {
		Connection conn = liveViewManager.getSelectedLiveView().getCurrentConnection();
		
		if (conn != null && conn.isConnected()) {
			return true;
		}
		
		return false;
	}
	
	private void initLandScapeToolbar() {
		liveControl = new LiveControl(this);
		liveControl.hideLandscapeToolbarFrame();
		liveControl.getLandscapeToolbar().setOnControlbarClickListener(mLandscapeControlbarClickListener);
		//liveControl.getLandscapeToolbar().setOnPTZBarClickListener(mLandscapePTZBarClickListener);
		liveControl.getLandscapeToolbar().setOnQualityClickListener(mLandscapeQualityBarClickListener);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initToolbar() {
		mToolbar = super.getBaseToolbar();

		ArrayList itemList = new ArrayList();
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PLAY_PAUSE,
				R.drawable.toolbar_play_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PICTURE,
				R.drawable.toolbar_take_picture_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.QUALITY,
				R.drawable.toolbar_quality_high_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PTZ,
				R.drawable.toolbar_ptz_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.MICROPHONE,
				R.drawable.toolbar_microphone_stop_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.SOUND,
				R.drawable.toolbar_sound_off_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.VIDEO_RECORD,
				R.drawable.toolbar_video_record_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.ALARM,
				R.drawable.toolbar_alarm_selector));

		mToolbar.createToolbar(itemList, GlobalApplication.getInstance()
				.getScreenWidth(),
				getResources().getDimensionPixelSize(R.dimen.toolbar_height));

		this.mToolbar.setOnItemClickListener(mToolbarOnItemClickListener);
	}

	public static enum TOOLBAR_EXTEND_MENU {
		PAGER, MENU_QUALITY, MENU_PTZ
	}

	private ImageButton mQualityMenuFluency;
	private ImageButton mQualityMenuStandard;
	private ImageButton mQualityMenuHigh;
	private ImageButton mQualityMenuCustom;

	private OnClickListener mOnQualityMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.quality_controlbar_menu_fluency:
				mQualityMenuFluency.setSelected(true);
				mQualityMenuStandard.setSelected(false);
				mQualityMenuHigh.setSelected(false);
				mQualityMenuCustom.setSelected(false);
				mToolbar.setActionImageButtonBg(Toolbar.ACTION_ENUM.QUALITY,
						R.drawable.toolbar_quality_fluency_selector);
				break;
			case R.id.quality_controlbar_menu_standard:
				mQualityMenuFluency.setSelected(false);
				mQualityMenuStandard.setSelected(true);
				mQualityMenuHigh.setSelected(false);
				mQualityMenuCustom.setSelected(false);
				mToolbar.setActionImageButtonBg(Toolbar.ACTION_ENUM.QUALITY,
						R.drawable.toolbar_quality_standard_selector);
				break;
			case R.id.quality_controlbar_menu_high:
				mQualityMenuFluency.setSelected(false);
				mQualityMenuStandard.setSelected(false);
				mQualityMenuHigh.setSelected(true);
				mQualityMenuCustom.setSelected(false);
				mToolbar.setActionImageButtonBg(Toolbar.ACTION_ENUM.QUALITY,
						R.drawable.toolbar_quality_high_selector);
				break;
			case R.id.quality_controlbar_menu_custom:
				mQualityMenuFluency.setSelected(false);
				mQualityMenuStandard.setSelected(false);
				mQualityMenuHigh.setSelected(false);
				mQualityMenuCustom.setSelected(true);
				mToolbar.setActionImageButtonBg(Toolbar.ACTION_ENUM.QUALITY,
						R.drawable.toolbar_quality_custom_selector);
				break;
			default:
				break;
			}

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, true);
		}

	};


	private void initToolbarExtendMenu() {
		mPager = (VideoPager) findViewById(R.id.pager);
		mPager.initContent(ActivityUtility.getScreenSize(this).x,
				getResources().getDimensionPixelSize(R.dimen.toolbar_height));
		mPager.setOnActionClickListener(mPagerOnActionClickListener);

		mControlbar = (FrameLayout) findViewById(R.id.realplay_controlbar);
		
		mQualityControlbarMenu = (LinearLayout) findViewById(R.id.quality_controlbar_menu);
		mQualityMenuFluency = (ImageButton) findViewById(R.id.quality_controlbar_menu_fluency);
		mQualityMenuStandard = (ImageButton) findViewById(R.id.quality_controlbar_menu_standard);
		mQualityMenuHigh = (ImageButton) findViewById(R.id.quality_controlbar_menu_high);
		mQualityMenuCustom = (ImageButton) findViewById(R.id.quality_controlbar_menu_custom);

		mQualityMenuFluency.setOnClickListener(mOnQualityMenuClickListener);
		mQualityMenuStandard.setOnClickListener(mOnQualityMenuClickListener);
		mQualityMenuHigh.setOnClickListener(mOnQualityMenuClickListener);
		mQualityMenuCustom.setOnClickListener(mOnQualityMenuClickListener);

		showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.PAGER);
	}

	public void showToolbarExtendMenu(TOOLBAR_EXTEND_MENU menuId) {

		switch (menuId) {
		case PAGER:
			mPager.setVisibility(View.VISIBLE);
			mQualityControlbarMenu.setVisibility(View.GONE);
			ptzControl.showPTZBar(false);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, false);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, false);

			bQualityPressed = false;
			break;
		case MENU_QUALITY:
			mPager.setVisibility(View.GONE);
			mQualityControlbarMenu.setVisibility(View.VISIBLE);
			ptzControl.showPTZBar(false);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, true);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, false);

			bQualityPressed = true;
			break;
		case MENU_PTZ:
			mPager.setVisibility(View.GONE);
			mQualityControlbarMenu.setVisibility(View.GONE);
			ptzControl.showPTZBar(true);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, false);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, true);

			bQualityPressed = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case 8:
			mIsStartedCompleted = true;
			
			Parcelable[] _devices = (Parcelable[]) data.getExtras().get("DEVICE_ITEM_LIST");
			List<PreviewDeviceItem> devices = new ArrayList<PreviewDeviceItem>();

			for (int i = 0; i < _devices.length; i++) {
				devices.add((PreviewDeviceItem)_devices[i]);
			}
			
			setPreviewDevices(devices);
			
			// 更新adapter
			VideoPagerAdapter vpAdapter = null;
			if (!liveViewManager.isMultiMode()) { // 单通道
				vpAdapter = new VideoPagerAdapter(this, PageMode.SINGLE, previewDevices);
			} else { // 多通道
				vpAdapter = new VideoPagerAdapter(this, PageMode.MULTIPLE, previewDevices);
			}
			
			mVideoPager.setAdapter(vpAdapter);
			vpAdapter.notifyDataSetChanged();
			
			mVideoPager.post(new Runnable() {
				@Override
				public void run() {
					startPreview();
				}
			});
			
			
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void startPreview() {
		mVideoPager.setCurrentItem(0);
		
		refillLiveviews();
		
		liveViewManager.setCurrenSelectedLiveViewtIndex(1);
		
		mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
		mPager.setAmount(liveViewManager.getLiveViewTotalCount());
		
		liveViewManager.preview();
		
		liveViewManager.selectLiveView(liveViewManager.getSelectedLiveViewIndex());
		
		if (!liveViewManager.isMultiMode()) {
			getVR().showSingleOrMultiMode(true);
			ptzControl.setIsEnterPTZInSingleMode(true);
		} else {
			getVR().showSingleOrMultiMode(false);
			ptzControl.setIsEnterPTZInSingleMode(false);
		}
	}
	
	/**
	 * 确保页面切换或程序退出时，正在进行的录像操作正常保存
	 */
	private void makeSureVideoRecordOff() {
		List<LiveViewItemContainer> liveviews = liveViewManager.getListviews();
		int count = liveviews.size();
		
		for (int i = 0; i < count; i++) {
			if (liveviews.get(i).getSurfaceView().isStartRecord()) {
				liveviews.get(i).stopMP4Record();
			}
		}
	}

	@Override
	protected void onDestroy() {		
		liveViewManager.closeAllConnection(false);
		// makeSureVideoRecordOff();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		boolean isMultiMode = liveViewManager.isMultiMode();
		if (isMultiMode) {
			editor.putInt("PREVIEW_MODE", 4); // 当前预览模式
		} else {
			editor.putInt("PREVIEW_MODE", 1);
		}
		if (liveViewManager.getPager() != null) {
			editor.putInt("PAGE", liveViewManager.getCurrentPageNumber()); // 当前页数
			editor.putInt("PAGE_COUNT", liveViewManager.getCurrentPageCount()); // 当前页项数
		} else {
			editor.putInt("PAGE", 0);
			editor.putInt("PAGE_COUNT", 0);
		}
		editor.commit();

		try {
			PreviewItemXMLUtils.writePreviewItemListInfoToXML(previewDevices,
					getString(R.string.common_last_devicelist_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		super.onDestroy();
	}
	
	public void setPreviewDevices(List<PreviewDeviceItem> devices) {
		if (devices == null || devices.size() == 0) {
			throw new IllegalArgumentException("Invalid parameter devices, null or zero size.");
		}
		
		previewDevices.clear();
		
		for (int i = 0; i < devices.size(); i++) {
			previewDevices.add(devices.get(i));
		}
		
		if (mVideoPager.getAdapter() != null) {
			mVideoPager.getAdapter().notifyDataSetChanged();
		}
		
		liveViewManager.setDeviceList(previewDevices);
		
		if (mIsStartedCompleted) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
	
	/**
	 * 获取当前正在预览的设备列表
	 * @return 设备列表信息的List
	 */
	public List<PreviewDeviceItem> getPreviewDevices() {
		return previewDevices;
	}
	
	/**
	 * 当前正在预览的设备信息发生变化后调用。如若删除的收藏夹或星云平台账户中有对应的
	 * 设备，则在对预览设备列表同步后，就调用此方法
	 */
	public void notifyPreviewDevicesContentChanged() {
		if (previewDevices.size() > 0) {
			if (mIsStartedCompleted) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			
			if (mVideoPager.getAdapter() != null) {
				mVideoPager.getAdapter().notifyDataSetChanged();
			}
			
			liveViewManager.setDeviceList(previewDevices);

			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
			
			liveViewManager.selectLiveView(liveViewManager.getSelectedLiveViewIndex());
		} else {
			if (mIsStartedCompleted) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			
			if (mVideoPager.getAdapter() != null) {
				mVideoPager.getAdapter().notifyDataSetChanged();
			}
			
			liveViewManager.setDeviceList(null);
			liveViewManager.setMultiMode(null);
			//showSingleOrMultiMode(null);
			if (getVR() != null) {
				getVR().showSingleOrMultiMode(null);
			}
			
			bIsPlaying = false;
			updatePlayStatus(bIsPlaying);
			
			mPager.setNum(0);
			mPager.setAmount(0);
		}
	}
	

	@Override
	protected void gotoRealtimePreview() {
		
	}
	
	private void leaveRealtimePreview() {
		//makeSureVideoRecordOff();
		bVideoRecordPressed = false;
		
		if (liveViewManager != null) {
//			liveViewManager.closeAllConnection(true);
			liveViewManager.stopPreview();
		}
		
		bIsPlaying = false;
		updatePlayStatus(bIsPlaying);
	}	

	@Override
	protected void gotoPictureManagement() {
		leaveRealtimePreview();
		
		super.gotoPictureManagement();
	}

	@Override
	protected void gotoPlayback() {
		leaveRealtimePreview();
		super.gotoPlayback();
	}

	@Override
	protected void gotoDeviceManagement() {
		leaveRealtimePreview();
		
		super.gotoDeviceManagement();
	}

	@Override
	protected void gotoSystemSetting() {
		leaveRealtimePreview();
		
		super.gotoSystemSetting();
	}
	
	@Override
	protected void gotoAlarm() {
		leaveRealtimePreview();
		super.gotoAlarm();
	}
	
	public LiveViewManager getLiveviewManager() {
		return liveViewManager;
	}
	
	public RelativeLayout getNavbarContainer() {
		return super.getNavbarContainer();
	}

	
	private VideoPagerChangedCallback mVideoPagerChangedCallback = new VideoPagerChangedCallback() {
		
		@Override
		public void getCurrentPageIndex(int index, int action) {
			Log.i(TAG, "###第 " + index + "页, 共 " + mVideoPager.getChildCount() + " 页");
			
			if ((action == 0) || ptzControl.isPTZModeOn()
					|| liveViewManager.getPager() == null) { // 不作滑动
				return;
			}
			
			Log.i(TAG, "###changeView called");
			bVideoRecordPressed = false;
			updateUIElementsStatus(false);
			
			refillLiveviews();
			
			if (action > 0) {
				liveViewManager.nextPage();
			} else {
				liveViewManager.previousPage();
			}
			
			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
			
			if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，且分页器显示，则刷新分页器
				if (liveControl.getLandscapeToolbar().isLandToolbarShow()) {
					liveControl.getLandscapeToolbar().showLandscapeToolbar();
				}
			}
			
		}
		
		@Override
		public void changeView(boolean left, boolean right) {}
	};
	
	/**
	 * 装载新的视频容器
	 */
	private void refillLiveviews() {
		liveViewManager.closeAllConnection(false);
		liveViewManager.resetLiveView(liveViewManager.getCurrentPageCount());
		
		liveViewManager.clearLiveView();
		
		if (liveViewManager.isMultiMode()) { // 多通道
			Log.i(TAG, "getVR():" + getVR());
			
			getVR().showSingleOrMultiMode(false);
			
			List<LiveViewItemContainer> l = getVR().getSurfaceMultiLayout().getLiveviews();
			for (int i = 0; i < l.size(); i++) {
				liveViewManager.addLiveView(l.get(i));
			}
			
		} else {
			getVR().showSingleOrMultiMode(true);
			
			liveViewManager.addLiveView(getVR().getSurfaceSingleLayout().getLiveview());
		}
	} 

	/**
	 * 延迟屏幕方向起始时刻，防止以横屏方式启动程序时出现的非预期横屏情况
	 */
	private class DelayOrientationSetting extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			setRequestedOrientation(mOldOrientation);
			mIsStartedCompleted = true;
		}
	}

	public void setPreviewDevices_copy(List<PreviewDeviceItem> devices) {
		this.previewDevices = devices;
	}
}