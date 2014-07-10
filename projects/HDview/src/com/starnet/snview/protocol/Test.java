package com.starnet.snview.protocol;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.view.SurfaceView;



public class Test {

	public static void main(String[] args) {
		final Connection c = new Connection("119.86.153.112", 8080);

		c.connect();
		
		
		final Connection c1 = new Connection("119.86.153.112", 8080);
		
		c1.connect();
		
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		c.disconnect();
		c1.disconnect();

		SurfaceView s;
		
//		Runnable task = new Runnable() {   
//		    @Override   
//		    public void run() {   
//		        c.disconnect();   
//		    }   
//		};   
//		
//		Runnable task1 = new Runnable() {   
//		    @Override   
//		    public void run() {   
//		        c1.disconnect();   
//		    }   
//		};   
//   
//		   
//		Executor executor = Executors.newScheduledThreadPool(1);   
//		ScheduledExecutorService scheduler = (ScheduledExecutorService) executor;   
//		scheduler.schedule(task, 10, TimeUnit.SECONDS);
//		scheduler.schedule(task1, 15, TimeUnit.SECONDS);
	}

}
