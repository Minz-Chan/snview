package com.starnet.snview.channelmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountUtil;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.channelmanager.xml.CloudService;
import com.starnet.snview.channelmanager.xml.CloudServiceImpl;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;

/**
 * @author 陈名珍
 * @Date 2014/7/3
 * @ClassName ChannelListActivity.java
 * @Description 主要用于星云账号、账号中的平台内的信息显示;1、显示本地通道列表；2、加载网络的设备列表
 * @Modifier 赵康
 * @Modify date 2014/7/7
 * @Modify description 增加了字段：starUserNameList、starPlatformList
 */
@SuppressLint("SdCardPath")
public class ChannelListActivity extends BaseActivity {

	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";

	private ExpandableListView mExpandableListView;
	private CloudAccountXML caXML ;
	private CloudAccountUtil cAccountUtil;
	private static int stateSelected = 1;
	
	private List<CloudAccount> groupList = new ArrayList<CloudAccount>();// 保存用户信息的列表，DeviceItem包含用于显示星云账号，用于显示星云账号
	private Context curContext;
	private ChannelExpandableListviewAdapter chExpandableListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_listview_activity);
		initView();
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_channel_list));// 设置列表标题名
		super.setToolbarVisiable(false);
		mExpandableListView = (ExpandableListView) findViewById(R.id.channel_listview);	
		caXML = new CloudAccountXML();		
		//当用户选择了1以后，便是每次打开软件后，都从从网络上读取设备信息；
		//当用户选择了0以后，即用户从此便从上次保存的文档中获取用户信息；根据用户的选择而改变		

		List<CloudAccount>netAccountList = getGroupListFromNet();
		int netSize = netAccountList.size();
		for (int i = 0; i < netSize; i++) {
			groupList.add(netAccountList.get(i));
		}
		
		curContext = ChannelListActivity.this;
		chExpandableListAdapter = new ChannelExpandableListviewAdapter(curContext, groupList);
		mExpandableListView.setAdapter(chExpandableListAdapter);

		mExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,int groupPosition, long id) {
				CloudAccount cloudAccount = (CloudAccount) parent.getExpandableListAdapter().getGroup(groupPosition);// 获取用户账号信息
				if (cloudAccount.isExpanded()) {// 判断列表是否已经展开
					cloudAccount.setExpanded(false);
				} else {
					cloudAccount.setExpanded(true);
				}
				return false;
			}
		});
	}

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description 从网络获取
	 * @return
	 */
	private List<CloudAccount> getGroupListFromNet() {
		List<CloudAccount> groupList = getCloudAccountList();
		int size = groupList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = groupList.get(i);
			caXML.writeNewCloudAccountToXML(cloudAccount, CLOUD_ACCOUNT_PATH);
		}		
		return groupList;
	}

	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description 从本地获取设备的通道列表
	 * @return
	 */
	public List<CloudAccount> getGroupListFromLocal() {//注意，目前只有一个用户的情况下；从收藏设备中读取账户
		List<CloudAccount> groupList = caXML.readCloudAccountFromXML(CLOUD_ACCOUNT_PATH);
		return groupList;
	}

	/**
	 * 
	 * @author ZhaoKang
	 * @Date Jul 7, 2014
	 * @Description TODO :用于从远程服务器端请求星云平台账号名信息
	 * @return 星云平台账号名列表
	 */
	private List<CloudAccount> getCloudAccountList() {		
		return readFromCloudAccountXmlfile();
	}
	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 10, 2014
	 * @Description 从xml文档中获取信息
	 * @return
	 */
	private List<CloudAccount> readFromCloudAccountXmlfile() {
		List<CloudAccount> cloudAccounts = new ArrayList<CloudAccount>();
		List<String>cloudAccountInfo = getCloudAccountInfoFromUI();//从设置界面中获取用户信息		
		//请求星云账号中设备平台的信息
		String conn_name = cloudAccountInfo.get(5);
		CloudService cloudService = new CloudServiceImpl(conn_name);		
		cAccountUtil = new CloudAccountUtil(cloudService, cloudAccountInfo);
		try {
			CloudAccount cloudAccount=cAccountUtil.getCloudAccountFromURL();
			cloudAccounts.add(cloudAccount);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return cloudAccounts;
	}
	/**
	 * 
	 * @author zhaohongxu
	 * @Date Jul 13, 2014
	 * @Description TODO
	 * @return
	 */
	private List<String> getCloudAccountInfoFromUI() {//从设置界面中获取用户信息	
		List<String>accoutInfo = new ArrayList<String>();
		String domain = "xy.star-netsecurity.com";
		String port = "80";
		String username = "jtpt";
		String password ="xwrj123";
		String deviceName = "";
		String conn_name = "conn1";
		accoutInfo.add(domain);
		accoutInfo.add(port);
		accoutInfo.add(username);
		accoutInfo.add(password);
		accoutInfo.add(deviceName);
		accoutInfo.add(conn_name);
		return accoutInfo;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//根据得到的值确定状态框的显示情形,全选、半选或者空选,通知ExpandableListView中状态框的改变
		groupList = caXML.readCloudAccountFromXML(CLOUD_ACCOUNT_PATH);//从文档中获取信息、
		chExpandableListAdapter = new ChannelExpandableListviewAdapter(curContext, groupList);
		mExpandableListView.setAdapter(chExpandableListAdapter);		
	}
}
//从本地文档中获取通道列表，并且组名显示为“收藏设备”