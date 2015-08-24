package com.morzeux.bioseclogger.logic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

import com.morzeux.bioseclogger.R;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import biosecLogger.core.UserLoggerManager;

/**
 * Class stores information about device hardware.
 * 
 * @author Stefan Smihla
 * 
 */
public class HardwareInfo {

	private Context ctx;
	private ActivityManager activityManager;
	private MemoryInfo mi;
	private IntentFilter ifilter;
	private Intent batteryStatus;

	private String model;
	private String osVersion;
	private long totalInternalSpace;
	private long totalExternalSpace;
	private long totalMemory;
	private long displayWidth;
	private long displayHeight;
	private double displayInch;
	private String cpuType;
	private long cpuFreq;

	/**
	 * Constructs hardware info and initialize information about hardware.
	 * 
	 * @param ctx
	 *            source context
	 */
	public HardwareInfo(Context ctx) {
		this.ctx = ctx;
		this.activityManager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		this.mi = new MemoryInfo();
		this.ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		this.batteryStatus = ctx.registerReceiver(null, ifilter);

		this.model = android.os.Build.MODEL;
		this.osVersion = android.os.Build.VERSION.RELEASE;
		this.totalInternalSpace = storageSpace(true, true);
		this.totalExternalSpace = storageSpace(false, true);
		this.totalMemory = parseTotalMemory();
		this.displayWidth = (long) getDisplay(0);
		this.displayHeight = (long) getDisplay(1);
		this.displayInch = getDisplay(2);
		this.cpuType = parseCpuInfo();
		this.cpuFreq = parseCpuFreq();
	}

	/**
	 * Returns specific line in file.
	 * 
	 * @param file
	 *            source file name
	 * @param line
	 *            specific line in file
	 * @return parsed line from source file
	 * @throws IOException
	 *             raise on file error
	 */
	private String parseFileLine(String file, long line) throws IOException {
		RandomAccessFile reader = null;
		StringBuilder sline = new StringBuilder();
		long cur_line = 0;

		try {
			reader = new RandomAccessFile(file, "r");
			while (cur_line < line) {
				sline.append(reader.readLine());
				cur_line++;
			}
		} finally {
			if (reader != null){
				reader.close();
			}
		}

		return sline.toString();
	}

	/**
	 * Returns device CPU information.
	 * 
	 * @return CPU info
	 */
	private String parseCpuInfo() {
		String info;
		try {
			info = parseFileLine(ctx.getResources().getString(R.string.CPUINFO_FILE), 1);
			return info.split(": ")[1];
		} catch (IOException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return null;
	}

	/**
	 * Returns device CPU frequency.
	 * 
	 * @return CPU frequency
	 */
	private long parseCpuFreq() {
		String info;
		try {
			info = parseFileLine(ctx.getResources().getString(R.string.CPUMAXFREQ_FILE), 1);
			return Long.parseLong(info) / 1000;
		} catch (IOException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return -1;
	}

	/**
	 * Returns device total memory.
	 * 
	 * @return total memory in MB
	 */
	private long parseTotalMemory() {
		String info;
		try {
			info = parseFileLine(ctx.getResources().getString(R.string.MEMINFO_FILE), 1);
			return Long.parseLong(info.replaceAll(ctx.getResources().getString(R.string.REGEX_NONDIGITS), ""))
					/ ctx.getResources().getInteger(R.integer.KB);
		} catch (IOException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return -1;
	}

	/**
	 * Returns available storage space.
	 * 
	 * @param internal
	 *            boolean if internal or external space
	 * @param total
	 *            boolean if total space or available space
	 * @return storage space in MB
	 */
	private long storageSpace(boolean internal, boolean total) {
		StatFs stat;
		long bytes;

		stat = new StatFs(internal ? Environment.getDataDirectory().getPath()
				: Environment.getExternalStorageDirectory().getPath());
		bytes = stat.getBlockSize()
				* (total ? (long) stat.getBlockCount() : (long) stat
						.getAvailableBlocks());

		return bytes / ctx.getResources().getInteger(R.integer.MB);
	}

	/**
	 * Returns device display dimensions in pixels or inches.
	 * 
	 * @param mode
	 *            0 for pixel width, 1 for pixel height, other for diagonal inch
	 * @return display dimension
	 */
	private double getDisplay(int mode) {
		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);

		if (mode == 0) {
			return metrics.widthPixels;
		} else if (mode == 1) {
			return metrics.heightPixels;
		} else {
			double width = Math.pow(metrics.widthPixels / metrics.xdpi, 2);
			double height = Math.pow(metrics.heightPixels / metrics.ydpi, 2);
			return Math.sqrt(width + height);
		}

	}

	/**
	 * Returns device model.
	 * 
	 * @return model of device
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Returns OS version.
	 * 
	 * @return version of OS.
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/**
	 * Returns available storage space.
	 * 
	 * @param internal
	 *            boolean if internal or external space
	 * @param total
	 *            boolean if total space or available space
	 * @return storage space in MB
	 */
	public long getSpace(boolean internal, boolean total) {

		if (total) {
			if (internal) {
				return totalInternalSpace;
			} else {
				return totalExternalSpace;
			}
		} else {
			return storageSpace(internal, total);
		}
	}

	/**
	 * Returns total device memory.
	 * 
	 * @return total device memory
	 */
	public long getTotalMemory() {
		return totalMemory;
	}

	/**
	 * Returns available device memory.
	 * 
	 * @return available device memory
	 */
	public long getFreeMemory() {
		activityManager.getMemoryInfo(mi);
		return mi.availMem / ctx.getResources().getInteger(R.integer.MB);
	}

	/**
	 * Returns device display width in pixels.
	 * 
	 * @return width
	 */
	public long getDisplayWidth() {
		return displayWidth;
	}

	/**
	 * Returns device display height in pixels.
	 * 
	 * @return height
	 */
	public long getDisplayHeight() {
		return displayHeight;
	}

	/**
	 * Returns display diagonal display inch.
	 * 
	 * @return diagional inch
	 */
	public String getDisplayInch() {
		return String.format(Locale.ENGLISH, "%.2f\"", this.displayInch);
	}

	/**
	 * Returns device CPU information.
	 * 
	 * @return CPU info
	 */
	public String getCpuInfo() {
		return cpuType;
	}

	/**
	 * Returns device CPU frequency.
	 * 
	 * @return CPU frequency
	 */
	public long getCpuFreq() {
		return cpuFreq;
	}

	/**
	 * Returns storage space as string.
	 * 
	 * @param internal
	 *            boolean if internal or external
	 * @return space as string
	 */
	public String getSpaceText(boolean internal) {
		return getSpace(internal, false) + " MB / " + getSpace(internal, true)
				+ " MB";
	}

	/**
	 * Returns memory space as string.
	 * 
	 * @return memory as string
	 */
	public String getMemoryText() {
		return getFreeMemory() + " MB / " + getTotalMemory() + " MB";
	}

	/**
	 * Returns display width x height as string.
	 * 
	 * @return display size
	 */
	public String getDisplayText() {
		return getDisplayWidth() + " x " + getDisplayHeight() + " pixels";
	}

	/**
	 * Returns CPU info as string.
	 * 
	 * @return CPU info string
	 */
	public String getCpuText() {
		return getCpuInfo() + " " + getCpuFreq() + " MHz";
	}

	/**
	 * Returns device battery level.
	 * 
	 * @return battery level
	 */
	public int getBatteryLevel() {
		return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	}
}
