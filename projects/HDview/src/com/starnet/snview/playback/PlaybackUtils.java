package com.starnet.snview.playback;

public class PlaybackUtils {

	public static boolean isClickOk = false;
	public static boolean stateFlag = false;
	public static boolean exapandFlag = false;

	public static int[] getValidateTime(String endTime) {
		int timeData[] = new int[5];
		String[] tempData = endTime.split(" ");
		String days = tempData[0];
		String[] data = days.split("-");
		int year = Integer.valueOf(data[0]);
		int month = Integer.valueOf(data[1]);
		int day = Integer.valueOf(data[2]);

		String hourMinute = tempData[1];

		String[] hm = hourMinute.split(":");

		int hour = Integer.valueOf(hm[0]);
		int mintute = Integer.valueOf(hm[1]);

		timeData[0] = year;
		timeData[1] = month;
		timeData[2] = day;
		timeData[3] = hour;
		timeData[4] = mintute;
		return timeData;
	}

	/** 判断年份是否为闰年 **/
	public static boolean isLeapYear(int year) {
		boolean isLeapYear = false;
		if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
			isLeapYear = true;
		} else {
			isLeapYear = false;
		}
		return isLeapYear;
	}

}
