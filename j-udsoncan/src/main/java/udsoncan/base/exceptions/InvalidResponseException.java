package udsoncan.base.exceptions;

import udsoncan.Response;

public class InvalidResponseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidResponseException() {
		// TODO Auto-generated constructor stub
	}

	public InvalidResponseException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InvalidResponseException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidResponseException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidResponseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public InvalidResponseException(Response response) {
		// TODO Auto-generated constructor stub
	}

	public InvalidResponseException(Response response, String string) {
		// TODO Auto-generated constructor stub
	}

}
