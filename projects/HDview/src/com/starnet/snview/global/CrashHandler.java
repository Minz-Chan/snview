package com.starnet.snview.global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.starnet.snview.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";

	private static CrashHandler instance = new CrashHandler();// CrashHandler实例

	private Context context;
	private Thread.UncaughtExceptionHandler defaultHandler;// 系统默认的UncaughtException处理类
	private Map<String, String> info = new HashMap<String, String>();// 用来存储设备信息和异常信息
	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ss");

	private final String CRASH_REPORT_DIRECTORY = "/mnt/sdcard/SNview/crash";

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		this.context = context;
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
		Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
	}

	/**
	 * 当UncaughtException发生时会转入该重写的方法来处理
	 */
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && defaultHandler != null) {
			// 如果自定义的没有处理则让系统默认的异常处理器来处理
			defaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 *            异常信息
	 * @return true 如果处理了该异常信息;否则返回false.
	 */
	public boolean handleException(Throwable ex) {
		if (ex == null)
			return false;
		new Thread() {
			public void run() {
				Looper.prepare();
				Toast.makeText(context,
						context.getString(R.string.global_exception_prompt),
						Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
		// 收集设备参数信息
		collectDeviceInfo(context);
		// 保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param context
	 *            上下文
	 */
	public void collectDeviceInfo(Context context) {
		try {
			PackageManager pm = context.getPackageManager();// 获得包管理器
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				info.put("versionName", versionName);
				info.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		Field[] fields = Build.class.getDeclaredFields();// 反射机制
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				info.put(field.getName(), field.get("").toString());
				Log.d(TAG, field.getName() + ":" + field.get(""));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存日志文件
	 * 
	 * @param ex
	 *            异常信息
	 * @return 文件名
	 */
	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : info.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\r\n");
		}

		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);

		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}

		pw.close();

		String result = writer.toString();
		sb.append(result);

		long timetamp = System.currentTimeMillis();
		String time = format.format(new Date());
		String fileName = "crash-" + time + "-" + timetamp + ".log";
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				File dir = new File(CRASH_REPORT_DIRECTORY);
				Log.i(TAG, dir.toString());
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(new File(dir,
						fileName));
				fos.write(sb.toString().getBytes());
				fos.close();
				return fileName;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
