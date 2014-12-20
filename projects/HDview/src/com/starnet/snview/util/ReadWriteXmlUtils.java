package com.starnet.snview.util;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.starnet.snview.alarmmanager.AlarmDevice;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.channelmanager.xml.MD5Util;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

@SuppressLint("SdCardPath")
public class ReadWriteXmlUtils {
	protected static final String TAG = "AlarmPersistenceUtils";

	public final static String ALARMS_PERSISTANCE_PATH = "/data/data/com.starnet.snview/ALARMS_PERSISTANCE_FILE.xml";

	private static String INDENT = "";
	private static String CHARSET = "UTF-8";

	/**
	 * 持久化报警信息到XML文件中
	 * 
	 * @param alarm
	 *            报警信息
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
			boolean isFileExist = f.exists();
			if (!isFileExist) {
				if (f.createNewFile()) {

				} else { // 无写入权限或空间不足

				}
			}

			if (isFileExist) {
				doc = (new SAXReader()).read(f);
				doc.normalize();
			} else {
				doc = DocumentHelper.createDocument();
				doc.addElement("AlarmDevices");
			}

			Element root = doc.getRootElement();
			Element item = root.addElement("AlarmDevice");

			item.addAttribute("imgIp", alarm.getIp());
			item.addAttribute("time", alarm.getAlarmTime());
			item.addAttribute("type", alarm.getAlarmType());
			item.addAttribute("password", alarm.getPassword());
			item.addAttribute("userName", alarm.getUserName());
			item.addAttribute("imageUrl", alarm.getImageUrl());
			item.addAttribute("content", alarm.getAlarmContent());
			item.addAttribute("deviceName", alarm.getDeviceName());
			item.addAttribute("port", String.valueOf(alarm.getPort()));
			item.addAttribute("pusherDomain", alarm.getPusherDomain());
			item.addAttribute("pusherUserName", alarm.getPusherUserName());
			item.addAttribute("pusherPassword", alarm.getPusherPassword());
			item.addAttribute("channel", String.valueOf(alarm.getChannel()));
			fw = new FileWriter(ALARMS_PERSISTANCE_PATH, false);
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

	/*** 从文档中移除指定位置的的AlarmDevice ***/
	@SuppressWarnings("unchecked")
	public static void removeSpecifyAlarm(int index) {
		File file = new File(ALARMS_PERSISTANCE_PATH);
		if (!file.exists()) {
			return;
		}
		FileWriter fileWriter = null;
		XMLWriter xmlWriter = null;
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			int size = subElements.size();
			for (int i = 0; i < size; i++) {// 判空处理
				if (index == i) {
					subElements.get(i).detach();
					break;
				}
			}
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			fileWriter = new FileWriter(ALARMS_PERSISTANCE_PATH);
			xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 从文档中移除指定的AlarmDevice
	@SuppressWarnings("unchecked")
	public static void removeAlarm(AlarmDevice alarmDevice) {
		File file = new File(ALARMS_PERSISTANCE_PATH);
		if (!file.exists()) {
			return;
		}
		FileWriter fileWriter = null;
		XMLWriter xmlWriter = null;
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			String pswd = alarmDevice.getPassword();
			String user = alarmDevice.getUserName();
			String name = alarmDevice.getDeviceName();
			int size = subElements.size();
			for (int i = 0; i < size; i++) {// 判空处理
				Element subElement = subElements.get(i);
				String xname = subElement.attributeValue("deviceName");
				String xpswd = subElement.attributeValue("password");
				String xuser = subElement.attributeValue("userName");
				if ((xpswd == pswd) && (xname == name) && (xuser == user)) {//
					subElement.detach();
				}
			}
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			fileWriter = new FileWriter(ALARMS_PERSISTANCE_PATH);
			xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final int DEFAULT_ALARM_PORT = 8080;
	private static final int DEFAULT_ALARM_CHANNEL = 1;

	/**
	 * 从XML文档中持久化的报警信息
	 * 
	 * @return 报警信息的信息列表
	 */
	@SuppressWarnings("unchecked")
	public static List<AlarmDevice> readAlarms() {
		List<AlarmDevice> alarmList = new ArrayList<AlarmDevice>();
		File file = new File(ALARMS_PERSISTANCE_PATH);
		try {
			if (!file.exists()) {
				return null;
			}
			Document doc = new SAXReader().read(file);
			Element root = doc.getRootElement();
			List<Element> elements = root.elements();
			for (Element item : elements) {
				AlarmDevice alarmDevice = new AlarmDevice();
				String pUserName = item.attributeValue("pusherUserName");
				String pPassword = item.attributeValue("pusherPassword");
				String deviceName = item.attributeValue("deviceName");
				String pDomain = item.attributeValue("pusherDomain");
				String imageUrl = item.attributeValue("imageUrl");
				String password = item.attributeValue("password");
				String userName = item.attributeValue("userName");
				String content = item.attributeValue("content");
				String channel = item.attributeValue("channel");
				String time = item.attributeValue("time");
				String type = item.attributeValue("type");
				String port = item.attributeValue("port");
				String ip = item.attributeValue("imgIp");

				alarmDevice.setPusherUserName(pUserName);
				alarmDevice.setPusherPassword(pPassword);
				alarmDevice.setDeviceName(deviceName);
				alarmDevice.setAlarmContent(content);
				alarmDevice.setPusherDomain(pDomain);
				alarmDevice.setImageUrl(imageUrl);
				alarmDevice.setPassword(password);
				alarmDevice.setUserName(userName);
				alarmDevice.setAlarmTime(time);
				alarmDevice.setAlarmType(type);
				alarmDevice.setIp(ip);

				if (port != null) {
					alarmDevice.setPort(Integer.valueOf(port));
				} else {
					alarmDevice.setPort(DEFAULT_ALARM_PORT);
				}
				if (channel != null) {
					alarmDevice.setChannel(Integer.valueOf(channel));
				} else {
					alarmDevice.setPort(DEFAULT_ALARM_CHANNEL);
				}
				alarmList.add(alarmDevice);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return alarmList;
	}

	/**
	 * 
	 * @param filePath
	 *            :指定的文档路径
	 * @param cloudAccounted
	 *            :被替换用户
	 * @param cloudAccountes
	 *            :替换成的用户
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static void replaceSpecifyCloudAccount(String filePath,
			CloudAccount cloudAccounted, CloudAccount cloudAccountes)
			throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		List<Element> subElements = root.elements();
		int size = subElements.size();

		String domained = cloudAccounted.getDomain();
		String passwded = cloudAccounted.getPassword();
		String usNameed = cloudAccounted.getUsername();
		String usPorted = cloudAccounted.getPort();

		String domaines = cloudAccountes.getDomain();
		String passwdes = cloudAccountes.getPassword();
		String usNamees = cloudAccountes.getUsername();
		String usPortes = cloudAccountes.getPort();

		for (int i = 0; i < size; i++) {
			Element sE = subElements.get(i);
			String domain = sE.attributeValue("domain");
			String port = sE.attributeValue("port");
			String username = sE.attributeValue("username");
			String password = sE.attributeValue("password");

			if (domain.equals(domained) && password.equals(passwded)
					&& username.equals(usNameed) && port.equals(usPorted)) {
				sE.setAttributeValue("domain", domaines);
				sE.setAttributeValue("port", usPortes);
				sE.setAttributeValue("username", usNamees);
				sE.setAttributeValue("password", passwdes);
				boolean isEnabled = cloudAccountes.isEnabled();
				sE.setAttributeValue("isEnabled", String.valueOf(isEnabled));
				break;
			}
		}
		OutputFormat opf = new OutputFormat("", true, "UTF-8");
		FileWriter fileWriter = new FileWriter(file);
		XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
		xmlWriter.write(document);
		fileWriter.close();
	}

	/*** 替换特定位置的元素 ***/
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static void replaceSpecifyDeviceItem(String filePath, int index,
			DeviceItem item) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		List<Element> subElements = root.elements();
		int size = subElements.size();
		for (int i = 0; i < size; i++) {
			if (i == index) {
				Element sEl = subElements.get(i);
				sEl.setAttributeValue("deviceName", item.getDeviceName());
				sEl.setAttributeValue("channelNumber", item.getChannelSum());
				sEl.setAttributeValue("loginUser", item.getLoginUser());
				sEl.setAttributeValue("loginPass", item.getLoginPass());
				sEl.setAttributeValue("defaultChannel",
						String.valueOf(item.getDefaultChannel()));
				sEl.setAttributeValue("serverIP", item.getSvrIp());
				sEl.setAttributeValue("serverPort", item.getSvrPort());
				sEl.setAttributeValue("deviceType",
						String.valueOf(item.getDeviceType()));
				sEl.setAttributeValue("isSecurityProtectionOpen",
						String.valueOf(item.isSecurityProtectionOpen()));
				sEl.setAttributeValue("isExpanded",
						String.valueOf(item.isExpanded()));
				sEl.setAttributeValue("isIdentify",
						String.valueOf(item.isIdentify()));
				sEl.setAttributeValue("isConnPass",
						String.valueOf(item.isConnPass()));
				List<Element> elList = sEl.elements();
				for (int j = 0; j < elList.size(); j++) {
					elList.get(j).detach();
				}
				List<Channel> channelList = item.getChannelList();
				if (channelList != null) {
					int channelSize = channelList.size();
					for (int k = 0; k < channelSize; k++) {
						Channel h = channelList.get(k);
						Element cl = sEl.addElement("channel");
						cl.addAttribute("channelName", h.getChannelName());
						cl.addAttribute("channelNo",
								String.valueOf(h.getChannelNo()));
						cl.addAttribute("isSelected",
								String.valueOf(h.isSelected()));
					}
				}
				break;
			}
		}
		OutputFormat opf = new OutputFormat("", true, "UTF-8");
		FileWriter fileWriter = new FileWriter(file);
		XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
		xmlWriter.write(document);
		fileWriter.close();
	}

	// 一键添加"设备列表"到指定的文档中
	public static void addDeviceItemListToXML(List<DeviceItem> deviceItemList,
			String filePath) throws IOException {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
			file.createNewFile();
		} else {
			file.createNewFile();
		}
		// 创建文件抛出异常...
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("deviceItems");// 增加了一个根...
		int size = deviceItemList.size();
		for (int i = 0; i < size; i++) {
			DeviceItem dItem = deviceItemList.get(i);
			Element sEl = root.addElement("deviceItem");
			sEl.addAttribute("deviceName", dItem.getDeviceName());
			sEl.addAttribute("channelNumber", dItem.getChannelSum());
			sEl.addAttribute("loginUser", dItem.getLoginUser());
			sEl.addAttribute("loginPass", dItem.getLoginPass());

			sEl.addAttribute("defaultChannel",
					String.valueOf(dItem.getDefaultChannel()));
			sEl.addAttribute("serverIP", dItem.getSvrIp());
			sEl.addAttribute("serverPort", dItem.getSvrPort());
			sEl.addAttribute("deviceType",
					String.valueOf(dItem.getDeviceType()));
			sEl.addAttribute("isSecurityProtectionOpen",
					String.valueOf(dItem.isSecurityProtectionOpen()));
			sEl.addAttribute("isExpanded", String.valueOf(dItem.isExpanded()));
			sEl.addAttribute("isIdentify", String.valueOf(dItem.isIdentify()));
			sEl.addAttribute("isConnPass", String.valueOf(dItem.isConnPass()));
			List<Channel> channelList = dItem.getChannelList();
			if (channelList != null) {
				int channelSize = channelList.size();
				for (int k = 0; k < channelSize; k++) {
					Channel channel = channelList.get(k);
					Element chEl = sEl.addElement("channel");
					chEl.addAttribute("channelName", channel.getChannelName());
					chEl.addAttribute("channelNo",
							String.valueOf(channel.getChannelNo()));
					chEl.addAttribute("isSelected",
							String.valueOf(channel.isSelected()));
				}
			}
		}
		OutputFormat opf = new OutputFormat("", true, "UTF-8");
		FileWriter fileWriter = new FileWriter(file);
		XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
		xmlWriter.write(document);
		fileWriter.close();
	}

	// 从收藏设备文档中删除指定的设备...
	@SuppressWarnings("unchecked")
	public static void removeDeviceItemToCollectEquipmentXML(
			DeviceItem deviceItem, String filePath) throws DocumentException,
			IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		List<Element> subElements = root.elements();

		int size = subElements.size();
		for (int i = 0; i < size; i++) {
			Element subElement = subElements.get(i);

			String deviceName = subElement.attributeValue("deviceName");
			String channelNumber = subElement.attributeValue("channelNumber");
			String loginUser = subElement.attributeValue("loginUser");
			String loginPass = subElement.attributeValue("loginPass");
			// String defaultChannel =
			// subElement.attributeValue("defaultChannel");

			String serverIP = subElement.attributeValue("serverIP");
			String serverPort = subElement.attributeValue("serverPort");

			if (deviceItem.getDeviceName().equals(deviceName)
					&& (deviceItem.getChannelSum().equals(channelNumber))
					&& (deviceItem.getLoginUser().equals(loginUser))
					&& (deviceItem.getLoginPass().equals(loginPass))
					&& (deviceItem.getSvrIp().equals(serverIP))
					&& (deviceItem.getSvrPort().equals(serverPort))) {
				subElement.detach();
			}
		}

		OutputFormat opf = new OutputFormat("", true, "UTF-8");
		FileWriter fileWriter = new FileWriter(filePath);
		XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
		xmlWriter.write(document);
		fileWriter.close();
	}

	// 从指定的文档中获取内容
	@SuppressWarnings("unchecked")
	public static List<CloudAccount> getCloudAccountList(String filePath)
			throws Exception {
		List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
		File file = new File(filePath);
		if (!file.exists()) {
			return cloudAccountList;
		}
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		List<Element> subElements = root.elements();

		for (Element subElement : subElements) {
			CloudAccount cloudAccount = new CloudAccount();

			String isEnabled = subElement.attributeValue("isEnabled");
			String username = subElement.attributeValue("username");
			String password = subElement.attributeValue("password");
			String domain = subElement.attributeValue("domain");
			String port = subElement.attributeValue("port");

			cloudAccount.setEnabled(Boolean.valueOf(isEnabled));
			cloudAccount.setUsername(username);
			cloudAccount.setPassword(password);
			cloudAccount.setDeviceList(null);
			cloudAccount.setRotate(false);
			cloudAccount.setDomain(domain);
			cloudAccount.setPort(port);

			cloudAccountList.add(cloudAccount);
		}
		return cloudAccountList;
	}

	// 从指定的xml文档中获取收藏设备列表...
	@SuppressWarnings("unchecked")
	public static List<DeviceItem> getCollectDeviceListFromXML(String fileName)
			throws Exception {
		List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		SAXReader saxReader = new SAXReader();
		File file = new File(fileName);
		if (!file.exists()) {
			return deviceList;
		}
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		List<Element> subElements = root.elements();// 子目录
		int size = subElements.size();
		for (int i = 0; i < size; i++) {
			DeviceItem dItem = new DeviceItem();// 构造收藏设备
			Element subElement = subElements.get(i);

			String deviceName = subElement.attributeValue("deviceName");
			String channelSum = subElement.attributeValue("channelNumber");
			String loginUser = subElement.attributeValue("loginUser");
			String loginPass = subElement.attributeValue("loginPass");
			String defaultChannel = subElement.attributeValue("defaultChannel");
			String platusername = subElement.attributeValue("platformusername");

			String svrIp = subElement.attributeValue("serverIP");
			String svrPort = subElement.attributeValue("serverPort");
			String deviceType = subElement.attributeValue("deviceType");
			String iso = subElement.attributeValue("isSecurityProtectionOpen");
			String isExpanded = subElement.attributeValue("isExpanded");
			String isConnPass = subElement.attributeValue("isConnPass");
			String isIdentify = subElement.attributeValue("isIdentify");
			if ((iso == null) || (iso.equals(null))) {
				iso = "false";
			}
			if ((isExpanded == null) || (isExpanded.equals(null))) {
				isExpanded = "false";
			}

			if ((isIdentify == null) || (isIdentify.equals(null))) {
				isIdentify = "false";
			}

			if ((isConnPass == null) || (isConnPass.equals(null))) {
				isConnPass = "false";
			}
			dItem.setChannelSum(channelSum);
			dItem.setDeviceName(deviceName);
			dItem.setLoginUser(loginUser);
			dItem.setLoginPass(loginPass);
			dItem.setDefaultChannel(Integer.valueOf(defaultChannel));
			dItem.setSvrIp(svrIp);
			dItem.setSvrPort(svrPort);
			dItem.setSecurityProtectionOpen(Boolean.valueOf(iso));
			dItem.setExpanded(Boolean.valueOf(isExpanded));
			dItem.setDeviceType(Integer.valueOf(deviceType));
			dItem.setPlatformUsername(platusername);
			dItem.setIdentify(Boolean.valueOf(isIdentify));
			dItem.setConnPass(Boolean.valueOf(isConnPass));
			List<Channel> channelList = new ArrayList<Channel>();

			List<Element> channelElements = subElement.elements();
			if (channelElements != null) {
				int channelSize = channelElements.size();
				for (int j = 0; j < channelSize; j++) {
					Channel channel = new Channel();
					Element chEl = channelElements.get(j);
					String channelName = chEl.attributeValue("channelName");
					String channelNo = chEl.attributeValue("channelNo");
					String isSelected = chEl.attributeValue("isSelected");
					channel.setChannelName(channelName);
					channel.setChannelNo(Integer.valueOf(channelNo));
					channel.setSelected(Boolean.valueOf(isSelected));
					channelList.add(channel);
				}
			}
			dItem.setChannelList(channelList);
			deviceList.add(dItem);
		}
		return deviceList;
	}

	/**
	 * 
	 * @author zhongxu
	 * @throws Exception
	 * @Date 2014年7月26日
	 * @Description 增加新的设备到收藏设备文档中
	 */
	@SuppressWarnings("unchecked")
	public static String addNewDeviceItemToCollectEquipmentXML(
			DeviceItem dItem, String filePath) throws Exception {

		String saveResult = "";
		// 创建一个文档，并检查文档是否存在，若存在则不创建；否则，则创建；
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
			// 为文档创建一个根
			Document document = DocumentHelper.createDocument();
			document.addElement("deviceItems");
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			FileWriter fileWriter = new FileWriter(filePath);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
			fileWriter.close();
		}

		// 增加内容
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		Element root = document.getRootElement();

		List<Element> subElements = root.elements();
		judgeSubElementsContainsDeviceItem(subElements, dItem);
		// 检查是否包含相同的元素，如果包含的话，则先删除，后添加；如果不包含，则添加；
		Element sEle = root.addElement("deviceItem");
		sEle.addAttribute("deviceName", dItem.getDeviceName());
		sEle.addAttribute("channelNumber", dItem.getChannelSum());
		sEle.addAttribute("loginUser", dItem.getLoginUser());
		sEle.addAttribute("loginPass", dItem.getLoginPass());
		sEle.addAttribute("platformusername", dItem.getPlatformUsername());

		sEle.addAttribute("defaultChannel",
				String.valueOf(dItem.getDefaultChannel()));
		sEle.addAttribute("serverIP", dItem.getSvrIp());
		sEle.addAttribute("serverPort", dItem.getSvrPort());
		sEle.addAttribute("deviceType", String.valueOf(dItem.getDeviceType()));
		sEle.addAttribute("isSecurityProtectionOpen",
				String.valueOf(dItem.isSecurityProtectionOpen()));
		sEle.addAttribute("isExpanded", String.valueOf(dItem.isExpanded()));
		sEle.addAttribute("isIdentify", String.valueOf(dItem.isIdentify()));
		sEle.addAttribute("isConnPass", String.valueOf(dItem.isConnPass()));
		List<Channel> channelList = dItem.getChannelList();
		if (channelList != null) {
			int channelSize = channelList.size();
			for (int k = 0; k < channelSize; k++) {
				Channel ch = channelList.get(k);
				Element chE = sEle.addElement("channel");
				chE.addAttribute("channelName", ch.getChannelName());
				chE.addAttribute("channelNo", String.valueOf(ch.getChannelNo()));
				chE.addAttribute("isSelected", String.valueOf(ch.isSelected()));
			}
		}

		OutputFormat opf = new OutputFormat("", true, "UTF-8");
		FileWriter fileWriter = new FileWriter(filePath);
		XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
		xmlWriter.write(document);
		fileWriter.close();
		saveResult = "saving success...";
		return saveResult;
	}

