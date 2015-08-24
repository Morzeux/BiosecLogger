package biosecLogger.core;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;

/**
 * Logger is attached to password field and using TextWatcher and
 * SensorEvenTListener log values during login or registration.
 * 
 * @author Stefan Smihla
 * 
 */
public enum Logger implements TextWatcher, SensorEventListener {
	INSTANCE;

	private int apiLevel;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;

	private Display display;
	private ServiceHandler serviceHandler;

	private List<LoggedKey> tempKeys;
	private List<LoggedKey> rowKeys;

	private boolean keyDel;
	private boolean longPress;

	private long backupTime;
	private long curTime;
	
	private double prevAxisX;
	private double prevAxisY;
	private double prevAxisZ;

	private long flyingTime;
	private double pressure;
	private boolean error;

	private double xAxis;
	private double yAxis;
	private double zAxis;

	private double accX;
	private double accY;
	private double accZ;

	/**
	 * Returns initialized instance of Logger.
	 * 
	 * @param ctx
	 *            activity context
	 * @return instance of Logger
	 */
	protected static Logger getInstance(Context ctx) {
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

		sensorManager = (SensorManager) ctx
				.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		apiLevel = android.os.Build.VERSION.SDK_INT;

		serviceHandler = ServiceHandler.getInstance(ctx);

		rowKeys = null;
		initValues();
		startLogging();
	}

	/**
	 * Initialize values on create or submit.
	 */
	protected void initValues() {
		tempKeys = new ArrayList<LoggedKey>();
		backupTime = 0;
		curTime = 0;
		keyDel = false;

		longPress = false;
		error = false;

		flyingTime = 0;
		pressure = 0;

		prevAxisX = xAxis = 0;
		prevAxisY = yAxis = 0;
		prevAxisZ = zAxis = 0;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		/* This code works good, refactoring could be dangerous */
		if (count == 1) {

			if (curTime != 0) {
				/* When not first key */

				if (longPress) {
					/* Remove key on long press when specific key is substituted to special key */

					flyingTime = getTimeDifference(getTime(), backupTime);
					removeLastKey();
				} else {
					flyingTime = getTimeDifference(getTime(), curTime);
				}
			}

			if (apiLevel < 14) {
				/* Pressure can not be obtained after API 14 */

				pressure = serviceHandler.getPressure();
			}

			addKey();
			error = false;
			longPress = false;

		} else if (!keyDel && (start != 0 || (start == 0 && before == 1))) {
			longPress = true;
		}

		if ((start != 0 || before == 0) && !longPress) {
			backupTime = curTime;
			curTime = getTime();
		}
	}

	@Override
	public void afterTextChanged(Editable e) {
	}

	/**
	 * Returns new listener for delete key.
	 * 
	 * @return OnKeyListener
	 */
	protected OnKeyListener addFlyingOnKeyListener() {
		return new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_DEL) {
					Log.v("Logger", "Delete pressed");
					removeLastKey();
					keyDel = true;
					error = true;
				} else {
					keyDel = false;
				}
				return false;
			}
		};
	}

	/**
	 * Returns current time in milliseconds.
	 * 
	 * @return time in millis
	 */
	private long getTime() {
		return System.currentTimeMillis();
	}

	/**
	 * Returns difference between two times.
	 * 
	 * @param time1
	 *            start time
	 * @param time2
	 *            end time
	 * @return difference between times
	 */
	private long getTimeDifference(long time1, long time2) {
		return time1 - time2;
	}

	/**
	 * Adds new logged key. If first key is pressed, then adds zero values.
	 */
	private void addKey() {
		double deltaAxisX = 0;
		double deltaAxisY = 0;
		double deltaAxisZ = 0;
		
		if (tempKeys.size() == 0) {
			flyingTime = 0;
			accX = 0;
			accY = 0;
			accZ = 0;
		} else {
			deltaAxisX = xAxis - prevAxisX;
			deltaAxisY = yAxis - prevAxisY;
			deltaAxisZ = zAxis - prevAxisZ;
		}

		tempKeys.add(new LoggedKey(flyingTime, longPress, error, display
				.getRotation(), pressure, deltaAxisX, deltaAxisY, deltaAxisZ, accX, accY, accZ));
		
		prevAxisX = xAxis;
		prevAxisY = yAxis;
		prevAxisZ = zAxis;
	}

	/**
	 * Remove last key from temporally logged keys.
	 */
	private void removeLastKey() {
		if (tempKeys.size() != 0) {
			if (tempKeys.get(tempKeys.size() - 1).getError() == true) {
				error = true;
			}
			tempKeys.remove(tempKeys.size() - 1);
		}
	}

	/**
	 * Submits single biometrics sample and reinitialize logger.
	 * 
	 * @return list of logged keys
	 */
	protected List<LoggedKey> submit() {
		rowKeys = tempKeys;
		initValues();
		return rowKeys;
	}

	/**
	 * Binds all listeners when logging starts or resumes.
	 */
	protected void startLogging() {
		if (apiLevel < 14) {
			serviceHandler.runService();
		}

		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magnetometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	/**
	 * Unbinds all listener when logging is finished or paused.
	 */
	protected void stopLogging() {
		if (apiLevel < 14) {
			serviceHandler.stopService();
		}

		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, magnetometer);
	}

	private final float alpha = 0.8f;
	private float gravity[] = { 0, 0, 0 };
	private float[] mGravity;
	private float[] mGeomagnetic;

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			/* Acceleration delta between key presses */
			mGravity = event.values;
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

			accX = event.values[0] - gravity[0];
			accY = event.values[1] - gravity[1];
			accZ = event.values[2] - gravity[2];
		}

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mGeomagnetic = event.values;
		}

		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];

			if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				
				xAxis = (float) Math.toDegrees(orientation[1]) + 180;
				yAxis = (float) Math.toDegrees(orientation[2]) + 180;
				zAxis = (float) Math.toDegrees(orientation[0]) + 180;
			}
		}
	}
}