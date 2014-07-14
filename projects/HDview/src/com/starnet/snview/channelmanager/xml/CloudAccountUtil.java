package com.starnet.snview.channelmanager.xml;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.syssetting.CloudAccount;

/**
 * 
 * @author zhongxu
 * @Date 2014年7月23日
 * @ClassName CloudAccountUtil.java
 * @Description 封装有关用户从界面获取的信息操作；
 * @Modifier zhongxu
 * @Modify date 2014年7月23日
 * @Modify description TODO
 */
public class CloudAccountUtil {
	/**
	 * 
	 * @author zhongxu
	 * @Date 2014年7月23日
	 * @Description 从用户保存界面获取拥护数据
	 * @return
	 */
	public List<CloudAccount> getCloudAccountInfoFromUI() {
		List<CloudAccount> accoutInfo = new ArrayList<CloudAccount>();
		
		String domain1 = "xy.star-netsecurity.com";
		String port1 = "80";
		String username1 = "jtpt";
		String password1 = "xwrj123";
		CloudAccount cloudAccount1 = new CloudAccount();
		cloudAccount1.setEnabled(false);
		cloudAccount1.setExpanded(false);
		cloudAccount1.setDomain(domain1);
		cloudAccount1.setPassword(password1);
		cloudAccount1.setPort(port1);
		cloudAccount1.setUsername(username1);

//		String domain2 = "xy.star-netsecurity.com";
//		String port2 = "80";
//		String username2 = "why";
//		String password2 = "c123";
//		CloudAccount cloudAccount2 = new CloudAccount();
//		cloudAccount2.setEnabled(false);
//		cloudAccount2.setExpanded(false);
//		cloudAccount2.setDomain(domain2);
//		cloudAccount2.setPassword(password2);
//		cloudAccount2.setPort(port2);
//		cloudAccount2.setUsername(username2);
		
		String domain3 = "xy.star-netsecurity.com";
		String port3 = "80";
		String username3 = "why";
		String password3 = "1";
		CloudAccount cloudAccount3 = new CloudAccount();
		cloudAccount3.setEnabled(false);
		cloudAccount3.setExpanded(false);
		cloudAccount3.setDomain(domain3);
		cloudAccount3.setPassword(password3);
		cloudAccount3.setPort(port3);
		cloudAccount3.setUsername(username3);
		
//		String domain4 = "xy.star-netsecurity.com";
//		String port4 = "80";
//		String username4 = "jtpt";
//		String password4 = "xwrj1";
//		CloudAccount cloudAccount4 = new CloudAccount();
//		cloudAccount4.setEnabled(false);
//		cloudAccount4.setExpanded(false);
//		cloudAccount4.setDomain(domain4);
//		cloudAccount4.setPassword(password4);
//		cloudAccount4.setPort(port4);
//		cloudAccount4.setUsername(username4);
		
//		accoutInfo.add(cloudAccount4);
		accoutInfo.add(cloudAccount1);
//		accoutInfo.add(cloudAccount2);
		accoutInfo.add(cloudAccount3);
		
		return accoutInfo;
	}
}