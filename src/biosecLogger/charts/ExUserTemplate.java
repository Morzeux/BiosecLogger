package biosecLogger.charts;

import java.util.List;

import biosecLogger.core.UserModel;
import biosecLogger.core.UserTemplate;

/**
 * Extended class for UserTemplate. Reason is to access protected methods from different package.
 * 
 * @author Stefan Smihla
 * @see UserTemplate
 */
public class ExUserTemplate extends UserTemplate {
	
	protected ExUserTemplate(UserModel user, int flag) {
		super(user, flag, false);
	}
	
	protected static List<List<Double>> computeAverageWithDeviation(List<List<Double>> values) {
		return UserTemplate.computeAverageWithDeviation(values, null);
	}
		
	@Override
	protected List<List<Double>> getFlyingTimes() {
		return super.getFlyingTimes();
	}
		
	@Override
	protected List<List<Double>> getAccs(int axis) {
		return super.getAccs(axis);
	}
	
	@Override
	protected List<List<Double>> getAxises(int axis) {
		return super.getAxises(axis);
	}
	
	protected final static int getXconst(){
		return X_AXIS;
	}
	
	protected final static int getYconst(){
		return Y_AXIS;
	}
	
	protected final static int getZconst(){
		return Z_AXIS;
	}
}
