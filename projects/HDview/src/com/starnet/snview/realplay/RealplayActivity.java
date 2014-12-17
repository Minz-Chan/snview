package com.starnet.snview.realplay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.AlarmDevice;
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
import com.starnet.snview.component.liveview.LiveViewGroup;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.QuarteredViewGroup.MODE;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.util.ActivityUtility;
import com.starnet.snview.util.PreviewItemXMLUtils;
import com.starnet.snview.util.ToastUtils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
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
	
	public static final String ALARM_DEVICE_DETAIL = "alarm_device_detail";
	
	private Toolbar mToolbar;
	private FrameLayout mControlbar;
	private LinearLayout mQualityControlbarMenu;
//	private com.starnet.snview.realplay.VideoPager mVideoPager; 	
	private com.starnet.snview.component.VideoPager mPager;
	
	private LiveViewGroup mLiveviewGroup;
	
	//private LiveViewManager liveViewManager;
	private LiveControl liveControl;
	private PTZControl ptzControl;
	
	private Animation mShotPictureAnim;
	private List<PreviewDeviceItem> mPreviewDevices;  // 当前正在预览的设备列表
	
	private int mOldOrientation;
	private boolean mIsStartedCompleted = false;
	private boolean mNeedAutoStart = true;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle inState) {
		Log.i(TAG, "onCreate()");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContainerMenuDrawer(true);
		super.onCreate(inState);
		setContentView(R.layout.realplay_activity);
		
		setBackPressedExitEventValid(true);
		getApp().init(this);		
		getApp().setHandler(mHandler);
		initView();
		initListener();
		initVideoData();
	}
	
	private GlobalApplication getApp() {
		return GlobalApplication.getInstance();
	}
	
	private void initVideoData() {
		if (isAlarmPreviewRequest()) {
			prepareAlarmPreview();
			executePreviewCurrentPage();
		} else {
			restoreLastPreviewStatus();
		}
	}
	
	private boolean isAlarmPreviewRequest() {
		boolean alarmPreivewReq = false;
		if (getIntent().getExtras() != null && getIntent().getExtras().get(
				ALARM_DEVICE_DETAIL) != null) {
			alarmPreivewReq = true;
		}
		return alarmPreivewReq;
	}
	
	private void prepareAlarmPreview() {
		final int mode = 1, page = 1, pageCount = 1;
		List<PreviewDeviceItem> devices = getAlarmDevices();
		
		restoreVideoRegionLayout(mode, page, pageCount, devices);
		requestOrientationDelaySetting();
	}
	
	private List<PreviewDeviceItem> getAlarmDevices() {
		List<PreviewDeviceItem> devices = new ArrayList<PreviewDeviceItem>();
		AlarmDevice alarmDevice = (AlarmDevice) getIntent().getExtras().get(
				ALARM_DEVICE_DETAIL);
		
		PreviewDeviceItem pdi = new PreviewDeviceItem();
		pdi.setDeviceRecordName(alarmDevice.getDeviceName());
		pdi.setSvrIp(alarmDevice.getIp());
		pdi.setSvrPort(String.valueOf(alarmDevice.getPort()));
		pdi.setLoginUser(alarmDevice.getUserName());
		pdi.setLoginPass(alarmDevice.getPassword());
		pdi.setChannel(alarmDevice.getChannel());
		devices.add(pdi);
		
		return devices;
	}
	
	private void executePreviewCurrentPage() {
		mLiveviewGroup.post(new Runnable() {
			@Override
			public void run() {
				playAndPause();
			}
		});
	}
	
	private void restoreLastPreviewStatus() {
		restoreLastPreviewVideo();
	}
	
	private void restoreLastPreviewVideo() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final int mode = sharedPreferences.getInt("PREVIEW_MODE", -1);
		final int page = sharedPreferences.getInt("PAGE", -1);  // start from 1
		final int pageCount = sharedPreferences.getInt("PAGE_COUNT", -1);
		
		List<PreviewDeviceItem> devices = getPreservedDevices();
		if (devices.size() > 0) {
			restoreVideoRegionLayout(mode, page, pageCount, devices);
			requestOrientationDelaySetting();
			refreshDeviceConnectionTask.start();
			showDeviceInfoUpdateProcess();
		} else { // 首次进入
			mPager.setAmount(0);
			mPager.setNum(0);
			
			bIsPlaying = false;
			updatePlayStatus(bIsPlaying);
			mOldOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}
	}
	
	private List<PreviewDeviceItem> getPreservedDevices() {
		return PreviewItemXMLUtils.getPreviewItemListInfoFromXML(this
				.getString(R.string.common_last_devicelist_path));
	}
	
	private void requestOrientationDelaySetting() {
		new DelayOrientationSetting().execute(new Object());
	}
	
	@SuppressWarnings("deprecation")
	private void showDeviceInfoUpdateProcess() {
		showDialog(RefreshDeviceConnectionTask.REFRESH_CLOUDACCOUT_PROCESS_DIALOG);
	}
	
	private void restoreVideoRegionLayout(int mode, int page, int pageCount, List<PreviewDeviceItem> devices) {
		Log.i(TAG, "mode: " + mode + ", page: " + page);
		if (mode != -1 && page != -1) {				
			Log.d(TAG, "mode:" + mode + ", page:" + page);
			modifyPreviewDevices(devices);
			mLiveviewGroup.regenerateLayout(
					mode == 4 ? MODE.MULTIPLE : MODE.SINGLE, // mode
					mPreviewDevices.size(),     // capacity
					mode == 4 ? (page-1)*4 : page-1);  // initial index
			
			if (!isMultiMode()) {
				ptzControl.setIsEnterPTZInSingleMode(true);
			}
		}
		mOldOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
	}
	
	private boolean isMultiMode() {
		return mLiveviewGroup.isMultiScreenMode();
	}
	
	@SuppressWarnings("deprecation")
	private RefreshDeviceConnectionTask refreshDeviceConnectionTask = 
			new RefreshDeviceConnectionTask(this) {
		private static final int DEVICE_UPDATE_SUCCESS = 0x00000001;
		private static final int DEVICE_UPDATE_FAILURE = 0x00000002;
		private static final int DEVICE_UPDATE_TIMEOUT = 0x00000004;
		
		@Override
		protected void onUpdateWorkFinished(List<PreviewDeviceItem> devices,
				boolean isDeviceConnectionInfoUpdated) {
			final List<PreviewDeviceItem> updatedPreviewDevices = devices;
			final boolean isDeviceUpdated = isDeviceConnectionInfoUpdated;
			if (devices.size() > 0) {
				RealplayActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dismissRefreshCloudAccountProcess();
						if (isDeviceUpdated) {
							modifyPreviewDevices(updatedPreviewDevices);
							showMessage(DEVICE_UPDATE_SUCCESS);
						}
						mLiveviewGroup.schedulePreview(true);
					}
				});
			}
		}
		
		@Override
		protected void onUpdateWorkTimeout() {
			RealplayActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dismissRefreshCloudAccountProcess();
					showMessage(DEVICE_UPDATE_TIMEOUT);
					mLiveviewGroup.schedulePreview(true);
				}
			});
		}
		
		@Override
		protected void onUpdateWorkFailed() {
			RealplayActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dismissRefreshCloudAccountProcess();
					showMessage(DEVICE_UPDATE_FAILURE);
					mLiveviewGroup.schedulePreview(true);
				}
			});
		}
		
		private void showMessage(int msgCode) {
			String msg = null;
			switch (msgCode) {
			case DEVICE_UPDATE_SUCCESS:
				msg = getString(R.string.realplay_update_previewdevicelist_succ);
				break;
			case DEVICE_UPDATE_FAILURE:
				msg = getString(R.string.realplay_update_previewdevicelist_failed);
				break;
			case DEVICE_UPDATE_TIMEOUT:
				msg = getString(R.string.realplay_update_previewdevicelist_timeout);
				break;
			}
			ToastUtils.show(RealplayActivity.this, msg);
		}
		
		private void dismissRefreshCloudAccountProcess() {
			RealplayActivity.this
			.dismissDialog(RefreshDeviceConnectionTask.REFRESH_CLOUDACCOUT_PROCESS_DIALOG);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case RefreshDeviceConnectionTask.REFRESH_CLOUDACCOUT_PROCESS_DIALOG:
			final ProgressDialog progress = new ProgressDialog(this);
			progress.setMessage(getString(R.string.realplay_update_previewdevicelist_wait));
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setButton(
					DialogInterface.BUTTON_NEGATIVE,
					getString(R.string.realplay_update_previewdevicelist_btn_cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							progress.cancel();
						}
					});
			progress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					refreshDeviceConnectionTask.cancel();
					progress.dismiss();
					playAndPause();
				}
			});
			return progress;
		default:
			return null;
		}
	}

	@Override
	protected void onRestart() {
		Log.i(TAG, "onRestart");
		if (mNeedAutoStart) {
			Log.i(TAG, "onRestart() ==> preview current page");
			previewCurrentPage();
		} else {
			mNeedAutoStart = true;
		}		
		super.onRestart();
	}
	
	private void previewCurrentPage() {
		if (mPager.getAmount() > 0) {
//			int currPageStart = (liveViewManager.getCurrentPageNumber() - 1)
//					* liveViewManager.getPageCapacity() + 1;
//			int currPageEnd = (liveViewManager.getCurrentPageNumber() - 1)
//					* liveViewManager.getPageCapacity()
//					+ liveViewManager.getCurrentPageCount();
//			liveViewManager.preview(currPageStart, currPageEnd - currPageStart
//					+ 1);
//			
//			mPager.setNum(currPageStart);
//			mPager.setAmount(liveViewManager.getPager().getTotalCount());
//			
//			liveViewManager.selectLiveView(currPageStart);
			
			mLiveviewGroup.previewCurrentScreen();
			mLiveviewGroup.updatePageInfo();
			
			updateUIElementsStatus();
		}
	}
	

	@Override
	protected void onStart() {
		super.setCurrentActiveViewId(R.id.menu_drawer_realtime_preview);
		super.onStart();
	}

	@Override
	protected void onStop() {
//		if (liveViewManager != null) {
//			liveViewManager.closeAllConnection(true);
//		}
		mLiveviewGroup.stopPreviewCurrentScreen();		
		super.onStop();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, "onNewIntent()");
		setIntent(intent);
		if (isAlarmPreviewRequest()) {
			Log.i(TAG, "Alarm preview request");
			mNeedAutoStart = false;
			bIsPlaying = false;
//			liveViewManager.closeAllConnection(false);
			mLiveviewGroup.stopPreviewCurrentScreen();
			prepareAlarmPreview();
			executePreviewCurrentPage();
		}
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
		int screenWidth = ActivityUtility.getScreenSize(this).x;
		int screenHeight = ActivityUtility.getScreenSize(this).y;

		getApp().setScreenWidth(screenWidth);
		getApp().setScreenHeight(screenHeight);

		// 根据新的宽度和高度重新计算mVideoRegion及其中的LiveView
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "ConfigurationChanged ->LANDSCAPE, width:" + screenWidth
					+ ", height:" + screenHeight);

			super.setMenuEnabled(false);
			super.getNavbarContainer().setVisibility(View.GONE);
			super.getToolbarContainer().setVisibility(View.GONE);
			mControlbar.setVisibility(View.GONE);

			getApp().setFullscreenMode(true);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			mLiveviewGroup.setLayoutParams(new FrameLayout.LayoutParams(
					screenWidth, screenHeight));

