package biosecLogger.core;

/**
 * Single touch key with logged values.
 * 
 * @author Stefan Smihla
 * 
 */
public class LoggedKey {

	private long flyingTime;
	private boolean error;
	private int orientation;
	private boolean longPress;
	private double pressure;
	private double xAxis;
	private double yAxis;
	private double zAxis;
	private double accX;
	private double accY;
	private double accZ;

	/**
	 * LoggedKey constructor with logged values.
	 * 
	 * @param flyingTime
	 *            time between last touch and this touch
	 * @param longPress
	 *            boolean if key was long pressed (for key substitution)
	 * @param error
	 *            boolean if delete key was pressed
	 * @param orientation
	 *            orientation of display (up, right, left)
	 * @param pressure
	 *            pressure of key (works in API less than 14)
	 * @param xAxis
	 *            orientation X axis
	 * @param yAxis
	 *            orientation Y axis
	 * @param zAxis
	 *            orientation Z axis
	 * @param accX
	 *            accelerator X axis
	 * @param accY
	 *            accelerator Y axis
	 * @param accZ
	 *            accelerator Z axis
	 */
	protected LoggedKey(long flyingTime, boolean longPress, boolean error,
			int orientation, double pressure, double xAxis, double yAxis,
			double zAxis, double accX, double accY, double accZ) {

		this.flyingTime = flyingTime;
		this.longPress = longPress;
		this.error = error;
		this.orientation = orientation;
		this.pressure = pressure;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;
		this.accX = accX;
		this.accY = accY;
		this.accZ = accZ;
	}

	/**
	 * Returns flying time.
	 * 
	 * @return flying time
	 */
	protected long getFlyingTime() {
		return flyingTime;
	}

	/**
	 * Returns orientation of display (UP, LEFT, RIGHT).
	 * 
	 * @return orientation
	 */
	protected int getOrientation() {
		return orientation;
	}

	/**
	 * Returns error boolean (true if delete key was pressed before key).
	 * 
	 * @return error
	 */
	protected boolean getError() {
		return error;
	}

	/**
	 * Returns boolean if key was long pressed. This way it checks if key
	 * substitution was performed on non alphabetic char or keyboard was
	 * changed.
	 * 
	 * @return long press
	 */
	protected boolean getLongPress() {
		return longPress;
	}

	/**
	 * Touch size on Android devices with API < 14. Unfortunately it won't work
	 * on newer devices.
	 * 
	 * @return pressure
	 */
	protected double getPressure() {
		return pressure;
	}

	/**
	 * Returns delta on orientation on axis X.
	 * 
	 * @return xAxis
	 */
	protected double getAxisX() {
		return xAxis;
	}

	/**
	 * Returns delta on orientation on axis Y.
	 * 
	 * @return yAxis
	 */
	protected double getAxisY() {
		return yAxis;
	}

	/**
	 * Returns delta on orientation on axis Z.
	 * 
	 * @return zAxis
	 */
	protected double getAxisZ() {
		return zAxis;
	}

	/**
	 * Returns delta on acceleration on axis X.
	 * 
	 * @return xAxis
	 */
	protected double getAccX() {
		return accX;
	}

	/**
	 * Returns delta on acceleration on axis Y.
	 * 
	 * @return yAxis
	 */
	protected double getAccY() {
		return accY;
	}

	/**
	 * Returns delta on acceleration on axis Z.
	 * 
	 * @return zAxis
	 */
	protected double getAccZ() {
		return accZ;
	}
}
