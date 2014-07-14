package com.starnet.snview.channelmanager.xml;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * @function     功能	          同步效果辅助实现类
 * 此类用于实现同步功能：
 *     suspend() 挂起等待通知方可继续往下执行；
 *     resume()  通知程序往下执行
 * @author       创建人                陈明珍
 * @date        创建日期           2013-10-06
 * @author       修改人                陈明珍
 * @date        修改日期           2013-10-06
 * @description 修改说明	 
 *   2013-10-06 创建此文件 陈明珍
 */
public class SynObject{

	private Handler mHandler;
    protected Object result;
    
    
    
    public static final int STATUS_SUSPEND = 0x3301;
    public static final int STATUS_RUN = 0x3302;
    
    private int status = STATUS_RUN;
	
	public SynObject() {}


	public void resume() {
		status = STATUS_RUN;
		mHandler.sendEmptyMessage(0);
	}
	
	static class SynHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
                throw new RuntimeException();
        }
	}


	public void suspend() {
		try {
			status = STATUS_SUSPEND;
	        Looper.getMainLooper();
	        mHandler = new SynHandler();
	        Looper.loop();
		} catch (Exception e) {
			
		}
	}


	public int getStatus() {
		return status;
	}
}
