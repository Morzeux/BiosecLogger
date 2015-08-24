package biosecLogger.core;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import biosecLogger.exceptions.HoldCounterException;

public class OptionsManager {

	private static final int EVAL_ALGORITHMS = 3;

	/** Evaluation algorithms */
	public static final int T_TESTS = 0;
	public static final int MANHATTAN = 1;
	public static final int EUCLIDEAN = 2;
	// public static final int MAHALANOBIS = 3;

	/** Flags */
	public static final int FLYINGTIMES = 1;
	public static final int ACCELERANCE = 2;
	public static final int ORIENTATION = 4;
	public static final int LONGPRESSRATE = 128;
	public static final int ERRORRATE = 256;

	private int evaluationAlgorithm;

	private int templateCreateCount;
	private int templateHoldCount;

	private boolean externalSaving;

	private int flag;
	private int graphs;
	private double pThreshold;
	private double[] sensitivity;

	/**
	 * Initialize instance of OptionsManager.
	 * 
	 * @param ctx
	 *            activity context
	 */
	public OptionsManager(Context ctx) {
		sensitivity = new double[EVAL_ALGORITHMS];

		/*
		 * Checking if settings file exists, if not, then settings file is
		 * created
		 */
		try {
			loadSettingsFromFile(ctx);
		} catch (IOException e) {
			loadSettingsFromDefaults();
		} catch (JSONException e) {
			loadSettingsFromDefaults();
		}
	}

	/**
	 * Loads default hard-coded settings when file is not found.
	 */
	private void loadSettingsFromDefaults() {
		evaluationAlgorithm = T_TESTS;
		templateCreateCount = 10;
		templateHoldCount = 50;
		externalSaving = true;

		flag = FLYINGTIMES | LONGPRESSRATE | ERRORRATE;
		graphs = 1;
		pThreshold = 0.15;

		sensitivity[T_TESTS] = 0.8;
		sensitivity[MANHATTAN] = 0.8;
		sensitivity[EUCLIDEAN] = 0.7;
	}

	/**
	 * Loads settings from file in internal storage.
	 * 
	 * @param ctx
	 *            activity context
	 * @throws IOException
	 *             raises when file is not found or damaged
	 * @throws JSONException
	 *             raises when data are corrupted
	 */
	private void loadSettingsFromFile(Context ctx) throws IOException,
			JSONException {
		JSONObject json = new JSONObject(StorageHandler.loadFile(
				UserLoggerManager.SETTINGS_FILE, ctx));
		evaluationAlgorithm = json.getInt("evaluationAlgorithm");
		templateCreateCount = json.getInt("templateCreateCount");
		templateHoldCount = json.getInt("templateHoldCount");
		externalSaving = json.getBoolean("externalSaving");
		flag = json.getInt("flag");
		graphs = json.getInt("graphs");
		pThreshold = json.getDouble("pThreshold");
		sensitivity[T_TESTS] = json.getDouble("tTestsSensitivity");
		sensitivity[MANHATTAN] = json.getDouble("manhattanSensitivity");
		sensitivity[EUCLIDEAN] = json.getDouble("euclideanSensitivity");
	}

	/**
	 * Save default settings to file.
	 * 
	 * @param ctx
	 *            activity context
	 * @return true on success, error on fail
	 */
	public boolean saveSettings(Context ctx) {
		JSONObject json = new JSONObject();
		try {
			json.put("evaluationAlgorithm", evaluationAlgorithm);
			json.put("templateCreateCount", templateCreateCount);
			json.put("templateHoldCount", templateHoldCount);
			json.put("externalSaving", externalSaving);
			json.put("flag", flag);
			json.put("graphs", graphs);
			json.put("pThreshold", pThreshold);
			json.put("tTestsSensitivity", sensitivity[T_TESTS]);
			json.put("manhattanSensitivity", sensitivity[MANHATTAN]);
			json.put("euclideanSensitivity", sensitivity[EUCLIDEAN]);
		} catch (JSONException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			return false;
		}

		try {
			StorageHandler.saveFile(UserLoggerManager.SETTINGS_FILE,
					json.toString(), ctx, false);
		} catch (IOException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			return false;
		}

		return true;
	}

