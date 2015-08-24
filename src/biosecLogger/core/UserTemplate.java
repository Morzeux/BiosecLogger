package biosecLogger.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * UserTemplate class creates formatted data from raw logged data. Formatted
 * data are used to evaluate with sample.
 * 
 * @author Stefan Smihla
 * 
 */
public class UserTemplate {

	/** Axis constants */
	protected static final int X_AXIS = 0;
	protected static final int Y_AXIS = 1;
	protected static final int Z_AXIS = 2;

	private UserModel user;

	private List<List<Double>> flyingTimes;
	private List<Double> avgFlyingTimes;
	private List<Double> flyingTimesDeviation;

	private List<List<Double>> accXs;
	private List<Double> avgAccXs;
	private List<Double> deviationAccXs;

	private List<List<Double>> accYs;
	private List<Double> avgAccYs;
	private List<Double> deviationAccYs;

	private List<List<Double>> accZs;
	private List<Double> avgAccZs;
	private List<Double> deviationAccZs;

	private List<List<Double>> xAxises;
	private List<Double> avgXAxises;
	private List<Double> deviationXAxises;

	private List<List<Double>> yAxises;
	private List<Double> avgYAxises;
	private List<Double> deviationYAxises;

	private List<List<Double>> zAxises;
	private List<Double> avgZAxises;
	private List<Double> deviationZAxises;

	private double errorRate;
	private double longPressRate;
	
	private int passLength;
	
	/**
	 * Creates instance of UserTemplate class and extracts values.
	 * 
	 * @param user
	 *            model of user with raw data
	 * @param completeLoad
	 * 			  performs complete load if true (with average and deviation values)           
	 * @param flag
	 *            describes which metrics to validate
	 */
	protected UserTemplate(UserModel user, int flag, boolean completeLoad) {
		extractsAll(user, flag);
		if (completeLoad){
			computeAveragesWithDeviations();
		}
	}
	
	/**
	 * Extracts basic values.
	 * 
	 * @param user
	 *            model of user with raw data
	 * @param flag
	 *            describes which metrics to validate
	 */
	private void extractsAll(UserModel user, int flag){
		this.user = user;
		passLength = getPasswordLength();
		
		if ((flag & OptionsManager.FLYINGTIMES) == OptionsManager.FLYINGTIMES){
			flyingTimes = extractFlyingTimes();
		}
				
		if ((flag & OptionsManager.ACCELERANCE) == OptionsManager.ACCELERANCE){
			accXs = extractSensors(OptionsManager.ACCELERANCE, X_AXIS);
			accYs = extractSensors(OptionsManager.ACCELERANCE, Y_AXIS);
			accZs = extractSensors(OptionsManager.ACCELERANCE, Z_AXIS);
		}
		
		if ((flag & OptionsManager.ORIENTATION) == OptionsManager.ORIENTATION){
			xAxises = extractSensors(OptionsManager.ORIENTATION, X_AXIS);
			yAxises = extractSensors(OptionsManager.ORIENTATION, Y_AXIS);
			zAxises = extractSensors(OptionsManager.ORIENTATION, Z_AXIS);
		}
		
		if ((flag & OptionsManager.ERRORRATE) == OptionsManager.ERRORRATE){
			errorRate = extractErrors();
		} else {
			errorRate = -1;
		}

		if ((flag & OptionsManager.LONGPRESSRATE) == OptionsManager.LONGPRESSRATE){
			longPressRate = extractLongPresses();
		} else {
			longPressRate = -1;
		}
	}
	
	/**
	 * Returns length of a password.
	 * 
	 * @return	length of a password
	 */
	private int getPasswordLength(){
		int[] sizes = new int[user.getLoggedKeys().size()];
		int i = 0;
		for (List<LoggedKey> row : user.getLoggedKeys()){
			sizes[i] = row.size();
			i++;
		}
		
		Arrays.sort(sizes);
		return sizes[sizes.length / 2];  
	}
	
