package com.starnet.snview.component;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.starnet.snview.R;
import com.starnet.snview.devicemanager.DeviceViewActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.images.ImagesManagerActivity;
import com.starnet.snview.playback.PlaybackActivity;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.syssetting.SystemSettingActivity;
import com.starnet.snview.util.ActivityUtility;

public abstract class BaseActivity extends Activity {
	private static final String TAG = "BaseActivity";
	
	
	private View mSuperContentView;
	private ViewGroup mContentView;
	
	/* 导航栏 */
	private TextView mTitle;
	private Button mLeftButton;
	private Button mRightButton;
	private Button mExtendButton;
	private RelativeLayout mNavbarContainer;
	
	/* 抽屉菜单 */
	private static final String STATE_MENUDRAWER = "net.simonvt.menudrawer.samples.WindowSample.menuDrawer";
    private static final String STATE_ACTIVE_VIEW_ID = "net.simonvt.menudrawer.samples.WindowSample.activeViewId";
	private MenuDrawer mMenuDrawer;
    //private TextView mContentTextView;
    private static int mActiveViewId;
    private boolean mIsContainMenuDrawer = false;
    
    /* 工具栏 */
    private Toolbar mToolbar;
    private FrameLayout mToolbarContainer;
    private ImageView mLeftArrow;
    private ImageView mRightArrow;
    
