package com.starnet.snview.realplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.LandscapeToolbar.LandControlbarClickListener;
import com.starnet.snview.component.LandscapeToolbar.PTZBarClickListener;
import com.starnet.snview.component.LandscapeToolbar.QUALITY_LEVEL;
import com.starnet.snview.component.LandscapeToolbar.QualityClickListener;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.component.SurfaceViewMultiLayout;
import com.starnet.snview.component.SurfaceViewSingleLayout;
import com.starnet.snview.component.ToastTextView;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.component.Toolbar.ACTION_ENUM;
import com.starnet.snview.component.Toolbar.ItemData;
import com.starnet.snview.component.VideoPager;
import com.starnet.snview.component.Toolbar.ActionImageButton;
import com.starnet.snview.component.VideoPager.ACTION;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.util.ActivityUtility;
import com.starnet.snview.util.ClickEventUtils;
import com.starnet.snview.util.ClickEventUtils.OnActionListener;
import com.starnet.snview.util.PreviewItemXMLUtils;
import com.starnet.snview.util.ToastUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class RealplayActivity extends BaseActivity {

	private static final String TAG = "RealplayActivity";

	private Toolbar mToolbar;

	private FrameLayout mControlbar;
	private VideoPager mPager;
	private LinearLayout mQualityControlbarMenu;

	
	private LiveViewManager liveViewManager;
	private FrameLayout mVideoRegion;
	
	
	private LiveControl liveControl;
	private PTZControl ptzControl;
	
	private List<PreviewDeviceItem> previewDevices;  // 当前正在预览的设备列表
	

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle inState) {
		Log.i(TAG, "onCreate()");
		
		setContainerMenuDrawer(true);
		super.onCreate(inState);
		setContentView(R.layout.realplay_activity);
		
		setBackPressedExitEventValid(true);
		 

		GlobalApplication.getInstance().init(this);		
		GlobalApplication.getInstance().setHandler(mHandler);

		
		
		initView();

		initListener();
		
		loadDataFromPreserved();
	}
	
	private void loadDataFromPreserved() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		List<PreviewDeviceItem> devices = null;
		
		devices = PreviewItemXMLUtils.getPreviewItemListInfoFromXML(getString(R.string.common_last_devicelist_path));
		
		Log.i(TAG, "Devices size: " + devices.size());
		
		if (devices.size() != 0) {
			int mode = sharedPreferences.getInt("PREVIEW_MODE", -1);
			int page = sharedPreferences.getInt("PAGE", -1);
			int pageCount = sharedPreferences.getInt("PAGE_COUNT", -1);
			
			Log.i(TAG, "mode: " + mode + ", page: " + page);
			if (mode != -1 && page != -1) {
				liveViewManager.setDeviceList(devices);
				liveViewManager.setMultiMode(mode == 4 ? true : false);
				
				int newCurrPos = (page - 1) * liveViewManager.getPageCapacity() + 1;
				liveViewManager.setCurrenSelectedLiveViewtIndex(newCurrPos); 
				liveViewManager.selectLiveView(newCurrPos);
				
				liveViewManager.resetLiveView(pageCount);
				
				mPager.setAmount(devices.size());
				mPager.setNum(newCurrPos);
			}
			
		} else { // 首次进入
			liveViewManager.setMultiMode(null);
			mPager.setAmount(0);
			mPager.setNum(0);
		}

		
	}

	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		liveViewManager.invalidateLiveViews();
		
		ActionImageButton playBtn = new ActionImageButton(this);
		playBtn.setItemData(new ItemData(ACTION_ENUM.PLAY_PAUSE, R.drawable.toolbar_play_selector));
		
		mToolbarOnItemClickListener.onItemClick(playBtn);
