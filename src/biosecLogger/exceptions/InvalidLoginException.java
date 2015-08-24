package biosecLogger.exceptions;

/**
 * Raises when login name or password is wrong.
 * 
 * @author Stefan Smihla
 * 
 */
public class InvalidLoginException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidLoginException() {
		super("Invalid login or password!");
	}
}