//			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
//					ActivityUtility.getScreenSize(this).x,
//					ActivityUtility.getScreenSize(this).y);
			// mVideoRegion.setLayoutParams(param);
//			mVideoPager.setLayoutParams(param);

			liveControl.showLandscapeToolbarFrame();

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

			Log.d(TAG, "ConfigurationChanged ->PORTRAIT, width:" + screenWidth
					+ ", height:" + screenHeight);

			super.setMenuEnabled(true);
			super.getNavbarContainer().setVisibility(View.VISIBLE);
			super.getToolbarContainer().setVisibility(View.VISIBLE);
			mControlbar.setVisibility(View.VISIBLE);

			getApp().setFullscreenMode(false);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			mLiveviewGroup.setLayoutParams(new FrameLayout.LayoutParams(
					screenWidth, screenWidth));
//			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
//					ActivityUtility.getScreenSize(this).x,
//					ActivityUtility.getScreenSize(this).x);
			// mVideoRegion.setLayoutParams(param);
//			mVideoPager.setLayoutParams(param);

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
				
				mLiveviewGroup.getSelectedLiveview().startAnimation(mShotPictureAnim);
				
				// 显示提示
				// ToastUtils.show(RealplayActivity.this, "Image Path: " + imgPath, Toast.LENGTH_LONG);
				
				Toast t = Toast.makeText(RealplayActivity.this, "", Toast.LENGTH_LONG);
				
				ToastTextView txt = new ToastTextView(RealplayActivity.this);
				txt.setText(RealplayActivity.this.getString(R.string.realplay_toast_take_pic) + imgPath);
				
				t.setView(txt);
				t.show();
				
				break;
			case Constants.SCREEN_CHANGE:
				bVideoRecordPressed = false;
				updateUIElementsStatus(false);				
				break;
			default:
				break;
			}
			
			
			super.handleMessage(msg);
		}
		
		
	};
	
	
	private void initListener() {
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
				List<LiveViewItemContainer> liveviews = 
						mLiveviewGroup.getLiveviewsInCurrentScreen();
				int size = liveviews.size();
				for (int i = 0; i < size; i++)	{
					LiveViewItemContainer c1 = liveviews.get(i);
					if (c1.isConnected() || c1.isConnecting()) {
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
//				int currPageCount = liveViewManager.getCurrentPageCount();
//				int index = liveViewManager.getIndexOfLiveView(c);
			
				if (/*index > currPageCount ||*/ c.isManualStop()) {
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
		
//		liveViewManager.setConnectionStatusListener(connectionStatusListener);
		mLiveviewGroup.setConnectionStatusListener(connectionStatusListener);
		
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
				
				mLiveviewGroup.stopPreviewCurrentScreen();
//				if (liveViewManager != null) {
//					liveViewManager.stopPreview();
//				}
			}
		});
		
		mShotPictureAnim = AnimationUtils.loadAnimation(RealplayActivity.this, R.anim.shot_picture);
//		liveViewManager = new LiveViewManager(this);
		mPreviewDevices = new ArrayList<PreviewDeviceItem>();
		
		initToolbar();
		initLandScapeToolbar();
		initToolbarExtendMenu();
		
		final int screenWidth = getApp().getScreenWidth();		
		mLiveviewGroup = new LiveViewGroup(this);
		((FrameLayout) findViewById(R.id.video_region)).addView(mLiveviewGroup, 
				new FrameLayout.LayoutParams(screenWidth, screenWidth));
//		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth, screenWidth);
		//mLiveviewGroup.setVisibility(View.GONE);
		
		ptzControl = new PTZControl(this);
		
		showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.PAGER);
	}
	
	
	public VideoPager getPager() {
		return mPager;
	}
	