	/**
	 * Computes averages and deviations for extracted metrics.
	 */
	private void computeAveragesWithDeviations(){
		List<List<Double>> result;
		
		if (flyingTimes != null){
			result = computeClearAverageWithDeviation(flyingTimes, null);
			avgFlyingTimes = result.get(0);
			flyingTimesDeviation = result.get(1);
		}
		
		if (accXs != null) {
			result = computeClearAverageWithDeviation(accXs, null);
			avgAccXs = result.get(0);
			deviationAccXs = result.get(1);

			result = computeClearAverageWithDeviation(accYs, null);
			avgAccYs = result.get(0);
			deviationAccYs = result.get(1);

			result = computeClearAverageWithDeviation(accZs, null);
			avgAccZs = result.get(0);
			deviationAccZs = result.get(1);
		}

		if (xAxises != null) {
			result = computeClearAverageWithDeviation(xAxises, null);
			avgXAxises = result.get(0);
			deviationXAxises = result.get(1);

			result = computeClearAverageWithDeviation(yAxises, null);
			avgYAxises = result.get(0);
			deviationYAxises = result.get(1);

			result = computeClearAverageWithDeviation(zAxises, null);
			avgZAxises = result.get(0);
			deviationZAxises = result.get(1);
		}
	}

	/**
	 * Returns extracted errors data from logged keys.
	 * 
	 * @return extracted error rate
	 */
	private double extractErrors() {
		List<List<LoggedKey>> loggedKeys = user.getLoggedKeys();
		double errors = 0;

		for (List<LoggedKey> row : loggedKeys) {
			for (LoggedKey key : row) {
				if (key.getError() == true) {
					errors += 1;
				}
			}
		}

		return errors /= loggedKeys.size();
	}

	/**
	 * Returns extracted long press data from logged keys.
	 * 
	 * @return extracted long press rate
	 */
	private double extractLongPresses() {
		List<List<LoggedKey>> loggedKeys = user.getLoggedKeys();
		double substitutions = 0;

		for (List<LoggedKey> row : loggedKeys) {
			for (LoggedKey key : row) {
				if (key.getLongPress() == true) {
					substitutions += 1;
				}
			}
		}

		return substitutions /= loggedKeys.size();
	}

	/**
	 * Extracts flying times data from logged keys.
	 * 
	 * @return two-dimensional flying times data.
	 */
	private List<List<Double>> extractFlyingTimes() {
		List<List<Double>> flyingTimes = new ArrayList<List<Double>>();
		
		for (List<LoggedKey> row : user.getLoggedKeys()) {
			List<Double> timeRow = new ArrayList<Double>();
			for (LoggedKey key : row) {
				timeRow.add((double) key.getFlyingTime());
			}
			flyingTimes.add(timeRow.subList(0, passLength));
		}

		return flyingTimes;
	}

	/**
	 * Extracts sensor data from logged keys.
	 * 
	 * @param mode
	 *            sensor to extract (OptionsManager.ACCELERANCE or
	 *            OptionsManager.ORIENTATION)
	 * @param axis
	 *            axis to extract (X_AXIS, Y_AXIS, Z_AXIS)
	 * @return extracted sensor data for specific sensor and specific axis
	 */
	private List<List<Double>> extractSensors(int mode, int axis) {
		List<List<Double>> values = new ArrayList<List<Double>>();

		for (List<LoggedKey> row : user.getLoggedKeys()) {
			List<Double> newRow = new ArrayList<Double>();
			for (LoggedKey key : row) {
				switch (mode) {
				case OptionsManager.ACCELERANCE:
					switch (axis) {
					case X_AXIS:
						newRow.add(key.getAccX());
						break;
					case Y_AXIS:
						newRow.add(key.getAccY());
						break;
					case Z_AXIS:
						newRow.add(key.getAccZ());
						break;
					}
					break;

				case OptionsManager.ORIENTATION:
					switch (axis) {
					case X_AXIS:
						newRow.add(key.getAxisX());
						break;
					case Y_AXIS:
						newRow.add(key.getAxisY());
						break;
					case Z_AXIS:
						newRow.add(key.getAxisZ());
						break;
					}
					break;
				}
			}
			values.add(newRow.subList(0, passLength));
		}

		return values;
	}

	/**
	 * Converts input values to n-graphs values.
	 * 
	 * @param values
	 *            input values
	 * @param graphs
	 *            number of graphs
	 * @return n-graph values
	 */
	private List<List<Double>> convertToNGraphs(List<List<Double>> values,
			int graphs) {
		List<List<Double>> newValues = new ArrayList<List<Double>>();

		for (List<Double> row : values) {
			List<Double> newRow = new ArrayList<Double>();
			for (int i = 0; i < row.size() + 1 - graphs; i++) {
				double value = 0;
				for (int j = 0; j < graphs; j++) {
					value += row.get(i + j);
				}
				newRow.add(value);
			}
			newValues.add(newRow);
		}

		return newValues;
	}

