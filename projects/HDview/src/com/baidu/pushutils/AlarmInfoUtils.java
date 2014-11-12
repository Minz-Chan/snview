package com.baidu.pushutils;

import java.io.File;
import java.io.FileWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class AlarmInfoUtils {
	
	public static boolean flag_start = false;
	public final String message_file_path = "/data/data/com.starnet.snview/alarm_message_info.xml";
	
	//用于保存报警信息，将报警信息写入到xml文档中
	public boolean writeAlarmInfoToXML(AlarmDeviceInfo alarm){
		boolean writeOver = false;
		File file = new File(message_file_path);
		try {
			if (!file.exists()) {// 如果文件不存在，则新建立一个文件，并添加根节点；
				file.createNewFile();
				Document document1 = DocumentHelper.createDocument();
				document1.addElement("cloudAccounts");
				OutputFormat format = new OutputFormat("    ", true, "UTF-8");
				FileWriter fw = new FileWriter(message_file_path);
				XMLWriter writer = new XMLWriter(fw, format);
				writer.write(document1);
				fw.close();
				System.out.println("Generate Over!");
			}
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			
			
			
			
			
		} catch (Exception e) {
			System.out.println("Wrong!");
			e.printStackTrace();
		}
		return writeOver;
	}
}
