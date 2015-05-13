package com.starnet.snview.component.h264;

public class MP4RecorderUtil {
	private static final int audioTimetick = 8000;
	public static int calcAudioDuration(long currFrameTimestamp,
			long preFrameTimestamp, int audioFrameSize) {
		int duration = 0;

		if (preFrameTimestamp == 0) { // 首帧
			duration = audioTimetick / (audioTimetick / audioFrameSize);	// 音频录制1秒相当于8000tick,
																			// 8000/帧率
		} else {
			duration = (int) ((currFrameTimestamp - preFrameTimestamp)
					* audioTimetick / 1000);
		}
		return duration;
	}
	
	private static final int videoTimetick = 90000;
	public static int calcVideoDuration(long currFrameTimestamp,
			long preFrameTimestamp, int framerate) {
		int duration = 0;

		if (preFrameTimestamp == 0) { // 首帧
			duration = videoTimetick / framerate;
		} else {
			duration = (int) ((currFrameTimestamp - preFrameTimestamp)
					* videoTimetick / 1000);  // 视频录制时1s相当于90000tick，此处为1/framerate秒时有多少tick
		}
		return duration;
	}
}
