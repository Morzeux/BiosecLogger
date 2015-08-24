package com.morzeux.bioseclogger.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.morzeux.bioseclogger.R;
import com.morzeux.bioseclogger.logic.HTTPConnector;
import com.morzeux.bioseclogger.logic.PopupBuilder;

/**
 * Task downloads new samples from remote server into external storage.
 * 
 * @author Stefan Smihla
 * @see HTTPConnector.download method
 */
public class DownloadTask extends AsyncTask<String, Void, Boolean> {

	private Activity act;
	private PopupBuilder popup;

	/**
	 * Constructor with activity. Activity is needed to invoke popup.
	 * 
	 * @param act
	 *            activity
	 */
	public DownloadTask(Activity act) {
		this.act = act;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		popup = new PopupBuilder(act, act.getResources().getString(
				R.string.infoDialogTitle), null, act.getResources().getString(
				R.string.closeButtonLabel));

		popup.createLoadingPopup(act.getResources().getString(
				R.string.downloading));
	}

	@Override
	protected Boolean doInBackground(String... params) {
		return HTTPConnector.getInstance(act).download();
	}

	@Override
	protected void onPostExecute(Boolean res) {
		popup.close();
		if (res) {
			new PopupBuilder(act, act.getResources().getString(
					R.string.infoDialogTitle), act.getResources().getString(
					R.string.downloadSuccess), act.getResources().getString(
					R.string.closeButtonLabel)).createClosingPopup();
		} else {
			new PopupBuilder(act, act.getResources().getString(
					R.string.infoDialogTitle), act.getResources().getString(
					R.string.downloadFailed), act.getResources().getString(
					R.string.closeButtonLabel)).createClosingPopup();
		}
	}
}