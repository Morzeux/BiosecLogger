package biosecLogger.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Controller to work with service. ServiceHandler starts, stops and listen to
 * service used to log pressure. Service handler works on Android API < 14.
 * 
 * @author Stefan Smihla
 * 
 */
public enum ServiceHandler {
	INSTANCE;

	private Context ctx;
	private PressureReceiver pressureReceiver;
	private IntentFilter intentFilter;
	private Intent intent;

	private float pressure;

	/**
	 * Returns initialized instance of ServiceHandler.
	 * 
	 * @param ctx
	 *            activity context
	 * @return instance of ServiceHandler
	 */
	protected static ServiceHandler getInstance(Context ctx) {
		INSTANCE.initInstance(ctx);
		return INSTANCE;
	}

	/**
	 * Initialize instance from beginning.
	 * 
	 * @param ctx
	 *            activity context
	 */
	private void initInstance(Context ctx) {
		this.ctx = ctx;

		pressureReceiver = new PressureReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(LoggerService.SCREEN_TOUCHED);
		intent = new Intent(ctx, LoggerService.class);

		pressure = 0;
	}

	/**
	 * Returns logged pressure.
	 * 
	 * @return pressure
	 */
	protected float getPressure() {
		return pressure;
	}

	/**
	 * Starts service.
	 */
	protected void runService() {
		ctx.registerReceiver(pressureReceiver, intentFilter);
		ctx.startService(intent);
	}

	/**
	 * Stops service.
	 */
	protected void stopService() {
		try {
			ctx.unregisterReceiver(pressureReceiver);
		} catch (IllegalArgumentException e) {
			Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		ctx.stopService(intent);
	}

	/**
	 * Receives pressure from service.
	 */
	private class PressureReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			pressure = arg1.getFloatExtra("pressure", 0);
		}
	}
}
