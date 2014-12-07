package com.starnet.snview.test;

import junit.framework.Assert;

import com.starnet.snview.util.MD5Utils;

import android.test.AndroidTestCase;
import android.util.Log;

public class MD5UtilsTester extends AndroidTestCase {
	private static final String TAG = "MD5UtilsTester";

	public void testEncrypt() throws Exception {
		Log.d(TAG, MD5Utils.createMD5("123456"));
		Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", MD5Utils.createMD5("123456"));
		Assert.assertEquals("a8011a3d19f0192622c64432b7709992", MD5Utils.createMD5("adf123456"));
		Assert.assertEquals("85dabd563f9b2ad6d9624d1ce01e2245", MD5Utils.createMD5("adbd 1234"));
		Assert.assertEquals("7a2444fa03667f5b6dcf0e49d4a58b77", MD5Utils.createMD5(" 1234adfd"));
		Assert.assertEquals("6238b409a020998a837d202f0c800f68", MD5Utils.createMD5(";'.fj12"));
	}
	
}
