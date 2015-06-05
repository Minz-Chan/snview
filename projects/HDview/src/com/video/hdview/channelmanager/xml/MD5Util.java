package com.video.hdview.channelmanager.xml;

import java.security.MessageDigest;

/**
 * @function     功能	  进行MD5加密
 * @author       创建人                肖远东
 * @date        创建日期           2013-04-23
 * @author       修改人                 肖远东
 * @date        修改日期           2013-04-23
 * @description 修改说明	             首次增加
 */
public class MD5Util {
	/**
	* md5 加密
	* @param str
	* @return
	*/

	public static String md5Encode(String str)
	{
		StringBuffer buf = new StringBuffer();
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(str.getBytes());
			byte bytes[] = md5.digest();
			for(int i = 0; i < bytes.length; i++)
			{
				String s = Integer.toHexString(bytes[i] & 0xff);
				if(s.length()==1){
					buf.append("0");
				}
				buf.append(s);
			}
		}
		catch(Exception ex){
		}
		return buf.toString();
	}
}
