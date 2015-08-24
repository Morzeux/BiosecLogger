package biosecLogger.core;

import java.util.ArrayList;
import java.util.List;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

/**
 * Combined listener which contains multiple OnKeyListeners.
 * 
 * @author Stefan Smihla
 * 
 */
public class CompositeOnKeyListener implements OnKeyListener {
	private List<OnKeyListener> listeners = new ArrayList<OnKeyListener>();

	/**
	 * Adds new listener to CompositeOnKeyListener.
	 * 
	 * @param listener
	 *            listener to be added
	 */
	protected void addListener(OnKeyListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		for (OnKeyListener l : listeners) {
			l.onKey(v, keyCode, event);
		}
		return false;
	}
}
