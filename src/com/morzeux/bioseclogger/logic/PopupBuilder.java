package com.morzeux.bioseclogger.logic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import biosecLogger.core.UserLoggerManager;

import com.morzeux.bioseclogger.R;

/**
 * Popup builder creates basic popups used in application.
 * 
 * @author Stefan Smihla
 * 
 */
public class PopupBuilder {

	private Activity act;
	private AlertDialog.Builder builder;
	private Dialog dialog;

	private String buttonName;

	/**
	 * Constructs basic popup.
	 * 
	 * @param act
	 *            source activity
	 * @param title
	 *            title of popup
	 * @param message
	 *            message text in popup
	 * @param buttonName
	 *            close button label
	 */
	public PopupBuilder(Activity act, String title, String message,
			String buttonName) {

		this.act = act;
		this.buttonName = buttonName;

		builder = new AlertDialog.Builder(act);
		builder.setTitle(title);

		if (message != null) {
			builder.setMessage(message);
		}

		builder.setCancelable(false);
		dialog = null;
	}

	/**
	 * Adds close logic to button.
	 * 
	 * @param name
	 *            closing button name
	 */
	private void addClosingButton(String name) {
		builder.setNegativeButton(name, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
	}

	/**
	 * Adds exit logic to button.
	 * 
	 * @param name
	 *            exiting button name
	 */
	private void addExitingButton(String name) {
		builder.setNegativeButton(name, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				act.finish();
			}
		});
	}

	/**
	 * Adds submit button logic. Invokes "popupSubmitted" method from source
	 * activity on click.
	 * 
	 * @param name
	 *            submit button label
	 */
	private void addSubmitButton(String name, final String submitMethod) {
		builder.setPositiveButton(name, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				try {
					Method m = act.getClass().getMethod(submitMethod);
					m.invoke(act);
				} catch (NoSuchMethodException e) {
					Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
				} catch (IllegalAccessException e) {
					Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
				} catch (InvocationTargetException e) {
					Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
				}

				dialog.cancel();
			}
		});
	}

	/**
	 * Set view as popup content.
	 * 
	 * @param view
	 *            source viev layout
	 */
	public void setView(View view) {
		builder.setView(view);
	}

	/**
	 * Creates popup with question.
	 * 
	 * @param yesString
	 *            positive text label
	 */
	public void createAskPopup(String yesString, String submitMethod) {
		addClosingButton(buttonName);
		addSubmitButton(yesString, submitMethod);
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * Creates popup which will close activity.
	 */
	public void createExitingPopup() {
		addExitingButton(buttonName);
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * Creates basic popup with close button.
	 */
	public void createClosingPopup() {
		addClosingButton(buttonName);
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * Creates popup with loading content and custom message.
	 * 
	 * @param message
	 *            custom message content
	 */
	public void createLoadingPopup(String message) {
		LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.loading_dialog, null);
		((TextView) view.findViewById(R.id.loadingLabel)).setText(message);

		builder.setView(view);
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * Close pupup from outside event.
	 */
	public void close() {
		if (dialog != null) {
			dialog.cancel();
		}
	}
}
