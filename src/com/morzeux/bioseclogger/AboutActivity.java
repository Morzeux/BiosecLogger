package com.morzeux.bioseclogger;

import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * About activity shows basic information about BiosecLogger application.
 * 
 * @author Stefan Smihla
 * 
 */
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		setupActionBar();
		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.loadDataWithBaseURL(null,
				String.format(Locale.ENGLISH, getResources().getString(R.string.WEBVIEW_FORMAT), formatAboutText()),
				"text/html", "utf-8", null);
	}

	private String formatString(String format, String... args){
		return String.format(Locale.ENGLISH, format, (Object[]) args);
	}
	
	/**
	 * Prepare about text as HTML.
	 * 
	 * @return formatted about text
	 */
	private String formatAboutText() {
		Resources res = getResources();
		StringBuilder text = new StringBuilder();

		text.append(formatString("%s %s<br/><br/>", 
				res.getString(R.string.app_name), 
				res.getString(R.string.aboutAppText)));
		text.append(formatString("%s<br/>", 
				res.getString(R.string.app_name)));
		text.append(formatString("%s: %s<br/>", 
				res.getString(R.string.versionText), 
				res.getString(R.string.version)));
		text.append(formatString("%s %s<br/>", 
				res.getString(R.string.copyrightText), 
				res.getString(R.string.myName)));
		text.append(formatString("%s: %s<br/><br/>",
				res.getString(R.string.superVisorLabel),
				res.getString(R.string.mySupervisor)));
		text.append(formatString("%s, %s", 
				res.getString(R.string.myFaculty),
				res.getString(R.string.myUniversity)));

		return text.toString();
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
}
