package com.starnet.snview.syssetting;

import java.util.HashMap;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.component.SnapshotSound;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class AalarmNotifyAdapter extends BaseAdapter {

	boolean isShake;
	boolean isSound;
	boolean isAllAcc;

	private Context ctx;
	private Vibrator vibrator;
	private LayoutInflater flater;
	private boolean isClickFlagAcc;
	private boolean isClickFlagSha;
	private boolean isClickFlagSou;
	private List<HashMap<String, Object>> mData;

	public AalarmNotifyAdapter(Context ctx,
			List<HashMap<String, Object>> mData, boolean isAllAcc,
			boolean isShake, boolean isSound) {
		this.ctx = ctx;
		this.mData = mData;
		this.isShake = isShake;
		this.isSound = isSound;
		this.isAllAcc = isAllAcc;
		this.isClickFlagSou = isSound;
		this.isClickFlagSha = isShake;
		this.isClickFlagAcc = isAllAcc;

		vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
		flater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mData.size();
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
		if (convertView == null) {
			convertView = flater
					.inflate(R.layout.alarmnotifyadapter_item, null);
		}

		TextView txt = (TextView) convertView.findViewById(R.id.alarm_txt);
		final ImageButton imgBtn = (ImageButton) convertView
				.findViewById(R.id.imgBtn);

		HashMap<String, Object> map = mData.get(position);
		String content = map.get("text").toString();
		txt.setText("" + content);

		final TextView cnt = (TextView) convertView.findViewById(R.id.alarm_cnt);

		final int pos = position;

		if (pos == 0) {
			if (isClickFlagAcc) {
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
				cnt.setText(ctx.getString(R.string.notify_accept_open));
			} else {
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
				cnt.setText(ctx.getString(R.string.notify_accept_off));
			}
		} else if (pos == 1) {
			if (isClickFlagSha) {
				cnt.setText(ctx.getString(R.string.remind_shake_open));
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
			} else {
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
				cnt.setText(ctx.getString(R.string.remind_shake_off));
			}
		} else if (pos == 2) {
			if (isClickFlagSou) {
				cnt.setText(ctx.getString(R.string.remind_sound_open));
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
			} else {
				cnt.setText(ctx.getString(R.string.remind_sound_off));
				imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			}
		}

		imgBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (pos == 0) {
					if (isClickFlagAcc) {
						isClickFlagAcc = false;
						cnt.setText(ctx.getString(R.string.notify_accept_off));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
					} else {
						isClickFlagAcc = true;
						cnt.setText(ctx.getString(R.string.notify_accept_open));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
					}
				} else if (pos == 1) {
					if (isClickFlagSha) {
						isClickFlagSha = false;
						cnt.setText(ctx.getString(R.string.remind_shake_off));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
					} else {
						isClickFlagSha = true;
						long[] pattern = { 50, 200, 50, 200 };
						vibrator.vibrate(pattern, -1);
						cnt.setText(ctx.getString(R.string.remind_shake_open));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
					}
				} else if (pos == 2) {
					if (isClickFlagSou) {
						isClickFlagSou = false;
						cnt.setText(ctx.getString(R.string.remind_sound_off));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
					} else {
						isClickFlagSou = true;
						cnt.setText(ctx.getString(R.string.remind_sound_open));
						imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
						new Thread(new Runnable() {
							@Override
							public void run() {
								SnapshotSound s = new SnapshotSound(ctx);
								s.playPushSetSound();
							}
						}).start();
					}
				}
			}
		});
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
}
