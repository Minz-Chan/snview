package com.starnet.snview.util;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.starnet.snview.alarmmanager.AlarmDevice;

@SuppressLint("SdCardPath")
public class ReadWriteXmlUtils {
	private static final String TAG = "AlarmPersistenceUtils";
	
	public final static String ALARMS_PERSISTANCE_PATH = "/data/data/com.starnet.snview/ALARMS_PERSISTANCE_FILE.xml";

	private static String INDENT = "    ";
	private static String CHARSET = "UTF-8";
	/**
	 * 持久化报警信息到XML文件中
	 * @param alarm 报警信息
	 * @return true, 成功；false, 失败 
	 */
	public static boolean writeAlarm(AlarmDevice alarm) {
		if (alarm == null) {
			return false;
		}
		
		boolean result = true;
		File f = new File(ALARMS_PERSISTANCE_PATH);
		Document doc = null;
		FileWriter fw = null;
		OutputFormat format = null;
		XMLWriter xmlOuter = null;
		try {
			boolean isFileExist =  f.exists();
			if (!isFileExist) {
				if (f.createNewFile()) {
					
				} else { // 无写入权限或空间不足
					
				}
			} 			
			
			if (isFileExist) {
				doc = (new SAXReader()).read(f);
			} else {
				doc = DocumentHelper.createDocument();
				doc.addElement("AlarmDevices");
			}
			
			Element root = doc.getRootElement();
			Element item = root.addElement("AlarmDevice");
			item.addAttribute("content",alarm.getAlarmContent());
			item.addAttribute("time", alarm.getAlarmTime());
			item.addAttribute("type", alarm.getAlarmType());
			item.addAttribute("deviceName",alarm.getDeviceName());
			item.addAttribute("imageUrl", alarm.getImageUrl());
			item.addAttribute("imgIp", alarm.getIp());
			item.addAttribute("password", alarm.getPassword());
			item.addAttribute("userName", alarm.getUserName());
			item.addAttribute("channel",String.valueOf(alarm.getChannel()));
			item.addAttribute("port",String.valueOf(alarm.getPort()));
			
			fw = new FileWriter(ALARMS_PERSISTANCE_PATH, false); // false to overwrite file
			format = new OutputFormat(INDENT, true, CHARSET);
			xmlOuter = new XMLWriter(fw, format);
			
			xmlOuter.write(doc);
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		} finally {
			if (xmlOuter != null) {
				try {
					xmlOuter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 从XML文档中持久化的报警信息
	 * @return 报警信息的信息列表
	 */
	public static List<AlarmDevice> readAlarms() {
		List<AlarmDevice> alarmList = new ArrayList<AlarmDevice>();
		
		return alarmList;
	}
}