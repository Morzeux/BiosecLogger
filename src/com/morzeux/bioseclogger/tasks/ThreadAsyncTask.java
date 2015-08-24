package com.morzeux.bioseclogger.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.morzeux.bioseclogger.thirdParty.ThreadControl;

/**
 * ThreadAsyncTask creates asynchronous thread controller.
 * 
 * @author Stefan Smihla
 * 
 */
public abstract class ThreadAsyncTask extends AsyncTask<Void, Void, Void> {

	protected Activity act;
	protected ThreadControl tControl;

	/**
	 * Construct instance for ThreadAsyncTask.
	 * 
	 * @param act
	 *            activity with thread
	 * @param tControl
	 *            thread controller
	 */
	public ThreadAsyncTask(Activity act, ThreadControl tControl) {
		this.act = act;
		this.tControl = tControl;
	}

}