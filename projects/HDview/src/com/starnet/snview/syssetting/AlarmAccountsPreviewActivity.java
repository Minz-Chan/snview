package com.starnet.snview.syssetting;

import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.AlarmSettingUtils;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.MD5Utils;

public class AlarmAccountsPreviewActivity extends BaseActivity {

	private Context ctx;
	private ListView userListView;
	private AlarmAccountsPreviewAdapter caAdapter;
	private final int ADDINGTCODE = 0x0006;
	private final int REQUESTCODE_ADD = 0x0003;
	private final int REQUESTCODE_EDIT = 0x0004;

	private List<String> tagList;
	private AlarmUser clickAlarmUser;
	private SharedPreferences spsOfTag;//用于管理tag 标签的SharedPreferences
	private List<AlarmUser> mAlarmUserList;//保村报警账户的列表
	private AlarmSettingUtils alarmSettingUtils;

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
				startActivityForResult(intent, REQUESTCODE_ADD);
			}
		});

		userListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("pos", position);
				clickAlarmUser = mAlarmUserList.get(position);
				intent.putExtra("cla", clickAlarmUser);
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
		String user = mAlarmUserList.get(pos).getUserName();
		String con = getString(R.string.system_setting_alarm_pushset_user_del);
		String ok = getString(R.string.system_setting_alarm_pushset_user_del_ok);
		String ca = getString(R.string.system_setting_alarm_pushset_user_del_cancel);
		builder.setTitle(con + user + " ?");
		builder.setNegativeButton(ca, null);
		final int position = pos;
		builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//sharedPreference文档中删除标签
				try {
					String userName = mAlarmUserList.get(position).getUserName();
					String paswd = mAlarmUserList.get(position).getPassword();
					String tag = userName + paswd;
					for (int i = 0; i < tagList.size(); i++) {
						String tempTag = tagList.get(i);
						if (tempTag.contains(tag)) {
							tagList.remove(i);
							break;
						}
					}
					//先擦除，再重写
					//spsOfTag.edit().clear().commit();
					alarmSettingUtils.writeAlarmUserToXml(tagList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				mAlarmUserList.remove(position);
				caAdapter.notifyDataSetChanged();
			}
		});
		builder.show();
	}
	
	private void initViews() {

		super.hideExtendButton();
		super.setToolbarVisiable(false);
		ctx = AlarmAccountsPreviewActivity.this;
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.system_setting_alarmuser_preview));
		spsOfTag = ctx.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE);
		
		alarmSettingUtils = AlarmSettingUtils.getInstance();
//		alarmSettingUtils.setContext(ctx);
		tagList = alarmSettingUtils.getAlarmUserTagsWithLength();
		mAlarmUserList = alarmSettingUtils.getAlarmUsers();
		
		caAdapter = new AlarmAccountsPreviewAdapter(ctx, mAlarmUserList);
		userListView = (ListView) findViewById(R.id.userListView);
		userListView.setAdapter(caAdapter);
		
		caAdapter = new AlarmAccountsPreviewAdapter(ctx, mAlarmUserList);
		userListView = (ListView) findViewById(R.id.userListView);
		userListView.setAdapter(caAdapter);
				
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ADDINGTCODE) {
			if (data != null) {
				boolean isCover = data.getBooleanExtra("cover", false);
				AlarmUser user = data.getParcelableExtra("alarmUser");
				if (isCover) {//需要覆盖
					int index = getIndexOfAlarmUser(user);
					mAlarmUserList.set(index, user);
				}else{
					mAlarmUserList.add(user);
				}
				caAdapter.notifyDataSetChanged();
			}
		} else if (resultCode == REQUESTCODE_EDIT) {//youd
			if (data != null) {
				boolean isCover = data.getBooleanExtra("cover", false);
				int position = data.getIntExtra("position", 0);
				AlarmUser user = data.getParcelableExtra("claa");
				if(isCover){//覆盖其他的，删除当前的
					int index = getIndexOfAlarmUser(user);
					mAlarmUserList.set(index, user);//先覆盖
					mAlarmUserList.remove(position);//再移除
				}else{//仅仅修改了单击的用户
					mAlarmUserList.set(position, user);
				}
				caAdapter.notifyDataSetChanged();
				String tags = getNewTags();
				spsOfTag.edit().putString("tags", tags).commit();
			}
		}
	}
	private String getNewTags(){
		String result = "";
		
			int size = mAlarmUserList.size();
			for (int i = 0; i < size-1; i++) {
				AlarmUser user = mAlarmUserList.get(i);
				String userName = user.getUserName();
				String password = user.getPassword();
				
				String tag = userName + password + "|" + userName.length() + ",";
				result += tag;
			}
			AlarmUser user = mAlarmUserList.get(size-1);
			String temp = user.getUserName() + user.getPassword() + "|"+user.getUserName().length();
			result = result + temp;
		
		
		return result;
	}

	private int getIndexOfAlarmUser(AlarmUser user) {
		int index = 0;
		int size = mAlarmUserList.size();
		for (int i = 0; i < size; i++) {
			if (mAlarmUserList.get(i).getUserName().equals(user.getUserName())) {
				index = i;
				break;
			}
		}
		return index;
	}
}