//		if (liveViewManager.getPager() != null) {
//			int count = liveViewManager.getCurrentPageCount();
//			
//			for (int i = 0; i < count; i++) {
//				liveViewManager.getListviews().get(i).getRefreshImageView().performClick();
//			}
//		}

		super.onPostCreate(savedInstanceState);
	}
	
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i(TAG, "ConfigurationChanged");

		GlobalApplication.getInstance().setScreenWidth(ActivityUtility.getScreenSize(this).x);
		GlobalApplication.getInstance().setScreenHeight(ActivityUtility.getScreenSize(this).y);
		
		// 根据新的宽度和高度重新计算mVideoRegion及其中的LiveView
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			
            Log.i(TAG, "ConfigurationChanged ->LANDSCAPE, width:" + ActivityUtility.getScreenSize(this).x
            		+ ", height:" + ActivityUtility.getScreenSize(this).y);
            
			super.getNavbarContainer().setVisibility(View.GONE);
			super.getToolbarContainer().setVisibility(View.GONE);
			mControlbar.setVisibility(View.GONE);
			
			GlobalApplication.getInstance().setFullscreenMode(true);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
					ActivityUtility.getScreenSize(this).x, ActivityUtility.getScreenSize(this).y);
			mVideoRegion.setLayoutParams(param);
			
			liveControl.showLandscapeToolbarFrame();
			
       } else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
    	   
    	   Log.i(TAG, "ConfigurationChanged ->PORTRAIT, width:" + ActivityUtility.getScreenSize(this).x
           		+ ", height:" + ActivityUtility.getScreenSize(this).y);
    	   
    	   
			super.getNavbarContainer().setVisibility(View.VISIBLE);
			super.getToolbarContainer().setVisibility(View.VISIBLE);
			mControlbar.setVisibility(View.VISIBLE);
			
			GlobalApplication.getInstance().setFullscreenMode(false);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
					ActivityUtility.getScreenSize(this).x, ActivityUtility.getScreenSize(this).x);
			mVideoRegion.setLayoutParams(param);
			
			liveControl.hideLandscapeToolbarFrame();
       }
		
		
		ptzControl.syncPTZStatus();
		
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
	
	
	private boolean mIsScaleOperator = false;
	
	private void initListener() {
		Log.i(TAG, "In function test()");

//		mPtzReqSender = new PTZRequestSender(liveViewManager);

		
		mGestureDetector =  new GestureDetector(this, new GestureListener(onGestureListener));
		mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureListener(onGestureListener));
		
		
		
		mVideoRegion.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(TAG, "mVideoRegion, onTouch");
				
				int action = event.getActionMasked();
				
				/* 若检测到多点事件，则将这以后的事件从mGestureDetector中过滤
				 * 即若检测到缩放事件，则余下事件仅交由mScaleGestureDetector处理
				 */
				if (action == MotionEvent.ACTION_POINTER_DOWN) {
					mIsScaleOperator = true;  
				}
				
				/*
				if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
					mIsScaleOperator = false;
				}*/
				
				Log.i(TAG, "onTouch(), mIsPTZModeOn:" + ptzControl.isPTZModeOn() + ", mIsScaleOperator:" + mIsScaleOperator);
				
				if (mIsScaleOperator) {
					if (action == MotionEvent.ACTION_UP) {
						mIsScaleOperator = false;
					}
					
					return mScaleGestureDetector.onTouchEvent(event);
				} else {
					boolean r1 = mScaleGestureDetector.onTouchEvent(event);
					boolean r2 = mGestureDetector.onTouchEvent(event);
					
					if (ptzControl.isPTZModeOn() && !ptzControl.isFlingAction() && action == MotionEvent.ACTION_UP) {
						onGestureListener.onSlidingMoveUp();
					} 
					
					return r1 || r2;
				}
			}			
		});
		
		Log.i(TAG, "mVideoRegion, Register onTouch <== xxx");
		
		
		final LiveViewItemContainer.OnRefreshButtonClickListener onRefreshButtonClickListener
			= new LiveViewItemContainer.OnRefreshButtonClickListener() {
				
			@Override
			public void onClick(View v) {
				Log.i(TAG, "OnRefreshClick, " + v);
				if (liveViewManager.getPager() == null) {
					return;
				}
				
				LiveViewItemContainer c = findVideoContainerByView(v);
				
				Log.i(TAG, "OnRefreshClick, c:" + c);
				
				if (c != null) {
					int pos = liveViewManager.getIndexOfLiveView(c);
					int page = liveViewManager.getCurrentPageNumber();
					int index = (page - 1 ) * liveViewManager.getPageCapacity() + pos;
					
					liveViewManager.tryPreview(index);
				}
				
			}
		};
		


		// 初始化视频区域布局大小
		final int screenWidth = GlobalApplication.getInstance().getScreenWidth();
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, screenWidth);
		
		mVideoRegion.setLayoutParams(param);
		
		// 单通道/多通道模式切换 事件
		LiveViewManager.OnVideoModeChangedListener onVideoModeChangedListener
			= new LiveViewManager.OnVideoModeChangedListener() {

				@Override
				public void OnVideoModeChanged(boolean isMultiMode) {
					mVideoRegion.removeAllViews();
					liveViewManager.clearLiveView();

					
					Log.i(TAG, "VideoRegion, width: " + mVideoRegion.getWidth() + ", height: " + mVideoRegion.getHeight());
					
					List<PreviewDeviceItem> devices = liveViewManager.getDeviceList();
					if (devices == null || devices.size() == 0) {
						return;
					}
					
					
					if (isMultiMode) { // 多通道模式
						
						SurfaceViewMultiLayout svml = new SurfaceViewMultiLayout(RealplayActivity.this);
						svml.setLiveviewRefreshButtonClickListener(onRefreshButtonClickListener);
						
						mVideoRegion.addView(svml);
						
						List<LiveViewItemContainer> l = svml.getLiveviews();
						
						for (int i = 0; i < l.size(); i++) {
							liveViewManager.addLiveView(l.get(i));
						}										
						
					} else { // 单通道模式
						SurfaceViewSingleLayout svsl = new SurfaceViewSingleLayout(RealplayActivity.this);
						svsl.setLiveviewRefreshButtonClickListener(onRefreshButtonClickListener);
						
						mVideoRegion.addView(svsl);
						
						liveViewManager.addLiveView(svsl.getLiveview());

					}
					
					onContentChanged();
					
				}
			
		};
		
		liveViewManager.setOnVideoModeChangedListener(onVideoModeChangedListener);
		
		// 初始化为多通道模式
		//onVideoModeChangedListener.OnVideoModeChanged(true);
		
		
				
				
		
		
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
	
	private LiveViewItemContainer findVideoContainerByView(View v) {
		View curr = v;
		
		while (curr != null) {
			if (curr instanceof LiveViewItemContainer) {
				break;
			}
			
			curr = (View) curr.getParent();
		}
		
		return (LiveViewItemContainer) curr;
	}


	private void initView() {
		Button deviceList = super.getRightButton();

		deviceList.setOnClickListener(new OnClickListener() {
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
		
		mVideoRegion = (FrameLayout) findViewById(R.id.video_region);
		liveViewManager = new LiveViewManager(this);
		previewDevices = new ArrayList<PreviewDeviceItem>();
		
		
		initToolbar();
		initLandScapeToolbar();
		
		ptzControl = new PTZControl(this);

		initToolbarExtendMenu();

		initToolbarSubMenu();
		
		
		
		
	}
	
	
	public FrameLayout getVideoRegion() {
		return mVideoRegion;
	}

	private VideoPager.OnActionClickListener mPagerOnActionClickListener = new VideoPager.OnActionClickListener() {
		@Override
		public void OnActionClick(View v, ACTION action) {
			if (liveViewManager.getPager() == null) {
				return;
			}
			
			switch (action) {
			case PREVIOUS:
				liveViewManager.previousPage();				
				break;
			case NEXT:
				liveViewManager.nextPage();
				break;
			default:
				break;
			}

			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
		}
	};

	private boolean bIsPlaying = false;
	private boolean bQualityPressed = false;
	private boolean bPTZPressed = false;
	private boolean bIsMicrophoneOpen = false;
	private boolean bIsSoundOpen = false;
	private boolean bVideoRecordPressed = false;
	private Toolbar.OnItemClickListener mToolbarOnItemClickListener = new Toolbar.OnItemClickListener() {

		@Override
		public void onItemClick(ActionImageButton imgBtn) {
			//LiveViewItemContainer c = liveViewManager.getSelectedLiveView();
		
			
			
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
				mToolbar.setActionImageButtonSelected(
						Toolbar.ACTION_ENUM.VIDEO_RECORD, !bVideoRecordPressed);
				bVideoRecordPressed = !bVideoRecordPressed;
				break;
			case ALARM:
				break;
			default:
				break;
			}
		}
	};
	
	private void playAndPause() {
		if (!bIsPlaying) { // 播放
			Log.i(TAG, "play video");
			
			if (liveViewManager.getPager() != null) {
				int count = liveViewManager.getCurrentPageCount();
				
				for (int i = 0; i < count; i++) {
					liveViewManager.getListviews().get(i).getRefreshImageView().performClick();
				}
			}
		} else { // 暂停
			Log.i(TAG, "stop video");
			liveViewManager.stopPreview();
			
			bIsPlaying = false;
			updatePlayStatus(bIsPlaying);
		}
	}
	
	private void takePicture() {
		LiveViewItemContainer c = liveViewManager.getSelectedLiveView();
		
		if (c.getCurrentConnection() != null && c.getCurrentConnection().isConnected()) {
			liveViewManager.getSelectedLiveView().getSurfaceView().setTakePicture(true);
		}
	}
	
	private void updatePlayStatus(boolean isPlaying) {
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
			}

		}
	};
	
	private PTZBarClickListener mLandscapePTZBarClickListener = new PTZBarClickListener() {
		@Override
		public void ptzBarClick(View v) {
			Log.i(TAG, "ptzBarClick");
			
			switch (v.getId()) {
			case R.id.landscape_liveview_ptz_auto:
				ptzControl.ptzAuto();
				break;
			case R.id.landscape_liveview_ptz_focal_length:
				ptzControl.ptzFocalLength();
				break;
			case R.id.landscape_liveview_ptz_focus:
				ptzControl.ptzFocus();
				break;
			case R.id.landscape_liveview_ptz_aperture:
				ptzControl.ptzAperture();
				break;
			case R.id.landscape_liveview_ptz_preset_point:
				ptzControl.ptzPresetPoint();
				break;
			case R.id.landscape_liveview_ptz_bar_back:
				ptzControl.closePTZ();
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
		liveControl.getLandscapeToolbar().setOnPTZBarClickListener(mLandscapePTZBarClickListener);
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

	private void initToolbarSubMenu() {
		// mToolbarSubMenu = (LinearLayout) findViewById(R.id.toolbar_sub_menu);
		// mToolbarSubMenuText = (TextView)
		// findViewById(R.id.base_toolbar_sub_menu_text);

		// int menuWidth = (int)
		// (GlobalApplication.getInstance().getScreenWidth() / 5 * 2);
		// int menuHeight = getResources().getDimensionPixelSize(
		// R.dimen.toolbar_height);
		// LinearLayout.LayoutParams subMenuLayout = new
		// LinearLayout.LayoutParams(
		// menuWidth, menuHeight);
		// mToolbarSubMenu.setLayoutParams(subMenuLayout);
	}

	public void showToolbarExtendMenu(TOOLBAR_EXTEND_MENU menuId) {

		switch (menuId) {
		case PAGER:
			mPager.setVisibility(View.VISIBLE);
			mQualityControlbarMenu.setVisibility(View.GONE);
//			mPTZControlbarMenu.setVisibility(View.GONE);
			ptzControl.showPTZBar(false);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, false);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, false);

			bQualityPressed = false;
			bPTZPressed = false;
			break;
		case MENU_QUALITY:
			mPager.setVisibility(View.GONE);
			mQualityControlbarMenu.setVisibility(View.VISIBLE);
//			mPTZControlbarMenu.setVisibility(View.GONE);
			ptzControl.showPTZBar(false);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, true);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, false);

			bQualityPressed = true;
			bPTZPressed = false;
			break;
		case MENU_PTZ:
			mPager.setVisibility(View.GONE);
			mQualityControlbarMenu.setVisibility(View.GONE);
//			mPTZControlbarMenu.setVisibility(View.VISIBLE);
//
//			mPTZPopFrame.setVisibility(View.VISIBLE);
			ptzControl.showPTZBar(true);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, false);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, true);

			bQualityPressed = false;
			bPTZPressed = true;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case 8:
