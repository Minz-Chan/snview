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

				cloudAccount.setDomain(domain);
				cloudAccount.setEnabled(Boolean.valueOf(isEnabled));
				cloudAccount.setExpanded(Boolean.valueOf(isExpanded));
				cloudAccount.setPassword(password);
				cloudAccount.setPort(port);
				cloudAccount.setUsername(username);

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
					deviceItem.setDefaultChannel(defaultChannel);
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
	public void writeNewCloudAccountToXML(CloudAccount cloudAccount,
			String fileName) {

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
				cloudAccountElement.addAttribute("password",cloudAccount.getPassword());
				cloudAccountElement.addAttribute("port", cloudAccount.getPort());
				cloudAccountElement.addAttribute("username",cloudAccount.getUsername());
				cloudAccountElement.addAttribute("enabled",String.valueOf(cloudAccount.isEnabled()));
				cloudAccountElement.addAttribute("isExpanded",String.valueOf(cloudAccount.isEnabled()));
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
						deviceElement.addAttribute("defaultChannel",deviceItem.getDefaultChannel());
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
	 * @Description 添加新的用户信息到指定的文件中
	 * @param fileName
	 * @param cloudAccount
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean addNewCloudAccoutNodeToRootXML(String fileName,
			CloudAccount cloudAccount) {
		boolean result = false;
		SAXReader saxReader = new SAXReader();
		File file = new File(fileName);
		try {
			Document document = saxReader.read(file);
			Element rootElement = document.getRootElement();
			List<Element> subElements = rootElement.elements();
			boolean contain = judgeContainCloudAccount(subElements,
					cloudAccount);// 遍历跟元素是否包含了包含cloudAccount
			if (!contain) {// 如果不包含，则添加；否则，不添加
				addCloudAccoutToRootXML(fileName, cloudAccount);
			}
			result = true;
		} catch (Exception e) {
			System.out.println("Wrong!");
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	private boolean judgeContainCloudAccount(List<Element> subElements,
			CloudAccount cloudAccount) {
		boolean result = false;
		int size = subElements.size();
		for (int i = 0; i < size; i++) {
			Element subElement = subElements.get(i);
			String domain = subElement.attributeValue("domain");
			String port = subElement.attributeValue("port");
			String username = subElement.attributeValue("username");
			String password = subElement.attributeValue("password");
			if (domain.equals(cloudAccount.getDomain())
					&& port.equals(cloudAccount.getPort())
					&& username.equals(cloudAccount.getUsername())
					&& password.equals(cloudAccount.getPassword())) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * @author zhaohongxu
	 * @Date Jul 12, 2014
	 * @Description 添加一个星云账户
	 * @param fileName
	 *            文件的名称
	 * @param cloudAccount
	 *            要添加的用户信息
	 */
	private boolean addCloudAccoutToRootXML(String fileName,
			CloudAccount cloudAccount) {
		// 需要遍历原来的数据，查看是否有与现在的结点是否相同？(domain/port/username/password),不相同，则增加；否则不增加；
		boolean result = false;
		SAXReader saxReader = new SAXReader();
		File file = new File(fileName);
		try {
			Document document = saxReader.read(file);
			Element rootElement = document.getRootElement();
			Element subElement = rootElement.addElement("cloudAccount");
			subElement.addAttribute("domain", cloudAccount.getDomain());
			subElement.addAttribute("port", cloudAccount.getPort());
			subElement.addAttribute("username", cloudAccount.getUsername());
			subElement.addAttribute("password", cloudAccount.getPassword());
			subElement.addAttribute("isEnabled",
					String.valueOf(cloudAccount.isEnabled()));
			// 开始输入到文档中
			OutputFormat format = new OutputFormat("    ", true, "UTF-8");
			FileWriter fw = new FileWriter(fileName);
			XMLWriter writer = new XMLWriter(fw, format);
			writer.write(document);
			fw.close();
			System.out.println("Generate Over!");
			result = true;
		} catch (Exception e) {
			System.out.println("Wrong!");
			e.printStackTrace();
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
	public boolean removeCloudAccoutFromXML(String fileName,
			CloudAccount cloudAccount) {
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
				String isEnabled = subElement.attributeValue("isEnabled");
				boolean isEnable = Boolean.valueOf(isEnabled);
				if (domain.equals(cloudAccount.getDomain())
						&& (port.equals(cloudAccount.getPort()))
						&& (username.equals(cloudAccount.getUsername()))
						&& (password.equals(cloudAccount.getPassword()))
						&& (isEnable == cloudAccount.isEnabled())) {
					subElement.detach();
				}
			}
			OutputFormat opf = new OutputFormat("    ", true, "UTF-8");
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
}