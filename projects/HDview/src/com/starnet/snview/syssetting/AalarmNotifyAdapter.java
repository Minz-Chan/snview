package com.starnet.snview.syssetting;

import java.util.HashMap;
import java.util.List;
import com.starnet.snview.R;
import com.starnet.snview.component.SnapshotSound;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressLint("ClickableViewAccessibility")
public class AalarmNotifyAdapter extends BaseAdapter {

	private final String TAG = "AalarmNotifyAdapter";

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

	private GestureDetector mGestureDetector;

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

		gestureListener = new FlipOnGestureListener();
		mGestureDetector = new GestureDetector(ctx, gestureListener);
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

	private int curPostion;
	private TextView curTXT;
	private ImageButton curImgBtn;

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

		final TextView cnt = (TextView) convertView
				.findViewById(R.id.alarm_cnt);

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

		imgBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				curTXT = cnt;
				curPostion = pos;
				curImgBtn = imgBtn;
				return mGestureDetector.onTouchEvent(event);
			}
		});

		imgBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (pos == 0) {
				// if (isClickFlagAcc) {
				// isClickFlagAcc = false;
				// cnt.setText(ctx.getString(R.string.notify_accept_off));
				// imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
				// } else {
				// isClickFlagAcc = true;
				// cnt.setText(ctx.getString(R.string.notify_accept_open));
				// imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
				// }
				// } else if (pos == 1) {
				// if (isClickFlagSha) {
				// isClickFlagSha = false;
				// cnt.setText(ctx.getString(R.string.remind_shake_off));
				// imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
				// } else {
				// isClickFlagSha = true;
				// long[] pattern = { 50, 200, 50, 200 };
				// vibrator.vibrate(pattern, -1);
				// cnt.setText(ctx.getString(R.string.remind_shake_open));
				// imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
				// }
				// } else if (pos == 2) {
				// if (isClickFlagSou) {
				// isClickFlagSou = false;
				// cnt.setText(ctx.getString(R.string.remind_sound_off));
				// imgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
				// } else {
				// isClickFlagSou = true;
				// cnt.setText(ctx.getString(R.string.remind_sound_open));
				// imgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
				// new Thread(new Runnable() {
				// @Override
				// public void run() {
				// SnapshotSound s = new SnapshotSound(ctx);
				// s.playPushSetSound();
				// }
				// }).start();
				// }
				// }
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

	private FlipOnGestureListener gestureListener;

	private final class FlipOnGestureListener implements OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			Log.i(TAG, "====onDown====");
			pressDownAction();
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Log.i(TAG, "====onScroll====distanceX：" + distanceX);
			if (distanceX > 0) {//表示左滑关闭
				scrollCloseAction();
			} else {//右滑表示打开，左滑表示
				scrollOpenAction();
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "====onLongPress====");
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.i(TAG, "====onFling====");
			return false;
		}
	}

	private void pressDownAction() {
		if (curPostion == 0) {
			if (isClickFlagAcc) {
				isClickFlagAcc = false;
				curTXT.setText(ctx.getString(R.string.notify_accept_off));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			} else {
				isClickFlagAcc = true;
				curTXT.setText(ctx.getString(R.string.notify_accept_open));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
			}
		} else if (curPostion == 1) {
			if (isClickFlagSha) {
				isClickFlagSha = false;
				curTXT.setText(ctx.getString(R.string.remind_shake_off));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			} else {
				isClickFlagSha = true;
				long[] pattern = { 50, 200, 50, 200 };
				vibrator.vibrate(pattern, -1);
				curTXT.setText(ctx.getString(R.string.remind_shake_open));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
			}
		} else if (curPostion == 2) {
			if (isClickFlagSou) {
				isClickFlagSou = false;
				curTXT.setText(ctx.getString(R.string.remind_sound_off));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			} else {
				isClickFlagSou = true;
				curTXT.setText(ctx.getString(R.string.remind_sound_open));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
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

	private void scrollCloseAction() {
		if (curPostion == 0) {
			if (isClickFlagAcc) {
				isClickFlagAcc = false;
				curTXT.setText(ctx.getString(R.string.notify_accept_off));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			}
		} else if (curPostion == 1) {
			if (isClickFlagSha) {
				isClickFlagSha = false;
				curTXT.setText(ctx.getString(R.string.remind_shake_off));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			}
		} else if (curPostion == 2) {
			if (isClickFlagSou) {
				isClickFlagSou = false;
				curTXT.setText(ctx.getString(R.string.remind_sound_off));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_off);
			}
		}
	}

	private void scrollOpenAction() {
		if (curPostion == 0) {
			if (!isClickFlagAcc){
				isClickFlagAcc = true;
				curTXT.setText(ctx.getString(R.string.notify_accept_open));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
			}
		} else if (curPostion == 1) {
			if (!isClickFlagSha) {
				isClickFlagSha = true;
				long[] pattern = { 50, 200, 50, 200 };
				vibrator.vibrate(pattern, -1);
				curTXT.setText(ctx.getString(R.string.remind_shake_open));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
			}
		} else if (curPostion == 2) {
			if (!isClickFlagSou) {
				isClickFlagSou = true;
				curTXT.setText(ctx.getString(R.string.remind_sound_open));
				curImgBtn.setBackgroundResource(R.drawable.pushset_notify_open);
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
}