package com.morzeux.bioseclogger.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import biosecLogger.core.OptionsManager;
import biosecLogger.core.UserLoggerManager;
import biosecLogger.exceptions.HoldCounterException;

import com.morzeux.bioseclogger.AboutActivity;
import com.morzeux.bioseclogger.DeviceInfoActivity;
import com.morzeux.bioseclogger.R;
import com.morzeux.bioseclogger.SettingsActivity;

/**
 * ActionMenu provides actions to OptionsMenu and sets OptionsManager from
 * SharedPreferences.
 * 
 * @author Stefan Smihla
 * 
 */
public class ActionMenu {

	private ActionMenu() {
	};

	/**
	 * Runs activity on selected action.
	 * 
	 * @param ctx
	 *            source context
	 * @param itemID
	 *            ID of selected button
	 * @return intent of new activity
	 */
	public static Intent onOptionsItemSelected(Context ctx, int itemID) {
		Intent intent = null;

		switch (itemID) {
		case R.id.aboutButton:
			intent = new Intent(ctx, AboutActivity.class);
			break;
		case R.id.settingsButton:
			intent = new Intent(ctx, SettingsActivity.class);
			break;
		case R.id.deviceInfoButton:
			intent = new Intent(ctx, DeviceInfoActivity.class);
			break;
		}

		return intent;
	}

	/**
	 * Returns value from shared references.
	 * 
	 * @param sp
	 *            instance of shared preferences
	 * @param key
	 *            key in shared preferences
	 * @param def
	 *            default value if shared preference is not present
	 * @return value from shared references
	 */
	private static int getValue(SharedPreferences sp, String key, int def) {
		return Integer.parseInt(sp.getString(key, Integer.toString(def)));
	}

	/**
	 * Sets instance of OptionsManager class from shared preferences.
	 * 
	 * @param sp
	 *            instance of shared preferences
	 * @param oManager
	 *            instance of options manager
	 */
	public static void setOptions(Activity act, SharedPreferences sp, OptionsManager oManager) {
		if (sp.getBoolean("extenalSaving", true)) {
			oManager.allowExternalSaving();
		} else {
			oManager.disableExternalSaving();
		}

		oManager.setTemplateCreateCounter(getValue(sp, "createCount",
				act.getResources().getInteger(R.integer.CREATE_COUNT)));
		try {
			oManager.setTemplateHoldCounter(getValue(sp, "holdCount",
					act.getResources().getInteger(R.integer.HOLD_COUNT)));
		} catch (HoldCounterException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}
	}
}
