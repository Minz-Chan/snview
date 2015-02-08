package com.starnet.snview.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.starnet.snview.component.audio.AudioCodec;
import com.starnet.snview.component.audio.AudioPlayer;

import android.media.AudioFormat;
import android.os.Environment;
import android.test.AndroidTestCase;

public class AudioCodecTester extends AndroidTestCase {
	private static final String TAG = "AudioCodecTester";

	private AudioPlayer player;
	private AudioCodec audioCodec;
	
	@Override
	protected void setUp() throws Exception {
		player = new AudioPlayer(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, 
				AudioFormat.ENCODING_PCM_16BIT);
		player.init();
		audioCodec = new AudioCodec();
	}



	@Override
	protected void tearDown() throws Exception {
		player.release();
	}



	public void testPlay() throws Exception {
		byte[] alawData = getAlawData();
		byte[] pcmData = new byte[alawData.length*2];
		
		audioCodec.g711aDecode(alawData, alawData.length, pcmData, pcmData.length);
		
		int pcmLen = pcmData.length;
		int primePlaySize = player.getPrimePlaySize();
		
		int offset = 0;
		while (offset < pcmLen) {
			player.playAudioTrack(pcmData, offset, primePlaySize);
			offset += primePlaySize;
		}
		
	}
	
	/*
	public byte[] convertToPCMData(byte[] alawDatas) {
		int size = alawDatas.length;
		byte[] pcmDatas = new byte[size*2];
		int k = 0;
		
		for (k = 0; k < size; k++) {
			short decodedByte = audioCodec.g711ToPCM((char) alawDatas[k]);
			pcmDatas[2*k] = (byte) (decodedByte & 0x00ff);
			pcmDatas[2*k+1] = (byte) ((decodedByte >> 8) & 0x00ff);
		}
		
		return pcmDatas;
	}*/
	
	public byte[] getAlawData()
    {
    	File file = new File(Environment.getExternalStorageDirectory()
    			.getAbsolutePath() + "/alaw1.g711");
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
