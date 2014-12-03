package com.starnet.snview.util;

import java.io.UnsupportedEncodingException;

import android.util.Base64;


public class Base64Util {
	public static final String CHARSET = "UTF-8";
	
    public static String encode(String s) {
    	String encoded = null;
    	try {
    		encoded = Base64.encodeToString(s.getBytes(CHARSET), Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	return encoded;
    }  
  
    public static String decode(String s) {  
    	String decoded = null;
        try {
        	decoded =  new String(Base64.decode(s.getBytes(), Base64.DEFAULT), CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}  
        
        return decoded;
    }  
}
