package com.starnet.snview.syssetting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

public class AlarmAccountsPreviewActivity extends BaseActivity {

	private Context ctx;
	private ListView userListView;
	private List<CloudAccount> mList;
	private AlarmAccountsPreviewAdapter caAdapter;
	private final int ADDINGTCODE = 0x0006;
	private final int REQUESTCODE_ADD = 0x0003;
	private final int REQUESTCODE_EDIT = 0x0004;

	private boolean isAlarmUserAccept;//报警账户接收标记，为true表示开启，false表示不开启
	private boolean isPushServiceAccept;//推送服务接收标记，为true表示开启，false表示不开启
	
	private String tags;
	private List<String> tagList;
	private SharedPreferences spsOfTag;//用于管理tag 标签的SharedPreferences

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_accounts_preview_activity);

		initViews();
		setListeners();

	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmAccountsPreviewActivity.this.finish();
			}
		});

		super.getRightButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(ctx, AlarmAccountsAddingActivity.class);
				// intent.setClass(ctx, AlarmAccountsAddActivity.class);
				startActivityForResult(intent, REQUESTCODE_ADD);
			}
		});

		userListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("pos", position);
				intent.putExtra("cla", mList.get(position));
				intent.setClass(ctx, AlarmAccountsEditingActivity.class);
				startActivityForResult(intent, REQUESTCODE_EDIT);
			}
		});

		userListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				jumpDeleteDialog(position);
				return true;
			}
		});
	}

	//删除的时候，需要注销掉标签
	private void jumpDeleteDialog(int pos) {
		Builder builder = new Builder(ctx);
		String user = mList.get(pos).getUsername();
		String con = getString(R.string.system_setting_alarm_pushset_user_del);
		String ok = getString(R.string.system_setting_alarm_pushset_user_del_ok);
		String ca = getString(R.string.system_setting_alarm_pushset_user_del_cancel);
		builder.setTitle(con + user + " ?");
		builder.setNegativeButton(ca, null);
		final int position = pos;
		builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//xml文档中删除标签,修改文档中的setTags 标签为delTags
				try {
					String userName = mList.get(position).getUsername();
					String paswd = mList.get(position).getPassword();
					String tag = userName + MD5Utils.createMD5(paswd);
					
					for (int i = 0; i < tagList.size(); i++) {
						String tempTag = tagList.get(i);
						if (tempTag.contains(tag)) {
							if (tempTag.contains("setTags")) {
								tempTag = tempTag.replace("setTags", "delTags");
								tagList.set(i, tempTag);
							}
							break;
						}
					}
					//先擦除，再重写
					spsOfTag.edit().clear().commit();				
					String tempTags="";
					int size = tagList.size();
					if (size == 0) {
						tempTags = "";
					}else if (size == 1) {
						tempTags = tagList.get(0);
					}else {
						for (int i = 0;i < size - 1; i++) {
							if (i == 0) {
								tempTags = tagList.get(0)+",";
							}else{
								tempTags = tempTags + tagList.get(i) + ",";
							}
						}
						tempTags = tempTags + tagList.get(size-1);
					}
					spsOfTag.edit().putString("tags", tempTags).commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				new Thread(){
					@Override
					public void run() {
						ReadWriteXmlUtils.deleteAlarmPushUserToXML(position);
					}
				}.start();
								
				mList.remove(position);
				caAdapter.notifyDataSetChanged();
			}
		});
		builder.show();
	}

	protected void deleteTags(CloudAccount clA) {
		try {
			List<String> delTags = new ArrayList<String>();
			delTags.add(clA.getUsername() + "" + MD5Utils.createMD5(clA.getPassword()));
			PushManager.delTags(ctx, delTags);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	protected void showToast(String content) {
		Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
	}

	private void initViews() {

		super.hideExtendButton();
		super.setToolbarVisiable(false);
		ctx = AlarmAccountsPreviewActivity.this;
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.system_setting_alarmuser_preview));
		
		SharedPreferences sps = ctx.getSharedPreferences("", Context.MODE_PRIVATE);
		isAlarmUserAccept = sps.getBoolean("isAllAccept", false);
		isPushServiceAccept = sps.getBoolean("isAllAccept", false);

		mList = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		if (mList == null) {
			mList = new ArrayList<CloudAccount>();
		}
		
		tagList = new ArrayList<String>();
		getTagsList();
		
		caAdapter = new AlarmAccountsPreviewAdapter(ctx, mList);
		userListView = (ListView) findViewById(R.id.userListView);
		userListView.setAdapter(caAdapter);
				
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ADDINGTCODE) {
			if (data != null) {
				CloudAccount ca = (CloudAccount) data.getSerializableExtra("alarmUser");
				boolean cover = data.getBooleanExtra("cover", false);
				if (!cover) {
					ReadWriteXmlUtils.addAlarmPushUserToXML(ca);
					mList.add(ca);
					caAdapter.notifyDataSetChanged();
				} else {
					mList = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
					int index = 0;
					for (int i = 0; i < mList.size(); i++) {
						if (ca.getUsername().equals(mList.get(i).getUsername())) {
							index = i;
							mList.set(i, ca);
							caAdapter.notifyDataSetChanged();
							break;
						}
					}
					ReadWriteXmlUtils.replaceAlarmPushUserToXML(ca, index);
				}
			}
		} else if (resultCode == REQUESTCODE_EDIT) {//youd
			if (data != null) {
				CloudAccount da = (CloudAccount) data.getSerializableExtra("claa");
				da.setEnabled(true);
				int pos = data.getIntExtra("position", 0);
				String flag = data.getStringExtra("flag");
				if (flag != null && flag.equals("cover")) {// 表示需要替代
					boolean cover = data.getBooleanExtra("cover", false);
					if (cover) {// 表示需要覆盖
						int ind = getIndex(da);
						mList.set(ind, da);
						ReadWriteXmlUtils.replaceAlarmPushUserToXML(da, ind);
						mList.remove(pos);
						ReadWriteXmlUtils.deleteAlarmPushUserToXML(pos);
						caAdapter.notifyDataSetChanged();
					}
				} else if (flag == null) {
					mList.set(pos, da);
					caAdapter.notifyDataSetChanged();
				}
			}
		}
		tagList.clear();
		getTagsList();
	}
	/**获取推送标签**/
	private void getTagsList() {
		spsOfTag = ctx.getSharedPreferences("alarmAccounts", Context.MODE_PRIVATE);
		tags = spsOfTag.getString("tags", "");
		if (tags==null || tags.equals("") || tags.length()==0) {
			
		}else{
			String[] tagStrings = tags.split(",");
			for (int i = 0; i < tagStrings.length; i++) {
				tagList.add(tagStrings[i]);
			}
		}
	}

	private int getIndex(CloudAccount da) {
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i).getUsername().equals(da.getUsername())) {
				return i;
			}
		}
		return 0;
	}
}