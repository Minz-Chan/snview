package com.starnet.snview.playback.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.starnet.snview.channelmanager.Channel;

@SuppressLint("SimpleDateFormat")
public class TimeSettingUtils {

	public static long getBetweenDays(String dateStart, String dateStop) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date d1 = format.parse(dateStart);
		Date d2 = format.parse(dateStop);
		long diff = d2.getTime() - d1.getTime();// 毫秒ms
		if (diff < 0) {
			return -1;
		}else if (diff == 0) {
			long dis = getBetweenMinutes(dateStart,dateStop);
			if (dis <= 0) {
				return -1;
			}else {
				return 3;
			}
		}
		long diffDays = diff / (24 * 60 * 60 * 1000);
		return diffDays;
	}
	
	public static long getBetweenHours(String dateStart, String dateStop) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date d1 = format.parse(dateStart);
		Date d2 = format.parse(dateStop);
		long diff = d2.getTime() - d1.getTime();// 毫秒ms
		long diffHours = diff / (60 * 60 * 1000) % 24;
		return diffHours;
	}
	
	public static long getBetweenMinutes(String dateStart, String dateStop) throws ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date d1 = format.parse(dateStart);
		Date d2 = format.parse(dateStop);
		long diff = d2.getTime() - d1.getTime();// 毫秒ms
		long diffMinutes = diff / (60 * 1000) % 60;
		return diffMinutes;
	}
	
	public static int getScanChannel(List<Channel> chanList) {
		int no = 0;
		int size = chanList.size();
		for (int i = 0; i < size; i++) {
			Channel channel = chanList.get(i);
			if (channel.isSelected()) {
				no = i;
				break;
			}
		}
		return no;
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
	
	public static String setEndDateTime(boolean isLeaapYear,String dayTime , int yNum,int moNum,String yearNum,String monNums ){
		String newContDate = "";
		int dayTimeNum = Integer.valueOf(dayTime);
		if (isLeaapYear) {
			if (moNum == 2) {
				if (dayTimeNum > 3) {
					String data = "";
					if ((dayTimeNum - 3) < 10) {
						data = "0" + (dayTimeNum - 3);
					} else {
						data = "" + (dayTimeNum - 3);
					}
					newContDate = yearNum + "-" + monNums + "-" + data;
				} else {
					newContDate = yearNum + "-" + monNums + "-" + "01";
				}
			}
		} else {
			if (moNum == 2) {
				if (dayTimeNum > 3) {
					String data = "";
					if ((dayTimeNum - 3) < 10) {
						data = "0" + (dayTimeNum - 3);
					} else {
						data = "" + (dayTimeNum - 3);
					}
					newContDate = yearNum + "-" + monNums + "-" + data;
				} else {
					newContDate = yearNum + "-" + monNums + "-" + "01";
				}
			}
		}
		if ((moNum == 4) || (moNum == 6) || (moNum == 9)
				|| (moNum == 11)) {
			if (dayTimeNum > 3) {
				String data = "";
				if ((dayTimeNum - 3) < 10) {
					data = "0" + (dayTimeNum - 3);
				} else {
					data = "" + (dayTimeNum - 3);
				}
				newContDate = yearNum + "-" + monNums + "-" + data;
			} else {
				newContDate = yearNum + "-" + monNums + "-01";
			}
		} else if ((moNum != 2)) {
			if (dayTimeNum > 3) {
				String data = "";
				if ((dayTimeNum - 3) < 10) {
					data = "0" + (dayTimeNum - 3);
				} else {
					data = "" + (dayTimeNum - 3);
				}
				newContDate = yearNum + "-" + monNums + "-" + data;
			} else {
				newContDate = yearNum + "-" + monNums + "-01";
			}
		}
		
		return newContDate;
	}

	public static String setStartDate(boolean isLeaapYear, int dayTimeNum, String yearNum,int moNum, String monNums) {
		String newContDate = "";
		if (isLeaapYear) {
			if (moNum == 2) {
				if (dayTimeNum < 27) {
					String data = "";
					if ((dayTimeNum + 3) < 10) {
						data = "0" + (dayTimeNum + 3);
					} else {
						data = "" + (dayTimeNum + 3);
					}
					newContDate = yearNum + "-" + monNums + "-" + data;
				} else {
					newContDate = yearNum + "-" + monNums + "-" + 29;
				}
			}
		} else {
			if (moNum == 2) {
				if (dayTimeNum < 26) {
					String data = "";
					if ((dayTimeNum + 3) < 10) {
						data = "0" + (dayTimeNum + 3);
					} else {
						data = "" + (dayTimeNum + 3);
					}
					newContDate = yearNum + "-" + monNums + "-" + data;
				} else {
					newContDate = yearNum + "-" + monNums + "-" + 28;
				}
			}
		}
		if ((moNum == 4) || (moNum == 6) || (moNum == 9) || (moNum == 11)) {
			if (dayTimeNum < 28) {
				String data = "";
				if ((dayTimeNum + 3) < 10) {
					data = "0" + (dayTimeNum + 3);
				} else {
					data = "" + (dayTimeNum + 3);
				}
				newContDate = yearNum + "-" + monNums + "-" + data;
			} else {
				newContDate = yearNum + "-" + monNums + "-" + 30;
			}
		} else if ((moNum != 2)) {
			if (dayTimeNum < 29) {
				String data = "";
				if ((dayTimeNum + 3) < 10) {
					data = "0" + (dayTimeNum + 3);
				} else {
					data = "" + (dayTimeNum + 3);
				}
				newContDate = yearNum + "-" + monNums + "-" + data;
			} else {
				newContDate = yearNum + "-" + monNums + "-" + 31;
			}
		}

		return newContDate;
	}
}