package com.starnet.snview.util;

import java.io.File;
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

import com.starnet.snview.realplay.PreviewDeviceItem;

public class PreviewItemXMLUtils {

	/**
	 * 
	 * @param pdiList
	 *            ：预览列表
	 * @param page
	 *            :所在页
	 * @param channelMode
	 *            :通道模式(多/单)
	 * @param filePath
	 *            :文件路径；
	 * @return :true表示正常写入，FALSE，表示写入错误；
	 * @throws IOException
	 */
	public boolean writePreviewItemListInfoToXML( List<PreviewDeviceItem> previewDeviceItemList, int page, boolean channelMode,String filePath) throws IOException {
		boolean result = false;
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
		}
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("previewDeviceItems");
		
		Element subElement = root.addElement("previewDeviceItem");
		int size = previewDeviceItemList.size();
		for (int i = 0; i < size; i++) {
			PreviewDeviceItem previewDeviceItem = previewDeviceItemList.get(i);
			
			String dRName = previewDeviceItem.getDeviceRecordName();
			String lgPass = previewDeviceItem.getLoginPass();
			String lgUser = previewDeviceItem.getLoginUser();
			
			String dSvrIp = previewDeviceItem.getSvrIp();
			String svPort = previewDeviceItem.getSvrPort();
			String channl = String.valueOf(previewDeviceItem.getChannel());
			
			String pageWR = String.valueOf(page);
			String chnlMd = String.valueOf(channelMode);
			
			subElement.addAttribute("dRName", dRName);
			subElement.addAttribute("lgPass", lgPass);
			subElement.addAttribute("lgUser", lgUser);
			
			subElement.addAttribute("dSvrIp", dSvrIp);
			subElement.addAttribute("svPort", svPort);
			subElement.addAttribute("channl", channl);
			
			subElement.addAttribute("pageWR", pageWR);
			subElement.addAttribute("chnlMd", chnlMd);
		}
		
		OutputFormat opf = new OutputFormat("", true, "UTF-8");
		FileWriter writer = new FileWriter(file);
		XMLWriter xmlWriter = new XMLWriter(writer,opf);
		xmlWriter.write(document);
		writer.close();
		result = true;
		return result;
	}
	
	/**
	 * 
	 * @param filePath:文件路径
	 * @return 预览列表
	 */
	public List<PreviewDeviceItem> getPreviewItemListInfoFromXML(String filePath){
		List<PreviewDeviceItem> previewDeviceItemList = new ArrayList<PreviewDeviceItem>();
		File file = new File(filePath);
		if (!file.exists()) {
			return previewDeviceItemList;
		}
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(file);
			Element root = document.getRootElement();
			List<Element> subElements = root.elements();
			int size = subElements.size();
			for (int i = 0; i < size; i++) {
				Element subElement = subElements.get(i);
				PreviewDeviceItem previewDeviceItem = new PreviewDeviceItem();
				String dRName = subElement.attributeValue("dRName");
				String lgPass = subElement.attributeValue("lgPass");
				String lgUser = subElement.attributeValue("lgUser");
				String dSvrIp = subElement.attributeValue("dSvrIp");
				
				String svPort = subElement.attributeValue("svPort");
				String channl = subElement.attributeValue("channl");
				String pageWR = subElement.attributeValue("pageWR");
				String chnlMd = subElement.attributeValue("chnlMd");
				int channel = Integer.valueOf(channl);
				
				previewDeviceItem.setDeviceRecordName(dRName);
				previewDeviceItem.setLoginPass(lgPass);
				previewDeviceItem.setLoginUser(lgUser);
				previewDeviceItem.setSvrIp(dSvrIp);
				previewDeviceItem.setSvrPort(svPort);
				previewDeviceItem.setChannel(channel);
				
				previewDeviceItemList.add(previewDeviceItem);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
			return previewDeviceItemList;
		}
		return previewDeviceItemList;
	}
}