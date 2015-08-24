package com.morzeux.bioseclogger;

import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import biosecLogger.charts.ChartBuilder;
import biosecLogger.charts.Datasets;

import com.morzeux.bioseclogger.logic.ActionMenu;
import com.morzeux.bioseclogger.tasks.LoadSamplesTask;

/**
 * Activity is supposed to show individual metrics for individual users as graphs with various settings.
 * 
 * @author Stefan Smihla
 *
 */
public class SamplesOverviewActivity extends Activity {

	private Datasets dataset;
	
	private TextView userLabel;
	private Spinner userDropdown;
	private Spinner characteristicDropdown;
	
	private RadioButton simplePhrase;
	private RadioButton complexPhrase;
	private RadioButton userIndividualRadio;
	private RadioButton averagesRadio;
	private RadioButton deviationsRadio;
	
	private CheckBox grubbsTest;
	private CheckBox experimentalSamples;
	private ChartBuilder chartBuilder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_samples_overview);
		setupActionBar();
		
		dataset = null;
		
		userLabel = (TextView) findViewById(R.id.userLabel);
		userDropdown = (Spinner) findViewById(R.id.userDropdown);
		characteristicDropdown = (Spinner) findViewById(R.id.characteristicDropdown);
		
		simplePhrase = (RadioButton) findViewById(R.id.simplePassRadio);
		complexPhrase = (RadioButton) findViewById(R.id.complexPassRadio);
		userIndividualRadio = (RadioButton) findViewById(R.id.userIndividualRadio);
		averagesRadio = (RadioButton) findViewById(R.id.averagesRadio);
		deviationsRadio = (RadioButton) findViewById(R.id.deviationsRadio);
		
		grubbsTest = (CheckBox) findViewById(R.id.grubbsCleanUpCheckBox);
		experimentalSamples = (CheckBox) findViewById(R.id.experimentalCheckBox);
		chartBuilder = new ChartBuilder(this);
		
		RadioGroup computationType = (RadioGroup) findViewById(R.id.graphComputationType);
		computationType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	    	@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
	    		boolean isChecked = userIndividualRadio.isChecked();
	    		userLabel.setEnabled(isChecked);
	    		userDropdown.setEnabled(isChecked);
	    	}
	    });
		
	    experimentalSamples.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
	    	@Override
	    	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	    		simplePhrase.setEnabled(isChecked);
	    		complexPhrase.setEnabled(isChecked);
	    		
	    		userIndividualRadio.setEnabled(isChecked);
	    		averagesRadio.setEnabled(isChecked);
	    		deviationsRadio.setEnabled(isChecked);
	    		
	    		if (dataset != null) {
	    			renderDropDown(userDropdown, dataset.getUserNames(isChecked));
	    		}
	    	}
	    });
	    
		LoadSamplesTask task = new LoadSamplesTask(this);
		task.execute();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = ActionMenu
				.onOptionsItemSelected(this, item.getItemId());

		if (intent != null) {
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Renders dropdown data.
	 * 
	 * @param dropdown
	 * 				rendered dropdown
	 * @param data
	 * 				data used to fill dropdown
	 */
	public void renderDropDown(Spinner dropdown, String[] data){
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item, data);
		
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dropdown.setAdapter(spinnerArrayAdapter);
	}
	
	/**
	 * Loads data from asynchronous task and sets spinner.
	 * 
	 * @param dataset	returned dataset from asynchronous task
	 */
	public void renderData(Datasets dataset){
		this.dataset = dataset;
		renderDropDown(userDropdown, dataset.getUserNames(true));
	}
	
	/**
	 * Returns formated user's title from dropdown selection.
	 * 
	 * @param userIndex
	 * 				index for userDropdown
	 * @param charIndex
	 * 				index of characteristicDropdown
	 * @return formated title
	 */
	private String getUserTitle(int userIndex, int charIndex){
		String user = userDropdown.getItemAtPosition(userIndex).toString();
		String characteristics = characteristicDropdown.getItemAtPosition(charIndex).toString();
		return String.format(Locale.ENGLISH, "%s %s %s", characteristics, 
				getResources().getString(R.string.forText), user);
	}
	
	/**
	 * Returns formated title from dropdown selection.
	 * 
	 * @param text
	 * 				main title text
	 * @param charIndex
	 * 				index of characteristicDropdown
	 * @return formated title
	 */
	private String getTitle(String text, int charIndex){
		String characteristics = characteristicDropdown.getItemAtPosition(charIndex).toString();
		return String.format(Locale.ENGLISH, "%s - %s", text, characteristics);
	}
	
	/**
	 * Gets Y label from characteristicDropdown selection.
	 * 
	 * @param charIndex
	 * 				index of characteristicDropdown
	 * @return	y label
	 */
	public String getYLabel(int charIndex){
		switch (charIndex) {
			case Datasets.FLYINGTIMES:
				return getResources().getString(R.string.yLabelLineTime);
			case Datasets.ACCELERANCE_X:
			case Datasets.ACCELERANCE_Y:
			case Datasets.ACCELERANCE_Z:
				return getResources().getString(R.string.yLabelLineSpd);
			case Datasets.ORIENTATION_X:
			case Datasets.ORIENTATION_Y:
			case Datasets.ORIENTATION_Z:
				return getResources().getString(R.string.yLabelLineDec);
			default:
				return getResources().getString(R.string.nullString);
		}
	}
	
	/**
	 * Loads data and runs graph activity.
	 * 
	 * @param view	source view
	 */
	public final void onClick(View view) {
		int userIndex = userDropdown.getSelectedItemPosition();
		int charIndex = characteristicDropdown.getSelectedItemPosition();
		
		String title;
		
		if (experimentalSamples.isChecked() && averagesRadio.isChecked()){
			userIndex = Datasets.AVERAGES;
			title = getTitle(getResources().getString(R.string.averagesLabel), charIndex);
		} else if (experimentalSamples.isChecked() && deviationsRadio.isChecked()){
			userIndex = Datasets.DEVIATIONS;
			title = getTitle(getResources().getString(R.string.deviationLabel), charIndex);
		} else {
			title = getUserTitle(userIndex, charIndex);
		}
		
		int userTypes;
		if (!experimentalSamples.isChecked()) {
			userTypes = 0;
		} else if (simplePhrase.isChecked()) {
			userTypes = 1;
		} else {
			userTypes = 2;
		}
		
		List<List<Double>> values = dataset.getExtractedValues(userIndex, charIndex, 
				userTypes, grubbsTest.isChecked());
		
		Intent intent = chartBuilder.buildLineChart(title, 
				getResources().getString(R.string.xLabelLine), 
				getYLabel(charIndex), values);
		
		startActivity(intent);
	}

}
