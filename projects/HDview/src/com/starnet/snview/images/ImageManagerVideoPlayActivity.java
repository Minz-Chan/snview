package com.starnet.snview.images;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.component.ToastTextView;
import com.starnet.snview.images.Image.ImageType;
import com.starnet.snview.images.utils.TimeManager;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.BitmapUtils;
import com.starnet.snview.util.SDCardUtils;

public class ImageManagerVideoPlayActivity extends Activity implements
		OnClickListener, OnBufferingUpdateListener, OnCompletionListener,
		OnPreparedListener,OnErrorListener {
	
	private final String TAG = "ImageManagerVideoPlayActivity";
	private TextView show_num_sum_text;
	
	private Button mBackBtn ;						// 左上角返回按钮
	
	private String mVideoPath;						// 实时传过来的录像路径...

	private MediaPlayer mMediaPlayer;
	private SurfaceView mSurfaceView;				// 视屏播放控件

	private ImageButton mPlayBtn;					// surfaceView上的按钮...
	private ImageButton mPlayStopBtn;				// 播放、停止按钮
	private ImageButton mSoundBtn;					// 声音按钮,控制静音或者播放音量
	private ImageButton mCaptureBtn;				// 拍照按钮
	
	private TextView mCurrentVideoTimeTxt;			// 显示当前的播放时间
	private TextView mTotalVideoTimeTxt;			// 显示当前文件的播放总时间
	private SeekBar mVideoProgressBar;				// 显示播放进度条
	
	private ArrayList<Image> mCaptureList = new ArrayList<Image>();
	
	private static int mDisapperTime = 1;			// 令布局消失的次数控制
	private View mNavigationbar;					// 顶端布局消失时
	private View mCtrPlayToolbar;					// 控制播放的布局
	private View mPlayProgressView;					// 显示播放进度信息的布局

	private static int play_click_time = 0;
	private static int soud_click_time = 1;

	private enum Play_state {						// 播放状态，PLAY：表示可以播放；PAUSE：表示暂停；
		NONE, PLAY, PAUSE
	};											

	private enum Soud_state {						// 声音状态，SOUND：表示有声音；UNSOUND：表示无声音；
		NONE, SOUND, UNSOUND
	};											

	private static Soud_state soud_state = Soud_state.NONE; // 开始时，无任何状态
	private static Play_state play_state = Play_state.NONE; // 开始时，无任何状态
	
	private Timer mTimer = new Timer();
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int duration = mMediaPlayer.getDuration();		 //时间总长度...
			int position = mMediaPlayer.getCurrentPosition();//当前的位置...
            if (duration > 0) {  
                long pos = mVideoProgressBar.getMax() * position / duration;  
                mVideoProgressBar.setProgress((int) pos);	//显示进度条位置...
                String cur_time = TimeManager.caculateCurrentTime(position);
                mCurrentVideoTimeTxt.setText(cur_time);
            }
		}
	};
	
	TimerTask mTimerTask = new TimerTask() {				//通过定时器和Handler来更新进度条 
        @Override  
        public void run() {  
            if(mMediaPlayer==null)  {
            	mTimerTask.cancel();
            	return;  
            }                
            if (mMediaPlayer.isPlaying()){
            	mHandler.sendEmptyMessage(0);
            }
        }  
    };  
    
    private int mCurrentPicture = 0;
    private int mNavigationSumTxt = 0;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_manager_localplay_activity);
		mMediaPlayer = new MediaPlayer();

		Bundle bundle = getIntent().getExtras();
		mCurrentPicture = bundle.getInt("cur_postion");
		mNavigationSumTxt = bundle.getInt("showSum");
		
		mVideoPath = getIntent().getStringExtra("video_path");
		show_num_sum_text = (TextView) findViewById(R.id.show_video_num_sum);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.localplay_surfaceview);
		mPlayStopBtn = (ImageButton) findViewById(R.id.media_player_start);
		mCaptureBtn = (ImageButton) findViewById(R.id.localplay_pict_btn);
		mSoundBtn = (ImageButton) findViewById(R.id.localplay_sound_btn);
		
		mTotalVideoTimeTxt = (TextView) findViewById(R.id.localplay_sum_time_text);
		mCurrentVideoTimeTxt = (TextView) findViewById(R.id.localplay_time_text);
		
		mNavigationbar = findViewById(R.id.show_play_info_layout);
		mCtrPlayToolbar = findViewById(R.id.show_play_ctrl_layout);
		mPlayProgressView = findViewById(R.id.show_play_info_progress);
		mPlayBtn = (ImageButton) findViewById(R.id.surfaceview_btn);
		
		mBackBtn = (Button) findViewById(R.id.local_play_back_btn);//左上角返回按钮

		mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceView.getHolder().addCallback(mSurviewCallback);//用于控制界面加载的...
		
		if (mCurrentPicture == 0) {
			mCurrentPicture++;
		}
		show_num_sum_text.setText("("+mCurrentPicture+"/"+mNavigationSumTxt+")");
		
		mVideoProgressBar = (SeekBar) findViewById(R.id.localplay_progressbar);
		mVideoProgressBar.setOnSeekBarChangeListener(mOnVideoProgressChangedListener);
		
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		mPlayStopBtn.setOnClickListener(this);
		mCaptureBtn.setOnClickListener(this);
		mSoundBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
		
		mMediaPlayer.setOnPreparedListener(this);			//注册准备监听器...
		mMediaPlayer.setOnBufferingUpdateListener(this);	//注册更新监听器...
		mMediaPlayer.setOnCompletionListener(this);			//注册播放完毕监听器...
		mMediaPlayer.setOnErrorListener(this);				//注册错误监听器...
		
		Log.i(TAG, "onCreat End");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDisapperTime++;
			break;
		case MotionEvent.ACTION_UP:
			if (((mDisapperTime % 2) == 0)) {
				mNavigationbar.setVisibility(View.GONE);
				mCtrPlayToolbar.setVisibility(View.GONE);
				mPlayProgressView.setVisibility(View.GONE);
				if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
					mPlayBtn.setVisibility(View.VISIBLE);
				}
			} else {
				mNavigationbar.setVisibility(View.VISIBLE);
				mCtrPlayToolbar.setVisibility(View.VISIBLE);
				mPlayProgressView.setVisibility(View.VISIBLE);
				if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
					mPlayBtn.setVisibility(View.VISIBLE);
				}
			}
			break;
		}
		return true;
	}
	
	
	private OnSeekBarChangeListener mOnVideoProgressChangedListener = new OnSeekBarChangeListener() {
		int max_progress = 100;
		int user_progress = 0;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Log.i(TAG, "onProgressChanged ... progress :" + progress);// 查看progress的值
			// 获取时间显示...
			if (fromUser) {									// 如果是用户改变的滑动条，则设置条到滑动位置;同时，令计时器记录新的时间
				user_progress = progress;
				mVideoProgressBar.setProgress(progress);
				max_progress = seekBar.getMax();
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {	// 通知用户已经开始一个触摸拖动手势
			Log.i(TAG, "onStartTrackingTouch ... ");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {	// 通知用户触摸手势已经结束
			Log.i(TAG, "onStopTrackingTouch ... ");
			if (mMediaPlayer != null) {
				int duration = mMediaPlayer.getDuration();
				String cur_time = TimeManager.caculateCurrentTime(
						user_progress, max_progress, duration);
				mCurrentVideoTimeTxt.setText(cur_time);
				int msec = TimeManager.caculateCurProgress(user_progress,
						max_progress, duration);
				mMediaPlayer.seekTo(msec);
			}
		}
	};
	
	private Callback mSurviewCallback = new Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {				
				mMediaPlayer.setDataSource(mVideoPath);
				mMediaPlayer.setDisplay(holder);
				mMediaPlayer.prepare();
			} catch (Exception e) {
				e.printStackTrace();
				Log.v(TAG, e.toString());
				Log.v(TAG, e.toString(), e);
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
			Log.i(TAG, "surfaceChanged ...");
			int duration = mMediaPlayer.getDuration();
			if(duration > 0 ){
				String show_time = TimeManager.caculateCurrentTime(duration);
				mTotalVideoTimeTxt.setText(show_time);
			}else {
				mTotalVideoTimeTxt.setText("00:00:00");
			}
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "surfaceDestroyed ...");
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
					mMediaPlayer.release();
					mMediaPlayer = null;
				}
			}
		}
		
	};
	
	private int THUMBNAIL_HEIGHT = 200;
	
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.surfaceview_btn:
			soud_click_time++;
			if (mMediaPlayer != null) {
				if (!mMediaPlayer.isPlaying()) {
					mPlayStopBtn.setBackgroundResource(R.drawable.image_pause_btn);
					mPlayBtn.setVisibility(View.GONE);
					mMediaPlayer.start();
				}
			}
			break;
		case R.id.local_play_back_btn:
			ImageManagerVideoPlayActivity.this.finish();
		case R.id.media_player_start:
			play_click_time++;
			if (play_click_time % 2 == 0) {
				play_state = Play_state.PLAY;
			} else {
				play_state = Play_state.PAUSE;
			}
			if (play_state == Play_state.PLAY) {
				mPlayStopBtn.setBackgroundResource(R.drawable.image_pause_btn);
				mPlayBtn.setVisibility(View.GONE);
				try {
					mMediaPlayer.start();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			} else {
				mPlayStopBtn.setBackgroundResource(R.drawable.image_play_btn);
				if (mMediaPlayer.isPlaying()) {
					mPlayBtn.setVisibility(View.VISIBLE);
					mMediaPlayer.pause();	
				}
			}
			break;
		case R.id.localplay_sound_btn:
			soud_click_time++;
			if (soud_click_time % 2 == 0) {
				soud_state = Soud_state.UNSOUND;
			} else {
				soud_state = Soud_state.SOUND;
			}
			if (soud_state == Soud_state.SOUND) {
				mSoundBtn.setBackgroundResource(R.drawable.image_sound_on_btn);
				mMediaPlayer.setVolume(0.5f, 0.5f);	// 声音开启... 
			} else {
				mSoundBtn.setBackgroundResource(R.drawable.image_sound_off_btn);
				mMediaPlayer.setVolume(0, 0);		// 声音关闭...
			}
			break;
			
		case R.id.localplay_pict_btn:				//拍照操作,在拍照保存文件的时候，必须要检查存储空间是否能够存储使用			
													//首先判定，截图拍照的时候是在什么状态之下进行的，若是在暂停状态下保存的，拍照完毕后
			
			if(Environment.getExternalStorageState()==Environment.MEDIA_UNMOUNTED){
                Toast.makeText(ImageManagerVideoPlayActivity.this, "sd卡不存在", Toast.LENGTH_SHORT).show();
                return;
            }
			if (SDCardUtils.IS_MOUNTED) {
				
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				Bitmap bitmap = null;
				
				try {
					retriever.setDataSource(mVideoPath);//资源路径
					String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
					long time = Long.parseLong(timeString) * 1000 ;
					long currentPostion = (long)((time * mVideoProgressBar.getProgress()) * 1.0 /mVideoProgressBar.getMax());//通过这个计算出想截取的画面所在的时间

					bitmap = retriever.getFrameAtTime(currentPostion,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);// 按当前播放位置选择帧	
				} catch (Exception e) {
					
				} finally {
					retriever.release();
				}
				
	            int thumbnailHeight = THUMBNAIL_HEIGHT;
				int thumbnailWidth = THUMBNAIL_HEIGHT * bitmap.getWidth() / bitmap.getHeight();
	            Bitmap thumbnail = BitmapUtils.extractMiniThumb(bitmap, thumbnailWidth, thumbnailHeight, false);
	            
	            String fileName = LocalFileUtils.getFormatedFileName("paizhao_luxiang",1);//前面的名字改如何命名???
	            String fullImgPath = LocalFileUtils.getCaptureFileFullPath(fileName, true);
				String fullThumbImgPath = LocalFileUtils.getThumbnailsFileFullPath(fileName, true);;
	        	
				BitmapUtils.saveBmpFile(bitmap, fullImgPath);
				BitmapUtils.saveBmpFile(thumbnail, fullThumbImgPath);	
				
				Calendar c = Calendar.getInstance();
				String imgDate =  String.format("%04d-%02d-%02d", c.get(Calendar.YEAR),
						c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				Image img = new Image(ImageType.PICTURE, fileName, fullImgPath, fullThumbImgPath, imgDate, System.currentTimeMillis());
				
				ImagesManager.getInstance().addImage(img);
				mCaptureList.add(img);
				
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						SnapshotSound s = new SnapshotSound(ImageManagerVideoPlayActivity.this);
						s.playSound();
					}
				}).start();
				
				// 提示保存路径
				Toast t = Toast.makeText(this, "", Toast.LENGTH_LONG);
				ToastTextView txt = new ToastTextView(this);
				txt.setText(getString(R.string.realplay_toast_take_pic) + fullImgPath);
				t.setView(txt);
				t.show();
				
				mCurrentPicture++;
				mNavigationSumTxt++;
				show_num_sum_text.setText("("+mCurrentPicture+"/"+mNavigationSumTxt+")");//改变序数\总数
				
				Intent data = new Intent();
				data.putExtra("paizhao", "paizhao");
				data.putExtra("cur_postion", mCurrentPicture);
				data.putExtra("showSum", mNavigationSumTxt);
				data.putExtra("CAPTURE_NEW_ADDED", mCaptureList);
				
				setResult(11, data);
			}
			break;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
	}

	@Override
	public void onCompletion(MediaPlayer mp) {				//监听是否播放完毕完毕
		mPlayStopBtn.setBackgroundResource(R.drawable.image_play_btn);
		soud_click_time++;
		mPlayBtn.setVisibility(View.VISIBLE);
		ImageManagerVideoPlayActivity.this.finish();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {				//监听是否准备完毕
		try {
			mPlayBtn.setVisibility(View.GONE);
			mMediaPlayer.start();
			mPlayStopBtn.setBackgroundResource(R.drawable.image_pause_btn);
			mTimer.schedule(mTimerTask, 0, 1000);
		} catch (IllegalStateException e) {
			Log.e(TAG, " onPrepared11 ... ", e);
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {		
		if (mTimerTask != null) {
			mTimerTask.cancel();
		}			
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mMediaPlayer.reset();
		return false;
	}
}
