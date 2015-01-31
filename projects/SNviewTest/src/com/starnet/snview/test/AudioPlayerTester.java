package com.starnet.snview.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.starnet.snview.component.audio.AudioPlayer;

import android.media.AudioFormat;
import android.os.Environment;
import android.test.AndroidTestCase;

public class AudioPlayerTester extends AndroidTestCase {
	private static final String TAG = "AudioPlayerTester";

	private AudioPlayer player;
	
	@Override
	protected void setUp() throws Exception {
		player = new AudioPlayer(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, 
				AudioFormat.ENCODING_PCM_16BIT);
		player.init();
	}



	@Override
	protected void tearDown() throws Exception {
		player.release();
	}



	public void testPlay() throws Exception {
		byte[] pcmData = getPCMData();
		int pcmLen = pcmData.length;
		int primePlaySize = player.getPrimePlaySize();
		
		int offset = 0;
		while (offset < pcmLen) {
			player.playAudioTrack(pcmData, offset, primePlaySize);
			offset += primePlaySize;
			
//			Thread.sleep(100);
		}
		
	}
	
	public byte[] getPCMData()
    {
    	File file = new File(Environment.getExternalStorageDirectory()
    			.getAbsolutePath() + "/alaw1.pcm");
    	FileInputStream inStream;
		try {
			inStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		byte[] data_pack = null;
    	if (inStream != null){
    		long size = file.length();
    		data_pack = new byte[(int) size];
    		try {
				inStream.read(data_pack);
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
    		
    	}
    	
    	return data_pack;
    }
}
