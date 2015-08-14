package com.qpidnetwork.framework.util;

public class Log {

	/**
	 * 日志级别 <code>
	 * {@link android.util.Log#VERBOSE}
	 * {@link android.util.Log#DEBUG}
	 * {@link android.util.Log#INFO}
	 * {@link android.util.Log#WARN}
	 * {@link android.util.Log#ERROR}
	 * </code>
	 */
	private static int LOG_LEVEL = android.util.Log.DEBUG;

	private static boolean VERBOSE = false;
	private static boolean DEBUG = false;
	private static boolean INFO = false;
	private static boolean WARN = false;
	private static boolean ERROR = false;

	static {
		switch (LOG_LEVEL) {
		case android.util.Log.VERBOSE:
			VERBOSE = true;
		case android.util.Log.DEBUG:
			DEBUG = true;
		case android.util.Log.INFO:
			INFO = true;
		case android.util.Log.WARN:
			WARN = true;
		case android.util.Log.ERROR:
			ERROR = true;
		}
	}
	
	/**
	 * 设置log级别
	 * @param level	log级别（如：android.util.Log.DEBUG等）
	 */
	public static void SetLevel(int level)
	{
		LOG_LEVEL = level;
	}

	public static void v(String tag, String msg, Object... args) {
		if (VERBOSE) {
			android.util.Log.v(tag, String.format(msg, args));
		}
	}

	public static void d(String tag, String msg, Object... args) {
		if (DEBUG) {
			String m = String.format(msg, args);
			// m = (m.length() > 300 ? m.substring(0, 300) : m);
			android.util.Log.d(tag, m);
		}
	}

	public static void i(String tag, String msg, Object... args) {
		if (INFO) {
			android.util.Log.i(tag, String.format(msg, args));
		}
	}

	public static void w(String tag, String msg, Object... args) {
		if (WARN) {
			android.util.Log.w(tag, String.format(msg, args));
		}
	}

	public static void w(String tag, String msg, Throwable tr, Object... args) {
		if (WARN) {
			android.util.Log.w(tag, String.format(msg, args), tr);
		}
	}

	public static void e(String tag, String msg, Object... args) {
		if (ERROR) {
			android.util.Log.e(tag, String.format(msg, args));
		}
	}

	public static void e(String tag, String msg, Throwable tr, Object... args) {
		if (ERROR) {
			android.util.Log.e(tag, String.format(msg, args), tr);
		}
	}
}