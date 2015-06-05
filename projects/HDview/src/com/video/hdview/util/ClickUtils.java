package com.video.hdview.util;

public class ClickUtils {
    private static long lastClickTime;
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 200) {   //200
            return true;   
        }   
        lastClickTime = time;   
        return false;   
    }
}
