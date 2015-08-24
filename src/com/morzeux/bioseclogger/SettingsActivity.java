package com.morzeux.bioseclogger;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Settings Activity provides basic layout with application preferences.
 * 
 * @author Stefan Smihla
 * 
 */
public class SettingsActivity extends PreferenceActivity {

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* To ensure compatibility with API < 14 deprecated method is used */
		addPreferencesFromResource(R.xml.preferences);
	}
}