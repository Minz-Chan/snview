/*
 * FileName:CloundServiceImpl.java
 * 
 * Package:com.starsecurity.service.impl
 * 
 * Date:2013-04-18
 * 
 * Copyright: Copyright (c) 2013 Minz.Chan
 */
package com.starnet.snview.devicemanager;

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
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.channelmanager.xml.MD5Util;


/**
 * @function     功能	  云台控制接口实现类
 * @author       创建人              肖远东
 * @date        创建日期           2013-04-18
 * @author       修改人              陈明珍
 * @date        修改日期           2013-11-09
 * @description 修改说明	          
 *     2013-11-09 加入HttpConnection超时设置（20s） 陈明珍
 */
public class CloudServiceImpl implements CloudService {
	private String conn_name;
	
	public CloudServiceImpl(String conn_name) {
		super();
		this.conn_name = conn_name;
	}

	
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
	public Document SendURLPost(String domain,String port,String username,String password) throws IOException, DocumentException , SocketTimeoutException {
		
			String urlStr;
			URL url;
			HttpURLConnection httpURLConnection;

			urlStr = "http://"+domain+":"+port+"/xml_device-list";   
			url = new URL(urlStr);
			httpURLConnection = (HttpURLConnection) url.openConnection(); //获取连接
			httpURLConnection.setRequestMethod("POST"); //设置请求方法为POST, 也可以为GET
			httpURLConnection.setDoOutput(true);  
			httpURLConnection.setConnectTimeout(20000); // 设置连接超时时间为20s
			String encoded = MD5Util.md5Encode(password);      
			StringBuffer param = new StringBuffer("wu="+username+"&wp="+encoded+"&pn=");  //请求URL的查询参数     
			OutputStream os = httpURLConnection.getOutputStream();   
			os.write(param.toString().getBytes());   
			os.flush();   
			os.close();
			
			InputStream inputStr = httpURLConnection.getInputStream();
			SAXReader sr = new SAXReader();//读取类 
			Document doc = sr.read(inputStr);
			inputStr.close();
			return doc;
	}


	@Override
	public String readXmlStatus(Document doc) throws DocumentException {
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


	@Override
	public List<DVRDevice> readXmlDVRDevices(Document doc)
			throws DocumentException {
		//获取所有的设备节点元素
		List deviceListTemp = null;
		deviceListTemp = doc.selectNodes("//Devices/Device");
		Iterator deviceIter = deviceListTemp.iterator();
		List<DVRDevice> deviceList = new ArrayList();
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