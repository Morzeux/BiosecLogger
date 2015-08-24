package com.morzeux.bioseclogger.tasks;

import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import biosecLogger.core.UserLoggerManager;

import com.morzeux.bioseclogger.AnalyzeActivity;
import com.morzeux.bioseclogger.R;
import com.morzeux.bioseclogger.logic.HTTPConnector;

/**
 * Checks if new test of samples are available.
 * 
 * @author Stefan Smihla
 * @see HTTPConnector.newVerion method
 */
public class CheckNewVersionTask extends AsyncTask<String, Void, Boolean> {

	private Activity act;

	/**
	 * Constructor with activity. Activity is needed to invoke popup.
	 * 
	 * @param act
	 *            activity
	 */
	public CheckNewVersionTask(AnalyzeActivity act) {
		this.act = act;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			return HTTPConnector.getInstance(act).newVersion(act.getResources().getString(R.string.BIOSEC_DATA));
		} catch (IOException e) {
			/* No Internet Access */
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			((AnalyzeActivity) act).downloadPopup(act.getResources().getString(
					R.string.newSamples));
		}
	}
}