package com.starnet.snview.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.syssetting.CloudAccount;

public class CommonUtils {
    private static long lastClickTime;
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 100) {   
            return true;   
        }   
        lastClickTime = time;   
        return false;   
    }
    
    /** 删除百度标签 **/
	public static void delTags(Context context , CloudAccount ca) {
		try {
			List<String> tagList = new ArrayList<String>();
			String uName = ca.getUsername();
			String pswd = MD5Utils.createMD5(ca.getPassword());
			tagList.add(uName + "" + pswd);
			PushManager.delTags(context, tagList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setTags(Context context,CloudAccount ca){
		try {
			List<String> tagList = new ArrayList<String>();
			String uName = ca.getUsername();
			String pswd = MD5Utils.createMD5(ca.getPassword());
			tagList.add(uName + "" + pswd);
			PushManager.setTags(context, tagList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
