package com.starnet.snview.realplay;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.component.VideoPager;
import com.starnet.snview.component.Toolbar.ActionImageButton;
import com.starnet.snview.component.VideoPager.ACTION;
import com.starnet.snview.component.liveview.LiveView;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.LiveViewManager;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.util.ActivityUtility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class RealplayActivity extends BaseActivity {

	// private static final String TAG = "RealplayActivity";

	private Toolbar mToolbar;

	private VideoPager mPager;
	private LinearLayout mQualityControlbarMenu;
	private LinearLayout mPTZControlbarMenu;
	private LinearLayout mPTZPopFrame;

	private LiveViewItemContainer liveViewContainer1;
	// private LinearLayout mToolbarSubMenu;
	// private TextView mToolbarSubMenuText;

	private LiveView mLiveView1;
	private LiveView mLiveView2;
	private LiveView mLiveView3;
	private LiveView mLiveView4;
	
	
	private int page = 1;
	
	private LiveViewManager liveViewManager;
	
	private FrameLayout mVideoRegion;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		setContentView(R.layout.realplay_activity);

		GlobalApplication.getInstance().setScreenWidth(
				ActivityUtility.getScreenSize(this).x);

		initView();

		test();

	}

	private void test() {
		mVideoRegion = (FrameLayout) findViewById(R.id.video_region);
		liveViewManager = new LiveViewManager(this);
		// mLiveView1 = (LiveView) findViewById(R.id.liveview1);
		//
		//
		// List<LiveView> l = new ArrayList<LiveView>();
		// l.add(mLiveView1);
		//
		// ConnectionManager.getInstance().bindLiveViewList(l);
		
		// 根据单通道或多通道模式初始化相关控件
		
		
		final LiveViewItemContainer.OnLiveViewContainerClickListener onLiveViewContainerClickListener 
			= new LiveViewItemContainer.OnLiveViewContainerClickListener() {

			@Override
			public void onClick(View v) {
				LiveViewItemContainer lvContainer = (LiveViewItemContainer) v;
				int pos = liveViewManager.getIndexOfLiveView(lvContainer);
				int index = (page - 1) * 4 + pos;

				liveViewManager.selectLiveView(index);
			}
		};
		
		
//		liveViewContainer1 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem1);
//
//		liveViewContainer1.setLiveViewContainerClickListener(onLiveViewContainerClickListener);
//		liveViewContainer1.findSubViews();
//		liveViewContainer1.initListener();
//		
//		liveViewContainer1.getSurfaceView().setLayoutParams(param);
//		
//		liveViewManager.addLiveView(liveViewContainer1);
		
		final int screenWidth = GlobalApplication.getInstance().getScreenWidth();
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(screenWidth, screenWidth);
		
		mVideoRegion.setLayoutParams(param);
		
		LiveViewManager.OnVideoModeChangedListener onVideoModeChangedListener
			= new LiveViewManager.OnVideoModeChangedListener() {

				@Override
				public void OnVideoModeChanged(boolean isMultiMode) {
					mVideoRegion.removeAllViews();
					liveViewManager.clearLiveView();
					
					if (isMultiMode) { // 多通道模式
						((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
								.inflate(R.layout.surfaceview_multi_layout,
										mVideoRegion, true);
						
						LinearLayout linear1 = (LinearLayout) findViewById(R.id.view_video_linear1);
						LinearLayout linear2 = (LinearLayout) findViewById(R.id.view_video_linear2);
						LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, screenWidth / 2);
						
						linear1.setLayoutParams(param1);
						linear2.setLayoutParams(param1);
						
						LiveViewItemContainer liveview1 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem1);
						LiveViewItemContainer liveview2 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem2);
						LiveViewItemContainer liveview3 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem3);
						LiveViewItemContainer liveview4 = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem4);
						
						liveview1.setLiveViewContainerClickListener(onLiveViewContainerClickListener);
						liveview1.findSubViews();
						liveview1.initListener();
						//liveview1.getSurfaceView().setBackgroundColor(Color.RED);
						
						liveview2.setLiveViewContainerClickListener(onLiveViewContainerClickListener);
						liveview2.findSubViews();
						liveview2.initListener();
						//liveview2.getSurfaceView().setBackgroundColor(Color.YELLOW);
						
						liveview3.setLiveViewContainerClickListener(onLiveViewContainerClickListener);
						liveview3.findSubViews();
						liveview3.initListener();
						//liveview3.getSurfaceView().setBackgroundColor(Color.WHITE);
						
						liveview4.setLiveViewContainerClickListener(onLiveViewContainerClickListener);
						liveview4.findSubViews();
						liveview4.initListener();
						//liveview4.getSurfaceView().setBackgroundColor(Color.GREEN);
						
						liveViewManager.addLiveView(liveview1);
						liveViewManager.addLiveView(liveview2);
						liveViewManager.addLiveView(liveview3);
						liveViewManager.addLiveView(liveview4);						
						
					} else { // 单通道模式
						((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.surfaceview_single_layout,
								mVideoRegion, true);
						
						LiveViewItemContainer liveview = (LiveViewItemContainer) findViewById(R.id.liveview_liveitem);
						
						liveview.setLiveViewContainerClickListener(onLiveViewContainerClickListener);
						liveview.findSubViews();
						liveview.initListener();
						//liveview.getSurfaceView().setBackgroundColor(Color.BLUE);
						
						liveViewManager.addLiveView(liveview);
					}
					
					onContentChanged();
					
				}
			
		};
		
		liveViewManager.setOnVideoModeChangedListener(onVideoModeChangedListener);
		
		onVideoModeChangedListener.OnVideoModeChanged(true);
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
				
				if (liveViewManager != null) {
					liveViewManager.stopPreview();
				}
			}
		});

		initToolbar();

		initToolbarExtendMenu();

		initToolbarSubMenu();
		
		
	}
	
	
	public FrameLayout getVideoRegion() {
		return mVideoRegion;
	}

	private VideoPager.OnActionClickListener mPagerOnActionClickListener = new VideoPager.OnActionClickListener() {
		@Override
		public void OnActionClick(View v, ACTION action) {
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

			switch (imgBtn.getItemData().getActionID()) {
			case PLAY_PAUSE:
				if (!bIsPlaying) { // 播放
					bIsPlaying = true;
					mToolbar.setActionImageButtonBg(
							Toolbar.ACTION_ENUM.PLAY_PAUSE,
							R.drawable.toolbar_pause_selector);
				} else { // 暂停
					bIsPlaying = false;
					mToolbar.setActionImageButtonBg(
							Toolbar.ACTION_ENUM.PLAY_PAUSE,
							R.drawable.toolbar_play_selector);
				}

				break;
			case PICTURE:
				Toast.makeText(
						getBaseContext(),
						"Width: " + mPager.getPrevious().getWidth()
								+ ", height: "
								+ mPager.getPrevious().getHeight(),
						Toast.LENGTH_LONG).show();
				break;
			case QUALITY:
				bQualityPressed = !bQualityPressed;

				mPTZPopFrame.setVisibility(View.GONE);
				showPTZFrame(PTZ_POP_FRAME.SCAN, false);

				if (bQualityPressed) {
					showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.MENU_QUALITY);
				} else {
					showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.PAGER);
				}

				break;
			case PTZ:
				bPTZPressed = !bPTZPressed;

				mPTZPopFrame.setVisibility(View.GONE);
				showPTZFrame(PTZ_POP_FRAME.SCAN, false);
				mPTZMenuScan.setSelected(false);
				mPTZMenuFocalLength.setSelected(false);
				mPTZMenuFocus.setSelected(false);
				mPTZMenuAperture.setSelected(false);
				mPTZMenuPreset.setSelected(false);

				if (bPTZPressed) {
					showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.MENU_PTZ);
				} else {
					showToolbarExtendMenu(TOOLBAR_EXTEND_MENU.PAGER);
				}
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

	private enum TOOLBAR_EXTEND_MENU {
		PAGER, MENU_QUALITY, MENU_PTZ
	}

	private ImageButton mQualityMenuFluency;
	private ImageButton mQualityMenuStandard;
	private ImageButton mQualityMenuHigh;
	private ImageButton mQualityMenuCustom;
	private ImageButton mPTZMenuScan;
	private ImageButton mPTZMenuFocalLength;
	private ImageButton mPTZMenuFocus;
	private ImageButton mPTZMenuAperture;
	private ImageButton mPTZMenuPreset;

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

	private enum PTZ_POP_FRAME {
		SCAN, FOCAL_LENGTH, FOCUS, APERTURE, PRESET
	};

	private OnClickListener mOnPTZMenuClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.ptz_controlbar_menu_scan:
				break;
			case R.id.ptz_controlbar_menu_focal_length:
				if (!mPTZMenuFocalLength.isSelected()) {
					// mToolbarSubMenu.setVisibility(View.VISIBLE);
					// mToolbarSubMenuText.setText(getString(R.string.toolbar_sub_menu_focal_length));
					showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, true);
					mPTZMenuScan.setSelected(false);
					mPTZMenuFocalLength.setSelected(true);
					mPTZMenuFocus.setSelected(false);
					mPTZMenuAperture.setSelected(false);
					mPTZMenuPreset.setSelected(false);
				} else {
					// mToolbarSubMenu.setVisibility(View.GONE);
					showPTZFrame(PTZ_POP_FRAME.FOCAL_LENGTH, false);
					mPTZMenuFocalLength.setSelected(false);
				}
				break;
			case R.id.ptz_controlbar_menu_focus:
				if (!mPTZMenuFocus.isSelected()) {
					// mToolbarSubMenu.setVisibility(View.VISIBLE);
					// mToolbarSubMenuText.setText(getString(R.string.toolbar_sub_menu_focus));
					showPTZFrame(PTZ_POP_FRAME.FOCUS, true);
					mPTZMenuScan.setSelected(false);
					mPTZMenuFocalLength.setSelected(false);
					mPTZMenuFocus.setSelected(true);
					mPTZMenuAperture.setSelected(false);
					mPTZMenuPreset.setSelected(false);
				} else {
					// mToolbarSubMenu.setVisibility(View.GONE);
					showPTZFrame(PTZ_POP_FRAME.FOCUS, false);
					mPTZMenuFocus.setSelected(false);
				}
				break;
			case R.id.ptz_controlbar_menu_aperture:
				if (!mPTZMenuAperture.isSelected()) {
					// mToolbarSubMenu.setVisibility(View.VISIBLE);
					// mToolbarSubMenuText.setText(getString(R.string.toolbar_sub_menu_aperture));
					showPTZFrame(PTZ_POP_FRAME.APERTURE, true);
					mPTZMenuScan.setSelected(false);
					mPTZMenuFocalLength.setSelected(false);
					mPTZMenuFocus.setSelected(false);
					mPTZMenuAperture.setSelected(true);
					mPTZMenuPreset.setSelected(false);
				} else {
					// mToolbarSubMenu.setVisibility(View.GONE);
					showPTZFrame(PTZ_POP_FRAME.APERTURE, false);
					mPTZMenuAperture.setSelected(false);
				}
				break;
			case R.id.ptz_controlbar_menu_preset:
				break;
			}

		}
	};

	private void showPTZFrame(PTZ_POP_FRAME ppf, boolean isShow) {
		if (isShow) {
			switch (ppf) {
			case SCAN:
				break;
			case FOCAL_LENGTH:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				break;
			case FOCUS:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.GONE);
				break;
			case APERTURE:
				((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
						.setVisibility(View.VISIBLE);
				break;
			case PRESET:
				break;
			}
		} else {
			((LinearLayout) findViewById(R.id.ptz_pop_focal_length_frame))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ptz_pop_focus_frame))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ptz_pop_aperture_frame))
					.setVisibility(View.GONE);
		}
	}

	private void initToolbarExtendMenu() {
		mPager = (VideoPager) findViewById(R.id.pager);
		mPager.initContent(ActivityUtility.getScreenSize(this).x,
				getResources().getDimensionPixelSize(R.dimen.toolbar_height));
		mPager.setOnActionClickListener(mPagerOnActionClickListener);

		mQualityControlbarMenu = (LinearLayout) findViewById(R.id.quality_controlbar_menu);
		mQualityMenuFluency = (ImageButton) findViewById(R.id.quality_controlbar_menu_fluency);
		mQualityMenuStandard = (ImageButton) findViewById(R.id.quality_controlbar_menu_standard);
		mQualityMenuHigh = (ImageButton) findViewById(R.id.quality_controlbar_menu_high);
		mQualityMenuCustom = (ImageButton) findViewById(R.id.quality_controlbar_menu_custom);

		mQualityMenuFluency.setOnClickListener(mOnQualityMenuClickListener);
		mQualityMenuStandard.setOnClickListener(mOnQualityMenuClickListener);
		mQualityMenuHigh.setOnClickListener(mOnQualityMenuClickListener);
		mQualityMenuCustom.setOnClickListener(mOnQualityMenuClickListener);

		mPTZControlbarMenu = (LinearLayout) findViewById(R.id.ptz_controlbar_menu);
		mPTZMenuScan = (ImageButton) findViewById(R.id.ptz_controlbar_menu_scan);
		mPTZMenuFocalLength = (ImageButton) findViewById(R.id.ptz_controlbar_menu_focal_length);
		mPTZMenuFocus = (ImageButton) findViewById(R.id.ptz_controlbar_menu_focus);
		mPTZMenuAperture = (ImageButton) findViewById(R.id.ptz_controlbar_menu_aperture);
		mPTZMenuPreset = (ImageButton) findViewById(R.id.ptz_controlbar_menu_preset);

		mPTZMenuScan.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuFocalLength.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuFocus.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuAperture.setOnClickListener(mOnPTZMenuClickListener);
		mPTZMenuPreset.setOnClickListener(mOnPTZMenuClickListener);

		mPTZPopFrame = (LinearLayout) findViewById(R.id.ptz_pop_frame);

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

	private void showToolbarExtendMenu(TOOLBAR_EXTEND_MENU menuId) {

		switch (menuId) {
		case PAGER:
			mPager.setVisibility(View.VISIBLE);
			mQualityControlbarMenu.setVisibility(View.GONE);
			mPTZControlbarMenu.setVisibility(View.GONE);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, false);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, false);

			bQualityPressed = false;
			bPTZPressed = false;
			break;
		case MENU_QUALITY:
			mPager.setVisibility(View.GONE);
			mQualityControlbarMenu.setVisibility(View.VISIBLE);
			mPTZControlbarMenu.setVisibility(View.GONE);

			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.QUALITY, true);
			mToolbar.setActionItemSelected(Toolbar.ACTION_ENUM.PTZ, false);

			bQualityPressed = true;
			bPTZPressed = false;
			break;
		case MENU_PTZ:
			mPager.setVisibility(View.GONE);
			mQualityControlbarMenu.setVisibility(View.GONE);
			mPTZControlbarMenu.setVisibility(View.VISIBLE);

			mPTZPopFrame.setVisibility(View.VISIBLE);

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
			
			liveViewManager.setDeviceList(devices);
			
			mPager.setNum(liveViewManager.getSelectedLiveViewIndex());
			mPager.setAmount(liveViewManager.getLiveViewTotalCount());
			
			liveViewManager.preview();
			
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
