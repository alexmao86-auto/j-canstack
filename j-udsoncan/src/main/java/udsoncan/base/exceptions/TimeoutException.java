package udsoncan.base.exceptions;

/**
 * Simple extension of ``Exception`` with no additional property. Raised when a
 * timeout in the communication happens.
 */
public class TimeoutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 284957337112729386L;

	public TimeoutException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	

}
