package com.morzeux.bioseclogger;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import com.morzeux.bioseclogger.logic.HardwareInfo;
import com.morzeux.bioseclogger.logic.HardwareInfoTexts;
import com.morzeux.bioseclogger.tasks.UpdateTextsTask;
import com.morzeux.bioseclogger.thirdParty.ThreadControl;

/**
 * Shows information about device hardware. Information are periodically
 * updated.
 * 
 * @author Stefan Smihla
 * 
 */
public class DeviceInfoActivity extends Activity {

	private HardwareInfo hw;
	private HardwareInfoTexts hwt;
	private ThreadControl tControl;
	private UpdateTextsTask updateTexts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_info);
		setupActionBar();
		hw = new HardwareInfo(this.getApplicationContext());
		hwt = new HardwareInfoTexts(this, hw);
		tControl = new ThreadControl();
		updateTexts = new UpdateTextsTask(this, tControl, hwt);

		hwt.setStaticTexts();
		updateTexts.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tControl.cancel();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isFinishing()) {
			tControl.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		tControl.resume();
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
