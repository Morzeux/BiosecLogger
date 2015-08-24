package com.morzeux.bioseclogger;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.widget.EditText;
import android.widget.TextView;
import biosecLogger.core.OptionsManager;
import biosecLogger.core.UserLoggerManager;
import biosecLogger.exceptions.InvalidLoginException;
import biosecLogger.exceptions.PatternMismatchException;

import com.morzeux.bioseclogger.logic.ActionMenu;
import com.morzeux.bioseclogger.logic.PopupBuilder;

public class UserLoginActivity extends Activity {

	private SharedPreferences sp;
	private EditText usernameText;
	private EditText passwordText;
	private TextView infoText;

	private UserLoggerManager userManager;
	private OptionsManager oManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_login);
		setupActionBar();

		sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		usernameText = (EditText) findViewById(R.id.usernameText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		infoText = (TextView) findViewById(R.id.templateInfoText);

		oManager = new OptionsManager(this);
		ActionMenu.setOptions(this, sp, oManager);

		userManager = UserLoggerManager.getInstance(this, passwordText,
				oManager);
		userManager.addExternalOnKeyListener(getOnKeyEnterListener());

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

	private void setInfoText(String string, int color) {
		infoText.setText(string);
		infoText.setTextColor(getResources().getColorStateList(color));
		infoText.setVisibility(View.VISIBLE);
	}

	private String getString(EditText text) {
		return String.valueOf(text.getText());
	}

	public final void onClick(View view) {

		if (!oManager.checkExternalSaving()
				|| userManager.checkExternalStorage() == UserLoggerManager.WRITABLE) {

			try {
				userManager.submitUser(getString(usernameText),
						getString(passwordText));
				new PopupBuilder(this, getResources().getString(
						R.string.infoDialogTitle), getResources().getString(
						R.string.loginSuccess), getResources().getString(
						R.string.closeButtonLabel)).createExitingPopup();

			} catch (PatternMismatchException e) {
				setInfoText(getResources().getString(R.string.patternMismatch),
						R.color.badColor); // if bad user
			} catch (InvalidLoginException e) {
				setInfoText(
						getResources().getString(R.string.wrongPasswordError),
						R.color.badColor); // if wrong password
			} catch (IOException e) {
				setInfoText(getResources().getString(R.string.openDataError),
						R.color.badColor);
			}

		} else if (userManager.checkExternalStorage() == UserLoggerManager.READABLE) {
			setInfoText(getResources().getString(R.string.readableSdCard),
					R.color.badColor);
		} else {
			setInfoText(getResources().getString(R.string.noSdCard),
					R.color.badColor);
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
