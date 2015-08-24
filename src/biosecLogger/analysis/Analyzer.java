package biosecLogger.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import biosecLogger.core.LoggedKey;
import biosecLogger.core.LoginEvaluator;
import biosecLogger.core.OptionsManager;
import biosecLogger.core.StorageHandler;
import biosecLogger.core.UserLoggerManager;
import biosecLogger.core.UserModel;
import biosecLogger.exceptions.InvalidLoginException;

/**
 * Analyzer is supposed to test experimental samples obtained from volunteers.
 * 
 * @author Stefan Smihla
 * 
 */
public class Analyzer {

	List<Results> resultsList;
	private OptionsManager oManager;
	private ExLoginEvaluator loginEvaluator;
	private ExStorageHandler storage;

	private List<UserModel> simpleUsers;
	private List<List<List<LoggedKey>>> simpleTestSamples;

	private List<UserModel> complexUsers;
	private List<List<List<LoggedKey>>> complexTestSamples;

	/**
	 * Extended StorageHandler to be accessible from analysis sub-package.
	 * 
	 * @see StorageHandler class
	 */
	private class ExStorageHandler extends StorageHandler {
		protected boolean exSaveFile(String filename, String content, Context ctx, boolean b64encode) throws IOException {
			return StorageHandler.saveFile(filename, content, ctx, b64encode);
		}

		protected String exLoadFile(String filename, Context ctx) throws FileNotFoundException, IOException {
			return StorageHandler.loadFile(filename, ctx);
		}
	};

	/**
	 * Extended LoginEvaluator to be accessible from analysis sub-package.
	 * 
	 * @see LoginEvaluator class
	 */
	private class ExLoginEvaluator extends LoginEvaluator {
		protected ExLoginEvaluator(OptionsManager oManager) {
			super(oManager);
		}

		protected boolean exCheckPatter(UserModel user, List<LoggedKey> row) {
			return super.checkPatter(user, row);
		}
	}
	
	/**
	 * Initialize instance of Analyzer.
	 * 
	 * @param oManager
	 *            evaluation accuracy settings
	 */
	public Analyzer(OptionsManager oManager){
		resultsList = new ArrayList<Results>();
		storage = new ExStorageHandler();
		
		loginEvaluator = new ExLoginEvaluator(oManager);
		this.oManager = oManager;
		
		simpleUsers = new ArrayList<UserModel>();
		complexUsers = new ArrayList<UserModel>();

		simpleTestSamples = new ArrayList<List<List<LoggedKey>>>();
		complexTestSamples = new ArrayList<List<List<LoggedKey>>>();
	}

	/**
	 * Add absolute path to file from external storage.
	 * 
	 * @param fileName
	 *            name of relative file or directory
	 * @return absolute path to file
	 */
	private String getPath(String fileName) {
		String dataLocation = Environment.getExternalStorageDirectory()
				.toString();
		return dataLocation + "/" + fileName;
	}

	/**
	 * Sha256 password encryption.
	 * 
	 * @param password
	 *            password in plain text
	 * @return encrypted password in sha256
	 */
	private String encrypt(String password) {
		return new String(Hex.encodeHex(DigestUtils.sha256(password.getBytes())));
	}

	/**
	 * Returns array of files in directory.
	 * 
	 * @param directory
	 *            name of directory
	 * @return array of files in directory
	 */
	private File[] getFiles(String directory) {
		File direct = new File(getPath(directory));
		return direct.listFiles();
	}

	/**
	 * Analyze experimental samples and returns results. Results contains FAR
	 * and FRR for each experimental user and evaluated phrase.
	 * 
	 * @param users
	 *            list of user models with experimental template
	 * @param testSamples
	 *            list of samples selected as experimental
	 * @param phrase
	 *            evaluated phrase
	 * @return Result object with FAR, FRR for each user
	 */
	private Result analyzeData(List<UserModel> users,
			List<List<List<LoggedKey>>> testSamples, String phrase) {
		int falseAccept[] = new int[users.size()];
		int incorrectTotal[] = new int[users.size()];
		int falseReject[] = new int[users.size()];
		int correctTotal[] = new int[users.size()];
		double far[] = new double[users.size()];
		double frr[] = new double[users.size()];

		for (List<List<LoggedKey>> userTestSamples : testSamples) {
			int index = testSamples.indexOf(userTestSamples);
			for (List<LoggedKey> testSample : userTestSamples) {
				for (UserModel user : users) {
					boolean result = loginEvaluator.exCheckPatter(user,
							testSample);

					if (index == users.indexOf(user)) {
						if (result == false) {
							falseReject[index]++;
						}
						correctTotal[index]++;
					} else {
						if (result == true) {
							falseAccept[index]++;
						}
						incorrectTotal[index]++;
					}
				}
			}
		}

		for (int i = 0; i < users.size(); i++) {
			far[i] = (double) falseAccept[i] / incorrectTotal[i];
			frr[i] = (double) falseReject[i] / correctTotal[i];
		}

		return new Result(far, frr, phrase);
	}
	
