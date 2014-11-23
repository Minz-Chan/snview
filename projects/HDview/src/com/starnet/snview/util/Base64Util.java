package com.starnet.snview.util;

import android.util.Base64;


public class Base64Util {
    public static String encode(String s) {
    	return Base64.encodeToString(s.getBytes(), Base64.DEFAULT);
    }  
  
    public static String decode(String s) {  
        return new String(Base64.decode(s.getBytes(), Base64.DEFAULT));  
    }  
}
