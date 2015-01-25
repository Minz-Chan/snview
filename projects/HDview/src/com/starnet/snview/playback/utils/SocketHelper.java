package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketHelper {

	private static Socket socket;

	public static Socket getSocketInstance(String host, int port)
			throws UnknownHostException, IOException {
		if (socket == null) {
			socket = new Socket(host, port);
		}
		return socket;
	}

}