	/**
	 * Compute average value for specific column in two-dimensional list.
	 * 
	 * @param col
	 *            column to compute
	 * @param values
	 *            input values
	 * @param testRow
	 *            row to skip - if null, no row will be skipped
	 * @return computed average value
	 */
	private static double computeAverageValue(int col, List<List<Double>> values,
			List<Double> testRow) {
		Double value;
		double average = 0;
		int size = 0;

		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) != testRow) {
				value = values.get(i).get(col);
				if (value != null) {
					average += values.get(i).get(col);
					size++;
				}
			}
		}

		if (testRow != null) {
			size -= 1;
		}

		return average / size;
	}

	/**
	 * Compute standard deviation value for specific column in two-dimensional
	 * list.
	 * 
	 * @param col
	 *            column to compute
	 * @param average
	 *            average value
	 * @param values
	 *            input values
	 * @param testRow
	 *            row to skip - if null, no row will be skipped
	 * @return computed standard deviation
	 */
	private static double computeDeviation(int col, double average,
			List<List<Double>> values, List<Double> testRow) {
		Double value;
		double deviation = 0;
		int size = 0;

		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) != testRow) {
				value = values.get(i).get(col);
				if (value != null) {
					deviation += Math.pow(values.get(i).get(col) - average, 2);
					size++;
				}
			}
		}

		if (testRow != null) {
			size -= 1;
		}

		return Math.sqrt(deviation / size);
	}

	/**
	 * Converts several vectors into mean vector with standard deviation vector.
	 * 
	 * @param values
	 *            input values
	 * @param testRow
	 *            row to skip - if null, no row will be skipped
	 * @return list with vectors of average values and standard deviation values
	 */
	protected static List<List<Double>> computeAverageWithDeviation(
			List<List<Double>> values, List<Double> testRow) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		List<Double> average = new ArrayList<Double>();
		List<Double> deviation = new ArrayList<Double>();

		for (int i = 0; i < values.get(0).size(); i++) {
			double averageKey = computeAverageValue(i, values, testRow);
			average.add(averageKey);
			deviation.add(computeDeviation(i, averageKey, values, testRow));
		}

		result.add(average);
		result.add(deviation);

		return result;
	}

	/**
	 * Converts several vectors into mean vector with standard deviation vector.
	 * Additionally Grubb's correction is performed.
	 * 
	 * @param values
	 *            input values
	 * @param testRow
	 *            row to skip - if null, no row will be skipped
	 * @return list with vectors of average values and standard deviation values
	 */
	private List<List<Double>> computeClearAverageWithDeviation(
			List<List<Double>> values, List<Double> testRow) {
		List<List<Double>> result = computeAverageWithDeviation(values, testRow);
		values = GrubbsTest.grubbsOutlierCorrection(values, result.get(0),
				result.get(1));
		return computeAverageWithDeviation(values, testRow);
	}

	/**
	 * Computes individual user's threshold for specific distance vector based
	 * algorithm.
	 * 
	 * @param values
	 *            input values
	 * @param algorithm
	 *            distance vector algorithm (OptionsManager.MANHATTAN or
	 *            OptionsManager.EUCLIDEAN)
	 * @return threshold value
	 */
	private double computeThreshold(List<List<Double>> values, int algorithm) {
		List<Double> thresholds = new ArrayList<Double>();

		List<List<Double>> result = computeAverageWithDeviation(values, null);
		List<Double> averageValues;
		List<Double> deviations;
		double score;

		values = GrubbsTest.grubbsOutlierCorrection(values, result.get(0),
				result.get(1));

		for (List<Double> testRow : values) {
			if (testRow.indexOf(null) != -1) {
				continue;
			}

			result = computeAverageWithDeviation(values, testRow);
			averageValues = result.get(0);
			deviations = result.get(1);

			score = 0;
			for (int i = 0; i < testRow.size(); i++) {
				if (deviations.get(i) == 0) {
					deviations.set(i, (double) 1);
				}

				switch (algorithm) {
				case OptionsManager.MANHATTAN:
					score += Math.abs((testRow.get(i) - averageValues.get(i)));
					break;
				case OptionsManager.EUCLIDEAN:
					score += Math.pow(testRow.get(i) - averageValues.get(i), 2);
					break;
				/*
				 * case UserLoggerManager.MAHALANOBIS: score +=
				 * Math.pow((testRow.get(i) - averageValues.get(i)) /
				 * deviations.get(i), 2); break;
				 */
				}
			}

			switch (algorithm) {
			case OptionsManager.EUCLIDEAN:
				score = Math.sqrt(score);
				break;
			}

			thresholds.add(score);
		}

		double threshold = 0;
		for (int i = 0; i < thresholds.size(); i++) {
			threshold += thresholds.get(i);
		}
		threshold /= thresholds.size();

		return threshold;
	}

	/*************************************************************************/

	/**
	 * Returns individual threshold for input values and distance vector based
	 * algorithm.
	 * 
	 * @param values
	 *            input values
	 * @param graphs
	 *            number of n-graphs
	 * @param algorithm
	 *            distance vector algorithm (OptionsManager.MANHATTAN or
	 *            OptionsManager.EUCLIDEAN)
	 * @return computed threshold
	 */
	protected double getThreshold(List<List<Double>> values, int graphs,
			int algorithm) {
		return computeThreshold(convertToNGraphs(values, graphs), algorithm);
	}

	/**
	 * Returns extracted flying times.
	 * 
	 * @return extracted flying times
	 */
	protected List<List<Double>> getFlyingTimes() {
		return flyingTimes;
	}

	/**
	 * Returns average flying times vector.
	 * 
	 * @return average flying times
	 */
	protected List<Double> getAverageFlyingTimes() {
		return avgFlyingTimes;
	}

	/**
	 * Returns standard deviation flying times vector.
	 * 
	 * @return flying times deviations
	 */
	protected List<Double> getFlyingTimesDeviation() {
		return flyingTimesDeviation;
	}

	/**
	 * Returns extracted acceleration values.
	 * 
	 * @param axis
	 *            axis to return (X_AXIS, Y_AXIS, Z_AXIS)
	 * @return extracted flying times
	 */
	protected List<List<Double>> getAccs(int axis) {
		switch (axis) {
		case X_AXIS:
			return accXs;
		case Y_AXIS:
			return accXs;
		case Z_AXIS:
			return accXs;
		default:
			return null;
		}
	}

	/**
	 * Returns average acceleration values.
	 * 
	 * @param axis
	 *            axis to return (X_AXIS, Y_AXIS, Z_AXIS)
	 * @return average acceleration vector
	 */
	protected List<Double> getAvgAccs(int axis) {
		switch (axis) {
		case X_AXIS:
			return avgAccXs;
		case Y_AXIS:
			return avgAccYs;
		case Z_AXIS:
			return avgAccZs;
		default:
			return null;
		}
	}

	/**
	 * Returns standard deviation acceleration vector.
	 * 
	 * @param axis
	 *            axis to return (X_AXIS, Y_AXIS, Z_AXIS)
	 * @return acceleration deviations
	 */
	protected List<Double> getDeviationAccs(int axis) {
		switch (axis) {
		case X_AXIS:
			return deviationAccXs;
		case Y_AXIS:
			return deviationAccYs;
		case Z_AXIS:
			return deviationAccZs;
		default:
			return null;
		}
	}

	/**
	 * Returns extracted device orientation values.
	 * 
	 * @param axis
	 *            axis to return (X_AXIS, Y_AXIS, Z_AXIS)
	 * @return extracted device orientation vectors
	 */
	protected List<List<Double>> getAxises(int axis) {
		switch (axis) {
		case X_AXIS:
			return xAxises;
		case Y_AXIS:
			return yAxises;
		case Z_AXIS:
			return zAxises;
		default:
			return null;
		}
	}

	/**
	 * Returns average device orientation vector.
	 * 
	 * @param axis
	 *            axis to return (X_AXIS, Y_AXIS, Z_AXIS)
	 * @return average device orientation vector
	 */
	protected List<Double> getAvgAxises(int axis) {
		switch (axis) {
		case X_AXIS:
			return avgXAxises;
		case Y_AXIS:
			return avgYAxises;
		case Z_AXIS:
			return avgZAxises;
		default:
			return null;
		}
	}

	/**
	 * Returns standard deviation from device orientation values.
	 * 
	 * @param axis
	 *            axis to return (X_AXIS, Y_AXIS, Z_AXIS)
	 * @return standard deviations vector
	 */
	protected List<Double> getDeviationAxises(int axis) {
		switch (axis) {
		case X_AXIS:
			return deviationXAxises;
		case Y_AXIS:
			return deviationYAxises;
		case Z_AXIS:
			return deviationZAxises;
		default:
			return null;
		}
	}

	/**
	 * Returns computed error rate.
	 * 
	 * @return error rate
	 */
	protected double getErrorRate() {
		return errorRate;
	}

	/**
	 * Returns computed long press rate.
	 * 
	 * @return long press rate
	 */
	protected double getLongPressRate() {
		return longPressRate;
	}
}