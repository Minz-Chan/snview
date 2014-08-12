package com.starnet.snview.playback;

import java.util.ArrayList;
import java.util.Calendar;

import android.os.Bundle;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.component.Toolbar;
import com.starnet.snview.global.GlobalApplication;

public class PlaybackActivity extends BaseActivity {
	private static final String TAG = "PlaybackActivity";
	
	private Toolbar mToolbar;
	
	private TimeBar mTimebar;
	private TimeBar.TimePickedCallBack mTimeBarCallBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.playback_activity);
		
		setBackPressedExitEventValid(true);
		
		initView();
		
		
		
	}
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_remote_playback));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_search_btn_selector);
		
		initToolbar();
		
		initTimebar();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initToolbar() {
    	mToolbar = super.getBaseToolbar();
    	
    	ArrayList itemList = new ArrayList();
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PLAY_PAUSE, R.drawable.toolbar_play_selector));
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PICTURE, R.drawable.toolbar_take_picture_selector));
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.QUALITY, R.drawable.toolbar_quality_high_selector));
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.PTZ, R.drawable.toolbar_ptz_selector));
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.MICROPHONE, R.drawable.toolbar_microphone_stop_selector));
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.SOUND, R.drawable.toolbar_sound_off_selector));
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.VIDEO_RECORD, R.drawable.toolbar_video_record_selector));
    	itemList.add(new Toolbar.ItemData(Toolbar.ACTION_ENUM.ALARM, R.drawable.toolbar_alarm_selector));
    	
    	mToolbar.createToolbar(itemList, GlobalApplication.getInstance().getScreenWidth(), getResources().getDimensionPixelSize(R.dimen.toolbar_height));
    	
    	//this.mToolbar.setOnItemClickListener(mToolbarOnItemClickListener);
    }
	
	private void initTimebar() {
		mTimebar = (TimeBar) findViewById(R.id.timebar_control);
		
		mTimeBarCallBack = new TimeBar.TimePickedCallBack()
	    {
	      public void onTimePickedCallback(Calendar calendar)
	      {
	       
	      }
	    };
		
	    Calendar c = Calendar.getInstance(); 
	    
	    Calendar c1 = Calendar.getInstance();
	    c1.add(Calendar.MINUTE, 20);
	    c1.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), 
	    		c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE));
	    Calendar c2 = Calendar.getInstance();
	    c2.add(Calendar.MINUTE, 50);
	    c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH), 
	    		c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE));
	    mTimebar.addFileInfo(1, c1, c2);
	    
	    Calendar c3 = Calendar.getInstance();
	    c3.add(Calendar.MINUTE, 70);
	    c3.set(c3.get(Calendar.YEAR), c3.get(Calendar.MONTH), c3.get(Calendar.DAY_OF_MONTH), 
	    		c3.get(Calendar.HOUR_OF_DAY), c3.get(Calendar.MINUTE));
	    Calendar c4 = Calendar.getInstance();
	    c4.add(Calendar.MINUTE, 110);
	    c4.set(c4.get(Calendar.YEAR), c4.get(Calendar.MONTH), c4.get(Calendar.DAY_OF_MONTH), 
	    		c4.get(Calendar.HOUR_OF_DAY), c4.get(Calendar.MINUTE));
	    mTimebar.addFileInfo(1, c3, c4);
	    
	    Calendar c5 = Calendar.getInstance();
	    c5.add(Calendar.MINUTE, 130);
	    c5.set(c5.get(Calendar.YEAR), c5.get(Calendar.MONTH), c5.get(Calendar.DAY_OF_MONTH), 
	    		c5.get(Calendar.HOUR_OF_DAY), c5.get(Calendar.MINUTE));
	    Calendar c6 = Calendar.getInstance();
	    c6.add(Calendar.MINUTE, 200);
	    c6.set(c6.get(Calendar.YEAR), c6.get(Calendar.MONTH), c6.get(Calendar.DAY_OF_MONTH), 
	    		c6.get(Calendar.HOUR_OF_DAY), c6.get(Calendar.MINUTE));
	    mTimebar.addFileInfo(1, c5, c6);
	    
	    Calendar c7 = Calendar.getInstance();
	    c7.add(Calendar.MINUTE, 220);
	    c7.set(c7.get(Calendar.YEAR), c7.get(Calendar.MONTH), c7.get(Calendar.DAY_OF_MONTH), 
	    		c7.get(Calendar.HOUR_OF_DAY), c7.get(Calendar.MINUTE));
	    Calendar c8 = Calendar.getInstance();
	    c8.add(Calendar.MINUTE, 260);
	    c8.set(c8.get(Calendar.YEAR), c8.get(Calendar.MONTH), c8.get(Calendar.DAY_OF_MONTH), 
	    		c8.get(Calendar.HOUR_OF_DAY), c8.get(Calendar.MINUTE));
	    mTimebar.addFileInfo(1, c7, c8);
	}

}
