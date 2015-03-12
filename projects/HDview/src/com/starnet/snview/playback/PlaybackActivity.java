package com.starnet.snview.playback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.LandscapeToolbar.LandControlbarClickListener;
import com.starnet.snview.component.PlaybackLandscapeToolbar;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.component.ToastTextView;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.component.Toolbar.ACTION_ENUM;
import com.starnet.snview.component.Toolbar.ActionImageButton;
import com.starnet.snview.component.audio.AudioPlayer;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.playback.utils.PlaybackControllTask.PlaybackRequest;
import com.starnet.snview.playback.utils.PlaybackDeviceItem;
import com.starnet.snview.playback.utils.PlaybackControllTask;
import com.starnet.snview.playback.utils.TLV_V_RecordInfo;
import com.starnet.snview.playback.utils.TLV_V_SearchRecordRequest;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.util.ActivityUtility;
import com.starnet.snview.util.NetWorkUtils;

public class PlaybackActivity extends BaseActivity {
	private static final String TAG = "PlaybackActivity";

	private Context context;
	private FrameLayout mControlbar;
	private Toolbar mToolbar;
	
	private RelativeLayout mLandscapePopFrame;
	private PlaybackLandscapeToolbar mPlaybackLandscapeToolbar;
	
	private TimeBar mTimebar;
	private TimeBar.TimePickedCallBack mTimeBarCallBack;

	private PlaybackLiveViewItemContainer mVideoContainer;
	private Animation mShotPictureAnim;

	private final int PLAYBACK_REQ_DIALOG = 0x0005;

	
	
	public static final int TIMESETTING_RTN_CODE = 0x0007;

	public static final int PAUSE_RESUME_TIMEOUT = 0x0002;

	public static final int NOTIFYREMOTEUIFRESH_SUC = 0x11240001;
	public static final int NOTIFYREMOTEUIFRESH_TMOUT = 0x11240002;
	public static final int NOTIFYREMOTEUIFRESH_EXCEPTION = 0x11240003;
	public static final int RECV_STREAM_DATA_FORMAT = 0x11240004;
	public static final int UPDATE_MIDDLE_TIME = 0x11240005;
	
	public static final int ACTION_PLAY_SUCC = 0x11250000;
	public static final int ACTION_PAUSE_SUCC = 0x11250001;
	public static final int ACTION_PAUSE_FAIL = 0x11250002;
	public static final int ACTION_RESUME_SUCC = 0x11250003;
	public static final int ACTION_RESUME_FAIL = 0x11250004;
	public static final int ACTION_RANDOM_SUCC = 0x11250005;
	public static final int ACTION_STOP_SUCC = 0x11250006;

	private OWSPDateTime firstRecordFileStarttime;
	private ProgressDialog prg;

	private PlaybackDeviceItem loginItem;
	private PlaybackControllTask pbcTask;
	private TLV_V_SearchRecordRequest srr;

	private boolean isPlaying = false;// 是否是正在进行播放
	private boolean isFirstIn = false; // 是否第一次进行远程回放界面
	private boolean canUpdateTimebar = true; // 当正在请求随机播放时，时间轴不更新，防止来回跳动
	private boolean hasRecordFile = false;

	private boolean bVideoRecordPressed; // 是否正在录像
	