//			PreviewDeviceItem p = (PreviewDeviceItem) data.getExtras().get(
//					"DEVICE_ITEM");			
//			List<PreviewDeviceItem> devices = new ArrayList<PreviewDeviceItem>();
//			devices.add(p);
			
			Parcelable[] _devices = (Parcelable[]) data.getExtras().get("DEVICE_ITEM_LIST");
			
			List<PreviewDeviceItem> devices = new ArrayList<PreviewDeviceItem>();

			for (int i = 0; i < _devices.length; i++) {
				devices.add((PreviewDeviceItem)_devices[i]);
			}
			
			setPreviewDevices(devices);
			
			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
			
			liveViewManager.preview();
			
			liveViewManager.selectLiveView(liveViewManager.getSelectedLiveViewIndex());
			
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	

	@Override
	protected void onRestart() {
		setActiveMenuId(R.id.menu_drawer_realtime_preview);
		super.onRestart();
	}
	
	

	@Override
	protected void onStart() {
		setActiveMenuId(R.id.menu_drawer_realtime_preview);
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setActiveMenuId(R.id.menu_drawer_realtime_preview);
		
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		liveViewManager.closeAllConnection(false);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		boolean isMultiMode = liveViewManager.isMultiMode(); 
		
		if (isMultiMode) {
			editor.putInt("PREVIEW_MODE", 4);  // 当前预览模式
		} else {
			editor.putInt("PREVIEW_MODE", 1);  
		}

		editor.putInt("PAGE", liveViewManager.getCurrentPageNumber());       // 当前页数
		editor.putInt("PAGE_COUNT", liveViewManager.getCurrentPageCount());  // 当前页项数
		editor.commit();
		
		// /data/data/com.starsecurity
		try {
			PreviewItemXMLUtils.writePreviewItemListInfoToXML(liveViewManager.getDeviceList(), getString(R.string.common_last_devicelist_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.i(TAG, "onDestroy@Devices size: " + liveViewManager.getDeviceList().size() + ", mode: " + (isMultiMode ? 4 : 1) 
				+ ", page: " + liveViewManager.getCurrentPageNumber() + ", page count: " + liveViewManager.getCurrentPageCount() );
		
		super.onDestroy();
	}
	
	private void setPreviewDevices(List<PreviewDeviceItem> devices) {
		if (devices == null || devices.size() == 0) {
			throw new IllegalArgumentException("Invalid parameter devices, null or zero size.");
		}
		
		previewDevices.clear();
		
		for (int i = 0; i < devices.size(); i++) {
			previewDevices.add(devices.get(i));
		}
		
		liveViewManager.setDeviceList(previewDevices);
	}
	
	public List<PreviewDeviceItem> getPreviewDevices() {
		return previewDevices;
	}
	
	public void notifyPreviewDevicesContentChanged() {
		
	}
	

	@Override
	protected void gotoRealtimePreview() {
		
	}
	
	private void leaveRealtimePreview() {
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
	
	
	public LiveViewManager getLiveviewManager() {
		return liveViewManager;
	}
	
	
	
	private ClickEventUtils mPTZStopMoveDelay = new ClickEventUtils(new OnActionListener() {
		@Override
		public void OnAction(int clickCount, Object... params) {
			ptzControl.getPtzReqSender().stopMove();		
			liveViewManager.getSelectedLiveView().stopArrowAnimation();
		}
	}, 300);
	
	
	private OnGestureListener onGestureListener 
		= new OnGestureListener() {

		@Override
		public void onSingleClick(MotionEvent e) {	
			if (ptzControl.isPTZModeOn() && checkIsPTZDeviceConnected()) { // PTZ模式情况下单击无效
				return;
			}
			Log.i(TAG, "On single click");
			
			if (liveViewManager.getPager() == null) {
				return;
			}
		
			int pos = getIndexOfLiveview(e.getX(), e.getY());
			int index = (liveViewManager.getCurrentPageNumber() - 1) * liveViewManager.getPageCapacity() + pos;
			
			Log.i(TAG, "single_click, getX:" + e.getX() + ", getY:" + e.getY() + ", pos:" + pos + ", index:" + index);
			
			if (index > liveViewManager.getLiveViewTotalCount()) {  
				return;  // 非有效通道，不作处理 
			}
			
			liveViewManager.setCurrenSelectedLiveViewtIndex(index);  // 变更当前选择索引
			
			liveViewManager.selectLiveView(index); 
			
			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
		}
	
		@Override
		public void onDoubleClick(MotionEvent e) {
			if (ptzControl.isPTZModeOn() && checkIsPTZDeviceConnected()) { // PTZ模式情况下双击无效
				return;
			}
			
			Log.i(TAG, "On Double click");
			
			if (liveViewManager.getPager() == null) {
				return;
			}
			
			int pos;
			
			if (liveViewManager.getPageCapacity() == 1) {
				pos = 1;
			} else {
				pos = getIndexOfLiveview(e.getX(), e.getY());
			}
			
			int index = (liveViewManager.getCurrentPageNumber() - 1) * liveViewManager.getPageCapacity() + pos;
			
			Log.i(TAG, "double_click, getX:" + e.getX() + ", getY:" + e.getY() + ", pos:" + pos + ", index:" + index);
			
			if (index > liveViewManager.getLiveViewTotalCount()) {  
				return;  // 非有效通道，不作处理 
			}
			
			liveViewManager.setCurrenSelectedLiveViewtIndex(index);  // 变更当前选择索引
			
			liveViewManager.closeAllConnection(false);  // 关闭正在预览的设备
			
			if (liveViewManager.isMultiMode()) { // 切换到单通道模式
				liveViewManager.setMultiMode(false);							
				liveViewManager.preview(index);
			} else { // 切换到多通道模式
				liveViewManager.setMultiMode(true);
				
				int currPageStart = (liveViewManager.getCurrentPageNumber() - 1) * 4 + 1;
				int currPageEnd = (liveViewManager.getCurrentPageNumber() - 1) * 4 + liveViewManager.getCurrentPageCount();
				
				liveViewManager.preview(currPageStart, currPageEnd - currPageStart + 1);
				
				// 若发现此时PTZ模式开启，则重围PTZ模式，即退出PTZ模式
				if (ptzControl.isPTZModeOn()) { 
					ptzControl.showPTZBar(false);
				}
				
				
				
			}
			
			liveViewManager.selectLiveView(index); 
			
			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
		}
		
		private int getIndexOfLiveview(float x, float y) {
			int videoWidth, videoHeight;
		
			if (GlobalApplication.getInstance().isIsFullMode()) {
				videoWidth = GlobalApplication.getInstance().getScreenWidth();
				videoHeight = GlobalApplication.getInstance().getScreenHeight();
			} else {
				videoWidth = GlobalApplication.getInstance().getScreenWidth();
				videoHeight = GlobalApplication.getInstance().getScreenWidth();
			}
			
			Log.i(TAG, "videoWidth:" + videoWidth + ", videoHeight:" + videoHeight);
			
			return (x < videoWidth / 2) ? (y < videoHeight / 2 ? 1 : 3)   // 触点属于第几个视频区域
					: (y < videoHeight / 2 ? 2 : 4); 
		}
		
		@Override
		public void onSlidingLeft() {
			Log.i(TAG, "onSlidingLeft(), mIsPTZModeOn:" + ptzControl.isPTZModeOn() + " mPtzControl:" + ptzControl.getPtzReqSender()
					+ " mIsScaleOperator:" + mIsScaleOperator);
			
			if (!ptzControl.isPTZModeOn()) { // 向左滑屏
				if (liveViewManager.getPager() != null) {
					liveViewManager.nextPage();
					
					mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
					mPager.setAmount(liveViewManager.getLiveViewTotalCount());
				}
			} else { // PTZ, 向左	
				Log.i(TAG, "PTZ Action");
				liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.LEFT);
				
				if (ptzControl.getPtzReqSender() != null) {
					Log.i(TAG, "PTZ Action -----> left");
					ptzControl.getPtzReqSender().moveLeft();
				}
			}
			
			
		}
		
		@Override
		public void onSlidingRight() {
			Log.i(TAG, "onSlidingRight(), mIsPTZModeOn:" + ptzControl.isPTZModeOn() + " mPtzControl:" + ptzControl.getPtzReqSender()
					+ " mIsScaleOperator:" + mIsScaleOperator);
			if (!ptzControl.isPTZModeOn()) { // 向右滑屏
				if (liveViewManager.getPager() != null) {
					liveViewManager.previousPage();
					
					mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
					mPager.setAmount(liveViewManager.getLiveViewTotalCount());
				}
			} else { // PTZ, 向右
				Log.i(TAG, "PTZ Action");
				liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.RIGHT);
				
				if (ptzControl.getPtzReqSender() != null) {
					Log.i(TAG, "PTZ Action -----> right");
					ptzControl.getPtzReqSender().moveRight();
				}
			}
			
		}

		@Override
		public void onSlidingLeftUp() {
			liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.LEFT_UP);
			
			/*
			if (ptzControl != null) {
				ptzControl.moveLeft();
				ptzControl.moveUp();
			}*/
		}

		@Override
		public void onSlidingLeftDown() {			
			liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.LEFT_DOWN);
			
			/*
			if (ptzControl != null) {
				ptzControl.moveLeft();
				ptzControl.moveDown();
			}*/
		}

		@Override
		public void onSlidingRightUp() {
			liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.RIGHT_UP);
			
			/*
			if (ptzControl != null) {
				ptzControl.moveRight();
				ptzControl.moveUp();
			}*/
		}

		@Override
		public void onSlidingRightDown() {
			liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.RIGHT_DOWN);
			
			/*
			if (ptzControl != null) {
				ptzControl.moveRight();
				ptzControl.moveDown();
			}*/
		}

		@Override
		public void onSlidingUp() {
			Log.i(TAG, "PTZ Action");
			liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.UP);
			
			if (ptzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> up");
				ptzControl.getPtzReqSender().moveUp();
			}
		}

		@Override
		public void onSlidingDown() {
			Log.i(TAG, "PTZ Action");
			liveViewManager.getSelectedLiveView().showArrowAnimation(Constants.ARROW.DOWN);
			
			if (ptzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> down");
				ptzControl.getPtzReqSender().moveDown();
			}
		}
		
		@Override
		public void onSlidingMoveUp() {
			Log.i(TAG, "PTZ Action, onSlidingMoveUp");
			
			liveViewManager.getSelectedLiveView().stopArrowAnimation();
			
			if (ptzControl.getPtzReqSender() != null) {
				ptzControl.getPtzReqSender().stopMove();
			}
			
//			mIsPTZInMoving = false;
			ptzControl.setIsPTZInMoving(false);
			
		}

		@Override
		public void onZoomIn() {
			Log.i(TAG, "PTZ Action");
			//ToastUtils.show(RealplayActivity.this, "手势放大");
			liveViewManager.getSelectedLiveView().showFocalLengthAnimation(true);
			
			if (ptzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> focal length increase");
				ptzControl.getPtzReqSender().focalLengthIncrease();
			}
			

			mPTZStopMoveDelay.makeContinuousClickCalledOnce(this.hashCode(), new Object());
			
		}

		@Override
		public void onZoomOut() {
			Log.i(TAG, "PTZ Action");
			//ToastUtils.show(RealplayActivity.this, "手势缩小");
			liveViewManager.getSelectedLiveView().showFocalLengthAnimation(false);
			if (ptzControl.getPtzReqSender() != null) {
				Log.i(TAG, "PTZ Action -----> focal length decrease");
				ptzControl.getPtzReqSender().focalLengthDecrease();
			}
			
			mPTZStopMoveDelay.makeContinuousClickCalledOnce(this.hashCode(), new Object());
		}

		@Override
		public void onFling(int horizontalOffsetFlag, int vertitalOffsetFlag) {
			int h = horizontalOffsetFlag;
			int v = vertitalOffsetFlag;
			
			// 上下左右四个方向抛手势默认延时300ms后发送STOP_MOVE控制指令
			switch (h) {
    		case -1:
    			if (v == 0) {
    				ptzControl.setIsFlingAction(true);
    				onSlidingLeft();
    			}
    			break;
    		case 0:
    			if (v == -1) {
    				ptzControl.setIsFlingAction(true);
    				onSlidingUp();
    			} else if (v == 1) {
    				ptzControl.setIsFlingAction(true);
    				onSlidingDown();
    			}
    		case 1:
    			if (v == 0) {
    				ptzControl.setIsFlingAction(true);
    				onSlidingRight();
    			}
    			break;
    		}
			
			
			mVideoRegion.postDelayed(new Runnable() {
				@Override
				public void run() {
					ptzControl.setIsFlingAction(false);
					onSlidingMoveUp();
				}
				
			}, 300);
		}
	};
	
	
	
	
	private ViewFlipper mFlipper;			              // ViewFlipper实例
	private GestureDetector mGestureDetector;             // 定义手势检测器实例
    private ScaleGestureDetector mScaleGestureDetector;   // 缩放手势检测器实例
    
    //定义一个动画数组，用于为ViewFlipper指定切换动画效果
    Animation[] animations = new Animation[4];
    
	
	private class GestureListener extends SimpleOnGestureListener  
    {  	
	    final int FLIP_DISTANCE = 50;  //定义手势动作两点之间的最小距离
	    //private boolean mIsDoubleClick = false;
	    
	    private OnGestureListener mGestureListener;
		
		public GestureListener(OnGestureListener listener) {
			if (listener == null) {
        		throw new NullPointerException("Error parameter, listener is null");
        	}
			
			this.mGestureListener = listener;
		}
  
		@Override  
        public boolean onSingleTapUp(MotionEvent e)  
        {  
            
            return super.onSingleTapUp(e);  
        }  
		
		
		
        @Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
        	if (mGestureListener != null) {
				mGestureListener.onSingleClick(e);
			}
        	
			return true;
		}

		@Override  
        public boolean onDoubleTap(MotionEvent e)  
        {   
            Log.i("TEST", "onDoubleTap");  
            final MotionEvent _e = e;
            
            //mIsDoubleClick = true;
            
            if (mGestureListener != null) {
				mGestureListener.onDoubleClick(_e);;
			}
            
            return true;  
        }  
        
        
  
        @Override  
        public boolean onDown(MotionEvent e)  
        {   
            Log.i("TEST", "onDown");
            return true;
        }  
  
        @Override  
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
                float velocityY)  
        {  
        	float a0 = e1.getX();
        	float a1 = e2.getX();
        	float b0 = e1.getY();
        	float b1 = e2.getY();
        	
        	int h = Math.abs(a1 - a0) > FLIP_DISTANCE ? (a1 - a0 > 0 ? 1 : -1) : 0;  // -1:左；0：水平无滑动；1：右
        	int v = Math.abs(b1 - b0) > FLIP_DISTANCE ? (b1 - b0 > 0 ? 1 : -1) : 0;  // -1：上；0：垂直无滑动；1：下

        	if (!ptzControl.isPTZModeOn()) { // 非PTZ模式， 即左右滑屏
        		if (h == -1) {
        			// 为flipper设置切换的的动画效果
        			//mFlipper.setInAnimation(animations[0]);
        			//mFlipper.setOutAnimation(animations[1]);
        			//mFlipper.showPrevious();
        			
        			ToastUtils.show(RealplayActivity.this, "向左滑动");
        			mGestureListener.onSlidingLeft();
        		} else if (h == 1) {
        			// 为flipper设置切换的的动画效果
        			//mFlipper.setInAnimation(animations[2]);
        			//mFlipper.setOutAnimation(animations[3]);
        			//mFlipper.showNext();
        			
        			ToastUtils.show(RealplayActivity.this, "向右滑动");
        			mGestureListener.onSlidingRight();
        		}
        		
        		return true;
        	} else {  // PTZ模式
        		
        		mGestureListener.onFling(h, v);    		
        	}
    		
    		return false;  
        }  
  
        @Override  
        public void onLongPress(MotionEvent e)  
        {  
            Log.i("TEST", "onLongPress");  
            super.onLongPress(e);  
        }  
  
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2,  
                float distanceX, float distanceY)  
        {  
            Log.i("TEST", "onScroll:distanceX = " + distanceX + " distanceY = " + distanceY);
            
            Log.i(TAG, "mIsPTZInMoving: " + ptzControl.isPTZInMoving());
            
            /*
             * 目前服务端不支持连续发送 _OWSP_ACTIONCode事件，故若侦测到已发送云台移动事件
             * 则需等待 OWSP_ACTION_MD_STOP事件后方可再次发送
             */
            if (ptzControl.isPTZInMoving()) {
            	return true;
            }
            
            float a0 = e1.getX();
        	float b0 = e1.getY();
        	float a1 = e2.getX();
        	float b1 = e2.getY();
        	
        	int h = Math.abs(a1 - a0) > FLIP_DISTANCE ? (a1 - a0 > 0 ? 1 : -1) : 0;  // -1:左；0：水平无滑动；1：右
        	int v = Math.abs(b1 - b0) > FLIP_DISTANCE ? (b1 - b0 > 0 ? 1 : -1) : 0;  // -1：上；0：垂直无滑动；1：下
            
        	if (!ptzControl.isPTZModeOn() || !checkIsPTZDeviceConnected()) { // 若设备处于未连接或断开状态，则不启用云台控制手势        			
    			return true;
    		}
    		
    		Log.i(TAG, "h: " + h + ", v: " + v);
        	
//    		mIsPTZInMoving = true;
    		ptzControl.setIsPTZInMoving(true);
    		
    		switch (h) {
    		case -1:
    			if (v == -1) {
    				mGestureListener.onSlidingLeftUp();
    			} else if (v == 0) {
    				mGestureListener.onSlidingLeft();
    			} else if (v == 1) {
    				mGestureListener.onSlidingLeftDown();
    			}
    			break;
    		case 0:
    			if (v == -1) {
    				mGestureListener.onSlidingUp();
    			} else if (v == 0) {
//    				mIsPTZInMoving = false;
    				ptzControl.setIsPTZInMoving(false);
    			} else if (v == 1) {
    				mGestureListener.onSlidingDown();;
    			}
    			break;
    		case 1:
    			if (v == -1) {
    				mGestureListener.onSlidingRightUp();
    			} else if (v == 0) {
    				mGestureListener.onSlidingRight();
    			} else if (v == 1) {
    				mGestureListener.onSlidingRightDown();
    			}
    			break;
    		}
    		
    		
            
            return super.onScroll(e1, e2, distanceX, distanceY);  
        }  
    }
	
	private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
		private float scaleFactorSum = 0;
		private int count = 0;
		
		private OnGestureListener mGestureListener;
		
		public ScaleGestureListener(OnGestureListener listener) {
			if (listener == null) {
        		throw new NullPointerException("Error parameter, listener is null");
        	}
			
			this.mGestureListener = listener;
		}
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			//scaleFactor = detector.getScaleFactor();
			
			scaleFactorSum += detector.getScaleFactor();
			count++;
			
			Log.i(TAG, "onScale, currentSpan:" + detector.getCurrentSpan()
					+ ", previousSpan:" + detector.getPreviousSpan());
			Log.i(TAG, "onScale, scaleFactor:" + detector.getScaleFactor());
			
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			if ((ptzControl.isPTZModeOn() && !checkIsPTZDeviceConnected())
					|| !ptzControl.isPTZModeOn()) { // 未启用PTZ模式或未连接状态均不触发
				return;
			}
			
			float avg = scaleFactorSum / count; // 求均值确定放大还是缩小操作
			
			if (avg > 1) {
				mGestureListener.onZoomIn();
			} else {
				mGestureListener.onZoomOut();
			}
			
			scaleFactorSum = 0;
			count = 0;
			
		}
		
	}

	
	public static interface OnGestureListener {
		public void onSingleClick(MotionEvent e);
		public void onDoubleClick(MotionEvent e);
		public void onSlidingLeft();
		public void onSlidingLeftUp();
		public void onSlidingLeftDown();
		public void onSlidingRight();
		public void onSlidingRightUp();
		public void onSlidingRightDown();
		public void onSlidingUp();
		public void onSlidingDown();
		public void onSlidingMoveUp();
		public void onFling(int horizontalOffsetFlag, int verticalOffsetFlag);
		public void onZoomIn();
		public void onZoomOut();
	}
}
