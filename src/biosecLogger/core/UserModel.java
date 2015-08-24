package biosecLogger.core;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import biosecLogger.exceptions.InvalidLoginException;

/**
 * Class contains information about user and raw logged data.
 * 
 * @author Stefan Smihla
 * 
 */
public class UserModel {

	private String username;
	private String password;
	private int templateHoldCount;
	private int counter;
	
	private List<List<LoggedKey>> loggedKeys;

	/**
	 * Creates new instance of UserModel class.
	 * 
	 * @param username
	 *            name of user
	 * @param password
	 *            encrypted password
	 * @param templateHoldCount
	 *            number of maximum samples to hold
	 */
	protected UserModel(String username, String password, int templateHoldCount) {
		this.username = username;
		this.password = password;
		this.templateHoldCount = templateHoldCount;
		this.counter = 0;
		
		this.loggedKeys = new ArrayList<List<LoggedKey>>();
	}

	/**
	 * Increases samples counter.
	 */
	protected void incrCounter() {
		counter++;
	}

	/**
	 * Decreases samples counter.
	 */
	protected void decrCounter() {
		counter--;
	}

	/**
	 * Returns actual samples count.
	 * 
	 * @return number of samples
	 */
	protected int getCounter() {
		return counter;
	}

	/**
	 * Returns raw logged keys.
	 * 
	 * @return logged keys
	 */
	protected List<List<LoggedKey>> getLoggedKeys() {
		return loggedKeys;
	}

	/**
	 * Adds new sample to logged samples.
	 */
	protected void addRow(List<LoggedKey> row) {
		while (counter >= templateHoldCount) {
			loggedKeys.remove(0);
			decrCounter();
		}

		loggedKeys.add(row);
		incrCounter();
	}

	/**
	 * Returns name of the user.
	 * 
	 * @return name of user
	 */
	protected String getUsername() {
		return username;
	}

	/**
	 * Validates if user name and password is valid.
	 * 
	 * @param username
	 *            name of user to validate
	 * @param password
	 *            password of user to validate
	 * @return true if correct else false
	 */
	protected boolean checkUser(String username, String password) {
		return (this.username.equals(username) && this.password
				.equals(password)) ? true : false;
	}

	/**
	 * Converts user data to JSON string.
	 * 
	 * @return JSON string
	 */
	protected String saveTemplateToString() {
		JSONObject json = new JSONObject();

		try {
			json.put("username", username);
			json.put("password", password);
			json.put("counter", counter);
			json.put("holdCount", templateHoldCount);
			json.put("samplesCount", loggedKeys.size());

			JSONArray jArray = new JSONArray();
			for (List<LoggedKey> row : loggedKeys) {
				JSONObject jSample = new JSONObject();

				JSONArray flyingTimes = new JSONArray();
				JSONArray errors = new JSONArray();
				JSONArray substitutions = new JSONArray();
				JSONArray orientations = new JSONArray();
				JSONArray pressures = new JSONArray();
				JSONArray xAxises = new JSONArray();
				JSONArray yAxises = new JSONArray();
				JSONArray zAxises = new JSONArray();
				JSONArray accXs = new JSONArray();
				JSONArray accYs = new JSONArray();
				JSONArray accZs = new JSONArray();

				for (LoggedKey key : row) {
					flyingTimes.put(key.getFlyingTime());
					errors.put(key.getError());
					substitutions.put(key.getLongPress());
					orientations.put(key.getOrientation());
					pressures.put(key.getPressure());
					xAxises.put(key.getAxisX());
					yAxises.put(key.getAxisY());
					zAxises.put(key.getAxisZ());
					accXs.put(key.getAccX());
					accYs.put(key.getAccY());
					accZs.put(key.getAccZ());
				}
				jSample.put("flyingTimes", flyingTimes);
				jSample.put("errors", errors);
				jSample.put("substitutions", substitutions);
				jSample.put("orientations", orientations);
				jSample.put("pressures", pressures);

				jSample.put("xAxises", xAxises);
				jSample.put("yAxises", yAxises);
				jSample.put("zAxises", zAxises);

				jSample.put("accXs", accXs);
				jSample.put("accYs", accYs);
				jSample.put("accZs", accZs);

				jArray.put(jSample);
			}

			json.put("samples", jArray);

		} catch (JSONException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
		}

		return json.toString();
	}

	/**
	 * Load user's template from JSON data.
	 * 
	 * @param template
	 *            template JSON string
	 * @param password
	 *            encrypted password with sha256
	 * @return true on success, false when data are wrong
	 * @throws InvalidLoginException
	 *             raises when password mismatch from template password
	 */
	protected boolean loadTemplateFromString(String template, String password)
			throws InvalidLoginException {
		JSONObject json;

		try {
			json = new JSONObject(template);

			username = json.getString("username");
			this.password = json.getString("password");

			if (!this.password.equals(password)) {
				throw new InvalidLoginException();
			}

			counter = json.getInt("counter");
			templateHoldCount = json.getInt("holdCount");

			JSONArray samples = json.getJSONArray("samples");

			for (int i = 0; i < json.getInt("samplesCount"); i++) {
				List<LoggedKey> rowValues = new ArrayList<LoggedKey>();
				JSONObject sample = samples.getJSONObject(i);

				JSONArray flyingTimes = sample.getJSONArray("flyingTimes");
				JSONArray errors = sample.getJSONArray("errors");
				JSONArray substitutions = sample.getJSONArray("substitutions");
				JSONArray orientations = sample.getJSONArray("orientations");
				JSONArray pressures = sample.getJSONArray("pressures");
				JSONArray xAxises = sample.getJSONArray("xAxises");
				JSONArray yAxises = sample.getJSONArray("yAxises");
				JSONArray zAxises = sample.getJSONArray("zAxises");
				JSONArray accXs = sample.getJSONArray("accXs");
				JSONArray accYs = sample.getJSONArray("accYs");
				JSONArray accZs = sample.getJSONArray("accZs");

				for (int j = 0; j < flyingTimes.length(); j++) {
					rowValues.add(new LoggedKey(flyingTimes.getLong(j),
							substitutions.getBoolean(j), errors.getBoolean(j),
							orientations.getInt(j), pressures.getDouble(j),
							xAxises.getDouble(j), yAxises.getDouble(j), zAxises
									.getDouble(j), accXs.getDouble(j), accYs
									.getDouble(j), accZs.getDouble(j)));
				}
				loggedKeys.add(rowValues);
			}
		} catch (JSONException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if user have logged orientation.
	 * 
	 * @return	true if yes, else false
	 */
	protected boolean checkOrientationLogging(){
		double sum = 0;
		for (LoggedKey key : getLoggedKeys().get(0)){
			sum += key.getAxisX() + key.getAxisY() + key.getAxisZ();
		}
		
		return (sum != 0.0) ? true : false;
	}
}