	private PlaybackControlAction action;
	
	
	private int screenWidth;
	private int screenHeight;

	
	
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case RECV_STREAM_DATA_FORMAT:
				canUpdateTimebar = true;
				mVideoContainer.setWindowInfoText(mVideoContainer
						.getPlaybackItem().getDeviceRecordName());
				break;
			case NOTIFYREMOTEUIFRESH_SUC:
				isFirstIn = false;
				Bundle data = msg.getData();
				ArrayList<TLV_V_RecordInfo> list = data
						.getParcelableArrayList("srres");
				if (list == null) {
					isPlaying = false;
					hasRecordFile = false;
					mVideoContainer
							.setWindowInfoContent(getString(R.string.playback_status_nonerecordfile));
				} else {
					if (list.size() > 0) {
						isPlaying = true;
						hasRecordFile = true;
						firstRecordFileStarttime = list.get(0).getStartTime();
						setNewTimeBar(mTimebar, list);
						setNewTimeBar(mPlaybackLandscapeToolbar.getTimeBar(), list);
						setButtonToPause();
					} else {
						isPlaying = false;
						hasRecordFile = false;
						mVideoContainer
								.setWindowInfoContent(getString(R.string.playback_status_nonerecordfile));
					}
				}
				break;
			case NOTIFYREMOTEUIFRESH_EXCEPTION:
				isFirstIn = false;
				hasRecordFile = false;
				mVideoContainer
						.setWindowInfoContent(getString(R.string.playback_status_connect_fail));
				break;
			case NOTIFYREMOTEUIFRESH_TMOUT:
				dismissPlaybackReqDialog();
				isFirstIn = false;
				hasRecordFile = false;
				showTostContent(getString(R.string.playback_netvisit_timeout));
				mVideoContainer
						.setWindowInfoContent(getString(R.string.playback_status_connect_fail));
				break;
			case ACTION_PLAY_SUCC:
				isPlaying = true;
				setButtonToPause();
				break;
			case ACTION_PAUSE_SUCC:// 更新图标
				mVideoContainer
						.setWindowInfoContent(getString(R.string.playback_status_pause));
				isPlaying = false;
				setButtonToPlay();
				break;
			case ACTION_PAUSE_FAIL:
				boolean isOpen = NetWorkUtils.checkNetConnection(context);
				if (isOpen) {
					pause();
				} else {
					showTostContent(getString(R.string.playback_not_remoteinfo));
				}
				break;
			case ACTION_RESUME_SUCC:// 更新图标
				//dismissPlaybackReqDialog();
				isPlaying = true;
				mVideoContainer.setWindowInfoText(mVideoContainer
						.getPlaybackItem().getDeviceRecordName());
				setButtonToPause();
				break;
			case ACTION_RESUME_FAIL:
				// 不更新图标
				boolean isOpen2 = NetWorkUtils.checkNetConnection(context);
				if (isOpen2) {
					showDialog(PLAYBACK_REQ_DIALOG);
					resume();
				} else {
					showTostContent(getString(R.string.playback_not_remoteinfo));
				}
				break;
			case ACTION_RANDOM_SUCC:
				isPlaying = true;
				mVideoContainer.setWindowInfoText(mVideoContainer
						.getPlaybackItem().getDeviceRecordName());
				setButtonToPause();
				break;
			case ACTION_STOP_SUCC:
				mVideoContainer.setWindowInfoContent(getString(R.string.playback_status_stop));
				isPlaying = false;
				setButtonToPlay();
				updateTimebar(convertOWSPDateTime2Calendar(firstRecordFileStarttime));
				break;
			case PAUSE_RESUME_TIMEOUT:
				dismissPlaybackReqDialog();
				showTostContent(getString(R.string.playback_netvisit_timeout));
				break;
			case UPDATE_MIDDLE_TIME:
				if (canUpdateTimebar) {
					long timestamp = msg.getData().getLong("VIDEO_TIME");
					Calendar c = getQueryStartTimeBase();
					if (c != null) {
						c.setTimeInMillis(c.getTimeInMillis()+timestamp);
						updateTimebar(c);
					}
				}
//				Calendar c = Calendar.getInstance();
//				c.set(2015, 2, 1, 0, 0, 0);
//				c.setTimeInMillis(c.getTimeInMillis() + timestamp);
				break;
			case Constants.TAKE_PICTURE:
				String imgPath = (String) msg.getData()
						.get("PICTURE_FULL_PATH");

				// 播放声音
				new Thread(new Runnable() {
					@Override
					public void run() {
						SnapshotSound s = new SnapshotSound(
								PlaybackActivity.this);
						s.playSound();
					}
				}).start();

