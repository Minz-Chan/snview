package com.starnet.snview.test;

import java.io.File;

import com.starnet.snview.util.SDCardUtils;

import android.test.AndroidTestCase;
import android.util.Log;

public class SDCardUtilsTester extends AndroidTestCase {

	private static final String TAG = "SDCardUtilsTester";

	public void testGetSDCardPath() throws Exception {
		String s = SDCardUtils.getSDCardPath();
		
		File f = new File(s + "test11");
		if (f.exists()) {
			f.delete();
		}
		
		f.createNewFile();
		
		Log.d(TAG, "SD Card Path: " + s);
	}
}
