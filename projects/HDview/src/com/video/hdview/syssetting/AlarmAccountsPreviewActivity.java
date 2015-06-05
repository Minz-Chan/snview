package com.video.hdview.syssetting;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.baidu.android.pushservice.PushManager;
import com.video.hdview.R;
import com.video.hdview.component.BaseActivity;
import com.video.hdview.util.MD5Utils;
import com.video.hdview.util.NetWorkUtils;
import com.video.hdview.util.ReadWriteXmlUtils;

public class AlarmAccountsPreviewActivity extends BaseActivity {

	private Context ctx;
	private ListView userListView;
	private List<CloudAccount> mList;
	private AlarmAccountsPreviewAdapter caAdapter;
	private final int ADDINGTCODE = 0x0006;
	private final int REQUESTCODE_ADD = 0x0003;
	private final int REQUESTCODE_EDIT = 0x0004;

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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("pos", position);
				intent.putExtra("cla", mList.get(position));
				intent.setClass(ctx, AlarmAccountsEditingActivity.class);
				startActivityForResult(intent, REQUESTCODE_EDIT);
			}
		});

		userListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				jumpDeleteDialog(position);
				return true;
			}
		});
	}

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
				
				new Thread(){
					@Override
					public void run() {
						ReadWriteXmlUtils.deleteAlarmPushUserToXML(position);
					}
					
				}.start();
				
				if (NetWorkUtils.checkNetConnection(ctx)) {
					deleteTags(mList.get(position));
				}
				
				mList.remove(position);
				caAdapter.notifyDataSetChanged();
			}
		});
		builder.show();
	}

	protected void deleteTags(CloudAccount clA) {
		try {
			List<String> delTags = new ArrayList<String>();
			delTags.add(clA.getUsername() + ""
					+ MD5Utils.createMD5(clA.getPassword()));
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

		mList = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		if (mList == null) {
			mList = new ArrayList<CloudAccount>();
		}
		caAdapter = new AlarmAccountsPreviewAdapter(ctx, mList);
		userListView = (ListView) findViewById(R.id.userListView);
		userListView.setAdapter(caAdapter);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == ADDINGTCODE) {
			if (data != null) {
				CloudAccount ca = (CloudAccount) data
						.getSerializableExtra("alarmUser");
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
		} else if (resultCode == REQUESTCODE_ADD) {
			if (data != null) {
				CloudAccount ca = (CloudAccount) data
						.getSerializableExtra("ca");
				ca.setEnabled(true);
				boolean cover = data.getBooleanExtra("cover", false);
				if (!cover) {
					mList.add(ca);
					caAdapter.notifyDataSetChanged();
				} else {
					for (int i = 0; i < mList.size(); i++) {
						if (ca.getUsername().equals(mList.get(i).getUsername())) {
							mList.set(i, ca);
							caAdapter.notifyDataSetChanged();
							break;
						}
					}
				}
			}
		} else if (resultCode == REQUESTCODE_EDIT) {
			if (data != null) {
				CloudAccount da = (CloudAccount) data
						.getSerializableExtra("claa");
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