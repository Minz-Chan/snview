package com.starnet.snview.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:主要封装，有关IP地址，Port端口号的判断...
 */
public class IPAndPortUtils {

	/**
	 * 判断是否是IP地址；
	 * 
	 * @param ipString
	 *            ：判断内容
	 * @return ：是IP地址，返回true，如果不是，则返回FALSE；
	 */
	public boolean isIPAddress(String addr) {
		boolean isIP = false;
		if (addr.length() < 7 || addr.length() > 15 || addr.equals("")) {
			isIP = false;
			return isIP;
		}
		/**
		 * 判断IP格式和范围
		 */
		String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		Pattern pat = Pattern.compile(rexp);
		Matcher mat = pat.matcher(addr);
		isIP = mat.find();
		return isIP;
	}

	/**
	 * 判断是否是网络端口号；
	 * 
	 * @param portString
	 *            ：判断内容
	 * @return ：是网络端口号，返回true，如果不是，则返回FALSE；
	 */
	public boolean isNetPort(String portString) {
		boolean isNetPort = false;
		int len = portString.length();
		if ((len > 5) || (len == 0)) {
			isNetPort = true;
			return isNetPort;
		} else {
			int port = Integer.valueOf(portString);
			if ((port < 65535) && (port >= 0)) {
				isNetPort = true;
				return isNetPort;
			} else {
				return isNetPort;
			}
		}
	}
}