//	public com.starnet.snview.realplay.VideoPager getVideoPager() {
////		return mVideoPager;
//		return  null;
//	}
	
	public PTZControl getPtzControl() {
		return ptzControl;
	}
	
	public LiveControl getLiveControl() {
		return liveControl;
	}
	
//	public LiveViewManager getLiveViewManager() {
//		return liveViewManager;
//	}
	
	public LiveViewGroup getLiveViewGroup() {
		return mLiveviewGroup;
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

	private VideoPager.OnActionClickListener mPagerOnActionClickListener = new VideoPager.OnActionClickListener() {
		@Override
		public void OnActionClick(View v, ACTION action) {
			if (mPreviewDevices.size() == 0) {
				return;
			}
			switch (action) {
			case PREVIOUS:
				mLiveviewGroup.previous();
				break;
			case NEXT:
				mLiveviewGroup.next();
				break;
			default:
				break;
			}
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
		return mPreviewDevices == null
				|| (mPreviewDevices != null && mPreviewDevices.size() == 0);
	}
	
	private void playAndPause() {
		Log.i(TAG, "playAndPause()");
		if (checkIfPreviewDeviceListEmpty()) {
			return;
		}
		
		if (!bIsPlaying) { // 播放
			Log.i(TAG, "play video");
			mLiveviewGroup.previewCurrentScreen();
//			int count = liveViewManager.getCurrentPageCount();
//
//			for (int i = 0; i < count; i++) {
//				liveViewManager.getListviews().get(i).getRefreshImageView().performClick();
//			}
		} else { // 暂停
			Log.i(TAG, "stop video");
			mLiveviewGroup.stopPreviewCurrentScreen();
//			liveViewManager.stopPreview();
	
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
		
		LiveViewItemContainer c = mLiveviewGroup.getSelectedLiveview();
		if (c.isConnected()) {
			c.takePicture();
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
			mLiveviewGroup.getSelectedLiveview().startMP4Record();
		} else { // 关闭录像
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, false);
			mLiveviewGroup.getSelectedLiveview().stopMP4Record();
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
			bVideoRecordPressed = mLiveviewGroup.getSelectedLiveview().getSurfaceView().isStartRecord();
		}
		
		if (!getApp().isIsFullMode()) {
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
		Connection conn = mLiveviewGroup.getSelectedLiveview().getConnection();
		
		if (conn.isConnected()) {
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
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.VIDEO_RECORD,
				R.drawable.toolbar_video_record_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PTZ,
				R.drawable.toolbar_ptz_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.QUALITY,
				R.drawable.toolbar_quality_high_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.MICROPHONE,
				R.drawable.toolbar_microphone_stop_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.SOUND,
				R.drawable.toolbar_sound_off_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.ALARM,
				R.drawable.toolbar_alarm_selector));

		mToolbar.createToolbar(itemList, getApp().getScreenWidth(),
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
			mNeedAutoStart = false;
			mIsStartedCompleted = true;
			
			Parcelable[] _devices = (Parcelable[]) data.getExtras().get("DEVICE_ITEM_LIST");
			List<PreviewDeviceItem> devices = new ArrayList<PreviewDeviceItem>();
			for (int i = 0; i < _devices.length; i++) {
				devices.add((PreviewDeviceItem)_devices[i]);
			}
			
			setPreviewDevices(devices);
			mLiveviewGroup.schedulePreview();
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onDestroy() {		
//		liveViewManager.closeAllConnection(false);
		mLiveviewGroup.stopPreviewCurrentScreen();
		savePreviewStatus();
		super.onDestroy();
	}
	
	private void savePreviewStatus() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		boolean isMultiScreenMode = mLiveviewGroup.isMultiScreenMode();
		if (isMultiScreenMode) {
			editor.putInt("PREVIEW_MODE", 4);  // 多通道模式
		} else {
			editor.putInt("PREVIEW_MODE", 1);  // 单通道模式
		}
		if (mPager != null) {
			editor.putInt("PAGE", mLiveviewGroup.getScreenIndex()+1); 
			editor.putInt("PAGE_COUNT", mLiveviewGroup.getScreenLimit());
		} else {
			editor.putInt("PAGE", 0);
			editor.putInt("PAGE_COUNT", 0);
		}
		editor.commit();

		try {
			PreviewItemXMLUtils.writePreviewItemListInfoToXML(mPreviewDevices,
					getString(R.string.common_last_devicelist_path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设备信息更新（适用于设备数量变化，增加或删除，需要重新进行视频控件布局）
	 * @param devices
	 */
	public void setPreviewDevices(List<PreviewDeviceItem> devices) {
		if (devices == null || devices.size() == 0) {
			throw new IllegalArgumentException(
					"Invalid parameter devices, null or zero size.");
		}

		mPreviewDevices.clear();
		for (int i = 0; i < devices.size(); i++) {
			mPreviewDevices.add(devices.get(i));
		}

		mLiveviewGroup.setVisibility(View.VISIBLE);
		mLiveviewGroup.setDevices(mPreviewDevices);
		
		if (mIsStartedCompleted) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
	
	/**
	 * 设备信息更新（适用于设备数量不变，无需重新进行视频控件布局）
	 * @param devices 设备列表
	 */
	private void modifyPreviewDevices(List<PreviewDeviceItem> devices) {
		if (devices == null || devices.size() == 0) {
			throw new IllegalArgumentException(
					"Invalid parameter devices, null or zero size.");
		}

		mPreviewDevices.clear();
		for (int i = 0; i < devices.size(); i++) {
			mPreviewDevices.add(devices.get(i));
		}
		mLiveviewGroup.setDevices(devices, false);
	}
	
	/**
	 * 获取当前正在预览的设备列表
	 * @return 设备列表信息的List
	 */
	public List<PreviewDeviceItem> getPreviewDevices() {
		return mPreviewDevices;
	}
	
	/**
	 * 当前正在预览的设备信息发生变化后调用。如若删除的收藏夹或星云平台账户中有对应的
	 * 设备，则在对预览设备列表同步后，就调用此方法
	 */
	public void notifyPreviewDevicesContentChanged() {
		if (mPreviewDevices.size() > 0) {
			if (mIsStartedCompleted) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			
//			if (mVideoPager.getAdapter() != null) {
//				mVideoPager.getAdapter().notifyDataSetChanged();
//			}
			
//			liveViewManager.setDeviceList(mPreviewDevices);
//
//			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
//			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
//			
//			liveViewManager.selectLiveView(liveViewManager.getSelectedLiveViewIndex());
			mLiveviewGroup.setVisibility(View.VISIBLE);
			mLiveviewGroup.setDevices(mPreviewDevices);
		} else {
			if (mIsStartedCompleted) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			
//			if (mVideoPager.getAdapter() != null) {
//				mVideoPager.getAdapter().notifyDataSetChanged();
//			}
			
//			liveViewManager.setDeviceList(null);
//			liveViewManager.setMultiMode(null);
//			if (getVR() != null) {
//				getVR().showSingleOrMultiMode(null);
//			}
			
			mLiveviewGroup.setVisibility(View.GONE);
			
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
		
//		if (liveViewManager != null) {
//			liveViewManager.stopPreview();
//		}
		mLiveviewGroup.stopPreviewCurrentScreen();
		
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
	
//	public LiveViewManager getLiveviewManager() {
//		return liveViewManager;
//	}
	
	public RelativeLayout getNavbarContainer() {
		return super.getNavbarContainer();
	}

	
//	private VideoPagerChangedCallback mVideoPagerChangedCallback = new VideoPagerChangedCallback() {
//		
//		@Override
//		public void getCurrentPageIndex(int index, int action) {
////			Log.i(TAG, "###第 " + index + "页, 共 " + mVideoPager.getChildCount() + " 页");
//			
//			if ((action == 0) || ptzControl.isPTZModeOn()
//					|| liveViewManager.getPager() == null) { // 不作滑动
//				return;
//			}
//			
//			Log.i(TAG, "###changeView called");
//			bVideoRecordPressed = false;
//			updateUIElementsStatus(false);
//			
//			refillLiveviews();
//			
//			if (action > 0) {
//				liveViewManager.nextPage();
//			} else {
//				liveViewManager.previousPage();
//			}
//			
//			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
//			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
//			
//			if (GlobalApplication.getInstance().isIsFullMode()) { // 若为横屏模式，且分页器显示，则刷新分页器
//				if (liveControl.getLandscapeToolbar().isLandToolbarShow()) {
//					liveControl.getLandscapeToolbar().showLandscapeToolbar();
//				}
//			}
//			
//		}
//		
//		@Override
//		public void changeView(boolean left, boolean right) {}
//	};
	
//	/**
//	 * 装载新的视频容器
//	 */
//	private void refillLiveviews() {
//		liveViewManager.closeAllConnection(false);
//		liveViewManager.resetLiveView(liveViewManager.getCurrentPageCount());
//		
//		liveViewManager.clearLiveView();
//		
//		if (liveViewManager.isMultiMode()) { // 多通道
//			Log.i(TAG, "getVR():" + getVR());
//			
//			getVR().showSingleOrMultiMode(false);
//			
//			List<LiveViewItemContainer> l = getVR().getSurfaceMultiLayout().getLiveviews();
//			for (int i = 0; i < l.size(); i++) {
//				liveViewManager.addLiveView(l.get(i));
//			}
//			
//		} else {
//			getVR().showSingleOrMultiMode(true);
//			
//			liveViewManager.addLiveView(getVR().getSurfaceSingleLayout().getLiveview());
//		}
//	} 

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
		this.mPreviewDevices = devices;
	}
}