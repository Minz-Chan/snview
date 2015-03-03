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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.component.ToastTextView;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.component.Toolbar.ACTION_ENUM;
import com.starnet.snview.component.Toolbar.ActionImageButton;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.global.Constants;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.playback.utils.PlaybackControllTask.PlaybackRequest;
import com.starnet.snview.playback.utils.PlaybackDeviceItem;
import com.starnet.snview.playback.utils.PlaybackControllTask;
import com.starnet.snview.playback.utils.TLV_V_RecordInfo;
import com.starnet.snview.playback.utils.TLV_V_SearchRecordRequest;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.util.NetWorkUtils;

public class PlaybackActivity extends BaseActivity {
	private static final String TAG = "PlaybackActivity";

	private Context ctx;
	private Toolbar mToolbar;
	private TimeBar mTimebar;
	private TimeBar.TimePickedCallBack mTimeBarCallBack;

	private PlaybackLiveViewItemContainer mVideoContainer;
	private Animation mShotPictureAnim;

	private final int PLAYBACK_REQ_DIALOG = 0x0005;

	public static final int UPDATE_MIDDLE_TIME = 0x99990001;
	public static final int RECV_STREAM_DATA_FORMAT = 0x12342222;
	public static final int TIMESETTING_RTN_CODE = 0x0007;

	public static final int PAUSE_RESUME_TIMEOUT = 0x0002;

	public static final int NOTIFYREMOTEUIFRESH_SUC = 0x0008;
	public static final int NOTIFYREMOTEUIFRESH_TMOUT = 0x0006;
	public static final int NOTIFYREMOTEUIFRESH_EXCEPTION = 0x0009;

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
//	private boolean isOnPlayControl = false; // 是否正在进行播放控制（暂停、继续）
//	private boolean isInRandomPlay = false;
	private boolean hasRecordFile = false;

	private boolean bVideoRecordPressed; // 是否正在录像
	
