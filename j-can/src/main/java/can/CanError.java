package can;

import java.io.IOException; 

/** Indicates an error with the CAN network. */
public class CanError extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7911583682890316588L;

	public CanError() {
		// TODO Auto-generated constructor stub
	}

	public CanError(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CanError(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public CanError(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
