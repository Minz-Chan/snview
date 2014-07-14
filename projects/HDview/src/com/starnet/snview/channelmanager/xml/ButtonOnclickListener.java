package com.starnet.snview.channelmanager.xml;

import java.util.List;
import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.channelmanager.ChannelListViewActivity;
import com.starnet.snview.devicemanager.DeviceItem;
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
		//csxml = new CloudAccountXML();
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
			intent.putExtras(bundle);//String pos = "parentPos:"+parentPos+";childPos:"+childPos;//Toast toast = Toast.makeText(context, pos, Toast.LENGTH_SHORT);//toast.show();
			((ChannelListActivity) context).startActivityForResult(intent, 31);
			break;
		case R.id.button_state:
			if ((bs.getState() == "half")||(bs.getState().equals("half"))) {
				state_button.setBackgroundResource(R.drawable.zz_half_select);
				bs.setState("all");					
				//将通道列表的状态写入到指定的XML状态文件中	
				//1、修改某一组中某一个选项的通道列表的信息
				DeviceItem deviceItem = cloudAccountList.get(parentPos).getDeviceList().get(childPos);
				List<Channel> channels = deviceItem.getChannelList();
				int channelSize = channels.size();
				for (int i = 0; i < channelSize; i++) {
					channels.get(i).setSelected(false);
				}
				int size = cloudAccountList.size();
				for (int i = 0; i < size; i++) {
					CloudAccount cloudAccount = cloudAccountList.get(i);
					csxml.writeNewCloudAccountToXML(cloudAccount, CLOUDACCOUNTFILEPATH);
				}
				
//				((ChannelListActivity) context).startActivityForResult(intent, 32);
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
				int size = cloudAccountList.size();
				for (int i = 0; i < size; i++) {
					CloudAccount cloudAccount = cloudAccountList.get(i);
					csxml.writeNewCloudAccountToXML(cloudAccount, CLOUDACCOUNTFILEPATH);
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
				int size = cloudAccountList.size();
				for (int i = 0; i < size; i++) {
					CloudAccount cloudAccount = cloudAccountList.get(i);
					csxml.writeNewCloudAccountToXML(cloudAccount, CLOUDACCOUNTFILEPATH);
				}
			}
		break;
		default:
			break;
		}
	}
}