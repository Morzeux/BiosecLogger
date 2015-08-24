package com.morzeux.bioseclogger.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import biosecLogger.core.UserLoggerManager;

import com.google.gson.stream.JsonReader;
import com.morzeux.bioseclogger.R;

/**
 * HTTPConnector connects application to remote server. Through HTTPConnector
 * Biosec Logger checks, downloads and uploades new samples.
 * 
 * @author Stefan Smihla
 * 
 */
public enum HTTPConnector {
	INSTANCE;

	private HttpClient client;
	private String server;

	/**
	 * Returns initialized instance of HTTPConnector.
	 * 
	 * @return instance of HTTPConnector
	 */
	public static HTTPConnector getInstance(Activity act) {
		INSTANCE.initInstance(act);
		return INSTANCE;
	}

	/**
	 * Initialize instance with basic HTTP client.
	 */
	private void initInstance(Activity act) {
		client = new DefaultHttpClient();
		server = act.getResources().getString(R.string.SERVER_URL);
	}

	/**
	 * Reads HTTP response content.
	 * 
	 * @param input
	 *            input stream
	 * @return response content
	 * @throws IOException
	 *             raises on bad connection
	 */
	private String readContent(InputStream input) throws IOException {
		BufferedReader rd = null;
		StringBuffer content = new StringBuffer();
		String line;

		try {
			rd = new BufferedReader(new InputStreamReader(input));
			while ((line = rd.readLine()) != null) {
				content.append(line);
			}
		} finally {
			if (rd != null){
				rd.close();
			}
		}

		return content.toString();
	}

	/**
	 * Checks date time of directory.
	 * 
	 * @param direct
	 *            name of directory to check
	 * @return formatted date time
	 */
	private String checkDirDate(String direct) {
		File dir = new File(ExtendedStorageHandler.getExternalPath(direct));
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.ENGLISH);
		return dateFormat.format(new Date(dir.lastModified()));
	}

	/**
	 * Checks new version from remote server.
	 * 
	 * @param direct
	 *            directory to be checked
	 * @return true if new version on server else false
	 * @throws IOException
	 *             raises on data error
	 */
	public boolean newVersion(String direct) throws IOException {
		HttpGet request = new HttpGet(server + "/checkversion");
		HttpResponse response = client.execute(request);

		if (response.getStatusLine().getStatusCode() != 200)
			throw new IOException();

		String fileTime = checkDirDate(direct);
		String serverTime = readContent(response.getEntity().getContent());

		return (fileTime.compareTo(serverTime) < 0) ? true : false;
	}

	/**
	 * Validates response content with MD5 checksum.
	 * 
	 * @param checksum
	 *            validation checksum
	 * @param data
	 *            content to validation
	 * @return true if correct else false
	 */
	private boolean validateData(String checksum, String data) {
		return (checksum.equals(new String(Hex.encodeHex(DigestUtils.md5(data
				.getBytes()))))) ? true : false;
	}

	/**
	 * Reads and writes downloaded JSON content.
	 * 
	 * @return downloaded file name
	 * @throws IOException
	 *             raises on corrupted data
	 */
	private String readJsonContent() throws IOException {
		HttpGet request = new HttpGet(server + "/download");
		HttpResponse response = client.execute(request);
		
		String fileName = null;
		String data = null;
		String checksum = null;
		
		JsonReader reader = null;
		try {
			reader = new JsonReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
	
			reader.beginObject();
			while (reader.hasNext()) {
				String key = reader.nextName();
				if (key.equals("checksum")) {
					checksum = reader.nextString();
				} else if (key.equals("filename")) {
					fileName = reader.nextString();
				} else if (key.equals("data")) {
					data = reader.nextString();
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		if (!validateData(checksum, data)) {
			throw new IOException();
		}

		FileOutputStream fileOuputStream = null;
		try {
			fileOuputStream = new FileOutputStream(ExtendedStorageHandler.getExternalPath(fileName));
			fileOuputStream.write(Base64.decodeBase64(data.getBytes()));
		} finally {
			if (fileOuputStream != null){
				fileOuputStream.close();
			}
		}
		

		return fileName;
	}

	/**
	 * Downloads new samples.
	 * 
	 * @return true if correct, false on error
	 */
	public boolean download() {
		try {
			String zipFileName = readJsonContent();
			Zipper.unzip(zipFileName, "");
			Zipper.deleteFile(zipFileName);
			return true;
		} catch (IOException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		} catch (ZipException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return false;
	}

	/**
	 * Prepares JSON data to POST request. Encodes data by base64 and compute
	 * MD5 checksum. Prepared JSON contains data, checksum and filename as
	 * key-value.
	 * 
	 * @param zFileName
	 *            source zip file
	 * @return JSON as string
	 * @throws IOException
	 *             raises on bad file
	 * @throws JSONException
	 *             raises on bad JSON format
	 */
	private String prepareJsonSample(String zFileName) throws IOException,
			JSONException {
		JSONObject json = new JSONObject();

		File file = new File(ExtendedStorageHandler.getExternalPath(zFileName));
		byte[] bFile = new byte[(int) file.length()];

		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			in.read(bFile);
		} finally {
			if (in != null){
				in.close();
			}
		}

		String data = new String(Base64.encodeBase64(bFile));

		json.put("filename", zFileName);
		json.put("data", data);
		json.put("checksum",
				new String(Hex.encodeHex(DigestUtils.md5(data.getBytes()))));

		return json.toString();
	}

	/**
	 * Sends sample to remote server.
	 * 
	 * @param zFileName
	 *            source file name to be sent
	 * @return true if correct (HTTP response 200) else false
	 */
	public boolean sendSample(String zFileName) {
		HttpPost post = new HttpPost(server + "/addsample");

		try {
			String content = prepareJsonSample(zFileName);
			post.setEntity(new StringEntity(content));
			post.addHeader("content-type", "application/json");
			HttpResponse response = client.execute(post);

			if (response.getStatusLine().getStatusCode() == 200) {
				return true;
			}
		} catch (IOException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		} catch (JSONException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return false;
	}
}
