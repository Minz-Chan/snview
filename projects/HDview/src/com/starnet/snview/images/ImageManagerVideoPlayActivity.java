package com.starnet.snview.images;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import com.starnet.snview.R;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.images.utils.TimeManager;
import com.starnet.snview.util.BitmapUtils;
import com.starnet.snview.util.SDCardUtils;

public class ImageManagerVideoPlayActivity extends Activity implements
		OnClickListener, OnBufferingUpdateListener, OnCompletionListener,
		OnPreparedListener,OnErrorListener {
	
	private final String TAG = "ImageManagerVideoPlayActivity";
	private TextView show_num_sum_text;
	
	
	private Button local_play_back_btn ;////左上角返回按钮
	private ImageButton surfaceview_btn;//surfaceView上的按钮...

	private Intent intent;
	private String video_path;// 以后要使用的实时传过来的路径...

	private String temp_video_path = "/mnt/sdcard/video_test.mp4";;// 暂时使用，是个绝对路径，死路径....以后要使用的实时传过来的路径...
	private MyCallBack myCallBack;
	private MediaPlayer mMediaPlayer;
	private SurfaceView mSurfaceView;// 视屏播放控件
	private SurfaceHolder surfaceHolder;
	private ImageButton play_btn;// 播放、停止按钮...
	private ImageButton soud_btn;// 声音按钮,控制静音或者播放音量...
	private ImageButton pict_btn;// 拍照按钮...
	private AudioManager mAudioManager;// 安卓声音管理器....
	private TextView localplay_tim_text;//显示当前的播放时间...
	private TextView localplay_sum_text;//显示当前文件的播放总时间...
	private SeekBar show_play_seekBar;//显示播放进度条...
	private MyOnSeekBarChange mSeekBarChange;
//	private int tempVolume;

	private static int touch_time = 1;// 令布局消失的次数控制...
	private View show_play_info_layout;// 顶端布局消失时
	private View show_play_ctrl_layout;// 控制播放的布局
	private View show_play_info_prgrss;// 显示播放进度信息的布局

	private static int play_click_time = 0;
	private static int soud_click_time = 1;

	private enum Play_state {
		NONE, PLAY, PAUSE
	};// 播放状态，PLAY：表示可以播放；PAUSE：表示暂停；

	private enum Soud_state {
		NONE, SOUND, UNSOUND
	};// 声音状态，SOUND：表示有声音；UNSOUND：表示无声音；

	private static Soud_state soud_state = Soud_state.NONE;// 开始时，无任何状态
	private static Play_state play_state = Play_state.NONE;// 开始时，无任何状态
	
	private Timer mTimer = new Timer();
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int duration = mMediaPlayer.getDuration();//时间总长度...
			int position = mMediaPlayer.getCurrentPosition();  //当前的位置...
            if (duration > 0) {  
                long pos = show_play_seekBar.getMax() * position / duration;  
                show_play_seekBar.setProgress((int) pos);  //显示进度条位置...
                String cur_time = TimeManager.caculateCurrentTime(position);
                localplay_tim_text.setText(cur_time);
            }
		}
	};
	
	
	/*通过定时器和Handler来更新进度条 */
	TimerTask mTimerTask = new TimerTask() {
        @Override  
        public void run() {  
            if(mMediaPlayer==null)  {
            	mTimerTask.cancel();
            	return;  
            }                
            if (mMediaPlayer.isPlaying() && (!show_play_seekBar.isPressed())){
            	mHandler.sendEmptyMessage(0);
            }  
        }  
    };  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_manager_localplay_activity);
		mMediaPlayer = new MediaPlayer();
		myCallBack = new MyCallBack();
		mSeekBarChange = new MyOnSeekBarChange();

		int cur_postion = 0;
		int showSum = 0;
		intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				cur_postion = bundle.getInt("cur_postion");
				showSum = bundle.getInt("showSum");
			}
		}
		video_path = intent.getStringExtra("imagePath");
		show_num_sum_text = (TextView) findViewById(R.id.show_video_num_sum);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.localplay_surfaceview);
		play_btn = (ImageButton) findViewById(R.id.media_player_start);
		pict_btn = (ImageButton) findViewById(R.id.localplay_pict_btn);
		soud_btn = (ImageButton) findViewById(R.id.localplay_sound_btn);
		
		localplay_sum_text = (TextView) findViewById(R.id.localplay_sum_time_text);
		localplay_tim_text = (TextView) findViewById(R.id.localplay_time_text);;
		
		show_play_info_layout = findViewById(R.id.show_play_info_layout);
		show_play_ctrl_layout = findViewById(R.id.show_play_ctrl_layout);
		show_play_info_prgrss = findViewById(R.id.show_play_info_progress);
		surfaceview_btn = (ImageButton) findViewById(R.id.surfaceview_btn);
		
		local_play_back_btn = (Button) findViewById(R.id.local_play_back_btn);//左上角返回按钮

		surfaceHolder = mSurfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(myCallBack);//用于控制界面加载的...
		
		show_num_sum_text.setText("("+cur_postion+"/"+showSum+")");
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
//		tempVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		show_play_seekBar = (SeekBar) findViewById(R.id.localplay_progressbar);
		show_play_seekBar.setOnSeekBarChangeListener(mSeekBarChange);
		
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		play_btn.setOnClickListener(this);
		pict_btn.setOnClickListener(this);
		soud_btn.setOnClickListener(this);
		local_play_back_btn.setOnClickListener(this);
		surfaceview_btn.setOnClickListener(this);
		
		mMediaPlayer.setOnPreparedListener(this);//注册准备监听器...
		mMediaPlayer.setOnBufferingUpdateListener(this);//注册更新监听器...
		mMediaPlayer.setOnCompletionListener(this);//注册播放完毕监听器...
		mMediaPlayer.setOnErrorListener(this);//注册错误监听器...
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_time++;
			break;
		case MotionEvent.ACTION_UP:
			if (((touch_time % 2) == 0)) {
				show_play_info_layout.setVisibility(View.GONE);
				show_play_ctrl_layout.setVisibility(View.GONE);
				show_play_info_prgrss.setVisibility(View.GONE);
				if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
					surfaceview_btn.setVisibility(View.VISIBLE);
				}
			} else {
				show_play_info_layout.setVisibility(View.VISIBLE);
				show_play_ctrl_layout.setVisibility(View.VISIBLE);
				show_play_info_prgrss.setVisibility(View.VISIBLE);
				if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
					surfaceview_btn.setVisibility(View.VISIBLE);
				}
			}
			break;
		}
		return true;
	}

	private class MyOnSeekBarChange implements OnSeekBarChangeListener{
		 int max_progress = 100;  
		 int user_progress = 0 ;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
			Log.i(TAG, "onProgressChanged ... progress :"+progress);//查看progress的值
			//获取时间显示...
			if (fromUser) {//如果是用户改变的滑动条，则设置条到滑动位置;同时，令计时器记录新的时间
				user_progress = progress;
				show_play_seekBar.setProgress(progress);
				max_progress = seekBar.getMax();
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {//通知用户已经开始一个触摸拖动手势
			Log.i(TAG, "onStartTrackingTouch ... ");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {//通知用户触摸手势已经结束
			Log.i(TAG, "onStopTrackingTouch ... ");
			int duration = mMediaPlayer.getDuration();
			String cur_time = TimeManager.caculateCurrentTime(user_progress,max_progress,duration);
			localplay_tim_text.setText(cur_time);
			int msec = TimeManager.caculateCurProgress(user_progress,max_progress,duration);
			mMediaPlayer.seekTo(msec);
		}
	}
	private class MyCallBack implements Callback {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(TAG, "surfaceCreated ...");// 创建surface...
			try {
				mMediaPlayer.setDataSource(temp_video_path);// 设置录像的播放路径...
				mMediaPlayer.setDisplay(holder);
				mMediaPlayer.prepare();//同步调用...
			} catch (Exception e) {
				e.printStackTrace();
				Log.v(TAG, e.toString());
				Log.v(TAG, e.toString(), e);
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {// surface改变...
			Log.i(TAG, "surfaceChanged ...");
			int duration = mMediaPlayer.getDuration();
			if(duration > 0 ){
				//获取小时；
				String show_time = TimeManager.caculateCurrentTime(duration);
				localplay_sum_text.setText(show_time);
			}else {
				localplay_sum_text.setText("00:00:00");
			}
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {// surface销毁...
			Log.i(TAG, "surfaceDestroyed ...");
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
					mMediaPlayer.release();
					mMediaPlayer = null;
				}
			}
		}
	}
	
	private int THUMBNAIL_HEIGHT = 200;
	
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.surfaceview_btn://点击surfaceView上的按钮...
			soud_click_time++;
			if (mMediaPlayer != null) {
				if (!mMediaPlayer.isPlaying()) {
					play_btn.setBackgroundResource(R.drawable.image_pause_btn);
					surfaceview_btn.setVisibility(View.GONE);
					mMediaPlayer.start();
				}
			}
			Log.v(TAG, "soud_click_time-->"+soud_click_time);
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
			if (play_state == Play_state.PLAY) {//开始播放....
				play_btn.setBackgroundResource(R.drawable.image_pause_btn);
				//播放时，判断判断MediaPlayer是否准备完全；若是已经准备好，则开始播放，并加载进度条的显示；若没有准备好，则重头加载，并将进度条的位置置为开始处；
				//进度条根据当前位置显示...
				surfaceview_btn.setVisibility(View.GONE);
				try {
					mMediaPlayer.start();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			} else {//表示暂停...
				play_btn.setBackgroundResource(R.drawable.image_play_btn);
				if (mMediaPlayer.isPlaying()) {
					surfaceview_btn.setVisibility(View.VISIBLE);
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
				soud_btn.setBackgroundResource(R.drawable.image_sound_on_btn);
				mMediaPlayer.setVolume(0.5f, 0.5f);
//				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,tempVolume, 0);// 声音开启... 
			} else {
				soud_btn.setBackgroundResource(R.drawable.image_sound_off_btn);
				mMediaPlayer.setVolume(0, 0);
//				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0, 0);// 声音关闭...
			}
			break;
			
		case R.id.localplay_pict_btn://拍照操作...在拍照保存文件的时候，必须要检查存储空间是否能够存储使用
			
			//首先判定，截图拍照的时候是在什么状态之下进行的，若是在暂停状态下保存的，拍照完毕后
			//继续保持暂停状态；如果是播放状态之下进行的，则拍照照片后继续播放；直接获取surfaceView的视图，进行拍照；
			
			if (SDCardUtils.IS_MOUNTED) {
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				retriever.setDataSource(temp_video_path);//资源路径
				String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				long time = Long.parseLong(timeString) * 1000;
				long currentPostion = (time * show_play_seekBar.getProgress())/show_play_seekBar.getMax();//通过这个计算出想截取的画面所在的时间
	            Bitmap bitmap = retriever.getFrameAtTime(currentPostion);// 按当前播放位置选择帧
	            int thumbnailHeight = THUMBNAIL_HEIGHT;
				int thumbnailWidth = THUMBNAIL_HEIGHT * bitmap.getWidth() / bitmap.getHeight();
	            Bitmap thumbnail = BitmapUtils.extractMiniThumb(bitmap, thumbnailWidth, thumbnailHeight, false);
	            //保存文件...获取当前的时间...
	            String fileName = LocalFileUtils.getFormatedFileName("paizhao_测试",1);//前面的名字改如何命名。。。
	            String fullImgPath = LocalFileUtils.getCaptureFileFullPath(fileName, true);
				String fullThumbImgPath = LocalFileUtils.getThumbnailsFileFullPath(fileName, true);;
	        	
				BitmapUtils.saveBmpFile(bitmap, fullImgPath);
				BitmapUtils.saveBmpFile(thumbnail, fullThumbImgPath);	
				SnapshotSound s = new SnapshotSound(ImageManagerVideoPlayActivity.this);
				s.playSound();
				retriever.release();//释放资源...
			}
			
			
			break;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.v(TAG, " onBufferingUpdate ... " );
	}

	@Override
	public void onCompletion(MediaPlayer mp) {//监听是否播放完毕完毕...
		Log.v(TAG, " onBufferingUpdate ... " );
		play_btn.setBackgroundResource(R.drawable.image_play_btn);
		soud_click_time++;
		surfaceview_btn.setVisibility(View.VISIBLE);
		ImageManagerVideoPlayActivity.this.finish();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {//监听是否准备完毕...
		Log.v(TAG, " onPrepared ... " );
		try {
			surfaceview_btn.setVisibility(View.GONE);
			mMediaPlayer.start();
			play_btn.setBackgroundResource(R.drawable.image_pause_btn);
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
		Log.v(TAG, "mp:"+mp.toString()+"-->what:"+what+"-->extra"+extra);
		return false;
	}
}