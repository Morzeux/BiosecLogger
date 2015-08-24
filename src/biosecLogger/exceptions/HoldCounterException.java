package biosecLogger.exceptions;

/**
 * Exception raises when wrong hold count (less than create count) is set.
 * 
 * @author Stefan Smihla
 * 
 */
public class HoldCounterException extends Exception {

	private static final long serialVersionUID = 1L;

	public HoldCounterException() {
		super("Hold counter can't be less than create counter!");
	}

}
