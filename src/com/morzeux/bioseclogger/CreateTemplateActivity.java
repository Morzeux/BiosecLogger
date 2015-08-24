package com.morzeux.bioseclogger;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import biosecLogger.core.OptionsManager;
import biosecLogger.core.UserLoggerManager;
import biosecLogger.exceptions.ExistingUserException;
import biosecLogger.exceptions.InvalidLoginException;

import com.morzeux.bioseclogger.logic.ActionMenu;
import com.morzeux.bioseclogger.logic.PopupBuilder;

/**
 * Activity simulates registration process. User is tasked to enter password
 * several times and biometric template is creating during registration process.
 * 
 * @author Stefan Smihla
 * 
 */
public class CreateTemplateActivity extends Activity {

	private SharedPreferences sp;
	private EditText usernameText;
	private EditText passwordText;
	private TextView infoText;
	private InputMethodManager imm;

	private UserLoggerManager userManager;
	private OptionsManager oManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_template);
		// Show the Up button in the action bar.
		setupActionBar();

		sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		usernameText = (EditText) findViewById(R.id.usernameText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		infoText = (TextView) findViewById(R.id.templateInfoText);

		oManager = new OptionsManager(this);
		ActionMenu.setOptions(this, sp, oManager);

		userManager = UserLoggerManager.getInstance(this, passwordText,
				oManager);
		userManager.addExternalOnKeyListener(getOnKeyEnterListener());

		if (getResources().getBoolean(R.bool.INTERNAL_REMOVE)) {
			userManager.removeDataFiles();
		}

	}

	/**
	 * Returns listener to enter keyboard. Password is submitted on button or on
	 * enter key.
	 * 
	 * @return OnKeyListener
	 */
	private OnKeyListener getOnKeyEnterListener() {
		return new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					onClick(v);
				}
				return false;
			}
		};
	}

	/**
	 * Sets text as information about registration process.
	 * 
	 * @param string
	 *            text to display
	 * @param color
	 *            color of text
	 */
	private void setInfoText(String string, int color) {
		infoText.setText(string);
		infoText.setTextColor(getResources().getColorStateList(color));
		infoText.setVisibility(View.VISIBLE);
	}

	/**
	 * Returns how many times password was submitted as text.
	 * 
	 * @return submit times text
	 */
	private String getCountText() {
		return userManager.getCounter() + "/"
				+ oManager.getTemplateCreateCounter();
	}

	/**
	 * Returns complete text on correct submit.
	 * 
	 * @return text
	 */
	private String getGoodSubmitText() {
		return getCountText() + " "
				+ getResources().getString(R.string.samplesSaved);
	}

	/**
	 * Returns string from edit text.
	 * 
	 * @param text
	 *            source edit text
	 * @return string in edit text
	 */
	private String getString(EditText text) {
		return String.valueOf(text.getText());
	}

	/**
	 * Validates user's input.
	 * 
	 * @return true if username and password are correctly typed, else false
	 */
	private boolean correctInput() {
		return (!getString(usernameText).equals("") && !getString(passwordText)
				.equals("")) ? true : false;
	}

	/**
	 * Submits user's input as biometric sample. Creates, updates and completes
	 * template.
	 * 
	 * @param view
	 *            source view
	 */
	public final void onClick(View view) {

		if (!oManager.checkExternalSaving()
				|| userManager.checkExternalStorage() == UserLoggerManager.WRITABLE) {
			if (correctInput() == true) {
				try {
					int check = userManager.submitSample(
							getString(usernameText), getString(passwordText));
					if (check == UserLoggerManager.COMPLETED) {
						new PopupBuilder(this, getResources().getString(
								R.string.infoDialogTitle), getResources()
								.getString(R.string.templateCompleteText),
								getResources().getString(R.string.no))
								.createExitingPopup();
						imm.hideSoftInputFromWindow(
								passwordText.getWindowToken(), 0);
						return;
					} else if (check == UserLoggerManager.CREATED
							|| check == UserLoggerManager.UPDATED) {
						setInfoText(getGoodSubmitText(), R.color.goodColor); // correct
																				// submit
					}
				} catch (InvalidLoginException e) {
					setInfoText(
							getResources().getString(
									R.string.wrongPasswordError),
							R.color.badColor);
				} catch (ExistingUserException e) {
					setInfoText(
							getResources()
									.getString(R.string.existingUserError),
							R.color.badColor);
				} catch (IOException e) {
					setInfoText(getResources()
							.getString(R.string.saveDataError),
							R.color.badColor);
				}

			} else {
				setInfoText(
						getResources().getString(R.string.emptyPasswordError),
						R.color.badColor); // if wrong password
			}
		} else if (userManager.checkExternalStorage() == UserLoggerManager.READABLE) { 
			setInfoText(getResources().getString(R.string.readableSdCard),
					R.color.badColor);
		} else {
			setInfoText(getResources().getString(R.string.noSdCard),
					R.color.badColor);
		}

		imm.hideSoftInputFromWindow(passwordText.getWindowToken(), 0);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
	protected void onDestroy() {
		super.onDestroy();
		userManager.stopLogging();
	}

	@Override
	protected void onPause() {
		super.onPause();
		userManager.stopLogging();
	}

	@Override
	protected void onResume() {
		super.onResume();
		userManager.resumeLogging();
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
