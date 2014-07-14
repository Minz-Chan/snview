/*
 * FileName:CloudService.java
 * 
 * Package:com.starsecurity.service
 * 
 * Date:2013-04-18
 * 
 * Copyright: Copyright (c) 2013 Minz.Chan
 */
package com.starnet.snview.channelmanager.xml;

import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

/**
 * @function     功能	  	云台控制接口
 * @author       创建人              肖远东
 * @date        创建日期           2013-04-18
 * @author       修改人              肖远东
 * @date        修改日期           2013-04-18
 * @description 修改说明	          首次增加
 */
public interface CloudService {
	/***
	 * 与云台建立连接
	 * @param domain
	 * @param port
	 * @param username
	 * @param password
	 * @param deviceName
	 * @return
	 * @throws IOException
	 * @throws DocumentException 
	 */
	public Document SendURLPost(String domain,String port,String username,String password,String deviceName) throws IOException, DocumentException; 
	
	/***
	 * 云台返回的XML文件状态信息解析
	 * @param inputStr
	 * @throws DocumentException
	 */
	public String readXmlStatus(Document doc) throws DocumentException; 
	
	/***
	 * 云台返回的平台信息解析
	 * @param inputStr
	 * @throws DocumentException
	 */
	public List<DVRDevice> readXmlDVRDevices(Document doc) throws DocumentException; 
	
	
}
