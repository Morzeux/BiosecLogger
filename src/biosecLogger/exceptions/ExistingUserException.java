package biosecLogger.exceptions;

/**
 * Exception raises when user already exists in storage.
 * 
 * @author Stefan Smihla
 * 
 */
public class ExistingUserException extends Exception {

	private static final long serialVersionUID = 1L;

	public ExistingUserException() {
		super("User already exists!");
	}

}
