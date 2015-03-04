package com.starnet.snview.playback;

import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.xml.ConnectionIdentifyTask;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AccountsPlayBackExpanableAdapter extends BaseExpandableListAdapter {

	private Context ctx;
	private List<CloudAccount> users;// 星云账户
	private final int REQ = 0x0005;

	private Handler mHandler;
	private CloudAccount clickUser;
	private DeviceItem clickDItem;
	
	public void setHandler(Handler handler){
		this.mHandler = handler;
	}
	public void setDeviceItem(DeviceItem item){
		this.clickDItem = item;
	}

	public AccountsPlayBackExpanableAdapter(Handler handler,Context ctx,List<CloudAccount> users) {
		this.ctx = ctx;
		this.users = users;
		this.mHandler = handler;
	}

	@Override
	public int getGroupCount() {
		int size = 0;
		if (users != null) {
			size = users.size();
		}
		return size;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int size = 0;
		List<DeviceItem> items = users.get(groupPosition).getDeviceList();
		if (items != null) {
			size = items.size();
		}
		return size;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return users.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (users == null) {
			return null;
		}
		CloudAccount ca = users.get(groupPosition);
		if (ca == null) {
			return null;
		}
		List<DeviceItem> dList = ca.getDeviceList();
		if (dList == null) {
			return null;
		}
		return dList.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(ctx).inflate(R.layout.playback_cloudaccount_preview_item, null);
		}

		ProgressBar prg = (ProgressBar) convertView.findViewById(R.id.prgBar);
		if (users.get(groupPosition).isRotate()) {
			prg.setVisibility(View.GONE);
		}

		TextView txt = (TextView) convertView.findViewById(R.id.account_name);
		txt.setText(users.get(groupPosition).getUsername());
		ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);
		if (users.get(groupPosition).isExpanded()) {
			arrow.setBackgroundResource(R.drawable.channel_listview_down_arrow_sel);
		} else {
			arrow.setBackgroundResource(R.drawable.channel_listview_right_arrow_sel);
		}
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(ctx).inflate(R.layout.playback_deviceitems_act, null);
		}
		List<DeviceItem> list = users.get(groupPosition).getDeviceList();
		TextView txt = (TextView) convertView.findViewById(R.id.channel_name);
		txt.setText(list.get(childPosition).getDeviceName());
		final Button stateBtn = (Button) convertView.findViewById(R.id.stateBtn);
		DeviceItem item = users.get(groupPosition).getDeviceList().get(childPosition);
		boolean isSelected = judgeChannelIsSelected(item);
//		if ((clickDItem != null) && users.get(groupPosition).getDeviceList().get(childPosition).getDeviceName().contains(clickDItem.getDeviceName())) {
		if(isSelected){//  && item.isConnPass() 
			stateBtn.setBackgroundResource(R.drawable.channellist_select_alled);
		} else {
			stateBtn.setBackgroundResource(R.drawable.channellist_select_empty);
		}
		final int group = groupPosition;
		final int child = childPosition;
		stateBtn.setOnClickListener(new OnClickListener() {//考虑收藏设备的联网验证情形
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				clickChild = child;
				clickGroup = group;
				Intent intent = new Intent();
				clickUser = users.get(clickGroup);
				List<DeviceItem> dList = clickUser.getDeviceList();
				clickDItem = dList.get(clickChild);
				intent.putExtra("group", clickGroup);
				intent.putExtra("child", clickChild);
				intent.putExtra("device", clickDItem);
				if (clickGroup == 0) {
					if (clickDItem.isConnPass()) {
						intent.setClass(ctx, PlayBackChannelListViewActivity.class);
						((TimeSettingActivity) ctx).startActivityForResult(intent, REQ);
					}else {//进行联网验证
						((TimeSettingActivity)ctx).showDialog(TimeSettingActivity.CONNECTIDENTIFY_PROGRESSBAR);
						task = new ConnectionIdentifyTask(mHandler,clickUser,clickDItem,clickGroup,clickChild);
						task.setContext(ctx);
						task.setCancel(false);
						task.start();
					}
				}else {
					intent.setClass(ctx, PlayBackChannelListViewActivity.class);
					((TimeSettingActivity) ctx).startActivityForResult(intent, REQ);
				}
			}
		});
		return convertView;
	}
	
	

	private boolean judgeChannelIsSelected(DeviceItem item) {
		boolean isSelected = false;
		if (item != null ) {
			List<Channel> chList = item.getChannelList();
			if (chList!=null&&chList.size()>0) {
				for (Channel channel : chList) {
					if (channel.isSelected()) {
						isSelected = true;
						break;
					}
				}
			}
		}
		return isSelected;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	private ConnectionIdentifyTask task;
	
	public void setCancel(boolean isCancel){
		task.setCanceled(isCancel);
	}
	
//	private class ConnectIdentifyTask extends AsyncTask<Void,String,String>{
//		
//		private DeviceItem devItem;
//		private boolean isCancel;
//		
//		public ConnectIdentifyTask(DeviceItem item){
//			this.devItem = item;
//		};
//		
//		public void setCancel(boolean isCancel){
//			this.isCancel = isCancel;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			super.onPostExecute(result);
//			if (!isCancel) {
//				String ip = devItem.getSvrIp();
//				String port = devItem.getSvrPort();
//				String username = devItem.getLoginUser();
//				String pswd = devItem.getLoginPass();
//				String dName = devItem.getDeviceName();
//				try {
//					Document doc = ReadWriteXmlUtils.SendURLPost(ip, port, username, pswd, dName);
//					String status = ReadWriteXmlUtils.readXmlStatus(doc);
//				} catch (IOException e) {
//					e.printStackTrace();
//				} catch (DocumentException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		@Override
//		protected String doInBackground(Void... params) {
//			if (!isCancel) {//执行后台访问
//				
//			}
//			return null;
//		}
//	};
	
	private ProgressBar prg;
	public void setIdentifyProgressBar(ProgressBar progressBar){
		this.prg = progressBar;
	}

	private static int clickGroup;
	private static int clickChild;
	protected boolean okFlag;

	public void setOkFlag(boolean okFlag) {
		this.okFlag = okFlag;
	}

	public void setGroup(int group) {
		clickGroup = group;
	}

	public void setChild(int child) {
		clickChild = child;
	}
}
