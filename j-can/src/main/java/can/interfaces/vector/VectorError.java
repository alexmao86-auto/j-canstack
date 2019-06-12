package can.interfaces.vector;

import can.CanError;

public class VectorError extends CanError {

	public int error_code;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3091904759848293614L;

	public VectorError() {
		// TODO Auto-generated constructor stub
	}

	public VectorError(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public VectorError(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public VectorError(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public VectorError(int error_code, String error_string, String function) {
		super(function + " failed " + error_string);
	}

}
