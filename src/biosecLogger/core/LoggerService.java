package biosecLogger.core;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

/**
 * Service is supposed to log pressure. Works only on android API less than 14.
 * 
 * @author Stefan Smihla
 * 
 */
@SuppressLint("InlinedApi")
public class LoggerService extends Service {

	public final static String SCREEN_TOUCHED = "Key Pressed";
	private Intent sender;
	private View myView;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sender = new Intent();
		sender.setAction(SCREEN_TOUCHED);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		myView = new View(this);

		OnTouchListener toucher = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				sender.putExtra("pressure", event.getSize());
				sendBroadcast(sender);
				return false;
			}
		};

		myView.setOnTouchListener(toucher);
		wm.addView(myView, params);
		Log.v(UserLoggerManager.LOGNAME, "Service started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		myView.setOnTouchListener(null);
		Log.v(UserLoggerManager.LOGNAME, "Service stopped");
	}
}