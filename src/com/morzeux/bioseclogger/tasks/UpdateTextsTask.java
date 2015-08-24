package com.morzeux.bioseclogger.tasks;

import android.app.Activity;
import android.util.Log;
import biosecLogger.core.UserLoggerManager;

import com.morzeux.bioseclogger.DeviceInfoActivity;
import com.morzeux.bioseclogger.R;
import com.morzeux.bioseclogger.logic.HardwareInfoTexts;
import com.morzeux.bioseclogger.thirdParty.ThreadControl;

/**
 * Updates ThreadAsyncTask with continual texts updating on DeviceInfoActivity.
 * 
 * @author Stefan Smihla
 * @see DeviceInfoActivity class
 */
public class UpdateTextsTask extends ThreadAsyncTask {

	private HardwareInfoTexts hwt;

	/**
	 * Constructs task from provided activity, thread control and hardware
	 * texts.
	 * 
	 * @param act
	 *            activity
	 * @param tControl
	 *            thread controller
	 * @param hwt
	 *            hardware info texts class
	 */
	public UpdateTextsTask(Activity act, ThreadControl tControl,
			HardwareInfoTexts hwt) {
		super(act, tControl);
		this.hwt = hwt;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			while (true) {

				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						hwt.setDynamicTexts();
					}
				});

				tControl.waitIfPaused();
				if (tControl.isCancelled()) {
					break;
				}

				Thread.sleep(act.getResources().getInteger(R.integer.SLEEP_MILISEC));
			}
		} catch (InterruptedException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return null;
	}
}