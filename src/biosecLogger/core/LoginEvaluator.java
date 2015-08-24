package biosecLogger.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Class evaluates user's biometric sample against biometric template.
 * 
 * @author Stefan Smihla
 * 
 */
public class LoginEvaluator {
	private OptionsManager om;

	private int flyingTimesWeight;
	private int errorRateWeight;
	private int substituteRateWeight;
	private int acceleratorWeight;
	private int orientationWeight;
	private int totalWeight;

	/**
	 * Initialize instance of LoginEvaluator
	 * 
	 * @param oManager
	 *            evaluation accuracy settings
	 */
	protected LoginEvaluator(OptionsManager oManager) {
		om = oManager;

		flyingTimesWeight = 5;
		errorRateWeight = 1;
		substituteRateWeight = 2;
		acceleratorWeight = 5;
		orientationWeight = 5;
	}

	/**
	 * Compares single value in t-tests method.
	 * 
	 * @param templateValue
	 *            average template value
	 * @param templateDeviation
	 *            standard deviation of template
	 * @param testValue
	 *            value which is compared against average
	 * @return boolean with result if passed or not
	 */
	private boolean compareSampleValue(double templateValue,
			double templateDeviation, double testValue) {
		double actMin = (1 - om.getThresholdP()) * templateValue
				- templateDeviation;
		double actMax = (1 + om.getThresholdP()) * templateValue
				+ templateDeviation;

		/*
		 * if (actMax - actMin == 0){ return 1; } else { return 1 / (16 *
		 * Math.pow((templateValue - testValue) / (actMax - actMin), 4) + 1);
		 * //return 1 / (4 * Math.pow((templateValue - testValue) / (actMax -
		 * actMin), 2) + 1); }
		 */

		return (testValue >= actMin && testValue <= actMax) ? true : false;
	}

	/**
	 * Converts values to n-graph values.
	 * 
	 * @param values
	 *            raw unconverted values
	 * @param n
	 *            number of graphs
	 * @return converted values
	 */
	private List<Double> getGraphs(List<Double> values, int n) {
		List<Double> graphs = new ArrayList<Double>();
		double value;

		if (n == 0) {
			n++;
		}

		for (int i = 0; i < values.size() + 1 - n; i++) {
			value = 0;
			for (int j = 0; j < n; j++) {
				value += values.get(i);
			}
			graphs.add(value);
		}

		return graphs;
	}

	/**
	 * Distance vector based comparison.
	 * 
	 * @param testValues
	 *            test vector of values
	 * @param averageValues
	 *            average vector of values
	 * @param deviations
	 *            standard deviation vector of values
	 * @param threshold
	 *            user's computed threshold
	 * @param algorithm
	 *            evaluation distance algorithm
	 * @return similarity between test vector and average vector
	 */
	private double distanceComparision(List<Double> testValues,
			List<Double> averageValues, List<Double> deviations,
			double threshold, int algorithm) {

		double score = 0;

		for (int i = 0; i < testValues.size(); i++) {
			if (deviations.get(i) == 0) {
				continue;
			}

			switch (algorithm) {
			case OptionsManager.MANHATTAN:
				score += Math.abs((testValues.get(i) - averageValues.get(i)));
				break;
			case OptionsManager.EUCLIDEAN:
				score += Math.pow(testValues.get(i) - averageValues.get(i), 2);
				break;
			// case UserLoggerManager.MAHALANOBIS:
			// score += Math.pow((testValues.get(i) - averageValues.get(i)) /
			// deviations.get(i), 2);
			// break;
			}
		}

		switch (algorithm) {
		case OptionsManager.EUCLIDEAN:
			score = Math.sqrt(score);
			break;
		}

		return (score <= threshold) ? 1 : 1 - Math.abs(1 - (score / threshold));
	}

	/**
	 * T-test based vector comparison
	 * 
	 * @param testValues
	 *            test vector of values
	 * @param averageValues
	 *            average vector of values
	 * @param deviations
	 *            standard deviation vector of values
	 * @return similarity between test vector and average vector
	 */
	private double tTestComparision(List<Double> testValues,
			List<Double> averageValues, List<Double> deviations) {
		double testPassed = 0;
		double testTotal = testValues.size();

		double average;
		double deviation;
		double value;

		for (int i = 0; i < testValues.size(); i++) {
			average = averageValues.get(i);
			deviation = deviations.get(i);
			value = testValues.get(i);

			if (compareSampleValue(Math.abs(average), Math.abs(deviation),
					Math.abs(value))) {
				testPassed += 1;
			}
		}

		testPassed /= testTotal;

		return testPassed;
	}

