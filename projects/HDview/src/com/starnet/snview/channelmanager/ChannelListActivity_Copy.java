package com.starnet.snview.channelmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountUtil;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.channelmanager.xml.CloudService;
import com.starnet.snview.channelmanager.xml.CloudServiceImpl;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.channelmanager.xml.NetCloudAccountThread;
import com.starnet.snview.channelmanager.xml.PinyinComparator;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.CloudAccountUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.PinyinComparatorUtils;

/**
 * @author 陈名珍
 * @Date 2014/7/3
 * @ClassName ChannelListActivity.java
 * @Description 主要用于星云账号、账号中的平台内的信息显示;1、显示本地通道列表；2、加载网络的设备列表
 * @Modifier 赵康
 * @Modify date 2014/7/7
 * @Modify description 增加了字段：starUserNameList、starPlatformList
 */

@SuppressLint({ "SdCardPath", "HandlerLeak" })
public class ChannelListActivity_Copy extends BaseActivity {
	
	private static final String TAG = "ChannelListActivity";

	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";

	private ExpandableListView mExpandableListView;
	private CloudAccountXML caXML;

	private List<CloudAccount> cloudAccounts = new ArrayList<CloudAccount>();// 用于网络访问时用户信息的显示(访问前与访问后)；
	private Context curContext;
	private ChannelExpandableListviewAdapter chExpandableListAdapter;
	
	private MyHandler myHandler;
	private CloudService cloudService ;
	private CloudAccount cAccount;
	private MyRunnable myRunnable;
	private MyHandlerThread myHandlerThread;
	
	
	private HandlerThread handlerThread = new HandlerThread(TAG){

		@Override
		public void run() {
			super.run();
			
			Log.i(TAG, "position=,size=");
			Log.i(TAG, "position=,size=");
			
		}
		
	};
	
	
	private class MyHandlerThread extends HandlerThread{

		public MyHandlerThread(String name) {
			super(name);
		}

		@Override
		public void run() {
			super.run();
			Log.i(TAG, "position=,size=");
			Log.i(TAG, "position=,size=");
			
			caXML = new CloudAccountXML();
			cloudAccounts = getCloudAccountInfoFromUI();//获取收藏设备，以及用户信息
			int netSize = cloudAccounts.size();
			NetWorkUtils netWorkUtils = new NetWorkUtils();
			boolean isOpen = netWorkUtils.checkNetConnection(ChannelListActivity_Copy.this);
			if (isOpen) {
//				for (int i = 1; i < netSize; i++) {// 启动线程进行网络访问，每个用户对应着一个线程
//					cAccount = cloudAccounts.get(i);
//					boolean isEnable = cAccount.isEnabled();
//					if(isEnable){
//						cAccount.setRotate(false);
//					}else{
//						cAccount.setRotate(true);
//					}
//					if(isEnable){
//						myHandler = new MyHandler(looper);
//						myRunnable = new MyRunnable(cAccount, String.valueOf(i));
//						myHandler.post(myRunnable);//开启线程...
//						Log.i(TAG, "myHandler start!");
//					}
//				}
			}else {
				String printSentence = getString(R.string.channel_manager_channellistview_netnotopen);
				Toast toast = Toast.makeText(ChannelListActivity_Copy.this, printSentence,Toast.LENGTH_LONG);
				toast.show();
			}
			
		}		
	};

	//有关开启线程的操作；	
	private class MyHandler extends Handler{//处理子线程返回的消息...
		
		public MyHandler(Looper looper ){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String position = data.getString("position");
			String success = data.getString("success");
			CloudAccount myCloudAccount = (CloudAccount) data.getSerializable("myCloudAccount");
			if (success.equals("Yes")) {// 通知ExpandableListView的第position个位置的progressBar不再转动;获取到访问的整个网络数据；			
				int pos = Integer.valueOf(position);	
				ArrayList<DVRDevice> dvrDeviceList = data.getParcelableArrayList("dvrDeviceList");
				int size = dvrDeviceList.size();
				Log.i(TAG, "position="+pos+",size="+size);
				CloudAccountUtils cAUtils = new CloudAccountUtils();
				myCloudAccount = cAUtils.getCloudAccountFromDVRDevice(dvrDeviceList, myCloudAccount);
				myCloudAccount.setRotate(true);
				cloudAccounts.set(pos, myCloudAccount);
			} else {
				int pos = Integer.valueOf(position);
//				CloudAccount cloudAccount = (CloudAccount) data.getSerializable("netCloudAccount");// 取回网络访问数据；
				myCloudAccount.setRotate(false);
				cloudAccounts.set(pos, myCloudAccount);
			}
			chExpandableListAdapter.notifyDataSetChanged();
		}
		
	};
	
