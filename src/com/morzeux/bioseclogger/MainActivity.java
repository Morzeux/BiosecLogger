package com.morzeux.bioseclogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.morzeux.bioseclogger.logic.ActionMenu;
import com.morzeux.bioseclogger.tasks.SendSampleTask;

/**
 * Main activity for Biosec Logger. Biosec Logger evaluates multifactor
 * authentication. Basic password is extended with keystroke dynamics and
 * hardware sensors. Biosec Logger provides registration and login to collect
 * and evaluates samples, also provides possibility to send and download
 * experimental samples which evaluates biometric solution. In the future
 * evaluated samples will be visualized.
 * 
 * @author Stefan Smihla
 * 
 */
public class MainActivity extends Activity {

	private Activity act;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		act = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
	 * Starts sub-activity from selected button.
	 * 
	 * @param view
	 *            source view
	 */
	public void onClick(View view) {
		Intent intent = null;

		switch (view.getId()) {

		case R.id.feedbackButton:
			intent = new Intent(this, FeedbackActivity.class);
			break;
		case R.id.createTemplateButton:
			intent = new Intent(this, CreateTemplateActivity.class);
			break;
		case R.id.userLoginButton:
			intent = new Intent(this, UserLoginActivity.class);
			break;
		case R.id.sendEmailButton:
			SendSampleTask sampleTask = new SendSampleTask(act, getResources()
					.getString(R.string.sending), getResources().getString(R.string.EMAIL_USERNAME),
					getResources().getString(R.string.EMAIL_PASSWORD));
			sampleTask.prepareEmail(getResources().getString(R.string.EMAIL_ALIAS), 
					getResources().getString(R.string.RECIPIENTS),
					getResources().getString(R.string.SUBJECT), 
					getResources().getString(R.string.BODY));
			sampleTask.execute();

			return;
		case R.id.analyzeButton:
			intent = new Intent(this, AnalyzeActivity.class);
			break;
		default:
			return;
		}

		startActivity(intent);

	}
}
