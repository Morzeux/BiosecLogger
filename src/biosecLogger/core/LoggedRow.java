package biosecLogger.core;

import java.util.ArrayList;
import java.util.List;

/**
 * LoggedRow converts values from list of LoggedKeys to row with vector values.
 * 
 * @author Stefan Smihla
 * 
 */
public class LoggedRow {

	private List<Double> flyingTimes;
	private List<Double> errors;
	private List<Double> orientations;
	private List<Double> longPresses;
	private List<Double> pressures;
	private List<Double> xAxises;
	private List<Double> yAxises;
	private List<Double> zAxises;
	private List<Double> accXs;
	private List<Double> accYs;
	private List<Double> accZs;

	/**
	 * Initialize row from raw logged keys.
	 * 
	 * @param row
	 *            list of logged keys
	 */
	protected LoggedRow(List<LoggedKey> row) {
		flyingTimes = new ArrayList<Double>();
		errors = new ArrayList<Double>();
		orientations = new ArrayList<Double>();
		longPresses = new ArrayList<Double>();
		pressures = new ArrayList<Double>();
		xAxises = new ArrayList<Double>();
		yAxises = new ArrayList<Double>();
		zAxises = new ArrayList<Double>();
		accXs = new ArrayList<Double>();
		accYs = new ArrayList<Double>();
		accZs = new ArrayList<Double>();

		for (LoggedKey key : row) {
			flyingTimes.add((double) key.getFlyingTime());

			if (key.getError()) {
				errors.add((double) 1);
			} else {
				errors.add((double) 0);
			}

			if (key.getLongPress()) {
				longPresses.add((double) 1);
			} else {
				longPresses.add((double) 0);
			}

			orientations.add((double) key.getOrientation());
			pressures.add(key.getPressure());
			xAxises.add(key.getAxisX());
			yAxises.add(key.getAxisY());
			zAxises.add(key.getAxisZ());
			accXs.add(key.getAccX());
			accYs.add(key.getAccY());
			accZs.add(key.getAccZ());
		}
	}

	/**
	 * Returns vector from flying times.
	 * 
	 * @return flying times
	 */
	protected List<Double> getFlyingTimes() {
		return flyingTimes;
	}

	/**
	 * Returns number of errors in vector.
	 * 
	 * @return number of errors
	 */
	protected int getErrorCount() {
		int errorsCount = 0;
		for (double error : errors) {
			errorsCount += (int) error;
		}

		return errorsCount;
	}

	/**
	 * Returns number of long presses in vector.
	 * 
	 * @return number of long presses
	 */
	protected int getLongPressCount() {
		int longPressCount = 0;
		for (double longPress : longPresses) {
			longPressCount += (int) longPress;
		}

		return longPressCount;
	}

	/**
	 * Returns acceleration vector for specific axis in row.
	 * 
	 * @param axis
	 *            constant of specific axis { X, Y, Z }
	 * @return acceleration vector of specific axis
	 */
	protected List<Double> getAccs(int axis) {
		switch (axis) {
		case UserTemplate.X_AXIS:
			return accXs;
		case UserTemplate.Y_AXIS:
			return accYs;
		case UserTemplate.Z_AXIS:
			return accZs;
		default:
			return null;
		}
	}

	/**
	 * Returns orientation vector for specific axis in row.
	 * 
	 * @param axis
	 *            constant of specific axis { X, Y, Z }
	 * @return orientation vector of specific axis
	 */
	protected List<Double> getAxises(int axis) {
		switch (axis) {
		case UserTemplate.X_AXIS:
			return xAxises;
		case UserTemplate.Y_AXIS:
			return yAxises;
		case UserTemplate.Z_AXIS:
			return zAxises;
		default:
			return null;
		}
	}
}
