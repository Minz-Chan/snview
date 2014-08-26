package com.starnet.snview.component;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

import com.starnet.snview.R;

public class SnapshotSound implements MediaPlayer.OnCompletionListener {
	private Context mContext;

	public SnapshotSound(Context context) {
		this.mContext = context;
	}

	public void onCompletion(MediaPlayer paramMediaPlayer) {
		paramMediaPlayer.release();
	}

	public void playSound() {
		try {
			MediaPlayer mp = new MediaPlayer();
			mp.setOnCompletionListener(this);
			AssetFileDescriptor afd = this.mContext.getResources()
					.openRawResourceFd(R.raw.paizhao);
			mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			afd.close();
			mp.prepare();
			mp.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/*
 * Location:
 * D:\kuaipan\我的资料\研究生阶段\项目\星网安防\star-security\iVMS-4500\classes_dex2jar.jar
 * Qualified Name: com.mcu.iVMS.component.SnapshotSound JD-Core Version:
 * 0.7.0-SNAPSHOT-20130630
 */