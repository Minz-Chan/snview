package com.starnet.snview.syssetting;

import java.util.HashMap;
import java.util.List;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.Utils;
import com.starnet.snview.component.SnapshotSound;
import com.starnet.snview.componet.switchbutton.CheckSwitchButton;
import com.starnet.snview.util.NetWorkUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
public class AalarmNotifyAdapter extends BaseAdapter implements OnCheckedChangeListener{

	private final String TAG = "AalarmNotifyAdapter";

	boolean isShake;
	boolean isSound;
	boolean isAllAcc;

	private Context ctx;
	private String apiKey;
	private Vibrator vibrator;
	private LayoutInflater flater;
	private boolean isClickFlagAcc;
	private boolean isClickFlagSha;
	private boolean isClickFlagSou;
	private HashMap<String, String> mHashmap;
	private List<HashMap<String, Object>> mData;
	
	public AalarmNotifyAdapter(Context ctx,List<HashMap<String, Object>> mData, boolean isAllAcc, boolean isShake, boolean isSound) {
		
		this.ctx = ctx;
		this.mData = mData;
		this.isShake = isShake;
		this.isSound = isSound;
		this.isAllAcc = isAllAcc;
		this.isClickFlagSou = isSound;
		this.isClickFlagSha = isShake;
		this.isClickFlagAcc = isAllAcc;
		vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
		flater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
   public AalarmNotifyAdapter(Context ctx,HashMap<String, String> map, boolean isAllAcc, boolean isShake, boolean isSound) {
		
		this.ctx = ctx;
		this.mHashmap = map;
		this.isShake = isShake;
		this.isSound = isSound;
		this.isAllAcc = isAllAcc;
		this.isClickFlagSou = isSound;
		this.isClickFlagSha = isShake;
		this.isClickFlagAcc = isAllAcc;
		
		apiKey = Utils.getMetaValue(ctx.getApplicationContext(), "api_key");
		vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
		flater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

//	private int curPostion;
	private TextView tv_push;
	private TextView tv_shake;
	private TextView tv_sound;
	private CheckSwitchButton csv_push;
	private CheckSwitchButton csv_shake;
	private CheckSwitchButton csv_sound;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = flater.inflate(R.layout.alarm_notifyadapter_items, null);
		}
		
		tv_push = (TextView) convertView.findViewById(R.id.alarm_cnt);
		csv_push = (CheckSwitchButton) convertView.findViewById(R.id.imgBtn);
		
		tv_shake = (TextView) convertView.findViewById(R.id.tv_alarm_shake);
		csv_shake = (CheckSwitchButton) convertView.findViewById(R.id.csv_shake);

		tv_sound = (TextView) convertView.findViewById(R.id.tv_alarm_sound);
		csv_sound = (CheckSwitchButton) convertView.findViewById(R.id.csv_sound);

		if(isClickFlagAcc){
			csv_push.setChecked(true);
			tv_push.setText(ctx.getString(R.string.notify_accept_open));
			csv_shake.setEnabled(true);
			csv_sound.setEnabled(true);
		}else{
			csv_push.setChecked(false);
			tv_push.setText(ctx.getString(R.string.notify_accept_off));
			csv_shake.setEnabled(false);
			csv_sound.setEnabled(false);
		}
		
		if(isShake){
			csv_shake.setChecked(true);
			tv_shake.setText(ctx.getString(R.string.remind_shake_open));
		}else{
			tv_shake.setText(ctx.getString(R.string.remind_shake_off));
			csv_shake.setChecked(false);
		}
		
		if(isSound){
			csv_sound.setChecked(true);
			tv_sound.setText(ctx.getString(R.string.remind_sound_open));
		}else{
			csv_sound.setChecked(false);
			tv_sound.setText(ctx.getString(R.string.remind_sound_off));
		}
		
		csv_push.setOnCheckedChangeListener(this);
		csv_shake.setOnCheckedChangeListener(this);
		csv_sound.setOnCheckedChangeListener(this);
//		{
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				isSound = isChecked;
//				if(isChecked){
//					String content = ctx.getString(R.string.remind_sound_open);
//					tv_sound.setText(content);
//					new Thread(new Runnable() {
//						@Override
//						public void run() {
//							SnapshotSound s = new SnapshotSound(ctx);
//							s.playPushSetSound();
//						}
//					}).start();				
//				}else{
//					String content = ctx.getString(R.string.remind_sound_off);
//					tv_sound.setText(content);
//				}
//			}
//		});
		
		return convertView;
	}

