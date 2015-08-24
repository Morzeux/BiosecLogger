package biosecLogger.analysis;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import biosecLogger.core.OptionsManager;
import biosecLogger.core.UserLoggerManager;

/**
 * Results class contains results for single and complex phrase as well as
 * sensitivity and evaluation settings.
 * 
 * 
 * @author Stefan Smihla
 * 
 */
public class Results {

	private Result simplePassword;
	private Result complexPassword;

	private int graphs;
	private int flag;
	private int algorithm;
	private double pThreshold;
	private double sensitivity;

	/**
	 * Constructor consist from result for single phrase, complex phrase and
	 * Options Manager instance class.
	 * 
	 * @param simple
	 *            result object of single phrase
	 * @param complex
	 *            result object of complex phrase
	 * @param oManager
	 *            evaluations settings
	 */
	protected Results(Result simple, Result complex, OptionsManager oManager) {
		simplePassword = simple;
		complexPassword = complex;
		
		graphs = oManager.getGraphs();
		flag = oManager.getFlag();
		algorithm = oManager.getEvaluationAlgorithm();
		pThreshold = oManager.getThresholdP();
		sensitivity = oManager.getSensitivity();
	}

	/**
	 * Returns FAR for single phrase.
	 * 
	 * @return FAR for single phrase
	 */
	public double getSimpleFar() {
		return simplePassword.getFar();

	}

	/**
	 * Returns FRR for single phrase.
	 * 
	 * @return FRR for single phrase
	 */
	public double getSimpleFrr() {
		return simplePassword.getFrr();
	}

	/**
	 * Returns FAR for complex phrase.
	 * 
	 * @return FAR for complex phrase
	 */
	public double getComplexFar() {
		return complexPassword.getFar();
	}

	/**
	 * Returns FRR for complex phrase.
	 * 
	 * @return FRR for complex phrase
	 */
	public double getComplexFrr() {
		return complexPassword.getFrr();
	}

	/**
	 * Returns results as JSON object.
	 * 
	 * @return results as JSON
	 */
	public JSONObject getJsonObject() {
		JSONObject json = new JSONObject();

		try {
			json.put("simplePassword", simplePassword.getJsonObject());
			json.put("complexPassword", complexPassword.getJsonObject());
			json.put("users", simplePassword.getUserCount());
			json.put("graphs", graphs);
			json.put("flag", flag);
			json.put("algorithm", algorithm);
			json.put("pThreshold", pThreshold);
			json.put("sensitivity", sensitivity);
		} catch (JSONException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			return null;
		}

		return json;
	}
	
	/**
	 * Returns result object for simple password.
	 * 
	 * @return	Result object for simple password
	 */
	public Result getSimpleResult(){
		return simplePassword;
	}
	
	/**
	 * Returns result object for complex password.
	 * 
	 * @return	Result object for complex password
	 */
	public Result getComplexResult(){
		return complexPassword;
	}
}
