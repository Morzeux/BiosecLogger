package biosecLogger.analysis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import biosecLogger.core.UserLoggerManager;

/**
 * Result class contains individual FAR and FRR per user, total FAR and FRR. It
 * is supposed to show and store analyzed results.
 * 
 * @author Stefan Smihla
 * 
 */
public class Result {

	private int users;

	private double[] farByUser;
	private double[] frrByUser;

	private double far;
	private double frr;

	private String phrase;

	/**
	 * Construct result from individual FAR and FRR and for specific phrase.
	 * Total FAR and FRR will be computed as well.
	 * 
	 * @param farByUser
	 *            false acceptance rate per user
	 * @param frrByUser
	 *            false rejection rate per user
	 * @param phrase
	 *            password phrase
	 */
	protected Result(double[] farByUser, double[] frrByUser, String phrase) {

		this.phrase = phrase;
		this.farByUser = farByUser;
		this.frrByUser = frrByUser;
		users = farByUser.length;

		far = 0;
		frr = 0;

		for (int i = 0; i < users; i++) {
			far += farByUser[i];
			frr += frrByUser[i];
		}

		far = (far / users) * 100;
		frr = (frr / users) * 100;
	}

	/**
	 * Returns JSONArray from input array values.
	 * 
	 * @param values
	 *            array of values
	 * @return converted JSONArray
	 */
	private JSONArray getJSONArray(double[] values) {
		JSONArray json = new JSONArray();
		for (double value : values) {
			try {
				json.put(value);
			} catch (JSONException e) {
				Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			}
		}
		return json;
	}

	/**
	 * Returns password phrase.
	 * 
	 * @return password phrase
	 */
	public String getPhrase() {
		return phrase;
	}

	/**
	 * Returns array of FARs per user.
	 * 
	 * @return array of FARs
	 */
	public double[] getFarByUser() {
		return farByUser;
	}

	/**
	 * Returns array of FRRs per user.
	 * 
	 * @return array of FRRs
	 */
	public double[] getFrrByUser() {
		return frrByUser;
	}

	/**
	 * Returns computed total FAR.
	 * 
	 * @return total FAR
	 */
	public double getFar() {
		return far;
	}

	/**
	 * Returns computed total FRR.
	 * 
	 * @return total FRR
	 */
	public double getFrr() {
		return frr;
	}

	/**
	 * Returns number of evaluated users.
	 * 
	 * @return users count
	 */
	public int getUserCount() {
		return farByUser.length;
	}

	/**
	 * Returns result as JSON object.
	 * 
	 * @return result as JSON
	 */
	protected JSONObject getJsonObject() {
		JSONObject json = new JSONObject();

		try {
			json.put("phrase", getPhrase());
			json.put("farByUser", getJSONArray(getFarByUser()));
			json.put("frrByUser", getJSONArray(getFrrByUser()));

			json.put("far", getFar());
			json.put("frr", getFrr());
		} catch (JSONException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			return null;
		}

		return json;
	}
}
