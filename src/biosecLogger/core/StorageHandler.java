package biosecLogger.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.codec.binary.Base64;

import android.content.Context;
import android.os.Environment;

/**
 * StorageHandler is supposed to handle saving and loading files. Handler also
 * works with internal and external storage.
 * 
 * @author Stefan Smihla
 * 
 */
public class StorageHandler {

	private static final String EXTERNAL_DIRECTORY = "/user_biometrics";

	/**
	 * Checks if external storage is available. Returns integer constant about
	 * its state. 0 if WRITABLE, 1 if READABLE, 2 if UNMOUNTED
	 * 
	 * @return state of environment
	 */
	protected static int checkEnvironment() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return UserLoggerManager.WRITABLE;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return UserLoggerManager.READABLE;
		} else {
			return UserLoggerManager.UNMOUNTED;
		}
	}

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
	protected static boolean saveFile(String filename, String content,
			Context ctx, boolean b64encode) throws IOException {
		FileOutputStream out = null;
		
		try {
			if (ctx != null) {
				out = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
			} else {
				String root = Environment.getExternalStorageDirectory().toString();
				File dir = new File(root + EXTERNAL_DIRECTORY);
				dir.mkdirs();
				File file = new File(dir, filename);
				out = new FileOutputStream(file);
			}
	
			if (b64encode) {
				out.write(Base64.encodeBase64(content.getBytes()));
			} else {
				out.write(content.getBytes());
			}
		} finally {
			if (out != null){
				out.close();
			}
		}

		return true;
	}

	/**
	 * Removes all internal storage data.
	 * 
	 * @param ctx
	 *            activity context
	 */
	protected static void removeDataFiles(Context ctx) {
		for (String file : ctx.fileList()) {
			ctx.deleteFile(file);
		}
	}

	/**
	 * Loads file from external or internal storage.
	 * 
	 * @param filename
	 *            file to be loaded
	 * @param ctx
	 *            if activity context is present, file will be loaded from
	 *            internal storage, otherwise external storage is used
	 * @return content of file
	 * @throws FileNotFoundException
	 *             raises when file is not found
	 * @throws IOException
	 *             raises when couldn't read from file
	 */
	protected static String loadFile(String filename, Context ctx) throws FileNotFoundException, IOException {
		String line;
		StringBuilder template = new StringBuilder();
		FileInputStream in = null;
		BufferedReader br;

		try {
			in = (ctx != null) ? ctx.openFileInput(filename)
					: new FileInputStream(filename);

			br = new BufferedReader(new InputStreamReader(in));
			while ((line = br.readLine()) != null) {
				template.append(line + "\n");
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}

		return template.toString();
	}
}
