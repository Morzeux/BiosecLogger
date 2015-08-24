package com.morzeux.bioseclogger;

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import biosecLogger.analysis.Analyzer;
import biosecLogger.charts.ChartBuilder;
import biosecLogger.core.OptionsManager;
import biosecLogger.core.UserLoggerManager;

import com.morzeux.bioseclogger.logic.ActionMenu;
import com.morzeux.bioseclogger.logic.ExtendedStorageHandler;
import com.morzeux.bioseclogger.logic.PopupBuilder;
import com.morzeux.bioseclogger.tasks.AnalyserTask;
import com.morzeux.bioseclogger.tasks.CheckNewVersionTask;
import com.morzeux.bioseclogger.tasks.DownloadTask;

/**
 * Activity analyzes experimental samples with different options.
 * 
 * @author Stefan Smihla
 * 
 */
public class AnalyzeActivity extends Activity {

	private Analyzer analyser;
	private OptionsManager oManager;

	private TextView infoText;
	private ProgressBar loadingBar;

	private Spinner algorithmDropdown;
	private EditText nGraphsEditText;
	private EditText pThresholdEditText;
	private EditText sensitivityEditText;

	private Button testButton;
	private Button viewButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analyze);

		oManager = new OptionsManager(this);
		analyser = new Analyzer(oManager);

		infoText = (TextView) findViewById(R.id.resultsLabel);
		loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
		testButton = (Button) findViewById(R.id.testButton);
		viewButton = (Button) findViewById(R.id.viewSamplesButton);
		
		algorithmDropdown = (Spinner) findViewById(R.id.algorithmDropdown);
		algorithmDropdown.setOnItemSelectedListener(new ItemSelector());

		nGraphsEditText = (EditText) findViewById(R.id.nGraphsEditText);
		pThresholdEditText = (EditText) findViewById(R.id.pThresholdEditText);
		sensitivityEditText = (EditText) findViewById(R.id.sensitivityEditText);

		algorithmDropdown.setSelection(oManager.getEvaluationAlgorithm());
		nGraphsEditText.setText(String.format(Locale.ENGLISH, "%d",
				oManager.getGraphs()));
		pThresholdEditText.setText(String.format(Locale.ENGLISH, "%d",
				(int) (oManager.getThresholdP() * 100)));
		sensitivityEditText.setText(String.format(Locale.ENGLISH, "%d",
				(int) (oManager.getSensitivity() * 100)));

		int flag = oManager.getFlag();
		setCheckBox(flag, OptionsManager.FLYINGTIMES, R.id.flyingTimesCheckBox);
		setCheckBox(flag, OptionsManager.ACCELERANCE, R.id.acceleratorCheckBox);
		setCheckBox(flag, OptionsManager.ORIENTATION, R.id.orientationCheckBox);
		setCheckBox(flag, OptionsManager.ERRORRATE, R.id.errorRateCheckBox);
		setCheckBox(flag, OptionsManager.LONGPRESSRATE,
				R.id.substitutionCheckBox);

		if (!ExtendedStorageHandler.checkFile(getResources().getString(R.string.BIOSEC_DATA))) {
			downloadPopup(getResources().getString(R.string.noSamples));
		} else {
			new CheckNewVersionTask(this).execute();
		}
	}

	/**
	 * Updates view on different option selection.
	 * 
	 * @author Stefan Smihla
	 * 
	 */
	private class ItemSelector implements AdapterView.OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			int algorithm = algorithmDropdown.getSelectedItemPosition();

			if (algorithm == OptionsManager.T_TESTS) {
				pThresholdEditText.setEnabled(true);
			} else {
				pThresholdEditText.setEnabled(false);
			}

			oManager.setEvaluationAlgorithm(algorithm);
			sensitivityEditText.setText(String.format(Locale.ENGLISH, "%d",
					(int) (oManager.getSensitivity() * 100)));
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}

	/**
	 * Saves evaluated results to file.
	 */
	public void saveResults() {
		try {
			analyser.saveToFile(getResources().getString(R.string.RESULTS_FILE));
			new PopupBuilder(this, getResources().getString(
					R.string.infoDialogTitle), getResources().getString(
					R.string.fileSaved).replace("XXXX", getResources().getString(R.string.RESULTS_FILE)),
					getResources().getString(R.string.closeButtonLabel))
					.createClosingPopup();
		} catch (IOException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);

			new PopupBuilder(this, getResources().getString(
					R.string.infoDialogTitle), getResources().getString(
					R.string.savingError), getResources().getString(
					R.string.closeButtonLabel)).createClosingPopup();
		}
	}

	/**
	 * Sets checkbox according to flag value.
	 * 
	 * @param flag
	 *            source flag
	 * @param value
	 *            value to evaluate
	 * @param rID
	 *            ID of checkbox
	 */
	private void setCheckBox(int flag, int value, int rID) {
		if ((flag & value) != 0) {
			((CheckBox) findViewById(rID)).setChecked(true);
		} else {
			((CheckBox) findViewById(rID)).setChecked(false);
		}

	}

	/**
	 * Checks if checkbox is checked.
	 * 
	 * @param rID
	 *            ID of checkbox
	 * @return true if checked, otherwise false
	 */
	private boolean checkCheckBox(int rID) {
		return ((CheckBox) findViewById(rID)).isChecked();
	}

	/**
	 * Computes flag from checked checkboxes.
	 * 
	 * @return computed flag
	 */
	private int computeFlag() {
		int flag = 0;

		if (checkCheckBox(R.id.flyingTimesCheckBox))
			flag |= OptionsManager.FLYINGTIMES;
		if (checkCheckBox(R.id.acceleratorCheckBox))
			flag |= OptionsManager.ACCELERANCE;
		if (checkCheckBox(R.id.orientationCheckBox))
			flag |= OptionsManager.ORIENTATION;
		if (checkCheckBox(R.id.errorRateCheckBox))
			flag |= OptionsManager.ERRORRATE;
		if (checkCheckBox(R.id.substitutionCheckBox))
			flag |= OptionsManager.LONGPRESSRATE;

		return flag;
	}

	/**
	 * Starts asynchronous analyze task.
	 */
	public void analyseSamples(){
		if (!ExtendedStorageHandler.checkFile(getResources().getString(R.string.BIOSEC_DATA))) {
			downloadPopup(getResources().getString(R.string.noSamples));
		} else {
			
			oManager.setEvaluationAlgorithm(algorithmDropdown
					.getSelectedItemPosition());
			oManager.setGraphs(Integer.parseInt(nGraphsEditText.getText()
					.toString()));
			oManager.setThresholdP(Double.parseDouble(pThresholdEditText
					.getText().toString()) / 100);
			oManager.setSensitivity(Double.parseDouble(sensitivityEditText
					.getText().toString()) / 100);
			oManager.setFlag(computeFlag());

			oManager.saveSettings(this);

			new AnalyserTask(this).execute(analyser);
		}
	}
	
	/**
	 * Starts action from selected button.
	 * 
	 * @param view
	 *            source view
	 */
	public final void onClick(View view) {

		switch (view.getId()) {
		case R.id.testButton:
			analyseSamples();
			break;
		case R.id.viewSamplesButton:
			startActivity(new Intent(this, SamplesOverviewActivity.class));
			break;
		}
		
		
	}

	/**
	 * Starts download task on popup submission.
	 */
	public void popupSubmitted() {
		new DownloadTask(this).execute();
	}
	
	/**
	 * Shows visualized results.
	 */
	public void showResults() {
		Intent intent = new ChartBuilder(this).buildBarChart(
				getResources().getString(R.string.titleBar),
				getResources().getString(R.string.xLabelBar),
				getResources().getString(R.string.yLabelBar),
				analyser.getResults());
	    startActivity(intent);
	}

	/**
	 * Opens download popup.
	 * 
	 * @param message
	 *            custom content message
	 */
	public void downloadPopup(String message) {
		new PopupBuilder(this, getResources().getString(
				R.string.infoDialogTitle), message, getResources().getString(
				R.string.closeButtonLabel)).createAskPopup(getResources()
				.getString(R.string.download), "popupSubmitted");
	}

	/**
	 * Returns test button. This is used for asynchronous task to access button.
	 * 
	 * @return test button
	 */
	public Button getTestButton() {
		return testButton;
	}
	
	/**
	 * Returns view button. This is used for asynchronous task to access button.
	 * 
	 * @return view button
	 */
	public Button getViewButton() {
		return viewButton;
	}

	/**
	 * Returns progress bar. This is used for asynchronous task to access
	 * button.
	 * 
	 * @return loading bar
	 */
	public ProgressBar getProgressBar() {
		return loadingBar;
	}

	/**
	 * Returns info text for asynchronous task to access.
	 * 
	 * @return info text
	 */
	public TextView getInfoText() {
		return infoText;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.saveResultsButton).setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;

		if (item.getItemId() == R.id.saveResultsButton) {
			saveResults();
		} else {
			intent = ActionMenu.onOptionsItemSelected(this, item.getItemId());
		}

		if (intent != null) {
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}
}
