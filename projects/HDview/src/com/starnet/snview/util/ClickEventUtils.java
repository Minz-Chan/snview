package com.starnet.snview.util;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;


public class ClickEventUtils {
	private Timer delayTimer;
    private TimerTask task;
    private long interval = 300; // 连续调用有效时间间隔(ms)
    
    private long lasttime = 0;
    private int lastIdentifier = -1;
    private int clickCount = 0;
    
    
    
    private Object[] params;
    
    private Handler handler;
    private OnActionListener onActionListener;
    
    @SuppressLint("HandlerLeak")
	public ClickEventUtils(OnActionListener listener) {
    	if (listener == null) {
    		throw new IllegalArgumentException("OnActionListener can't be null");
    	}
    	
    	this.onActionListener = listener;
    	
    	// 点击事件结束后的事件处理
    	handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	ClickEventUtils.this.onActionListener.OnAction(clickCount, params);
                delayTimer.cancel();
                clickCount = 0;
                super.handleMessage(msg);
            }
        };
    }
    
    public ClickEventUtils(OnActionListener listener, long interval) {
  	   	this(listener);
  	   	
    	this.interval = interval;
    }
    
    public void setInterval(long interval) {
    	this.interval = interval;
    }
    
    /**
     * 调用此方法确认操作是否结束。若用户操作结束（即超出连击时间间隔internal），则执行预定task；
     * 否则，继续等待一个时间间隔（interval），直到用户连击间隔超过interval，task被执行
     * @param identifier 确保调用来自同一事件源
     * @param params 延缓执行的函数附带的参数
     */
    public void makeContinuousClickCalledOnce(int identifier, Object... params) {
    	this.params = params;
    	
    	long now = System.currentTimeMillis();
    	
    	if (now - lasttime <= interval && identifier == lastIdentifier) {
    		clickCount++;
    	} else {
    		clickCount = 1;
    	}
    	
        delay();  // 延迟，用于判断用户的点击操作是否结束
        lasttime = now;
        lastIdentifier = identifier;
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
    
    public static final int CLICK = 1;
    public static final int DOUBLE_CLICK = 2;
    
    public static interface OnActionListener {
    	public void OnAction(int clickCount, Object... params);
    }
}
