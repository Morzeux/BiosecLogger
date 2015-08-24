package com.morzeux.bioseclogger.logic;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import biosecLogger.core.StorageHandler;
import biosecLogger.core.UserLoggerManager;

/**
 * Extends StorageHandler from biosecLogger.core to be usable in different
 * packages. I do not want to have original StorageHandler as public.
 * 
 * @author Stefan Smihla
 * @see StorageHandler class
 */
public class ExtendedStorageHandler extends StorageHandler {

	/**
	 * Save file to storage.
	 * 
	 * @param filename
	 *            new file name
	 * @param content
	 *            content of file
	 * @param ctx
	 *            if activity context is set, file is saved to internal storage,
	 *            otherwise to external storage
	 * @param b64encode
	 *            if true, content of file is base64 encoded
	 * @return true if file is saved
	 * @throws IOException
	 *             raises when file could not be saved
	 */
	public static boolean saveFile(String filename, String content,
			Context ctx, boolean b64encode) {
		try {
			return StorageHandler.saveFile(filename, content, ctx, b64encode);
		} catch (IOException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Checks if file exists.
	 * 
	 * @param path
	 *            path to file
	 * @return true if exists else false
	 */
	public static Boolean checkFile(String path) {
		return new File(getExternalPath(path)).exists() ? true : false;
	}
	
	/**
	 * Returns absolute path to provided file or directory name.
	 * 
	 * @param fileName
	 *            name of file or directory
	 * @return absolute path to file or directory
	 */
	public static final String getExternalPath(String fileName) {
		return Environment.getExternalStorageDirectory().toString() + "/"
				+ fileName;
	}
}
