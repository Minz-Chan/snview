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

	private String subSpace(String IP) {// 去掉IP字符串前后所有的空格
		while (IP.startsWith(" ")) {
			IP = IP.substring(1, IP.length()).trim();
		}
		while (IP.endsWith(" ")) {
			IP = IP.substring(0, IP.length() - 1).trim();
		}
		return IP;
	}

	public boolean isIp(String IP) {// 判断是否是一个IP
		boolean b = false;
		IP = this.subSpace(IP);
		if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
			String s[] = IP.split("\\.");
			if (Integer.parseInt(s[0]) < 255)
				if (Integer.parseInt(s[1]) < 255)
					if (Integer.parseInt(s[2]) < 255)
						if (Integer.parseInt(s[3]) < 255)
							b = true;
		}
		return b;
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