	/**
	 * Evaluates acceleration score.
	 * 
	 * @param ut
	 *            template of user
	 * @param us
	 *            test sample
	 * @return acceleration score
	 */
	private double getAcceleratorScore(UserTemplate ut, LoggedRow us) {
		double score = 0;

		for (int i = UserTemplate.X_AXIS; i <= UserTemplate.Z_AXIS; i++) {
			for (int j = 1; j <= om.getGraphs(); j++) {
				switch (om.getEvaluationAlgorithm()) {
				case OptionsManager.T_TESTS:
					score += tTestComparision(getGraphs(us.getAccs(i), j),
							getGraphs(ut.getAvgAccs(i), j),
							getGraphs(ut.getDeviationAccs(i), j));
					break;
				case OptionsManager.MANHATTAN:
				case OptionsManager.EUCLIDEAN:
					score += distanceComparision(
							getGraphs(us.getAccs(i), j),
							getGraphs(ut.getAvgAccs(i), j),
							getGraphs(ut.getDeviationAccs(i), j),
							ut.getThreshold(ut.getAccs(i), j,
									om.getEvaluationAlgorithm()),
							om.getEvaluationAlgorithm());
					break;
				}
			}
		}

		score /= (3 * om.getGraphs());
		totalWeight += acceleratorWeight;
		return score * acceleratorWeight;
	}

	/**
	 * Evaluates orientation score.
	 * 
	 * @param ut
	 *            template of user
	 * @param us
	 *            test sample
	 * @return orientation score
	 */
	private double getOrientationScore(UserTemplate ut, LoggedRow us) {
		double score = 0;

		for (int i = UserTemplate.X_AXIS; i <= UserTemplate.Z_AXIS; i++) {
			for (int j = 1; j <= om.getGraphs(); j++) {
				switch (om.getEvaluationAlgorithm()) {
				case OptionsManager.T_TESTS:
					score += tTestComparision(getGraphs(us.getAxises(i), j),
							getGraphs(ut.getAvgAxises(i), j),
							getGraphs(ut.getDeviationAxises(i), j));
					break;
				case OptionsManager.MANHATTAN:
				case OptionsManager.EUCLIDEAN:
					score += distanceComparision(
							getGraphs(us.getAxises(i), j),
							getGraphs(ut.getAvgAxises(i), j),
							getGraphs(ut.getDeviationAxises(i), j),
							ut.getThreshold(ut.getAxises(i), j,
									om.getEvaluationAlgorithm()),
							om.getEvaluationAlgorithm());
					break;
				}
			}
		}

		score /= (3 * om.getGraphs());
		totalWeight += orientationWeight;
		return score * orientationWeight;
	}

	/**
	 * Evaluates flying time score.
	 * 
	 * @param ut
	 *            template of user
	 * @param us
	 *            test sample
	 * @return flying time score
	 */
	private double getFlyingTimesScore(UserTemplate ut, LoggedRow us) {
		double score = 0;

		for (int i = 1; i <= om.getGraphs(); i++) {
			switch (om.getEvaluationAlgorithm()) {
			case OptionsManager.T_TESTS:
				score += tTestComparision(getGraphs(us.getFlyingTimes(), i),
						getGraphs(ut.getAverageFlyingTimes(), i),
						getGraphs(ut.getFlyingTimesDeviation(), i));
				break;
			case OptionsManager.MANHATTAN:
			case OptionsManager.EUCLIDEAN:
				score += distanceComparision(
						getGraphs(us.getFlyingTimes(), i),
						getGraphs(ut.getAverageFlyingTimes(), i),
						getGraphs(ut.getFlyingTimesDeviation(), i),
						ut.getThreshold(ut.getFlyingTimes(), i,
								om.getEvaluationAlgorithm()),
						om.getEvaluationAlgorithm());
				break;
			}
		}

		score /= om.getGraphs();
		totalWeight += flyingTimesWeight;
		return score * flyingTimesWeight;
	}

	/**
	 * Evaluates error or long press rate and add weight if not passed.
	 * 
	 * @param sampleRate
	 *            test values
	 * @param templateRate
	 *            template values
	 * @param weight
	 *            weight of metric
	 */
	private void evaluateRate(double sampleRate, double templateRate, int weight) {
		if (sampleRate > Math.ceil(templateRate)
				|| sampleRate < Math.floor(templateRate)) {
			totalWeight += weight;
		}
	}

	/*************************************************************************/

	/**
	 * Checks biometric sample against biometric template.
	 * 
	 * @param user
	 *            tested user template
	 * @param row
	 *            tested sample
	 * @return boolean if passed or not
	 */
	protected boolean checkPatter(UserModel user, List<LoggedKey> row) {
		UserTemplate ut = new UserTemplate(user, om.getFlag(), true);
		LoggedRow us = new LoggedRow(row);

		double finalScore = totalWeight = 0;

		if ((om.getFlag() & OptionsManager.FLYINGTIMES) == OptionsManager.FLYINGTIMES)
			finalScore += getFlyingTimesScore(ut, us);
		if ((om.getFlag() & OptionsManager.ACCELERANCE) == OptionsManager.ACCELERANCE)
			finalScore += getAcceleratorScore(ut, us);
		if ((om.getFlag() & OptionsManager.ORIENTATION) == OptionsManager.ORIENTATION)
			finalScore += getOrientationScore(ut, us);

		if ((om.getFlag() & OptionsManager.LONGPRESSRATE) == OptionsManager.LONGPRESSRATE)
			evaluateRate(us.getLongPressCount(), ut.getLongPressRate(),
					substituteRateWeight);
		if ((om.getFlag() & OptionsManager.ERRORRATE) == OptionsManager.ERRORRATE)
			evaluateRate(us.getErrorCount(), ut.getErrorRate(), errorRateWeight);

		finalScore /= totalWeight;

		return (finalScore >= om.getSensitivity()) ? true : false;
	}
}
