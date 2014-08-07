package com.starnet.snview.util;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

/**
 * 工具类：短时间范围内（两次连击时间间隔不大于interval），使得多次连击事件方法体内部分代码只得到一次执行
 * @author 陈明珍
 *
 */
public class ClickEventUtil {
    private Timer delayTimer;
    private TimerTask task;
    private long interval = 500; // 连续调用有效时间间隔(ms)
    
    
    private Handler handler;
    private OnActionListener onActionListener;
    
    @SuppressLint("HandlerLeak")
	public ClickEventUtil(OnActionListener listener) {
    	if (listener == null) {
    		throw new IllegalArgumentException("OnActionListener can't be null");
    	}
    	
    	this.onActionListener = listener;
    	
    	// 点击事件结束后的事件处理
    	handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	ClickEventUtil.this.onActionListener.OnAction();
                delayTimer.cancel();
                super.handleMessage(msg);
            }
        };
    }
    
    /**
     * 调用此方法确认操作是否结束。若用户操作结束（即超出连击时间间隔internal），则执行预定task；
     * 否则，继续等待一个时间间隔（interval），直到用户连击间隔超过interval，task被执行
     */
    public void makeContinuousClickCalledOnce() {
        delay();  // 延迟，用于判断用户的点击操作是否结束
    }
 

    /**
     * 若两次或多次调用delay()符合连击的有效时间范围，则延缓task的执行；
     * 即使得原本在连续短时间多次调用的情况下，task的多次执行变为只执行一次。
     */
    private void delay() {
        if (task != null)
            task.cancel();
 
        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                handler.sendMessage(message);
            }
        };
        delayTimer = new Timer();
        delayTimer.schedule(task, interval);
    }
    
    public static interface OnActionListener {
    	public void OnAction();
    }
    
}