	private PlaybackControlAction action;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case RECV_STREAM_DATA_FORMAT:
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
						setNewTimeBar(list);
						mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
								R.drawable.toolbar_pause_selector);
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
				break;
			case ACTION_PLAY_SUCC:
				isPlaying = true;
				mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
						R.drawable.toolbar_pause_selector);
				break;
			case ACTION_PAUSE_SUCC:// 更新图标
				mVideoContainer
						.setWindowInfoContent(getString(R.string.playback_status_pause));
				isPlaying = false;
				mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
						R.drawable.toolbar_play_selector);
				break;
			case ACTION_PAUSE_FAIL:
				boolean isOpen = NetWorkUtils.checkNetConnection(ctx);
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
				mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
						R.drawable.toolbar_pause_selector);
				break;
			case ACTION_RESUME_FAIL:
				// 不更新图标
				boolean isOpen2 = NetWorkUtils.checkNetConnection(ctx);
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
				mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
						R.drawable.toolbar_pause_selector);
				break;
			case ACTION_STOP_SUCC:
				mVideoContainer.setWindowInfoContent("停止");
				isPlaying = false;
				mToolbar.setActionImageButtonBg(ACTION_ENUM.PLAY_PAUSE,
						R.drawable.toolbar_play_selector);
				mTimebar.setCurrentTime(convertOWSPDateTime2Calendar(firstRecordFileStarttime));
				break;
			case PAUSE_RESUME_TIMEOUT:
				dismissPlaybackReqDialog();
				showTostContent(getString(R.string.playback_netvisit_timeout));
				break;
			case UPDATE_MIDDLE_TIME:
				long timestamp = msg.getData().getLong("AUDIO_TIME");
				// Calendar c = getQueryStartTimeBase();
				// if (c != null) {
				// c.setTimeInMillis(c.getTimeInMillis()+timestamp);
				// mTimebar.setCurrentTime(c);
				// }
				Calendar c = Calendar.getInstance();
				c.set(2015, 2, 1, 0, 0, 0);
				c.setTimeInMillis(c.getTimeInMillis() + timestamp);
				mTimebar.setCurrentTime(c);

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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playback_activity);

		GlobalApplication.getInstance().setPlaybackHandler(mHandler);
		initView();
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
		ctx = PlaybackActivity.this;

		super.setTitleViewText(getString(R.string.navigation_title_remote_playback));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_search_btn_selector);
		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimebar.reset();
				Intent intent = new Intent();
				intent.setClass(ctx, TimeSettingActivity.class);
				startActivityForResult(intent, TIMESETTING_RTN_CODE);
			}
		});

		initToolbar();
		initTimebar();

		final int screenWidth = getApp().getScreenWidth();
		FrameLayout playbackVideoRegion = (FrameLayout) findViewById(R.id.playback_video_region);
		mVideoContainer = new PlaybackLiveViewItemContainer(this);
		mVideoContainer.findSubViews();
		playbackVideoRegion.addView(mVideoContainer,
				new FrameLayout.LayoutParams(screenWidth, screenWidth));

		mShotPictureAnim = AnimationUtils.loadAnimation(PlaybackActivity.this,
				R.anim.shot_picture);
	}

	/** 设置新的时间显示条 **/
	private void setNewTimeBar(ArrayList<TLV_V_RecordInfo> list) {
		if (list != null && list.size() > 0) {
			mTimebar.reset();
			int size = list.size();
			TLV_V_RecordInfo rcd = list.get(0);
			OWSPDateTime sT = rcd.getStartTime();
			Calendar calendar = Calendar.getInstance();
			calendar.set(sT.getYear(), sT.getMonth() - 1, sT.getDay(),
					sT.getHour(), sT.getMinute(), sT.getSecond());
			mTimebar.setCurrentTime(calendar);
			for (int i = 0; i < size; i++) {
				TLV_V_RecordInfo rcdInfo = list.get(i);
				OWSPDateTime starTime = rcdInfo.getStartTime();
				OWSPDateTime endTime = rcdInfo.getEndTime();
				addRecordFileRegion(starTime, endTime);
			}
			mTimebar.updateFileRect();
		}
	}

	/**
	 * 根据录像文件起始时间添加时间工具上的录像区块信息
	 */
	private void addRecordFileRegion(OWSPDateTime startTime,
			OWSPDateTime endTime) {
		Calendar t1 = Calendar.getInstance();
		t1.set(startTime.getYear(), startTime.getMonth() - 1,
				startTime.getDay(), startTime.getHour(), startTime.getMinute());

		Calendar t2 = Calendar.getInstance();
		t2.set(endTime.getYear(), endTime.getMonth() - 1, endTime.getDay(),
				endTime.getHour(), endTime.getMinute());
		mTimebar.addFileInfo(1, t1, t2);
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
				//saveLastQueryStartTime(calendar);

				// 停止当前回放

				// 按新的查询起始时间重新发送另一个回放请求
				random(calendar);
			}
		};

		mTimebar.setTimeBarCallback(mTimeBarCallBack);
		mTimebar.reset();
		mTimebar.setCurrentTime(Calendar.getInstance());
		/*
		 * Calendar c = Calendar.getInstance(); Calendar c1 =
		 * Calendar.getInstance(); c1.add(Calendar.MINUTE, 20);
		 * c1.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH),
		 * c1.get(Calendar.DAY_OF_MONTH), c1.get(Calendar.HOUR_OF_DAY),
		 * c1.get(Calendar.MINUTE)); Calendar c2 = Calendar.getInstance();
		 * c2.add(Calendar.MINUTE, 50); c2.set(c2.get(Calendar.YEAR),
		 * c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH),
		 * c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE));
		 * mTimebar.addFileInfo(1, c1, c2);
		 * 
		 * Calendar c3 = Calendar.getInstance(); c3.add(Calendar.MINUTE, 70);
		 * c3.set(c3.get(Calendar.YEAR), c3.get(Calendar.MONTH),
		 * c3.get(Calendar.DAY_OF_MONTH), c3.get(Calendar.HOUR_OF_DAY),
		 * c3.get(Calendar.MINUTE)); Calendar c4 = Calendar.getInstance();
		 * c4.add(Calendar.MINUTE, 110); c4.set(c4.get(Calendar.YEAR),
		 * c4.get(Calendar.MONTH), c4.get(Calendar.DAY_OF_MONTH),
		 * c4.get(Calendar.HOUR_OF_DAY), c4.get(Calendar.MINUTE));
		 * mTimebar.addFileInfo(1, c3, c4);
		 * 
		 * Calendar c5 = Calendar.getInstance();
		 * 
		 * c5.add(Calendar.MINUTE, 130); c5.set(c5.get(Calendar.YEAR),
		 * c5.get(Calendar.MONTH), c5.get(Calendar.DAY_OF_MONTH),
		 * c5.get(Calendar.HOUR_OF_DAY), c5.get(Calendar.MINUTE)); Calendar c6 =
		 * Calendar.getInstance(); c6.add(Calendar.MINUTE, 200);
		 * c6.set(c6.get(Calendar.YEAR), c6.get(Calendar.MONTH),
		 * c6.get(Calendar.DAY_OF_MONTH), c6.get(Calendar.HOUR_OF_DAY),
		 * c6.get(Calendar.MINUTE)); mTimebar.addFileInfo(1, c5, c6);
		 * 
		 * Calendar c7 = Calendar.getInstance(); c7.add(Calendar.MINUTE, 220);
		 * c7.set(c7.get(Calendar.YEAR), c7.get(Calendar.MONTH),
		 * c7.get(Calendar.DAY_OF_MONTH), c7.get(Calendar.HOUR_OF_DAY),
		 * c7.get(Calendar.MINUTE)); Calendar c8 = Calendar.getInstance();
		 * c8.add(Calendar.MINUTE, 260); c8.set(c8.get(Calendar.YEAR),
		 * c8.get(Calendar.MONTH), c8.get(Calendar.DAY_OF_MONTH),
		 * c8.get(Calendar.HOUR_OF_DAY), c8.get(Calendar.MINUTE));
		 * mTimebar.addFileInfo(1, c7, c8);
		 * 
		 * mTimebar.reset();
		 */
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
			default:
			case STOP:
				stop();
				break;
			}
		}
	};

	@SuppressWarnings("deprecation")
	private void playOrPause(OWSPDateTime startTime) {
		boolean isOpen = NetWorkUtils.checkNetConnection(ctx);
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

	private void processVideoRecord() {
		Log.i(TAG, "processVideoRecord");
		if (!isPlaying) {
			bVideoRecordPressed = false;
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, false);
			return;
		}

		bVideoRecordPressed = !bVideoRecordPressed;

		if (bVideoRecordPressed) { // 开启录像
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, true);
			mVideoContainer.startMP4Record();
		} else { // 关闭录像
			mToolbar.setActionImageButtonSelected(
					Toolbar.ACTION_ENUM.VIDEO_RECORD, false);
			mVideoContainer.stopMP4Record();
		}
	}

	private void showTostContent(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
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
		mVideoContainer.setWindowInfoContent("请求停止中...");
		action = PlaybackControlAction.STOP;
		pbcTask.stop();
	}
	
	protected void random(Calendar calendar) {
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		Log.i(TAG, "random play, time:" + year + "-" + month + "-" + day + " "
				+ hour + ":" + minute + ":" + second);

		final OWSPDateTime startTime = new OWSPDateTime();
		startTime.setDay(1);
		startTime.setYear(2015);
		startTime.setMonth(3);
		startTime.setHour(13);
		startTime.setMinute(58);
		startTime.setSecond(0);
		
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

			isFirstIn = false;
//			isOnPlayControl = false;

			PlaybackDeviceItem item = new PlaybackDeviceItem();
			item.setDeviceRecordName("test");
			item.setChannel(3);
			mVideoContainer.setPlaybackItem(item);
			mVideoContainer.setDeviceRecordName(item.getDeviceRecordName());

			testStartPlayTask(srr, item);

			/*
			 * REAL CODE
			 */
			// if (data != null) {
			// isFirstIn = false;
			// isOnPlayControl = false;
			// Bundle bundle = data.getExtras();
			// srr = (TLV_V_SearchRecordRequest) bundle.getParcelable("srr");
			// loginItem = bundle.getParcelable("loginItem");
			// mVideoContainer.setPlaybackItem(loginItem);
			// if (loginItem != null) {
			// startPlayTaskWithLoginItem(srr, loginItem);
			// }else{
			// testStartPlayTask(srr, loginItem);
			// }
			// }
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
		pbcTask = new PlaybackControllTask(ctx, mHandler, pr);
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
		// //
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
		stTime.setMinute(50);
		stTime.setSecond(0);
		srr.setStartTime(stTime);

		OWSPDateTime endTime = new OWSPDateTime();
		endTime.setYear(2015 - 2009);
		endTime.setMonth(3);
		endTime.setDay(2);
		endTime.setHour(14);
		endTime.setMinute(10);
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
		pbcTask = new PlaybackControllTask(ctx, mHandler, pr);
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
	}

	private void closeRemoteSocket() {
		if (pbcTask != null) {
			pbcTask.exit();
		}
	}

	public Handler getHandler() {
		return mHandler;
	}
}