	private static boolean judgeSubElementsContainsDeviceItem(
			List<Element> subElements, DeviceItem deviceItem) {
		boolean result = false;
		int subElementSize = subElements.size();
		for (int i = 0; i < subElementSize; i++) {
			Element subElement = subElements.get(i);
			String deviceName = subElement.attributeValue("deviceName");
			if (deviceName.equals(deviceItem.getDeviceName())) {
				subElement.detach();// 删除节点
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * 移除某个元素：星云账户
	 * 
	 * @return 移除成功与否，true表示移除成功；FALSE，表示移除失败；
	 */
	@SuppressWarnings("unchecked")
	public static synchronized boolean removeCloudAccoutFromXML(
			String fileName, CloudAccount cloudAccount) {
		// 首先从文档中构造出一个对象，如果该对象的数据和cloudAccount的属性值相同的话，那么则删除该用户
		boolean result = false;
		SAXReader saxReader = new SAXReader();
		File file = new File(fileName);
		try {
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			for (int i = 0; i < subElements.size(); i++) {
				Element subElement = subElements.get(i);
				String domain = subElement.attributeValue("domain");
				String port = subElement.attributeValue("port");
				String username = subElement.attributeValue("username");
				String password = subElement.attributeValue("password");
				if (domain.equals(cloudAccount.getDomain())
						&& (port.equals(cloudAccount.getPort()))
						&& (username.equals(cloudAccount.getUsername()))
						&& (password.equals(cloudAccount.getPassword()))) {
					subElement.detach();
				}
			}
			OutputFormat op = new OutputFormat("", true, "UTF-8");
			XMLWriter wrt = new XMLWriter(new FileOutputStream(fileName), op);
			wrt.write(document);
			wrt.close();
			result = true;
			System.out.println("remove Success!!!");
		} catch (Exception e) {
			result = false;
			System.out.println("remove Failed!!!");
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description 添加新的用户信息到指定的文件中,如果包含旧的用户，则覆盖；
	 * @param fileName
	 *            文件路径
	 * @param cloudAccount
	 *            星云账户
	 */
	@SuppressWarnings("unchecked")
	public static synchronized boolean addNewCloudAccoutNodeToRootXML(
			String fileName, CloudAccount cloudAccount) {//
		boolean result = false;
		try {
			File file = new File(fileName);
			SAXReader saxReader = new SAXReader();
			if (!file.exists()) {// 如果文件不存在，则创建文件，并打开文件进行读写操作
				file.createNewFile();
				Document doc = DocumentHelper.createDocument();
				doc.addElement("cloundAccounts");
				OutputFormat op = new OutputFormat("", true, "UTF-8");
				XMLWriter wr = new XMLWriter(new FileOutputStream(fileName), op);
				wr.write(doc);
				wr.close();
			}
			Document document = saxReader.read(file);
			Element rootElement = document.getRootElement();
			List<Element> subElements = rootElement.elements();

			String caDomain = cloudAccount.getDomain();
			String caPort = cloudAccount.getPort();
			String caPassword = cloudAccount.getPassword();
			String caUsername = cloudAccount.getUsername();

			int size = subElements.size();
			for (int i = 0; i < size; i++) {
				Element subElement = subElements.get(i);
				String domain = subElement.attributeValue("domain");
				String port = subElement.attributeValue("port");
				String username = subElement.attributeValue("username");
				String password = subElement.attributeValue("password");
				if (domain.equals(caDomain) && port.equals(caPort)
						&& username.equals(caUsername)
						&& password.equals(caPassword)) {
					subElement.detach();
					break;
				}
			}
			// 开始写入
			Element clAElement = rootElement.addElement("cloudAccount");
			clAElement.addAttribute("username", caUsername);
			clAElement.addAttribute("password", caPassword);
			clAElement.addAttribute("domain", caDomain);
			clAElement.addAttribute("port", caPort);
			boolean isEnabled = cloudAccount.isEnabled();
			clAElement.addAttribute("isEnabled", String.valueOf(isEnabled));

			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			XMLWriter wrt = new XMLWriter(new FileOutputStream(fileName), opf);
			wrt.write(document);
			wrt.close();
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 10, 2014
	 * @Description 将cloudAccountList写入到XML文件中
	 * @param cloudAccountList
	 * @param fileName
	 */
	@SuppressWarnings("unchecked")
	public synchronized static void writeNewCloudAccountToXML(
			CloudAccount clAcc, String fileName) {

		File file = new File(fileName);
		try {
			if (!file.exists()) {// 如果文件不存在，则新建立一个文件，并添加根节点；
				file.createNewFile();
				Document document1 = DocumentHelper.createDocument();
				document1.addElement("cloudAccounts");
				OutputFormat format = new OutputFormat("    ", true, "UTF-8");
				FileWriter fw = new FileWriter(fileName);
				XMLWriter writer = new XMLWriter(fw, format);
				writer.write(document1);
				fw.close();
				System.out.println("Generate Over!");
			}
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			if (clAcc != null) {
				// 判断文件是否存在，如果存在，则删除
				List<Element> subElements = root.elements();
				if (subElements != null) {
					int size = subElements.size();
					for (int i = 0; i < size; i++) {
						Element subElement = subElements.get(i);
						String domain = subElement.attributeValue("domain");
						String port = subElement.attributeValue("port");
						String password = subElement.attributeValue("password");
						String username = subElement.attributeValue("username");

						if (domain.equals(clAcc.getDomain())
								&& port.equals(clAcc.getPort())
								&& password.equals(clAcc.getPassword())
								&& username.equals(clAcc.getUsername())) {
							subElement.detach();
						}
					}
				}
				// 开始书写用户信息；首先指定用户属性
				Element clAElement = root.addElement("cloudAccount");
				clAElement.addAttribute("domain", clAcc.getDomain());
				clAElement.addAttribute("port", clAcc.getPort());
				clAElement.addAttribute("username", clAcc.getUsername());
				clAElement.addAttribute("password", clAcc.getPassword());
				clAElement.addAttribute("enabled",
						String.valueOf(clAcc.isEnabled()));
				clAElement.addAttribute("isExpanded",
						String.valueOf(clAcc.isEnabled()));
				clAElement.addAttribute("isRotate", String.valueOf(true));
				// 添加用户的设备列表
				List<DeviceItem> deviceItems = clAcc.getDeviceList();
				if (deviceItems != null) {
					int deviceSize = deviceItems.size();
					for (int j = 0; j < deviceSize; j++) {
						DeviceItem dItem = deviceItems.get(j);
						Element dEle = clAElement.addElement("device");
						dEle.addAttribute("deviceName", dItem.getDeviceName());
						dEle.addAttribute("svrIp", dItem.getSvrIp());
						dEle.addAttribute("svrPort", dItem.getSvrPort());
						dEle.addAttribute("loginUser", dItem.getLoginUser());
						dEle.addAttribute("loginPass", dItem.getLoginPass());
						dEle.addAttribute("defaultChannel",
								String.valueOf(dItem.getDefaultChannel()));
						dEle.addAttribute("channelSum", dItem.getChannelSum());
						dEle.addAttribute("deviceType",
								String.valueOf(dItem.getDeviceType()));
						dEle.addAttribute("isSecurityProtectionOpen", String
								.valueOf(dItem.isSecurityProtectionOpen()));
						dEle.addAttribute("isExpanded",
								String.valueOf(dItem.isExpanded()));
						dEle.addAttribute("isIdentify",
								String.valueOf(dItem.isIdentify()));
						dEle.addAttribute("isConnPass",
								String.valueOf(dItem.isConnPass()));
						List<Channel> chList = dItem.getChannelList();
						if (chList != null) {
							int channelSize = chList.size();
							for (int k = 0; k < channelSize; k++) {
								Channel chl = chList.get(k);
								Element cEle = dEle.addElement("channel");
								cEle.addAttribute("channelName",
										chl.getChannelName());
								cEle.addAttribute("channelNo",
										String.valueOf(chl.getChannelNo()));
								cEle.addAttribute("isSelected",
										String.valueOf(chl.isSelected()));
							}
						}
					}
				}
			}
			// 开始输入到文档中
			OutputFormat format = new OutputFormat("    ", true, "UTF-8");
			FileWriter fw = new FileWriter(fileName);
			XMLWriter writer = new XMLWriter(fw, format);
			writer.write(document);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<CloudAccount> readCloudAccountFromXML(String fileName) {
		List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(new File(fileName));// 报错
			Element root = document.getRootElement();
			List<Element> cloudElements = root.elements();
			int cloudAccountSize = cloudElements.size();
			// 得到了个数
			for (int i = 0; i < cloudAccountSize; i++) {
				CloudAccount cloudAccount = new CloudAccount();
				Element cAElement = cloudElements.get(i);
				// 获取cloudAccountElement的属性值
				String port = cAElement.attributeValue("port");
				String domain = cAElement.attributeValue("domain");
				String password = cAElement.attributeValue("password");
				String username = cAElement.attributeValue("username");
				String isEnabled = cAElement.attributeValue("enabled");
				String isRotate = cAElement.attributeValue("isRotate");
				String isExpanded = cAElement.attributeValue("isExpanded");
				cloudAccount.setPort(port);
				cloudAccount.setDomain(domain);
				cloudAccount.setPassword(password);
				cloudAccount.setUsername(username);
				cloudAccount.setRotate(Boolean.valueOf(isRotate));
				cloudAccount.setEnabled(Boolean.valueOf(isEnabled));
				cloudAccount.setExpanded(Boolean.valueOf(isExpanded));

				// 获取设备列表
				List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
				List<Element> dElev = cAElement.elements();
				for (int j = 0; j < dElev.size(); j++) {
					DeviceItem dItem = new DeviceItem();
					Element dEle = dElev.get(j);
					String svrIp = dEle.attributeValue("svrIp");
					String svrPort = dEle.attributeValue("svrPort");
					String isExd = dEle.attributeValue("isExpanded");
					String isIde = dEle.attributeValue("isIdentify");
					String isCon = dEle.attributeValue("isConnPass");
					String loginUser = dEle.attributeValue("loginUser");
					String loginPass = dEle.attributeValue("loginPass");
					String deviceName = dEle.attributeValue("deviceName");
					String channelSum = dEle.attributeValue("channelSum");
					String deviceType = dEle.attributeValue("deviceType");
					String deChannel = dEle.attributeValue("defaultChannel");
					String iso = dEle
							.attributeValue("isSecurityProtectionOpen");

					dItem.setSvrIp(svrIp);
					dItem.setSvrPort(svrPort);
					dItem.setLoginPass(loginPass);
					dItem.setLoginUser(loginUser);
					dItem.setChannelSum(channelSum);
					dItem.setDeviceName(deviceName);
					dItem.setExpanded(Boolean.valueOf(isExd));
					dItem.setExpanded(Boolean.valueOf(isIde));
					dItem.setExpanded(Boolean.valueOf(isCon));
					dItem.setDeviceType(Integer.valueOf(deviceType));
					dItem.setDefaultChannel(Integer.valueOf(deChannel));
					dItem.setSecurityProtectionOpen(Boolean.valueOf(iso));
					List<Channel> channelList = new ArrayList<Channel>();
					List<Element> chElement = dEle.elements();

					for (int k = 0; k < chElement.size(); k++) {
						Channel channel = new Channel();
						Element chEle = chElement.get(k);
						String chName = chEle.attributeValue("channelName");
						String channelNo = chEle.attributeValue("channelNo");
						String isSelected = chEle.attributeValue("isSelected");
						channel.setChannelName(chName);
						channel.setChannelNo(Integer.valueOf(channelNo));
						channel.setSelected(Boolean.valueOf(isSelected));
						channelList.add(channel);
					}
					dItem.setChannelList(channelList);
					deviceList.add(dItem);
				}
				cloudAccount.setDeviceList(deviceList);
				cloudAccountList.add(cloudAccount);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return cloudAccountList;
	}

	private final static String ALARMPUSHUSER_FILEPATH = "/data/data/com.starnet.snview/ALARMS_PUSHUSERS_FILE.xml";

	/** 向xml文档中添加报警推送账户 **/
	public static void addAlarmPushUserToXML(CloudAccount user) {
		File file = new File(ALARMPUSHUSER_FILEPATH);
		try {
			if (!file.exists()) {// 如果文件不存在，则新建立一个文件，并添加根节点；
				file.createNewFile();
				Document document1 = DocumentHelper.createDocument();
				document1.addElement("alarmPushusers");
				OutputFormat format = new OutputFormat("", true, "UTF-8");
				FileWriter fw = new FileWriter(ALARMPUSHUSER_FILEPATH);
				XMLWriter writer = new XMLWriter(fw, format);
				writer.write(document1);
				fw.close();
			}
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			if (user != null) {
				Element aEle = root.addElement("alarmPushuser");
				aEle.addAttribute("domain", user.getDomain());
				aEle.addAttribute("port", user.getPort());
				aEle.addAttribute("username", user.getUsername());
				aEle.addAttribute("password", user.getPassword());
			}
			OutputFormat format = new OutputFormat("", true, "UTF-8");
			FileWriter fw = new FileWriter(ALARMPUSHUSER_FILEPATH);
			XMLWriter writer = new XMLWriter(fw, format);
			writer.write(document);
			fw.close();
		} catch (Exception e) {

		}
	}

	/** 从指定文档中获取一系列用户 **/
	@SuppressWarnings("unchecked")
	public static List<CloudAccount> getAlarmPushUsersFromXML() {
		List<CloudAccount> users = new ArrayList<CloudAccount>();
		File file = new File(ALARMPUSHUSER_FILEPATH);
		if (!file.exists()) {
			return null;
		}
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> eles = root.elements();
			int cSize = eles.size();
			// 得到了个数
			for (int i = 0; i < cSize; i++) {
				CloudAccount cloudAccount = new CloudAccount();
				Element cAElement = eles.get(i);
				// 获取cloudAccountElement的属性值
				String domain = cAElement.attributeValue("domain");
				String port = cAElement.attributeValue("port");
				String username = cAElement.attributeValue("username");
				String password = cAElement.attributeValue("password");
				cloudAccount.setDomain(domain);
				cloudAccount.setPort(port);
				cloudAccount.setEnabled(true);
				cloudAccount.setUsername(username);
				cloudAccount.setPassword(password);
				users.add(cloudAccount);
			}
		} catch (Exception e) {
			return null;
		}
		return users;
	}
	
	/*** 从文档中移除指定位置的的AlarmDevice ***/
	@SuppressWarnings("unchecked")
	public static void removeSpecifyPushsetAlarm(int index) {
		File file = new File(ALARMPUSHUSER_FILEPATH);
		if (!file.exists()) {
			return;
		}
		FileWriter fileWriter = null;
		XMLWriter xmlWriter = null;
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			int size = subElements.size();
			for (int i = 0; i < size; i++) {// 判空处理
				if (index == i) {
					subElements.get(i).detach();
					break;
				}
			}
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			fileWriter = new FileWriter(ALARMPUSHUSER_FILEPATH);
			xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/** 替换指定位置的用户 **/
	@SuppressWarnings("unchecked")
	public static void deleteAlarmPushUserToXML(int index) {
		File file = new File(ALARMPUSHUSER_FILEPATH);
		if (!file.exists()) {
			return;
		}
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			int size = subElements.size();
			for (int i = 0; i < size; i++) {
				if (i == index) {
					subElements.get(i).detach();
					break;
				}
			}
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			FileWriter fileWriter = new FileWriter(file);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
			fileWriter.close();
		} catch (Exception e) {

		}
	}
	
	/** 清楚所有的报警信息,清楚成功返回true；否则返回false **/
	@SuppressWarnings("unchecked")
	public static boolean clearAllAlarmInfo() {
		File file = new File(ALARMPUSHUSER_FILEPATH);
		if (!file.exists()) {
			return true;
		}
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			int size = subElements.size();
			for (int i = 0; i < size; i++) {
				Element sEl = subElements.get(i);
				sEl.detach();
			}
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			FileWriter fileWriter = new FileWriter(file);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
			fileWriter.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** 替换指定位置的用户 **/
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static void replaceAlarmPushUserToXML(CloudAccount user, int index) {
		File file = new File(ALARMPUSHUSER_FILEPATH);
		if (!file.exists()) {
			return;
		}
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			int size = subElements.size();
			for (int i = 0; i < size; i++) {
				if (i == index) {
					Element sEl = subElements.get(i);
					sEl.setAttributeValue("domain", user.getDomain());
					sEl.setAttributeValue("port", user.getPort());
					sEl.setAttributeValue("password", user.getPassword());
					sEl.setAttributeValue("username", user.getUsername());
					break;
				}
			}
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			FileWriter fileWriter = new FileWriter(file);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
			fileWriter.close();
		} catch (Exception e) {

		}
	}
	
	
	/**与云台建立连接*/
	public static Document SendURLPost(String domain,String port,String username,String password,String deviceName) throws IOException, DocumentException {
		try {
			String urlStr;
			URL url;
			HttpURLConnection httpURLConnection;

			urlStr = "http://"+domain+":"+port+"/xml_device-list";   
			url = new URL(urlStr);
			httpURLConnection = (HttpURLConnection) url.openConnection(); //获取连接
			httpURLConnection.setRequestMethod("POST"); //设置请求方法为POST, 也可以为GET
			httpURLConnection.setDoOutput(true);  
			httpURLConnection.setConnectTimeout(10000); // 设置连接超时时间为10s
			String encoded = MD5Util.md5Encode(password);//为密码进行加密；      
			StringBuffer param = new StringBuffer("wu="+username+"&wp="+encoded+"&pn=");  //请求URL的查询参数     
			OutputStream os = httpURLConnection.getOutputStream();   
			os.write(param.toString().getBytes()); //将数据转化成byte数组，即字节数组；
			os.flush();   
			os.close();
			
			InputStream inputStr = httpURLConnection.getInputStream();
			SAXReader sr = new SAXReader();//读取类 
			Document doc = sr.read(inputStr);
			inputStr.close();
			return doc;
		} catch (DocumentException e) {
			throw e;
		} catch (SocketTimeoutException e) {
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	public static String readXmlStatus(Document doc) throws DocumentException {
		//获取状态码及相应状态信息，用于检测是否连接成功
		List resCodeList = null;
		resCodeList = doc.selectNodes("//Devices/resCode");
		Iterator resCodeIter = resCodeList.iterator();
		Element resCodeElement = (Element)resCodeIter.next();

		//获取状态码相对应信息，用于检测是否连接成功
		List descList = null;
		descList = doc.selectNodes("//Devices/desc");
		Iterator descIter = descList.iterator();
		Element descElement = (Element)descIter.next();
		
		//若登陆失败则返回失败原因，否则返回null
		if(!resCodeElement.getText().equals("0")){
			return descElement.getText().toString();
		}
		else
			return null;
	}

	@SuppressWarnings("rawtypes")
	public static List<DVRDevice> readXmlDVRDevices(Document doc)
			throws DocumentException {//获取设备信息
		//获取所有的设备节点元素
		List deviceListTemp = null;
		deviceListTemp = doc.selectNodes("//Devices/Device");
		Iterator deviceIter = deviceListTemp.iterator();
		List<DVRDevice> deviceList = new ArrayList<DVRDevice>();
		while(deviceIter.hasNext()){
			DVRDevice dvrDevice = new DVRDevice();
			//获得具体的元素   
			Element element = (Element)deviceIter.next();
			//<du>登陆设备用户名</du>
			Element duElement = element.element("du");
			dvrDevice.setLoginUsername(duElement.getText().toString());
			//<dp>登陆设备密码</dp>
			Element dpElement = element.element("dp");
			dvrDevice.setLoginPassword(dpElement.getText().toString());
			//<dm>登陆设备模式(0.IP  1.域名)</dm>
			Element dmElement = element.element("dm");
			dvrDevice.setLoginMode(dmElement.getText().toString());
			//<dip>设备IP[登陆设备模式 为IP的时候有效]</dip>
			Element dipElement = element.element("dip");
			dvrDevice.setLoginIP(dipElement.getText().toString());
			//<dd>设备域名[登陆设备模式 为域名的时候有效]</dd>
			Element ddElement = element.element("dd");
			dvrDevice.setLoginDomain(ddElement.getText().toString());
			//<dpn>登陆设备端口号</dpn>
			Element dpnElement = element.element("dpn");
			dvrDevice.setLoginPort(dpnElement.getText().toString());
			//<scn>开始通道号</scn>
			Element scnElement = element.element("scn");
			dvrDevice.setStarChannel(scnElement.getText().toString());
			//<cn>通道个数</cn>
			Element cnElement = element.element("cn");
			dvrDevice.setChannelNumber(cnElement.getText().toString());
			//<ain>报警输入个数</ain>
			Element ainElement = element.element("ain");
			dvrDevice.setWarningInputNumber(ainElement.getText().toString());
			//<aon>报警输出个数</aon>
			Element aonElement = element.element("aon");
			dvrDevice.setWarningOutputNumber(aonElement.getText().toString());
			//<acn>音频通道个数</acn>
			Element acnElement = element.element("acn");
			dvrDevice.setAudioChannelNumber(acnElement.getText().toString());
			//<vn>语音对讲个数</vn>
			Element vnElement = element.element("vn");
			dvrDevice.setIntercomNumber(vnElement.getText().toString());
			//<dn>硬盘个数</dn>
			Element dnElement = element.element("dn");
			dvrDevice.setHDNumber(dnElement.getText().toString());
			//<mdn>最大支持的移动侦测区域个数</mdn>
			Element mdnElement = element.element("mdn");
			dvrDevice.setMaxMobileDetectionNumber(mdnElement.getText().toString());
			//<can>最大支持的视频遮盖区域个数</can>
			Element canElement = element.element("can");
			dvrDevice.setMaxOverlayAreaNumber(canElement.getText().toString());
			//<tp>产品型号</tp>
			Element tpElement = element.element("tp");
			dvrDevice.setProductModel(tpElement.getText().toString());
			//<ma>厂家类型: 星网锐捷[枚举类型]</ma>
			Element maElement = element.element("ma");
			dvrDevice.setManufacturer(maElement.getText().toString());
			//<sn>设备序列号</sn>
			Element snElement = element.element("sn");
			dvrDevice.setSerialNumber(snElement.getText().toString());
			//<dmc>设备网卡地址</dmc>
			Element dmcElement = element.element("dmc");
			dvrDevice.setEthernetaddress(dmcElement.getText().toString());
			//<dv>设备版本号</dv>
			Element dvElement = element.element("dv");
			dvrDevice.setVersionNumber(dvElement.getText().toString());
			//<wu>平台用户名</wu>
			Element wuElement = element.element("wu");
			dvrDevice.setPlatformUsername(wuElement.getText().toString());
			//<wp>密码（密文）</wp>
			Element wpElement = element.element("wp");
			dvrDevice.setPlatformPassword(wpElement.getText().toString());
			//<dwp>设备WEB端口</dwp>
			Element dwpElement = element.element("dwp");
			dvrDevice.setWEBPort(dwpElement.getText().toString());
			//<dna>(在线、离线)设备名称</dna>
			Element dnaElement = element.element("dna");
			dvrDevice.setDeviceName(dnaElement.getText().toString());
			//<mp>手机端口号</mp>
			Element mpElement = element.element("mp");
			dvrDevice.setMobliePhonePort(mpElement.getText().toString());
			//<up>是否通过upnp上网(0.否，1.是)</up>
			Element upElement = element.element("up");
			dvrDevice.setIsUPNP(upElement.getText().toString());
			deviceList.add(dvrDevice);
		}
		return deviceList;
	}
}
