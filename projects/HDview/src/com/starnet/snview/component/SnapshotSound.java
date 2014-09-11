package com.starnet.snview.component;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import com.starnet.snview.R;

public class SnapshotSound implements MediaPlayer.OnCompletionListener {
	private static final String TAG = "SnapshotSound";
	
	private Context mContext;

	public SnapshotSound(Context context) {
		this.mContext = context;
	}

	public void onCompletion(MediaPlayer mp) {
		Log.i(TAG, "onCompletion");
		mp.release();
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