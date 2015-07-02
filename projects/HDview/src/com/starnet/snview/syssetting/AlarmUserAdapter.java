package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.componet.switchbutton.CheckSwitchButton;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class AlarmUserAdapter extends BaseAdapter{

	private Context ctx;
//	private boolean isAccept;
	private boolean isPushAccept;
	private boolean isShake;
	private boolean isSound;
	private boolean isClickFlag;
	private LayoutInflater flater;
	private List<CloudAccount> tags;
	private List<String> setDelTags;
	private List<HashMap<String, Object>> mData;

	private TextView curTxt;
	private CheckSwitchButton csv_push;
	private AalarmNotifyAdapter alarmNotifyAdapter;
	
	public void setAalarmNotifyAdapter(AalarmNotifyAdapter adapter){
		this.alarmNotifyAdapter = adapter;
	}
	
	public AlarmUserAdapter(Context ctx, List<HashMap<String, Object>> mData, boolean isAccept) {
		this.ctx = ctx;
		this.mData = mData;
		this.isClickFlag = isAccept;
		flater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		tags = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
	}

	@Override
	public int getCount() {
		int size = 0;
		if (mData != null) {
			size = mData.size();
		}
		return size;
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			convertView = flater.inflate(R.layout.alarmnotifyadapter_item, null);		
			csv_push = (CheckSwitchButton) convertView.findViewById(R.id.imgBtn);
			curTxt = (TextView) convertView.findViewById(R.id.alarm_cnt);
			
			if (isClickFlag) {
				curTxt.setText(ctx.getString(R.string.alarm_accept_open));
				csv_push.setChecked(true);
			} else {
				curTxt.setText(ctx.getString(R.string.alarm_accept_off));
				csv_push.setChecked(false);
			}

			SharedPreferences sp = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", 0);
			
			boolean result = sp.getBoolean("isAllAccept", true);
			if(result){
				csv_push.setEnabled(true);
			}else{
				csv_push.setEnabled(false);
			}

			csv_push.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					if(isFastDoubleClick()){
						return;
					}
					
					if((pushServicePrg != null) && (pushServicePrg.isShowing())){
					return;	
					}
					
					boolean isOpen = NetWorkUtils.checkNetConnection(ctx);
					if(!isOpen){
						isClickFlag = !isChecked;
						showToast(ctx.getString(R.string.pushservice_network_notopen));
						notifyDataSetChanged();
						return;
					}
					isClickFlag = isChecked;
					SharedPreferences sps = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", Context.MODE_PRIVATE);
					Editor editor = sps.edit();
					editor.putBoolean("isAccept", isClickFlag);
					editor.commit();
					try{		
						if(isChecked){
							//报警账户接收功能开启
							if((tags==null)||(tags.isEmpty())){
								isClickFlag = !isChecked;
								showToast(ctx.getString(R.string.pushservice_alarmusr_null_open));
								notifyDataSetChanged();
								return;
							}
							curTxt.setText(ctx.getString(R.string.alarm_accept_open));
							csv_push.setChecked(true);
							String setTags = "";
							for(CloudAccount ca : tags){
								String uName = ca.getUsername();
								String pswd = MD5Utils.createMD5(ca.getPassword());
								String temp = uName + pswd;
								if(ca.equals(tags.get(tags.size()-1))){
									setTags += temp;
								}else{
									setTags += temp+",";
								}
							}
							setTagsWithBaiduPushService(setTags);
						}else{
							//报警账户接收功能关闭
							if((tags==null)||(tags.isEmpty())){
								isClickFlag = !isChecked;
								showToast(ctx.getString(R.string.pushservice_alarmusr_null_open));
								notifyDataSetChanged();
								return;
							}
							curTxt.setText(ctx.getString(R.string.alarm_accept_off));
							csv_push.setChecked(false);				
							String setTags = "";
							for(CloudAccount ca : tags){
								String uName = ca.getUsername();
								String pswd = MD5Utils.createMD5(ca.getPassword());
								String temp = uName + pswd;
								if(ca.equals(tags.get(tags.size()-1))){
									setTags += temp;
								}else{
									setTags += temp+",";
								}
							}
							delTagsWithBaiduPushService(setTags);
						}
						notifyDataSetChanged();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			});
		} else if (position == 1) {
			convertView = flater.inflate(R.layout.alarmuseradapter_item, null);
			TextView cnt = (TextView) convertView.findViewById(R.id.pset_cnt);
			HashMap<String, Object> map = mData.get(position);
			String content = map.get("text").toString();
			if (content.length() >= 18) {
				content = content.substring(0, 18) + "...";
			}
			cnt.setText("" + content);
		}
		return convertView;
	}
	
	public boolean isClickFlag() {
		return isClickFlag;
	}

	private long lastClickTime;
	private boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
	
	private void showToast(String text){
		Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
	}
	
	public static boolean isSetOrDelTags = false;//用于标识在AlarmReceiver中是否通知AlarmPushManagerAcitivity
	private boolean isSetTagsFlag = false;
	private ProgressDialog pushServicePrg;
	
	private void openProgressDialogForSetOrDelTags(String title){
		pushServicePrg = ProgressDialog.show(ctx, null,title, true, true);
		pushServicePrg.setCanceledOnTouchOutside(false);
		pushServicePrg.show();
	}
	
    public void closeProgreeDialog(int errorCode){
		
		if((pushServicePrg != null) && (pushServicePrg.isShowing())){
			pushServicePrg.dismiss();
		}
		if(errorCode == 0){
			if(isSetTagsFlag){
				showToast(ctx.getString(R.string.pushservice_settags_success));
			}else{
				showToast(ctx.getString(R.string.pushservice_deltags_success));
			}
		}else{//操作失败是，置回原来的形状
			if(isSetTagsFlag){
				showToast(ctx.getString(R.string.pushservice_settags_failure));
			}else{
				showToast(ctx.getString(R.string.pushservice_deltags_failure));
			}
			csv_push.setChecked(false);
			SharedPreferences sps = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", Context.MODE_PRIVATE);
			Editor editor = sps.edit();
			editor.putBoolean("isAccept", !isClickFlag);
			editor.commit();
		}
	}
	
	public void delTagsWithBaiduPushService(String tags){
		if((tags == null) ||(tags.equals(""))){
			csv_push.setChecked(false);
			showToast(ctx.getString(R.string.pushservice_alarmusr_null_open));
		}else{
			isSetTagsFlag = false;
			isSetOrDelTags = true;
			AalarmNotifyAdapter.isStartOrStopWork = false;
			String title = ctx.getString(R.string.pushservice_alarmusr_deltags);
			openProgressDialogForSetOrDelTags(title);
			List<String>tempTags = new ArrayList<String>();
			tempTags.add(tags);
			PushManager.delTags(ctx.getApplicationContext(), tempTags);
		}
	}
	
	public void setTagsWithBaiduPushService(String tags){
		if((tags == null) || tags.equals("")){
			csv_push.setChecked(false);
			showToast(ctx.getString(R.string.pushservice_alarmusr_null_open));
		}else{
			isSetTagsFlag = true;
			isSetOrDelTags = true;
			AalarmNotifyAdapter.isStartOrStopWork = false;
			String title = ctx.getString(R.string.pushservice_alarmusr_settags);
			openProgressDialogForSetOrDelTags(title);
			List<String>tempTags = new ArrayList<String>();
			tempTags.add(tags);
			PushManager.setTags(ctx.getApplicationContext(), tempTags);
		}
	}

	private int baiduServiceSwicthFlag = -1;
	private static final int OPEN_FLAG = 1;
	private static final int CLOSE_FLAG = 2;
}