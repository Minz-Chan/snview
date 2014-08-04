package com.starnet.snview.channelmanager.xml;

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

import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

/**
 * 
 * @author zhaohongxu
 * @Date Jul 10, 2014
 * @ClassName CloudAccountXML.java
 * @Description 封装了CloudAccount到XML文档之间的转化
 * @Modifier zhaohongxu
 * @Modify date Jul 10, 2014
 * @Modify description TODO
 */
public class CloudAccountXML {

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 10, 2014
	 * @Description 读取XML文件获取内容
	 * @param fileName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<CloudAccount> readCloudAccountFromXML(String fileName) {
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
				String password = cloudAccountElement.attributeValue("password");
				String port = cloudAccountElement.attributeValue("port");
				String username = cloudAccountElement.attributeValue("username");
				String isEnabled = cloudAccountElement.attributeValue("enabled");
				String isExpanded = cloudAccountElement.attributeValue("isExpanded");
				String isRotate = cloudAccountElement.attributeValue("isRotate");

				cloudAccount.setDomain(domain);
				cloudAccount.setEnabled(Boolean.valueOf(isEnabled));
				cloudAccount.setExpanded(Boolean.valueOf(isExpanded));
				cloudAccount.setPassword(password);
				cloudAccount.setPort(port);
				cloudAccount.setUsername(username);
				cloudAccount.setRotate(Boolean.valueOf(isRotate));

				// 获取设备列表
				List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
				List<Element> deviceListElement = cloudAccountElement.elements();
				int deviceSize = deviceListElement.size();
				for (int j = 0; j < deviceSize; j++) {
					DeviceItem deviceItem = new DeviceItem();
					Element deviceElement = deviceListElement.get(j);
					String deviceName = deviceElement.attributeValue("deviceName");
					String svrIp = deviceElement.attributeValue("svrIp");
					String svrPort = deviceElement.attributeValue("svrPort");
					String loginUser = deviceElement.attributeValue("loginUser");
					String loginPass = deviceElement.attributeValue("loginPass");
					String defaultChannel = deviceElement.attributeValue("defaultChannel");
					String channelSum = deviceElement.attributeValue("channelSum");
					String deviceType = deviceElement.attributeValue("deviceType");
					String iSPO = deviceElement.attributeValue("isSecurityProtectionOpen");
					String isExpanded2 = deviceElement.attributeValue("isExpanded");

					deviceItem.setChannelSum(channelSum);
					deviceItem.setDefaultChannel(Integer.valueOf(defaultChannel));
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
						String channelName = chElement.attributeValue("channelName");
						String channelNo = chElement.attributeValue("channelNo");
						String isSelected = chElement.attributeValue("isSelected");
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

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 10, 2014
	 * @Description 将cloudAccountList写入到XML文件中
	 * @param cloudAccountList
	 * @param fileName
	 */
	@SuppressWarnings("unchecked")
	public synchronized void writeNewCloudAccountToXML(CloudAccount cloudAccount,String fileName) {

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
				cloudAccountElement.addAttribute("domain",cloudAccount.getDomain());
				cloudAccountElement.addAttribute("port", cloudAccount.getPort());
				cloudAccountElement.addAttribute("username",cloudAccount.getUsername());
				cloudAccountElement.addAttribute("password",cloudAccount.getPassword());
				cloudAccountElement.addAttribute("enabled",String.valueOf(cloudAccount.isEnabled()));
				cloudAccountElement.addAttribute("isExpanded",String.valueOf(cloudAccount.isEnabled()));
				cloudAccountElement.addAttribute("isRotate",String.valueOf(true));
				// 添加用户的设备列表
				List<DeviceItem> deviceItems = cloudAccount.getDeviceList();
				if (deviceItems != null) {
					int deviceSize = deviceItems.size();
					for (int j = 0; j < deviceSize; j++) {
						DeviceItem deviceItem = deviceItems.get(j);
						Element deviceElement = cloudAccountElement.addElement("device");
						deviceElement.addAttribute("deviceName",deviceItem.getDeviceName());
						deviceElement.addAttribute("svrIp",deviceItem.getSvrIp());
						deviceElement.addAttribute("svrPort",deviceItem.getSvrPort());
						deviceElement.addAttribute("loginUser",deviceItem.getLoginUser());
						deviceElement.addAttribute("loginPass",deviceItem.getLoginPass());
						deviceElement.addAttribute("defaultChannel",String.valueOf(deviceItem.getDefaultChannel()));
						deviceElement.addAttribute("channelSum",deviceItem.getChannelSum());
						deviceElement.addAttribute("deviceType",String.valueOf(deviceItem.getDeviceType()));
						deviceElement.addAttribute("isSecurityProtectionOpen",String.valueOf(deviceItem.isSecurityProtectionOpen()));
						deviceElement.addAttribute("isExpanded",String.valueOf(deviceItem.isExpanded()));
						List<Channel> channelList = deviceItem.getChannelList();
						if (channelList != null) {
							int channelSize = channelList.size();
							for (int k = 0; k < channelSize; k++) {
								Channel channel = channelList.get(k);
								Element chnnelElement = deviceElement.addElement("channel");
								chnnelElement.addAttribute("channelName",channel.getChannelName());
								chnnelElement.addAttribute("channelNo",String.valueOf(channel.getChannelNo()));
								chnnelElement.addAttribute("isSelected",String.valueOf(channel.isSelected()));
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

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 12, 2014
	 * @Description 创建星云账户的根节点
	 * @param fileName
	 *            文件路径
	 * @param rootName
	 *            根的名字
	 */
	public boolean createRootXMLForCloudAccout(String fileName, String rootName) {
		boolean result = false;
		Document document = DocumentHelper.createDocument();
		document.addElement("cloudAccounts");

		// 开始输入到文档中
		OutputFormat format = new OutputFormat("    ", true, "UTF-8");
		try {
			FileWriter fw = new FileWriter(fileName);
			XMLWriter writer = new XMLWriter(fw, format);
			writer.write(document);
			fw.close();
			System.out.println("Generate Over!");
			result = true;
		} catch (IOException e) {
			System.out.println("Wrong!");
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description 添加新的用户信息到指定的文件中,如果包含旧的用户，则覆盖；
	 * @param fileName 文件路径
	 * @param cloudAccount 星云账户
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean addNewCloudAccoutNodeToRootXML(String fileName,CloudAccount cloudAccount) {//
		boolean result = false;
		try {
		    File file = new File(fileName);
		    SAXReader saxReader = new SAXReader();
		    if(!file.exists()){//如果文件不存在，则创建文件，并打开文件进行读写操作
		    	file.createNewFile();
		    	//创建根
		    	Document doc = DocumentHelper.createDocument();
		    	doc.addElement("cloundAccounts");
		    	OutputFormat opf = new OutputFormat("", true, "UTF-8");
				XMLWriter writer = new XMLWriter(new FileOutputStream(fileName),opf);
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
				if (domain.equals(caDomain)&& port.equals(caPort)&& username.equals(caUsername)&& password.equals(caPassword)) {
					subElement.detach();
					break;
				}
			}
			//开始写入
			Element cloudAccountElement = rootElement.addElement("cloudAccount");
			cloudAccountElement.addAttribute("username", caUsername);
			cloudAccountElement.addAttribute("password", caPassword);
			cloudAccountElement.addAttribute("domain", caDomain);
			cloudAccountElement.addAttribute("port", caPort);
			boolean isEnabled = cloudAccount.isEnabled();
			cloudAccountElement.addAttribute("isEnabled", String.valueOf(isEnabled));
			
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			XMLWriter writer = new XMLWriter(new FileOutputStream(fileName),opf);
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
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description 移除某个元素
	 * @param fileName
	 *            ：文件名称
	 * @param cloudAccount
	 *            ：星云账户
	 * @return 移除成功与否，true表示移除成功；FALSE，表示移除失败；
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean removeCloudAccoutFromXML(String fileName,CloudAccount cloudAccount) {
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
			XMLWriter writer = new XMLWriter(new FileOutputStream(fileName),opf);
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
	 * @author zhongxu
	 * @throws Exception 
	 * @Date 2014年7月26日
	 * @Description 增加新的设备到收藏设备文档中
	 */
	@SuppressWarnings("unchecked")
	public String addNewDeviceItemToCollectEquipmentXML(DeviceItem deviceItem,String filePath) throws Exception{
		
		String saveResult = "";
		//创建一个文档，并检查文档是否存在，若存在则不创建；否则，则创建；
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
			//为文档创建一个根
			Document document = DocumentHelper.createDocument();
			document.addElement("deviceItems");
			OutputFormat opf = new OutputFormat("", true, "UTF-8");
			FileWriter fileWriter = new FileWriter(filePath);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, opf);
			xmlWriter.write(document);
			fileWriter.close();
		}
		
		//增加内容
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		
		List<Element> subElements = root.elements();
		judgeSubElementsContainsDeviceItem(subElements,deviceItem);
		//检查是否包含相同的元素，如果包含的话，则先删除，后添加；如果不包含，则添加；
			Element subElement = root.addElement("deviceItem");
			subElement.addAttribute("deviceName", deviceItem.getDeviceName());
			subElement.addAttribute("channelNumber", deviceItem.getChannelSum());
			subElement.addAttribute("loginUser", deviceItem.getLoginUser());
			subElement.addAttribute("loginPass", deviceItem.getLoginPass());
			
			subElement.addAttribute("defaultChannel", String.valueOf(deviceItem.getDefaultChannel()));
			subElement.addAttribute("serverIP", deviceItem.getSvrIp());
			subElement.addAttribute("serverPort", deviceItem.getSvrPort());
			subElement.addAttribute("deviceType",String.valueOf(deviceItem.getDeviceType()));
			subElement.addAttribute("isSecurityProtectionOpen",String.valueOf(deviceItem.isSecurityProtectionOpen()));
			subElement.addAttribute("isExpanded",String.valueOf(deviceItem.isExpanded()));
			List<Channel> channelList = deviceItem.getChannelList();
			if (channelList != null) {
				int channelSize = channelList.size();
				for (int k = 0; k < channelSize; k++) {
					Channel channel = channelList.get(k);
					Element chnnelElement = subElement.addElement("channel");
					chnnelElement.addAttribute("channelName",channel.getChannelName());
					chnnelElement.addAttribute("channelNo",String.valueOf(channel.getChannelNo()));
					chnnelElement.addAttribute("isSelected",String.valueOf(channel.isSelected()));
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

	private boolean judgeSubElementsContainsDeviceItem(List<Element> subElements, DeviceItem deviceItem) {
		boolean result = false;
		int subElementSize = subElements.size();
		for (int i = 0; i < subElementSize; i++) {//检查是否包含相同的元素，如果包含的话，则删除；
			Element subElement = subElements.get(i);
			String deviceName = subElement.attributeValue("deviceName");
			String channelNumber = subElement.attributeValue("channelNumber");
			String loginUser = subElement.attributeValue("loginUser");
			String loginPass = subElement.attributeValue("loginPass");
			
			String defaultChannel = subElement.attributeValue("defaultChannel");
			String serverIP = subElement.attributeValue("serverIP");
			String serverPort = subElement.attributeValue("serverPort");
			
			if (deviceName.equals(deviceItem.getDeviceName())&&channelNumber.equals(deviceItem.getChannelSum())
					&&loginUser.equals(deviceItem.getLoginUser())	&&loginPass.equals(deviceItem.getLoginPass())
					&&serverIP.equals(deviceItem.getSvrIp())&&serverPort.equals(deviceItem.getSvrPort())
					&&defaultChannel.equals(String.valueOf(deviceItem.getDefaultChannel()))) {
				subElement.detach();//删除节点
				result = true;
				break;
			}
		}
		return result;
	}
	//从指定的xml文档中获取收藏设备列表...
	@SuppressWarnings("unchecked")
	public List<DeviceItem> getCollectDeviceListFromXML(String fileName) throws Exception{
		List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		SAXReader saxReader = new SAXReader();
		File file = new File(fileName);
		if(!file.exists()){
			return deviceList;
		}
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		List <Element> subElements = root.elements();//子目录
		int size = subElements.size();
		for(int i =0 ;i<size ;i++){
			DeviceItem deviceItem = new DeviceItem();//构造收藏设备
			Element subElement = subElements.get(i);
			
			String deviceName = subElement.attributeValue("deviceName");
			String channelSum = subElement.attributeValue("channelNumber");
			String loginUser = subElement.attributeValue("loginUser");
			String loginPass = subElement.attributeValue("loginPass");
			String defaultChannel = subElement.attributeValue("defaultChannel");
			
			String svrIp = subElement.attributeValue("serverIP");
			String svrPort = subElement.attributeValue("serverPort");
			String deviceType = subElement.attributeValue("deviceType");
			String isSecurityProtectionOpen = subElement.attributeValue("isSecurityProtectionOpen");
			String isExpanded = subElement.attributeValue("isExpanded");
			
			if((isSecurityProtectionOpen == null)||(isSecurityProtectionOpen.equals(null))){
				isSecurityProtectionOpen = "false";
			}
			if((isExpanded == null)||(isExpanded.equals(null))){
				isExpanded = "false";
			}
			deviceItem.setChannelSum(channelSum);
			deviceItem.setDeviceName(deviceName);
			deviceItem.setLoginUser(loginUser);
			deviceItem.setLoginPass(loginPass);
			deviceItem.setDefaultChannel(Integer.valueOf(defaultChannel));
			deviceItem.setSvrIp(svrIp);
			deviceItem.setSvrPort(svrPort);
			deviceItem.setSecurityProtectionOpen(Boolean.valueOf(isSecurityProtectionOpen));
			deviceItem.setExpanded(Boolean.valueOf(isExpanded));
			deviceItem.setDeviceType(Integer.valueOf(deviceType));
			List<Channel>channelList = new ArrayList<Channel>();
			
			List<Element> channelElements = subElement.elements();
			if(channelElements != null){
				int channelSize = channelElements.size();
				for(int j = 0 ;j < channelSize;j++){
					Channel channel = new Channel();
					
					Element channelElement = channelElements.get(j);
					String channelName = channelElement.attributeValue("channelName");
					String channelNo = channelElement.attributeValue("channelNo");
					String isSelected = channelElement.attributeValue("isSelected");
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
	
	//从指定的文档中获取内容
	@SuppressWarnings("unchecked")
	public List<CloudAccount>  getCloudAccountList(String filePath) throws Exception{
		List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
		File file = new File(filePath);
		if(!file.exists()){
			return cloudAccountList;
		}
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		Element root = document.getRootElement();
		List<Element> subElements = root.elements();
		
		for( Element subElement : subElements){
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
}