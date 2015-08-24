package biosecLogger.charts;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import biosecLogger.analysis.Analyzer;
import biosecLogger.analysis.ExUserModel;
import biosecLogger.core.GrubbsTest;
import biosecLogger.core.OptionsManager;

/**
 * Loads and stores users with experimental samples and values. 
 * 
 * @author Stefan Smihla
 *
 */
public class Datasets {
	
	public final static int FLYINGTIMES = 0;
	public final static int ACCELERANCE_X = 1;
	public final static int ACCELERANCE_Y = 2;
	public final static int ACCELERANCE_Z = 3;
	public final static int ORIENTATION_X = 4;
	public final static int ORIENTATION_Y = 5;
	public final static int ORIENTATION_Z = 6;
	
	public final static int AVERAGES = -1;
	public final static int DEVIATIONS = -2;

	private List<ExUserModel> internalUsers;
	private List<ExUserModel> simpleUsers;
	private List<ExUserModel> complexUsers;
	
	/**
	 * Loads data from source directories.
	 */
	public Datasets(Context ctx) {
		Analyzer an = new Analyzer(null);
		internalUsers = an.loadUsersOnly(ctx);
		simpleUsers = an.loadUsersOnly("biosec_data/1", "vcelimed");
		complexUsers = an.loadUsersOnly("biosec_data/2", "l3kvarov@strudla");
	}
	
	/**
	 * Applies grubbs test for xy values. Also removes bad samples.
	 * 
	 * @param values
	 * 			source values
	 * @return	cleaned values
	 */
	private List<List<Double>> applyGrubbs(List<List<Double>> values) {
		List<List<Double>> result = ExUserTemplate.computeAverageWithDeviation(values);
		List<List<Double>> grubbsValues = GrubbsTest.grubbsOutlierCorrection(
				values, result.get(0), result.get(1));
		List<List<Double>> newValues = new ArrayList<List<Double>>();
		
		for (List<Double> row : grubbsValues){
			if (row.indexOf(null) == -1){
				newValues.add(row);
			}
		}
		
		return newValues;
	}
		
	/**
	 * Returns count of users.
	 * 
	 * @return	number of users
	 */
	public int getUserCount(){
		return simpleUsers.size();
	}
	
	/**
	 * Returns user names
	 * 
	 * @return	user names
	 */
	public String[] getUserNames(boolean experimental){
		List<ExUserModel> users;
		if (experimental){
			users = simpleUsers;
		} else {
			users = internalUsers;
		}
		
		String[] names = new String[users.size()];
		
		for (int i = 0; i < users.size(); i++) {
			names[i] = users.get(i).getUsername();
		}
		
		return names;
	}
	
	/**
	 * Returns extracted values on specific position and settings for single user.
	 * 
	 * @param userIndex
	 * 				index of user in templates
	 * @param characteristic
	 * 				constant for characteristic
	 * @param userTypes
	 * 				0 for internal users, 1 for simple phrase and 2 for complex phrase
	 * @param grubbs
	 * 				if true, applies Grubbs test cleanup
	 * @return	xy values
	 */
	private List<List<Double>> _getExtractedValues(int userIndex, int characteristic, int userTypes, boolean grubbs){
		List<List<Double>> values;
		ExUserModel user = null;
		
		switch (userTypes) {
			case 0:
				user = internalUsers.get(userIndex);
				break;
			case 1:
				user = simpleUsers.get(userIndex);
				break;
			case 2:
				user = complexUsers.get(userIndex);
				break;
			default:
				return null;
		}
		
		int flag;
		
		if (characteristic == FLYINGTIMES){
			flag = OptionsManager.FLYINGTIMES;
		} else if (characteristic >= ACCELERANCE_X && characteristic <= ACCELERANCE_Z){
			flag = OptionsManager.ACCELERANCE;
		} else if (characteristic >= ORIENTATION_X && characteristic <= ORIENTATION_Z){
			flag = OptionsManager.ORIENTATION;
		} else {
			return null;
		}
		
		ExUserTemplate userTemplate = new ExUserTemplate(user, flag);
		
		switch (characteristic){
			case FLYINGTIMES:
				values = userTemplate.getFlyingTimes();
				break;
			case ACCELERANCE_X:
				values = userTemplate.getAccs(ExUserTemplate.getXconst());
				break;
			case ACCELERANCE_Y:
				values = userTemplate.getAccs(ExUserTemplate.getYconst());
				break;
			case ACCELERANCE_Z:
				values = userTemplate.getAccs(ExUserTemplate.getZconst());
				break;
			case ORIENTATION_X:
				values = userTemplate.getAxises(ExUserTemplate.getXconst());
				break;
			case ORIENTATION_Y:
				values = userTemplate.getAxises(ExUserTemplate.getYconst());
				break;
			case ORIENTATION_Z:
				values = userTemplate.getAxises(ExUserTemplate.getZconst());
				break;
			default:
				return null;
		}
		
		if(grubbs){
			values = applyGrubbs(values);
		}
		
		return values;
	}
	
	/**
	 * Returns extracted values on specific position and settings.
	 * 
	 * @param userIndex
	 * 				index of user in templates
	 * @param characteristic
	 * 				constant for characteristic
	 * @param userTypes
	 * 				0 for internal users, 1 for simple phrase and 2 for complex phrase
	 * @param grubbs
	 * 				if true, applies Grubbs test cleanup
	 * @return	xy values
	 */
	public List<List<Double>> getExtractedValues(int userIndex, int characteristic, int userTypes, boolean grubbs){
		List<List<Double>> values;
		List<List<Double>> computedValues = new ArrayList<List<Double>>();
		
		switch (userIndex){
			case AVERAGES:
				userIndex = 0;
				break;
			case DEVIATIONS:
				userIndex = 1;
				break;
			default:
				return _getExtractedValues(userIndex, characteristic, userTypes, grubbs);
		}
		
		int size;
		switch (userTypes) {
			case 0:
				size = internalUsers.size();
				break;
			case 1:
				size = simpleUsers.size();
				break;
			case 2:
				size = complexUsers.size();
				break;
			default:
				return null;
		}
			
		for (int i = 0; i < size; i++){
			values = _getExtractedValues(i, characteristic, userTypes, grubbs);
			List<Double> result = ExUserTemplate.computeAverageWithDeviation(values).get(userIndex);
			computedValues.add(result);
		}
			
		return computedValues;
	}
}