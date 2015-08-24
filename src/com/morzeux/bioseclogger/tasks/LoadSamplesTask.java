package com.morzeux.bioseclogger.tasks;

import android.os.AsyncTask;
import biosecLogger.charts.Datasets;

import com.morzeux.bioseclogger.R;
import com.morzeux.bioseclogger.SamplesOverviewActivity;
import com.morzeux.bioseclogger.logic.PopupBuilder;

/**
 * Loads samples from source directory as asynchronous task.
 * 
 * @author Stefan Smihla
 * 
 */
public class LoadSamplesTask extends AsyncTask<String, Void, Datasets> {

	private SamplesOverviewActivity act;
	private PopupBuilder popup;

	/**
	 * Constructor with activity. Activity is needed to invoke popup.
	 * 
	 * @param act
	 *            source activity
	 */
	public LoadSamplesTask(SamplesOverviewActivity act) {
		this.act = act;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		popup = new PopupBuilder(act, act.getResources().getString(
				R.string.infoDialogTitle), null, act.getResources().getString(
				R.string.closeButtonLabel));

		popup.createLoadingPopup(act.getResources().getString(
				R.string.loadingSamples));
	}

	@Override
	protected Datasets doInBackground(String... params) {
		return new Datasets(act);
	}

	@Override
	protected void onPostExecute(Datasets dataset) {
		popup.close();
		act.renderData(dataset);
	}
}