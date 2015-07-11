package com.starnet.snview.alarmmanager;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.syssetting.CloudAccountAddingActivity;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

public class AlarmSettingUtils {
	public static final String ALARM_CONFIG = "ALARM_CONFIG";
	public static final String ALARM_CONFIG_GLOBAL_ALARM = "GLOBAL_ALARM_SWITCH";
	public static final String ALARM_CONFIG_SHAKE = "SHAKE_SWITCH";
	public static final String ALARM_CONFIG_SOUND = "SOUND_SWITCH";
	public static final String ALARM_CONFIG_USER_ALARM = "USER_ALARM_SWITCH";
	
	private static SharedPreferences alarmConfig;
	private static AlarmSettingUtils singleInstance;
	
	private Handler handler;	// 用于更新配置界面ui元素
	
	private boolean initalized = false;

	private AlarmSettingUtils() {
		
	}

	public static AlarmSettingUtils getInstance() {
		if (singleInstance == null) {
			singleInstance = new AlarmSettingUtils();
		}
		
		return singleInstance;
	}
	
	public void init(Context context) {
		if (context == null) {
			throw new NullPointerException("Context can't be null.");
		}
		alarmConfig = context.getSharedPreferences(ALARM_CONFIG,
				Context.MODE_PRIVATE);
		initalized = true;
	}
	
	public boolean isInitalized() {
		return initalized;
	}

	public void attachHandler(Handler h) {
		this.handler = h;
	}
	
	public boolean notifyUIChanges(Message msg) {
		if (handler != null) {
			return handler.sendMessage(msg);
		} else {
			return false;
		}
	}
	
	/*
	 * 报警推送接收开关是否开启
	 */
	public boolean isPushOpen() {
		return alarmConfig.getBoolean(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, true);
	}

	/*
	 * 声音是否开启
	 */
	public boolean isSoundOpen() {
		return alarmConfig.getBoolean(AlarmSettingUtils.ALARM_CONFIG_SOUND, true);
	}

	/*
	 * 震动是否开启
	 */
	public boolean isShakeOpen() {
		return alarmConfig.getBoolean(AlarmSettingUtils.ALARM_CONFIG_SHAKE, true);
	}

	/*
	 * 报警账户推送是否开启
	 */
	public boolean isUserAlarmOpen() {
		return alarmConfig.getBoolean(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, true);
	}
	
	/*
	 * 返回所有用户tag列表（包含已启用的星云账户tag列表和报警账户tag列表）
	 */
	public List<String> getAllUserTags() {
		List<String> tags = new ArrayList<String>();
		
		List<String> alarmUserTags = getAlarmUserTags();
		if (alarmUserTags.size() > 0) {
			for (String tag : alarmUserTags) {
				tags.add(tag);
			}
		}
		
		List<String> starUserTags = getStarnetAccountsTags();
		if (starUserTags.size() > 0) {
			for (String tag : starUserTags) {
				tags.add(tag);
			}
		}
		
		return tags;
	}

	/*
	 * 获取报警账户tag列表（tag=用户名+32位小写md5密码）,为空的时候，返回一个size=0的列表
	 */
	public List<String> getAlarmUserTags() {
		List<String> result = new ArrayList<String>();
		String tag = alarmConfig.getString("tags", "");
		if (tag != null && tag.length() > 0) {
			String[] tags = tag.split(",");
			for (int i = 0; i < tags.length; i++) {
				if (tags[i].contains("|false")&&tags[i].contains("|setTags")&&!tags[i].contains("|delTags")) {
					tags[i] = tags[i].replace("|false", "");
					tags[i] = tags[i].replace("|setTags", "");
					if (!checkTagContained(tags[i], result)) {
						result.add(tags[i]);
					}
				}
			}
		}
		// TODO mock数据，release时删除
//		result.add("minze10adc3949ba59abbe56e057f20f883e");
//		result.add("aaaae10adc3949ba59abbe56e057f20f883e");
//		result.add("bbbbe10adc3949ba59abbe56e057f20f883e");
		return result;
	}

	// 检测result列表中是否已经包含该报警账户
	private boolean checkTagContained(String tag, List<String> result) {
		boolean contain = false;
		if (result.size() == 0) {
			contain = false;
		} else {
			int size = result.size();
			for (int i = 0; i < size; i++) {
				if (result.get(i).equals(tag)) {
					contain = true;
					break;
				}
			}
		}
		return contain;
	}
	
	/*
	 * 获取星云平台账户的tag（tag=用户名+32位小写md5密码）,为空的时候，返回一个size=0的列表
	 */
	@SuppressWarnings("finally")
	public List<String> getStarnetAccountsTags(){
		List<String> result = new ArrayList<String>();
		try {
			List<CloudAccount> accounts = ReadWriteXmlUtils.getCloudAccountList(CloudAccountAddingActivity.STARUSERSFILEPATH);
			if (accounts == null || accounts.size() == 0) {
				return result;
			}
			int size = accounts.size();
			for (int i = 0; i < size; i++) {
				CloudAccount account = accounts.get(i);
				if (account.isEnabled()) {
					String userName = account.getUsername();
					String password = account.getPassword();
					password = MD5Utils.createMD5(password);
					String tag = userName + password;
					result.add(tag);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			return result;
		}
		
		// TODO mock数据，release时删除
//		result.add("stare10adc3949ba59abbe56e057f20f883e");
//		return result;
	}
	
	public void writeAlarmUserToXml(List<String> tags){
		String tempTags="";
		int size = tags.size();
		if (size == 0) {
			tempTags = "";
		}else if (size == 1) {
			tempTags = tags.get(0);// + "|false|setTags"
		}else {
			for (int i = 0;i < size - 1; i++) {
				if (i == 0) {
					tempTags = tags.get(0) + ",";
				}else{
					tempTags = tempTags + tags.get(i) + ",";
				}
			}
			tempTags = tempTags + tags.get(size-1);
		}
		alarmConfig.edit().putString("tags", tempTags).commit();
	}
}
