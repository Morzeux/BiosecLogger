package com.morzeux.bioseclogger.logic;

import android.app.Activity;
import android.widget.TextView;

import com.morzeux.bioseclogger.R;

/**
 * Updates texts on view from DeviceInfo class.
 * 
 * @author Stefan Smihla
 * 
 */
public class HardwareInfoTexts {

	private HardwareInfo hw;

	private TextView modelInfoText;
	private TextView versionInfoText;
	private TextView cpuInfoText;
	private TextView displaySizeText;
	private TextView batteryLevelText;
	private TextView internalSpaceText;
	private TextView externalSpaceText;
	private TextView memoryUsageText;

	/**
	 * Finds text views in provided context.
	 * 
	 * @param ctx
	 *            context of source activity
	 * @param hw
	 *            device info class
	 */
	public HardwareInfoTexts(Activity ctx, HardwareInfo hw) {
		this.hw = hw;
		this.modelInfoText = (TextView) ctx.findViewById(R.id.modelInfoText);
		this.versionInfoText = (TextView) ctx
				.findViewById(R.id.versionInfoText);
		this.cpuInfoText = (TextView) ctx.findViewById(R.id.cpuInfoText);
		this.displaySizeText = (TextView) ctx
				.findViewById(R.id.displaySizeText);
		this.batteryLevelText = (TextView) ctx
				.findViewById(R.id.batteryLevelText);
		this.internalSpaceText = (TextView) ctx
				.findViewById(R.id.internalSpaceText);
		this.externalSpaceText = (TextView) ctx
				.findViewById(R.id.externalSpaceText);
		this.memoryUsageText = (TextView) ctx
				.findViewById(R.id.memoryUsageText);
	}

	/**
	 * Sets static labels.
	 */
	public void setStaticTexts() {
		modelInfoText.setText(hw.getModel());
		versionInfoText.setText(hw.getOsVersion());
		cpuInfoText.setText(hw.getCpuText());
		displaySizeText.setText(hw.getDisplayText());
	}

	/**
	 * Sets labels which changes runtime.
	 */
	public void setDynamicTexts() {
		batteryLevelText.setText(hw.getBatteryLevel() + "%");
		internalSpaceText.setText(hw.getSpaceText(true));
		externalSpaceText.setText(hw.getSpaceText(false));
		memoryUsageText.setText(hw.getMemoryText());
	}
}
