package biosecLogger.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grubb's tests evaluator. Grubb's method is also called as extreme studentized
 * deviate (ESD). ESD method is supposed to clean values with extreme deviation.
 * 
 * @author Stefan Smihla
 * @see http://graphpad.com/support/faqid/1598/
 */
public class GrubbsTest {

	private static final Map<Double, Double> criticalValues;

	static {
		criticalValues = new HashMap<Double, Double>();
		Double[][] pairs = { { 3.0, 1.15 }, { 4.0, 1.48 }, { 5.0, 1.71 },
				{ 6.0, 1.89 }, { 7.0, 2.02 }, { 8.0, 2.13 }, { 9.0, 2.21 },
				{ 10.0, 2.29 }, { 11.0, 2.34 }, { 12.0, 2.41 }, { 13.0, 2.46 },
				{ 14.0, 2.51 }, { 15.0, 2.55 }, { 16.0, 2.59 }, { 17.0, 2.62 },
				{ 18.0, 2.65 }, { 19.0, 2.68 }, { 20.0, 2.71 }, { 21.0, 2.73 },
				{ 22.0, 2.76 }, { 23.0, 2.78 }, { 24.0, 2.80 }, { 25.0, 2.82 },
				{ 26.0, 2.84 }, { 27.0, 2.86 }, { 28.0, 2.88 }, { 29.0, 2.89 },
				{ 30.0, 2.91 }, { 31.0, 2.92 }, { 32.0, 2.94 }, { 33.0, 2.95 },
				{ 34.0, 2.97 }, { 35.0, 2.98 }, { 36.0, 2.99 }, { 37.0, 3.00 },
				{ 38.0, 3.01 }, { 39.0, 3.03 }, { 40.0, 3.04 } };

		for (Double[] pair : pairs) {
			criticalValues.put(pair[0], pair[1]);
		}
	}

	/**
	 * Compute Z value from tested value.
	 * 
	 * @param value
	 *            test value
	 * @param average
	 *            average value
	 * @param deviation
	 *            standard deviation of value
	 * @return Z value
	 */
	private static double getValueZ(double value, double average,
			double deviation) {
		return deviation != 0 ? Math.abs(average - value) / deviation : 0;
	}

	/**
	 * Critical Z value works as threshold in value testing.
	 * 
	 * @param n
	 *            number of vectors
	 * @return critical value
	 */
	private static double getCriticalValue(int n) {
		if (n < 3) {
			return criticalValues.get(3.0);
		} else if (n > 40) {
			return (n - 1) / Math.sqrt(n);
		} else {
			return criticalValues.get((double) n);
		}
	}

	/**
	 * Test for single value.
	 * 
	 * @param value
	 *            tested value
	 * @param average
	 *            average value
	 * @param deviation
	 *            standard deviation
	 * @param n
	 *            number of vectors
	 * @return result of test as boolean
	 */
	private static boolean performGrubbsTest(double value, double average,
			double deviation, int n) {
		double z = getValueZ(value, average, deviation);
		double g = getCriticalValue(n);
		return z <= g ? true : false;
	}

	/**
	 * Corrects values with ESD method. Substitutes extreme values with null.
	 * 
	 * @param values
	 *            raw vectors
	 * @param averages
	 *            average vector
	 * @param deviations
	 *            standard deviation vector
	 * @return corrected vectors
	 */
	public static List<List<Double>> grubbsOutlierCorrection(
			List<List<Double>> values, List<Double> averages,
			List<Double> deviations) {

		List<List<Double>> newValues = new ArrayList<List<Double>>();
		List<Double> newRow;

		for (List<Double> row : values) {
			newRow = new ArrayList<Double>();
			for (int i = 0; i < row.size(); i++) {
				if (performGrubbsTest(row.get(i), averages.get(i),
						deviations.get(i), values.size())) {
					newRow.add(row.get(i));
				} else {
					newRow.add(null);
				}
			}
			newValues.add(newRow);
		}

		return newValues;
	}
}