    /* 退出对话框 */
	private boolean mIsBackPressedExitEventValid;
	
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
            mActiveViewId = savedInstanceState.getInt(STATE_ACTIVE_VIEW_ID);
        }

		mIsBackPressedExitEventValid = false;
		GlobalApplication.getInstance().setAppName(getString(R.string.app_name));
		
		mSuperContentView = getLayoutInflater().inflate(R.layout.base_activity, null);
	    super.setContentView(this.mSuperContentView);
	    findViews(this.mSuperContentView);
	    setListener();
	    
	    if (mIsContainMenuDrawer) {
	    	initMenuDrawer();
	    }
	    
		
	}
    
    public void setContentView(int layoutResid) {
    	mContentView.removeAllViews();
        ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResid, this.mContentView, true);
        onContentChanged();
    }
    
    public void setContentView(View view) {
    	mContentView.removeAllViews();
        mContentView.addView(view);
        onContentChanged();
    }
    
    public void setContentView(View view, ViewGroup.LayoutParams layoutPara)
    {
        mContentView.removeAllViews();
        mContentView.addView(view, layoutPara);
        onContentChanged();
    }
    
	private void findViews(View v) {
		mContentView = (ViewGroup) findViewById(R.id.base_content);
		
		mTitle = (TextView) findViewById(R.id.base_navigationbar_title);
		mLeftButton = (Button) findViewById(R.id.base_navigationbar_left_btn);
		mRightButton = (Button) findViewById(R.id.base_navigationbar_right_btn);
		mExtendButton = (Button) findViewById(R.id.base_navigationbar_extend_btn);
		mNavbarContainer = (RelativeLayout) findViewById(R.id.navigation_bar);
		
		mToolbar = (Toolbar) findViewById(R.id.base_toolbar);
		mToolbarContainer = (FrameLayout) findViewById(R.id.base_toolbar_container);
        mLeftArrow = (ImageView) findViewById(R.id.base_toolbar_container_arrowleft);
        mRightArrow = (ImageView) findViewById(R.id.base_toolbar_container_arrowright);
		
	}
	
	private void setListener() {
		mToolbar.setOnScrollCallBack(new Toolbar.ScrollCallBack()
        {
          public void onScroll(int scrollX, int scrollY, int offset)
          {
            mLeftArrow.getBackground().setAlpha((int)(255.0D * (1.0D * scrollX / offset)));
            mRightArrow.getBackground().setAlpha((int)(255.0D * (1.0D - 1.0D * scrollX / offset)));
          }
        });
		
		mLeftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuDrawer.openMenu();
			}
		});
	}

	protected void setBackPressedExitEventValid(boolean b) {
		this.mIsBackPressedExitEventValid = b;
	}

	protected TextView getTitleView() {
		return mTitle;
	}
	
	protected void setTitleViewText(String s) {
		if (mTitle != null) {
			mTitle.setText(s);
		}
	}

	protected Button getLeftButton() {
		return mLeftButton;
	}
	
	protected void setLeftButtonBg(int resid) {
		if (mLeftButton != null) {
			mLeftButton.setBackgroundResource(resid);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void setLeftButtonBg(Drawable background) {
		if (mLeftButton != null) {
			mLeftButton.setBackgroundDrawable(background);
		}
	}
	
	protected void hideLeftButton() {
		if (mLeftButton != null) {
			mLeftButton.setVisibility(View.INVISIBLE);
		}
	}

	protected Button getRightButton() {
		return mRightButton;
	}
	
	protected void setRightButtonBg(int resid) {
		if (mRightButton != null) {
			mRightButton.setBackgroundResource(resid);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void setRightButtonBg(Drawable background) {
		if (mRightButton != null) {
			mRightButton.setBackgroundDrawable(background);
		}
	}
	
	protected void hideRightButton() {
		if (mRightButton != null) {
			mRightButton.setVisibility(View.INVISIBLE);
		}
	}

	protected Button getExtendButton() {
		return mExtendButton;
	}
	
	protected void setExtendButtonBg(int resid) {
		if (mExtendButton != null) {
			mExtendButton.setBackgroundResource(resid);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void setExtendButtonBg(Drawable background) {
		if (mExtendButton != null) {
			mExtendButton.setBackgroundDrawable(background);
		}
	}
	
	protected void hideExtendButton() {
		if (mExtendButton != null) {
			mExtendButton.setVisibility(View.INVISIBLE);
		}
	}
	
	protected void setNavbarBg(int resid) {
		if (mNavbarContainer != null) {
			mNavbarContainer.setBackgroundResource(resid);
		}
	}
	
	protected void setNavbarBgFromColor(int color) {
		if (mNavbarContainer != null) {
			mNavbarContainer.setBackgroundColor(color);;
		}
	}
	
	private OnClickListener mOnMenudrawerItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int oldActivityViewId = mActiveViewId;
			
			mMenuDrawer.setActiveView(v);
	        //mContentTextView.setText("Active item: " + ((TextView) v).getText());
	        mMenuDrawer.closeMenu();
	        mActiveViewId = v.getId();
	        
	        switch (mActiveViewId) {
	        case R.id.menu_drawer_top:
	        	break;
	        case R.id.menu_drawer_realtime_preview:
	        	gotoRealtimePreview();
	        	break;
	        case R.id.menu_drawer_remote_playback:
	        	gotoPlayback();
	        	break;
	        case R.id.menu_drawer_device_managerment:
	        	gotoDeviceManagement();
	        	break;
	        case R.id.menu_drawer_picture_management:
	        	gotoPictureManagement();
	        	break;
	        case R.id.menu_drawer_sys_setting:
	        	gotoSystemSetting();
	        	break;
	        }
	        
	        if (oldActivityViewId != R.id.menu_drawer_realtime_preview
	        		&& mActiveViewId != R.id.menu_drawer_realtime_preview
	        		&& oldActivityViewId != mActiveViewId) {
	        	BaseActivity.this.finish();
	        }
	        
	        //BaseActivity.this.finish();
	        
		}
		
	};
	
	protected void gotoRealtimePreview() {
		Intent intent = new Intent();
        intent.setClass(BaseActivity.this, RealplayActivity.class); 
        startActivity(intent);
	}
	
	protected void gotoPictureManagement() {
		Intent intent = new Intent();
        intent.setClass(BaseActivity.this, ImagesManagerActivity.class); 
        startActivity(intent);
	}
	
	protected void gotoPlayback() {
		Intent intent = new Intent();
        intent.setClass(BaseActivity.this, PlaybackActivity.class); 
        startActivity(intent);
	}
	
	protected void gotoDeviceManagement() {
		Intent intent = new Intent();
        intent.setClass(BaseActivity.this, DeviceViewActivity.class); 
        startActivity(intent);
	}
	
	protected void gotoSystemSetting() {
		Intent intent = new Intent();
        intent.setClass(BaseActivity.this, SystemSettingActivity.class); 
        startActivity(intent);
	}
	
	@Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        if (mMenuDrawer != null) {
        	mMenuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
        }        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMenuDrawer != null) {
        	outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
        }
        
        outState.putInt(STATE_ACTIVE_VIEW_ID, mActiveViewId);
    }
    
    protected void closeMenuDrawer() {
    	if (mMenuDrawer == null) {
    		return;
    	}
    	
    	final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mMenuDrawer.toggleMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    	closeMenuDrawer();

        super.onBackPressed();
    }
    
    
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (mIsBackPressedExitEventValid) {
			if (keyCode == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				new AlertDialog.Builder(this)
						.setMessage(getString(R.string.exit_dialog_content))
						.setPositiveButton(getString(R.string.exit_dialog_ensure),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
//										android.os.Process
//												.killProcess(android.os.Process
//														.myPid());
										BaseActivity.this.finish();
									}
								}).setNegativeButton(R.string.exit_dialog_dispose, null)
						.show();
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} 
		
		return super.onKeyDown(keyCode, event);
	}

	private void initMenuDrawer() {
    	mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        //mMenuDrawer.setContentView(R.layout.base_activity); 
        mMenuDrawer.setMenuView(R.layout.menu_scrollview);
        
        int screenWidth = ActivityUtility.getScreenSize(this).x;
        mMenuDrawer.setMenuSize((int)(screenWidth * 0.6));
        mMenuDrawer.setDropShadowEnabled(false);
        
        Log.d(TAG, "screen width: " + screenWidth);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            //getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //mContentTextView = (TextView) findViewById(R.id.contentText);

        findViewById(R.id.menu_drawer_top).setOnClickListener(mOnMenudrawerItemClickListener);
        findViewById(R.id.menu_drawer_realtime_preview).setOnClickListener(mOnMenudrawerItemClickListener);
        findViewById(R.id.menu_drawer_remote_playback).setOnClickListener(mOnMenudrawerItemClickListener);
        findViewById(R.id.menu_drawer_device_managerment).setOnClickListener(mOnMenudrawerItemClickListener);
        findViewById(R.id.menu_drawer_picture_management).setOnClickListener(mOnMenudrawerItemClickListener);
        findViewById(R.id.menu_drawer_sys_setting).setOnClickListener(mOnMenudrawerItemClickListener);

        TextView activeView = (TextView) findViewById(mActiveViewId);
        if (activeView != null) {
            mMenuDrawer.setActiveView(activeView);  // 新的Activity启动时，活动项设置为上一次所选项
            //mContentTextView.setText("Active item: " + activeView.getText());
        } else {
        	mActiveViewId = R.id.menu_drawer_realtime_preview;  // 程序启动时设置【实时预览】为当前活动项
        }

        // This will animate the drawer open and closed until the user manually drags it. Usually this would only be
        // called on first launch.
        //mMenuDrawer.peekDrawer();
    }
	
	protected void setContainerMenuDrawer(boolean isContainMenuDrawer) {
		this.mIsContainMenuDrawer = isContainMenuDrawer;
	}
	
	protected void setActiveMenuId(int id) {
		this.mActiveViewId = id;
	}

	protected Toolbar getBaseToolbar() {
		return mToolbar;
	}
	
	protected void setToolbarVisiable(boolean flag) {
		if (flag) {
			mToolbarContainer.setVisibility(View.VISIBLE);
		} else {
			mToolbarContainer.setVisibility(View.GONE);
		}
	}

	protected FrameLayout getToolbarContainer() {
		return mToolbarContainer;
	}
	
	protected RelativeLayout getNavbarContainer() {
		return mNavbarContainer;
	}

}