	/**
	 * Sets number of samples needed to create template.
	 * 
	 * @param count
	 *            number of samples
	 */
	public void setTemplateCreateCounter(int count) {
		templateCreateCount = count;
	}

	/**
	 * Sets maximum number of samples used to template.
	 * 
	 * @param count
	 *            number of samples
	 * @throws HoldCounterException
	 *             raises when less than create counter
	 */
	public void setTemplateHoldCounter(int count) throws HoldCounterException {
		if (count < templateCreateCount)
			throw new HoldCounterException();
		templateHoldCount = count;
	}

	/**
	 * Allows external saving.
	 */
	public void allowExternalSaving() {
		externalSaving = true;
	}

	/**
	 * Forbid external saving.
	 */
	public void disableExternalSaving() {
		externalSaving = false;
	}

	/**
	 * Returns number of samples needed to create new template.
	 * 
	 * @return number of samples
	 */
	public int getTemplateCreateCounter() {
		return templateCreateCount;
	}

	/**
	 * Returns number of samples hold in template.
	 * 
	 * @return maximum samples in one template
	 */
	public int getTemplateHoldCounter() {
		return templateHoldCount;
	}

	/**
	 * Returns boolean, true if external saving is allowed, false otherwise.
	 * 
	 * @return true if allowed, otherwise false
	 */
	public boolean checkExternalSaving() {
		return externalSaving;
	}

	/**
	 * Returns evaluation algorithm which is actually set as constant integer.
	 * 
	 * @return evaluation algorithm constant
	 */
	public int getEvaluationAlgorithm() {
		return evaluationAlgorithm;
	}

	/**
	 * Sets algorithm used to evaluate user.
	 * 
	 * @param algorithm
	 *            constant for algorithm (constants are found in
	 *            UserLoggerManager class)
	 */
	public void setEvaluationAlgorithm(int algorithm) {
		evaluationAlgorithm = (algorithm < 0) ? 0
				: (algorithm > EUCLIDEAN) ? EUCLIDEAN : algorithm;
	}

	/**
	 * Returns actual sensitivity level.
	 * 
	 * @return sensitivity level
	 */
	public double getSensitivity() {
		return sensitivity[evaluationAlgorithm];
	}

	/**
	 * Sets sensitivity for actual evaluation algorithm. Sensitivity will be set
	 * in range <0.25, 1>.
	 * 
	 * @param value
	 *            sensitivity level
	 */
	public void setSensitivity(double value) {
		sensitivity[evaluationAlgorithm] = (value < 0.25) ? 0.25
				: (value > 1) ? 1 : value;
	}

	/**
	 * Returns actual flag. Flag describes which metrics will be evaluated.
	 * 
	 * @return actual flag.
	 */
	public int getFlag() {
		return flag;
	}

	/**
	 * Sets flag used for evaluation process. Flag describes which metrics will
	 * be evaluated.
	 * 
	 * @param value
	 *            new flag value
	 */
	public void setFlag(int value) {
		flag = (value < 0) ? 0 : value;
	}

	/**
	 * Returns actual n-graph count.
	 * 
	 * @return n-graphs count
	 */
	public int getGraphs() {
		return graphs;
	}

	/**
	 * Sets number of n-graphs.
	 * 
	 * @param value
	 *            number of n-graphs
	 */
	public void setGraphs(int value) {
		graphs = (value < 0) ? 0 : value;
	}

	/**
	 * Returns actual P-threshold.
	 * 
	 * @return p-threshold
	 */
	public double getThresholdP() {
		return pThreshold;
	}

	/**
	 * Sets P-threshold used in T-tests. P-threshold will be set in range <0,
	 * 0.5>.
	 * 
	 * @param value
	 *            new threshold value
	 */
	public void setThresholdP(double value) {
		pThreshold = (value < 0) ? 0 : (value > 0.5) ? 0.5 : value;
	}
}
