package com.video.hdview.util;

import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.video.hdview.util.Params;

/**
 * @author : 桥下一粒砂 chenyoca@gmail.com
 * date    : 2012-11-13
 * Activity帮助器类
 */
public final class ActivityUtility {

	/**
	 * 获取当前任务栈栈顶
	 * @param context
	 * @return
	 */
	public static String getTopActivity(Activity context)
	{
	     ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE) ;
	     List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1) ;
	         
	     if(runningTaskInfos != null)
	       return (runningTaskInfos.get(0).topActivity).toString() ;
	          else
	       return null ;
	}
	
	/**
	 * 切换全屏状态。
	 * @param activity Activity
	 * @param isFull 设置为true则全屏，否则非全屏
	 */
	public static void toggleFullScreen(Activity activity,boolean isFull){
		hideTitleBar(activity);
		Window window = activity.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		if (isFull) {
			params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			window.setAttributes(params);
			window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		} else {
			params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.setAttributes(params);
			window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}
	
	/**
	 * 设置为全屏
	 * @param activity Activity
	 */
	public static void setFullScreen(Activity activity){
		toggleFullScreen(activity,true);
	}
	
	/**
	 * 获取系统状态栏高度
	 * @param activity Activity
	 * @return 状态栏高度
	 *
	 */
	public static int getStatusBarHeight(Activity activity){
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object object = clazz.newInstance();
			Field field = clazz.getField("status_bar_height");
		    int dpHeight = Integer.parseInt(field.get(object).toString());
		    return activity.getResources().getDimensionPixelSize(dpHeight);
		} catch (Exception e1) {
		    e1.printStackTrace();
		    return 0;
		} 
	}

	/**
	 * 获取屏幕大小，单位px
	 * @param activity Activity
	 * @return 屏幕大小对象
	 */
	@SuppressWarnings("deprecation")
	public static Point getScreenSize(Activity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		size.set(display.getWidth(),display.getHeight());
		return size;
	}
	
	/**
	 * 隐藏Activity的系统默认标题栏
	 * @param activity Activity
	 */
	public static void hideTitleBar(Activity activity){
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	/**
	 * 强制设置Actiity的显示方向为垂直方向。
	 * @param activity Activity
	 */
	public static void setScreenVertical(Activity activity){
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	/**
	 * 强制设置Activity的显示方向为横向。
	 * @param activity Activity
	 */
	public static void setScreenHorizontal(Activity activity){
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}
	
	/**
	 * 隐藏软件输入法
	 * @param activity Activity
	 */
	public static void hideSoftInput(Activity activity){
	    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	/**
	 * 关闭已经显示的输入法窗口。
	 * @param context 上下文对象，一般为Activity
	 * @param focusingView 输入法所在焦点的View
	 *
	 */
	public static void closeSoftInput(Context context,View focusingView){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(focusingView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
	}
	
	/**
	 * 使UI适配输入法
	 * @param activity Activity
	 */
	public static void adjustSoftInput(Activity activity) {
		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	
	/**
	 * 跳转到某个Activity
	 * @param activity 当前Activity
	 * @param targetActivity 目标Activity
	 */
	public static void switchTo(Activity activity,Class<? extends Activity> targetActivity){
		switchTo(activity, new Intent(activity,targetActivity));
	}
	
	/**
	 * 根据给定的Intent进行Activity跳转
	 * @param activity 当前Activity
	 * @param intent 目标Activity的Intent
	 */
	public static void switchTo(Activity activity,Intent intent){
		activity.startActivity(intent);
		activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
	}
	
	/**
	 * 带参数进行Activity跳转
	 * @param activity 当前Activity
	 * @param target 目标Activity
	 * @param params 参数
	 */
	public static void switchTo(Activity activity,Class<? extends Activity> target,Params params){
		Intent intent = new Intent(activity,target);
		if( null != params ){
			for(Params.NameValue item : params.nameValueArray){
				IntentUtility.setValueToIntent(intent, item.name, item.value);
			}
		}
		switchTo(activity, intent);
	}
	
	/**
	 * 带参数和返回请求进行Activity跳转
	 * @param activity 当前Activity
	 * @param targetActivity 目标Activity
	 * @param params 参数
	 * @param requestCode Activity请求码
	 */
	public static void switchTo(Activity activity,Class<? extends Activity> targetActivity,Params params, int requestCode){
		Intent intent = new Intent(activity,targetActivity);
		if( null != params ){
			for(Params.NameValue item : params.nameValueArray){
				IntentUtility.setValueToIntent(intent, item.name, item.value);
			}
		}
		activity.startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 带返回请求进行Activity跳转
	 * @param activity 当前Activity
	 * @param targetActivity 目标Activity
	 * @param requestCode Activity请求码
	 */
	public static void switchTo(Activity activity,Class<? extends Activity> targetActivity,int requestCode){
		Intent intent = new Intent(activity,targetActivity);
		activity.startActivityForResult(intent, requestCode);
	}
	
	public interface MessageFilter{
		String filter(String msg);
	}
	public static MessageFilter msgFilter;
	
	/**
	 * 短时间显示Toast消息，并保证运行在UI线程中
	 * @param activity Activity
	 * @param message 消息内容
	 */
	public static void show(final Activity activity,final String message){
		showAtCenter(activity,message,false);
	}

    public static void showE(final Activity activity,final String message){
        showAtCenter(activity,message,true);
    }

    public static void showAtCenter(final Activity activity,final String message,final boolean center){
        final String msg = msgFilter != null ? msgFilter.filter(message) : message;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
                if(center) toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });
    }
	
	/**
	 * 长时间显示Toast消息，并保证运行在UI线程中
	 * @param activity Activity
	 * @param message 消息内容
	 */
	public static void showL(final Activity activity,final String message){
		final String msg = msgFilter != null ? msgFilter.filter(message) : message;
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	/**
	 * 以较短时间显示Toast消息
	 * @param activity Activity
	 * @param msgResID 消息资源ID
	 */
	public static void show(Activity activity,int msgResID){
		show(activity,activity.getResources().getString(msgResID));
	}

	/**
	 * 以较长时间显示Toast消息
	 * @param activity Activity
	 * @param msgResID 消息资源ID
	 */
	public static void showL(Activity activity,int msgResID){
		showL(activity,activity.getResources().getString(msgResID));
	}
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
	 * @param context 上下文，一般为Activity
	 * @param dpValue dp数据值
	 * @return px像素值
	 */
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     * @param context 上下文，一般为Activity
     * @param pxValue px像素值
     * @return dp数据值
     */
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }
}