	/**
	 * Loads only users without analyze from internal storage.
	 * 
	 * @param ctx
	 *            activity context
	 * @return	list of loaded users
	 */
	public List<ExUserModel> loadUsersOnly(Context ctx) {
		List<ExUserModel> users = new ArrayList<ExUserModel>();
		File[] files = ctx.getFilesDir().listFiles();
		JSONObject json;
		String template, password;
		for (File file : files){
			ExUserModel user = new ExUserModel(null, null, 0);
			
			try {
				template = storage.exLoadFile(file.getName(), ctx);
				json = new JSONObject(template);
				password = json.getString("password");
				
				user.loadTemplateFromString(template, password);
				users.add(user);
			} catch (IOException e) {
				Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
			} catch (InvalidLoginException e) {
				Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			} catch (JSONException e) {
				/* Not correct file */
				continue;
			}
		}
		
		return users;
	}
	
	/**
	 * Loads only users without analyze.
	 * 
	 * @param plainPhrase
	 *            plain password phrase to be evaluated
	 * @param directory
	 *            directory which contains experimental data
	 * @return	list of loaded users
	 */
	public List<ExUserModel> loadUsersOnly(String directory, String plainPhrase) {
		List<ExUserModel> users = new ArrayList<ExUserModel>();
		
		File[] files = getFiles(directory);
		String phrase = encrypt(plainPhrase);

		Arrays.sort(files);
		for (File file : files) {
			ExUserModel user = new ExUserModel(null, null, 0);

			try {
				user.loadTemplateFromString(
						storage.exLoadFile(file.toString(), null), phrase);
				users.add(user);
			} catch (IOException e) {
				Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
				continue;
			} catch (InvalidLoginException e) {
				Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
				continue;
			}
		}
		
		return users;
	}
	
	/**
	 * Loads data from directory which contains experimental data for certain
	 * phrase.
	 * 
	 * @param users
	 *            empty list to fill with experimental user's template
	 * @param testSamples
	 *            empty list to fill with experimental test samples
	 * @param plainPhrase
	 *            plain password phrase to be evaluated
	 * @param directory
	 *            directory which contains experimental data
	 */
	private void loadData(List<UserModel> users,
			List<List<List<LoggedKey>>> testSamples, String plainPhrase,
			String directory) {
		if (users.size() == 0 && testSamples.size() == 0) {
			File[] files = getFiles(directory);
			String phrase = encrypt(plainPhrase);

			for (File file : files) {
				ExUserModel user = new ExUserModel(null, null,
						oManager.getTemplateHoldCounter());

				try {
					user.loadTemplateFromString(
							storage.exLoadFile(file.toString(), null), phrase);
				} catch (IOException e) {
					Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
					continue;
				} catch (InvalidLoginException e) {
					Log.w(UserLoggerManager.LOGNAME, e.getMessage(), e);
					continue;
				}

				if (!user.analyzeWhenOrientation(oManager.getFlag())){
					continue;
				}
				
				List<List<LoggedKey>> userTestSamples = new ArrayList<List<LoggedKey>>();

				for (int i = 15; i < user.getLoggedKeys().size(); i++) {
					userTestSamples.add(user.getLoggedKeys().get(i));
				}

				for (int i = 0; i < 5; i++) {
					user.getLoggedKeys().remove(0);
					user.getLoggedKeys().remove(user.getLoggedKeys().size() - 1);
				}

				users.add(user);
				testSamples.add(userTestSamples);
			}
		}
	}

	/**
	 * Runs complete analyze for experimental samples. Samples must be
	 * downloaded and located biosec_data directory in external storage.
	 * 
	 * @return Results object contains evaluated data
	 */
	public Results run() {
		loadData(simpleUsers, simpleTestSamples, "vcelimed", "biosec_data/1");
		loadData(complexUsers, complexTestSamples, "l3kvarov@strudla",
				"biosec_data/2");

		resultsList.add(new Results(analyzeData(simpleUsers, simpleTestSamples,
				"vcelimed"), analyzeData(complexUsers, complexTestSamples,
				"l3kvarov@strudla"), oManager));

		return resultsList.get(resultsList.size() - 1);
	}
	
	/**
	 * Returns last results from analyzer.
	 * 
	 * @return	Results object from last analyze
	 */
	public Results getResults() {
		return resultsList.get(resultsList.size() - 1);
	}

	/**
	 * Save results to file in JSON format.
	 * 
	 * @param fileName
	 *            name of output file
	 * @throws IOException
	 *             raises on IO problems
	 */
	public void saveToFile(String fileName) throws IOException {
		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();

		for (Results res : resultsList) {
			arr.put(res.getJsonObject());
		}

		try {
			json.put("results", arr);
		} catch (JSONException e) {
			Log.e(UserLoggerManager.LOGNAME, e.getMessage(), e);
			return;
		}

		storage.exSaveFile(fileName, json.toString(), null, false);
	}

}
