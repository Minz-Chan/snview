package com.starnet.snview.component.audio;

public class AudioCodec {
	static {
		System.loadLibrary("AudioCodec");
    }

	private static native int g711a2pcm(byte[] in, int inLen, byte[] out, int outLen);
	
	public int g711aDecode(byte[] in, int inLen, byte[] out, int outLen) {
		return g711a2pcm(in, inLen, out, outLen);
	}
}
