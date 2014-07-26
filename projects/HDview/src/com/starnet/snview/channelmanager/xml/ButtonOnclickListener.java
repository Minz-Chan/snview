package com.starnet.snview.channelmanager.xml;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.channelmanager.ChannelListViewActivity;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 
 * @author zhaohongxu
 * @Date Jul 12, 2014
 * @ClassName ButtonOnclickListener.java
 * @Description 封装了按钮的单击操作
 * @Modifier zhaohongxu
 * @Modify date Jul 12, 2014
 * @Modify description TODO
 */
public class ButtonOnclickListener implements OnClickListener {
	
	@SuppressLint("SdCardPath")
	private final String CLOUDACCOUNTFILEPATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";
	
	private Context context;//上下文环境
	private int parentPos;//父元素的位置
	private int childPos;//子元素的位置
	
	private Button state_button;
	private ButtonState bs;
	
	private CloudAccountXML csxml;//
	private List<CloudAccount> cloudAccountList;//星云账号信息
	private CloudAccount clickCloudAccount;//星云账号信息
	
	List<PreviewDeviceItem> previewChannelList;
	
	public ButtonOnclickListener(int parentPos, int childPos,Button state_button, ButtonState bs,List<CloudAccount> cloudAccountList) {
		super();
		this.parentPos = parentPos;
		this.childPos = childPos;
		this.state_button = state_button;
		this.bs = bs;
		this.cloudAccountList = cloudAccountList;
		csxml = new CloudAccountXML();
	}

	public ButtonOnclickListener(Button state_button,ButtonState bs,int parentPos, int childPos) {
		super();
		this.parentPos = parentPos;
		this.childPos = childPos;
		this.state_button = state_button;
		this.bs = bs;
		csxml = new CloudAccountXML();
	}

	public ButtonOnclickListener(Context context,int parentPos,int childPos) {
		super();
		this.context = context;
		this.parentPos = parentPos;
		this.childPos = childPos;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_channel_list:
			Intent intent = new Intent(context,ChannelListViewActivity.class);	
			Bundle bundle = new Bundle();
			bundle.putString("groupPosition", String.valueOf(parentPos));
			bundle.putString("childPosition", String.valueOf(childPos));
			
			bundle.putSerializable("clickCloudAccount", clickCloudAccount);
			intent.putExtras(bundle);
			
//			ChannelListViewActivity clva = new ChannelListViewActivity(state_button);
			((ChannelListActivity) context).startActivityForResult(intent, 31);
			break;
		case R.id.button_state:
			if ((bs.getState() == "half")||(bs.getState().equals("half"))) {
				state_button.setBackgroundResource(R.drawable.zz_half_select);
				bs.setState("all");					
				//将通道列表的状态写入到指定的XML状态文件中;1、修改某一组中某一个选项的通道列表的信息
				CloudAccount selectCloudAccount = cloudAccountList.get(parentPos);
				DeviceItem deviceItem = selectCloudAccount.getDeviceList().get(childPos);
				List<Channel> channels = deviceItem.getChannelList();
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(false);
				}			
			}else if ((bs.getState() == "all")||(bs.getState().equals("all"))) {
				state_button.setBackgroundResource(R.drawable.zz_all_select);
				bs.setState("empty");					
				//将通道列表的状态写入到指定的XML状态文件中
				//1、修改某一组中某一个选项的通道列表的信息
				DeviceItem deviceItem = cloudAccountList.get(parentPos).getDeviceList().get(childPos);
				List<Channel> channels = deviceItem.getChannelList();
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(true);
				}				
			}else {					
				state_button.setBackgroundResource(R.drawable.zz_empty_select);
				bs.setState("all");					
				//将通道列表的状态写入到指定的XML状态文件中	
				//1、修改某一组中某一个选项的通道列表的信息
				DeviceItem deviceItem = cloudAccountList.get(parentPos).getDeviceList().get(childPos);
				List<Channel> channels = deviceItem.getChannelList();
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(false);
				}
			}
			Thread thread = new Thread(){//采用多线程操作写操作文件，需要注意，写同步问题。。。。。？？？？
				@Override
				public void run() {
					super.run();
			        csxml.writeNewCloudAccountToXML(cloudAccountList.get(parentPos), CLOUDACCOUNTFILEPATH);
				}
			};
			thread.start();
		    break;
		default:
			break;
		}
	}

	public ButtonOnclickListener(Context context) {
		super();
		this.context = context;
		csxml = new CloudAccountXML();
	}

	public ButtonOnclickListener(Context context2,CloudAccount clickCloudAccount, int groupPosition, int childPosition,Button staButton) {
		this.context = context2;
		csxml = new CloudAccountXML();
		this.clickCloudAccount = clickCloudAccount;
		this.parentPos = groupPosition;
		this.childPos = childPosition;
		this.state_button = staButton;
		}
}