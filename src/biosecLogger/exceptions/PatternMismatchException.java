package biosecLogger.exceptions;

/**
 * Raises when biometric sample and biometric template does not match.
 * 
 * @author Stefan Smihla
 * 
 */
public class PatternMismatchException extends Exception {
	private static final long serialVersionUID = 1L;

	public PatternMismatchException() {
		super("Entered sample mismatch with user's pattern!");
	}
}
