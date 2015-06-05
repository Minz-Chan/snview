package com.video.hdview.images.utils;

public class TimeManager {
	
	/**
	 * 
	 * @param cur_length:当前的进度
	 * @return
	 */
	public static String caculateCurrentTime(int cur_length){
		String time = "";
		if (cur_length == 0 ) {
			return "00:00:00";
		}
		cur_length = cur_length / 1000;
		int hour_duration = cur_length/3600 ;//计算小时
		int minute_yushu = cur_length%3600 ;
		int mint_duration = minute_yushu/60;//计算分钟
		int scnd_duration = minute_yushu%60;//计算秒
		
		String show_hour = "";
		String show_mint = "";
		String show_secd = "";
		
		if ( hour_duration < 10) {
			show_hour = "0" + hour_duration;
		}else {
			show_hour = "" + hour_duration;
		}
		if (mint_duration < 10) {
			show_mint = "0" + mint_duration;
		}else {
			show_mint = "" + mint_duration;
		}
		if(scnd_duration < 10){
			show_secd = "0" + scnd_duration;
		}else {
			show_secd = "" + scnd_duration;
		}
		
		time = show_hour +":"+show_mint+":"+show_secd;
		return time;
	}
	
	public static String caculateCurrentTime(int user_progress,int maxa_progress,int duration){
		String time = "";
		if (user_progress == 0) {
			return "00:00:00";
		}
		int cur_duration = (duration * user_progress) / maxa_progress;
		time = caculateCurrentTime(cur_duration);
		return time;
	}
	
	public static int caculateCurProgress(int user_progress,int maxa_progress,int duration){
		int msec = (duration * user_progress )/maxa_progress;
		return msec;
	}
}