				mVideoContainer.startAnimation(mShotPictureAnim);

				Toast t = Toast.makeText(PlaybackActivity.this, "",
						Toast.LENGTH_LONG);
				ToastTextView txt = new ToastTextView(PlaybackActivity.this);
				txt.setText(PlaybackActivity.this
						.getString(R.string.realplay_toast_take_pic) + imgPath);
				t.setView(txt);
				t.show();
				break;
			default:
				break;
			}
		}
	};

	
	private void updateTimebar(Calendar c) {
		mTimebar.setCurrentTime(c);
		mPlaybackLandscapeToolbar.getTimeBar().setCurrentTime(c);
	}
	
	/**
	 * 获取查询起始日时间基点
	 * 
	 * @return
	 */
	private Calendar getQueryStartTimeBase() {
		Calendar c = getLastQueryStartTime();
		if (c != null) {
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
		}

		return c;
	}

	/**
	 * 获取上一次保存的起始查询时间
	 * 
	 * @return 返回上一次保存的起始查询时间，若首次进入远程回放界面，则返回null
	 */
	@SuppressLint("SimpleDateFormat")
	private Calendar getLastQueryStartTime() {
		SharedPreferences pref = getSharedPreferences(
				TimeSettingActivity.PLAYBACK_TIMESETTING, MODE_PRIVATE);
		String startTime = pref.getString("last_query_starttime", null);

		if (startTime != null) {
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			try {
				c.setTime(sdf.parse(startTime));
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
			return c;
		}

		return null;
	}
	
	private OWSPDateTime getLastQueryStartTime1() {
		Calendar c = getLastQueryStartTime();
		if (c != null) {
			return convertCalendar2OWSPDateTime(c);
		}
		return null;
	}
	
	private OWSPDateTime convertCalendar2OWSPDateTime(Calendar c) {
		OWSPDateTime t = new OWSPDateTime();
		t.setYear(c.get(Calendar.YEAR));
		t.setMonth(c.get(Calendar.MONTH)+1);
		t.setDay(c.get(Calendar.DAY_OF_MONTH));
		t.setHour(c.get(Calendar.HOUR_OF_DAY));
		t.setMinute(c.get(Calendar.MINUTE));
		t.setSecond(c.get(Calendar.SECOND));
		return t;
	}
	
	private Calendar convertOWSPDateTime2Calendar(OWSPDateTime t) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, t.getYear());
		c.set(Calendar.MONTH, t.getMonth()-1);
		c.set(Calendar.DAY_OF_MONTH, t.getDay());
		c.set(Calendar.HOUR_OF_DAY, t.getHour());
		c.set(Calendar.MINUTE, t.getMinute());
		c.set(Calendar.SECOND, t.getSecond());
		
		return c;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContainerMenuDrawer(true);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playback_activity);

		GlobalApplication.getInstance().setPlaybackHandler(mHandler);
		initView();
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC); // 指定系统音量键影响的音频流
		
		requestOrientationDelaySetting();
	}

	protected void dismissPlaybackReqDialog() {
		if (prg != null && prg.isShowing()) {
			prg.dismiss();
		}
	}

	private GlobalApplication getApp() {
		return GlobalApplication.getInstance();
	}

	public PlaybackLiveViewItemContainer getVideoContainer() {
		return mVideoContainer;
	}

	private void initView() {
		isFirstIn = true;
		context = PlaybackActivity.this;
		
		screenWidth = getApp().getScreenWidth();
		screenHeight = getApp().getScreenHeight();

		mControlbar = (FrameLayout) findViewById(R.id.playback_controlbar);
		mLandscapePopFrame = (RelativeLayout) findViewById(R.id.playback_landscape_pop_frame);
		
		super.setTitleViewText(getString(R.string.navigation_title_remote_playback));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_search_btn_selector);
		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resetTimebar();
				Intent intent = new Intent();
				intent.setClass(context, TimeSettingActivity.class);
				startActivityForResult(intent, TIMESETTING_RTN_CODE);
			}
		});

		initToolbar();
		initTimebar();
		initLandscapeToolbar();
		initVideoContainer();
	}
	
	private void resetTimebar() {
		mTimebar.reset();
		mPlaybackLandscapeToolbar.getTimeBar().reset();
	}

	/** 设置新的时间显示条 **/
	private void setNewTimeBar(TimeBar tb, ArrayList<TLV_V_RecordInfo> list) {
		if (list != null && list.size() > 0) {
			tb.reset();
			int size = list.size();
			TLV_V_RecordInfo rcd = list.get(0);
			OWSPDateTime sT = rcd.getStartTime();
			Calendar calendar = Calendar.getInstance();
			calendar.set(sT.getYear(), sT.getMonth() - 1, sT.getDay(),
					sT.getHour(), sT.getMinute(), sT.getSecond());
			tb.setCurrentTime(calendar);
			for (int i = 0; i < size; i++) {
				TLV_V_RecordInfo rcdInfo = list.get(i);
				OWSPDateTime starTime = rcdInfo.getStartTime();
				OWSPDateTime endTime = rcdInfo.getEndTime();
				addRecordFileRegion(tb, starTime, endTime);
			}
			tb.updateFileRect();
		}
	}

	/**
	 * 根据录像文件起始时间添加时间工具上的录像区块信息
	 */
	private void addRecordFileRegion(TimeBar tb, OWSPDateTime startTime,
			OWSPDateTime endTime) {
		Calendar t1 = Calendar.getInstance();
		t1.set(startTime.getYear(), startTime.getMonth() - 1,
				startTime.getDay(), startTime.getHour(), startTime.getMinute());

		Calendar t2 = Calendar.getInstance();
		t2.set(endTime.getYear(), endTime.getMonth() - 1, endTime.getDay(),
				endTime.getHour(), endTime.getMinute());
		tb.addFileInfo(1, t1, t2);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initToolbar() {
		mToolbar = super.getBaseToolbar();
		ArrayList itemList = new ArrayList();
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PICTURE,
				R.drawable.toolbar_take_picture_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.VIDEO_RECORD,
				R.drawable.toolbar_video_cut_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PLAY_PAUSE,
				R.drawable.toolbar_play_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.SOUND,
				R.drawable.toolbar_sound_off_selector));
		itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.STOP,
				R.drawable.toolbar_stop_selector)); 
		mToolbar.createToolbar(itemList, GlobalApplication.getInstance()
				.getScreenWidth(),
				getResources().getDimensionPixelSize(R.dimen.toolbar_height));
		this.mToolbar.setOnItemClickListener(mToolbarOnItemClickListener);
	}

	private void initTimebar() {
		mTimebar = (TimeBar) findViewById(R.id.timebar_control);
		mTimeBarCallBack = new TimeBar.TimePickedCallBack() {
			public void onTimePickedCallback(Calendar calendar) {
				Log.i(TAG, "Called when MOVE_UP event occurs");
				if (mVideoContainer.isInRecording()) {
					showTostContent(getString(R.string.playback_msg_canot_random));
					return;
				}
				
				canUpdateTimebar = false;
				if (pbcTask != null) {
					random(calendar);
				}
			}
		};

		mTimebar.setTimeBarCallback(mTimeBarCallBack);
		mTimebar.reset();
		mTimebar.setCurrentTime(Calendar.getInstance());
		
	}

	private void initLandscapeToolbar() {
		mPlaybackLandscapeToolbar = (PlaybackLandscapeToolbar) findViewById(R.id.playback_landscape_toolbar);
		mPlaybackLandscapeToolbar.findViews();
		
		mPlaybackLandscapeToolbar.setOnLandControlbarListener(mPlaybackLandToolbarClickListener);
		
		mPlaybackLandscapeToolbar.getTimeBar().setTimeBarCallback(mTimeBarCallBack);
		mPlaybackLandscapeToolbar.getTimeBar().reset();
		mPlaybackLandscapeToolbar.getTimeBar().setCurrentTime(Calendar.getInstance());
	}
	
	private void initLandscapeToolbarPosition() {
		int w = getApp().getScreenWidth();
		int h = getApp().getScreenHeight();
		LinearLayout landTimebarFrame = (LinearLayout) mPlaybackLandscapeToolbar
				.findViewById(R.id.playback_landscape_timebar_frame);
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) landTimebarFrame
				.getLayoutParams();
		lp.width = mPlaybackLandscapeToolbar.getTimeBarWidth();
		landTimebarFrame.setLayoutParams(lp);
		RelativeLayout.LayoutParams lpLandToolbar = (RelativeLayout.LayoutParams) mPlaybackLandscapeToolbar
				.getLayoutParams();
		int landscapeWidth = mPlaybackLandscapeToolbar.getLandscapeWidth();
		if (landscapeWidth > 0) {
			lpLandToolbar.leftMargin = ((w - landscapeWidth) / 2);
		}
		lpLandToolbar.topMargin = h * 2 / 3;
		mPlaybackLandscapeToolbar.setLayoutParams(lpLandToolbar);
	}
	
	
	private void initVideoContainer() {
		FrameLayout playbackVideoRegion = (FrameLayout) findViewById(R.id.playback_video_region);
		mVideoContainer = new PlaybackLiveViewItemContainer(this);
		mVideoContainer.findSubViews();
		mVideoContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPlaybackLandscapeToolbar.controlLandscapeToolbarShowOrHide();
			}
		});
		playbackVideoRegion.addView(mVideoContainer,
				new FrameLayout.LayoutParams(screenWidth, screenWidth));

		mShotPictureAnim = AnimationUtils.loadAnimation(PlaybackActivity.this,
				R.anim.shot_picture);
	}
	protected void randomPlay(Calendar calendar) {
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		
		Log.i(TAG, "random play, time:" + year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second);
		
		OWSPDateTime startTime = new OWSPDateTime();
		startTime.setDay(1);
		startTime.setYear(6);
		startTime.setMonth(3);
		startTime.setHour(14);
		startTime.setMinute(0);
		startTime.setSecond(24);
		
//		startTime.setDay(day);
//		startTime.setYear(year-2009);
//		startTime.setMonth(month);
//		startTime.setHour(hour);
//		startTime.setMinute(minute);
//		startTime.setSecond(second);
		start(startTime);
	}

	@SuppressLint("SimpleDateFormat")
	private void saveLastQueryStartTime(Calendar c) {
		SharedPreferences pref = getSharedPreferences(
				TimeSettingActivity.PLAYBACK_TIMESETTING, MODE_PRIVATE);
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		Editor editor = pref.edit();
		editor.putString("last_query_starttime", sdf.format(c.getTime()));
		editor.commit();
	}

	private Toolbar.OnItemClickListener mToolbarOnItemClickListener = new Toolbar.OnItemClickListener() {
		@Override
		public void onItemClick(ActionImageButton imgBtn) {
			if (pbcTask == null) {
				return;
			}
			
			switch (imgBtn.getItemData().getActionID()) {
			case PLAY_PAUSE:
				if (isFirstIn) {
					showTostContent(getString(R.string.playback_not_remoteinfo));
				} else {
					String curTime = mTimebar.getCurrentTime();
					playOrPause(PlaybackUtils.getOWSPDateTime(curTime));
				}
				break;
			case PICTURE:
				mVideoContainer.takePicture();
				break;
			case VIDEO_RECORD:
				processVideoRecord();
				break;
			case SOUND:
				controlSound();
				break;
			case STOP:
				stop();
				break;
			default:
				break;
			}
		}
	};
	
	private LandControlbarClickListener mPlaybackLandToolbarClickListener = new LandControlbarClickListener() {
		@Override
		public void landControlbarClick(View v) {
			if (pbcTask == null) {
				return;
			}
			
			switch (v.getId()) {
			case R.id.playback_landscape_capture_button:
				mVideoContainer.takePicture();
				break;
			case R.id.playback_landscape_record_button:
				processVideoRecord();
				break;
			case R.id.playback_landscape_pause_play_button:
				if (isFirstIn) {
					showTostContent(getString(R.string.playback_not_remoteinfo));
				} else {
					String curTime = mPlaybackLandscapeToolbar.getTimeBar().getCurrentTime();
					playOrPause(PlaybackUtils.getOWSPDateTime(curTime));
				}
				break;
			case R.id.playback_landscape_sound_button:
				controlSound();
				break;
			case R.id.playback_landscape_stop_button:
				stop();
				break;
			default:
				break;
			}
			
		}
	};

	@SuppressWarnings("deprecation")
	private void playOrPause(OWSPDateTime startTime) {
		boolean isOpen = NetWorkUtils.checkNetConnection(context);
		if (isOpen) {
			if (isPlaying) {// 如果正在进行播放,单击按钮进行暂停
				if (hasRecordFile) {
					pause();
				} else {
					showTostContent(getString(R.string.playback_not_remoteinfo));
				}
			} else {
				if (hasRecordFile) {
					if (action == PlaybackControlAction.PAUSE
							|| action == PlaybackControlAction.RANDOM_PLAY) {
						resume();
					} else if (action == PlaybackControlAction.STOP) {
						start(startTime);
					}
					
				} else {
					showTostContent(getString(R.string.playback_not_remoteinfo));
				}
			}
		} else {
			showTostContent(getString(R.string.playback_not_open_play));
		}
	}
	
	private void setButtonToPlay() {
		mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
				R.drawable.toolbar_play_selector);
		mPlaybackLandscapeToolbar.getPausePlayButton().setSelected(true);
	}	
	
	private void setButtonToPause() {
		mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
				R.drawable.toolbar_pause_selector);
		mPlaybackLandscapeToolbar.getPausePlayButton().setSelected(false);
	}

	private void processVideoRecord() {
		Log.i(TAG, "processVideoRecord");
		if (!isPlaying) {
			bVideoRecordPressed = false;
			setRecordButtonSelected(false);
			return;
		}

		bVideoRecordPressed = !bVideoRecordPressed;

		if (bVideoRecordPressed) { // 开启录像
			setRecordButtonSelected(true);
			mVideoContainer.startMP4Record();
		} else { // 关闭录像
			setRecordButtonSelected(false);
			mVideoContainer.stopMP4Record();
		}
	}
	
	private void setRecordButtonSelected(boolean selected) {
		if (selected) {
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, true);
			mPlaybackLandscapeToolbar.getRecordButton().setSelected(true);
		} else {
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, false);
			mPlaybackLandscapeToolbar.getRecordButton().setSelected(false);
		}
	}
	
	
	
	private void controlSound() {
		AudioPlayer ap = getAudioPlayer();
		
		if (ap == null) {
			return;
		}
		
		if (ap.isSoundOn()) {
			ap.turnSoundOff();
			mToolbar.setActionImageButtonBg(ACTION_ENUM.SOUND, R.drawable.toolbar_sound_selector);
			mPlaybackLandscapeToolbar.getSoundButton().setSelected(true);
		} else {
			ap.turnSoundOn();
			mToolbar.setActionImageButtonBg(ACTION_ENUM.SOUND, R.drawable.toolbar_sound_off_selector);
			mPlaybackLandscapeToolbar.getSoundButton().setSelected(false);
		}
	}
	
	private AudioPlayer getAudioPlayer() {
		if (pbcTask != null) {
			return pbcTask.getAudioPlayer();
		}
		
		return null;
	}	

	private void showTostContent(String content) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}

	public boolean isFirstPlay() {
		return isFirstIn;
	}

	public boolean isPlaying() {
		return isPlaying;
	}
	
	public PlaybackControlAction getAction() {
		return action;
	}
	
	private void start() {
		mVideoContainer.setWindowInfoContent(getString(R.string.playback_status_play_requsting));
		action = PlaybackControlAction.PLAY;
		pbcTask.start();
	}
	
	private void start(OWSPDateTime startTime) {
		mVideoContainer.setWindowInfoContent(getString(R.string.playback_status_play_requsting));
		action = PlaybackControlAction.PLAY;
		pbcTask.start(startTime);
	}

	private void resume() {
		mVideoContainer.setWindowInfoContent(getString(R.string.playback_status_resume_requesting));
		action = PlaybackControlAction.RESUME;
		pbcTask.resume();
	}

	private void pause() {
		mVideoContainer.setWindowInfoContent(getString(R.string.playback_status_pause_requesting));
		action = PlaybackControlAction.PAUSE;
		pbcTask.pause();
	}

	private void stop() {
		mVideoContainer.setWindowInfoContent(getString(R.string.playback_status_stop_requesting));
		action = PlaybackControlAction.STOP;
		pbcTask.stop();
	}
	
	protected void random(Calendar calendar) {
		mVideoContainer.setWindowInfoContent(getString(R.string.playback_status_play_requsting));
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		Log.i(TAG, "random play, time:" + year + "-" + month + "-" + day + " "
				+ hour + ":" + minute + ":" + second);

		final OWSPDateTime startTime = new OWSPDateTime();
		startTime.setYear(year);
		startTime.setMonth(month);
		startTime.setDay(day);
		startTime.setHour(hour);
		startTime.setMinute(minute);
		startTime.setSecond(second);
//		startTime.setDay(1);
//		startTime.setYear(2015);
//		startTime.setMonth(3);
//		startTime.setHour(13);
//		startTime.setMinute(58);
//		startTime.setSecond(0);
		
		action = PlaybackControlAction.RANDOM_PLAY;
		pbcTask.random(startTime);
		pbcTask.resumePlay();
//		if (/*action != PlaybackControlAction.PAUSE
//				&& */action != PlaybackControlAction.STOP) {
//			
//		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TIMESETTING_RTN_CODE) {
			/*
			 * FOR TESTING ...
			 */

//			isFirstIn = false;
//			PlaybackDeviceItem item = new PlaybackDeviceItem();
//			item.setDeviceRecordName("test");
//			item.setChannel(3);
//			mVideoContainer.setPlaybackItem(item);
//			mVideoContainer.setDeviceRecordName(item.getDeviceRecordName());
//
//			testStartPlayTask(srr, item);

			/*
			 * REAL CODE
			 */
			if (data != null) {
				isFirstIn = false;
				Bundle bundle = data.getExtras();
				srr = (TLV_V_SearchRecordRequest) bundle.getParcelable("srr");
				loginItem = bundle.getParcelable("loginItem");
				if (loginItem != null) {
					startPlayTaskWithLoginItem(srr, loginItem);
				} else {
					testStartPlayTask(srr, loginItem);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void startPlayTaskWithLoginItem(TLV_V_SearchRecordRequest srr,
			PlaybackDeviceItem playbackItem) {
		mVideoContainer.setPlaybackItem(playbackItem);
		mVideoContainer.setDeviceRecordName(playbackItem.getDeviceRecordName());
		
		if (pbcTask != null) {
			pbcTask.exit();
			pbcTask = null;
		}
		PlaybackRequest pr = new PlaybackRequest();
		pr.setSearchRecordRequestInfo(srr);
		pr.setDeviceInfo(playbackItem);
		pbcTask = new PlaybackControllTask(context, mHandler, pr);
		start();
	}

	protected void testStartPlayTask(TLV_V_SearchRecordRequest srr1,
			PlaybackDeviceItem dItem1) {
		PlaybackDeviceItem dItem = new PlaybackDeviceItem();
		// dItem.setSvrIp("61.131.16.27");
		String ips = "10.18.72.222";
		dItem.setSvrIp(ips);
		dItem.setSvrPort("8080");
		// dItem.setSvrPort("9509");
		dItem.setLoginUser("admin");
		dItem.setLoginPass("");
		srr = new TLV_V_SearchRecordRequest();
		OWSPDateTime stTime = new OWSPDateTime();

		// channel 4, 2.12 14:50 CIF 2.12 15:00 D1
		// stTime.setYear(2015 - 2009);
		// stTime.setMonth(2);
		// stTime.setDay(12);
		// stTime.setHour(14);
		// stTime.setMinute(50);
		// stTime.setSecond(0);

		// channel 2, 2.12 12:00 1280x960
		stTime.setYear(2015 - 2009);
		stTime.setMonth(3);
		stTime.setDay(1);
		stTime.setHour(13);
		stTime.setMinute(45);
		stTime.setSecond(0);
		srr.setStartTime(stTime);

		OWSPDateTime endTime = new OWSPDateTime();
		endTime.setYear(2015 - 2009);
		endTime.setMonth(3);
		endTime.setDay(1);
		endTime.setHour(16);
		endTime.setMinute(42);
		endTime.setSecond(0);
		srr.setEndTime(endTime);

		srr.setCount(255);
		srr.setRecordType(0);
		srr.setDeviceId(0);
		srr.setChannel(2);

		if (pbcTask != null) {
			pbcTask.exit();
			pbcTask = null;
		}

		PlaybackRequest pr = new PlaybackRequest();
		pr.setSearchRecordRequestInfo(srr);
		pr.setDeviceInfo(dItem);
		pbcTask = new PlaybackControllTask(context, mHandler, pr);
		start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case PLAYBACK_REQ_DIALOG:
			prg = new ProgressDialog(this);
			prg.setMessage(getString(R.string.playback_timesetting_reqinfo));
			prg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			prg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (prg != null && prg.isShowing()) {
						prg.dismiss();
						pbcTask.setCancel(true);
					}
				}
			});
			return prg;
		default:
			return null;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		closeRemoteSocket();
		if (mVideoContainer.isInRecording()) {
			setRecordButtonSelected(false);
			mVideoContainer.stopMP4Record();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void closeRemoteSocket() {
		if (pbcTask != null) {
			pbcTask.exit();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		int screenWidth = ActivityUtility.getScreenSize(this).x;
		int screenHeight = ActivityUtility.getScreenSize(this).y;
		getApp().setScreenWidth(screenWidth);
		getApp().setScreenHeight(screenHeight);
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.i(TAG, "onConfigurationChanged ==> ORIENTATION_LANDSCAPE");
			super.setMenuEnabled(false);
			super.getNavbarContainer().setVisibility(View.GONE);
			super.getToolbarContainer().setVisibility(View.GONE);
			mControlbar.setVisibility(View.GONE);
			getApp().setFullscreenMode(true);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mVideoContainer.setLayoutParams(new FrameLayout.LayoutParams(
					screenWidth, screenHeight));

			mLandscapePopFrame.setVisibility(View.VISIBLE);
			initLandscapeToolbarPosition();
			mPlaybackLandscapeToolbar.showLandscapeToolbar();			
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.i(TAG, "onConfigurationChanged ==> ORIENTATION_PORTRAIT");
			super.setMenuEnabled(true);
			super.getNavbarContainer().setVisibility(View.VISIBLE);
			super.getToolbarContainer().setVisibility(View.VISIBLE);
			mControlbar.setVisibility(View.VISIBLE);
			getApp().setFullscreenMode(false);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mVideoContainer.setLayoutParams(new FrameLayout.LayoutParams(
					screenWidth, screenWidth));
			
			mLandscapePopFrame.setVisibility(View.GONE);
			mPlaybackLandscapeToolbar.hideLandscapeToolbar();
		}
		
		
		super.onConfigurationChanged(newConfig);
	}

	public Handler getHandler() {
		return mHandler;
	}
	
	private void requestOrientationDelaySetting() {
		new DelayOrientationSetting().execute(new Object());
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
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}
}
