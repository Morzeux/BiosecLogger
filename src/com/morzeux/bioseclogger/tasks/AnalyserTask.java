package com.morzeux.bioseclogger.tasks;

import java.util.Locale;

import android.os.AsyncTask;
import android.view.View;
import biosecLogger.analysis.Analyzer;
import biosecLogger.analysis.Results;

import com.morzeux.bioseclogger.AnalyzeActivity;
import com.morzeux.bioseclogger.R;
import com.morzeux.bioseclogger.logic.PopupBuilder;

/**
 * Tasks analyze samples on background. Shows popup with results at the end.
 * 
 * @author Stefan Smihla
 * 
 */
public class AnalyserTask extends AsyncTask<Analyzer, Void, Results> {

	private AnalyzeActivity act;

	/**
	 * Constructs task with provided source activity.
	 * 
	 * @param act
	 *            source activity
	 */
	public AnalyserTask(AnalyzeActivity act) {
		this.act = act;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		act.getTestButton().setEnabled(false);
		act.getViewButton().setEnabled(false);
		act.getProgressBar().setVisibility(View.VISIBLE);
		act.getInfoText().setText(
				act.getResources().getString(R.string.testing));
	}

	@Override
	protected Results doInBackground(Analyzer... analyser) {
		return analyser[0].run();
	}

	@Override
	protected void onPostExecute(Results res) {
		act.getProgressBar().setVisibility(View.INVISIBLE);
		act.getInfoText().setText("");
		act.getTestButton().setEnabled(true);
		act.getViewButton().setEnabled(true);

		new PopupBuilder(act, 
				act.getResources().getString(R.string.infoDialogTitle), 
				String.format(Locale.getDefault(),
						"%s:\n\tFAR: %.2f%%, FRR: %.2f%%\n%s:\n\tFAR: %.2f%%, FRR: %.2f%%",
						act.getResources().getString(R.string.easyPassword),
						res.getSimpleFar(), res.getSimpleFrr(),
						act.getResources().getString(R.string.strongPassword),
						res.getComplexFar(), res.getComplexFrr()), 
				act.getResources().getString(R.string.closeButtonLabel)).
				createAskPopup(act.getResources().getString(R.string.details), "showResults");
	}
}