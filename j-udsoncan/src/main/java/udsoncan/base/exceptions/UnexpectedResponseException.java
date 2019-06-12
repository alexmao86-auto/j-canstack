package udsoncan.base.exceptions;

import udsoncan.Response;

public class UnexpectedResponseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnexpectedResponseException() {
		// TODO Auto-generated constructor stub
	}

	public UnexpectedResponseException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnexpectedResponseException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UnexpectedResponseException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnexpectedResponseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public UnexpectedResponseException(Response response, String msg) {
		super(msg);
	}

}