	protected class MyRunnable implements Runnable {
		
		private CloudAccount myCloudAccount ;
		private String postion;
		private Message msg;
		
		public MyRunnable(CloudAccount cloudAccount, String postion) {
			super();
			this.myCloudAccount = cloudAccount;
			this.postion = postion;
		}

		@Override
		public void run() {
			msg = new Message();
			String conn_name = "conn1";
			cloudService = new CloudServiceImpl(conn_name);
			String domain = myCloudAccount.getDomain();
			String port = myCloudAccount.getPort();
			String username = myCloudAccount.getUsername();
			String password = myCloudAccount.getPassword();
			try {
				Document document = cloudService.SendURLPost(domain, port, username, password, "");
				String status = cloudService.readXmlStatus(document);
				if (status == null) {
					List<DVRDevice> dvrDeviceList = cloudService.readXmlDVRDevices(document);
					Bundle data = new Bundle();
					data.putString("position", postion);
					data.putString("success", "Yes");
					data.putSerializable("myCloudAccount", myCloudAccount);
					Collections.sort(dvrDeviceList, new PinyinComparatorUtils());
					data.putParcelableArrayList("dvrDeviceList", (ArrayList<? extends Parcelable>) dvrDeviceList);
					msg.setData(data);
					int size = dvrDeviceList.size();
					Log.i(TAG, "size = "+size);
				}else {
					Log.i(TAG, "visit wrong!");
					Bundle data = new Bundle();
					data.putString("position", postion);
					data.putString("success", "No");
					data.putSerializable("myCloudAccount", myCloudAccount);
					msg.setData(data);
				}
			} catch (IOException e) {
				e.printStackTrace();
				Log.i(TAG, "IOException!");
				Bundle data = new Bundle();
				data.putString("position", postion);
				data.putString("success", "No");
				data.putSerializable("myCloudAccount", myCloudAccount);
				msg.setData(data);
			} catch (DocumentException e) {
				e.printStackTrace();
				Log.i(TAG, "DocumentException!");
				Bundle data = new Bundle();
				data.putString("position", postion);
				data.putString("success", "No");
				data.putSerializable("myCloudAccount", myCloudAccount);
				msg.setData(data);
			}
			myHandler.sendMessage(msg);
		}
		
	};	
	
	private void setListenersForWadgets() {	}

	private void initView() {

		super.setTitleViewText(getString(R.string.navigation_title_channel_list));// 设置列表标题名
		super.setToolbarVisiable(false);
		super.hideRightButton();
		super.hideExtendButton();
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);

		curContext = ChannelListActivity_Copy.this;
		mExpandableListView = (ExpandableListView) findViewById(R.id.channel_listview);
		caXML = new CloudAccountXML();		
		
		caXML = new CloudAccountXML();
		cloudAccounts = getCloudAccountInfoFromUI();//获取收藏设备，以及用户信息
		curContext = ChannelListActivity_Copy.this;
		chExpandableListAdapter = new ChannelExpandableListviewAdapter(curContext, cloudAccounts);
		mExpandableListView.setAdapter(chExpandableListAdapter);
		
		File file = new File(CLOUD_ACCOUNT_PATH);
		if (file.exists()) {
			file.delete();
		}
		
		myHandlerThread = new MyHandlerThread(TAG);
		myHandlerThread.start();
		myHandlerThread.run();
		handlerThread.start();
//		Looper looper = myHandlerThread.getLooper();
//		myHandler = new MyHandler(looper);//用于处理消息的Handler
//		Message msg = myHandler.obtainMessage();
//		msg.sendToTarget();
	}

	//从设置界面中获取用户信息
	private List<CloudAccount> getCloudAccountInfoFromUI() {
		CloudAccountUtil caUtil = new CloudAccountUtil();
		List<CloudAccount> accoutInfo = new ArrayList<CloudAccount>();
		accoutInfo = caUtil.getCloudAccountInfoFromUI();
		return accoutInfo;
	}

	//从本地获取设备的通道列表
	public List<CloudAccount> getGroupListFromLocal() {//注意，目前只有一个用户的情况下；从收藏设备中读取账户
		List<CloudAccount> groupList = caXML.readCloudAccountFromXML(CLOUD_ACCOUNT_PATH);
		return groupList;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_listview_activity);
		initView();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setListenersForWadgets();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		myHandlerThread.quit();
//		myHandler.removeCallbacks(myRunnable);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myHandlerThread.quit();
//		myHandler.removeCallbacks(myRunnable);
	}
}