	public boolean isClickFlagAcc() {
		return isClickFlagAcc;
	}

	public void setClickFlagAcc(boolean isClickFlagAcc) {
		this.isClickFlagAcc = isClickFlagAcc;
	}

	public boolean isClickFlagSha() {
		return isClickFlagSha;
	}

	public void setClickFlagSha(boolean isClickFlagSha) {
		this.isClickFlagSha = isClickFlagSha;
	}

	public boolean isClickFlagSou() {
		return isClickFlagSou;
	}

	public void setClickFlagSou(boolean isClickFlagSou) {
		this.isClickFlagSou = isClickFlagSou;
	}
	
	public static boolean isStartOrStopWork = false;

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {//灰色代表关闭，蓝色代表开启
		Log.i(TAG, "isChecked:" + isChecked);
		switch(buttonView.getId()){
		case R.id.imgBtn:
			Log.i(TAG, "R.id.imgBtn");
			boolean isOpen = NetWorkUtils.checkNetConnection(ctx);
			if(!isOpen){
				showToast(ctx.getString(R.string.pushservice_network_notopen));
				return;
			}
			csv_shake.setEnabled(isChecked);
			csv_sound.setEnabled(isChecked);
			if(!isChecked){//关闭时，则
				isRequestStartWork = false;
				isStartOrStopWork = true;
//				if(!PushManager.isPushEnabled(ctx)){
					PushManager.stopWork(ctx);
					openProgressDialogForPushService(ctx.getString(R.string.system_setting_pushservice_closing));
//				}
			}else{
				isRequestStartWork = true;
				isStartOrStopWork = true;
//				if(PushManager.isPushEnabled(ctx)){
					PushManager.startWork(ctx,PushConstants.LOGIN_TYPE_API_KEY,apiKey);
					openProgressDialogForPushService(ctx.getString(R.string.system_setting_pushservice_openning));
//				}
			}
			//开启加载圈
			break;
		case R.id.csv_shake:
			isShake = isChecked;
			if(isShake){
				tv_shake.setText(ctx.getString(R.string.remind_shake_open));
				long[] pattern = { 50, 200, 50, 200 };
				vibrator.vibrate(pattern, -1);
			}else{
				tv_shake.setText(ctx.getString(R.string.remind_shake_off));
			}
			break;
		case R.id.csv_sound:
			isSound = isChecked;
			if(isSound){
				String content = ctx.getString(R.string.remind_sound_open);
				tv_sound.setText(content);
				new Thread(new Runnable() {
					@Override
					public void run() {
						SnapshotSound s = new SnapshotSound(ctx);
						s.playPushSetSound();
					}
				}).start();				
			}else{
				String content = ctx.getString(R.string.remind_sound_off);
				tv_sound.setText(content);
			}
			break;
		}
		
		//SharedPreference 的读写
		SharedPreferences sps = ctx.getSharedPreferences("ALARM_PUSHSET_FILE", Context.MODE_PRIVATE);
		Editor editor = sps.edit();
		editor.putBoolean("isAllAccept", isAllAcc);
		editor.putBoolean("isShake", isShake);
		editor.putBoolean("isSound", isSound);
		editor.commit();
		
	}
	
	private void showToast(String text){
		Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
	}
	
	private void openProgressDialogForPushService(String title){
		pushServicePrg = ProgressDialog.show(ctx, "",title,  true, false);
		pushServicePrg.show();
	}
	
	private boolean isRequestStartWork ;//标识百度推送服务是否启动
	private ProgressDialog pushServicePrg;
	
	public void closeProgreeDialog(int errorCode){
		
		if((pushServicePrg != null) && (pushServicePrg.isShowing())){
			pushServicePrg.dismiss();
		}
		
		if(errorCode == 0){
			if(isRequestStartWork){
				showToast(ctx.getString(R.string.pushservice_open_success));
			}else{
				showToast(ctx.getString(R.string.pushservice_close_success));
			}
		}else{
			if(isRequestStartWork){
				showToast(ctx.getString(R.string.pushservice_open_failure));
			}else{
				showToast(ctx.getString(R.string.pushservice_close_failure));
			}
			csv_push.setChecked(false);
		}
	}
}