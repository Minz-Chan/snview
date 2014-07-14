package com.starnet.snview.channelmanager;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.syssetting.CloudAccount;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * 
 * @author 赵康
 * @Date Jul 7, 2014
 * @ClassName ChannelListViewActivity.java
 * @Description 显示通道列表的视图
 * @Modifier zhaohongxu
 * @Modify date Jul 7, 2014
 * @Modify description TODO 主要完成通道列表操作的类，包含
 */
@SuppressLint("SdCardPath")
public class ChannelListViewActivity extends Activity {// 被观察的对象

	private final String CLOUDACCOUNTFILEPATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";

	private ChannelListViewAdapter adapter = null;// ListView的适配器
	private ListView myListView = null; // 显示列表listview
	private Context context = null;// 上下文换进

	private List<Channel> channelList;// 通道列表

	private Button button_ok;// 确定按钮

	private Button button_cancel;// 取消按钮

	private CloudAccountXML caxml;
	private List<CloudAccount> cloudAccountList;
	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		channelList = new ArrayList<Channel>();
		cloudAccountList = new ArrayList<CloudAccount>();
		caxml = new CloudAccountXML();
		cloudAccountList = caxml.readCloudAccountFromXML(CLOUDACCOUNTFILEPATH);// 从指定的文件中获取星云账号的全部信息

		intent = getIntent();
		Bundle bundle = intent.getExtras();
//		String groupPosition = bundle.getString("groupPosition");
		String childPosition = bundle.getString("childPosition");
//		int groupPos = Integer.valueOf(groupPosition);
		int childPos = Integer.valueOf(childPosition);
		
		CloudAccount clickCloudAccount = (CloudAccount) bundle.getSerializable("clickCloudAccount");
		
		String domain = clickCloudAccount.getDomain();
		String port = clickCloudAccount.getPort();
		String userName = clickCloudAccount.getUsername();
		String password = clickCloudAccount.getPassword();
		
		System.out.println("domain:"+domain+"port:"+port+"userName:"+userName+"password:"+password);
		
		//判断点击的设备通道列表图标所对应的文档的用户
		int cloudAccountSize = 0;
		int channelSize = 0;
		if (cloudAccountList != null) {
			cloudAccountSize = cloudAccountList.size();
			for (int i = 0; i < cloudAccountSize; i++) {//从中找到与clickCloudAccount相同的类
				CloudAccount cloudAccount = cloudAccountList.get(i);
				
				String clickDomain = clickCloudAccount.getDomain();
				String clickPort = clickCloudAccount.getPort();
				String clickUserName = clickCloudAccount.getUsername();
				String clickPassword = clickCloudAccount.getPassword();
				
				String cDomain = cloudAccount.getDomain();
				String cPort = cloudAccount.getPort();
				String cUsername = cloudAccount.getUsername();
				String cPassword = cloudAccount.getPassword();
				
				if (cDomain.equals(clickDomain)&&(cPort.equals(clickPort))
						&&(cUsername.equals(clickUserName))&&(cPassword.equals(clickPassword))) {
					channelList = cloudAccount.getDeviceList().get(childPos).getChannelList();
					channelSize = channelList.size();
					break;
				}
			}
		} else {
			channelSize = 0;
		}
		// 判断通道数量的多少，如果数量比较多的话，则显示一个固定的界面；如果比较少的话，则根据通道数量的多少来显示界面的大小
		if (channelSize < 11) {
			setContentView(R.layout.channel_listview_channel_layout_other);
		} else {
			setContentView(R.layout.channel_listview_device_layout);// 主要需要改动，添加“确定”按钮,“取消”按钮
		}
		initWadgetsAndAddListeners();
	}

	private void initWadgetsAndAddListeners() {
		context = ChannelListViewActivity.this;
		myListView = (ListView) findViewById(R.id.channel_sublistview);
		button_cancel = (Button) findViewById(R.id.channel_listview_cancel);
		button_ok = (Button) findViewById(R.id.channel_listview_ok);
		adapter = new ChannelListViewAdapter(context, channelList);
		myListView.setAdapter(adapter);

		button_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int size = cloudAccountList.size();
				for (int i = 0; i < size; i++) {
					CloudAccount cloudAccount = cloudAccountList.get(i);
					caxml.writeNewCloudAccountToXML(cloudAccount,CLOUDACCOUNTFILEPATH);// 将选择的通道情况信息写入到XML文档中
				}
				setResult(20, intent);// 新增
				ChannelListViewActivity.this.finish();
			}
		});
		// 为弹出的Item添加点击事件，每次单击的时候，需要从XML状态文件中读取，通道的选择情况来渲染界面；
		// 为listview添加响应事件，并置单击后的图像变化，及通道的选择情况；
		myListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				ImageView img = (ImageView) view.findViewById(R.id.channel_listview_device_item_chkbox);// 方框显示按钮
				Channel channel = (Channel) parent.getItemAtPosition(position);
				if (channel.isSelected()) {
					channel.setSelected(false);
					img.setImageResource(R.drawable.channel_listview_unselected);
				} else {
					channel.setSelected(true);
					img.setImageResource(R.drawable.channel_listview_selected);
				}
			}
		});

		button_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChannelListViewActivity.this.finish();
			}
		});
	}
}