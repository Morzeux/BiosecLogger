package com.morzeux.bioseclogger.tasks;

import net.lingala.zip4j.exception.ZipException;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import biosecLogger.core.UserLoggerManager;

import com.morzeux.bioseclogger.R;
import com.morzeux.bioseclogger.logic.EmailSender;
import com.morzeux.bioseclogger.logic.HTTPConnector;
import com.morzeux.bioseclogger.logic.PopupBuilder;
import com.morzeux.bioseclogger.logic.Zipper;

/**
 * Task sends sample from external storage to remote server and also to email.
 * 
 * @author Stefan Smihla
 * @see HTTPConnector sendSample
 */
public class SendSampleTask extends AsyncTask<String, Void, Boolean> {

	private PopupBuilder popup;
	private Activity act;
	private String labelText;
	private EmailSender emailSender;

	private String alias;
	private String subject;
	private String to;
	private String message;

	/**
	 * Constructor for this task needs activity, gmail password and user name
	 * and popup label text.
	 * 
	 * @param act
	 *            activity where task was invoked
	 * @param labelText
	 *            text for loading popup
	 * @param username
	 *            gmail user name
	 * @param password
	 *            gmail password
	 */
	public SendSampleTask(Activity act, String labelText, String username,
			String password) {
		super();
		this.act = act;
		this.labelText = labelText;
		emailSender = EmailSender.getInstance();
		emailSender.setSession(username, password);
	}

	/**
	 * Sets email values.
	 * 
	 * @param alias
	 *            email alias
	 * @param to
	 *            email receiver
	 * @param subject
	 *            email subject
	 * @param message
	 *            email content message
	 */
	public void prepareEmail(String alias, String to, String subject,
			String message) {
		this.alias = alias;
		this.to = to;
		this.subject = subject;
		this.message = message;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		popup = new PopupBuilder(act, act.getResources().getString(
				R.string.infoDialogTitle), null, act.getResources().getString(
				R.string.closeButtonLabel));
		popup.createLoadingPopup(labelText);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			String zFileName = Zipper.zip(act.getResources().getString(R.string.ATTACHMENT),
					"results" + Zipper.getDate() + ".zip");
			boolean emailSend = emailSender.sendEmail(alias, to, subject,
					message, zFileName);
			boolean serverSend = HTTPConnector.getInstance(act).sendSample(
					zFileName);
			boolean passed = emailSend && serverSend;

			if (passed) {
				Zipper.deleteFile(zFileName);
			}

			return passed;
		} catch (ZipException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		popup.close();
		if (result) {
			new PopupBuilder(act, act.getResources().getString(
					R.string.infoDialogTitle), act.getResources().getString(
					R.string.sendSuccess), act.getResources().getString(
					R.string.closeButtonLabel)).createClosingPopup();
		} else {
			new PopupBuilder(act, act.getResources().getString(
					R.string.infoDialogTitle), act.getResources().getString(
					R.string.sendFailed), act.getResources().getString(
					R.string.closeButtonLabel)).createClosingPopup();
		}
	}
}
