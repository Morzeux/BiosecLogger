package com.morzeux.bioseclogger;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import biosecLogger.core.UserLoggerManager;

import com.morzeux.bioseclogger.logic.ActionMenu;
import com.morzeux.bioseclogger.logic.ExtendedStorageHandler;
import com.morzeux.bioseclogger.logic.HardwareInfo;
import com.morzeux.bioseclogger.logic.PopupBuilder;

/**
 * Feedback activity provides explicitly feedback for volunteers.
 * 
 * @author Stefan Smihla
 * 
 */
public class FeedbackActivity extends Activity {

	private HardwareInfo hw;

	/**
	 * Sets default dropdown selection.
	 * 
	 * @param dropDownID
	 *            ID of dropdown menu
	 * @param position
	 *            default position
	 */
	private void setDropDownDefault(int dropDownID, int position) {
		Spinner dropDown = (Spinner) findViewById(dropDownID);
		dropDown.setSelection(position);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		setupActionBar();

		setDropDownDefault(R.id.emotionDropdown, 2);
		setDropDownDefault(R.id.algorithmDropdown, 2);

		hw = new HardwareInfo(this);
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

	/**
	 * Returns selected item from dropdown.
	 * 
	 * @param dropDownID
	 *            ID of dropdown
	 * @return dropdown selection
	 */
	private String getDropDownItem(int dropDownID) {
		Spinner dropDown = (Spinner) findViewById(dropDownID);
		return dropDown.getSelectedItem().toString();
	}

	/**
	 * Returns string from selected edit text.
	 * 
	 * @param editTextID
	 *            ID of edit text
	 * @return content as string
	 */
	private String getEditTextString(int editTextID) {
		EditText editText = (EditText) findViewById(editTextID);
		return editText.getText().toString();
	}

	/**
	 * Prepares hardware information and feedback as JSON object.
	 * 
	 * @return JSON object as string
	 */
	private String prepareString() {
		JSONObject feedBack = null;

		try {
			feedBack = new JSONObject();
			feedBack.put("model", hw.getModel());
			feedBack.put("os", hw.getOsVersion());
			feedBack.put("cpu", hw.getCpuText());
			feedBack.put("display", hw.getDisplayText());
			feedBack.put("displayDiag", hw.getDisplayInch());

			feedBack.put("internalStorage", hw.getSpace(true, true));
			feedBack.put("externalStorage", hw.getSpace(false, true));
			feedBack.put("memory", hw.getTotalMemory());

			feedBack.put("age", getDropDownItem(R.id.algorithmDropdown));
			feedBack.put("skill", getDropDownItem(R.id.skillDropDown));
			feedBack.put("env", getDropDownItem(R.id.environmentDropdown));
			feedBack.put("hand", getDropDownItem(R.id.handDropdown));
			feedBack.put("message", getEditTextString(R.id.customMessageText));
			feedBack.put("mood", getDropDownItem(R.id.emotionDropdown));
		} catch (JSONException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return feedBack.toString();
	}

	/**
	 * Saves feedback to file.
	 * 
	 * @param view
	 *            source view
	 */
	public void onClick(View view) {
		String text = prepareString();
		if (ExtendedStorageHandler.saveFile(getResources().getString(R.string.FEEDBACK_NAME), 
				text, null, false)) {
			new PopupBuilder(this, getResources().getString(
					R.string.infoDialogTitle), getResources().getString(
					R.string.feedbackSaved), getResources().getString(
					R.string.closeButtonLabel)).createExitingPopup();

		} else {
			new PopupBuilder(this, getResources().getString(
					R.string.infoDialogTitle), getResources().getString(
					R.string.feedbackError), getResources().getString(
					R.string.closeButtonLabel)).createClosingPopup();
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

}
