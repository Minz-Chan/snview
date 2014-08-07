package com.starnet.snview.util;

public class CommonUtils {
    private static long lastClickTime;
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 400) {   
            return true;   
        }   
        lastClickTime = time;   
        return false;   
    }
}
