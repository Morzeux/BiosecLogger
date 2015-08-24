package biosecLogger.analysis;

import java.util.List;

import biosecLogger.core.LoggedKey;
import biosecLogger.core.OptionsManager;
import biosecLogger.core.UserModel;
import biosecLogger.exceptions.InvalidLoginException;

/**
 * Extended class for UserModel. Reason is to access protected methods from different package.
 * 
 * @author Stefan Smihla
 *
 */
public class ExUserModel extends UserModel {
	
	protected ExUserModel(String username, String password, int templateHoldCount) {
		super(username, password, templateHoldCount);
	}
	
	@Override
	public String getUsername(){
		return super.getUsername();
	}
	
	@Override
	protected boolean loadTemplateFromString(String template, String password)
			throws InvalidLoginException {
		return super.loadTemplateFromString(template, password);
	}
	
	@Override
	protected List<List<LoggedKey>> getLoggedKeys() {
		return super.getLoggedKeys();
	}
	
	/**
	 * Checks if orientation is on and if yes, checks if sample have logged orientation.
	 * Some devices do not log orientation.
	 * 
	 * @param flag
	 * 			evaluation flag
	 * @return	true if user have logged orientation, else false
	 */
	protected boolean analyzeWhenOrientation(int flag){
		if ((flag & OptionsManager.ORIENTATION) == OptionsManager.ORIENTATION 
				&& !checkOrientationLogging()){
			return false;
		} else {
			return true;
		}
	}
}
