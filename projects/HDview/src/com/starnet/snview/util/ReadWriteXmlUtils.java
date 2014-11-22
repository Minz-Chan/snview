package com.starnet.snview.util;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

@SuppressLint("SdCardPath")
public class ReadWriteXmlUtils {
	protected static final String TAG = "AlarmPersistenceUtils";

	public final static String ALARMS_PERSISTANCE_PATH = "/data/data/com.starnet.snview/ALARMS_PERSISTANCE_FILE.xml";

	private static String INDENT = "    ";
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
			} else {
				doc = DocumentHelper.createDocument();
				doc.addElement("AlarmDevices");
			}

			Element root = doc.getRootElement();
			Element item = root.addElement("AlarmDevice");
			item.addAttribute("content", alarm.getAlarmContent());
			item.addAttribute("time", alarm.getAlarmTime());
			item.addAttribute("type", alarm.getAlarmType());
			item.addAttribute("deviceName", alarm.getDeviceName());
			item.addAttribute("imageUrl", alarm.getImageUrl());
			item.addAttribute("imgIp", alarm.getIp());
			item.addAttribute("password", alarm.getPassword());
			item.addAttribute("userName", alarm.getUserName());
			item.addAttribute("channel", String.valueOf(alarm.getChannel()));
			item.addAttribute("port", String.valueOf(alarm.getPort()));

			fw = new FileWriter(ALARMS_PERSISTANCE_PATH, false); // false to
																	// overwrite
																	// file
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

	// 从文档中移除指定的AlarmDevice
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

			int size = subElements.size();
			for (int i = 0; i < size; i++) {
				Element subElement = subElements.get(i);
				String deviceName = subElement.attributeValue("deviceName");
				String password = subElement.attributeValue("password");
				String userName = subElement.attributeValue("userName");
				if (alarmDevice.getDeviceName().equals(deviceName)
				&& (alarmDevice.getPassword().equals(password))&& (alarmDevice.getUserName().equals(userName))) {//
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

				String content = item.attributeValue("content");
				String time = item.attributeValue("time");
				String type = item.attributeValue("type");
				String deviceName = item.attributeValue("deviceName");
				String imageUrl = item.attributeValue("imageUrl");

				alarmDevice.setAlarmContent(content);
				alarmDevice.setAlarmTime(time);
				alarmDevice.setAlarmType(type);
				alarmDevice.setDeviceName(deviceName);
				alarmDevice.setImageUrl(imageUrl);

				String ip = item.attributeValue("imgIp");
				String password = item.attributeValue("password");
				String userName = item.attributeValue("userName");
				String channel = item.attributeValue("channel");
				String port = item.attributeValue("port");
				alarmDevice.setIp(ip);
				alarmDevice.setPassword(password);
				alarmDevice.setUserName(userName);

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
	@SuppressWarnings("deprecation")
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
			Element subElement = subElements.get(i);
			String domain = subElement.attributeValue("domain");
			String port = subElement.attributeValue("port");
			String username = subElement.attributeValue("username");
			String password = subElement.attributeValue("password");

			if (domain.equals(domained) && password.equals(passwded)
					&& username.equals(usNameed) && port.equals(usPorted)) {
				subElement.setAttributeValue("domain", domaines);
				subElement.setAttributeValue("port", usPortes);
				subElement.setAttributeValue("username", usNamees);
				subElement.setAttributeValue("password", passwdes);
				boolean isEnabled = cloudAccountes.isEnabled();
				subElement.setAttributeValue("isEnabled",
						String.valueOf(isEnabled));
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
			DeviceItem deviceItem = deviceItemList.get(i);
			Element subElement = root.addElement("deviceItem");
			subElement.addAttribute("deviceName", deviceItem.getDeviceName());
			subElement
					.addAttribute("channelNumber", deviceItem.getChannelSum());
			subElement.addAttribute("loginUser", deviceItem.getLoginUser());
			subElement.addAttribute("loginPass", deviceItem.getLoginPass());

			subElement.addAttribute("defaultChannel",
					String.valueOf(deviceItem.getDefaultChannel()));
			subElement.addAttribute("serverIP", deviceItem.getSvrIp());
			subElement.addAttribute("serverPort", deviceItem.getSvrPort());
			subElement.addAttribute("deviceType",
					String.valueOf(deviceItem.getDeviceType()));
			subElement.addAttribute("isSecurityProtectionOpen",
					String.valueOf(deviceItem.isSecurityProtectionOpen()));
			subElement.addAttribute("isExpanded",
					String.valueOf(deviceItem.isExpanded()));
			List<Channel> channelList = deviceItem.getChannelList();
			if (channelList != null) {
				int channelSize = channelList.size();
				for (int k = 0; k < channelSize; k++) {
					Channel channel = channelList.get(k);
					Element chnnelElement = subElement.addElement("channel");
					chnnelElement.addAttribute("channelName",
							channel.getChannelName());
					chnnelElement.addAttribute("channelNo",
							String.valueOf(channel.getChannelNo()));
					chnnelElement.addAttribute("isSelected",
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

			String username = subElement.attributeValue("username");
			String password = subElement.attributeValue("password");
			String domain = subElement.attributeValue("domain");
			String port = subElement.attributeValue("port");
			String isEnabled = subElement.attributeValue("isEnabled");

			cloudAccount.setDeviceList(null);
			cloudAccount.setRotate(false);
			cloudAccount.setEnabled(Boolean.valueOf(isEnabled));
			cloudAccount.setDomain(domain);
			cloudAccount.setPort(port);
			cloudAccount.setUsername(username);
			cloudAccount.setPassword(password);

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
			DeviceItem deviceItem = new DeviceItem();// 构造收藏设备
			Element subElement = subElements.get(i);

			String deviceName = subElement.attributeValue("deviceName");
			String channelSum = subElement.attributeValue("channelNumber");
			String loginUser = subElement.attributeValue("loginUser");
			String loginPass = subElement.attributeValue("loginPass");
			String defaultChannel = subElement.attributeValue("defaultChannel");
			String platformusername = subElement
					.attributeValue("platformusername");

			String svrIp = subElement.attributeValue("serverIP");
			String svrPort = subElement.attributeValue("serverPort");
			String deviceType = subElement.attributeValue("deviceType");
			String isSecurityProtectionOpen = subElement
					.attributeValue("isSecurityProtectionOpen");
			String isExpanded = subElement.attributeValue("isExpanded");

			if ((isSecurityProtectionOpen == null)
					|| (isSecurityProtectionOpen.equals(null))) {
				isSecurityProtectionOpen = "false";
			}
			if ((isExpanded == null) || (isExpanded.equals(null))) {
				isExpanded = "false";
			}
			deviceItem.setChannelSum(channelSum);
			deviceItem.setDeviceName(deviceName);
			deviceItem.setLoginUser(loginUser);
			deviceItem.setLoginPass(loginPass);
			deviceItem.setDefaultChannel(Integer.valueOf(defaultChannel));
			deviceItem.setSvrIp(svrIp);
			deviceItem.setSvrPort(svrPort);
			deviceItem.setSecurityProtectionOpen(Boolean
					.valueOf(isSecurityProtectionOpen));
			deviceItem.setExpanded(Boolean.valueOf(isExpanded));
			deviceItem.setDeviceType(Integer.valueOf(deviceType));
			deviceItem.setPlatformUsername(platformusername);

			List<Channel> channelList = new ArrayList<Channel>();

			List<Element> channelElements = subElement.elements();
			if (channelElements != null) {
				int channelSize = channelElements.size();
				for (int j = 0; j < channelSize; j++) {
					Channel channel = new Channel();

					Element channelElement = channelElements.get(j);
					String channelName = channelElement
							.attributeValue("channelName");
					String channelNo = channelElement
							.attributeValue("channelNo");
					String isSelected = channelElement
							.attributeValue("isSelected");
					channel.setChannelName(channelName);
					channel.setChannelNo(Integer.valueOf(channelNo));
					channel.setSelected(Boolean.valueOf(isSelected));

					channelList.add(channel);
				}
			}
			deviceItem.setChannelList(channelList);
			deviceList.add(deviceItem);
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
			DeviceItem deviceItem, String filePath) throws Exception {

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
		judgeSubElementsContainsDeviceItem(subElements, deviceItem);
		// 检查是否包含相同的元素，如果包含的话，则先删除，后添加；如果不包含，则添加；
		Element subElement = root.addElement("deviceItem");
		subElement.addAttribute("deviceName", deviceItem.getDeviceName());
		subElement.addAttribute("channelNumber", deviceItem.getChannelSum());
		subElement.addAttribute("loginUser", deviceItem.getLoginUser());
		subElement.addAttribute("loginPass", deviceItem.getLoginPass());
		subElement.addAttribute("platformusername",
				deviceItem.getPlatformUsername());

		subElement.addAttribute("defaultChannel",
				String.valueOf(deviceItem.getDefaultChannel()));
		subElement.addAttribute("serverIP", deviceItem.getSvrIp());
		subElement.addAttribute("serverPort", deviceItem.getSvrPort());
		subElement.addAttribute("deviceType",
				String.valueOf(deviceItem.getDeviceType()));
		subElement.addAttribute("isSecurityProtectionOpen",
				String.valueOf(deviceItem.isSecurityProtectionOpen()));
		subElement.addAttribute("isExpanded",
				String.valueOf(deviceItem.isExpanded()));
		List<Channel> channelList = deviceItem.getChannelList();
		if (channelList != null) {
			int channelSize = channelList.size();
			for (int k = 0; k < channelSize; k++) {
				Channel channel = channelList.get(k);
				Element chnnelElement = subElement.addElement("channel");
				chnnelElement.addAttribute("channelName",
						channel.getChannelName());
				chnnelElement.addAttribute("channelNo",
						String.valueOf(channel.getChannelNo()));
				chnnelElement.addAttribute("isSelected",
						String.valueOf(channel.isSelected()));
			}
		}

		OutputFormat opf = new OutputFormat("", true, "UTF-8");
		FileWriter fileWriter = new FileWriter(filePath);
		XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
		xmlWriter.write(document);
		fileWriter.close();
		saveResult = "保存成功！";
		return saveResult;
	}

	private static boolean judgeSubElementsContainsDeviceItem(
			List<Element> subElements, DeviceItem deviceItem) {
		boolean result = false;
		int subElementSize = subElements.size();
		for (int i = 0; i < subElementSize; i++) {// 检查是否包含相同的元素，如果包含的话，则删除；
			Element subElement = subElements.get(i);
			String deviceName = subElement.attributeValue("deviceName");
			String channelNumber = subElement.attributeValue("channelNumber");
			String loginUser = subElement.attributeValue("loginUser");
			String loginPass = subElement.attributeValue("loginPass");

			String defaultChannel = subElement.attributeValue("defaultChannel");
			String serverIP = subElement.attributeValue("serverIP");
			String serverPort = subElement.attributeValue("serverPort");

			if (deviceName.equals(deviceItem.getDeviceName())
					&& channelNumber.equals(deviceItem.getChannelSum())
					&& loginUser.equals(deviceItem.getLoginUser())
					&& loginPass.equals(deviceItem.getLoginPass())
					&& serverIP.equals(deviceItem.getSvrIp())
					&& serverPort.equals(deviceItem.getSvrPort())
					&& defaultChannel.equals(String.valueOf(deviceItem
							.getDefaultChannel()))) {
				subElement.detach();// 删除节点
				result = true;
				break;// 表示删除第一个，如果，不使用break，则表示删除所有的...
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
			int size = subElements.size();
			for (int i = 0; i < size; i++) {
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
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			XMLWriter writer = new XMLWriter(new FileOutputStream(fileName),
					opf);
			writer.write(document);
			writer.close();
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
				// 创建根
				Document doc = DocumentHelper.createDocument();
				doc.addElement("cloundAccounts");
				OutputFormat opf = new OutputFormat("", true, "UTF-8");
				XMLWriter writer = new XMLWriter(
						new FileOutputStream(fileName), opf);
				writer.write(doc);
				writer.close();
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
			Element cloudAccountElement = rootElement
					.addElement("cloudAccount");
			cloudAccountElement.addAttribute("username", caUsername);
			cloudAccountElement.addAttribute("password", caPassword);
			cloudAccountElement.addAttribute("domain", caDomain);
			cloudAccountElement.addAttribute("port", caPort);
			boolean isEnabled = cloudAccount.isEnabled();
			cloudAccountElement.addAttribute("isEnabled",
					String.valueOf(isEnabled));

			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			XMLWriter writer = new XMLWriter(new FileOutputStream(fileName),
					opf);
			writer.write(document);
			writer.close();
			result = true;
		} catch (Exception e) {
			System.out.println("Wrong!");
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
			CloudAccount cloudAccount, String fileName) {

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
			if (cloudAccount != null) {
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

						if (domain.equals(cloudAccount.getDomain())
								&& port.equals(cloudAccount.getPort())
								&& password.equals(cloudAccount.getPassword())
								&& username.equals(cloudAccount.getUsername())) {
							subElement.detach();
						}
					}
				}
				// 开始书写用户信息；首先指定用户属性
				Element cloudAccountElement = root.addElement("cloudAccount");
				cloudAccountElement.addAttribute("domain",
						cloudAccount.getDomain());
				cloudAccountElement
						.addAttribute("port", cloudAccount.getPort());
				cloudAccountElement.addAttribute("username",
						cloudAccount.getUsername());
				cloudAccountElement.addAttribute("password",
						cloudAccount.getPassword());
				cloudAccountElement.addAttribute("enabled",
						String.valueOf(cloudAccount.isEnabled()));
				cloudAccountElement.addAttribute("isExpanded",
						String.valueOf(cloudAccount.isEnabled()));
				cloudAccountElement.addAttribute("isRotate",
						String.valueOf(true));
				// 添加用户的设备列表
				List<DeviceItem> deviceItems = cloudAccount.getDeviceList();
				if (deviceItems != null) {
					int deviceSize = deviceItems.size();
					for (int j = 0; j < deviceSize; j++) {
						DeviceItem deviceItem = deviceItems.get(j);
						Element deviceElement = cloudAccountElement
								.addElement("device");
						deviceElement.addAttribute("deviceName",
								deviceItem.getDeviceName());
						deviceElement.addAttribute("svrIp",
								deviceItem.getSvrIp());
						deviceElement.addAttribute("svrPort",
								deviceItem.getSvrPort());
						deviceElement.addAttribute("loginUser",
								deviceItem.getLoginUser());
						deviceElement.addAttribute("loginPass",
								deviceItem.getLoginPass());
						deviceElement.addAttribute("defaultChannel",
								String.valueOf(deviceItem.getDefaultChannel()));
						deviceElement.addAttribute("channelSum",
								deviceItem.getChannelSum());
						deviceElement.addAttribute("deviceType",
								String.valueOf(deviceItem.getDeviceType()));
						deviceElement.addAttribute("isSecurityProtectionOpen",
								String.valueOf(deviceItem
										.isSecurityProtectionOpen()));
						deviceElement.addAttribute("isExpanded",
								String.valueOf(deviceItem.isExpanded()));
						List<Channel> channelList = deviceItem.getChannelList();
						if (channelList != null) {
							int channelSize = channelList.size();
							for (int k = 0; k < channelSize; k++) {
								Channel channel = channelList.get(k);
								Element chnnelElement = deviceElement
										.addElement("channel");
								chnnelElement.addAttribute("channelName",
										channel.getChannelName());
								chnnelElement.addAttribute("channelNo",
										String.valueOf(channel.getChannelNo()));
								chnnelElement.addAttribute("isSelected",
										String.valueOf(channel.isSelected()));
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
			System.out.println("Generate Over!");
		} catch (Exception e) {
			System.out.println("Wrong!");
			e.printStackTrace();
		}
	}

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
				Element cloudAccountElement = cloudElements.get(i);
				// 获取cloudAccountElement的属性值
				String domain = cloudAccountElement.attributeValue("domain");
				String password = cloudAccountElement
						.attributeValue("password");
				String port = cloudAccountElement.attributeValue("port");
				String username = cloudAccountElement
						.attributeValue("username");
				String isEnabled = cloudAccountElement
						.attributeValue("enabled");
				String isExpanded = cloudAccountElement
						.attributeValue("isExpanded");
				String isRotate = cloudAccountElement
						.attributeValue("isRotate");

				cloudAccount.setDomain(domain);
				cloudAccount.setEnabled(Boolean.valueOf(isEnabled));
				cloudAccount.setExpanded(Boolean.valueOf(isExpanded));
				cloudAccount.setPassword(password);
				cloudAccount.setPort(port);
				cloudAccount.setUsername(username);
				cloudAccount.setRotate(Boolean.valueOf(isRotate));

				// 获取设备列表
				List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
				List<Element> deviceListElement = cloudAccountElement
						.elements();
				int deviceSize = deviceListElement.size();
				for (int j = 0; j < deviceSize; j++) {
					DeviceItem deviceItem = new DeviceItem();
					Element deviceElement = deviceListElement.get(j);
					String deviceName = deviceElement
							.attributeValue("deviceName");
					String svrIp = deviceElement.attributeValue("svrIp");
					String svrPort = deviceElement.attributeValue("svrPort");
					String loginUser = deviceElement
							.attributeValue("loginUser");
					String loginPass = deviceElement
							.attributeValue("loginPass");
					String defaultChannel = deviceElement
							.attributeValue("defaultChannel");
					String channelSum = deviceElement
							.attributeValue("channelSum");
					String deviceType = deviceElement
							.attributeValue("deviceType");
					String iSPO = deviceElement
							.attributeValue("isSecurityProtectionOpen");
					String isExpanded2 = deviceElement
							.attributeValue("isExpanded");

					deviceItem.setChannelSum(channelSum);
					deviceItem.setDefaultChannel(Integer
							.valueOf(defaultChannel));
					deviceItem.setDeviceName(deviceName);
					deviceItem.setDeviceType(Integer.valueOf(deviceType));
					deviceItem.setExpanded(Boolean.valueOf(isExpanded2));
					deviceItem.setSecurityProtectionOpen(Boolean.valueOf(iSPO));
					deviceItem.setLoginPass(loginPass);
					deviceItem.setLoginUser(loginUser);
					deviceItem.setSvrIp(svrIp);
					deviceItem.setSvrPort(svrPort);

					List<Element> channelElement = deviceElement.elements();
					List<Channel> channelList = new ArrayList<Channel>();
					int channelSize = channelElement.size();
					for (int k = 0; k < channelSize; k++) {
						Channel channel = new Channel();
						Element chElement = channelElement.get(k);
						String channelName = chElement
								.attributeValue("channelName");
						String channelNo = chElement
								.attributeValue("channelNo");
						String isSelected = chElement
								.attributeValue("isSelected");
						channel.setChannelName(channelName);
						channel.setChannelNo(Integer.valueOf(channelNo));
						channel.setSelected(Boolean.valueOf(isSelected));
						channelList.add(channel);
					}
					deviceItem.setChannelList(channelList);
					deviceList.add(deviceItem);
				}
				cloudAccount.setDeviceList(deviceList);
				cloudAccountList.add(cloudAccount);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return cloudAccountList;
	}
}